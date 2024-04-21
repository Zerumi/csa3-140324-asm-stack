import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.io.path.fileSize

class AlgorithmTest {

    @Test
    fun catTest() {
        abstractAlgorithmTest("cat_golden")
    }

    @Test
    fun helloTest() {
        abstractAlgorithmTest("hello_golden")
    }

    @Test
    fun helloUserNameTest() {
        abstractAlgorithmTest("hello_username_golden")
    }

    @Test
    fun prob2Test() {
        abstractAlgorithmTest("prob2_golden")
    }

    private fun abstractAlgorithmTest(directoryName: String) {
        // load resources (assembly code)
        val classLoader = javaClass.classLoader
        val propFs = classLoader.getResourceAsStream("${directoryName}/comp.properties")!!.bufferedReader()
        val props = Properties()
        props.load(propFs)

        val asmFile = File(classLoader.getResource("${directoryName}/${props.getProperty("assembly")}")!!.file)
        val translatorOutput = File.createTempFile("transtator_output_${props.getProperty("name")}", ".json")
        translatorOutput.deleteOnExit()

        // run translator
        io.github.zerumi.csa3.asm.main(
            arrayOf(
                asmFile.absolutePath,
                translatorOutput.absolutePath
            )
        )

        // prepare comp arguments
        val stdin = File(classLoader.getResource("${directoryName}/${props.getProperty("stdin")}")!!.file)
        val stdout = File(classLoader.getResource("${directoryName}/${props.getProperty("stdout")}")!!.file)
        val logFile = File(classLoader.getResource("${directoryName}/${props.getProperty("logfile")}")!!.file)

        // load expected results
        val stdoutExpected =
            File(
                classLoader.getResource("${directoryName}/${props.getProperty("expected_out")}")!!.file
            )
        val logFileExpected =
            File(
                classLoader.getResource("${directoryName}/${props.getProperty("expected_log")}")!!.file
            )

        // run comp
        io.github.zerumi.csa3.comp.main(
            arrayOf(
                "--program-file",
                translatorOutput.absolutePath,
                "--input-file",
                stdin.absolutePath,
                "--output-file",
                stdout.absolutePath,
                "--log-file",
                logFile.absolutePath.toString()
            )
        )

        if (System.getProperty("updateExpected").equals("true", ignoreCase = true)) {
            val outExpectedResourceFile = File(props.getProperty("expected_out_resource"))
            stdout.copyTo(outExpectedResourceFile, true)

            val logExpectedResourceFile = File(props.getProperty("expected_log_resource"))
            logExpectedResourceFile.printWriter().use { out -> out.println(logFile.toPath().fileSize()) }
        } else {
            // assert results
            assertEquals(stdout.readText(Charsets.UTF_8), stdoutExpected.readText(Charsets.UTF_8))
            assertEquals(logFile.readText(Charsets.UTF_8), logFileExpected.readText(Charsets.UTF_8))
        }
    }
}
