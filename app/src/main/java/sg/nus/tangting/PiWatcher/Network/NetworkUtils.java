package sg.nus.tangting.PiWatcher.Network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import sg.nus.tangting.PiWatcher.Movement;
import sg.nus.tangting.PiWatcher.Utils;

public class NetworkUtils {

    public static final String API_URL = "http://ipiwatcher.applinzi.com/api.php";

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static ArrayList<Movement> queryData(String uuid, String psw, long latestUpdateTime, int count){
        if(count < 0) count = 20;
        String para = String.format("action=query&uuid=%s&psw=%s&after=%s&count=%s",uuid,psw,latestUpdateTime,count);
        String realUrl = API_URL + "?" + para;

        String json = "";
        try{
            json = readResponse(realUrl);
        }catch (Exception ex){
            Logger.e(ex, "Network Error");
        }

        ArrayList<Movement> list;
        try{
            list = parseJsonDatas(json);
        }catch (JSONException ex){
            Logger.e(ex, "JSon Error");
            return new ArrayList<>();
        }

        return  list;
    }

    public static boolean clearData(String uuid, String psw){

        String para = String.format("action=clear&uuid=%s&psw=%s",uuid,psw);
        String realUrl = API_URL + "?" + para;
        String json = "";
        try{
            json = readResponse(realUrl);
        }catch (Exception ex){
            Logger.e(ex, "Network Error");
        }

        return isStatusOk(json);
    }

    public static boolean verifyAuthorization(String uuid, String psw){
        String para = String.format("action=verify&uuid=%s&psw=%s",uuid,psw);
        String realUrl = API_URL + "?" + para;

        String json;
        try{
            json = readResponse(realUrl);
        }catch (Exception ex){
            Logger.e(ex, "Network Error");
            return false;
        }

        return isStatusOk(json);
    }

    public static boolean setServerAlias(String uuid, String psw, String alias){
        String para = String.format("action=alias&uuid=%s&psw=%s&alias=%s",uuid,psw,alias);
        String realUrl = API_URL + "?" + para;

        String json;
        try{
            json = readResponse(realUrl);
        }catch (Exception ex){
            Logger.e(ex, "Network Error");
            return false;
        }

        return isStatusOk(json);
    }

    public static String readResponse(String urlPath) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inStream = conn.getInputStream();
        while ((len = inStream.read(data)) != -1) {
            outStream.write(data, 0, len);
        }
        inStream.close();
        return new String(outStream.toByteArray());//通过out.Stream.toByteArray获取到写的数据
    }


    private static boolean isStatusOk(String jsonStr){
        try{
            JSONObject jsonObj= new JSONObject(jsonStr);
            String status = jsonObj.getString("status");
            return status.equals("ok");
        }catch (JSONException ex){
            Logger.d("JSon Error");
            return false;
        }
    }


    private static ArrayList<Movement> parseJsonDatas(String jsonStr) throws JSONException {

        ArrayList<Movement> list = new ArrayList<Movement>();
        JSONObject jsonObj= new JSONObject(jsonStr);
        String status = jsonObj.getString("status");
        if(!status.equals("ok")){
            Logger.d("JSon status:"+status);
            return list;
        }

        JSONArray dataArray = jsonObj.getJSONArray("data");

        for(int i=0; i< dataArray.length();i++){
            long time = dataArray.getLong(i);
            String msg = Utils.timestampToDate(time,null);
            Movement movement = new Movement(msg,time);
            list.add(movement);
        }
        return list;
    }



}
