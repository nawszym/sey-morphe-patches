package app.morphe.extension.kicktv.homedestination;

import android.app.Activity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class HomeDestinationHelper {

    private static volatile boolean hasNavigated;

    private HomeDestinationHelper() {
    }

    /** Called from {@code MainActivity.onResume()} by the patch. */
    public static void onAppOpen(Activity activity) {
        try {
            if (hasNavigated) {
                return;
            }
            String destination = HomeDestinationPrefs.getHomeDestination(activity.getApplicationContext());
            if (destination == null || "Home".equals(destination)) {
                return;
            }
            hasNavigated = true;
            navigateTo(activity, destination);
        } catch (Throwable ignored) {
            // Fail silently: a patched app must never crash because a tweak is unavailable.
        }
    }

    /**
     * Switches KICK to the given tab. Uses reflection to call KICK's internal
     * navigation API discovered from KICK 1.9.0 with jadx/dexdump.
     *
     * <p>The destination is one of: Categories, Live, Following, Settings.</p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void navigateTo(Activity activity, String destination) {
        Object navHostController = null;
        Object pm1 = null;

        // Approach 1: Get NavHostController via Lu8.b(activity, activity)
        try {
            navHostController = getNavHostController(activity);
            if (navHostController != null) {
                // Try ok9.a(ub4Instance) - create instance of ub4 parameter type
                try {
                    for (java.lang.reflect.Method m : navHostController.getClass().getDeclaredMethods()) {
                        if (!m.getName().equals("a")) continue;
                        if (m.getParameterCount() != 1) continue;
                        m.setAccessible(true);
                        Class<?> ub4Class = m.getParameterTypes()[0];
                        try {
                            Object ub4Instance = ub4Class.getDeclaredConstructor().newInstance();
                            m.invoke(navHostController, ub4Instance);
                            return;
                        } catch (Throwable ignored) {
                            // ub4 has no no-arg constructor
                        }
                        // Try passing null
                        try {
                            m.invoke(navHostController, (Object) null);
                            return;
                        } catch (Throwable ignored) {
                            // null not accepted
                        }
                    }
                } catch (Throwable ignored) {
                    // ok9.a invocation failed
                }
                // Try standard NavHostController.navigate(String)
                try {
                    Class<?> nhcClass = Class.forName("androidx.navigation.NavHostController");
                    Method navigate = nhcClass.getMethod("navigate", String.class);
                    for (String route : new String[]{destination, destination.toLowerCase(), "/" + destination, "/" + destination.toLowerCase()}) {
                        try {
                            navigate.invoke(navHostController, route);
                            return;
                        } catch (Throwable ignored) {
                            // Try next route
                        }
                    }
                } catch (Throwable ignored) {
                    // NavHostController.navigate not available
                }
            }
        } catch (Throwable ignored) {
            // Approach 1 failed
        }

        // Approach 2: Try MainActivity.c() which delegates to Lu8.c()
        try {
            Method cMethod = activity.getClass().getMethod("c");
            cMethod.setAccessible(true);
            Object result = cMethod.invoke(activity);
            pm1 = result;
            if (result != null) {
                // Try navigate(String) on the result instance
                try {
                    Method navigate = result.getClass().getMethod("navigate", String.class);
                    for (String route : new String[]{destination, destination.toLowerCase(), "/" + destination, "/" + destination.toLowerCase()}) {
                        try {
                            navigate.invoke(result, route);
                            return;
                        } catch (Throwable ignored) {
                            // Try next route
                        }
                    }
                } catch (Throwable ignored) {
                    // navigate method not found
                }
                // Try pm1.a() - no-arg method
                try {
                    java.lang.reflect.Method aMethod = result.getClass().getDeclaredMethod("a");
                    aMethod.setAccessible(true);
                    aMethod.invoke(result);
                    return;
                } catch (Throwable ignored) {
                    // pm1.a() not available or failed
                }
                // Try ok9.a(pm1)
                if (navHostController != null) {
                    try {
                        for (java.lang.reflect.Method m : navHostController.getClass().getDeclaredMethods()) {
                            if (!m.getName().equals("a")) continue;
                            if (m.getParameterCount() != 1) continue;
                            m.setAccessible(true);
                            m.invoke(navHostController, pm1);
                            return;
                        }
                    } catch (Throwable ignored) {
                        // ok9.a(pm1) failed
                    }
                }
            }
        } catch (Throwable ignored) {
            // Approach 2 failed
        }
    }

    /**
     * Attempts to obtain a navigation controller from KICK's internal binding.
     * Returns {@code null} if unavailable.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getNavHostController(Activity activity) {
        try {
            // Get the ViewBinding via MainActivity.l() -> Lu8
            Method lMethod = activity.getClass().getMethod("l");
            lMethod.setAccessible(true);
            Object binding = lMethod.invoke(activity);
            if (binding == null) {
                return null;
            }

            // Try binding.D field directly - might be a NavHostController
            try {
                Field dField = binding.getClass().getDeclaredField("D");
                dField.setAccessible(true);
                Object dObj = dField.get(binding);
                if (dObj != null) {
                    Class<?> nhcClass = Class.forName("androidx.navigation.NavHostController");
                    if (nhcClass.isInstance(dObj)) {
                        return dObj;
                    }
                }
            } catch (Throwable ignored) {
                // binding.D not accessible or not NavHostController
            }

            // Call Lu8.b(MainActivity, MainActivity) -> ok9 (NavController wrapper).
            // From disassembly of MainActivity.n(): both args are MainActivity references.
            Class<?> lu8Class = binding.getClass();
            Method bMethod = lu8Class.getDeclaredMethod("b", activity.getClass(), activity.getClass());
            bMethod.setAccessible(true);
            Object navController = bMethod.invoke(null, activity, activity);
            return navController;
        } catch (Throwable t) {
            return null;
        }
    }
}
