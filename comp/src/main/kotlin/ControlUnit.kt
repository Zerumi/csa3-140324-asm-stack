import java.util.*
import kotlin.system.exitProcess

enum class Signal {
    // Stack operations
    DataStackPush, DataStackPop, ReturnStackPush, ReturnStackPop,

    // Register operations
    LatchAR, LatchTOS, LatchPC, LatchMPCounter,

    // Multiplexers latch
    JumpTypeJZ,
    JumpTypeJUMP,
    JumpTypeRET,
    MicroProgramCounterZero,
    MicroProgramCounterOpcode,
    MicroProgramCounterNext,
    TOSSelectInput,
    TOSSelectMemory,
    TOSSelectDS,
    TOSSelectALU,

    // I/O
    MemoryWrite, Output,

    // ALU operations
    ALUSum, ALUSub, ALUMul, ALUDiv, ALUAnd, ALUOr, ALUXor,
}

class ControlUnit(val initPc: Int, val program: Array<Instruction>) {
    var pc: Int = 0
    var mPc: Int = 0
    val dataPath = DataPath()
    var modelTick: Int = 0
    val returnStack = Stack<Int>()

    val mProgram = arrayOf(
        /* Instruction fetch */
        /* 0 */ arrayOf(Signal.LatchMPCounter, Signal.MicroProgramCounterOpcode),
        /* LOAD */
        /* 1 */ arrayOf(Signal.DataStackPush, Signal.TOSSelectMemory, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* STORE */
        /* 2 */ arrayOf(Signal.LatchAR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 3 */ arrayOf(Signal.MemoryWrite, Signal.TOSSelectDS, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 4 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* ADD */
        /* 5 */ arrayOf(Signal.ALUSum, Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 6 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
    )

    fun opcodeToMpc(opcode: Opcode): Int = when (opcode) {
        Opcode.LOAD -> 1
        Opcode.STORE -> 2
        Opcode.ADD -> 5
        Opcode.SUB -> TODO()
        Opcode.DROP -> TODO()
        Opcode.DUP -> TODO()
        Opcode.OR -> TODO()
        Opcode.AND -> TODO()
        Opcode.XOR -> TODO()
        Opcode.JZ -> TODO()
        Opcode.JUMP -> TODO()
        Opcode.CALL -> TODO()
        Opcode.RET -> TODO()
        Opcode.IN -> TODO()
        Opcode.OUT -> TODO()
        Opcode.HALT -> TODO()
        else -> exitProcess(-1)
    }
}
