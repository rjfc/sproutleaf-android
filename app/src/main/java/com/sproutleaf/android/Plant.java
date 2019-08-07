package com.sproutleaf.android;

public class Plant {
    public String plantName;
    public String plantSpecies;
    public String plantBirthday;
    public String uid;

    public Plant(String providedPlantName, String providedPlantSpecies, String providedPlantBirthday, String providedUid) {
        plantName = providedPlantName;
        plantSpecies = providedPlantSpecies;
        plantBirthday = providedPlantBirthday;
        uid = providedUid;
    }
}
