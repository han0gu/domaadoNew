package com.domaado.market;

import android.content.Context;

/**
 *
 */
public class Constant {

    public static final String REQUEST_TYPE = "vd2";

    public final static int CONNECT_TIMEOUT = 35000;
    public final static int READ_TIMEOUT = 35000;

    public final static int REUEST_TIMEOUT = 30;    // seconds

    public final static int[] cryptIvs = { 0x21, 0x40, 0x23, 0x24, 0x25, 0x5e, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30 };
    public final static String ENCODING = "UTF-8";

    public final static String FCM_SERVER_KEY = "";

    // 음성안내
    public final static String CONFIG_VOICE_GUIDE = "voiceguide";
    public final static String[] CONFIG_VOICE_GUIDE_VALUE = new String[]{"0", "1"};

    // 사용동의
    public final static String CONFIG_AGREE = "agree";
    public final static String[] CONFIG_AGREE_VALUE = new String[]{"0", "1"};

    // 추천인 입력
    public final static String CONFIG_RECOMMENDER = "recommender";
    public final static String[] CONFIG_RECOMMENDER_VALUE = new String[]{"0", "1"};

    // 7일간 안보이기 (타임스탬프 저장 후 비교하여 처리)
    public final static String CONFIG_KEEP_7DAYS = "keep_ad";

    // 사용언어 설정값
    public final static String CONFIG_USERLANGUAGE = "userlang";
    public final static String[] CONFIG_USERLANGUAGE_SERVER_VALUE = new String[]{"ko", "en", "zh", "jp", "fr"};

    // 업데이트 존재여부 저장 키!
    public final static String CONFIG_HAVEUPDATE 	= "haveupdate";
    public final static String REQUIRE_JOIN		    = "require_join";	// 회원가입이 필요함.

    public final static String DRIVER_GRADE         = "driver_grade";   // 발렛기사 구분

    // 기사구분값
    public final static int DRIVER_GRADE_MANAGER = 0;
    public final static int DRIVER_GRADE_DRIVER  = 1;
    public final static int DRIVER_GRADE_PARKER  = 2;

    public final static int getDriverGrade(Context ctx) {
        String grade = Common.getSharedPreferencesString(DRIVER_GRADE, ctx);

        if("manager".equalsIgnoreCase(grade)) return DRIVER_GRADE_MANAGER;
        else if("driver".equalsIgnoreCase(grade)) return DRIVER_GRADE_DRIVER;
        else if("parker".equalsIgnoreCase(grade)) return DRIVER_GRADE_PARKER;
        else return DRIVER_GRADE_DRIVER;
    }

    // 메인 하단에 주소창 표시 유지시간!
    public final static int MAIN_BOTTOM_GPS_BOX_SHOW    = 7000; // 7초

    public final static int LANGUAGE_KOREA      = 0;
    public final static int LANGUAGE_ENGLISH    = 1;
    public final static int LANGUAGE_CHINA      = 2;
    public final static int LANGUAGE_JAPAN      = 3;
    public final static int LANGUAGE_FRANCE     = 4;

    public static final String TTSEngine        = "com.google.android.tts";

    public static final int RESPONSE_SUCCESS	= 0;
    public static final int RESPONSE_FAILURE	= 1;
    public static final int RESPONSE_TIMEOUT	= 2;

    public static final int ALERTDIALOG_RESULT_YES      = 0;
    public static final int ALERTDIALOG_RESULT_NO       = 1;
    public static final int ALERTDIALOG_RESULT_NUTRUAL  = 3;

    public static final String MAIN_FRAGMENT_ARG_RANGE  = "range";
    public static final String MAIN_FRAGMENT_ARG_LAT    = "lat";
    public static final String MAIN_FRAGMENT_ARG_LON    = "lon";
    public static final String MAIN_FRAGMENT_ARG_ADDR   = "addr";
    public static final String MAIN_FRAGMENT_ARG_DATA   = "data";

    public static final int RESULT_SEND_PARKING_PHOTO   = 100;
    public static final int RESULT_SEND_DELEVERY_PHOTO  = 200;
    public static final int RESULT_SEND_ARRIVEDD_PHOTO  = 300;
    public static final int RESULT_CONNECTED_DRIVER     = 400;
    public static final int RESULT_SHOW_COMPLETE_VALET  = 500;
    public static final int RESULT_CHOOSE_VALET         = 600;
    public static final int RESULT_NAVI_CHOOSED         = 700;
    public static final int RESULT_ADD_VALET            = 800;
    public static final int RESULT_GO_PARKING           = 810;
    public static final int RESULT_MENU_RESULT          = 820;
    public static final int RESULT_GO_FINISH_QUESTION   = 830;
    public static final int RESULT_TAKE_PICTURE         = 990;

    /* push action */
    public static final String PUSH_ACTION_ALERT        = "alert";      // 알럿메시지!

    public final static String PUSH_ACTION_REFRESH_LIST = "refresh_list";
    public final static String PUSH_ACTION_OPEN_URL     = "open_url";

    public final static String REFRESH_FILTER = "com.domaado.market.firebase.refresh.list";
    public final static String OPENURL_FILTER = "com.domaado.market.firebase.open.url";

    public static final String DEFAULT_CALLBACK_ARRIVE_TIME = "10";     // 10분!

    public static final String CPU_ARCH_ARM64_V8A       = "arm64-v8a";
    public static final String CPU_ARCH_ARMEABI         = "armeabi";
    public static final String CPU_ARCH_ARMEABI_V7A     = "armeabi-v7a";
    public static final String CPU_ARCH_X86_64          = "x86_64";
    public static final String CPU_ARCH_X86             = "x86";

    public static final String DEFAULT_CAMERA_APP_KEY	= "def_camera_app";

    public static final String WEBVIEW_BRIDGE_PREFIX    = "android";

    public final static int REFRESH_UPDATE_LOCATION_SECONDS    = 1;

    public final static String MYCRYPT_IV(int[] keys) {
        StringBuffer sb = new StringBuffer("");
        for(int i=0; i<keys.length; i++) {
            sb.append((char)keys[i]);
        }

        return sb.toString();
    }

    public final static boolean EXIT_WITH_PRE_WEBVIEW_HISTORY   = false;    // false인경우 웹뷰 히스토리와 관계없이 back key로 종료된다.
}
