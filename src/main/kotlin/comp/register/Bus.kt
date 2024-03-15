package io.github.csa3.comp.register

class Bus(
    private val accessId: Int, private val fromRegister: Register, private val toRegister: Register
) {

    // setValveState used by Control Unit to open valve
    // if state is true then register value copies to another register
    fun setValveState(state: Boolean) {
        if (state) toRegister.setValue(fromRegister.getValue())
    }

    // connect directly to alu for set operation
}