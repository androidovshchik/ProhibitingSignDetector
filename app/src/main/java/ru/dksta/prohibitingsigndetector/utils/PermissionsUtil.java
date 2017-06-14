package ru.dksta.prohibitingsigndetector.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

public final class PermissionsUtil {

    public static final String[] ALL_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA
    };

    public static boolean hasPermission(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAllPermissions(Context context) {
        for (String permission : ALL_PERMISSIONS) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }
}
