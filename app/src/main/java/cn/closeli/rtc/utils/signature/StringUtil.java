//package cn.closeli.rtc.utils.signature;
//
//import java.util.Random;
//import java.util.regex.Pattern;
//
//public class StringUtil {
//    private static Random random = new Random();
//    private static final String[] chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
//
//    public static void assertStringNotNullOrEmpty(String param, String paramName){
//        if (param == null)
//            throw new NullPointerException("ParameterIsNull:" + paramName);
//        if (param.length() == 0)
//            throw new IllegalArgumentException("ParameterStringIsEmpty:" + paramName);
//    }
//
//    public static boolean isNullOrEmpty(String value) {
//        return (value == null) || (value.length() == 0);
//    }
//
//    public static String getNonce(int length) {
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < length; i++) {
//            sb.append(chars[random.nextInt(31)]);
//        }
//        return sb.toString();
//    }
//
//    public static boolean isPhoneNumber(String phoneNum) {
//        if (StringUtil.isNullOrEmpty(phoneNum)) return false;
//        return Pattern.compile("1\\d{10}").matcher(phoneNum.trim()).matches();
//    }
//
//    public static boolean isEmail(String email) {
//        if (StringUtil.isNullOrEmpty(email)) return false;
//        return Pattern.compile("^[\\w-.]+@[\\w-]+(\\.{1}[\\w-]+)+$").matcher(email.trim()).matches();
//    }
//
//    public static String createValidateCode(int count) {
//        Random random = new Random();
//        StringBuilder authCode = new StringBuilder();
//        for (int i = 0; i < count; i++) {
//            int code = random.nextInt(10);
//            authCode.append(code);
//        }
//        return authCode.toString();
//    }
//
//    public static String getAppUniqueStr(int length) {
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < length; i++) {
//            sb.append(chars[random.nextInt(61)]);
//        }
//        return sb.toString();
//    }
//
//    public static String generateActiveCode() {
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < 5; i++) {
//            stringBuilder.append(getNonce(4)).append("-");
//        }
//        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
//    }
//
//    public static String generateActiveCode(String ak) {
//        StringBuilder stringBuilder = new StringBuilder();
//        String salt = getAppUniqueStr(4);
//        stringBuilder.append(salt, 0, 1)
//                .append(AESUtil.encrypt(ak, salt))
//                .append(salt, 1, 4);
//        return stringBuilder.toString();
//    }
//
//    public static String getUserAkByActiveCode(String activeCode) {
//        int len = activeCode.length();
//        String salt = activeCode.substring(0, 1) + activeCode.substring(len - 3);
//        String aesEncStr = activeCode.substring(1, len - 3);
//        return AESUtil.decrypt(aesEncStr, salt);
//    }
//
//    public static void main(String[] args) {
////        String ak = getAppUniqueStr(16);
////        System.out.println("AK:" + ak);
////        String activecode = generateActiveCode(ak);
////        System.out.println("active code:" + activecode);
////        System.out.println("ak:" + getUserAkByActiveCode(activecode));
////        System.out.println(getUserAkByActiveCode("8vDJyWMwddeXMS8IrsLG8Og==7Hh"));
//        String uri = "/webportal/api/face/v1/detect";
//        System.out.println(uri.substring(10));
//    }
//
//}
