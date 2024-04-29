package io.github.zerumi.csa3.isa

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
    data class OperandInstruction(
        val opcode: Opcode,
        @Serializable(with = NumberSerializer::class)
        val operand: Number = 0
    ) : MemoryCell()

    @Serializable
    data class Data(
        @Serializable(with = NumberSerializer::class)
        var value: Number = 0
    ) : MemoryCell()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Number::class)
class NumberSerializer : KSerializer<Number> {
    override fun deserialize(decoder: Decoder): Number {
        val s = decoder.decodeString()
        return if (s.toIntOrNull() != null) {
            s.toInt()
        } else {
            s.toFloat()
        }
    }

    override fun serialize(encoder: Encoder, value: Number) {
        encoder.encodeString(value.toString())
    }
}
