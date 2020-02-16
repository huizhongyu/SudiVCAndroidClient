package cn.closeli.rtc.app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BaseError<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String jsonrpc;
    private String method;

    /** 真正需要的数据，这里的T类型可能为对象，数组，还可能是布尔值，字符串... */
    private T error;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getError() {
        return error;
    }

    public void setError(T error) {
        this.error = error;
    }

    /**
     * 是否请求成功，可重写
     *
     * @return
     */
    public boolean isSuccess() {
        return id == 1000;
    }

    public static BaseError fromJson(String json, Class clazz) {
        Gson gson = new Gson();
        Type objectType = type(BaseError.class, clazz);
        return gson.fromJson(json, objectType);
    }
    public static BaseError fromJson(JsonObject json, Class clazz) {
        Gson gson = new Gson();
        Type objectType = type(BaseError.class, clazz);
        return gson.fromJson(json, objectType);
    }
    public String toJson(Class<T> clazz) {
        Gson gson = new Gson();
        Type objectType = type(BaseError.class, clazz);
        return gson.toJson(this, objectType);
    }

    static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }
}
