import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path

fun readCode(filename: Path): Program {
    val text = filename.toFile().readText(Charsets.UTF_8)

    val jsonInstructions = Json.decodeFromString<Program>(text)
    return jsonInstructions
}

fun writeCode(program: Program, filename: String) {
    val jsonText = Json.encodeToString(program)
    File(filename).writeText(jsonText)
}
