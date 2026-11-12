package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class BackupTaskGateTest {
    @Test fun changingDocumentInvalidatesQueuedWork() {
        val gate=BackupTaskGate()
        val original=gate.currentRevision()
        assertTrue(gate.canRun(original))
        val changed=gate.revise()
        assertEquals(original+1,changed)
        assertFalse("A queued write cannot target the previous document",gate.canRun(original))
        assertTrue(gate.canRun(changed))
    }

    @Test fun stoppingActivityInvalidatesAllQueuedWork() {
        val gate=BackupTaskGate()
        val initial=gate.currentRevision()
        gate.close()
        assertTrue(gate.isClosed())
        assertFalse(gate.canRun(initial))
        assertFalse(gate.canRun(gate.currentRevision()))
        gate.close()
        assertFalse("Closing twice must never reopen work",gate.canRun(gate.currentRevision()))
    }

    @Test fun revisionsRemainMonotonicAcrossRapidOptInChanges() {
        val gate=BackupTaskGate()
        val a=gate.revise()
        val b=gate.revise()
        val c=gate.revise()
        assertEquals(a+1,b)
        assertEquals(b+1,c)
        assertFalse(gate.canRun(a))
        assertFalse(gate.canRun(b))
        assertTrue(gate.canRun(c))
    }
}
