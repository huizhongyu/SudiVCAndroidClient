package cn.closeli.rtc.net.interceptor;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;


import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * Created by yangfeng01 on 2017/11/14.
 */
public class CommonParamsInterceptor implements Interceptor {

	/**
	 * 传json字符串的media type
	 */
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");

	@Override
	public Response intercept(Chain chain) throws IOException {

		Request request = chain.request();
		Request.Builder requestBuilder = request.newBuilder();

		if (request.method().equals("POST")) {
			if (request.body() != null && request.body() instanceof MultipartBody) {
				requestBuilder.post(request.body());
			} else {
				String json = bodyToString(request.body());
				JSONObject jsonObject = JSONObject.parseObject(json);
				jsonObject.put("netType", "a");
				jsonObject.put("token", "");
				jsonObject.put("sign", "");

				RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, jsonObject.toJSONString());
				requestBuilder.post(requestBody);
			}
		}
		request = requestBuilder.build();
		return chain.proceed(request);

	}

	/**
	 * 获取post body转化为string
	 */
	private static String bodyToString(final RequestBody request) {
		try {
			final Buffer buffer = new Buffer();
			buffer.clear();
			if (request != null) {
				request.writeTo(buffer);
			} else {
				return "";
			}
			return buffer.readUtf8();
		} catch (IOException e) {
			return "did not work";
		}
	}

	public static class Builder {

		private CommonParamsInterceptor interceptor;

		public Builder() {
			interceptor = new CommonParamsInterceptor();
		}


	}

}
