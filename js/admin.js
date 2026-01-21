// js/admin.js
import { db } from "./firebase-init.js";
import { collection, getDocs, doc, getDoc, setDoc, updateDoc, deleteDoc, addDoc } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

export async function loadAllPatients() {
  const patientsSnap = await getDocs(collection(db, "patients"));
  const results = [];
  for (const docSnap of patientsSnap.docs) {
    const pid = docSnap.id;
    const data = docSnap.data() || {};
    results.push({
      id: pid,
      first_name: data.first_name || "",
      second_name: data.second_name || "",
    });
  }
  return results;
}

export async function loadPatientProfile(pid) {
  const ref = doc(db, "patients", String(pid));
  const snap = await getDoc(ref);
  return snap.exists() ? (snap.data() || {}) : {};
}

export async function updatePatient(pid, data) {
  const ref = doc(db, "patients", String(pid));
  await updateDoc(ref, {
    first_name: data.first_name || "",
    second_name: data.second_name || "",
  });
}

async function deleteSubcollection(pid, sub) {
  const snap = await getDocs(collection(db, "patients", String(pid), sub));
  const deletions = snap.docs.map(d => deleteDoc(d.ref));
  await Promise.all(deletions);
}

export async function deletePatientCascade(pid) {
  const subs = ["diagnoses", "allergies", "medications", "results", "documents", "vaccines"];
  for (const sub of subs) {
    await deleteSubcollection(pid, sub);
  }
  await deleteDoc(doc(db, "patients", String(pid)));
}

export async function addPatient(pid, first_name, second_name, username) {
  // Create patient doc
  await setDoc(doc(db, "patients", String(pid)), { first_name, second_name });
  // Create users mapping (auth account must be created separately in Firebase Auth)
  if (username && username.trim()) {
    await setDoc(doc(db, "users", username.trim()), {
      username: username.trim(),
      patient_id: String(pid),
      isAdmin: false,
    }, { merge: true });
  }
}

export async function loadPatientDetails(pid) {
  const sections = [
    { key: 'diagnoses', headers: ['diagnosis','date'] },
    { key: 'allergies', headers: ['allergy','severity'] },
    { key: 'medications', headers: ['medication_name','dosage'] },
    { key: 'results', headers: ['result_description','date'] },
    { key: 'documents', headers: ['document_title','upload_date'] },
    { key: 'vaccines', headers: ['vaccine_name','date_administered'] },
  ];
  const data = {};
  for (const s of sections) {
    const snap = await getDocs(collection(db, 'patients', String(pid), s.key));
    data[s.key] = snap.docs.map(d => ({ id: d.id, ...d.data() }));
  }
  return data;
}

export async function addSubItem(pid, sub, item) {
  await addDoc(collection(db, 'patients', String(pid), sub), item);
}

export async function updateSubItem(pid, sub, id, item) {
  await updateDoc(doc(db, 'patients', String(pid), sub, id), item);
}

export async function deleteSubItem(pid, sub, id) {
  await deleteDoc(doc(db, 'patients', String(pid), sub, id));
}


