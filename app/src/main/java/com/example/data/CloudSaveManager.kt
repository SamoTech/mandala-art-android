package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Account-scoped, idempotent cloud reconciliation.
 *
 * Firestore transactions merge each document against its current server value;
 * Room remains the offline source of truth and applies incoming data through a
 * transaction that re-reads the latest local row.
 */
class CloudSaveManager(
    private val dao: MandalaDao,
    private val deviceActorId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val syncMutex = Mutex()

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

    suspend fun syncProgress() = syncMutex.withLock {
        val uid = auth.currentUser?.uid ?: error("Sign in to sync your progress.")
        val collection = firestore.collection("users").document(uid).collection("mandalaProgress")

        // Snapshot supplies the document IDs. Each document is re-read inside a
        // transaction before its merge/write, avoiding stale snapshot overwrites.
        val remoteSnapshot = collection.get().await()
        ensureSameAccount(uid)
        val ids = (dao.getAllProgressOnce().map { it.mandalaId } +
            remoteSnapshot.documents.map { it.id }).toSet()

        for (id in ids) {
            ensureSameAccount(uid)
            val localCandidate = dao.getProgressOnce(id)
            val document = collection.document(id)

            val serverMerged = firestore.runTransaction { transaction ->
                check(auth.currentUser?.uid == uid) {
                    "Account changed during cloud sync; retry with the active account."
                }
                val snapshot = transaction.get(document)
                val remote = if (snapshot.exists()) snapshot.toProgressEntity() else null
                val merged = ProgressSyncMerge.merge(localCandidate, remote)
                if (merged != null) transaction.set(document, merged.toCloudMap())
                merged
            }.await()

            ensureSameAccount(uid)
            if (serverMerged != null) {
                // This DAO transaction re-reads the current local row, so a
                // concurrent editor write is not replaced by our stale candidate.
                dao.mergeProgress(serverMerged)
            }
        }
        ensureSameAccount(uid)
    }

    private fun ensureSameAccount(expectedUid: String) {
        check(auth.currentUser?.uid == expectedUid) {
            "Account changed during cloud sync; retry with the active account."
        }
    }

    private fun DocumentSnapshot.toProgressEntity(): MandalaProgressEntity {
        val legacyTimestamp = (getLong("lastEditedTimestamp") ?: 0L).coerceAtLeast(0L)
        return MandalaProgressEntity(
            mandalaId = id,
            coloredRegionsJson = getString("coloredRegionsJson").orEmpty(),
            percentCompleted = ((get("percentCompleted") as? Number)?.toFloat() ?: 0f),
            isFavorite = getBoolean("isFavorite") ?: false,
            isCompleted = getBoolean("isCompleted") ?: false,
            lastEditedTimestamp = legacyTimestamp,
            progressRevision = (getLong("progressRevision") ?: legacyTimestamp).coerceAtLeast(0L),
            progressActor = getString("progressActor") ?: "legacy",
            favoriteRevision = (getLong("favoriteRevision") ?: legacyTimestamp).coerceAtLeast(0L),
            favoriteActor = getString("favoriteActor") ?: "legacy",
            isDeleted = getBoolean("isDeleted") ?: false,
            deletionRevision = (getLong("deletionRevision") ?: 0L).coerceAtLeast(0L),
            deletionActor = getString("deletionActor") ?: "legacy"
        )
    }

    private fun MandalaProgressEntity.toCloudMap(): Map<String, Any> = mapOf(
        "coloredRegionsJson" to coloredRegionsJson,
        "percentCompleted" to percentCompleted.toDouble(),
        "isFavorite" to isFavorite,
        "isCompleted" to isCompleted,
        "lastEditedTimestamp" to lastEditedTimestamp,
        "progressRevision" to progressRevision,
        "progressActor" to progressActor,
        "favoriteRevision" to favoriteRevision,
        "favoriteActor" to favoriteActor,
        "isDeleted" to isDeleted,
        "deletionRevision" to deletionRevision,
        "deletionActor" to deletionActor
    )
}
