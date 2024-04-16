import kotlin.collections.ArrayDeque

class DataPath(
    dataStackSize: Int, memoryInitialSize: Int, program: Array<Instruction>
) {

    lateinit var controlUnit: ControlUnit
    lateinit var ioController: IOController

    val dataStack = ArrayDeque<Int>(dataStackSize)
    var tos = 0
    var ar = 0
    val memory = Array(memoryInitialSize) { Instruction(Opcode.WORD) }

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
            tos = memory[ar].operand
        } else if (Signal.TOSSelectALU in microcode) {
            tos = alu.output(microcode)
        } else if (Signal.TOSSelectInput in microcode) {
            tos = ioController.input(tos)
        }
    }

    fun onSignalOutput() {
        ioController.output(tos, dataStack.last())
    }

    fun onSignalMemoryWrite() {
        memory[ar].operand = dataStack.last()
    }
}

class ALU(private val dataPath: DataPath) {
    fun output(microcode: Array<Signal>): Int = if (Signal.ALUSum in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else if (Signal.ALUSub in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else if (Signal.ALUMul in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else if (Signal.ALUDiv in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else if (Signal.ALUAnd in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else if (Signal.ALUOr in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else if (Signal.ALUXor in microcode) {
        dataPath.tos + dataPath.dataStack.last()
    } else {
        0 // UB
    }
}

class IOController {
    private val connectedDevices = emptyMap<Int, IOUnit>().toMutableMap()

    fun input(port: Int): Int {
        val input = connectedDevices[port]!!.inputBuffer.first()
        connectedDevices[port]!!.inputBuffer.removeFirst()
        return input
    }

    fun output(port: Int, value: Int) {
        connectedDevices[port]!!.outputBuffer.add(value)
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
