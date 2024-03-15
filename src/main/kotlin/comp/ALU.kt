package io.github.csa3.comp

import io.github.csa3.comp.register.Register

class ALU {
    private val leftInput : Register = Register()
    private val rightInput : Register = Register()

    private val output : Register = Register()

    fun and() {
        output.setValue(leftInput.getValue() and rightInput.getValue())
    }

    fun or() {
        output.setValue(leftInput.getValue() or rightInput.getValue())
    }

    fun xor() {
        output.setValue(leftInput.getValue() xor rightInput.getValue())
    }

    fun sum() {
        output.setValue(leftInput.getValue() + rightInput.getValue())
    }

    fun sub() {
        output.setValue(leftInput.getValue() - rightInput.getValue())
    }

    fun mul() {
        output.setValue(leftInput.getValue() * rightInput.getValue())
    }

    fun div() {
        output.setValue(leftInput.getValue() / rightInput.getValue())
    }
}