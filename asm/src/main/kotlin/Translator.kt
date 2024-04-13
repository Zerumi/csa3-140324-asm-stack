import java.io.File

fun meaningfulToken(line: String): String {
    return line.split(";")[0].trim()
}

fun translatePart1(text: String): Pair<Map<String, Int>, List<Pair<Opcode, String>>> {
    val labels = emptyMap<String, Int>().toMutableMap()
    val instructions = emptyList<Pair<Opcode, String>>().toMutableList()

    var nextInstructionLine = 1

    for (line in text.lines()) {
        val token = meaningfulToken(line)
        if (token.isEmpty()) continue
        nextInstructionLine++

        if (token.endsWith(":")) {
            // this is a label
            val label = token.substringBefore(":")
            labels[label] = --nextInstructionLine
        } else if (token.contains(" ")) {
            // this is an operand instruction
            val instruction = token.split(" ")

            val opcode = instruction[0]
            val operand = instruction[1]

            val parsedOpcode = Opcode.valueOf(opcode.uppercase())
            // assert this is an operand opcode
            instructions.add(Pair(parsedOpcode, operand))
        } else {
            val opcode = Opcode.valueOf(token.uppercase())
            instructions.add(Pair(opcode, ""))
        }
    }

    return Pair(labels, instructions)
}

fun translatePart2(labels: Map<String, Int>, instructions: List<Pair<Opcode, String>>): List<Instruction> {
    val resultInstructions = emptyList<Instruction>().toMutableList()

    for (instruction in instructions) {
        if (instruction.second.isEmpty()) {
            resultInstructions.add(Instruction(instruction.first))
        }
        else {
            resultInstructions.add(Instruction(instruction.first, labels[instruction.second]!!))
        }
    }

    return resultInstructions
}

fun translateAsm(filename: String): Array<Instruction> {
    val text = File(filename).readText(Charsets.UTF_8)

    val resultOfFirstPart = translatePart1(text)
    val resultOfSecondPart = translatePart2(resultOfFirstPart.first, resultOfFirstPart.second)

    return resultOfSecondPart.toTypedArray()
}