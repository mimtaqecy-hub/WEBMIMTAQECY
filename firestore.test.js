const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_user_1";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Public user: can read school settings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("school_settings").doc("general").get());
});

test("Public user: cannot update school settings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("school_settings").doc("general").set({
    schoolName: "Hacked Name",
    accreditation: "A"
  }));
});

test("Authenticated admin: can update school settings", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(adminDb.collection("school_settings").doc("general").set({
    schoolName: "MIM Tanjung Qencono",
    accreditation: "B"
  }));
});

test("Public user: can read student master list", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("students").get());
});

test("Public user: cannot create student record", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("students").doc("std_1").set({
    fullName: "Ahmad",
    classLevel: 1,
    rombel: "Ar Rahman"
  }));
});

test("Authenticated admin: can create student record", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(adminDb.collection("students").doc("std_1").set({
    fullName: "Ahmad Dahlan",
    classLevel: 1,
    rombel: "Ar Rahman"
  }));
});

test("Authenticated admin: cannot create student with invalid class level", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertFails(adminDb.collection("students").doc("std_bad").set({
    fullName: "Invalid Student",
    classLevel: 7, // Exceeds 6
    rombel: "Ar Rahman"
  }));
});

test("Public user: can submit PPDB registration", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("ppdb").doc("ppdb_001").set({
    regNumber: "PPDB-2026-001",
    studentName: "Fatimah Azzahra",
    status: "PENDING"
  }));
});

test("Public user: cannot read all PPDB registrations", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("ppdb").get());
});

test("Authenticated admin: can read PPDB registrations", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(adminDb.collection("ppdb").get());
});
