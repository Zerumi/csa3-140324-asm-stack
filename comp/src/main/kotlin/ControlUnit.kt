import kotlin.collections.ArrayDeque
import kotlin.system.exitProcess

enum class Signal {
    // Stack operations
    DataStackPush, DataStackPop, ReturnStackPush, ReturnStackPop,

    // Register operations
    LatchAR, LatchBR, LatchTOS, LatchPC, LatchMPCounter,

    // Multiplexers latch
    PCJumpTypeJZ,
    PCJumpTypeTOS,
    PCJumpTypeRET,
    PCJumpTypeNext,
    MicroProgramCounterZero,
    MicroProgramCounterOpcode,
    MicroProgramCounterNext,
    TOSSelectInput,
    TOSSelectMemory,
    TOSSelectDS,
    TOSSelectALU,
    TOSSelectBR,
    ARSelectTOS,
    ARSelectPC,
    ALULeftOPZero,
    ALULeftOPDataStack,

    // I/O, Memory
    MemoryWrite, Output,

    // ALU operations
    ALUSum, ALUSub, ALUMul, ALUDiv, ALUAnd, ALUOr, ALUXor, ALUPlus1, ALUMinus1,
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

    // wont fix: Latching parallel registers in sequential model of computation
    private val mProgram = arrayOf(
        /* Instruction fetch */
        /* 0 */ arrayOf(Signal.LatchAR, Signal.ARSelectPC,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 1 */ arrayOf(Signal.LatchMPCounter, Signal.MicroProgramCounterOpcode),
        /* NOP */
        /* 2 */ arrayOf(
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* LIT */
        /* 3 */ arrayOf(Signal.DataStackPush, Signal.LatchAR, Signal.ARSelectPC,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 4 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectMemory,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* LOAD */
        /* 5 */ arrayOf(Signal.LatchAR, Signal.ARSelectTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 6 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectMemory,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
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
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* ADD */
        /* 12 */ arrayOf(Signal.ALUSum, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 13 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* SUB */
        /* 14 */ arrayOf(Signal.ALUSub, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 15 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* MUL */
        /* 16 */ arrayOf(Signal.ALUMul, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 17 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DIV */
        /* 18 */ arrayOf(Signal.ALUDiv, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 19 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* INC */
        /* 20 */ arrayOf(Signal.ALUPlus1, Signal.ALUSum, Signal.ALULeftOPZero,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DEC */
        /* 21 */ arrayOf(Signal.ALUMinus1, Signal.ALUSum, Signal.ALULeftOPZero,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DROP */
        /* 22 */ arrayOf(Signal.TOSSelectDS, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 23 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DUP */
        /* 24 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* SWAP */
        /* 25 */ arrayOf(Signal.LatchBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 26 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 27 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 28 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* OVER */
        /* 29 */ arrayOf(Signal.LatchBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 30 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 31 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 32 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 33 */ arrayOf(Signal.LatchBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 34 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 35 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* OR */
        /* 36 */ arrayOf(Signal.ALUOr, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 37 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* AND */
        /* 38 */ arrayOf(Signal.ALUAnd, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 39 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* XOR */
        /* 40 */ arrayOf(Signal.ALUXor, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 41 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* JZ */
        /* 42 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeJZ,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 43 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 44 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 45 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* JUMP */
        /* 46 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 47 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 48 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* CALL */
        /* 49 */ arrayOf(Signal.ReturnStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 50 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* RET */
        /* 51 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeRET,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 52 */ arrayOf(Signal.ReturnStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* IN */
        /* 53 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectInput,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* OUT */
        /* 54 */ arrayOf(Signal.Output,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 55 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 56 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 57 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
    )

    private fun opcodeToMpc(opcode: Opcode): Int = when (opcode) {
        Opcode.NOP -> 2
        Opcode.LIT -> 3
        Opcode.LOAD -> 5
        Opcode.STORE -> 7
        Opcode.ADD -> 12
        Opcode.SUB -> 14
        Opcode.MUL -> 16
        Opcode.DIV -> 18
        Opcode.INC -> 20
        Opcode.DEC -> 21
        Opcode.DROP -> 22
        Opcode.DUP -> 24
        Opcode.SWAP -> 25
        Opcode.OVER -> 29
        Opcode.OR -> 36
        Opcode.AND -> 38
        Opcode.XOR -> 40
        Opcode.JZ -> 42
        Opcode.JUMP -> 46
        Opcode.CALL -> 49
        Opcode.RET -> 51
        Opcode.IN -> 53
        Opcode.OUT -> 54
        Opcode.HALT -> throw HaltedException()
        else -> exitProcess(0) // WORD, etc..
    }

    private fun dispatchMicroInstruction(microcode: Array<Signal>) {
        for (signal in microcode) {
            when (signal) {
                Signal.DataStackPush -> dataPath.onSignalDataStackPush()
                Signal.DataStackPop -> dataPath.onSignalDataStackPop()
                Signal.ReturnStackPush -> this.onSignalReturnStackPush()
                Signal.ReturnStackPop -> this.onSignalReturnStackPop()
                Signal.LatchAR -> dataPath.onSignalLatchAR(microcode)
                Signal.LatchBR -> dataPath.onSignalLatchBR()
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
        if (Signal.PCJumpTypeNext in microcode) {
            pc++
        }
        else if (Signal.PCJumpTypeTOS in microcode) {
            pc = dataPath.tos
        }
        else if (Signal.PCJumpTypeJZ in microcode) {
            if (dataPath.tos == 0)
                pc = dataPath.dataStack.last()
            else
                pc++
        }
        else if (Signal.PCJumpTypeRET in microcode) {
            pc = returnStack.last()
        }
    }

    private fun onSignalReturnStackPop() {
        returnStack.removeLast()
    }

    private fun onSignalReturnStackPush() {
        returnStack.addLast(this.pc + 1)
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
