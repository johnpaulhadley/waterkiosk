package com.kiosk.models;

/**
 * Represents different waste disposal categories.
 * Task #126: Simple, one-sentence instructions for each category.
 */
public enum WasteCategory {
    RECYCLE("Recycling", 
            "Place plastic bottles, aluminum cans, paper, and cardboard in this bin.",
            "♻"),
    
    TRASH("Landfill", 
          "Dispose of food wrappers, napkins, and non-recyclable items here.",
          "🗑"),
    
    COMPOST("Compost", 
            "Put food scraps, coffee grounds, and compostable materials in this bin.",
            "🌱"),
    
    HAZARDOUS("Hazardous Waste", 
              "Please take batteries, electronics, and chemicals to the service desk.",
              "⚠");

    private final String categoryName;
    private final String instruction;
    private final String icon;

    WasteCategory(String categoryName, String instruction, String icon) {
        this.categoryName = categoryName;
        this.instruction = instruction;
        this.icon = icon;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getIcon() {
        return icon;
    }
}
