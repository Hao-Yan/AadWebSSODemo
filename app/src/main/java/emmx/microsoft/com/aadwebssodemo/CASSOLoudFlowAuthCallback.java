package emmx.microsoft.com.aadwebssodemo;

import android.util.Log;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationCancelError;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;

public class CASSOLoudFlowAuthCallback implements AuthenticationCallback<AuthenticationResult> {
    private static final String TAG = CASSOLoudFlowAuthCallback.class.getSimpleName();
    private SSOHandler mSSOHandler;

    /**
     * ctor.
     *
     * @param SSOHandler   the SSOHandler that made this call.
     */
    public CASSOLoudFlowAuthCallback(final SSOHandler SSOHandler) {
        mSSOHandler = SSOHandler;
    }

    @Override
    public void onSuccess(final AuthenticationResult result) {
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
                Log.d(TAG, "User cancelled ADAL authentication, will not try to acquire ca/sso "
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
        mSSOHandler.onSSOFlowFailed();

        // AuthenticationCancelError is a subclass of AuthenticationException so we must check for
        // it first.
        if (e instanceof AuthenticationCancelError) {
            Log.e(TAG, "ADALError on AuthenticationCancellError is "
                    + ((AuthenticationCancelError) e).getCode().name());
            return;
        }

        if (e instanceof AuthenticationException) {
            Log.e(TAG, "CASSO ADALError on AuthenticationException is "
                    + ((AuthenticationException) e).getCode().name());
            return;
        }

        Log.e(TAG, "ADALError code unknown.");
    }
}
