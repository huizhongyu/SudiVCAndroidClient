//package cn.closeli.rtc.utils.signature;
//
//import cn.suditech.rtc.common.constants.HttpHeaderConstants;
//import cn.suditech.rtc.common.manager.CacheManage;
//import cn.suditech.rtc.common.restresp.RespEnum;
//import cn.suditech.rtc.common.utils.MD5Util;
//import cn.suditech.rtc.common.utils.SignatureUtil;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//
//@Service
//public class RequestAuthHandler {
//
//    @Value("${request.expired-duration}")
//    private long reqExpiredDuration;
//
//    @Resource
//    private CacheManage cacheManage;
//
//    public RespEnum requstAuth(HttpServletRequest request, String requestBody, String signKey) {
//        String clientIp, contentMd5, application, apiver, nonce, timestamp, signature;
//
//        clientIp = request.getHeader(HttpHeaderConstants.X_REAL_IP);
//        contentMd5 = request.getHeader(HttpHeaderConstants.CONTENT_MD5);
//
//        application = request.getHeader(HttpHeaderConstants.X_SD_ACCOUNT);
//        apiver = request.getHeader(HttpHeaderConstants.X_SD_APIVER);
//        nonce = request.getHeader(HttpHeaderConstants.X_SD_NONCE);
//        timestamp = request.getHeader(HttpHeaderConstants.X_SD_TIMESTAMP);
//        signature = request.getHeader(HttpHeaderConstants.X_SD_SIGNATURE);
//        if (StringUtils.isEmpty(clientIp) || StringUtils.isEmpty(contentMd5) || StringUtils.isEmpty(application) ||
//                StringUtils.isEmpty(apiver) || StringUtils.isEmpty(nonce) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(signature))
//            return RespEnum.INVALID_REQUEST_PARAMETERS;
//
//        // check request date
//        if (Math.abs(System.currentTimeMillis() - Long.valueOf(timestamp)) > reqExpiredDuration)
//            return RespEnum.REQUEST_EXPIRED;
//
//        // check request nonce
//        if (cacheManage.checkNonceDuplicated(application, nonce)) return RespEnum.NONCE_DUPLICATED;
//
//        // check request content md5
//        if (!MD5Util.encode(requestBody).equalsIgnoreCase(contentMd5))  return RespEnum.MD5_ERROR;
//
//        // check request frequency ip limit
//        if (cacheManage.invokeOverLimitByIp(clientIp)) return RespEnum.INVOKE_OVER_LIMIT_BY_IP;
//
//        try {
//            if (signKey == null || !SignatureUtil.verifySignature(request, signKey)) return RespEnum.INVALID_SIGNATURE;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return RespEnum.INVALID_SIGNATURE;
//        }
//
//        return RespEnum.SUCCESS;
//    }
//}
