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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val syncMutex = Mutex()

    val currentUser: FirebaseUser? get() = auth.currentUser

    /**
     * Keeps the local Room partition aligned with Firebase Authentication.
     * The returned disposer must be called when the observing UI is removed.
     */
    fun observeAuthState(onChanged: (String) -> Unit): () -> Unit {
        onChanged(auth.currentUser?.uid.orEmpty())
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            onChanged(firebaseAuth.currentUser?.uid.orEmpty())
        }
        auth.addAuthStateListener(listener)
        return { auth.removeAuthStateListener(listener) }
    }

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
        // Only this Firebase UID's local partition participates in this sync.
        // Anonymous and other-account rows are never candidates for upload.
        val ids = (dao.getAllProgressOnce(uid).map { it.mandalaId } +
            remoteSnapshot.documents.map { it.id }).toSet()

        for (id in ids) {
            ensureSameAccount(uid)
            val localCandidate = dao.getProgressOnce(id, uid)
            val document = collection.document(id)

            val serverMerged = firestore.runTransaction { transaction ->
                check(auth.currentUser?.uid == uid) {
                    "Account changed during cloud sync; retry with the active account."
                }
                val snapshot = transaction.get(document)
                val remote = if (snapshot.exists()) snapshot.toProgressEntity(uid) else null
                val merged = ProgressSyncMerge.merge(localCandidate, remote)
                if (merged != null) transaction.set(document, merged.toCloudMap())
                merged
            }.await()

            ensureSameAccount(uid)
            if (serverMerged != null) {
                // This DAO transaction re-reads the current local row, so a
                // concurrent editor write is not replaced by our stale candidate.
                dao.mergeProgress(serverMerged.copy(ownerUid = uid))
            }
        }
        ensureSameAccount(uid)
    }

    /**
     * Explicitly imports anonymous/local-only saves into the signed-in account.
     * The anonymous partition is retained unchanged, so users can review or
     * keep using their local data after signing out. Nothing is imported
     * automatically because a shared device may contain another person's work.
     */
    suspend fun importAnonymousProgress() = syncMutex.withLock {
        val uid = auth.currentUser?.uid ?: error("Sign in before importing local progress.")
        val collection = firestore.collection("users").document(uid).collection("mandalaProgress")
        val anonymousRows = dao.getAllProgressOnce(ownerUid = "")

        for (anonymousRow in anonymousRows) {
            ensureSameAccount(uid)
            val localCandidate = anonymousRow.copy(ownerUid = uid)
            val document = collection.document(anonymousRow.mandalaId)

            val serverMerged = firestore.runTransaction { transaction ->
                check(auth.currentUser?.uid == uid) {
                    "Account changed during local import; retry with the active account."
                }
                val snapshot = transaction.get(document)
                val remote = if (snapshot.exists()) snapshot.toProgressEntity(uid) else null
                val merged = ProgressSyncMerge.merge(localCandidate, remote)
                if (merged != null) transaction.set(document, merged.toCloudMap())
                merged
            }.await()

            ensureSameAccount(uid)
            if (serverMerged != null) {
                dao.mergeProgress(serverMerged.copy(ownerUid = uid))
            }
        }
        ensureSameAccount(uid)
    }

    private fun ensureSameAccount(expectedUid: String) {
        check(auth.currentUser?.uid == expectedUid) {
            "Account changed during cloud sync; retry with the active account."
        }
    }

    private fun DocumentSnapshot.toProgressEntity(ownerUid: String): MandalaProgressEntity {
        val legacyTimestamp = (getLong("lastEditedTimestamp") ?: 0L).coerceAtLeast(0L)
        return MandalaProgressEntity(
            mandalaId = id,
            ownerUid = ownerUid,
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
            deletionActor = getString("deletionActor") ?: "legacy",
            restoredDeletionRevision = (getLong("restoredDeletionRevision") ?: 0L).coerceAtLeast(0L),
            restoredDeletionActor = getString("restoredDeletionActor") ?: "legacy",
            restorationEventRevision = (getLong("restorationEventRevision") ?: 0L).coerceAtLeast(0L),
            restorationEventActor = getString("restorationEventActor") ?: "legacy"
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
        "deletionActor" to deletionActor,
        "restoredDeletionRevision" to restoredDeletionRevision,
        "restoredDeletionActor" to restoredDeletionActor,
        "restorationEventRevision" to restorationEventRevision,
        "restorationEventActor" to restorationEventActor
    )
}
