import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.collections.ArrayDeque

enum class Signal {
    // Stack operations
    DataStackPush, DataStackPop, ReturnStackPush, ReturnStackPop,

    // Register operations
    LatchAR, LatchBR, LatchTOS, LatchPC, LatchMPCounter,

    // Multiplexers latch
    PCJumpTypeJZ,
    PCJumpTypeJN,
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
    ALUSum, ALUSub, ALUMul, ALUDiv, ALUMod, ALUAnd, ALUOr, ALUXor, ALUPlus1, ALUMinus1,
}

class ControlUnit(
    initPc: Int, private val dataPath: DataPath, returnStackSize: Int
) {
    var pc: Int = initPc
    private var mPc: Int = 0
    private var modelTick: Int = 0
    private val returnStack = ArrayDeque<Int>(returnStackSize)

    private val logger = KotlinLogging.logger {}

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
        /* DIV */
        /* 20 */ arrayOf(Signal.ALUMod, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 21 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* INC */
        /* 22 */ arrayOf(Signal.ALUPlus1, Signal.ALUSum, Signal.ALULeftOPZero,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DEC */
        /* 23 */ arrayOf(Signal.ALUMinus1, Signal.ALUSum, Signal.ALULeftOPZero,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DROP */
        /* 24 */ arrayOf(Signal.TOSSelectDS, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 25 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* DUP */
        /* 26 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* SWAP */
        /* 27 */ arrayOf(Signal.LatchBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 28 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 29 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 30 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* OVER */
        /* 31 */ arrayOf(Signal.LatchBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 32 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 33 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 34 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 35 */ arrayOf(Signal.LatchBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 36 */ arrayOf(Signal.DataStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 37 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectBR,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* OR */
        /* 38 */ arrayOf(Signal.ALUOr, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 39 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* AND */
        /* 40 */ arrayOf(Signal.ALUAnd, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 41 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* XOR */
        /* 42 */ arrayOf(Signal.ALUXor, Signal.ALULeftOPDataStack,
                    Signal.TOSSelectALU, Signal.LatchTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 43 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* JZ */
        /* 44 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeJZ,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 45 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 46 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 47 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* JN */
        /* 48 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeJN,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 49 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 50 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 51 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* JUMP */
        /* 52 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 53 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 54 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* CALL */
        /* 55 */ arrayOf(Signal.ReturnStackPush,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 56 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeTOS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 57 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 58 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* RET */
        /* 59 */ arrayOf(Signal.LatchPC, Signal.PCJumpTypeRET,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 60 */ arrayOf(Signal.ReturnStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero),
        /* IN */
        /* 61 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectInput,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterZero,
                    Signal.LatchPC, Signal.PCJumpTypeNext),
        /* OUT */
        /* 62 */ arrayOf(Signal.Output,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 63 */ arrayOf(Signal.DataStackPop,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 64 */ arrayOf(Signal.LatchTOS, Signal.TOSSelectDS,
                    Signal.LatchMPCounter, Signal.MicroProgramCounterNext),
        /* 65 */ arrayOf(Signal.DataStackPop,
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
        Opcode.MOD -> 20
        Opcode.INC -> 22
        Opcode.DEC -> 23
        Opcode.DROP -> 24
        Opcode.DUP -> 26
        Opcode.SWAP -> 27
        Opcode.OVER -> 31
        Opcode.OR -> 38
        Opcode.AND -> 40
        Opcode.XOR -> 42
        Opcode.JZ -> 44
        Opcode.JN -> 48
        Opcode.JUMP -> 52
        Opcode.CALL -> 55
        Opcode.RET -> 59
        Opcode.IN -> 61
        Opcode.OUT -> 62
        Opcode.HALT -> throw HaltedException()
        else -> throw UnknownOpcodeException() // WORD, etc..
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

    private fun generateTickLogString(): String =
        "TICK $modelTick -- MPC: $mPc / MicroInstruction: ${mProgram[mPc].joinToString()} \n" +
                "Stack: [${dataPath.tos} | ${dataPath.dataStack.takeLast(3).reversed().joinToString(", ")}]\n" +
                "Return stack: [${returnStack.takeLast(3).reversed().joinToString(", ")}]\n" +
                "PC: $pc AR: ${dataPath.ar} BR: ${dataPath.br}\n"

    private fun generateInstrLogString(): String = when (val currentInstr = dataPath.memory[pc]) {
        is MemoryCell.Instruction -> "NOW EXECUTING INSTRUCTION PC: $pc --> ${currentInstr.opcode}\n"
        is MemoryCell.OperandInstruction -> "NOW EXECUTING INSTRUCTION PC: $pc --> ${currentInstr.opcode} ${currentInstr.operand}\n"
        is MemoryCell.Data -> "NOW EXECUTING DATA INSTRUCTION PC: $pc --> value: ${currentInstr.value}. WATCH OUT!!!\n"
    }

    fun simulate() {
        try {
            while (true) {
                if (mPc == 0) {
                    // instruction changed
                    logger.info { generateInstrLogString() }
                }
                logger.info { generateTickLogString() }
                dispatchMicroInstruction(mProgram[mPc])
                updateTick()
            }
        } catch (_: HaltedException) {
            logger.info { "[HALTED] ${generateTickLogString()}" }
        }
    }

    private fun onSignalLatchPC(microcode: Array<Signal>) {
        if (Signal.PCJumpTypeNext in microcode) {
            pc++
        } else if (Signal.PCJumpTypeTOS in microcode) {
            pc = dataPath.tos
        } else if (Signal.PCJumpTypeJZ in microcode) {
            if (dataPath.tos == 0) pc = dataPath.dataStack.last()
            else pc++
        } else if (Signal.PCJumpTypeJN in microcode) {
            if (dataPath.tos < 0) pc = dataPath.dataStack.last()
            else pc++
        } else if (Signal.PCJumpTypeRET in microcode) {
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
        } else if (Signal.MicroProgramCounterZero in microcode) {
            mPc = 0
        } else if (Signal.MicroProgramCounterOpcode in microcode) {
            val memoryCell = dataPath.memory[dataPath.ar]
            mPc = opcodeToMpc(
                when (memoryCell) {
                    is MemoryCell.Instruction -> memoryCell.opcode
                    is MemoryCell.OperandInstruction -> memoryCell.opcode
                    else -> Opcode.WORD
                }
            )
        }
    }
}

class HaltedException : Throwable()

class UnknownOpcodeException : Throwable("Defined opcode is not implemented!")
