package emmx.microsoft.com.aadwebssodemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationSettings;
import com.microsoft.aad.adal.PromptBehavior;

public class AadActivity extends Activity{

    private final static String TAG = AadActivity.class.getSimpleName();
    /** The hashCode is not guaranteed to be positive, so we use Math.abs(). */
    private static final int GET_ACCOUNTS_REQUEST_CODE = Math.abs(Manifest.permission.GET_ACCOUNTS.hashCode());
    public static final String EXTRA_TOKEN_REQUEST = "tokenRequest";
    public static final int DEVICE_CLAIMS = 1;

    private AuthenticationContext mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthenticationSettings.INSTANCE.setUseBroker(true);
        mContext = new AuthenticationContext(getApplicationContext(), Constants.AAD_AUTHORITY_URL, true);

        if (savedInstanceState != null) {
            return;
        }

        /*
         * ADAL documentation recommends acquiring the contacts permission in order to use the broker
         * on API 23+, but recent changes mean it is not actually necessary to have this permission
         * if the token broker supports a workaround they implemented in ADAL 1.12.0.
         *
         * After speaking with token broker (Company Portal) engineering, the general consensus is
         * that we should still try to get this permission in case there is a bug in the workaround,
         * but it shouldn't be necessary. As a result, we still try to get it, but acquire a token
         * through the broker either way.
         */
        if (shouldRequestContactsPermission(AadActivity.this)) {
            requestContactsPermission(AadActivity.this);
        } else {
            int request = this.getIntent().getIntExtra(EXTRA_TOKEN_REQUEST, 0);
            switch (request) {
                case DEVICE_CLAIMS:
                    acquireDeviceClaimsToken();
                    break;
                default:
                    acquireAdalToken();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onMAMDestroy");
    }

    @Override
    public void setContentView(int layoutResID) {
    }

    @Override
    public void setContentView(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onMAMActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        mContext.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Regardless of the result, we should try to acquire an ADAL token.
        acquireAdalToken();
    }

    private void acquireAdalToken() {
        Log.d(TAG, "acquireAdalToken");

        AuthenticationCallback<com.microsoft.aad.adal.AuthenticationResult> callback = new AadAuthenticationCallback(this);

        mContext.acquireToken(AadActivity.this,
                Constants.AAD_GRAPH_RESOURCE_NAME,
                Constants.AAD_CLIENT_ID,
                mContext.getRedirectUriForBroker(),
                "",
                PromptBehavior.Auto,
                "",
                callback);
    }

    private void acquireDeviceClaimsToken() {
        DeviceClaimsTokenAuthCallback callback = new DeviceClaimsTokenAuthCallback(this);
        mContext.acquireToken(AadActivity.this,
                Constants.DEVICE_CLAIMS_RESOURCE,
                Constants.AAD_CLIENT_ID,
                mContext.getRedirectUriForBroker(),
                "",
                PromptBehavior.Auto,
                null,
                Constants.DEVICE_CLAIMS_DEVICEID_ESSENTIAL_STRING,
                callback);
    }

    private boolean shouldRequestContactsPermission(final Activity activity) {
        // We do not need this permission below API 23.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        // If we have the permission, we do not need to ask for it.
        if (activity.checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_DENIED) {
            return false;
        }
        return true;
    }

    public void requestContactsPermission(final Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.GET_ACCOUNTS},
                GET_ACCOUNTS_REQUEST_CODE);
    }

    private class AadAuthenticationCallback implements AuthenticationCallback<com.microsoft.aad.adal.AuthenticationResult> {
        private final Activity mActivity;

        AadAuthenticationCallback (Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onError(Exception exc) {
            if (exc instanceof AuthenticationException) {
                if (((AuthenticationException)exc).getCode() != ADALError.AUTH_FAILED_CANCELLED) {
                    Toast.makeText(getApplicationContext(), "AAD sign in failed", Toast.LENGTH_SHORT).show();
                }

                Log.d(TAG, "Cancelled");
            } else {
                Toast.makeText(getApplicationContext(), "AAD sign in failed", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Authentication error:" + exc.getMessage());
            }
            setResult(RESULT_CANCELED);
            finish();
        }

        @Override
        public void onSuccess(com.microsoft.aad.adal.AuthenticationResult result) {
            if (result == null || result.getAccessToken() == null
                    || result.getAccessToken().isEmpty()) {

                Toast.makeText(getApplicationContext(), "AAD sign in failed", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Token is empty");
                setResult(RESULT_CANCELED);
                finish();
            } else {
                SignInManager.getInstance().signIn(result);
                // request is successful
                Log.d(TAG, "Status:" + result.getStatus() + " Expired:"
                        + result.getExpiresOn().toString());
                finish();
            }
        }
    }
}
