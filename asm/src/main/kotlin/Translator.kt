import java.io.File

fun meaningfulToken(line: String): String {
    return line.split(";")[0].trim()
}

data class LabelInstruction(val instruction: Instruction, val label: String = "")

val POSSIBLE_OPERAND_INSTRUCTIONS = setOf(
    Opcode.JZ, Opcode.JUMP,
    Opcode.WORD, Opcode.LIT,
)

fun translatePart1(text: String): Pair<Map<String, Int>, List<LabelInstruction>> {
    val labels = emptyMap<String, Int>().toMutableMap()
    val instructions = emptyList<LabelInstruction>().toMutableList()

    var nextInstructionLine = 1

    for (line in text.lines()) {
        val token = meaningfulToken(line)
        if (token.isEmpty()) continue

        nextInstructionLine++

        if (token.endsWith(":")) {
            // this is a label
            val label = token.substringBefore(":")
            labels[label] = --nextInstructionLine
        } else {
            // this is an opcode instruction
            val instruction = token.split(" ")

            val opcode = instruction[0]
            val parsedOpcode = Opcode.valueOf(opcode.uppercase())

            val operand = if (instruction.size == 2) instruction[1] else ""

            when (parsedOpcode) {
                in POSSIBLE_OPERAND_INSTRUCTIONS -> {
                    // operand may be a number, otherwise this is a label
                    if (operand.toIntOrNull() != null)
                        instructions.add(LabelInstruction(Instruction(parsedOpcode, operand.toInt())))
                    else
                        instructions.add(LabelInstruction(Instruction(parsedOpcode), operand))
                }
                else -> {
                    // it's just an opcode w/o operand
                    instructions.add(LabelInstruction(Instruction(parsedOpcode)))
                }
            }
        }
    }

    return Pair(labels, instructions)
}

fun translatePart2(
    labels: Map<String, Int>,
    instructions: List<LabelInstruction>
): List<Instruction> {

    val resultInstructions = emptyList<Instruction>().toMutableList()

    for (labelInstruction in instructions) {
        if (labelInstruction.label.isEmpty()) {
            resultInstructions.add(labelInstruction.instruction)
        } else {
            resultInstructions.add(
                Instruction(
                    labelInstruction.instruction.opcode, labels[labelInstruction.label]!!
                )
            )
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
