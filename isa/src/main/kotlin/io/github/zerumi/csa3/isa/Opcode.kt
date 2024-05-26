package io.github.zerumi.csa3.isa

enum class Opcode {
    NOP, // No operation
    WORD, // Define a numeric constant
    BUF, // Define a zeroed buffer
    LIT, // Push literal on stack
    LOAD, // Load value to data stack
    STORE, // Push value from data stack to memory
    ADD, // Add 2 elements from stack, push result on stack
    SUB, // Subtract 2 elements from stack, push result on stack
    MUL, // Multiple 2 elements from stack, push result on stack
    DIV, // Divide 2 elements from stack, push result on stack
    ADDF, // Add 2 float elements from stack, push result on stack
    SUBF, // Subtract 2 float elements from stack, push result on stack
    MULF, // Multiple 2 float elements from stack, push result on stack
    DIVF, // Divide 2 float elements from stack, push result on stack
    MOD, // Get modulo of 2 elements from stack, push result on stack
    FTOI, // Convert float value to int
    INC, // Increase TOS by 1
    DEC, // Decrease TOS by 1
    DROP, // Throw out value from stack
    DUP, // Duplicate TOS on stack
    SWAP, // Swap TOS and pre-last element
    OVER, // Push TOS over a pre-last element of the stack
    AND, // And 2 elements from stack, push a result on stack
    OR, // Or 2 elements from stack, push a result on stack
    XOR, // Xor 2 elements from stack, push result on stack
    JZ, // If Top Of DS = 0, jump by TOS
    JN, // If Top Of DS < 0, jump by TOS
    JUMP, // Set PC of memory address
    CALL, // Call a subroutine
    RET, // Return from a subroutine
    IN, // Get buffer symbol from IO to stack
    OUT, // Write symbol from stack to buffer
    HALT, // Terminate CPU
}
