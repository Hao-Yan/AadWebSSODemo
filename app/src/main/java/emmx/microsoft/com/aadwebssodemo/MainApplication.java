package emmx.microsoft.com.aadwebssodemo;

import android.app.Application;

/**
 * Created by hayan on 18-9-19.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtils.initApplicationContext(this);
    }
}
