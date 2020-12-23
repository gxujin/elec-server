package com.cc.tcp.util;

import okhttp3.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    public static final MediaType form_type = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");
    public static final MediaType multipart_type = MediaType.parse("multipart/form-data;charset=utf-8");
    public static final MediaType json_type = MediaType.parse("application/json;charset=utf-8");
    public static final MediaType text_type = MediaType.parse("text/plain;charset=utf-8");

    private static long MAX_CONNECT_TIME_OUT = 10 * 1000;//判断连接超时的最大时间

    public static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(3 * 1000, TimeUnit.MILLISECONDS)//连接时间
            .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)//读取时间
            .build();

    //Get方法调用服务
    public static String httpGet(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response.body().string();// 返回的是string 类型
    }

    /**
     * 发送http json
     * @param url
     * @param content
     * @return
     * @throws Exception
     */
    public static String httpPostJson(String url, String content) throws Exception {
        HttpUrl httpUrl = HttpUrl.get(new URL(url));
        return httpPost(httpUrl, content);
    }

    /**
     * 发送json
     * @param url
     * @param content
     * @return
     * @throws Exception
     */
    public static String httpPost(HttpUrl url, String content) throws Exception {
        RequestBody requestBody = RequestBody.create(json_type, content);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return execute(request);
    }

    /**
     * 发送contentType为text.plain请求
     * @param url
     * @param content
     * @return
     * @throws Exception
     */
    public static String httpPostText(String url, String content) throws Exception {
        HttpUrl httpUrl = HttpUrl.get(new URL(url));
        return httpPost(httpUrl, content, text_type);
    }

    /**
     * 发送contentType为text.plain请求
     * @param url
     * @param content
     * @return
     * @throws Exception
     */
    public static String httpPostText(HttpUrl url, String content) throws Exception {
        return httpPost(url, content, text_type);
    }

    public static String httpPost(HttpUrl url, String content, MediaType mediaType) throws Exception {
        if (mediaType == null) {
            return httpPost(url, content);//默认json
        }
        RequestBody requestBody = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = httpClient.newCall(request).execute();
        return response.body().string();
    }

    /**
     * 提交form-data表单数据
     * @param url
     * @param data
     * @return
     * @throws IOException
     */
    public static String httpPostFormData(HttpUrl url, Map<String,String> data) throws Exception{
        List<String> keyList = new ArrayList<String>(data.keySet());
        FormBody.Builder builder = new FormBody.Builder();
        for(String key : keyList){
            builder.add(key, data.get(key));
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(request);
    }

    /**
     * 自定义发送请求，用于判断是否连接超时
     * @param request
     * @return
     * @throws Exception
     */
    private static String execute(Request request) throws Exception{
        Response response = null;
        long s = System.currentTimeMillis();
        try{
            response = httpClient.newCall(request).execute();
        }catch (Exception ex){
            ex.printStackTrace();
            if(ex instanceof ConnectException){//连接超时
                throw new Exception("服务连接失败");
            }
            long e = System.currentTimeMillis();
            if((e - s) < MAX_CONNECT_TIME_OUT){
                throw new Exception("服务异常");
            }
            throw new IOException(ex);//读取超时，实际报SocketTimeoutException
        }
        try {
            return response.body().string();
        }catch (Exception e){
            throw  new Exception(e.getMessage());
        }
    }

}
