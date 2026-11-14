package app.morphe.extension.kicktv.homedestination;

import android.app.Activity;

import app.morphe.extension.kicktv.base.MorpheContext;

/**
 * Reads the default home destination on app open and navigates there unless it
 * is the stock Home tab.
 *
 * <p>All calls are wrapped in {@code try/catch (Throwable)} so a missing or
 * changed KICK navigation API never crashes the app.</p>
 */
public final class HomeDestinationHelper {

    private HomeDestinationHelper() {
    }

    /** Called from {@code MainActivity.onCreate()} by the patch. */
    public static void onAppOpen() {
        try {
            String destination = HomeDestinationPrefs.getHomeDestination();
            if (destination == null || "Home".equals(destination)) {
                return;
            }
            navigateTo(destination);
        } catch (Throwable ignored) {
            // Fail silently: a patched app must never crash because a tweak is unavailable.
        }
    }

    /**
     * Switches KICK to the given tab. The actual KICK navigation call must be
     * derived from KICK 1.9.0 with jadx (see AGENTS.md §5) and filled in here.
     * The destination is one of: Categories, Live, Following, Settings.
     */
    private static void navigateTo(String destination) {
        Activity activity = MorpheContext.getActivity();
        if (activity == null) {
            return;
        }
        // TODO: derive KICK's tab-navigation API from the APK and call it here,
        // e.g. activity.findViewById(...).setSelection(...) or a NavController call.
    }
}
