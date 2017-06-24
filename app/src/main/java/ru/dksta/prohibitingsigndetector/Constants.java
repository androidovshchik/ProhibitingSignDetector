package ru.dksta.prohibitingsigndetector;

public interface Constants {

    // syncing with cpp defined vars
    int LAYER_RGBA = 1;
    int LAYER_HSV = 2;
    int LAYER_HUE_LOWER = 3;
    int LAYER_HUE_UPPER = 4;
    int LAYER_HUE = 5;
    int LAYER_SATURATION = 6;
    int LAYER_VALUE = 7;
    int LAYER_RED_FILTERED = 8;
    int LAYER_DILATED = 9;

    int NOISE_NONE = 1;
    int NOISE_SALT_PEPPER = 2;
}
