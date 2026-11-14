package com.brendigo.bopavi

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * Private, durable SAF overwrite recovery record. A document obtained using
 * ACTION_CREATE_DOCUMENT is not guaranteed to support atomic replacement.
 *
 * This journal protects the PREVIOUS complete document bytes before a "wt"
 * provider stream is opened. It is stored under noBackupFilesDir. It cannot
 * make a remote provider's write atomic, nor replace a physical-provider test.
 */
internal class SafBackupJournal(private val directory: File) {
    companion object {
        const val MAX_BYTES = 550_000
        private const val MAGIC = 0x42535631 // "BSV1"
        private const val MAX_URI_BYTES = 8_192
    }

    internal enum class Recovery { NONE, PREVIOUS_INTACT, NEW_WRITE_INTACT, RESTORED_PREVIOUS }

    private val record = File(directory, "saf_backup_recovery_v1.bin")
    private val temp = File(directory, "saf_backup_recovery_v1.tmp")

    private data class Entry(
        val destination: String,
        val original: ByteArray,
        val nextDigest: ByteArray
    )

    fun hasPending(): Boolean = record.exists()

    private fun digest(data: ByteArray) =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun persist(entry: Entry) {
        check(!hasPending()) { "Pending SAF backup recovery must be resolved first." }
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create backup recovery directory." }
        val uriBytes = entry.destination.toByteArray(Charsets.UTF_8)
        require(uriBytes.isNotEmpty() && uriBytes.size <= MAX_URI_BYTES)
        require(entry.original.size <= MAX_BYTES)
        FileOutputStream(temp, false).use { stream ->
            DataOutputStream(stream).use { output ->
                output.writeInt(MAGIC)
                output.writeInt(uriBytes.size)
                output.write(uriBytes)
                output.writeInt(entry.original.size)
                output.write(entry.original)
                output.write(digest(entry.original))
                output.write(entry.nextDigest)
                output.flush()
                stream.fd.sync()
            }
        }
        // Same app-private directory and filesystem; if ATOMIC_MOVE is not
        // supported, fail BEFORE touching the user's provider document.
        Files.move(temp.toPath(), record.toPath(), StandardCopyOption.ATOMIC_MOVE)
    }

    private fun readEntry(): Entry {
        DataInputStream(FileInputStream(record)).use { input ->
            check(input.readInt() == MAGIC) { "Unsupported SAF recovery record." }
            val uriLength = input.readInt()
            check(uriLength in 1..MAX_URI_BYTES) { "Invalid SAF recovery URI length." }
            val uriBytes = ByteArray(uriLength)
            input.readFully(uriBytes)
            val originalLength = input.readInt()
            check(originalLength in 0..MAX_BYTES) { "Invalid SAF recovery data length." }
            val original = ByteArray(originalLength)
            input.readFully(original)
            val originalDigest = ByteArray(32)
            input.readFully(originalDigest)
            val nextDigest = ByteArray(32)
            input.readFully(nextDigest)
            check(input.read() == -1) { "Unexpected trailing SAF recovery data." }
            check(digest(original).contentEquals(originalDigest)) {
                "SAF recovery record has failed integrity verification."
            }
            return Entry(String(uriBytes, Charsets.UTF_8), original, nextDigest)
        }
    }

    private fun clear() {
        check(record.delete()) { "Cannot clear verified SAF recovery record." }
    }

    /**
     * Never open a destructive stream without first saving and syncing the
     * prior provider bytes. On an ordinary I/O failure try restoring them;
     * if restore fails, keep the record for a later app launch.
     */
    fun protectedWrite(
        destination: String,
        replacement: ByteArray,
        read: () -> ByteArray,
        write: (ByteArray) -> Unit,
        canStart: () -> Boolean = { true }
    ) {
        require(replacement.size <= MAX_BYTES) { "Backup exceeds byte limit." }
        check(!hasPending()) { "An older SAF document still needs recovery." }
        val before = read()
        require(before.size <= MAX_BYTES) { "Existing backup exceeds safe recovery limit." }
        check(canStart()) { "SAF destination changed before backup started." }
        persist(Entry(destination, before, digest(replacement)))
        try {
            check(canStart()) { "SAF destination changed before backup stream was opened." }
            write(replacement)
            check(read().contentEquals(replacement)) {
                "SAF provider did not return the complete written backup."
            }
            clear()
        } catch (failure: Exception) {
            try {
                // Recovery is allowed even if the Activity gate closed AFTER
                // opening a stream: do not leave a truncated provider document.
                write(before)
                check(read().contentEquals(before)) { "SAF rollback verification failed." }
                clear()
            } catch (rollback: Exception) {
                failure.addSuppressed(rollback)
            }
            throw failure
        }
    }

    /**
     * After process death, resolve the durable journal before any NEW save.
     * Do not discard it if provider access, verification or restore fails.
     */
    fun recover(
        destination: String,
        read: () -> ByteArray,
        write: (ByteArray) -> Unit
    ): Recovery {
        if (!hasPending()) return Recovery.NONE
        val entry = readEntry()
        check(entry.destination == destination) { "Recovery belongs to another SAF document." }
        val current = read()
        require(current.size <= MAX_BYTES) { "Provider document too large for recovery." }
        if (digest(current).contentEquals(digest(entry.original))) {
            clear()
            return Recovery.PREVIOUS_INTACT
        }
        if (digest(current).contentEquals(entry.nextDigest)) {
            clear()
            return Recovery.NEW_WRITE_INTACT
        }
        // The provider document may have been partially overwritten. Restore
        // our exact prior bytes; this cannot detect an independent external edit.
        write(entry.original)
        check(read().contentEquals(entry.original)) { "Recovered bytes could not be verified." }
        clear()
        return Recovery.RESTORED_PREVIOUS
    }

    /** May be called even after destination opt-out, while its URI grant remains held. */
    fun pendingDestination(): String? = if (hasPending()) readEntry().destination else null
}
