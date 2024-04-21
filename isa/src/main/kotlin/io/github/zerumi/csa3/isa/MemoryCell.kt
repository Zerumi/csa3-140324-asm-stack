package io.github.zerumi.csa3.isa

import kotlinx.serialization.Serializable

/**
 * Memory cell may be an operand/no operand instruction, or data.
 *
 * Sealed classes are used in Kotlin to provide restricted inheritance mechanism.
 */
@Serializable
sealed class MemoryCell {
    @Serializable
    data class Instruction(val opcode: Opcode) : MemoryCell()

    @Serializable
    data class OperandInstruction(val opcode: Opcode, val operand: Int = 0) : MemoryCell()

    @Serializable
    data class Data(var value: Int = 0) : MemoryCell()
}
