package io.github.zerumi.csa3.comp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import io.github.zerumi.csa3.isa.readCode
import java.io.File
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.streams.toList

class BCompCLI : CliktCommand() {

    companion object {
        private const val UNINITIALIZED_MEMORY_SIZE = -1
        private const val DEFAULT_STACK_SIZE = 20
        private const val DEFAULT_MEMORY_SCALE = 1.1
    }

    private val programFile: Path by option(
        "-p",
        "--program-file",
        help = "Path to JSON program file for execution"
    ).path(
        mustExist = true,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to program file")

    private val inputFile: Path by option(
        "-i",
        "--input-file",
        help = "Path to standard input file"
    ).path(
        mustExist = true,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to standard input file")

    private val outputFile: Path by option(
        "-o",
        "--output-file",
        help = "Path to output file"
    ).path(
        mustExist = false,
        canBeFile = true,
        canBeDir = false
    ).prompt("Input path to standard output file")

    sealed class LogPolicy {
        data object LogPolicyStdout : LogPolicy()
        data class LogPolicyFile(val fp: Path) : LogPolicy()
    }

    private val outputCompLog: LogPolicy by mutuallyExclusiveOptions(
        option(
            "-stdout",
            "--log-stdout",
            help = "Use stdout for logging"
        ).convert { LogPolicy.LogPolicyStdout },
        option(
            "-l",
            "--log-file",
            help = "Use log file. Specify a file, or use /dev/null for suppress logs"
        ).convert { LogPolicy.LogPolicyFile(File(it).toPath()) }
    ).default(LogPolicy.LogPolicyStdout)

    private val memoryInitialSize: Int by option(
        "--memory-initial-size",
        help = "Memory initial size"
    ).int().default(UNINITIALIZED_MEMORY_SIZE)

    private val dataStackSize: Int by option(
        "--data-stack-size",
        help = "Data stack size"
    ).int().default(DEFAULT_STACK_SIZE)
    private val returnStackSize: Int by option(
        "--return-stack-size",
        help = "Return Stack size"
    ).int().default(DEFAULT_STACK_SIZE)

    override fun run() {
        val logTempFile = kotlin.io.path.createTempFile()

        when (outputCompLog) {
            is LogPolicy.LogPolicyStdout -> {
                System.setProperty("log.file.level", "off")
                System.setProperty("logfile.name", logTempFile.toAbsolutePath().toString())
            }

            is LogPolicy.LogPolicyFile -> {
                System.setProperty("log.console.level", "off")
                System.setProperty("log.file.level", "debug")
                System.setProperty(
                    "logfile.name",
                    (outputCompLog as LogPolicy.LogPolicyFile).fp.toAbsolutePath().toString()
                )
                (outputCompLog as LogPolicy.LogPolicyFile).fp.toFile().printWriter().use { "" }
            }
        }

        val program = readCode(programFile)

        val finalMemoryInitSize: Int = if (memoryInitialSize == UNINITIALIZED_MEMORY_SIZE) {
            (program.program.size * DEFAULT_MEMORY_SCALE).toInt()
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

        dataPath.ioController.disconnectDevice(ioUnit)

        ioUnit.outputBuffer.removeIf { x -> x == 0 }

        outputFile.toFile().printWriter().use { out ->
            out.print(ioUnit.outputBuffer.map { x -> Char(x) }.joinToString(separator = ""))
        }

        logTempFile.deleteIfExists()
    }
}
