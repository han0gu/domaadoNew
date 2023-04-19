package com.domaado.mobileapp.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.locale.LocaleUtils;
import com.domaado.mobileapp.widget.myLog;

import java.util.HashMap;
import java.util.Locale;

/**
 *
 * google tts engine packagename : com.google.android.tts
 *
 * Created by HongEuiChan on 2017. 10. 28..
 *
 * 앱 시작시 시작 TTS 서비스
 */

public class TTSService extends Service implements TextToSpeech.OnInitListener {

    private Context mContext;
    private String str;
    private TextToSpeech mTts;
    private static final String TAG = "TTSService";

    @Override

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {

        this.mContext = getBaseContext();

        LocaleUtils.initializeStartup(this);

        mTts = new TextToSpeech(mContext,
                this,  // OnInitListener
                Constant.TTSEngine
        );

        myLog.d(TAG, "*** TTSService: "+mTts.getDefaultEngine());

        super.onCreate();
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    Handler checkHandler;

    private void checkFinishTTS() {

        if(checkHandler!=null) checkHandler.removeCallbacks(null);

        if(mTts != null && !mTts.isSpeaking()) {

            mTts.shutdown();
            mTts = null;

            //this.stopSelf();
            stopService(new Intent(mContext, TTSService.class));
            myLog.v(TAG, "*** TTS finish");

        } else {
            checkHandler = new Handler();
            checkHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkFinishTTS();
                }
            }, 1000);
        }
    }

    @Override
    public void onInit(int status) {
        myLog.v(TAG, "*** TTS oninit - "+ status);

        if(status != TextToSpeech.ERROR) {
            Locale locale = Common.getConfigLanguageLocale(mContext);
            int result = mTts.setLanguage(locale);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                myLog.e(TAG, "*** Language is not available.");
            }

            mTts.setSpeechRate(1f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21("", TextToSpeech.QUEUE_FLUSH);
            } else {
                ttsUnder20("", TextToSpeech.QUEUE_FLUSH);
            }

            str = getResources().getString(R.string.voice_tts_startup);
            sayHello(str);

            checkHandler = new Handler();
            checkHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkFinishTTS();
                }
            }, 3000);
        } else {
            myLog.e(TAG, "*** Could not initialize TextToSpeech. ("+status+")");
        }

    }

    private void ttsUnder20(String text, int type) {
        if(TextUtils.isEmpty(text)) return;

        myLog.d(TAG, "*** ttsUnder20: text: "+text);

        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        mTts.speak(text, type, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text, int type) {
        if(TextUtils.isEmpty(text)) return;

        myLog.d(TAG, "*** ttsGreater21: text: "+text);

        String utteranceId=this.hashCode() + "";
        mTts.speak(text, type, null, utteranceId);
    }

    private void sayHello(String str) {

        String isAllow = Common.getConfig(mContext, Constant.CONFIG_VOICE_GUIDE);

        if("1".equals(isAllow)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(str, TextToSpeech.QUEUE_ADD);
            } else {
                ttsUnder20(str, TextToSpeech.QUEUE_ADD);
            }
        }
    }
}
