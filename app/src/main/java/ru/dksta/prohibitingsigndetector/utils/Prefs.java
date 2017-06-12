package ru.dksta.prohibitingsigndetector.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Prefs {

    /* Preferences */
    public static final String APP_RESTRICTIONS = "App Restrictions";

    public static final String LAYER_TYPE = "layerType";
    public static final String ROTATE_MAT = "rotateMat";

    /* SharedPreferences parameters */

    private SharedPreferences preferences;

    public Prefs(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @SuppressWarnings("unused")
    public int getInteger(String name) {
        return preferences.getInt(name, 0);
    }

    @SuppressWarnings("unused")
    public int getInteger(String name, int def) {
        return preferences.getInt(name, def);
    }

    @SuppressWarnings("unused")
    public boolean getBoolean(String name) {
        return preferences.getBoolean(name, false);
    }

    @SuppressWarnings("unused")
    public boolean getBoolean(String name, boolean def) {
        return preferences.getBoolean(name, def);
    }

    @SuppressWarnings("all")
    public String getString(String name) {
        return preferences.getString(name, "").trim();
    }

    @SuppressWarnings("unused")
    public <T> String getString(String name, T def) {
        return preferences.getString(name, str(def)).trim();
    }

    @SuppressWarnings("unused")
    public void putInteger(String name, int value) {
        preferences.edit().putInt(name, value).apply();
    }

    @SuppressWarnings("unused")
    public void putBoolean(String name, boolean value) {
        preferences.edit().putBoolean(name, value).apply();
    }

    @SuppressWarnings("unused")
    public <T> void putString(String name, T value) {
        preferences.edit().putString(name, str(value)).apply();
    }

    /* Controls functions */

    @SuppressWarnings("unused")
    private boolean has(String name) {
        return preferences.contains(name);
    }

    @SuppressWarnings("unused")
    public void clear() {
        preferences.edit().clear().apply();
    }

    @SuppressWarnings("unused")
    public void remove(String name) {
        if (has(name)) {
            preferences.edit().remove(name).apply();
        }
    }

    /* Utils functions */

    @SuppressWarnings("unused")
    private <T> String str(T value) {
        return String.class.isInstance(value)? ((String) value).trim() : String.valueOf(value);
    }

    @SuppressWarnings("unused")
    public void printAll() {
        Map<String, ?> prefAll = preferences.getAll();
        if (prefAll == null) {
            return;
        }
        List<Map.Entry<String, ?>> list = new ArrayList<>();
        list.addAll(prefAll.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, ?>>() {
            public int compare(final Map.Entry<String, ?> entry1, final Map.Entry<String, ?> entry2) {
                return entry1.getKey().compareTo(entry2.getKey());
            }
        });
        String classname = getClass().getSimpleName();
        LogUtil.logDivider(classname, "~");
        LogUtil.logCentered(" ", classname, "Printing all sharedPreferences");
        for(Map.Entry<String, ?> entry : list) {
            LogUtil.tag(classname).i(entry.getKey() + ": " + entry.getValue());
        }
        LogUtil.logDivider(classname, "~");
    }
}
