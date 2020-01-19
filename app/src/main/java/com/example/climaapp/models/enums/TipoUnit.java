package com.example.climaapp.models.enums;

public enum TipoUnit {

    METRIC("metric"),
    IMPERIAL("imperial"),
    STANDARD("standard");

    private String unit;

    TipoUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return this.unit;
    }

    public static TipoUnit fromString(String text) {

        for (TipoUnit b : TipoUnit.values()) {

            if (b.unit.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return null;
    }
}
