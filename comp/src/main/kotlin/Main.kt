import java.io.File
import kotlin.streams.toList

fun main(args: Array<String>) {
    // input
    val programFile = args[0]
    val program = readCode(programFile)

    val inputFile = args[1]
    val inputBuffer = ArrayDeque(inputFile.chars().toList())

    // solution
    val dataPath = DataPath(10, 20, program, inputBuffer)
    val controlUnit = ControlUnit(0, dataPath, 10)
    dataPath.controlUnit = controlUnit

    controlUnit.simulate()

    // output
    val outputFile = args[2]
    File(outputFile).printWriter().use { out ->
        out.println(dataPath.outputBuffer.map{x -> Char(x)}.joinToString(separator = ""))
    }
}