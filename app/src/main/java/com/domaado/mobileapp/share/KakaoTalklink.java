package com.domaado.mobileapp.share;

import static com.kakao.util.helper.Utility.getPackageInfo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.myLog;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.friend.client.PickerClient;
import com.kakao.sdk.friend.model.OpenPickerFriendRequestParams;
import com.kakao.sdk.friend.model.PickerOrientation;
import com.kakao.sdk.friend.model.PickerServiceTypeFilter;
import com.kakao.sdk.friend.model.ViewAppearance;
import com.kakao.sdk.share.ShareClient;
import com.kakao.sdk.share.WebSharerClient;
import com.kakao.sdk.talk.TalkApiClient;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.ItemContent;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.Social;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Created by kbins(James Hong) on 2020,January,17
 */
public class KakaoTalklink {

    private String TAG = KakaoTalklink.class.getSimpleName();
    private Context context;

    private static KakaoTalklink kakaoTalklink;

    private ShareUtil shareUtil;

    public static final int KAKAO_SUCCESS   = 0;
    public static final int KAKAO_FAILURE   = 1;

    public static synchronized KakaoTalklink getInstance(Context context) {
        if(kakaoTalklink==null) {
            kakaoTalklink = new KakaoTalklink(context);
        }

        return kakaoTalklink;
    }

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

        uploadImageFromFile(file.getAbsolutePath(), new Handler(msg -> {
            switch (msg.what) {
                case 0:
                    postMessage(title, message, file.getAbsolutePath(), url);
                    break;
                case 1:
                    Toast.makeText(context, context.getResources().getString(R.string.share_upload_image_fail), Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }));

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

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        try {
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ShareClient.getInstance().uploadImage(file, (imageUploadResult, error) -> {
            if(error != null) {
                myLog.e(TAG, "*** ShareClient Error: "+error.toString());
                if(handler!=null) handler.sendEmptyMessage(1);
            } else if(imageUploadResult != null) {
                myLog.d(TAG, "*** ImageUploadSuccess!: "+imageUploadResult.toString());
                if(handler!=null) handler.sendEmptyMessage(0);
            }
            return null;
        });
    }

    public void selectFriend(Handler handler) {
        OpenPickerFriendRequestParams openPickerFriendRequestParams = new OpenPickerFriendRequestParams(
                "친구를 선택하세요",
                PickerServiceTypeFilter.TALK,
                ViewAppearance.AUTO,
                PickerOrientation.AUTO,
                true,
                true,
                true,
                true,true
        );

        PickerClient.getInstance().selectFriendsPopup(
                context,
                openPickerFriendRequestParams,
                (selectedUsers, error) -> {
                    if(error != null) {
                        myLog.d(TAG, "*** selectFriend error! - "+error.toString());
                    } else {
                        myLog.d(TAG, "*** selectFriend onSuccess! - "+selectedUsers.getTotalCount());

                        if(handler!=null) {
                            Message message = new Message();
                            message.obj = selectedUsers.getUsers().get(0).getUuid();
                            message.what = 0;

                            handler.sendMessage(message);
                        }
                    }
                    return null;
                });
    }

    private void sendKakaoTalk(String urlText, String title, String bodyText, String imgUrl) {

        FeedTemplate feedTemplate = getDefaultFeedTemplate(urlText, title, bodyText, imgUrl);

        if(checkKakaoTalk()) {
            ShareClient.getInstance().shareDefault(context, feedTemplate, ((sharingResult, error) -> {
                if(error!=null) {
                    myLog.e(TAG, "*** Error: "+error.getMessage());
                } else if(sharingResult!=null) {
                    myLog.d(TAG, "*** SUCCESS: "+sharingResult.getIntent());
                    context.startActivity(sharingResult.getIntent());

                    if(sharingResult.getWarningMsg().size()>0) {
                        for(Map.Entry<String, String> entry : sharingResult.getWarningMsg().entrySet()) {
                            myLog.w(TAG, "*** WarningMessage: " + entry.getKey()+" - "+entry.getValue());
                        }
                    }

                    if(sharingResult.getArgumentMsg().size()>0) {
                        for(Map.Entry<String, String> entry : sharingResult.getArgumentMsg().entrySet()) {
                            myLog.w(TAG, "*** ArgumentMessage: " + entry.getKey()+" - "+entry.getValue());
                        }
                    }
                }
                return null;
            }));
        } else {
            // 카카오톡이 설치되어있지 않음.
            Uri uri = WebSharerClient.getInstance().makeDefaultUrl(feedTemplate);

            try {
                KakaoCustomTabsClient.INSTANCE.openWithDefault(context, uri);
            } catch(UnsupportedOperationException e) {
                e.printStackTrace();
                myLog.e(TAG, "*** UnsupportedOperationException: "+e.getMessage());
            }

            // 2. CustomTabs으로 디바이스 기본 브라우저 열기
            try {
                KakaoCustomTabsClient.INSTANCE.open(context, uri);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                myLog.e(TAG, "*** ActivityNotFoundException: "+e.getMessage());
            }
        }

        // 카카오가 설치되어 있는지 확인 하는 메서드또한 카카오에서 제공 콜백 객체를 이용함
        Function2<OAuthToken, Throwable, Unit> callback = new  Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
                if(oAuthToken != null) {

                }
                if (throwable != null) {

                }
                updateKakaoLoginUi();
                return null;
            }
        };

        TalkApiClient.getInstance().friends(((friendFriends, error) -> {
            if(error != null) {
                myLog.e(TAG, "카카오톡 친구 목록 가져오기 실패:" + error);
            } else {
                if(friendFriends.getElements().isEmpty()) {
                    myLog.e(TAG, "메시지를 보낼 수 있는 친구가 없습니다.");
                } else {

                }
            }

            return null;
        }));

    }

    /**
     * 카카오톡 로그인 : 0 - 성공, 1 - 실패
     * @param handler
     */
    public void loginKakao(Handler handler) {

        Function2<OAuthToken, Throwable, Unit> callback = new  Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable error) {
                // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
                if(error!=null) {
                    myLog.e(TAG, "*** LOGIN ERROR: " + error.getMessage());
                    if(error instanceof com.kakao.sdk.common.model.ClientError) {
                        com.kakao.sdk.common.model.ClientError clientError = (com.kakao.sdk.common.model.ClientError) error;
                        if (clientError.getReason() == ClientErrorCause.Cancelled) {
                            return null;
                        }
                    }
                    if(handler!=null) {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                } else if(oAuthToken != null) {
                    myLog.d(TAG, "*** LOGIN SUCCESS: " + oAuthToken.toString());
                    if(handler!=null) {
                        Message message = new Message();
                        message.obj = oAuthToken;
                        message.what = 0;

                        handler.sendMessage(message);
                    }
                }

                return null;
            }
        };

        if(checkKakaoTalk()) {
            UserApiClient.getInstance().loginWithKakaoTalk(context, new  Function2<OAuthToken, Throwable, Unit>() {
                @Override
                public Unit invoke(OAuthToken oAuthToken, Throwable error) {

                    if(error!=null) {
                        myLog.e(TAG, "*** LOGIN ERROR: "+error.getMessage());

                        if(error instanceof com.kakao.sdk.common.model.ClientError) {
                            com.kakao.sdk.common.model.ClientError clientError = (com.kakao.sdk.common.model.ClientError) error;
                            if(clientError.getReason() == ClientErrorCause.Cancelled) {
                                // 사용자 취소
                                if(handler!=null) {
                                    Message message = new Message();
                                    message.what = 1;

                                    handler.sendMessage(message);
                                }
                            } else {
                                UserApiClient.getInstance().loginWithKakaoAccount(context, callback);
                            }
                        }
                    } else {
                        myLog.d(TAG, "*** LOGIN SUCCESS: " + oAuthToken.toString());

                        if(handler!=null) {
                            Message message = new Message();
                            message.obj = oAuthToken;
                            message.what = 0;

                            handler.sendMessage(message);
                        }
                    }
                    return null;
                }
            });
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(context, callback);
        }
    }

    private  void updateKakaoLoginUi(){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어있으면
                if (user!=null){

                    // 유저의 아이디
                    Log.d(TAG,"invoke: id" + user.getId());
                    // 유저의 어카운트정보에 이메일
                    Log.d(TAG,"invoke: nickname" + user.getKakaoAccount().getEmail());
                    // 유저의 어카운트 정보의 프로파일에 닉네임
                    Log.d(TAG,"invoke: email" + user.getKakaoAccount().getProfile().getNickname());
                    // 유저의 어카운트 파일의 성별
                    Log.d(TAG,"invoke: gerder" + user.getKakaoAccount().getGender());
                    // 유저의 어카운트 정보에 나이
                    Log.d(TAG,"invoke: age" + user.getKakaoAccount().getAgeRange());

//                    nickName.setText(user.getKakaoAccount().getProfile().getNickname());
//
//                    Glide.with(profileImage).load(user.getKakaoAccount().
//                            getProfile().getProfileImageUrl()).circleCrop().into(profileImage);
//                    loginButton.setVisibility(View.GONE);
//                    logoutButton.setVisibility(View.VISIBLE);
                } else {
                    // 로그인이 되어 있지 않다면 위와 반대로
//                    nickName.setText(null);
//                    profileImage.setImageBitmap(null);
//                    loginButton.setVisibility(View.VISIBLE);
//                    logoutButton.setVisibility(View.GONE);
                }

                return null;
            }
        });
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

    private FeedTemplate getDefaultFeedTemplate(String urlText, String title, String bodyText, String imgUrl) {

        myLog.e(TAG, "*** urlText: "+urlText);
        myLog.e(TAG, "*** title: "+title);
        myLog.e(TAG, "*** bodyText: "+bodyText);
        myLog.e(TAG, "*** imgUrl: "+imgUrl);

        //String newUrl = String.format("%s?url=%s", context.getResources().getString(R.string.share_url), urlText);
        Button button = new Button("자세히 보기", new Link(urlText, urlText));

        Content content = new Content(title, imgUrl, new Link(imgUrl, imgUrl), bodyText);

        ItemContent itemContent = new ItemContent();
        Social social = new Social();
        Button[] buttons = new Button[]{button};

        FeedTemplate feedTemplate = new FeedTemplate(content,
                new ItemContent(),
                new Social(),
                Arrays.asList(buttons));

        return feedTemplate;
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

    public boolean checkKakaoTalk() {
        return ShareClient.getInstance().isKakaoTalkSharingAvailable(context);
    }
}
