package com.example.coolweather.utils;


import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

/**
 * @author Admin
 * @version $Rev$
 * @des 从服务器获取天气数据
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class HttpUtils {
    public static void sendOkHttpRequest(String address, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
