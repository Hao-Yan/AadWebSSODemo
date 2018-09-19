package emmx.microsoft.com.aadwebssodemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mAadSignInButton;
    private Button mAadSignOutButton;
    private Button mDCTButton;
    private Button mUrlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAadSignInButton = (Button)findViewById(R.id.buttonAadSignIn);
        mAadSignInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, AadActivity.class);
                startActivity(intent);
            }
        });

        mAadSignOutButton = (Button)findViewById(R.id.buttonAadSignOut);
        mAadSignOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SignInManager.getInstance().signOut();
            }
        });

        mDCTButton = (Button)findViewById(R.id.buttonDCT);
        mDCTButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SSOHandler.getInstance().start(new SSOHandler.SSOCallback() {
                    @Override
                    public boolean onSuccess(String accessToken) {
                        if (!TextUtils.isEmpty(accessToken)) {
                            SignInManager.getInstance().saveDCTToken(accessToken);
                        }
                        return true;
                    }

                    @Override
                    public boolean onFailure() {
                        return true;
                    }

                    @Override
                    public boolean onLoudPrompt() {
                        return true;
                    }

                    @Override
                    public boolean afterLoudPrompt() {
                        return true;
                    }
                });
            }
        });


        mUrlButton = (Button)findViewById(R.id.buttonUrl);
        mUrlButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, WebViewActivity.class);
                startActivity(intent);
            }
        });

        updateButtons();
    }

    @Override
    public void onResume() {
        updateButtons();
        super.onResume();
    }

    private void updateButtons() {
        if (SignInManager.getInstance().hasUserSignedIn()) {
            mAadSignInButton.setVisibility(View.GONE);
            mAadSignOutButton.setVisibility(View.VISIBLE);
        } else {
            mAadSignInButton.setVisibility(View.VISIBLE);
            mAadSignOutButton.setVisibility(View.GONE);
        }
    }
}
