package io.github.csa3.comp

import io.github.csa3.comp.mc.MicrocodeMemory
import io.github.csa3.comp.register.Bus
import io.github.csa3.comp.register.Register

class ControlUnit {
    private val instructionRegister : Register = Register()
    private var currentTick : ULong = 0u
    private var currentMicroInstruction : Int = 0

    private val buses : Array<Bus> = ComputerSchema.buses

    fun start() {

    }

    private fun tick() {
        val microcode = MicrocodeMemory.memory[currentMicroInstruction]
        // apply microcode instruction to all buses
        for (i in buses.indices) {
            buses[i].setValveState(microcode.valves[i] == 1)
        }
        currentTick++
    }

    // change currentMicroInstruction logic
    // command execution logic
    // how to parse struct machine code?
}