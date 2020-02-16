package cn.closeli.rtc.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

import cn.closeli.rtc.App;
import cn.closeli.rtc.model.http.AddrResp;
import cn.closeli.rtc.model.http.LoginResp;
import cn.closeli.rtc.model.http.TokenResp;

/**
 * Author: Delete
 * Date: 16:45
 * Description:
 */
public class SPEditor {

    private SharedPreferences preferences;


    public static final String SP_FILE_NAME = "sudi";

    private static final String EMPTY_STR = "";


    public static final String USER_ID = "userId";
    public static final String TOKEN = "token";
    public static final String ACCOUNT = "account";
    public static final String NICK_NAME = "nickName";
    public static final String WSS_ADDR = "wss";
    public static final String SESSION_ID = "sessionId";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_STATUS = "password_status";
    public static final String MAC_ADDR = "macAddr";
    public static final String JOIN_ROOM = "join_room";
    public static final String JOIN_ACCOUNT = "join_account";
    public static final String DEVICENAME = "deviceName";
    public static final String BASE_URL = "base_url";


    private SPEditor() {
        this.preferences = App.getInstance().getApplicationContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }


    public static SPEditor instance() {
        return InstaceHolder.instance;
    }

    /**
     * @return true -> http 已登录
     * false-> http 未登录
     */
    public boolean isLogin() {
        return !TextUtils.isEmpty(getToken());
    }

    public void saveSessionId(String sessionId) {
        preferences.edit().putString(SESSION_ID, sessionId).apply();
    }

    public String getSessionId() {
        return preferences.getString(SESSION_ID, EMPTY_STR);
    }

    private static final class InstaceHolder {
        private static final SPEditor instance = new SPEditor();
    }

    public void clearAll() {
        preferences.edit().clear().apply();
    }

    public void clearLogin() {
        clearAll();
    }

    public String getToken() {
        return preferences.getString(TOKEN, EMPTY_STR);
    }

    public void setToken(String token) {
        preferences.edit().putString(TOKEN, token).apply();
    }

    public void setUserId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        preferences.edit().putString(USER_ID, userId).apply();
    }

    public String getUserId() {
        return preferences.getString(USER_ID, EMPTY_STR);
    }

    public String getNickName() {
        return preferences.getString(NICK_NAME, EMPTY_STR);
    }

    public void setAccount(String account){
        preferences.edit().putString(ACCOUNT,account).apply();
    }

    public String getAccount() {
        return preferences.getString(ACCOUNT, EMPTY_STR);
    }

    public void setPWD(String pwd) {
        preferences.edit().putString(PASSWORD, pwd).apply();
    }

    public String getPWD() {
        return preferences.getString(PASSWORD, EMPTY_STR);
    }

    public int getInt(String name) {
        return preferences.getInt(name, 0);
    }

    public int getInt(String name, int def) {
        return preferences.getInt(name, def);
    }

    public long getLong(String name) throws ClassCastException {
        return preferences.getLong(name, 0);
    }

    public float getFloat(String name) {
        return preferences.getFloat(name, 0);
    }

    public boolean getBoolean(String name) {
        return preferences.getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean def) {
        return preferences.getBoolean(name, def);
    }

    public String getString(String name) {
        return preferences.getString(name, null);
    }

    public Set<String> getSet(String name) {
        return preferences.getStringSet(name, null);
    }

    public void setStringSet(String name, Set<String> set) {
        preferences.edit().putStringSet(name, set).apply();
    }

    public String getString(String name, String def) {
        String temp = preferences.getString(name, def);
        if (temp.length() == 0) {
            return def;
        } else {
            return temp;
        }
    }

    public void setInt(String name, int value) {
        preferences.edit().putInt(name, value).apply();
    }

    public void setLong(String name, long value) {
        preferences.edit().putLong(name, value).apply();
    }

    public void setFloat(String name, float value) {
        preferences.edit().putFloat(name, value).apply();
    }


    public void setBoolean(String name, boolean value) {
        preferences.edit().putBoolean(name, value).apply();
    }

    public void setString(String name, String value) {
        preferences.edit().putString(name, value).apply();
    }

    public void saveTokenResp(TokenResp tokenResp) {
        preferences.edit()
                .putString(USER_ID, String.valueOf(tokenResp.getUserId()))
                .putString(ACCOUNT, tokenResp.getAccount())
                .putString(TOKEN, tokenResp.getToken())
                .apply();
    }

    public void saveUser(LoginResp loginResp) {
        preferences.edit()
//                .putString(USER_ID, loginResp.getUserId())
                .putString(USER_ID, String.valueOf(loginResp.getUserId()))
                .putString(ACCOUNT, loginResp.getAccount())
                .putString(NICK_NAME, loginResp.getNickName())
                .apply();
    }

    public void setDeviceName(String str) {
        preferences.edit().putString(DEVICENAME, str).apply();
    }

    public String getDeviceName() {
        return preferences.getString(DEVICENAME, null);
    }

    public String getUserNick() {
        return preferences.getString(NICK_NAME, null);
    }

    public void saveAddrs(AddrResp addrResp) {
        // TODO: 2019/9/20 地址 List 怎么保存

        preferences.edit()
                .putString(WSS_ADDR,
                        addrResp.getSignalAddrList().size() == 1
                                ? addrResp.getSignalAddrList().get(0) : EMPTY_STR)
                .apply();
    }


}