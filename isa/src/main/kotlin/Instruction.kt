import kotlinx.serialization.*

/**
 * Data class that holds an instruction.
 * Instruction represents by its opcode (from isa) and possible operand
 *
 * Data classes in Kotlin is just simplification of classes,
 * whose objects only holds some values
 */

@Serializable
data class Instruction(val opcode: Opcode, var operand: Int = 0)