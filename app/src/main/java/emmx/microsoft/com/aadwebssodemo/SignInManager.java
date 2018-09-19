package emmx.microsoft.com.aadwebssodemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;

public class SignInManager {
    public final static String PREF_SIGNIN_MANAGER_USER_ID_KEY = "signin_manager_user_id";
    public final static String PREF_SIGNIN_MANAGER_EMAIL_ADDRESS_KEY = "signin_manager_email_address";
    public final static String PREF_SIGNIN_MANAGER_TENANT_ID_KEY = "signin_manager_tenant_id";
    public final static String PREF_SIGNIN_MANAGER_REFRESH_TOKEN_KEY = "signin_manager_refresh_token";

    private String mUserId;
    private String mEmailAddress;
    private String mTenantId;
    private String mRefreshToken;
    private String mDCTToken;

    private static class ThreadSafeLazyHolder {
        public static final SignInManager INSTANCE = new SignInManager();
    }

    public static SignInManager getInstance() {
        return ThreadSafeLazyHolder.INSTANCE;
    }

    private SignInManager() {
        initAuthInfoFromCache();
    }

    public void signIn(AuthenticationResult result) {
        mRefreshToken = result.getRefreshToken();
        mUserId = result.getUserInfo().getUserId();
        mEmailAddress = result.getUserInfo().getDisplayableId();
        mTenantId = result.getTenantId();

        persistAuthInfo();

        Toast.makeText(ContextUtils.getApplicationContext(), "Sign in succeeded", Toast.LENGTH_SHORT).show();
    }

    public void signOut() {
        new AuthenticationContext(ContextUtils.getApplicationContext(), Constants.AAD_AUTHORITY_URL, false).getCache().removeAll();

        CookieManager.getInstance().removeSessionCookie();
        CookieManager.getInstance().removeAllCookie();
        if (Build.VERSION.SDK_INT < 21) {
            CookieSyncManager.createInstance(ContextUtils.getApplicationContext());
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }

        mRefreshToken = "";
        mUserId = "";
        mEmailAddress = "";
        mTenantId = "";

        persistAuthInfo();

        Toast.makeText(ContextUtils.getApplicationContext(), "Sign Out succeeded", Toast.LENGTH_SHORT).show();
    }

    public String getUserId() {
        return mUserId;
    }

    public String getEmailAddress() {
        return mEmailAddress;
    }

    public void saveDCTToken(String dctToken) {
        mDCTToken = dctToken;

        Toast.makeText(ContextUtils.getApplicationContext(), "Get Device Claim token succeeded", Toast.LENGTH_SHORT).show();
    }

    public String getDeviceClaimToken() {
        return mDCTToken;
    }

    public boolean hasUserSignedIn() {
        return  /*!TextUtils.isEmpty(mRefreshToken) &&*/
                !TextUtils.isEmpty(mUserId) &&
                !TextUtils.isEmpty(mEmailAddress) &&
                !TextUtils.isEmpty(mTenantId);
    }

    private void persistAuthInfo() {
        SharedPreferences sharedPrefs = ContextUtils.getAppSharedPreferences();
        sharedPrefs.edit()
                .putString(PREF_SIGNIN_MANAGER_USER_ID_KEY, mUserId)
                .putString(PREF_SIGNIN_MANAGER_EMAIL_ADDRESS_KEY, mEmailAddress)
                .putString(PREF_SIGNIN_MANAGER_TENANT_ID_KEY, mTenantId)
                .putString(PREF_SIGNIN_MANAGER_REFRESH_TOKEN_KEY, mRefreshToken)
                .apply();
    }

    private void initAuthInfoFromCache() {
        SharedPreferences sharedPrefs = ContextUtils.getAppSharedPreferences();
        mUserId = sharedPrefs.getString(PREF_SIGNIN_MANAGER_USER_ID_KEY, "");
        mEmailAddress = sharedPrefs.getString(PREF_SIGNIN_MANAGER_EMAIL_ADDRESS_KEY, "");
        mTenantId = sharedPrefs.getString(PREF_SIGNIN_MANAGER_TENANT_ID_KEY, mTenantId);
        mRefreshToken = sharedPrefs.getString(PREF_SIGNIN_MANAGER_REFRESH_TOKEN_KEY, mRefreshToken);
    }
}
