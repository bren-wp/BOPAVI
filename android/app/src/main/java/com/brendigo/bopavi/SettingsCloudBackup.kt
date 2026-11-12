package com.brendigo.bopavi

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Opt-in, provider-neutral backup for BOPAVI.
 *
 * The user picks the actual target document using Android's Storage Access
 * Framework (e.g. on-device storage, Drive, or another installed provider).
 * This app never receives provider account credentials and never claims that
 * a backup succeeded before the provider has accepted the data.
 *
 * This backs up earned coins, unlocks and progress; NOT Google Play purchases.
 */
internal class SettingsCloudBackup(
    private val activity:Activity,
    private val progress:ProgressStore
) {
    private val uriPrefs=activity.getSharedPreferences("bopavi_cloud_destination_v1",
        Context.MODE_PRIVATE)
    private val gamePrefs=activity.getSharedPreferences("bopavi_native_v4",
        Context.MODE_PRIVATE)
    private val executor=Executors.newSingleThreadExecutor()
    private val pending=AtomicBoolean(false)
    private val active=AtomicBoolean(false)
    private val gate=BackupTaskGate()
    @Volatile private var started=false

    /** Work queued before stop/deactivate must not touch a stale Activity or URI. */
    private fun schedule(work:()->Unit):Boolean {
        if(gate.isClosed())return false
        return try {
            executor.execute { if(!gate.isClosed())work() }
            true
        } catch(_:RejectedExecutionException) {
            false
        }
    }

    private fun reportOnMain(report:(Boolean,String)->Unit,ok:Boolean,message:String) {
        if(gate.isClosed())return
        activity.runOnUiThread {
            if(!gate.isClosed() && !activity.isFinishing && !activity.isDestroyed)
                report(ok,message)
        }
    }

    private val changes=SharedPreferences.OnSharedPreferenceChangeListener { _,_->
        enqueueAutoSave()
    }

    fun enabled():Boolean = !uriPrefs.getString("document_uri",null).isNullOrEmpty()

    fun start(){
        if(started || gate.isClosed())return
        started=true
        gamePrefs.registerOnSharedPreferenceChangeListener(changes)
    }

    fun stop(){
        if(gate.isClosed())return
        gate.close()
        if(started){
            gamePrefs.unregisterOnSharedPreferenceChangeListener(changes)
            started=false
        }
        pending.set(false)
        // Let an in-flight provider write finish. Queued work sees the closed
        // gate and skips; do not interrupt a non-atomic SAF write mid-stream.
        executor.shutdown()
    }

    /** Return only persistable grants actually held for this specific document. */
    private fun releaseGrant(uri:Uri) {
        runCatching {
            val grant=activity.contentResolver.persistedUriPermissions.firstOrNull {
                it.uri==uri
            } ?: return@runCatching
            val flags=(if(grant.isReadPermission)
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
                (if(grant.isWritePermission)
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
            if(flags!=0)activity.contentResolver.releasePersistableUriPermission(uri,flags)
        }
    }

    /** Called only after ACTION_CREATE_DOCUMENT returned an actual document. */
    fun activate(uri:Uri,resultFlags:Int,report:(Boolean,String)->Unit){
        if(gate.isClosed())return
        val previous=destination()
        val flags=resultFlags and (
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if(flags and android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION==0){
            reportOnMain(report,false,"Odabrani pružatelj nije dodijelio pristup za pisanje.")
            return
        }
        try {
            // Persist exactly the flags granted by this provider.
            activity.contentResolver.takePersistableUriPermission(uri,flags)
        } catch(ex:Exception){
            reportOnMain(report,false,"Odabrani pružatelj ne dopušta trajni pristup kopiji.")
            return
        }
        // Do not keep a new provider grant if private destination persistence fails.
        if(!uriPrefs.edit().putString("document_uri",uri.toString()).commit()){
            if(previous!=uri)releaseGrant(uri)
            reportOnMain(report,false,"Spremanje odredišta nije uspjelo.")
            return
        }
        val revision=gate.revise()
        if(previous!=null && previous!=uri)releaseGrant(previous)
        saveNow { ok,message ->
            // Completion from an old target must not disable a newer opt-in.
            if(!gate.canRun(revision) || destination()!=uri)return@saveNow
            if(!ok)deactivate()
            report(ok,message)
        }
    }

    fun deactivate(){
        val previous=destination()
        // Do not revoke a still-configured grant on a failed disk commit.
        if(!uriPrefs.edit().remove("document_uri").commit())return
        gate.revise()
        pending.set(false)
        if(previous!=null)releaseGrant(previous)
        // The user-owned SAF document is left intact.
    }

    private fun destination():Uri? =
        uriPrefs.getString("document_uri",null)?.let { runCatching {Uri.parse(it)}.getOrNull() }

    private fun write(uri:Uri,revision:Long){
        // A scheduled task for a replaced/disabled destination cannot start a
        // new destructive write. Already-open provider streams may still finish.
        check(gate.canRun(revision) && destination()==uri) {
            "Odredište je promijenjeno ili isključeno."
        }
        val json=progress.exportJson()
        require(json.length<=550_000) { "Sigurnosna kopija je prevelika." }
        check(gate.canRun(revision) && destination()==uri) {
            "Odredište je promijenjeno ili isključeno."
        }
        // SAF does not guarantee atomic replacement for every provider.
        val stream=activity.contentResolver.openOutputStream(uri,"wt")
            ?: error("Odredišni dokument nije dostupan.")
        stream.use { file ->
            OutputStreamWriter(file,Charsets.UTF_8).use { writer ->
                writer.write(json)
                writer.flush()
            }
        }
    }

    fun saveNow(report:(Boolean,String)->Unit){
        val uri=destination()
        if(uri==null){
            reportOnMain(report,false,"Odaberite odredište sigurnosne kopije.")
            return
        }
        val revision=gate.currentRevision()
        if(!schedule {
            val result=runCatching { write(uri,revision) }
            if(!gate.canRun(revision))return@schedule
            if(result.isSuccess)reportOnMain(report,true,"Sigurnosna kopija je spremljena.")
            else reportOnMain(report,false,"Spremanje nije uspjelo: "+
                (result.exceptionOrNull()?.localizedMessage ?: "Nepoznata pogreška"))
        })reportOnMain(report,false,"Spremanje nije dostupno nakon zatvaranja igre.")
    }

    private fun enqueueAutoSave(){
        if(!started || gate.isClosed() || !enabled())return
        pending.set(true)
        if(!active.compareAndSet(false,true))return
        if(!schedule {
            try {
                while(pending.getAndSet(false) && !gate.isClosed()){
                    val revision=gate.currentRevision()
                    val uri=destination() ?: break
                    // Failed provider writes do not count as successful saves.
                    runCatching { write(uri,revision) }
                }
            } finally {
                active.set(false)
                if(pending.get() && started && enabled() && !gate.isClosed())
                    enqueueAutoSave()
            }
        }){
            active.set(false)
            pending.set(false)
        }
    }

    fun restoreFrom(uri:Uri,report:(Boolean,String)->Unit){
        if(!schedule {
            val result=runCatching {
                val source=activity.contentResolver.openInputStream(uri)
                    ?: error("Nije moguće otvoriti sigurnosnu kopiju.")
                // Reject oversized or malformed documents before importing.
                val backup=source.use { stream ->
                    InputStreamReader(stream,Charsets.UTF_8).use { reader ->
                        val buffer=CharArray(4096)
                        val content=StringBuilder()
                        while(true){
                            val count=reader.read(buffer)
                            if(count<0)break
                            content.append(buffer,0,count)
                            require(content.length<=550_000) {
                                "Sigurnosna kopija je prevelika."
                            }
                        }
                        content.toString()
                    }
                }
                check(!gate.isClosed()) { "Igra je zatvorena." }
                progress.importJson(backup)
            }
            if(result.isSuccess)reportOnMain(report,true,
                "Lokalni napredak i otključavanja vraćeni su iz sigurnosne kopije.")
            else reportOnMain(report,false,"Vraćanje nije uspjelo: "+
                (result.exceptionOrNull()?.localizedMessage ?: "Nevaljana kopija"))
        })reportOnMain(report,false,"Vraćanje nije dostupno nakon zatvaranja igre.")
    }
}
