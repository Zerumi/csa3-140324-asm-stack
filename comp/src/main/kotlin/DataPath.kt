import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.collections.ArrayDeque

class DataPath(
    dataStackSize: Int,
    memoryInitialSize: Int,
    program: Array<MemoryCell>
) {
    private val logger = KotlinLogging.logger {}

    lateinit var controlUnit: ControlUnit
    lateinit var ioController: IOController

    val dataStack = ArrayDeque<Int>(dataStackSize)
    var tos = 0
    var br = 0
    var ar = 0
    val memory = Array<MemoryCell>(memoryInitialSize) { MemoryCell.Data() }

    init {
        for (i in program.indices) memory[i] = program[i]
    }

    private val alu = ALU(this)

    fun onSignalDataStackPush() {
        dataStack.addLast(tos)
    }

    fun onSignalDataStackPop() {
        dataStack.removeLast()
    }

    fun onSignalLatchAR(microcode: Array<Signal>) {
        if (Signal.ARSelectTOS in microcode) {
            ar = tos
        } else if (Signal.ARSelectPC in microcode) {
            ar = controlUnit.pc
        }
    }

    fun onSignalLatchTOS(microcode: Array<Signal>) {
        if (Signal.TOSSelectDS in microcode) {
            tos = dataStack.last()
        } else if (Signal.TOSSelectMemory in microcode) {
            tos = when (val cell = memory[ar]) {
                is MemoryCell.Data -> cell.value
                is MemoryCell.OperandInstruction -> cell.operand
                else -> 0 // UB
            }
        } else if (Signal.TOSSelectALU in microcode) {
            tos = alu.output(microcode)
        } else if (Signal.TOSSelectInput in microcode) {
            tos = ioController.input(tos)
        } else if (Signal.TOSSelectBR in microcode) {
            tos = br
        }
    }

    fun onSignalOutput() {
        ioController.output(tos, dataStack.last())
    }

    private fun generateMemoryWriteLog(): String = "MEMORY WRITTEN VALUE: AR: $ar <--- ${dataStack.last()}"

    fun onSignalMemoryWrite() {
        memory[ar] = MemoryCell.Data(dataStack.last()) // assert in memory[ar] was data, won't fix
        logger.info { generateMemoryWriteLog() }
    }

    fun onSignalLatchBR() {
        br = dataStack.last()
    }
}

class ALU(private val dataPath: DataPath) {
    fun output(microcode: Array<Signal>): Int {
        val rightOperand = dataPath.tos
        val leftOperand = if (Signal.ALULeftOPDataStack in microcode) {
            dataPath.dataStack.last()
        } else if (Signal.ALULeftOPZero in microcode) {
            0
        } else {
            0 // UB
        }

        var result = if (Signal.ALUSum in microcode) {
            leftOperand + rightOperand
        } else if (Signal.ALUSub in microcode) {
            leftOperand - rightOperand
        } else if (Signal.ALUMul in microcode) {
            leftOperand * rightOperand
        } else if (Signal.ALUDiv in microcode) {
            leftOperand / rightOperand
        } else if (Signal.ALUMod in microcode) {
            leftOperand % rightOperand
        } else if (Signal.ALUAnd in microcode) {
            leftOperand and rightOperand
        } else if (Signal.ALUOr in microcode) {
            leftOperand or rightOperand
        } else if (Signal.ALUXor in microcode) {
            leftOperand xor rightOperand
        } else {
            0 // UB
        }
        if (Signal.ALUPlus1 in microcode) {
            result++
        }
        if (Signal.ALUMinus1 in microcode) {
            result--
        }
        return result
    }
}

class IOController {
    private val connectedDevices = emptyMap<Int, IOUnit>().toMutableMap()

    private val logger = KotlinLogging.logger {}

    private fun generateIOOutputLog(port: Int, value: Int): String =
        "I/O OPERATION OCCURS (OUT): port: $port <--- value: $value"

    private fun generateIOInLog(port: Int, value: Int): String =
        "I/O OPERATION OCCURS (IN): port: $port ---> value: $value"

    fun input(port: Int): Int {
        val input = connectedDevices[port]!!.inputBuffer.first()
        connectedDevices[port]!!.inputBuffer.removeFirst()
        logger.info { generateIOInLog(port, input) }
        return input
    }

    fun output(port: Int, value: Int) {
        connectedDevices[port]!!.outputBuffer.add(value)
        logger.info { generateIOOutputLog(port, value) }
    }

    fun connectDevice(port: Int, ioUnit: IOUnit) {
        connectedDevices[port] = ioUnit
    }

    fun disconnectDevice(port: Int) {
        connectedDevices.remove(port)
    }

    fun disconnectDevice(ioUnit: IOUnit) {
        connectedDevices.remove(connectedDevices.filterValues { x -> x == ioUnit }.keys.first())
    }
}

class IOUnit(val inputBuffer: ArrayDeque<Int>) {
    val outputBuffer: MutableList<Int> = mutableListOf()
}
