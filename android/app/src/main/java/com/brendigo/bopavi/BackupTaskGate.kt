package com.brendigo.bopavi

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Lifecycle and destination generation guard shared by all backup work.
 * A revision invalidates work queued for an old user-picked document.
 * It does not make provider writes transactional once a stream is opened.
 */
internal class BackupTaskGate {
    private val closed=AtomicBoolean(false)
    private val revision=AtomicLong(0L)

    fun isClosed():Boolean = closed.get()
    fun currentRevision():Long = revision.get()
    fun canRun(expectedRevision:Long):Boolean =
        !closed.get() && revision.get()==expectedRevision
    fun revise():Long = revision.incrementAndGet()

    fun close(){
        closed.set(true)
        revision.incrementAndGet()
    }
}
