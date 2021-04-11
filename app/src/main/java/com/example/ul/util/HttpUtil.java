package com.example.ul.util;


import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author luoweili
 */
public class HttpUtil {

    public static final String BASE_URL = "http://192.168.1.101:8080/ul/api/";//寝室WiFi
    private static Map<String, List<Cookie>> cookieStore = new HashMap<>();
    //创建线程池
    private static ExecutorService threadPool = Executors.newFixedThreadPool(30);
    //创建默认的OkHttpClient对象
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .cookieJar(new CookieJar(){
                @Override
                public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list){
                    cookieStore.put(httpUrl.host(),list);
                }
                @Override
                public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl){
                    List<Cookie> cookies = cookieStore.get(httpUrl.host());
                    return cookies==null?new ArrayList<>():cookies;
                }
            })
            .build();


    public static String newUrl(String url , HashMap<String, String> hashMap){
        /**
         * @Author:Wallace
         * @Description:发送GET和DELETE请求时，url拼接参数形成新的url
         * @Date:Created in 22:27 2021/4/1
         * @Modified By:
          * @param url  请求地址
         * @param hashMap   请求原本要携带的参数
         * @return: java.lang.String    新的url
         */
        // 拼接请求参数
        StringBuffer buffer = new StringBuffer(url);
        buffer.append('?');
        for (HashMap.Entry<String, String> entry : hashMap.entrySet()) {
            buffer.append(entry.getKey());
            buffer.append('=');
            buffer.append(entry.getValue());
            buffer.append('&');
        }
        buffer.deleteCharAt(buffer.length() - 1);
        url = buffer.toString();
        return url;
    }


    // GET方法
    public static void getRequest(String Authorization,String url, MyCallback callback, int code)  {
        FutureTask<String> task = new FutureTask<>(()->{
            //创建请求对象
            Request request;
            Request.Builder builder = new Request.Builder();
            builder.method("GET", null);
            if(Authorization!=null&&Authorization.length()>0){
                request = builder.addHeader("Authorization",Authorization).url(url).build();
            }else {
                request = builder.url(url).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failed(e,code);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.success(response,code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // POST 方法
    public static void postRequest(String Authorization, String url, HashMap<String, String> params, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            MediaType MultiPart_Form_Data = MediaType.parse("multipart/form-data; charset=utf-8");
            MultipartBody.Builder multiBuilder = new MultipartBody.Builder();
            multiBuilder.setType(MultiPart_Form_Data);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multiBuilder.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                        RequestBody.create(null, params.get(entry.getKey())));
            }
            RequestBody multiBody = multiBuilder.build();
            Request request;
            Request.Builder rBuilder = new Request.Builder();
            if (Authorization != null && Authorization.length() > 0) {
                request = rBuilder.addHeader("Authorization", Authorization).url(url).post(multiBody).build();
            } else {
                request = rBuilder.url(url).post(multiBody).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failed(e,code);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.success(response,code);
                }
            });
            return null;
        });
//        FormBody.Builder formBody = new FormBody.Builder();
//        if(!param.isEmpty()) {
//            for (Map.Entry<String,String> entry: param.entrySet()) {
//                formBody.add(entry.getKey(),entry.getValue());
//            }
//        }
//        RequestBody form = formBody.build();
//        Request.Builder builder = new Request.Builder();
//        Request request = builder.post(form)
//                .url(url)
//                .build();
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                callback.failed(e);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                callback.success(response);
//            }
//        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // DELETE方法
    public static void deleteRequest(String Authorization,String url, MyCallback callback, int code)  {
        FutureTask<String> task = new FutureTask<>(()->{
            //创建请求对象
            Request request;
            Request.Builder builder = new Request.Builder();
            builder.method("DELETE", null);
            if(Authorization!=null&&Authorization.length()>0){
                request = builder.addHeader("Authorization",Authorization).url(url).build();
            }else {
                request = builder.url(url).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failed(e,code);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.success(response,code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // PUT 方法
    public static void putRequest(String Authorization, String url, HashMap<String, String> params, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            MediaType MultiPart_Form_Data = MediaType.parse("multipart/form-data; charset=utf-8");
            MultipartBody.Builder multiBuilder = new MultipartBody.Builder();
            multiBuilder.setType(MultiPart_Form_Data);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multiBuilder.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                        RequestBody.create(null, params.get(entry.getKey())));
            }
            RequestBody multiBody = multiBuilder.build();
            Request request;
            Request.Builder rBuilder = new Request.Builder();
            if (Authorization != null && Authorization.length() > 0) {
                request = rBuilder.addHeader("Authorization", Authorization).url(url).put(multiBody).build();
            } else {
                request = rBuilder.url(url).put(multiBody).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failed(e,code);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.success(response,code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface MyCallback {
        void success(Response response,int code) throws IOException;
        void failed(IOException e,int code);
    }

    /**判断请求是否被服务器拦截*/
    public static boolean requestIsIntercepted(JSONObject jsonObject){
        try {
            String tip = jsonObject.getString("tip");
            String r = "请求被拦截！";
            if(r.equals(tip)){
                return true;
            }
        } catch (JSONException e) {
            return false;
        }
        return false;
    }
}
