package emmx.microsoft.com.aadwebssodemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

public class WebViewActivity extends Activity {

    private static final String sCaSSOHeaderName = "x-ms-ManagedBrowserCredential";

    private WebView mWebView;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // Add Additional headers
            if (AADUtils.isAADEndpoint(request.getUrl().toString()) && !TextUtils.isEmpty(SignInManager.getInstance().getDeviceClaimToken())) {

                // Is recursive request? (check for our custom headers)
                if (request.getRequestHeaders() != null && request.getRequestHeaders().containsKey(sCaSSOHeaderName))
                    return false;

                HashMap<String, String> headers = new HashMap<String, String>();
                if (request.getRequestHeaders() != null) {
                    headers.putAll(request.getRequestHeaders());
                }
                headers.put(sCaSSOHeaderName, SignInManager.getInstance().getDeviceClaimToken());

                view.loadUrl(request.getUrl().toString(), headers);
                return true;
            }

            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = (WebView) findViewById(R.id.webView1);

        mWebView.setWebViewClient(new MyWebViewClient());

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(" https://outlook.office.com");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
