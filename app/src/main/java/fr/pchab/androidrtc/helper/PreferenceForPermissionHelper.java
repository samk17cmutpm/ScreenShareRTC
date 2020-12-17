package fr.pchab.androidrtc.helper;

import android.content.Context;
import android.content.SharedPreferences;

import fr.pchab.androidrtc.share_prefer_manager.SharePreferManager;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceForPermissionHelper {
    static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime){
        SharedPreferences sharedPreference = context.getSharedPreferences(SharePreferManager.SETTINGS_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }
    static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(SharePreferManager.SETTINGS_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }
}
