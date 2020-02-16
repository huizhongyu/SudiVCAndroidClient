package cn.closeli.rtc.model.http;

public interface SudiHttpCallback<T> {
    void onSuccess(T response);

    void onFailed(Throwable e);
}
