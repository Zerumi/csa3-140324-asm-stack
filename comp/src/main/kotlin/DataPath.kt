import kotlin.collections.ArrayDeque

class DataPath(dataStackSize: Int,
               memoryInitialSize : Int,
               program: Array<Instruction>,
               private val inputBuffer : ArrayDeque<Int>) {

    lateinit var controlUnit: ControlUnit

    val dataStack = ArrayDeque<Int>(dataStackSize)
    var tos = 0
    var ar = 0
    val memory = Array(memoryInitialSize) { Instruction(Opcode.WORD) }

    init {
        for (i in program.indices)
            memory[i] = program[i]
    }

    private val alu = ALU(this)

    val outputBuffer = emptyList<Int>().toMutableList()

    fun onSignalDataStackPush() {
        dataStack.addLast(tos)
    }

    fun onSignalDataStackPop() {
        dataStack.removeLast()
    }

    fun onSignalLatchAR(microcode: Array<Signal>) {
        if (Signal.ARSelectTOS in microcode) {
            ar = tos
        }
        else if (Signal.ARSelectPC in microcode) {
            ar = controlUnit.pc
        }
    }

    fun onSignalLatchTOS(microcode: Array<Signal>) {
        if (Signal.TOSSelectDS in microcode) {
            tos = dataStack.last()
        }
        else if (Signal.TOSSelectMemory in microcode) {
            tos = memory[ar].operand
        }
        else if (Signal.TOSSelectALU in microcode) {
            tos = alu.output(microcode)
        }
        else if (Signal.TOSSelectInput in microcode) {
            tos = inputBuffer.first()
            inputBuffer.removeFirst()
        }
    }

    fun onSignalOutput() {
        outputBuffer.add(tos)
    }

    fun onSignalMemoryWrite() {
        memory[ar].operand = dataStack.last()
    }
}

class ALU(private val dataPath: DataPath) {
    fun output(microcode: Array<Signal>): Int =
        if (Signal.ALUSum in microcode) {
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
