package com.example.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Stable per-install actor identity. Stored under noBackupFilesDir so Android
 * Auto Backup/device transfer cannot clone one actor ID onto two devices.
 */
object DeviceIdentity {
    private const val FILE_NAME = "mandala_sync_actor_id"

    @Synchronized
    fun getOrCreate(context: Context): String {
        val identityFile = File(context.noBackupFilesDir, FILE_NAME)
        if (identityFile.isFile) {
            val existing = identityFile.readText().trim()
            if (existing.isNotBlank()) return existing
        }

        val actorId = UUID.randomUUID().toString()
        val temporaryFile = File(context.noBackupFilesDir, "$FILE_NAME.tmp")
        FileOutputStream(temporaryFile).use { output ->
            output.write(actorId.toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        if (!temporaryFile.renameTo(identityFile)) {
            temporaryFile.delete()
            if (identityFile.isFile) {
                val winner = identityFile.readText().trim()
                if (winner.isNotBlank()) return winner
            }
            error("Unable to persist this installation's sync identity.")
        }
        return actorId
    }
}
