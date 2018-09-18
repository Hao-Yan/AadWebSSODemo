package emmx.microsoft.com.aadwebssodemo;

import android.net.Uri;
import android.util.Log;

/**
 * Created by hayan on 18-9-18.
 */

public class AADUtils {
    private static final String TAG = AADUtils.class.getName();

    /**
     * Check if a URL is an AAD endpoint.
     *
     * @param urlString URL to check.
     * @return True if the hostname is the AAD hostname and the scheme is https, false otherwise.
     */
    public static boolean isAADEndpoint(final String urlString) {
        if (urlString == null) {
            return false;
        }
        Log.v(TAG, "Checking is AAD endpoint: " + urlString);
        Uri uri = Uri.parse(urlString);
        return Constants.AAD_HOSTNAME.equalsIgnoreCase(uri.getHost()) &&
                "https".equalsIgnoreCase(uri.getScheme());
    }
}
