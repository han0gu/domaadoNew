package com.domaado.mobileapp.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.widget.Toast;

import com.kakao.network.ErrorResult;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.ItemContent;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.Social;
import com.kakao.util.helper.log.Logger;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.myLog;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kakao.util.helper.Utility.getPackageInfo;

/**
 * Created by kbins(James Hong) on 2020,January,17
 */
public class KakaoTalklink {

    private String TAG = KakaoTalklink.class.getSimpleName();
    private Context context;

    private ResponseCallback<KakaoLinkResponse> callback;
    private Map<String, String> serverCallbackArgs;

    private ShareUtil shareUtil;

    public KakaoTalklink(Context context) {
        this.context = context;

        shareUtil = new ShareUtil(context);
    }

    public void postMessage(String title, String message) {
        String link = "https://play.google.com/store/apps/details?id="+ context.getPackageName();
        postMessage(title, message, link, R.drawable.ic_launcher);
    }

    public void postMessage(String title, String message, String url) {
        postMessage(title, message, url,  R.drawable.ic_launcher);
    }

    public void postMessage(String title, String message, String url, int res) {

        File cachePath = context.getCacheDir();
        String filename = String.format("%s.png", Common.getDate("yyyyMMdd_HHmmss"));
        File file = shareUtil.getFileFromDrawable(res, cachePath, filename);

        uploadImageFromFile(file.getAbsolutePath(), new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case 0:
                        postMessage(title, message, file.getAbsolutePath(), url);
                        break;
                    case 1:
                        Toast.makeText(context, context.getResources().getString(R.string.share_upload_image_fail), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

    }

    public void postMessage(String title, String message, String imageUrl, String url) {

        if (shareUtil.appInstalled(shareUtil.shareAppPkgName[ShareUtil.SHARE_KTALK_PKG])) {
            sendKakaoTalk(url, title, message, imageUrl);
        } else {
            shareUtil.shareAppDl(shareUtil.shareAppPkgName[ShareUtil.SHARE_KTALK_PKG]);
        }
    }

    public void uploadImageFromFile(String path, Handler handler) {
        File file = new File(path);

        KakaoLinkService.getInstance().uploadImage(context, false, file, new ResponseCallback<ImageUploadResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
                myLog.d(TAG, "*** uploadImageFromFile onFailure! - "+errorResult.toString());

                if(handler!=null) handler.sendEmptyMessage(1);
            }

            @Override
            public void onSuccess(ImageUploadResponse result) {
                if(handler!=null) handler.sendEmptyMessage(0);
            }
        });
    }

    public void shareApp() {

        String link = "https://play.google.com/store/apps/details?id="+ context.getPackageName();

        // 기본적인 스크랩 템플릿을 사용하여 보내는 코드
        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendScrap(context, link, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
                myLog.d(TAG, "*** shareApp onFailure! - "+errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                myLog.d(TAG, "*** shareApp onSuccess! - "+result.getTemplateId());
            }
        });
    }

    private void sendKakaoTalk(String urlText, String title, String bodyText, String imgUrl) {

        callback = new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                myLog.e(TAG, "*** onFailure: "+errorResult.getErrorMessage());
                Toast.makeText(context, errorResult.getErrorMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                myLog.e(TAG, "*** onSuccess: Successfully sent KakaoLink v2 message.");
//                Toast.makeText(context, "Successfully sent KakaoLink v2 message.", Toast.LENGTH_LONG).show();
            }
        };

        serverCallbackArgs = new HashMap<>();
        serverCallbackArgs.put("user_id", "1234");
        serverCallbackArgs.put("title", title);

        //sendLink(urlText, title, bodyText, imgUrl, width, height);
        sendDefaultFeedTemplate(urlText, title, bodyText, imgUrl);

    }

    private void sendTextToKaKao(String message) {
        Intent sendIntent = new Intent();
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.kakao.talk");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getString(R.string.text_share_intent_chooser)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void sendImageToKaKao(String imageUrl) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("image/png");
        Uri uri = Uri.parse(imageUrl);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setPackage("com.kakao.talk");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getString(R.string.text_share_intent_chooser)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void sendMultipleImagesToKaKao(String[] imageUtls) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        sendIntent.setType("image/png");
        ArrayList<Uri> files = new ArrayList<Uri>();
        if(imageUtls != null) {
            for(String url : imageUtls) {
                Uri uri1 = Uri.parse(url);
                files.add(uri1);
            }
        }
        sendIntent.setPackage("com.kakao.talk");
        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getString(R.string.text_share_intent_chooser)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void sendDefaultFeedTemplate(String urlText, String title, String bodyText, String imgUrl) {

        myLog.e(TAG, "*** urlText: "+urlText);
        myLog.e(TAG, "*** title: "+title);
        myLog.e(TAG, "*** bodyText: "+bodyText);
        myLog.e(TAG, "*** imgUrl: "+imgUrl);

        //String newUrl = String.format("%s?url=%s", context.getResources().getString(R.string.share_url), urlText);
        Button button = new Button("자세히 보기", new Link(urlText, urlText));

        FeedTemplate params = new FeedTemplate(new Content(title, imgUrl, new Link(imgUrl, imgUrl), bodyText),
                new ItemContent(),
                new Social(),

        List<Button> buttons = new ArrayList<>(){new Button("자세히 보기", new Link(urlText, urlText))};


        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder(title,
                        imgUrl,
                        LinkObject.newBuilder().setWebUrl(imgUrl)
                                .setMobileWebUrl(imgUrl).build())
                        .setDescrption(bodyText)
                        .build())
//				.setSocial(SocialObject.newBuilder().setLikeCount(286).setCommentCount(45)
//						.setSharedCount(845).build())
//                .addButton(new ButtonObject("이미지 보기", LinkObject.newBuilder().setWebUrl(imgUrl).setMobileWebUrl(imgUrl).build()))
                .addButton(new ButtonObject("자세히 보기", LinkObject.newBuilder().setWebUrl(urlText).setMobileWebUrl(urlText).build()))
//				.addButton(new ButtonObject("앱으로 보기", LinkObject.newBuilder()
//						.setWebUrl(urlText)
//						.setMobileWebUrl(urlText)
//						.setAndroidExecutionParams("")
//						.setIosExecutionParams("")
//						.build()))
                .build();

        KakaoLinkService.getInstance().sendDefault(context, params, serverCallbackArgs, callback);


    }

    /**
     * release: keytool -exportcert -alias <release_key_alias> -keystore <release_keystore_path> | openssl sha1 -binary | openssl base64
     * debug: keytool -exportcert -alias androiddebugkey -keystore <debug_keystore_path> -storepass android -keypass android | openssl sha1 -binary | openssl base64
     *
     * @return
     */
    public String getKeyHash() {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                myLog.w(TAG, "Unable to get MessageDigest. signature=" + signature + ": "+ e);
            }
        }
        return null;
    }
}
