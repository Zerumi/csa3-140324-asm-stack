import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

fun readCode(filename: String): Program {
    val text = File(filename).readText(Charsets.UTF_8)

    val jsonInstructions = Json.decodeFromString<Program>(text)
    return jsonInstructions
}

fun writeCode(program: Program, filename: String) {
    val jsonText = Json.encodeToString(program)
    File(filename).writeText(jsonText)
}
