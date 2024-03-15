package io.github.csa3.comp.mc

data class Microcode(val valves : Array<Int>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Microcode) return false

        if (!valves.contentEquals(other.valves)) return false

        return true
    }

    override fun hashCode(): Int {
        return valves.contentHashCode()
    }
}