import java.io.File
import kotlin.streams.toList

fun main(args: Array<String>) {
    // input
    val programFile = args[0]
    val program = readCode(programFile)

    val inputFile = args[1]
    val inputFileContent = File(inputFile).readText()
    val inputBuffer = ArrayDeque(inputFileContent.chars().toList())
    inputBuffer.addLast(0)

    // solution
    val dataPath = DataPath(10, 20, program.program)
    val ioController = IOController()
    val ioUnit = IOUnit(inputBuffer)
    ioController.connectDevice(1, ioUnit)
    val controlUnit = ControlUnit(program.initCommand, dataPath, 10)
    dataPath.controlUnit = controlUnit
    dataPath.ioController = ioController

    controlUnit.simulate()

    // output
    val outputFile = args[2]
    ioUnit.outputBuffer.removeIf { x -> x == 0 }
    File(outputFile).printWriter().use { out ->
        out.println(ioUnit.outputBuffer.map{x -> Char(x)}.joinToString(separator = ""))
    }
}