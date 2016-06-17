package sg.nus.tangting.PiWatcher.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import sg.nus.tangting.PiWatcher.Constant;
import sg.nus.tangting.PiWatcher.Movement;
import sg.nus.tangting.PiWatcher.Network.NetworkUtils;
import sg.nus.tangting.PiWatcher.R;
import sg.nus.tangting.PiWatcher.Utils;


public class MainActivity extends AppCompatActivity{

    public static final String KEY_JPUSH_MESSAGE = "message";
    public static final String KEY_JPUSH_EXTRA = "extra";

    public static boolean isForeground = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerAdapter mAdapter;

    private String mUUID;
    private String mPSW;
    private String mJPushAlias;

    private boolean isSettingJPushAlias = false;
    private boolean isSettingServerAlias = false;
    private boolean isPrefsJPushInit = false;
    private boolean isPrefsServerInit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.t(0).i("onCreate");
        setContentView(R.layout.activity_main);

        initIdentity();
        initPreference();
        initView();
        initJPushAndServer();

        registerMainReceiver();
    }

    private void initIdentity(){
        Intent intent = getIntent();
        mUUID = intent.getStringExtra(Constant.JSON_KEY_UUID);
        if(mUUID != null){
            mPSW = intent.getStringExtra(Constant.JSON_KEY_PSW);

        }else{
            SharedPreferences preferences = getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE);
            mUUID = preferences.getString(Constant.PREFS_KEY_UUID, "");
            mPSW = preferences.getString(Constant.PREFS_KEY_PSW, "");
        }
        mJPushAlias = mPSW.replace("-","");
    }

    private void initPreference(){
        SharedPreferences settings = getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE);
        isPrefsJPushInit = settings.getBoolean(Constant.PREFS_KEY_JPUSH_INIT, false);
        isPrefsServerInit = settings.getBoolean(Constant.PREFS_KEY_SERVER_INIT, false);
    }

    private void initView(){
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.main_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new RecyclerAdapter(this, new ArrayList<Movement>());
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.main_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_green_light);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFromUiThread();
            }
        });
    }

    private void logout(){
        getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE).edit().clear().apply();

        JPushInterface.stopPush(this);

        unregisterReceiver(mMainReceiver);
        mMainReceiver = null;

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    private static final int UPDATE_COMPLETE = 0x01;
    private static final String UPDATE_KEY_MOVEMENT = "Movement";
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_COMPLETE:
                    ArrayList<Movement> result = msg.getData().getParcelableArrayList(UPDATE_KEY_MOVEMENT);
                    mAdapter.addItems(result);
                    mAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    };

    private void updateFromUiThread(){
        if(NetworkUtils.isOnline(this)){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            new Thread(updateMovementRunnable).start();
        }else{
            mSwipeRefreshLayout.setRefreshing(false);
            Utils.showToast(this, R.string.network_not_connected);
        }
    }

    private Runnable updateMovementRunnable = new Runnable() {
        @Override
        public void run() {
            long latestUpdateTime = mAdapter.getLatestUpdateTime();
            List<Movement> result = NetworkUtils.queryData(mUUID, mPSW, latestUpdateTime, 20);
            Message msg = new Message();
            msg.what = UPDATE_COMPLETE;
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(UPDATE_KEY_MOVEMENT , (ArrayList<Movement>)result);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    };

    private void initJPushAndServer(){
        JPushInterface.init(this);
        if(JPushInterface.isPushStopped(this)){
            JPushInterface.resumePush(this);
        }
        if(NetworkUtils.isOnline(this)){
            setJPushAliasIfNeed();
            setServerAliasIfNeed();
        }
    }

    private void setJPushAliasIfNeed(){
        if( (!isSettingJPushAlias) && (!isPrefsJPushInit) ){
            isSettingJPushAlias = true;
            JPushInterface.setAlias(this,mJPushAlias, mJPushAliasCallback);
        }
    }

    private void setServerAliasIfNeed(){
        if( (!isSettingServerAlias) && (!isPrefsServerInit) ){
            isSettingServerAlias = true;
            new Thread((new Runnable() {
                @Override
                public void run() {
                    boolean result = NetworkUtils.setServerAlias(mUUID, mPSW, mJPushAlias);
                    if(result){
                        isPrefsServerInit = true;
                        saveServerInitStatus(true);
                        isSettingServerAlias = false;
                    }
                }
            })).start();
        }
    }

    private void saveJPushInitStatus(boolean status){
        SharedPreferences.Editor editor = getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE).edit();
        editor.putBoolean(Constant.PREFS_KEY_JPUSH_INIT, status);
        editor.apply();
    }

    private void saveServerInitStatus(boolean status){
        SharedPreferences.Editor editor = getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE).edit();
        editor.putBoolean(Constant.PREFS_KEY_SERVER_INIT, status);
        editor.apply();
    }

    private final TagAliasCallback mJPushAliasCallback = new TagAliasCallback(){
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            isSettingJPushAlias = false;
            switch (code){
                case 0:
                    Logger.d("JPush Init Alias Success");
                    saveJPushInitStatus(true);
                    break;
                default:
                    Logger.d("JPush Init Alias Fail");
                    Utils.showToast(MainActivity.this, R.string.push_init_fail);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_logout:
                logout();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        updateFromUiThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMainReceiver !=null) unregisterReceiver(mMainReceiver);
    }

    private MainReceiver mMainReceiver;
    public static final String JPUSH_MSG_RECEIVED_ACTION = "sg.nus.tangting.PiWatcher.MESSAGE_RECEIVED_ACTION";

    public void registerMainReceiver() {
        mMainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(JPUSH_MSG_RECEIVED_ACTION);
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mMainReceiver, filter);
    }

    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){
                case android.net.ConnectivityManager.CONNECTIVITY_ACTION:
                    if(NetworkUtils.isOnline(MainActivity.this)){
                        setJPushAliasIfNeed();
                        setServerAliasIfNeed();
                    }
                    break;
                case JPUSH_MSG_RECEIVED_ACTION:
                    updateFromUiThread();
                    break;
                default:
            }
        }
    }

}
