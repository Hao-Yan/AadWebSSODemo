package emmx.microsoft.com.aadwebssodemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hayan on 18-9-19.
 */

public class ContextUtils {
    private static final String TAG = "ContextUtils";
    private static Context sApplicationContext;

    /**
     * Initialization-on-demand holder. This exists for thread-safe lazy initialization.
     */
    private static class Holder {
        // Not final for tests.
        private static SharedPreferences sSharedPreferences = fetchAppSharedPreferences();
    }

    public static Context getApplicationContext() {
        return sApplicationContext;
    }

    public static void initApplicationContext(Context appContext) {
        if (sApplicationContext != null && sApplicationContext != appContext) {
            throw new RuntimeException("Attempting to set multiple global application contexts.");
        }
        initJavaSideApplicationContext(appContext);
    }

    /**
     * This is used to ensure that we always use the application context to fetch the default shared
     * preferences. This avoids needless I/O for android N and above. It also makes it clear that
     * the app-wide shared preference is desired, rather than the potentially context-specific one.
     *
     * @return application-wide shared preferences.
     */
    public static SharedPreferences getAppSharedPreferences() {
        return Holder.sSharedPreferences;
    }

    private static void initJavaSideApplicationContext(Context appContext) {
        if (appContext == null) {
            throw new RuntimeException("Global application context cannot be set to null.");
        }
        sApplicationContext = appContext;
    }

    /**
     * Only called by the static holder class and tests.
     *
     * @return The application-wide shared preferences.
     */
    private static SharedPreferences fetchAppSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
    }

}
