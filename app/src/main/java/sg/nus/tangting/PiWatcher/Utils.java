package sg.nus.tangting.PiWatcher;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Utils {

    public static void showToast(final Context context, int stringId) {
        final String msg = context.getString(stringId);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(final Context context, final String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        /*new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    public static boolean isJson(String s){
        if (!Utils.isEmpty(s)) {
            try {
                JSONObject extraJson = new JSONObject(s);
                if (extraJson.length() > 0) {
                    return true;
                }
            } catch (JSONException e) {
            }
        }
        return false;
    }

    public static String timestampToDate(long timeSecond, String template){
        Long timestamp = timeSecond * 1000;
        if(template == null){
            template = "yyyy/M/dd HH:mm:ss";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(template, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static HashMap<String,String> getAuthorization(String json){
        try{
            JSONObject jsonObj= new JSONObject(json);
            String uuid = jsonObj.getString(Constant.JSON_KEY_UUID);
            String psw = jsonObj.getString(Constant.JSON_KEY_PSW);
            HashMap<String,String> map = new HashMap<>();
            map.put(Constant.JSON_KEY_UUID, uuid);
            map.put(Constant.JSON_KEY_PSW, md5(uuid+psw));
            return map;
        }catch (JSONException ex){
            return null;
        }
    }

    public static String md5(String str) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(str.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception ex) {
            return "";
        }
    }


}
