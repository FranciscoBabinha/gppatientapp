// js/auth.js
import { auth, db } from "./firebase-init.js";
import {
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";
import {
  doc,
  getDoc,
} from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

export async function login(email, password) {
  await signInWithEmailAndPassword(auth, email.trim(), password.trim());
}

export async function logout() {
  await signOut(auth);
  window.location.href = "index.html";
}

export function watchAuth(whenLoggedIn, whenLoggedOut) {
  onAuthStateChanged(auth, (user) => {
    if (user) whenLoggedIn(user);
    else whenLoggedOut();
  });
}

export async function getPatientIdForUser(user) {
  // Your data currently lives in users/{username}
  const usernamePrefix = (user.email || "").split("@")[0];
  const userDocRef = doc(db, "users", usernamePrefix);
  const userDoc = await getDoc(userDocRef);
  if (userDoc.exists()) {
    return String(userDoc.data().patient_id || "");
  }
  // Fallback: userProfiles/{uid}
  const profileRef = doc(db, "userProfiles", user.uid);
  const profileDoc = await getDoc(profileRef);
  if (profileDoc.exists()) {
    return String(profileDoc.data().patient_id || "");
  }
  throw new Error("User mapping not found");
}

export async function getIsAdmin(user) {
  const usernamePrefix = (user.email || "").split("@")[0];
  const userDocRef = doc(db, "users", usernamePrefix);
  const userDoc = await getDoc(userDocRef);
  if (userDoc.exists()) {
    return !!userDoc.data().isAdmin;
  }
  const profileRef = doc(db, "userProfiles", user.uid);
  const profileDoc = await getDoc(profileRef);
  return profileDoc.exists() && !!profileDoc.data().isAdmin;
}