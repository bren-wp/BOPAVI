package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class ShopPurchaseTapGateTest {
    @Test fun secondAccidentalTapCannotChargeTwiceImmediately() {
        val gate=ShopPurchaseTapGate()
        assertTrue(gate.allow(0,1000))
        assertFalse(gate.allow(0,1090))
        assertFalse(gate.allow(0,1449))
        assertTrue(gate.allow(0,1450))
    }

    @Test fun distinctProductsAndLaterDeliberateBuysRemainAvailable() {
        val gate=ShopPurchaseTapGate()
        assertTrue(gate.allow(0,100))
        assertTrue(gate.allow(1,150))
        assertTrue(gate.allow(0,160))
        assertFalse(gate.allow(0,170))
        assertFalse(gate.allow(6,900))
        assertFalse(gate.allow(-1,900))
    }

    @Test fun clockResetDoesNotPermanentlyBlockPurchases() {
        val gate=ShopPurchaseTapGate()
        assertTrue(gate.allow(4,5000))
        assertTrue(gate.allow(4,100))
        assertFalse(gate.allow(4,140))
    }
}
