package emmx.microsoft.com.aadwebssodemo;

import android.util.Log;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;

public class CASSOSilentFlowAuthCallback implements AuthenticationCallback<AuthenticationResult> {
    private static final String TAG = CASSOSilentFlowAuthCallback.class.getSimpleName();
    private final SSOHandler mSSOHandler;

    /**
     * ctor.
     *  @param SSOHandler   the SSOHandler that made this call.
     */
    public CASSOSilentFlowAuthCallback(final SSOHandler SSOHandler) {
        mSSOHandler = SSOHandler;
    }

    @Override
    public void onSuccess(final AuthenticationResult result) {
        Log.i(TAG, "CASSOSilentFlowAuthCallback onSuccess");
        if (null == result || null == result.getStatus()) {
            Log.e(TAG, "ADAL succeeded, but the result is null for unknown reasons.");
            mSSOHandler.onSSOFlowFailed();
            return;
        }
        switch (result.getStatus()) {
            case Succeeded:
                mSSOHandler.handleSuccessfulTokenRequest(result);
                break;
            case Cancelled:
                Log.i(TAG, "User cancelled ADAL authentication, will not try to acquire ca/sso "
                        + "token.");
                mSSOHandler.onSSOFlowFailed();
                break;
            case Failed:
                Log.e(TAG, "Received Failed AuthenticationResult, will not try to acquire ca/sso "
                        + "token.");
                mSSOHandler.onSSOFlowFailed();
                break;
            default:
                mSSOHandler.onSSOFlowFailed();
                break;
        }
    }

    @Override
    public void onError(final Exception e) {
        Log.e(TAG, "ADAL CASSO failed to authenticate", e);

        if (e instanceof AuthenticationException) {
            Log.i(TAG, "Received AuthenticationException. Going through loud SSO flow. Error is: "
                    + ((AuthenticationException) e).getCode().name());
            mSSOHandler.requestDeviceClaimsToken();
            return;
        }

        Log.e(TAG, "ADAL error code unknown: " + e);
        mSSOHandler.onSSOFlowFailed();
    }
}
