import java.io.File

private fun meaningfulToken(line: String): String {
    return line.split(";")[0].trim()
}

data class LabelInstruction(val instruction: MemoryCell, val label: String = "")

private val POSSIBLE_OPERAND_INSTRUCTIONS = setOf(
    Opcode.LIT,
)

private fun addInstruction(instructions: MutableList<LabelInstruction>, parsedOpcode: Opcode, operand: String) {
    when (parsedOpcode) {
        Opcode.WORD -> {
            // data may be a number, otherwise this is a label
            if (operand.toIntOrNull() != null) instructions.add(
                LabelInstruction(
                    MemoryCell.Data(
                        operand.toInt()
                    )
                )
            )
            else instructions.add(LabelInstruction(MemoryCell.Data(), operand.lowercase()))
        }

        in POSSIBLE_OPERAND_INSTRUCTIONS -> {
            // operand may be a number, otherwise this is a label
            if (operand.toIntOrNull() != null) instructions.add(
                LabelInstruction(
                    MemoryCell.OperandInstruction(
                        parsedOpcode, operand.toInt()
                    )
                )
            )
            else instructions.add(LabelInstruction(MemoryCell.Instruction(parsedOpcode), operand.lowercase()))
        }

        else -> {
            // it's just an opcode w/o operand
            instructions.add(LabelInstruction(MemoryCell.Instruction(parsedOpcode)))
        }
    }
}

/**
 * The First stage of translation needs to fetch all labels in code,
 * and translate opcodes to instructions
 */
private fun translatePart1(text: String): Pair<Map<String, Int>, List<LabelInstruction>> {
    val labels = emptyMap<String, Int>().toMutableMap()
    val instructions = emptyList<LabelInstruction>().toMutableList()

    var nextInstructionLine = 0

    for (line in text.lines()) {
        val token = meaningfulToken(line)
        if (token.isEmpty()) continue

        nextInstructionLine++

        if (token.endsWith(":")) {
            // this is a label
            val label = token.substringBefore(":")
            labels[label.lowercase()] = --nextInstructionLine
        } else {
            // this is an opcode instruction
            val instruction = token.split(" ")

            val opcode = instruction[0]
            val parsedOpcode = Opcode.valueOf(opcode.uppercase())

            val operand = if (instruction.size == 2) instruction[1] else ""

            addInstruction(instructions, parsedOpcode, operand)
        }
    }

    return Pair(labels, instructions)
}

/**
 * The second stage of translation needs for replace labels to addresses, fetched in stage 1
 */
private fun translatePart2(
    labels: Map<String, Int>, instructions: List<LabelInstruction>
): Program {

    val resultInstructions = emptyList<MemoryCell>().toMutableList()

    for (labelInstruction in instructions) {
        if (labelInstruction.label.isEmpty()) {
            resultInstructions.add(labelInstruction.instruction)
        } else {
            when (val memoryCell = labelInstruction.instruction) {
                is MemoryCell.Instruction -> {
                    resultInstructions.add(
                        MemoryCell.OperandInstruction(
                            memoryCell.opcode, labels[labelInstruction.label]!!
                        )
                    )
                }

                is MemoryCell.Data -> {
                    resultInstructions.add(
                        MemoryCell.Data(
                            labels[labelInstruction.label]!!
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    val firstAddress = labels["start"]!!
    val program = Program(firstAddress, resultInstructions.toTypedArray())

    return program
}

/**
 * This function translates code provided in {filename} file
 * to machine code for stack computer
 *
 * Please, remember to put the label "start:" before first instruction!
 */
fun translateAsm(filename: String): Program {
    val text = File(filename).readText(Charsets.UTF_8)

    val resultOfFirstPart = translatePart1(text)
    val resultOfSecondPart = translatePart2(resultOfFirstPart.first, resultOfFirstPart.second)

    return resultOfSecondPart
}
