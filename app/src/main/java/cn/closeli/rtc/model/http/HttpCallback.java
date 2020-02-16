package cn.closeli.rtc.model.http;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;

import cn.closeli.rtc.App;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.UIUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HttpCallback<T> implements Callback {
    private Handler handler = new Handler(Looper.myLooper());
    private SudiHttpCallback<T> sudiHttpCallback;
    Class<T> tClass;

    public HttpCallback(SudiHttpCallback<T> sudiHttpCallback, Class<T> tClazz) {
        this.sudiHttpCallback = sudiHttpCallback;
        this.tClass = tClazz;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        handler.post(() -> {
            sudiHttpCallback.onFailed(e);
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
            App.post(() -> {
                UIUtils.toastMessage("登录失败 "+response.code());
            });
            L.d("login fail, "+response.code());
            return;
        }
        String jsonResp = response.body().string();
        if (TextUtils.isEmpty(jsonResp)) {
            onFailure(call, new SudiException(-1, "SERVER ERROR"));
            return;
        }
        SudiResponse sudiResponse = GsonProvider.provide().fromJson(jsonResp, SudiResponse.class);
        if (sudiResponse.isSuccess()) {
            T t = GsonProvider.provide().fromJson(GsonProvider.provide().toJson(sudiResponse.getResult()), tClass);
            if (null != t) {
                App.post(() -> sudiHttpCallback.onSuccess(t));
            } else {
                App.post(() -> onFailure(call, new SudiException(-1, "getToken response<T> null")));
            }
        } else {
            SudiException e = new SudiException(sudiResponse.getErrorCode(), sudiResponse.getErrorMsg());
            App.post(() -> onFailure(call, e));
        }
    }
}
