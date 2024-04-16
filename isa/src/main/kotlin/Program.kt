import kotlinx.serialization.Serializable

/**
 * Data class that holds a finite program.
 * Program represents by its first command address (in memory) and array of instructions
 *
 * Data classes in Kotlin is just simplification of classes,
 * whose objects only holds some values.
 */
@Serializable
data class Program(val initCommand: Int, val program: Array<Instruction>) {

    /*
     * We override these functions because arrays equality should be checked by
     * equality of each array element, not just pointers in memory.
     *
     * It's just a Kotlin restriction.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Program) return false

        if (initCommand != other.initCommand) return false
        if (!program.contentEquals(other.program)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = initCommand
        result = 31 * result + program.contentHashCode()
        return result
    }
}