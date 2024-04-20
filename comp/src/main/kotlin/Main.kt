import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.streams.toList

class BCompCLI : CliktCommand() {

    private val programFile: Path by option(help = "Path to JSON program file for execution").path(
        mustExist = true,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to program file")

    private val inputFile: Path by option(help = "Path to standard input file").path(
        mustExist = true,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to standard input file")

    private val outputFile: Path by option(help = "Path to output file").path(
        mustExist = false,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to standard output file")

    private val outputCompLog: Path by option(help = "Path to computer log file").path(
        mustExist = false,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to computer log file")

    private val memoryInitialSize: Int by option(help = "Memory initial size").int().default(-1)

    private val dataStackSize: Int by option(help = "Data stack size").int().default(20)
    private val returnStackSize: Int by option(help = "Return Stack size").int().default(20)

    override fun run() {

        val program = readCode(programFile)

        val finalMemoryInitSize: Int = if (memoryInitialSize == -1) {
            (program.program.size * 1.1).toInt()
        } else memoryInitialSize

        val inputBuffer = ArrayDeque(inputFile.readText(Charsets.UTF_8).chars().toList())
        inputBuffer.addLast(0)

        val dataPath = DataPath(dataStackSize, finalMemoryInitSize, program.program)
        val ioController = IOController()
        val ioUnit = IOUnit(inputBuffer)
        ioController.connectDevice(1, ioUnit)
        val controlUnit = ControlUnit(program.initCommand, dataPath, returnStackSize)
        dataPath.controlUnit = controlUnit
        dataPath.ioController = ioController

        controlUnit.simulate()
        ioUnit.outputBuffer.removeIf { x -> x == 0 }

        outputFile.toFile().printWriter().use { out ->
            out.println(ioUnit.outputBuffer.map { x -> Char(x) }.joinToString(separator = ""))
        }
    }
}

fun main(args: Array<String>) {
    BCompCLI().main(args)
}