const fs = require("node:fs");
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds
} = require("@firebase/rules-unit-testing");
const {
  doc,
  getDoc,
  setDoc,
  deleteDoc
} = require("firebase/firestore");

const projectId = "demo-mandala-art";

async function main() {
  const testEnv = await initializeTestEnvironment({
    projectId,
    firestore: {
      host: "127.0.0.1",
      port: 8080,
      rules: fs.readFileSync("firestore.rules", "utf8")
    }
  });

  try {
    const alice = testEnv.authenticatedContext("alice").firestore();
    const bob = testEnv.authenticatedContext("bob").firestore();

    const aliceProgress = doc(alice, "users/alice/mandalaProgress/lotus");
    const bobProgress = doc(bob, "users/bob/mandalaProgress/lotus");
    const aliceLock = doc(alice, "accountDeletionLocks/alice");

    // Normal account-scoped progress works before deletion is requested.
    await assertSucceeds(setDoc(aliceProgress, { progressRevision: 1 }));
    await assertFails(getDoc(doc(bob, "users/alice/mandalaProgress/lotus")));
    await assertFails(
      setDoc(doc(bob, "users/alice/mandalaProgress/forbidden"), { progressRevision: 1 })
    );

    // The owner can create and read the deletion lock, but cannot clear it.
    await assertSucceeds(setDoc(aliceLock, { deletionRequested: true }));
    await assertSucceeds(getDoc(aliceLock));
    await assertFails(setDoc(aliceLock, { deletionRequested: false }));
    await assertFails(deleteDoc(aliceLock));

    // Once locked, no device can create or update progress for this UID.
    await assertFails(setDoc(aliceProgress, { progressRevision: 2 }));
    await assertFails(
      setDoc(doc(alice, "users/alice/mandalaProgress/new-design"), { progressRevision: 1 })
    );

    // Cleanup remains possible for the owner after the lock is set.
    await assertSucceeds(deleteDoc(aliceProgress));

    // The lock does not affect another account's independent progress.
    await assertSucceeds(setDoc(bobProgress, { progressRevision: 1 }));
  } finally {
    await testEnv.cleanup();
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
