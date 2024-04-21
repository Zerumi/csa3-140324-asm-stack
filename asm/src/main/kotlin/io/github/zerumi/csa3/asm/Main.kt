package io.github.zerumi.csa3.asm

import io.github.zerumi.csa3.isa.writeCode

fun main(args: Array<String>) {
    val sourceFile = args[0]
    val targetFile = args[1]

    val code = translateAsm(sourceFile)
    writeCode(code, targetFile)
}
