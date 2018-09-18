package emmx.microsoft.com.aadwebssodemo;

import android.app.Activity;
import android.util.Log;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationCancelError;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.UserInfo;

public class DeviceClaimsTokenAuthCallback implements AuthenticationCallback<AuthenticationResult> {
    private static final String TAG = DeviceClaimsTokenAuthCallback.class.getSimpleName();
    private final Activity mActivity;
    private final SSOHandler mSSOHandler;

    /**
     * Caches all the information needed to make an ADAL acquireToken call in order to acquire
     * an authentication token with the Device claim.
     *
     * @param activity The activity used to make the user-prompting device claims request.
     */
    public DeviceClaimsTokenAuthCallback(Activity activity) {
        mActivity = activity;
        mSSOHandler = SSOHandler.getInstance(activity.getApplicationContext());
    }

    @Override
    public void onSuccess(final AuthenticationResult result) {
        try {
            Log.i(TAG, "DeviceClaims callback was hit onSuccess");
            if (null == result || null == result.getStatus()) {
                Log.e(TAG, "ADAL succeeded, but the result is null for unknown reasons.");
                mSSOHandler.onSSOFlowFailed();
                return;
            }
            switch (result.getStatus()) {
                case Succeeded:
                    if (result.getAccessToken() == null || result.getAccessToken().isEmpty()) {
                        Log.w(TAG, "ADAL reported success but did not return an access token.");
                        mSSOHandler.onSSOFlowFailed();
                        return;
                    }

                    UserInfo info = result.getUserInfo();
                    String username = SignInManager.getInstance().getEmailAddress();
                    if (null == info || (username != null && !username.equalsIgnoreCase(info.getDisplayableId()))) {
                        Log.e(TAG, "ADAL reported success but the work account is for a different user.");
                        // If we are MAM enrolled, and the MAM enrolled identity does not match
                        // the identity that we acquired a token for, then we should discard the result.
                        mSSOHandler.onSSOFlowFailed();
                        return;
                    }
                    mSSOHandler.requestSSOTokenAfterLoudFlow();
                    break;
                case Cancelled:
                    Log.w(TAG, "User cancelled ADAL authentication, will not try to mam enroll.");
                    mSSOHandler.onSSOFlowFailed();
                    break;
                case Failed:
                    Log.e(TAG, "Received Failed AuthenticationResult, will not try to mam enroll.");
                    mSSOHandler.onSSOFlowFailed();
                    break;
                default:
                    Log.w(TAG, "Unsure how to handle auth result, treating SSO flow as failed.");
                    mSSOHandler.onSSOFlowFailed();
                    break;
            }
        } finally {
            mActivity.finish();
        }
    }

    @Override
    public void onError(final Exception e) {
        try {
            Log.e(TAG, "ADAL failed: " + e);
            mSSOHandler.onSSOFlowFailed();

            // AuthenticationCancelError is a subclass of AuthenticationException so we must check for
            // it first.
            if (e instanceof AuthenticationCancelError) {
                Log.e(TAG, "ADALError on AuthenticationCancelError is " + ((AuthenticationCancelError) e).getCode().name());
                return;
            }

            if (e instanceof AuthenticationException) {
                Log.e(TAG, "ADALError on AuthenticationException is " + ((AuthenticationException) e).getCode().name());
                return;
            }

            Log.e(TAG, "ADALError code unknown.");
        } finally {
            mActivity.finish();
        }
    }
}
