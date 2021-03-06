package com.sproutleaf.android;

public class Plant {
    public String plantName;
    public String plantSpecies;
    public String plantBirthday;
    public String userID;

    public Plant() {}

    public Plant(String providedPlantName, String providedPlantSpecies, String providedPlantBirthday, String providedUserID) {
        plantName = providedPlantName;
        plantSpecies = providedPlantSpecies;
        plantBirthday = providedPlantBirthday;
        userID = providedUserID;
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

    public String getUserID() {
        return userID;
    }
}
