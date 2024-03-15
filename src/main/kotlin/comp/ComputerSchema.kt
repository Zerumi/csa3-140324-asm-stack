package io.github.csa3.comp

import io.github.csa3.comp.register.Bus
import io.github.csa3.comp.register.Register

object ComputerSchema {
     val buses = arrayOf(
         Bus(0, Register(), Register())
     )
}