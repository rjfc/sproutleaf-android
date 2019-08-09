package com.sproutleaf.android;

public class PlantCardItem {
    public String plantName;
    public String plantSpecies;
    public String plantBirthday;
    public String userID;
    public String plantID;

    public PlantCardItem() {}

    public PlantCardItem(String providedPlantName, String providedPlantSpecies, String providedPlantBirthday, String providedUserID, String providedPlantID) {
        plantName = providedPlantName;
        plantSpecies = providedPlantSpecies;
        plantBirthday = providedPlantBirthday;
        userID = providedUserID;
        plantID = providedPlantID;
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

    public String getPlantID() {
        return plantID;
    }
}
