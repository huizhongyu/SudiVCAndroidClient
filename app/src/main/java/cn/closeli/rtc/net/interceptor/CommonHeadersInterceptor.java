package cn.closeli.rtc.net.interceptor;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yangfeng01 on 2017/11/14.
 * <p>
 * 公共请求头
 */
public class CommonHeadersInterceptor implements Interceptor {

    private Map<String, String> headerMap = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request;
        request = chain.request()
                .newBuilder()
                .addHeader("netType", "ard")
                .build();

        Log.i("okhttp", "intercept: " + request.toString());

        return chain.proceed(request);
    }

    public static class Builder {

        private CommonHeadersInterceptor interceptor;

        public Builder() {
            interceptor = new CommonHeadersInterceptor();
        }

        public Builder addHeader(String key, String value) {
            interceptor.headerMap.put(key, value);
            return this;
        }
    }
}
