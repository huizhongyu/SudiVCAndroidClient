package cn.closeli.rtc.app.observer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.CallSuper;
import android.util.Log;

import cn.closeli.rtc.app.BaseResult;
import cn.closeli.rtc.app.exception.ApiException;
import cn.closeli.rtc.app.exception.ApiExceptionUtil;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**

 */

public abstract class BaseObserver<T> implements Observer<BaseResult<T>> {

    private static final String TAG = "BaseObserver";

    private Context context;
    private Disposable disposable;

    /**
     * 最终的回调
     *
     * @param t data或者整个ApiResponse
     */
    public abstract void onNext2(@NonNull T t);

    /**
     * 最终的回调
     *
     * @param t 转换过后的Exception
     */
    public abstract void onError2(@NonNull Throwable t);

    /**
     * 当返回码不是成功码的时候回调此方法，可以重写
     *
     * @param msg code
     */
    public void onFail(int code, String msg) {

    }

    /**
     * 返回信息（msg）
     *
     * @param msg
     */
    public void onNext1(String msg) {

    }

    public BaseObserver(Activity context) {

    }

    public BaseObserver() {

    }





    @Override
    public void onSubscribe(@NonNull Disposable d) {
        Log.d(TAG, "onSubscribe d == " + d);
        disposable = d;
    }

    @Override
    public void onNext(@NonNull BaseResult<T> value) {

        int code = value.getId();
        if (code != 0) {
            disposeCodeDataNullIng(value);
        } else {
            T t = value.getResult();
            onNext1(value.getJsonrpc());
            onNext2(t);

        }

    }

    /**
     * @param value 当data为空时
     */
    private void disposeCodeDataNullIng(BaseResult<T> value) {
            // Toast.makeText(BaseConfig.getContext(), value.getMsg(), Toast.LENGTH_SHORT).show();
            // onFail(value.getCode(), value.getMsg());
            onFailure(new ApiException(value.getId(), value.getJsonrpc()));

    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
        // 将其他所有异常转换为ApiException
        onFailure(ApiExceptionUtil.onError(e));

    }

    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete");
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @CallSuper
    public void onFailure(@NonNull Throwable t) {
        Log.d(TAG, "onFailure");
        // 一些统一处理的操作
        // UIUtils.toastMessage(t.getMessage());
        // 子类去处理
        onError2(t);
    }

}
