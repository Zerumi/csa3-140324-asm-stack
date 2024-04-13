enum class Opcode {
    WORD, // Define a constant
    LOAD, // Load value to data stack
    STORE, // Push value from data stack to memory
    ADD, // Add 2 elements from stack, push result on stack
    SUB, // Subtract 2 elements from stack, push result on stack
    DROP, // Throw out value from stack
    DUP, // Duplicate TOS on stack
    OR, // Or 2 elements from stack, push result on stack
    AND, // And 2 elements from stack, push result on stack
    XOR, // Xor 2 elements from stack, push result on stack
    JZ, // If TOS = 0, jump over one command
    JUMP, // Set PC of memory address
    CALL, // Call a subroutine
    RET, // Return from a subroutine
    IN, // Get buffer symbol from IO to stack
    OUT, // Write symbol from stack to buffer
    HALT // Terminate CPU
}
