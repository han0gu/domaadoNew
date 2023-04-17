package com.domaado.market.locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;

import androidx.annotation.StringDef;

import com.domaado.market.Common;
import com.domaado.market.Constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 *
 */
public class LocaleUtils {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({KOREAN, ENGLISH, CHINESE, JAPANESE, FRENCH})
    public @interface LocaleDef {
        String[] SUPPORTED_LOCALES = {KOREAN, ENGLISH, CHINESE, JAPANESE, FRENCH};
    }

    public static final String KOREAN   = "ko";
    public static final String ENGLISH  = "en";
    public static final String CHINESE  = "zh";
    public static final String JAPANESE = "ja";
    public static final String FRENCH   = "fr";

    public static void initialize(Context context) {
        @LocaleDef String lang = getLocale(context);

        setLocale(context, lang, true);
    }

    public static void initialize(Context context, @LocaleDef String defaultLanguage) {
        setLocale(context, defaultLanguage, true);
    }

    public static void initializeStartup(Context context) {
        @LocaleDef String lang = getLocale(context);

        setLocale(context, lang, false);
    }

    public static String getLocale(Context context) {
        return Common.getConfig(context, Constant.CONFIG_USERLANGUAGE);
    }

    public static boolean setLocale(Context context, @LocaleDef String language, boolean isSet) {
        return updateResources(context, language, isSet);
    }

    public static boolean setLocale(Context context, int languageIndex, boolean isSet) {
        if (languageIndex >= LocaleDef.SUPPORTED_LOCALES.length) {
            return false;
        }

        return updateResources(context, LocaleDef.SUPPORTED_LOCALES[languageIndex], isSet);
    }

    public static @LocaleDef
    String getLanguage(String lang) {
        @LocaleDef String result = KOREAN;

        if(!TextUtils.isEmpty(lang)) {
            if ("ko".equals(lang.toLowerCase()) || "kr".equals(lang.toLowerCase())) result = KOREAN;
            else if ("en".equals(lang.toLowerCase()) || "en".equals(lang.toLowerCase()) || "uk".equals(lang.toLowerCase())) result = ENGLISH;
            else if ("fr".equals(lang.toLowerCase())) result = FRENCH;
            else if ("ja".equals(lang.toLowerCase()) || "ja".equals(lang.toLowerCase())) result = JAPANESE;
            else if ("zh".equals(lang.toLowerCase()) || "zh".equals(lang.toLowerCase()) || "ch".equals(lang.toLowerCase())) result = CHINESE;
        }

        return result;
    }

    private static boolean updateResources(Context context, String language, boolean isSet) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        if(isSet) Common.setConfig(context, Constant.CONFIG_USERLANGUAGE, language);

        return true;
    }
}
