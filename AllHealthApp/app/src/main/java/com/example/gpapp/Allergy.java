package com.example.gpapp;

public class Allergy {
    private String allergen;
    private String reaction;
    private String patientName;

    public Allergy(String allergen, String reaction, String patientName) {
        this.allergen = allergen;
        this.reaction = reaction;
        this.patientName = patientName;
    }

    public String getAllergen() {
        return allergen;
    }

    public String getReaction() {
        return reaction;
    }

    public String getPatientName() {
        return patientName;
    }
}