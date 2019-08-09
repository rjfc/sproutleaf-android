package com.sproutleaf.android;

public class Plant {
    public String plantName;
    public String plantSpecies;
    public String plantBirthday;
    public String uid;

    public Plant() {}

    public Plant(String providedPlantName, String providedPlantSpecies, String providedPlantBirthday, String providedUid) {
        plantName = providedPlantName;
        plantSpecies = providedPlantSpecies;
        plantBirthday = providedPlantBirthday;
        uid = providedUid;
    }

    public String getPlantName() {
        return plantName;
    }

    public String getPlantSpecies() {
        return plantSpecies;
    }

    public String getPlantBirthday() {
        return plantBirthday;
    }

    public String getUid() {
        return uid;
    }
}
