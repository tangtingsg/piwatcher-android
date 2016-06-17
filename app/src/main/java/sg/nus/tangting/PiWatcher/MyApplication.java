package sg.nus.tangting.PiWatcher;

import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import cn.jpush.android.api.JPushInterface;


public class MyApplication extends Application{

    private static final String DEBUG_TAG = "PiTAG";

    public MyApplication() {
        Logger.init(DEBUG_TAG).hideThreadInfo().methodCount(1).logLevel(LogLevel.FULL);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init(DEBUG_TAG).hideThreadInfo().methodCount(1).logLevel(LogLevel.FULL);
        JPushInterface.setDebugMode(false);
    }
}
