import kotlin.collections.ArrayDeque

class DataPath(dataStackSize: Int, memoryInitialSize : Int) {

    private val dataStack = ArrayDeque<Int>(dataStackSize)
    var tos = 0
    private var ar = 0
    val memory = Array(memoryInitialSize) { Instruction(Opcode.WORD) }

    fun onSignalDataStackPush() {
        TODO("Not yet implemented")
    }

    fun onSignalDataStackPop() {
        TODO("Not yet implemented")
    }

    fun onSignalLatchAR(microcode: Array<Signal>) {
        TODO("Not yet implemented")
    }

    fun onSignalLatchTOS(microcode: Array<Signal>) {
        TODO("Not yet implemented")
    }

    fun onSignalOutput() {
        TODO("Not yet implemented")
    }

    fun onSignalMemoryWrite() {
        TODO("Not yet implemented")
    }
}