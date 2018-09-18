package emmx.microsoft.com.aadwebssodemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mAadSignInButton;
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

        mDCTButton = (Button)findViewById(R.id.buttonUrl);
        mDCTButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SSOHandler.getInstance(getApplicationContext()).start(new SSOHandler.SSOCallback() {
                    @Override
                    public boolean onSuccess(String accessToken) {
                        return false;
                    }

                    @Override
                    public boolean onFailure() {
                        return false;
                    }

                    @Override
                    public boolean onLoudPrompt() {
                        return false;
                    }

                    @Override
                    public boolean afterLoudPrompt() {
                        return false;
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
    }
}
