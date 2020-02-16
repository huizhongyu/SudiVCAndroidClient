package cn.closeli.rtc.model.http;

import com.google.gson.Gson;

public class GsonProvider {
    private static final Gson gson = new Gson();
    public static Gson provide() {
        return gson;
    }
}
