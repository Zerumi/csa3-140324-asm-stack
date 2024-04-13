import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

fun readCode(filename: String): Array<Instruction> {
    val text = File(filename).readText(Charsets.UTF_8)

    val jsonInstructions = Json.decodeFromString<Array<Instruction>>(text)
    return jsonInstructions
}

fun writeCode(instructions: Array<Instruction>, filename: String) {
    val jsonText = Json.encodeToString(instructions)
    File(filename).writeText(jsonText)
}
