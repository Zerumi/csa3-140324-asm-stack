fun main(args: Array<String>) {
    val sourceFile = args[0]
    val targetFile = args[1]

    val code = translateAsm(sourceFile)
    writeCode(code, targetFile)
}