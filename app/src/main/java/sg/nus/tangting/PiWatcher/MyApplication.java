package sg.nus.tangting.PiWatcher;

import android.app.Application;
import android.os.Debug;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import cn.jpush.android.api.JPushInterface;


public class MyApplication extends Application{

    private static final String DEBUG_TAG = "PiTAG";

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG){
            Logger.init(DEBUG_TAG).hideThreadInfo().methodCount(1).logLevel(LogLevel.FULL);
        }else {
            Logger.init(DEBUG_TAG).logLevel(LogLevel.NONE);
        }

        JPushInterface.setDebugMode(false);
    }
}
