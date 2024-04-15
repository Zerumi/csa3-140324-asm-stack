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
    JumpTypeNext,
    MicroProgramCounterZero,
    MicroProgramCounterOpcode,
    MicroProgramCounterNext,
    TOSSelectInput,
    TOSSelectMemory,
    TOSSelectDS,
    TOSSelectALU,
    ARSelectTOS,
    ARSelectPC,

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
        /* LIT */
        /* 1 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 2 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectMemory,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* LOAD */
        /* 3 */ arrayOf(Signal.LatchAR, Signal.ARSelectTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 4 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectMemory,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
        /* STORE */
        /* 5 */ arrayOf(Signal.LatchAR, Signal.ARSelectTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 6 */ arrayOf(Signal.MemoryWrite,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 7 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 8 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 9 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
        /* ADD */
        /* 10 */ arrayOf(Signal.ALUSum, Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 11 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
    )

    fun opcodeToMpc(opcode: Opcode): Int = when (opcode) {
        Opcode.LIT -> 1
        Opcode.LOAD -> 3
        Opcode.STORE -> 5
        Opcode.ADD -> 10
        Opcode.SUB -> TODO()
        Opcode.INC -> TODO()
        Opcode.DEC -> TODO()
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

    fun dispatchMicroInstruction(microcode: Array<Signal>) {
        for (signal in microcode) {
            when (signal) {
                Signal.DataStackPush -> dataPath.onSignalDataStackPush()
                Signal.DataStackPop -> dataPath.onSignalDataStackPop()
                Signal.ReturnStackPush -> this.onSignalReturnStackPush()
                Signal.ReturnStackPop -> this.onSignalReturnStackPop()
                Signal.LatchAR -> dataPath.onSignalLatchAR(microcode)
                Signal.LatchTOS -> dataPath.onSignalLatchTOS(microcode)
                Signal.LatchPC -> this.onSignalLatchPC(microcode)
                Signal.LatchMPCounter -> this.onSignalLatchMPCounter(microcode)
                Signal.MemoryWrite -> dataPath.onSignalMemoryWrite()
                Signal.Output -> dataPath.onSignalOutput()
                else -> Unit
            }
        }
    }

    private fun onSignalLatchPC(microcode: Array<Signal>) {
        TODO("Not yet implemented")
    }

    private fun onSignalReturnStackPop() {
        TODO("Not yet implemented")
    }

    private fun onSignalReturnStackPush() {
        TODO("Not yet implemented")
    }

    private fun onSignalLatchMPCounter(microcode: Array<Signal>) {
        TODO("Not yet implemented")
    }
}
