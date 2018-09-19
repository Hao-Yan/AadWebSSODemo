package emmx.microsoft.com.aadwebssodemo;

import android.content.Intent;
import android.util.Log;

import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationSettings;
import com.microsoft.aad.adal.UserInfo;

import java.util.concurrent.atomic.AtomicBoolean;

public class SSOHandler {
    private static final String TAG = SSOHandler.class.getName();

    private static AtomicBoolean sInProgress = new AtomicBoolean(false);
    private static SSOCallback sCallback;

    private AuthenticationContext mAuthContext;
    private String mAuthority;
    private String mClientId;
    // Use mRedirectUri as the redirect URI for calls that use the broker.
    private String mRedirectUri;

    private SSOHandler() {
        AuthenticationSettings.INSTANCE.setUseBroker(true);
        mAuthority = Constants.AAD_AUTHORITY_URL;
        mClientId = Constants.AAD_CLIENT_ID;
        mAuthContext = new AuthenticationContext(
                ContextUtils.getApplicationContext(),
                mAuthority, false);
        mRedirectUri = mAuthContext.getRedirectUriForBroker();
    }

    private static class ThreadSafeLazyHolder {
        public static final SSOHandler INSTANCE = new SSOHandler();
    }

    public static SSOHandler getInstance() {
        return ThreadSafeLazyHolder.INSTANCE;
    }

    /**
     * Check if the request's headers should be modified for CA/SSO.
     */
    public static boolean shouldModifyHeaders(final String url, boolean isMainframe, boolean isGet, boolean hasCaSsoHeader) {
        boolean isAadActive = true;
        // only append headers if:
        //  - the AAD user is active
        //  - the request is a mainframe (primary) request
        //  - the request is a GET request
        //  - the request does not already have a CA/SSO header
        //  - the request is to an AAD endpoint
        return isAadActive && isMainframe && isGet && !hasCaSsoHeader && AADUtils.isAADEndpoint(url);
    }

    /**
     * If this URL is an AAD endpoint, kick off the SSO flow.
     *
     * @param callback The callback to use on
     * @return the URL if necessary, or null if no header should be attached.
     */
    public boolean start(SSOCallback callback) {
        Log.i(TAG, "SSOHandler.start called");
        if (sInProgress.get()) {
            Log.i(TAG, "SSO flow already in progress.");
            return false;
        }
        sInProgress.set(true);
        sCallback = callback;
        trySilentSSOFlow();
        return true;
    }

    /**
     * If we hit this function we know we are enrolled and can retrieve the primary identity.
     * Try to request a CA SSO token.
     * This request may fail due to an invalid refresh token or because the device is not registered.
     * Either way, if it fails we will try to get a dummy token with device claims to both acquire
     * a refresh token and force device registration.
     * <p>
     * Its callback will kick off the next link in the chain.
     */
    private void trySilentSSOFlow() {
        Log.i(TAG, "trySilentSSOFlow");

        String aadId = SignInManager.getInstance().getUserId();
        if (aadId == null) {
            Log.i(TAG, "No AAD user, not acquiring a token for CA SSO");
            onSSOFlowFailed();
            return;
        }

        mAuthContext.acquireTokenSilentAsync(Constants.PURPOSE_TOKEN_RESOURCE, mClientId, aadId, new CASSOSilentFlowAuthCallback(this));
    }

    /**
     * Request Device Claims token. Its callback will kick off the next link in the chain.
     */
    public void requestDeviceClaimsToken() {
        Log.i(TAG, "requestDeviceClaimsToken");

        if (!sCallback.onLoudPrompt()) {
            sInProgress.set(false);
            return;
        }
        Intent intent = new Intent(ContextUtils.getApplicationContext(), AadActivity.class);
        intent.putExtra(AadActivity.EXTRA_TOKEN_REQUEST, AadActivity.DEVICE_CLAIMS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ContextUtils.getApplicationContext().startActivity(intent);
    }

    /**
     * Request CA SSO Token as part of the 'loud' SSO flow.
     * Its callback will kick off the next link in the chain.
     */
    public void requestSSOTokenAfterLoudFlow() {
        Log.i(TAG, "requestSSOTokenAfterLoudFlow");
        String aadId = SignInManager.getInstance().getUserId();
        if (aadId == null) {
            Log.i(TAG, "No AAD user, not acquiring a token for CA SSO");
            onSSOFlowFailed();
            return;
        }
        if (!sCallback.afterLoudPrompt()) {
            sInProgress.set(false);
            return;
        }

        mAuthContext.acquireTokenSilentAsync(Constants.PURPOSE_TOKEN_RESOURCE, mClientId, aadId, new CASSOLoudFlowAuthCallback(this));
    }

    /**
     * Get a CA SSO Token to attach to a base URL as a header.
     *
     * @param result The result from the CA SSO token call.
     */
    public void handleSuccessfulTokenRequest(final AuthenticationResult result) {
        Log.i(TAG, "handleSuccessfulTokenRequest");
        String accessToken = result.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            Log.w(TAG, "ADAL reported success but did not return an access token.");
            onSSOFlowFailed();
            return;
        }

        UserInfo info = result.getUserInfo();

        if (null == info) {
            Log.e(TAG, "ADAL reported success but the work account is for a different user.");
            onSSOFlowFailed();
            return;
        }
        sCallback.onSuccess(accessToken);
        sInProgress.set(false);
    }

    /**
     * When the flow has failed, we need to notify the native code waiting on a token, so we log a special
     * failure string.
     */
    public void onSSOFlowFailed() {
        Log.e(TAG, "SSO flow failed");
        sCallback.onFailure();
        sInProgress.set(false);
    }

    public interface SSOCallback {
        boolean onSuccess(String accessToken);
        boolean onFailure();
        boolean onLoudPrompt();
        boolean afterLoudPrompt();
    }
}
