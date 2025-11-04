// js/data.js
import { db } from "./firebase-init.js";
import {
  collection,
  getDocs,
  Timestamp,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

function fmt(val) {
  if (!val) return "";
  if (val instanceof Timestamp) {
    const d = val.toDate();
    return d.toISOString().slice(0, 10); // yyyy-MM-dd
  }
  if (val instanceof Date) {
    return val.toISOString().slice(0, 10);
  }
  return String(val);
}

export async function loadSubcollection(patientId, sub, headers) {
  const snap = await getDocs(collection(db, "patients", String(patientId), sub));
  return snap.docs.map((d) => {
    const row = {};
    headers.forEach((h) => {
      row[h] = fmt(d.get(h));
    });
    return row;
  });
}