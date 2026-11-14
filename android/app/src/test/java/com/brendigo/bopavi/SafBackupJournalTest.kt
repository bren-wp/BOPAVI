package com.brendigo.bopavi

import java.io.IOException
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SafBackupJournalTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun successfulSaveRequiresReadableVerificationAndClearsJournal() {
        val guard = SafBackupJournal(folder.root)
        var document = "old-v6".toByteArray()
        val replacement = "new-v6".toByteArray()
        guard.protectedWrite("content://owner/backup", replacement,
            read = { document.copyOf() },
            write = {
                assertTrue("Previous document must be journaled before opening write", guard.hasPending())
                document = it.copyOf()
            })
        assertArrayEquals(replacement, document)
        assertFalse(guard.hasPending())
    }

    @Test fun interruptedWriteRestoresPreviousBytesWithoutClaimingSuccess() {
        val guard = SafBackupJournal(folder.root)
        val original = "valid-original-backup".toByteArray()
        var document = original.copyOf()
        var attempts = 0
        try {
            guard.protectedWrite("content://owner/backup", "replacement".toByteArray(),
                read = { document.copyOf() },
                write = {
                    attempts++
                    document = if (attempts == 1) it.copyOfRange(0, 3) else it.copyOf()
                    if (attempts == 1) throw IOException("provider interrupted")
                })
            fail("Interrupted provider write must fail")
        } catch (expected: IOException) {
            assertEquals("provider interrupted", expected.message)
        }
        assertEquals(2, attempts)
        assertArrayEquals(original, document)
        assertFalse(guard.hasPending())
    }

    @Test fun failedRollbackPersistsSnapshotForRecoveryAfterProcessDeath() {
        val directory = folder.root
        val target = "content://owner/backup"
        val original = "old-valid-json".toByteArray()
        var document = original.copyOf()
        val firstProcess = SafBackupJournal(directory)
        try {
            firstProcess.protectedWrite(target, "new".toByteArray(),
                read = { document.copyOf() },
                write = {
                    document = byteArrayOf('n'.code.toByte(), 'e'.code.toByte())
                    throw IOException("offline")
                })
            fail("Provider failure must propagate")
        } catch (_: IOException) {
            assertTrue(firstProcess.hasPending())
        }
        val afterRestart = SafBackupJournal(directory)
        assertEquals(SafBackupJournal.Recovery.RESTORED_PREVIOUS,
            afterRestart.recover(target,
                read = { document.copyOf() },
                write = { document = it.copyOf() }))
        assertArrayEquals(original, document)
        assertFalse(afterRestart.hasPending())
    }

    @Test fun cancelBeforeDestructiveOpenNeverTouchesProvider() {
        val guard = SafBackupJournal(folder.root)
        var checks = 0
        var writes = 0
        try {
            guard.protectedWrite("content://owner/backup", byteArrayOf(5),
                read = { byteArrayOf(1) },
                write = { writes++ },
                canStart = { ++checks == 1 })
            fail("Stale destination must fail before provider write")
        } catch (_: IllegalStateException) {
            assertEquals(0, writes)
            assertFalse(guard.hasPending())
        }
    }

    @Test fun manualExternalEditIsNeverOverwrittenAfterInterruptedWrite() {
        val target = "content://owner/backup"
        var document = "old-complete-backup".toByteArray()
        val journal = SafBackupJournal(folder.root)
        try {
            journal.protectedWrite(target, "new-complete-backup".toByteArray(),
                read = { document.copyOf() },
                write = {
                    document = "ne".toByteArray()
                    throw IOException("remote provider stopped")
                })
            fail("Provider error must be surfaced")
        } catch (_: IOException) {
            assertTrue(journal.hasPending())
        }
        val ownerEdit = "third-party-owned-edits".toByteArray()
        document = ownerEdit.copyOf()
        var rollbackWrites = 0
        try {
            SafBackupJournal(folder.root).recover(target,
                read = { document.copyOf() },
                write = {
                    rollbackWrites++
                    document = it.copyOf()
                })
            fail("An external edit must require manual review")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message.orEmpty().contains("manual recovery"))
        }
        assertEquals(0, rollbackWrites)
        assertArrayEquals(ownerEdit, document)
        assertTrue(journal.hasPending())
    }

    @Test fun unknownProviderBytesOnFailureCannotTriggerAutomaticRollback() {
        var document = "existing".toByteArray()
        val journal = SafBackupJournal(folder.root)
        var attempts = 0
        try {
            journal.protectedWrite("content://owner/backup", "replacement".toByteArray(),
                read = { document.copyOf() },
                write = {
                    attempts++
                    document = "external-state".toByteArray()
                    throw IOException("write failed with unrelated provider state")
                })
            fail("Write failure expected")
        } catch (expected: IOException) {
            assertTrue(expected.suppressed.isNotEmpty())
        }
        assertEquals(1, attempts)
        assertEquals("external-state", String(document))
        assertTrue(journal.hasPending())
    }

    @Test fun recoveryBelongingToAnotherDocumentMustNotWrite() {
        var document = "before".toByteArray()
        val journal = SafBackupJournal(folder.root)
        try {
            journal.protectedWrite("content://owner/A", "after".toByteArray(),
                read = { document.copyOf() },
                write = {
                    document = "af".toByteArray()
                    throw IOException("offline")
                })
            fail("Write was expected to fail")
        } catch (_: IOException) {
            assertTrue(journal.hasPending())
        }
        var writes = 0
        try {
            journal.recover("content://owner/B",
                read = { document.copyOf() },
                write = { writes++ })
            fail("Old document recovery must not be directed to another URI")
        } catch (_: IllegalStateException) {
            assertEquals(0, writes)
            assertTrue(journal.hasPending())
        }
    }

    @Test fun oversizedDocumentAndUnrecognizedDestinationFailClosed() {
        val guard = SafBackupJournal(folder.root)
        var writes = 0
        try {
            guard.protectedWrite("content://owner/backup", byteArrayOf(1),
                read = { ByteArray(SafBackupJournal.MAX_BYTES + 1) },
                write = { writes++ })
            fail("Oversized original must be rejected")
        } catch (_: IllegalArgumentException) {
            assertEquals(0, writes)
        }
        assertFalse(guard.hasPending())
    }

    @Test fun intactReplacementAfterInterruptedCleanupMustNotBeRolledBack() {
        val dir = folder.root
        val guard = SafBackupJournal(dir)
        var document = "before".toByteArray()
        var failVerify = true
        try {
            guard.protectedWrite("content://owner/backup", "after".toByteArray(),
                read = {
                    if (failVerify && String(document) == "after") {
                        failVerify = false
                        throw IOException("temporary read failure")
                    }
                    document.copyOf()
                },
                write = {
                    if (String(it) == "before") throw IOException("offline on rollback")
                    document = it.copyOf()
                })
            fail("Verification failure must not be treated as success")
        } catch (_: IOException) {
            assertTrue(guard.hasPending())
        }
        assertEquals(SafBackupJournal.Recovery.NEW_WRITE_INTACT,
            SafBackupJournal(dir).recover("content://owner/backup",
                read = { document.copyOf() },
                write = { fail("The completed replacement must not be overwritten") }))
        assertFalse(guard.hasPending())
    }
}
