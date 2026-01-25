// js/data.js
import { db } from "./firebase-init.js";
import {
  collection,
  doc,
  getDoc,
  getDocs,
  runTransaction,
  setDoc,
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

const DEFAULT_BALANCE = 500;
const BALANCE_FIELD = "balance_due";

export async function getPatientBalance(patientId) {
  const ref = doc(db, "patients", String(patientId));
  const snap = await getDoc(ref);
  let balance = DEFAULT_BALANCE;
  if (snap.exists()) {
    const raw = snap.data()?.[BALANCE_FIELD];
    if (typeof raw === "number") {
      balance = raw;
      return balance;
    }
  }
  await setDoc(ref, { [BALANCE_FIELD]: balance }, { merge: true });
  return balance;
}

export async function processPayment(patientId, paymentAmount) {
  if (typeof paymentAmount !== "number" || Number.isNaN(paymentAmount) || paymentAmount <= 0) {
    throw new Error("invalid_amount");
  }
  const ref = doc(db, "patients", String(patientId));
  return runTransaction(db, async (transaction) => {
    const snap = await transaction.get(ref);
    let currentBalance = DEFAULT_BALANCE;
    if (snap.exists()) {
      const raw = snap.data()?.[BALANCE_FIELD];
      if (typeof raw === "number") {
        currentBalance = raw;
      }
    }
    if (paymentAmount > currentBalance) {
      throw new Error("amount_exceeds_balance");
    }
    const newBalance = currentBalance - paymentAmount;
    if (snap.exists()) {
      transaction.update(ref, { [BALANCE_FIELD]: newBalance });
    } else {
      transaction.set(ref, { [BALANCE_FIELD]: newBalance }, { merge: true });
    }
    return newBalance;
  });
}