package com.example.licenseplaterecognize.models;

import com.example.licenseplaterecognize.models.Bound;

public class LicenseInfo {
    public int color;
    public String license_plate_number;
    public Bound bound;

    public LicenseInfo(int color, String license_plate_number, Bound bound) {
        this.color = color;
        this.license_plate_number = license_plate_number;
        this.bound = bound;
    }

    LicenseInfo() {
    }
}
