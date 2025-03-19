package com.example.gpapp;

public class Patient {
    private String name;
    private String allergies;
    private String diagnoses;
    private String id;


    public Patient() {}

    public Patient(String name, String allergies) {
        this.name = name;
        this.allergies = allergies;
        this.diagnoses = "";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(String diagnoses) {
        this.diagnoses = diagnoses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}