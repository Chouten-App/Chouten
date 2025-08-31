@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.inumaki.relaywasm

expect fun platform(): String

expect class RelayWASM {
    fun init()
}