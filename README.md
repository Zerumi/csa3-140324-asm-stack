# csa3-140324-asm-stack [![CodeFactor](https://www.codefactor.io/repository/github/zerumi/csa3-140324-asm-stack/badge)](https://www.codefactor.io/repository/github/zerumi/csa3-140324-asm-stack)

Computer system architecture laboratory work #3  
Full variant --
`asm | stack | neum | mc | tick 
| struct | stream | port | cstr | prob2 | -`

(source: `asm | stack | neum | mc -> hw | tick -> instr
| struct | stream | port | cstr | prob2 | cache`)

Aimed for 30/40 points (variant made w/o complication)

Full report (in Russian) you may see here (Markdown): [report](/docs/report.md)  
Also in .pdf: [here](/docs/P3206%20Афанасьев_Кирилл_Александрович%20ЛР3%20Отчёт.pdf)

Task details: https://gitlab.se.ifmo.ru/computer-systems/csa-rolling/-/blob/master/lab3-task.md

## Assembly language

Syntax:

```bnf
<line> ::= <label> <comment>? "\n"
       | <instr> <comment>? "\n"
       | <comment> "\n"

<program> ::= <line>*

<label> ::= <label_name> ":"

<instr> ::= <op0>
        | <op1> " " <label_name>
        | <op1> " " <positive_integer>

<op0> ::= "nop"
      | "word"
      | "lit"
      | "load"
      | "store"
      | "add"
      | "sub"
      | "inc"
      | "dec"
      | "drop"
      | "dup"
      | "or"
      | "and"
      | "xor"
      | "ret"
      | "in"
      | "out"
      | "halt"

<op1> ::= "jmp"
      | "jz"
      | "call"

<positive_integer> ::= [0-9]+
<integer> ::= "-"? <positive_integer>

<lowercase_letter> ::= [a-z]
<uppercase_letter> ::= [A-Z]
<letter> ::= <lowercase_letter> | <uppercase_letter>

<letter_or_number> ::= <letter> | <integer>
<letter_or_number_with_underscore> ::= <letter_or_number> | "_"

<label_name> ::= <letter> <letter_or_number_with_underscore>*

<any_letter> ::= <letter_or_number_with_underscore> | " "

<comment> ::= " "* ";" " "* <letter_or_number_with_underscore>*
```

The Program completes sequentially, one instruction after another. Example of a program that calculates a factorial

```asm
res:
        word 0      ; result accumulator
fac:
        dup         ; Stack: arg arg
        lit 1       ; Stack: arg arg 1
        sub         ; Stack: arg 0/pos_num
        lit break   ; Stack: arg 0/pos_num break
        swap        ; Stack: arg break 0/pos_num
        jz          ; Stack: arg
        dup         ; Stack: arg arg
        dec         ; Stack: arg (arg - 1) -> arg
        lit fac     ; Stack: [...] arg fac
        call        ; Stack: [...] res
        mul         ; Stack: res
break:
        ret         ; Stack: arg/res

start:
        lit 11      ; Stack: 11
        lit fac     ; Stack: 11 fac
        call        ; Stack: 11!
        lit res     ; Stack: 11! res_addr
        store       ; Stack: <empty>
        halt        ; halted
```

## ISA

* `NOP` – no operation.
* `WORD <literal>` – define a variable in memory.
* `LIT <literal>` – push literal on top of the stack.
* `LOAD { address }` – load value in memory by address.
* `STORE { address, element }` – push value in memory by address.
* `ADD { e1, e2 }` – push the result of the addition operation onto the stack e2 + e1.
* `SUB { e1, e2 }` – push the result of the subtraction operation onto the stack e2 – e1.
* `MUL { e1, e2 }` – push the result of the multiplication operation onto the stack e2 * e1.
* `DIV { e1, e2 }` – push the result of the division operation onto the stack e2 / e1.
* `MOD { e1, e2 }` – push the result of the mod operation onto the stack e2 % e1.
* `INC { element }` – increment top of the stack.
* `DEC { element }` – decrement top of the stack.
* `DROP { element }` – remove element from stack.
* `DUP { element }` – duplicate the first element (tos) on stack.
* `SWAP { e1, e2 }` – swap 2 elements.
* `OVER { e1 } [ e2 ]` – duplicate the first element on the stack through the second. If there is only one element on
  the stack, the behavior is undefined.
* `AND { e1, e2 }` – push the result of a logical "AND" operation onto the stack e2 & e1.
* `OR { e1, e2 }` – push the result of a logical "OR" operation onto the stack e2 | e1.
* `XOR { e1, e2 }` – push the result of a logical "XOR" operation onto the stack e2 ^ e1.
* `JZ { element, address }` – if the element is 0, start executing instructions at the specified address. A type of
  conditional jump.
* `JN { element, address }` – if the element is negative, start executing instructions at the specified address. A type
  of conditional jump.
* `JUMP { address }` – proceed an unconditional transition to the specified address.
* `CALL { address }` – start execution of the procedure by the specified address.
* `RET` – return from a procedure.
* `IN { port }` – receive data from an external device by a specified port.
* `OUT { port, value }` – receive data to an external device by a specified port.
* `HALT` – stop clock generator and modeling process

## Assembly translator

CLI: `java -jar asm-1.0.jar <input_file> <target_file>`

Implemented in [asm](/asm) module.  
Two passes:

1) Generation of machine code without jump addresses and calculation of jump label values.
   Assembly mnemonics are translated one-to-one into machine instructions; except for the WORD mnemonics.
   In its case, a variable is initialized in memory without any opcode.
   However, WORD, along with instructions, also supports labels.
2) Substitution of transition marks in instructions.

## Computer simulation

CLI: `java -jar comp-1.0.jar [-p | --program-file <filepath>]
[-i | --input-file] [-o | --output-file]
[<-stdout | --log-stdout> | <-l | --log-file <filepath>>]
[--memory-initial-size <size>] [--data-stack-size <size>]
[--return-stack-size <size>]`  
or `java -jar comp-1.0.jar [-h | --help]`

Implemented in [comp](/comp) module.

Processor schema's available [here](/docs/csa-3-proc-scheme.pdf)

## Tests

Implemented integration golden-tests (based on `JUnit Platform v.5`)

For run test use this commands:

```shell
  # pwd ./csa3-140324-asm-stack
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.catTest" 
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.helloTest"
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.helloUserNameTest" 
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.prob2Test" 
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.facTest"
```

For update golden-configuration, use this commands (just add `-DupdateGolden=true`):

```shell
  # pwd ./csa3-140324-asm-stack
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.catTest" -DupdateGolden=true
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.helloTest" -DupdateGolden=true
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.helloUserNameTest" -DupdateGolden=true
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.prob2Test" -DupdateGolden=true
  ./gradlew :comp:integrationTest --tests "AlgorithmTest.facTest" -DupdateGolden=true
```

Implemented CI for GitHub Actions you may see [here](/.github/workflows/ci.yml)

Using these templates:

* lint:
  * DeteKt all
  * KtLint all
  * Markdown lint
* build:
  * Gradle build
* test:
  * run gradlew (commands above)

## Usage example

```zsh
zerumi@MacBook-Air-Kirill csa3-example % pwd
/Users/zerumi/Desktop/csa3-example
zerumi@MacBook-Air-Kirill csa3-example % java -jar asm-1.0.jar fac.sasm fac.json
zerumi@MacBook-Air-Kirill csa3-example % touch in.txt
zerumi@MacBook-Air-Kirill csa3-example % touch out.txt
zerumi@MacBook-Air-Kirill csa3-example % touch log.txt
zerumi@MacBook-Air-Kirill csa3-example % ls
asm-1.0.jar
comp-1.0.jar
fac.json
fac.sasm
in.txt
log.txt
out.txt
zerumi@MacBook-Air-Kirill csa3-example % java -jar comp-1.0.jar -i in.txt -o out.txt -l log.txt -p fac.json
zerumi@MacBook-Air-Kirill csa3-example % cat log.txt 
[INFO]: io.github.zerumi.csa3.comp.ControlUnit - NOW EXECUTING INSTRUCTION PC: 13 --> LIT 11
[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 0 -- MPC: 0 / MicroInstruction: LatchAR, ARSelectPC, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 1): [0 | ]
Return stack (size = 0): []
PC: 13 AR: 13 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 1 -- MPC: 1 / MicroInstruction: LatchMPCounter, MicroProgramCounterOpcode 
Stack (size = 1): [0 | ]
Return stack (size = 0): []
PC: 13 AR: 13 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 2 -- MPC: 3 / MicroInstruction: DataStackPush, LatchAR, ARSelectPC, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 2): [0 | 0]
Return stack (size = 0): []
PC: 13 AR: 13 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 3 -- MPC: 4 / MicroInstruction: LatchTOS, TOSSelectMemory, LatchMPCounter, MicroProgramCounterZero, LatchPC, PCJumpTypeNext 
Stack (size = 2): [11 | 0]
Return stack (size = 0): []
PC: 14 AR: 13 BR: 0
MEMORY READ VALUE: AR: 13 ---> OperandInstruction(opcode=LIT, operand=11)

……………………………………………………………………………………

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - NOW EXECUTING INSTRUCTION PC: 17 --> STORE
[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 559 -- MPC: 0 / MicroInstruction: LatchAR, ARSelectPC, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 3): [0 | 39916800, 0]
Return stack (size = 0): []
PC: 17 AR: 17 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 560 -- MPC: 1 / MicroInstruction: LatchMPCounter, MicroProgramCounterOpcode 
Stack (size = 3): [0 | 39916800, 0]
Return stack (size = 0): []
PC: 17 AR: 17 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 561 -- MPC: 7 / MicroInstruction: LatchAR, ARSelectTOS, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 3): [0 | 39916800, 0]
Return stack (size = 0): []
PC: 17 AR: 0 BR: 0

[INFO]: io.github.zerumi.csa3.comp.DataPath - MEMORY WRITTEN VALUE: AR: 0 <--- 39916800
[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 562 -- MPC: 8 / MicroInstruction: MemoryWrite, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 3): [0 | 39916800, 0]
Return stack (size = 0): []
PC: 17 AR: 0 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 563 -- MPC: 9 / MicroInstruction: DataStackPop, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 2): [0 | 0]
Return stack (size = 0): []
PC: 17 AR: 0 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 564 -- MPC: 10 / MicroInstruction: LatchTOS, TOSSelectDS, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 2): [0 | 0]
Return stack (size = 0): []
PC: 17 AR: 0 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 565 -- MPC: 11 / MicroInstruction: DataStackPop, LatchMPCounter, MicroProgramCounterZero, LatchPC, PCJumpTypeNext 
Stack (size = 1): [0 | ]
Return stack (size = 0): []
PC: 18 AR: 0 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - NOW EXECUTING INSTRUCTION PC: 18 --> HALT
[INFO]: io.github.zerumi.csa3.comp.ControlUnit - 
TICK 566 -- MPC: 0 / MicroInstruction: LatchAR, ARSelectPC, LatchMPCounter, MicroProgramCounterNext 
Stack (size = 1): [0 | ]
Return stack (size = 0): []
PC: 18 AR: 18 BR: 0

[INFO]: io.github.zerumi.csa3.comp.ControlUnit - [HALTED]
```

## Testing source example

```zsh
zerumi@MacBook-Air-Kirill csa3-140324-asm-stack % pwd
/Users/zerumi/IdeaProjects/csa3-140324-asm-stack
zerumi@MacBook-Air-Kirill csa3-140324-asm-stack % ./gradlew :comp:integrationTest --tests "AlgorithmTest.facTest"
Starting a Gradle Daemon, 2 incompatible and 1 stopped Daemons could not be reused, use --status for details

> Configure project :comp
w: file:///Users/zerumi/IdeaProjects/csa3-140324-asm-stack/comp/build.gradle.kts:72:9: 'getter for testSourceDirs: (Mutable)Set<File!>!' is deprecated. Deprecated in Java
w: file:///Users/zerumi/IdeaProjects/csa3-140324-asm-stack/comp/build.gradle.kts:72:9: 'getter for testSourceDirs: (Mutable)Set<File!>!' is deprecated. Deprecated in Java

The Kotlin Gradle plugin was loaded multiple times in different subprojects, which is not supported and may break the build. 
This might happen in subprojects that apply the Kotlin plugins with the Gradle 'plugins { ... }' DSL if they specify explicit versions, even if the versions are equal.
Please add the Kotlin plugin to the common parent project or the root project, then remove the versions in the subprojects.
If the parent project does not need the plugin, add 'apply false' to the plugin line.
See: https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
The Kotlin plugin was loaded in the following projects: ':asm', ':isa'

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.4/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 10s
15 actionable tasks: 4 executed, 11 up-to-date

The build scan was not published due to a configuration problem.

The buildScan extension 'termsOfUseAgree' value must be exactly the string 'yes' (without quotes).
The value given was 'no'.

For more information, please see https://gradle.com/help/gradle-plugin-terms-of-use.

Alternatively, if you are using Develocity, specify the server location.
For more information, please see https://gradle.com/help/gradle-plugin-config.
```

## General stats

```plain
|           Full name            |  alg  | loc | bytes | instr | exec_instr | tick | variant                                                                                       |
| Афанасьев Кирилл Александрович | hello | 47  |   -   |  23   |    209     | 914  | asm | stack | neum | mc -> hw | tick -> instr | struct | stream | port | cstr | prob2 | cache |
| Афанасьев Кирилл Александрович | prob2 | 95  |   -   |  79   |    786     | 3799 | asm | stack | neum | mc -> hw | tick -> instr | struct | stream | port | cstr | prob2 | cache |
| Афанасьев Кирилл Александрович |  fac  | 23  |   -   |  18   |    133     | 566  | asm | stack | neum | mc -> hw | tick -> instr | struct | stream | port | cstr | prob2 | cache |
```

v.1.0 by Zerumi, 22/04/2024
