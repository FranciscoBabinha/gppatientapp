// js/firebase-init.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.4/firebase-firestore.js";

export const firebaseConfig = {
  apiKey: "AIzaSyBILFQOO-XQJsDA0fqcrEQgyUTbbR1BbKM",
  authDomain: "allhealth-8d2e3.firebaseapp.com",
  projectId: "allhealth-8d2e3",
  storageBucket: "allhealth-8d2e3.firebasestorage.app",
  messagingSenderId: "700907151773",
  appId: "1:700907151773:web:1257d36a8c01c8556d0f74",
  measurementId: "G-FG8NS3JC7G"
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
// Analytics disabled on web to avoid "process is not defined" in some environments