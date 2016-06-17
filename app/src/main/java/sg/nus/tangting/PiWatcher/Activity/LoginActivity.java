package sg.nus.tangting.PiWatcher.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.orhanobut.logger.Logger;

import java.util.HashMap;

import sg.nus.tangting.PiWatcher.Constant;
import sg.nus.tangting.PiWatcher.Network.NetworkUtils;
import sg.nus.tangting.PiWatcher.R;
import sg.nus.tangting.PiWatcher.Utils;

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_SCANNER = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String uuid = getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE).
                getString(Constant.PREFS_KEY_UUID,null);
        if(uuid==null){
            setContentView(R.layout.activity_login);
        }else{
            finishToMainActivity();
        }
    }

    public void scanBarcode(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            new IntentIntegrator(this).setPrompt("").setOrientationLocked(true).setCaptureActivity(ScannerActivity.class).initiateScan();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_SCANNER);
        }
    }

    private void saveLoginPreferences(String uuid, String psw){
        SharedPreferences.Editor editor = getSharedPreferences(Constant.PREFS_FILE, MODE_PRIVATE).edit();
        editor.putString(Constant.PREFS_KEY_UUID, uuid);
        editor.putString(Constant.PREFS_KEY_PSW, psw);
        editor.apply();
    }

    private void finishToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    private void finishToMainActivity(String uuid, String psw){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constant.JSON_KEY_UUID, uuid);
        intent.putExtra(Constant.JSON_KEY_PSW, psw);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Logger.d( "Cancelled scan");
            } else {
                processScanResult(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processScanResult(String str){
        final HashMap<String,String> identity = Utils.getAuthorization(str);

        if(identity == null){
            Utils.showToast(this, "Invalid identity QR Code!");
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean verify = NetworkUtils.verifyAuthorization(identity.get(Constant.JSON_KEY_UUID), identity.get(Constant.JSON_KEY_PSW));

                    if(!verify){
                        Utils.showToast(LoginActivity.this, "Authorization failed!");
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String uuid = identity.get(Constant.JSON_KEY_UUID);
                            String psw = identity.get(Constant.JSON_KEY_PSW);
                            saveLoginPreferences(uuid,psw);
                            finishToMainActivity(uuid,psw);
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_SCANNER:
                for (int i = 0; i < permissions.length; i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        if(!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])){
                            new AlertDialog.Builder(this)
                                    .setMessage("You need to allow permission to access Camera!")
                                    .setPositiveButton("OK", null).create().show();
                            return;
                        }
                    }else{
                        new IntentIntegrator(this).setPrompt("").setOrientationLocked(true)
                                .setCaptureActivity(ScannerActivity.class).initiateScan();
                    }
                }
                break;
            default:
        }
    }

}
