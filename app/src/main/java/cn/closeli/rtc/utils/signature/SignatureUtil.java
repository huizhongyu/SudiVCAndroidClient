package cn.closeli.rtc.utils.signature;


//import cn.suditech.rtc.common.constants.HttpHeaderConstants;
//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.util.StringUtils;
//import javax.servlet.http.HttpServletRequest;
//

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.closeli.rtc.utils.L;

//@Slf4j
public class SignatureUtil {
//
//    public static boolean verifySignature(HttpServletRequest request, String signKey) throws NoSuchAlgorithmException, InvalidKeyException {
//        StringBuilder strBuilder = new StringBuilder();
//        strBuilder.append(request.getMethod()).append("\n")
//                .append(request.getRequestURI()).append("\n")
//                .append(request.getHeader(HttpHeaderConstants.CONTENT_MD5)).append("\n")
//                .append(request.getHeader(HttpHeaderConstants.CONTENT_TYPE)).append("\n");
//        if (!StringUtils.isEmpty(request.getHeader(HttpHeaderConstants.X_SD_ACCOUNT)))
//            strBuilder.append(HttpHeaderConstants.X_SD_ACCOUNT).append(":").append(request.getHeader(HttpHeaderConstants.X_SD_ACCOUNT)).append("\n");
//        if (!StringUtils.isEmpty(request.getHeader(HttpHeaderConstants.X_SD_APIVER)))
//            strBuilder.append(HttpHeaderConstants.X_SD_APIVER).append(":").append(request.getHeader(HttpHeaderConstants.X_SD_APIVER)).append("\n");
//        strBuilder.append(HttpHeaderConstants.X_SD_NONCE).append(":").append(request.getHeader(HttpHeaderConstants.X_SD_NONCE)).append("\n")
//                .append(HttpHeaderConstants.X_SD_TIMESTAMP).append(":").append(request.getHeader(HttpHeaderConstants.X_SD_TIMESTAMP)).append("\n");
//        log.info((strBuilder.toString()));
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(new SecretKeySpec(signKey.getBytes(), "HmacSHA256"));
//        byte[] rawHmac = mac.doFinal(strBuilder.toString().getBytes());
//        String sign = Base64.encode(rawHmac);
//        log.info("requ:" + request.getHeader(HttpHeaderConstants.X_SD_SIGNATURE));
//        log.info("calc:" + sign);
//        return request.getHeader(HttpHeaderConstants.X_SD_SIGNATURE).equals(sign);
//    }

    public static String base64HmacSha256(String signKey, String value) {
        try {
            L.d("base64HmacSha256-value -> %1$s", value);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signKey.getBytes(), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(value.getBytes());
            String sign = new String(Base64Util.encode(rawHmac));
            L.d("base64HmacSha256-base64 -> %1$s", sign);
            return sign;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }

//    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
//        String temp = "POST\n" +
//                "/get/token\n" +
//                "99914b932bd37a50b983c5e7c90ae93b\n" +
//                "application/json\n" +
//                "x-zwn-apiver:1.0\n" +
//                "x-zwn-application:YeAvNfkC8aTxSWGt\n" +
//                "x-zwn-nonce:GyN59KauZyaZOUuKKpdJySI8IMJ11\n" +
//                "x-zwn-timestamp:"+ System.currentTimeMillis() +"\n";
//        System.out.println(temp);
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(new SecretKeySpec("CCWP1Nw3e5fMTq4ogIyiGlFV7Sg3ep".getBytes(), "HmacSHA256"));
//        byte[] rawHmac = mac.doFinal(temp.getBytes());
//        System.out.println(Base64.encode(rawHmac));
//    }

}
