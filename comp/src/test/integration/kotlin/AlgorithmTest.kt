import io.github.zerumi.csa3.isa.readCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Properties

/**
 * Important notice!
 * You shouldn't run these tests simultaneously!
 *
 * Example of correct test usage:
 * gradlew :comp:integrationTest --tests "AlgorithmTest.helloUserNameTest"
 */
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

    @Test
    fun facTest() {
        abstractAlgorithmTest("fac_golden")
    }

    private fun abstractAlgorithmTest(directoryName: String) {
        // load resources (assembly code)
        val classLoader = javaClass.classLoader
        val propFs = classLoader.getResourceAsStream("$directoryName/comp.properties")!!.bufferedReader()
        val props = Properties()
        props.load(propFs)

        val asmFile = File(classLoader.getResource("$directoryName/${props.getProperty("assembly")}")!!.file)
        val translatorOutput = File.createTempFile(props.getProperty("name"), ".json")
        translatorOutput.deleteOnExit()

        val expectedAssemblyCode = File(
            classLoader.getResource("$directoryName/${props.getProperty("expected_assembly")}")!!.file
        )

        // run translator
        io.github.zerumi.csa3.asm.main(
            arrayOf(
                asmFile.absolutePath,
                translatorOutput.absolutePath
            )
        )

        // prepare comp arguments
        val stdin = File(classLoader.getResource("$directoryName/${props.getProperty("stdin")}")!!.file)
        val stdout = File.createTempFile("${props.getProperty("name")}_stdout", ".txt")
        val logFile = File.createTempFile("${props.getProperty("name")}_log", ".txt")
        stdout.deleteOnExit()
        logFile.deleteOnExit()

        // load expected results
        val stdoutExpected =
            File(
                classLoader.getResource("$directoryName/${props.getProperty("expected_out")}")!!.file
            )
        val logFileExpected =
            File(
                classLoader.getResource("$directoryName/${props.getProperty("expected_log")}")!!.file
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
                logFile.absolutePath
            )
        )

        if (System.getProperty("updateGolden") == "true") {
            val assemblyExpectedResourceFile = File(props.getProperty("expected_assembly_resource"))
            translatorOutput.copyTo(assemblyExpectedResourceFile, true)

            val outExpectedResourceFile = File(props.getProperty("expected_out_resource"))
            stdout.copyTo(outExpectedResourceFile, true)

            val logExpectedResourceFile = File(props.getProperty("expected_log_resource"))
            logFile.copyTo(logExpectedResourceFile, true)
        } else {
            // assert results
            assertEquals(readCode(expectedAssemblyCode.toPath()), readCode(translatorOutput.toPath()))

            val stdoutText = stdout.readText(Charsets.UTF_8)
            val stdoutExpectedText = stdoutExpected.readText(Charsets.UTF_8)
            assertEquals(stdoutText, stdoutExpectedText)

            val logText = logFile.readText(Charsets.UTF_8)
            val logExpectedText = logFileExpected.readText(Charsets.UTF_8)
            assertEquals(logText, logExpectedText)
        }
    }
}
