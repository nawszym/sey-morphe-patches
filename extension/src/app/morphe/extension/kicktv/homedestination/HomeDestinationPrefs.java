package app.morphe.extension.kicktv.homedestination;

import android.content.Context;
import android.content.SharedPreferences;

import app.morphe.extension.kicktv.base.MorpheContext;

/**
 * Persistent "default home destination" setting.
 *
 * <p>Chooses which KICK tab is opened automatically when the app starts:
 * Home, Categories, Live, Following or Settings. The value survives restarts
 * and is read by the patch injected into {@code MainActivity.onCreate()}.</p>
 */
public final class HomeDestinationPrefs {

    private static final String PREFS_NAME = "morphe_kicktv";
    private static final String KEY_DESTINATION = "home_destination";
    private static final String DEFAULT_DESTINATION = "Home";

    private static SharedPreferences prefs;
    private static volatile String cachedDestination;

    private HomeDestinationPrefs() {
    }

    private static synchronized SharedPreferences getPrefs() {
        if (prefs == null) {
            Context context = MorpheContext.getAppContext();
            if (context != null) {
                prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            }
        }
        return prefs;
    }

    /** Returns the selected destination key ("Home", "Categories", ...). */
    public static String getHomeDestination() {
        String cached = cachedDestination;
        if (cached != null) {
            return cached;
        }
        SharedPreferences p = getPrefs();
        if (p == null) {
            return DEFAULT_DESTINATION;
        }
        String value = p.getString(KEY_DESTINATION, DEFAULT_DESTINATION);
        cachedDestination = value;
        return value;
    }

    public static void setHomeDestination(String destination) {
        cachedDestination = destination;
        SharedPreferences p = getPrefs();
        if (p != null) {
            p.edit().putString(KEY_DESTINATION, destination).apply();
        }
    }
}
