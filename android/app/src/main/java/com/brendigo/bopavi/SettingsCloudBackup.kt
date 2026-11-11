package com.brendigo.bopavi

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.Executors
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
    private var started=false

    private val changes=SharedPreferences.OnSharedPreferenceChangeListener { _,_->
        enqueueAutoSave()
    }

    fun enabled():Boolean = !uriPrefs.getString("document_uri",null).isNullOrEmpty()

    fun start(){
        if(started)return
        started=true
        gamePrefs.registerOnSharedPreferenceChangeListener(changes)
    }

    fun stop(){
        if(started){
            gamePrefs.unregisterOnSharedPreferenceChangeListener(changes)
            started=false
        }
        executor.shutdown()
    }

    /** Called only after ACTION_CREATE_DOCUMENT returned an actual document. */
    fun activate(uri:Uri,resultFlags:Int,report:(Boolean,String)->Unit){
        val flags=resultFlags and (
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if(flags and android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION==0){
            report(false,"Odabrani pružatelj nije dodijelio pristup za pisanje.")
            return
        }
        try {
            // Persist exactly what this document provider actually granted,
            // never request permissions that were absent from the result.
            activity.contentResolver.takePersistableUriPermission(uri,flags)
        } catch(ex:Exception){
            report(false,"Odabrani pružatelj ne dopušta trajni pristup kopiji.")
            return
        }
        // The URI belongs only in a private preference, never in exportJson().
        if(!uriPrefs.edit().putString("document_uri",uri.toString()).commit()){
            report(false,"Spremanje odredišta nije uspjelo.")
            return
        }
        saveNow { ok,message ->
            // A denied, read-only or unavailable destination should not leave
            // the visible Cloud Save switch enabled after initial setup.
            if(!ok)deactivate()
            report(ok,message)
        }
    }

    fun deactivate(){
        val previous=destination()
        uriPrefs.edit().remove("document_uri").apply()
        pending.set(false)
        if(previous!=null){
            val grant=activity.contentResolver.persistedUriPermissions.firstOrNull {
                it.uri==previous
            }
            if(grant!=null){
                val flags=(if(grant.isReadPermission)
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
                    (if(grant.isWritePermission)
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
                if(flags!=0)runCatching {
                    activity.contentResolver.releasePersistableUriPermission(
                        previous,flags)
                }
            }
        }
        // Never delete the user's previously saved local/cloud document.
    }

    private fun destination():Uri? =
        uriPrefs.getString("document_uri",null)?.let { runCatching {Uri.parse(it)}.getOrNull() }

    private fun write(uri:Uri){
        val json=progress.exportJson()
        require(json.length<=550_000) { "Sigurnosna kopija je prevelika." }
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
            report(false,"Odaberite odredište sigurnosne kopije.")
            return
        }
        executor.execute {
            val result=runCatching { write(uri) }
            activity.runOnUiThread {
                if(result.isSuccess)report(true,"Sigurnosna kopija je spremljena.")
                else report(false,"Spremanje nije uspjelo: "+
                    (result.exceptionOrNull()?.localizedMessage ?: "Nepoznata pogreška"))
            }
        }
    }

    private fun enqueueAutoSave(){
        if(!started || !enabled())return
        pending.set(true)
        if(!active.compareAndSet(false,true))return
        executor.execute {
            try {
                while(pending.getAndSet(false)) {
                    val uri=destination() ?: break
                    // Do not automatically report "success" on screen; the
                    // provider can be unavailable when the app is offline.
                    runCatching { write(uri) }
                }
            } finally {
                active.set(false)
                if(pending.get() && started && enabled())enqueueAutoSave()
            }
        }
    }

    fun restoreFrom(uri:Uri,report:(Boolean,String)->Unit){
        executor.execute {
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
                progress.importJson(backup)
            }
            activity.runOnUiThread {
                if(result.isSuccess)report(true,
                    "Lokalni napredak i otključavanja vraćeni su iz sigurnosne kopije.")
                else report(false,"Vraćanje nije uspjelo: "+
                    (result.exceptionOrNull()?.localizedMessage ?: "Nevaljana kopija"))
            }
        }
    }
}
