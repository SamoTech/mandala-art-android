package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Account-scoped cloud backup for Room progress.
 *
 * Room remains the source of truth while offline. On a successful sign-in,
 * records are reconciled by lastEditedTimestamp; the newer record wins.
 */
class CloudSaveManager(
    private val dao: MandalaDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        syncProgress()
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        syncProgress()
    }

    fun signOut() = auth.signOut()

    suspend fun syncProgress() {
        val uid = auth.currentUser?.uid ?: error("Sign in to sync your progress.")
        val collection = firestore.collection("users").document(uid).collection("mandalaProgress")
        val localRecords = dao.getAllProgressOnce().associateBy { it.mandalaId }
        val remoteSnapshot = collection.get().await()
        val remoteRecords = remoteSnapshot.documents.associate { document ->
            val record = MandalaProgressEntity(
                mandalaId = document.id,
                coloredRegionsJson = document.getString("coloredRegionsJson").orEmpty(),
                percentCompleted = (document.getDouble("percentCompleted") ?: 0.0).toFloat(),
                isFavorite = document.getBoolean("isFavorite") ?: false,
                isCompleted = document.getBoolean("isCompleted") ?: false,
                lastEditedTimestamp = document.getLong("lastEditedTimestamp") ?: 0L
            )
            document.id to record
        }

        // Merge each ID independently; never replace a newer local record with an older cloud copy.
        (localRecords.keys + remoteRecords.keys).forEach { id ->
            val local = localRecords[id]
            val remote = remoteRecords[id]
            when {
                local == null && remote != null -> dao.upsertProgress(remote)
                local != null && (remote == null || local.lastEditedTimestamp >= remote.lastEditedTimestamp) ->
                    collection.document(id).set(local.toCloudMap()).await()
                local != null && remote != null -> dao.upsertProgress(remote)
            }
        }
    }

    private fun MandalaProgressEntity.toCloudMap(): Map<String, Any> = mapOf(
        "coloredRegionsJson" to coloredRegionsJson,
        "percentCompleted" to percentCompleted.toDouble(),
        "isFavorite" to isFavorite,
        "isCompleted" to isCompleted,
        "lastEditedTimestamp" to lastEditedTimestamp
    )
}
