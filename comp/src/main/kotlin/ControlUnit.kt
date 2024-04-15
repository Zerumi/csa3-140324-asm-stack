import kotlin.collections.ArrayDeque
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

    // I/O, Memory
    MemoryWrite, Output,

    // ALU operations
    ALUSum, ALUSub, ALUMul, ALUDiv, ALUAnd, ALUOr, ALUXor,
}

class ControlUnit(
    initPc: Int,
    private val dataPath: DataPath,
    returnStackSize: Int)
{
    var pc: Int = initPc
    private var mPc: Int = 0
    private var modelTick: Int = 0
    private val returnStack = ArrayDeque<Int>(returnStackSize)

    val mProgram = arrayOf(
        /* Instruction fetch */
        /* 0 */ arrayOf(Signal.LatchAR, Signal.ARSelectPC,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 1 */ arrayOf(Signal.LatchMPCounter, Signal.MicroProgramCounterOpcode),
        /* NOP */
        /* 2 */ arrayOf(
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
        /* LIT */
        /* 3 */ arrayOf(Signal.DataStackPush, Signal.LatchAR, Signal.ARSelectPC,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 4 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectMemory,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
        /* LOAD */
        /* 5 */ arrayOf(Signal.LatchAR, Signal.ARSelectTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 6 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectMemory,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
        /* STORE */
        /* 7 */ arrayOf(Signal.LatchAR, Signal.ARSelectTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 8 */ arrayOf(Signal.MemoryWrite,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 9 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 10 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 11 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
        /* ADD */
        /* 12 */ arrayOf(Signal.ALUSum, Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 13 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.JumpTypeNext),
    )

    private fun opcodeToMpc(opcode: Opcode): Int = when (opcode) {
        Opcode.NOP -> 2
        Opcode.LIT -> 3
        Opcode.LOAD -> 5
        Opcode.STORE -> 7
        Opcode.ADD -> 12
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
        Opcode.HALT -> throw HaltedException()
        else -> exitProcess(0)
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

    private fun updateTick() {
        modelTick++
    }

    fun simulate() {
        try {
            while (true) {
                dispatchMicroInstruction(mProgram[mPc])
                updateTick()
                println("tick...")
            }
        } catch (_: HaltedException) {
            println("halted")
        }
    }

    private fun onSignalLatchPC(microcode: Array<Signal>) {
        if (Signal.JumpTypeNext in microcode) {
            pc++
        }
        else if (Signal.JumpTypeJUMP in microcode) {
            pc = dataPath.memory[dataPath.ar].operand
        }
        else if (Signal.JumpTypeJZ in microcode) {
            if (dataPath.tos == 0)
                pc = dataPath.memory[dataPath.ar].operand
            else
                pc++
        }
        else if (Signal.JumpTypeRET in microcode) {
            pc = returnStack.last()
        }
    }

    private fun onSignalReturnStackPop() {
        returnStack.removeLast()
    }

    private fun onSignalReturnStackPush() {
        returnStack.addLast(dataPath.memory[dataPath.ar].operand)
    }

    private fun onSignalLatchMPCounter(microcode: Array<Signal>) {
        if (Signal.MicroProgramCounterNext in microcode) {
            mPc++
        }
        else if (Signal.MicroProgramCounterZero in microcode) {
            mPc = 0
        }
        else if (Signal.MicroProgramCounterOpcode in microcode) {
            mPc = opcodeToMpc(dataPath.memory[dataPath.ar].opcode)
        }
    }
}

class HaltedException : Throwable()
