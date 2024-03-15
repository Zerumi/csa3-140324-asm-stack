package io.github.csa3.comp.register

class Register {

    private var registerValue : Int = 0

    fun setValue(value : Int) {
        registerValue = value
    }

    fun getValue(): Int {
        return registerValue
    }
}