package emmx.microsoft.com.aadwebssodemo;

import com.microsoft.aad.adal.AuthenticationResult;

public class SignInManager {
    private String mUserId;
    private String mEmailAddress;
    private String mTenantId;
    private String mAccessToken;

    private static class ThreadSafeLazyHolder {
        public static final SignInManager INSTANCE = new SignInManager();
    }

    public static SignInManager getInstance() {
        return ThreadSafeLazyHolder.INSTANCE;
    }

    private SignInManager() {
    }

    public void signIn(AuthenticationResult result) {
        mAccessToken = result.getAccessToken();
        mUserId = result.getUserInfo().getUserId();
        mEmailAddress = result.getUserInfo().getDisplayableId();
        mTenantId = result.getTenantId();
    }

    public void signOut() {
        mAccessToken = null;
        mUserId = null;
        mEmailAddress = null;
        mTenantId = null;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getEmailAddress() {
        return mEmailAddress;
    }
}
