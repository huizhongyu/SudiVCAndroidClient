package cn.closeli.rtc.net.interceptor;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import cn.closeli.rtc.app.BaseResult;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ASUS on 2018/1/5.
 */

public class ResponseInterceptor implements Interceptor {
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");

	@Override
	public Response intercept(Chain chain) throws IOException {
		Response response = chain.proceed(chain.request());
		if (response.code() == 500) {
			//throw ApiExceptionUtil.onError(500, "");
			try {
				final String msg = response.body().string();
				BaseResult baseModel = new BaseResult();
				baseModel.setId(500);
				baseModel.setResult("网络异常，请稍后重试");
				baseModel.setResult(null);
				byte[] bodyBytes = JSON.toJSONBytes(baseModel);
				String str = new String(bodyBytes);
				return response.newBuilder().code(500).body(ResponseBody.create(MEDIA_TYPE_JSON,
						bodyBytes)).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}
}
