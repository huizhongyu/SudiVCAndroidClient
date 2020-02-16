package cn.closeli.rtc.net;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import cn.closeli.rtc.App;
import cn.closeli.rtc.BuildConfig;
import cn.closeli.rtc.entity.User;
import cn.closeli.rtc.model.http.AccountModel;
import cn.closeli.rtc.model.http.AddrResp;
import cn.closeli.rtc.model.http.GsonProvider;
import cn.closeli.rtc.model.http.HttpCallback;
import cn.closeli.rtc.model.http.LoginResp;
import cn.closeli.rtc.model.http.SudiHttpCallback;
import cn.closeli.rtc.model.http.TokenResp;
import cn.closeli.rtc.model.http.UpgradeResp;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.LooperExecutor;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.signature.MD5Util;
import cn.closeli.rtc.utils.signature.SignatureUtil;
import cn.closeli.rtc.utils.trust.TrustAllCertsTrustManager;
import cn.closeli.rtc.utils.trust.TrustAllHostnameVerifier;
import okhttp3.Authenticator;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;

public class SudiHttpClient {
    //zcx
//    public static final String baseUrl = "https://172.25.15.130:443";
//    public static final String baseUrl = "http://172.25.15.130:80";
//    public static final String baseUrl = "http://172.25.15.130:80";
    //sss
//    public static final String baseUrl = "http://172.25.22.66:5000";
//    public static final String baseUrl = "http://172.25.15.130:5000";
    //开发
//    public static String baseUrl = "http://172.25.23.212:5000";
//    public static String baseUrl = "http://172.25.19.229:5000";

    public static String baseUrl = "https://47.107.183.28";
//    public static String baseUrl = "https://118.31.225.46";
    //测试环境
//    public static String baseUrl = "https://172.25.21.255";
    //sss 公网
//    public static  String baseUrl = "https://119.23.239.146";

    private OkHttpClient okHttpClient;

    private SudiHttpClient() {
        initOkHttp();
    }

    private void initOkHttp() {
        if (null == okHttpClient) {
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
            logInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(createSSLSocketFactory(true), new TrustAllCertsTrustManager())
                    .hostnameVerifier(new TrustAllHostnameVerifier())
                    .cookieJar(new CookieJar() {
                        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            cookieStore.put(url.host(), cookies);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            List<Cookie> cookies = cookieStore.get(url.host());
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    })
                    .addNetworkInterceptor(logInterceptor)
                    .build();
        }
    }

    private SSLSocketFactory createSSLSocketFactory(boolean https) {
        SSLSocketFactory ssfFactory = null;
        try {
            if (https) {
                CertificateFactory cAf = CertificateFactory.getInstance("X.509");
                InputStream caIn = App.getInstance().getApplicationContext().getAssets().open("certs/client.crt");
                X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
                KeyStore caKs = KeyStore.getInstance("PKCS12");
                caKs.load(null, null);
                caKs.setCertificateEntry("ca-certificate", ca);
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
                tmf.init(caKs);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
                ssfFactory = sslContext.getSocketFactory();
            } else {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new TrustAllCertsTrustManager()}, new SecureRandom());
                ssfFactory = sc.getSocketFactory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

    /**
     * 获取账号密码
     * @param sudiHttpCallback
     */
    public void getAccount(String serialNumber, SudiHttpCallback<AccountModel> sudiHttpCallback) {
        final String subUrl = "/api/v1/auth/get/account";
        final String url = baseUrl.concat(subUrl);
        Map<String, Object> body = new HashMap<>();
        String contentJson = GsonProvider.provide().toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), contentJson);

        String apiVer = "1.0";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentMd5 = MD5Util.encode(contentJson).toLowerCase();
        String date = new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(new Date());
        String key = MD5Util.encode(String.format("%s_%s_%s", SystemUtil.getSerial(), "GeeDowTech", date));
        String signature = signatureAccount(key, subUrl, contentMd5, serialNumber, apiVer, nonce, timestamp);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-MD5", contentMd5)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-Sd-Apiver", apiVer)
                .addHeader("X-Sd-Nonce", nonce)
                .addHeader("X-Sd-SerialNumber", serialNumber)
                .addHeader("X-Sd-Timestamp", timestamp)
                .addHeader("X-Sd-Signature", signature)
                .addHeader("X-Real-Ip", "127.0.0.1")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new HttpCallback<>(sudiHttpCallback, AccountModel.class));
    }

    private static class InstanceHolder {
        private static SudiHttpClient instance = new SudiHttpClient();
    }

    public static SudiHttpClient get() {
        return InstanceHolder.instance;
    }
    //<editor-fold desc="http">

    /**
     * 2.2获取Token
     *
     * @param account  第一次换取token时为企业管理员分配给用户的账号，eg. 15168256662@suditech.cn 获取完token之后，该字段为获取token时返回的userId值
     * @param password 没卵用 作为request body 同时 参与 请求头里的 签名
     */
    public void getToken(String account, String password, SudiHttpCallback<TokenResp> sudiHttpCallback) {
        String subUrl = "/api/v1/auth/get/token";
        String url = baseUrl.concat(subUrl);
        Map<String, Object> body = new HashMap<>();
        String contentJson = GsonProvider.provide().toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), contentJson);

        String apiver = "1.0";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentMd5 = MD5Util.encode(contentJson).toLowerCase();
        //key: 请求应用临时token时，key为企业管理员分配的密码的MD5值(小写)
        //      请求能力接口时，key为应用获取的临时token
        String key = MD5Util.encode(password).toLowerCase();
        String signature = signature(password, subUrl, contentMd5, account, apiver, nonce, timestamp);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-MD5", contentMd5)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-Sd-Account", account)//第一次换取token时为企业管理员分配给用户的账号，eg. 15168256662@suditech.cn 获取完token之后，该字段为获取token时返回的userId值
                .addHeader("X-Sd-Apiver", apiver)//字段标识整体平台能力协议的版本，当前固定为1.0
                .addHeader("X-Sd-Nonce", nonce)//字段为32位的随机字符串（防重放攻击），5分钟内客户端产生的请求中存在相同nonce视为非法请求
                .addHeader("X-Sd-Timestamp", timestamp)//请求时间戳（防重放攻击），请求时间与服务器时间差值超过5分钟视为非法请求
                .addHeader("X-Sd-Signature", signature)
                .addHeader("X-Real-Ip", "127.0.0.1")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new HttpCallback<TokenResp>(sudiHttpCallback, TokenResp.class));
    }

    /**
     * @param key        请求应用临时token时，key为企业管理员分配的密码的MD5值(小写)
     *                   请求能力接口时，key为应用获取的临时token
     * @param contentMd5 requestBody 的 md5
     * @param account    第一次换取token时为企业管理员分配给用户的账号，eg. 15168256662@suditech.cn 获取完token之后，该字段为获取token时返回的userId值
     * @param apiver     字段标识整体平台能力协议的版本，当前固定为1.0
     * @param nonce      字段为32位的随机字符串（防重放攻击），5分钟内客户端产生的请求中存在相同nonce视为非法请求
     * @param timestamp  请求时间戳（防重放攻击），请求时间与服务器时间差值超过5分钟视为非法请求
     * @return
     */
    public String signature(String key,
                            String subUrl,
                            String contentMd5,
                            String account,
                            String apiver,
                            String nonce,
                            String timestamp
    ) {

        StringBuilder sb = new StringBuilder()
                .append("POST").append("\n")
//                .append("/api/v1/auth/get/token").append("\n")
                .append(subUrl).append("\n")
                .append(contentMd5).append("\n")
                .append("application/json; charset=utf-8").append("\n")
                .append("X-Sd-Account:" + account).append("\n")
                .append("X-Sd-Apiver:" + apiver).append("\n")
                .append("X-Sd-Nonce:" + nonce).append("\n")
                .append("X-Sd-Timestamp:" + timestamp).append("\n");
        String sighStr = sb.toString();
        String sign = SignatureUtil.base64HmacSha256(key, sighStr);
        return sign;
    }

    public String signatureAccount(String key,
                            String subUrl,
                            String contentMd5,
                            String serialNumber,
                            String apiver,
                            String nonce,
                            String timestamp
    ) {

        StringBuilder sb = new StringBuilder()
                .append("POST").append("\n")
//                .append("/api/v1/auth/get/token").append("\n")
                .append(subUrl).append("\n")
                .append(contentMd5).append("\n")
                .append("application/json; charset=utf-8").append("\n")
                .append("X-Sd-Apiver:" + apiver).append("\n")
                .append("X-Sd-Nonce:" + nonce).append("\n")
                .append("X-Sd-SerialNumber:" + serialNumber).append("\n")
                .append("X-Sd-Timestamp:" + timestamp).append("\n");
        String sighStr = sb.toString();
        String sign = SignatureUtil.base64HmacSha256(key, sighStr);
        return sign;
    }

    /**
     * 4.2 用户登入
     *
     * @param account
     * @param password
     * @param sudiHttpCallback
     */
    public void httpLogin(String account, String password, SudiHttpCallback<LoginResp> sudiHttpCallback) {
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            return;
        }
        String subUrl = "/api/voip/v1/user/login";
        String url = baseUrl.concat(subUrl);
        Map<String, Object> body = new HashMap<>();
        body.put("platform", "androidSoftPhone");
        String contentJson = GsonProvider.provide().toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), contentJson);

        String apiver = "1.0";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentMd5 = MD5Util.encode(contentJson).toLowerCase();
        //key: 请求应用临时token时，key为企业管理员分配的密码的MD5值(小写)
        //      请求能力接口时，key为应用获取的临时token
        String key = SPEditor.instance().getToken();
        String userIdAsAccount = SPEditor.instance().getAccount();
        String signature = signature(key, subUrl, contentMd5, userIdAsAccount, apiver, nonce, timestamp);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-MD5", contentMd5)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-Sd-Account", userIdAsAccount)
                .addHeader("X-Sd-Apiver", apiver)
                .addHeader("X-Sd-Nonce", nonce)
                .addHeader("X-Sd-Timestamp", timestamp)
                .addHeader("X-Sd-Signature", signature)
                .addHeader("X-Real-Ip", "127.0.0.1")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new HttpCallback<LoginResp>(sudiHttpCallback, LoginResp.class));
    }

    /**
     * 4.4 获取信令服务地址列表
     *
     * @param sudiHttpCallback
     */
    public void getAddr(SudiHttpCallback<AddrResp> sudiHttpCallback) {
        String subUrl = "/api/voip/v1/lookup/signal/address";
        String url = baseUrl.concat(subUrl);
        Map<String, Object> body = new HashMap<>();
        String contentJson = GsonProvider.provide().toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), contentJson);

        String apiver = "1.0";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentMd5 = MD5Util.encode(contentJson).toLowerCase();
        //key: 请求应用临时token时，key为企业管理员分配的密码的MD5值(小写)
        //      请求能力接口时，key为应用获取的临时token
        String key = SPEditor.instance().getToken();
        String userIdAsAccount = SPEditor.instance().getAccount();
        String signature = signature(key, subUrl, contentMd5, userIdAsAccount, apiver, nonce, timestamp);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-MD5", contentMd5)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-Sd-Account", userIdAsAccount)
                .addHeader("X-Sd-Apiver", apiver)
                .addHeader("X-Sd-Nonce", nonce)
                .addHeader("X-Sd-Timestamp", timestamp)
                .addHeader("X-Sd-Signature", signature)
                .addHeader("X-Real-Ip", "127.0.0.1")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new HttpCallback<AddrResp>(sudiHttpCallback, AddrResp.class));
    }

    /**
     * 4.3 用户登出
     *
     * @param sudiHttpCallback
     */
    public void logOut(SudiHttpCallback<JsonObject> sudiHttpCallback) {
        String subUrl = "/api/voip/v1/user/logout";
        String url = baseUrl.concat(subUrl);
        Map<String, Object> body = new HashMap<>();
        body.put("platform", "androidSoftPhone");
        String contentJson = GsonProvider.provide().toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), contentJson);

        String apiver = "1.0";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentMd5 = MD5Util.encode(contentJson).toLowerCase();
        //key: 请求应用临时token时，key为企业管理员分配的密码的MD5值(小写)
        //      请求能力接口时，key为应用获取的临时token
        String key = SPEditor.instance().getToken();
        String userIdAsAccount = SPEditor.instance().getAccount();
        String signature = signature(key, subUrl, contentMd5, userIdAsAccount, apiver, nonce, timestamp);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-MD5", contentMd5)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-Sd-Account", userIdAsAccount)
                .addHeader("X-Sd-Apiver", apiver)
                .addHeader("X-Sd-Nonce", nonce)
                .addHeader("X-Sd-Timestamp", timestamp)
                .addHeader("X-Sd-Signature", signature)
                .addHeader("X-Real-Ip", "127.0.0.1")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new HttpCallback<JsonObject>(sudiHttpCallback, JsonObject.class));
    }

    public void httpCheckVersion(SudiHttpCallback<UpgradeResp> sudiHttpCallback) {
        String subUrl = "/api/voip/v1/upgrade/getLatestVersion";
        String url = baseUrl.concat(subUrl);
        Map<String, Object> body = new HashMap<>();
        body.put("platform", "androidHardPhone");
        String contentJson = GsonProvider.provide().toJson(body);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), contentJson);

        String apiver = "1.0";
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String contentMd5 = MD5Util.encode(contentJson).toLowerCase();
        //key: 请求应用临时token时，key为企业管理员分配的密码的MD5值(小写)
        //      请求能力接口时，key为应用获取的临时token
        String key = SPEditor.instance().getToken();
        String userIdAsAccount = SPEditor.instance().getUserId();
        String signature = signature(key, subUrl, contentMd5, userIdAsAccount, apiver, nonce, timestamp);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-MD5", contentMd5)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-Sd-Account", userIdAsAccount)
                .addHeader("X-Sd-Apiver", apiver)
                .addHeader("X-Sd-Nonce", nonce)
                .addHeader("X-Sd-Timestamp", timestamp)
                .addHeader("X-Sd-Signature", signature)
                .addHeader("X-Real-Ip", "127.0.0.1")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new HttpCallback<UpgradeResp>(sudiHttpCallback, UpgradeResp.class));
    }
    //</editor-fold>


    //<editor-fold desc="Old Deprecated">
    private static final MediaType MEDIATYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    @Deprecated
    private String httpPost(String url, String body) throws IOException {
        RequestBody reqBody = RequestBody.create(MEDIATYPE_JSON, body);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .post(reqBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return (response.body() != null) ? response.body().string() : null;
        } else {
//            throw new IOException("Unexpected code " + response);
            L.d("httpPost: Unexpected code " + response.toString());
            return String.valueOf(response.code());
        }
    }

    @Deprecated
    private String httpGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return (response.body() != null) ? response.body().string() : null;
        } else {
            //throw new IOException("Unexpected code " + response);
            L.d("httpGet: Unexpected code " + response.toString());
            return String.valueOf(response.code());
        }

    }

    public static final String APPSERVER_URL = "https://119.23.239.146:5000";
    private static final String PATH_LOGIN = "/api-login";
    private static final String PATH_LOGOUT = "/api-logout";
    private static final String PATH_GROUP = "/api-groups";
    private static final String PATH_SESSION = "/api-sessions";

    private JsonObject jsonResToken;
    private LooperExecutor executor = new LooperExecutor();

    @Deprecated
    public JsonObject generateToken(String channelId, String userId) {
        jsonResToken = null;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String rsp = httpGet(APPSERVER_URL + PATH_SESSION + "/gentoken/" + channelId + "/" + userId);
                    if (rsp.equals("403")) {
                        jsonResToken = new JsonObject();
                        jsonResToken.addProperty("token", "403");
                        jsonResToken.addProperty("compereId", "");
                    } else {
                        jsonResToken = GsonProvider.provide().fromJson(rsp, JsonObject.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    synchronized (SudiHttpClient.this) {
                        SudiHttpClient.this.notifyAll();
                    }
                }
            }
        });

        synchronized (this) {
            try {
                wait(2000);
            } catch (Exception e) {
            }
        }

        return jsonResToken;
    }

    private int httpLoginStatus = 0;

    /**
     * 退出 登录
     *
     * @return
     */
    @Deprecated
    public int logOut() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = httpGet(APPSERVER_URL + PATH_LOGOUT);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
        });

        synchronized (this) {
            try {
                wait(2000);
            } catch (Exception e) {
            }
        }

        httpLoginStatus = 0;
//        closeWebSocket();

        return 0;
    }

    OkHttpClient.Builder okHttpClientBuilder;

    @Deprecated
    public User logIn(String account, String password) {
//        user = null;
        this.okHttpClient = this.okHttpClientBuilder.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) {
                String credential = Credentials.basic(account, password);
                if (credential.equals(response.request().header("Authorization"))) {
                    L.d("authenticate: auth failed.");
                    return null;
                }
                return response.request().newBuilder().header("Authorization", credential).build();
            }
        }).build();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = httpGet(APPSERVER_URL + PATH_LOGIN);
//                    this.user = gson.fromJson(response, User.class);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                }
            }
        });

        synchronized (this) {
            try {
                wait(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        if (user != null) {
//            httpLoginStatus = 1;
//            connectWebSocket();
//        }
//        return user;
        return null;
    }
    //</editor-fold>
}
