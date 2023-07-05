package com.domaado.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.domaado.mobileapp.data.CheckUpdateRequest;
import com.domaado.mobileapp.data.CheckUpdateResponse;
import com.domaado.mobileapp.data.QueryParams;
import com.domaado.mobileapp.firebase.MyFirebaseMessagingService;
import com.domaado.mobileapp.locale.LocaleUtils;
import com.domaado.mobileapp.sensors.GPSTracker;
import com.domaado.mobileapp.share.KakaoTalklink;
import com.domaado.mobileapp.submenus.WebContentActivity;
import com.domaado.mobileapp.widget.myLog;
import com.kakao.sdk.common.KakaoSdk;

import java.io.Serializable;

/**
 * Intro animation
 */
public class SplashScreen extends AppCompatActivity {
	
	private final static String TAG = SplashScreen.class.getSimpleName();
	private final static int fi = 500, fo = 500, fd = 1000;

	AlphaAnimation animFadeIn, animFadeOut;
	private long SPLASH_TIME_OUT;

	private InitialApplication initialApplication;
	private Handler loadHandler = new Handler(msg -> {
		switch(msg.what) {
			case InitialApplication.ERROR_RESPONSE_CONDITION: // 아직은 오류 무시!

			case InitialApplication.NORMAL_USER_CONDITION:
			case InitialApplication.NEED_LOGIN_CONDITION:
			case InitialApplication.NEW_USER_CONDITION: {
				PermissionManager permissionManager = new PermissionManager(this, new CallMethodObject() {
					@Override
					public Object call() {
						loadMainActivity(msg.what, false);
						return null;
					}
				});
				permissionManager.checkPermissionAll();

				break;
			}

//			case InitialApplication.NEW_USER_CONDITION:
//				openTutorials();
//				break;
			default:
				finish();
				break;
		}
		return true;
	});

	ActivityResultLauncher<Intent> SettingResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		checkSensors(new CallMethodObject() {
			@Override
			public Object call() {
				initSteps();
				return null;
			}
		});
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intro);

		//Common.setTaskBarColored(this, 0);

		LocaleUtils.initialize(this);

		ImageView demoImage = (ImageView) findViewById(R.id.intro_logo);
		int imagesToShow[] = { R.drawable.screen };

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		myLog.i(TAG, "*** KAKAOTALK HASH KEY: "+ KakaoTalklink.getInstance(this).getKeyHash());

		KakaoSdk.init(this, getResources().getString(R.string.kakao_app_key));

		setAnimationInit();
		animate(demoImage, imagesToShow, 0, false);

		SPLASH_TIME_OUT = (fi + fo + fd) * imagesToShow.length + 300;

		setTestMode();

		PermissionManager permissionManager = new PermissionManager(this, new CallMethodObject() {
			@Override
			public Object call() {

				runOnUiThread(() -> initSteps());

				return null;
			}
		});

		permissionManager.checkPermissionDefault();

		checkUpScheme(getIntent());
	}

	public void checkUpScheme(@NonNull Intent intent) {

		myLog.e(TAG, "*** checkScheme!");

		if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();

			String host = uri.getHost();
			String scheme = uri.getScheme();

			final String q = uri.getQueryParameter("q");
			QueryParams queryParams = new QueryParams(host, scheme, q);
			App.setQueryParam(queryParams);

			myLog.e(TAG, "*** checkScheme queryParams: " + queryParams.toString());

		} else if(intent.getExtras()!=null) {
			Bundle bundle = intent.getExtras();
			Serializable serializable = bundle!=null ? bundle.getSerializable("queryParams") : null;
			if(serializable!=null) {
				QueryParams qp = (QueryParams) serializable;
				myLog.e(TAG, "*** onResume queryParams: "+qp.toString());
				App.setQueryParam(qp);
			}
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		/**
		 * FirebaseMessagingService가 시스템을 통해 메시지가 전달되는 경우 {"data": } 내부 데이터가 전달됨.
		 */

		QueryParams queryParams = new QueryParams();
		if(intent!=null) {
			for(String key : queryParams.fields) {
				if(intent.hasExtra(key)) queryParams.set(key, intent.getStringExtra(key));
			}

			App.setQueryParam(queryParams);

		}

		myLog.e(TAG, "*** onNewIntent - queryParam: "+(App.getQueryParams()!=null ? App.getQueryParams().toString() : "null"));

	}

	@Override
	protected void onResume() {
		super.onResume();

		myLog.d(TAG, "*** onResume!");

	}

	public void setTestMode() {
		App.setIsTEST(false);
	}

	@Override
	protected void onStart() {
		super.onStart();

        myLog.d(TAG, "*** onStart!");

	}

	private void initSteps() {
		myLog.e(TAG, "*** initSteps!");

		checkDebug();

		isScheme(getIntent());

		// 퍼미션가이드!
//		openPermission();
		initApplication();
	}

	public boolean checkDebug() {
		String version = Common.getAppVersion(this);
		myLog.debugMode = false;

		((TextView)findViewById(R.id.intro_version)).setText(version);

		return myLog.debugMode;
	}

	private SchemeData schemeData = new SchemeData();
	class SchemeData {
		String callbackType;
		String seq;
	}
	private void isScheme(Intent intent) {
		if(intent!=null) {
			String callbackType = intent.getStringExtra("callbackType");
			String seq = intent.getStringExtra("seq");

			if(!TextUtils.isEmpty(callbackType)) {
				// 스킴으로 접근되어 콜된 상태!
				schemeData.callbackType = callbackType;
				schemeData.seq = seq;
			}

			// PUSH 등으로 앱이 구동되는 경우!
			QueryParams queryParams = (QueryParams) intent.getSerializableExtra("queryParam");
			if(queryParams!=null) {
				myLog.e(TAG, "*** queryParam: "+queryParams.toString());
				App.setQueryParam(queryParams);
			}
		}
	}

	private void initApplication() {
		myLog.d(TAG, "*** initApplication!");

		initialApplication = new InitialApplication(this, new InitialApplication.InitialListener() {
			@Override
			public void onUpdated(String deviceId, String fcmToken) {
				myLog.d(TAG, "*** onUpdated!");

				Common.setNewDefaultUUID(SplashScreen.this, deviceId);
				App.setFcmToken(fcmToken);

				initStart();
			}

			@Override
			public void onError(String message, String deviceId) {
				myLog.e(TAG, "*** ERROR: "+message);

				try {
					Common.alertMessage(SplashScreen.this,
							getResources().getString(R.string.app_name),
							message,
							getResources().getString(R.string.btn_continue),
							new Handler(msg -> {
								Common.setNewDefaultUUID(SplashScreen.this, deviceId);
								initStart();
								return true;
							}));
				} catch(Exception e) {
					e.printStackTrace();
					myLog.e(TAG, "*** Exception: "+e.getMessage());
					Toast.makeText(SplashScreen.this, message, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void responseSuccess(CheckUpdateResponse response) {
				myLog.d(TAG, "*** responseSuccess!");

				String hashKey = Common.getKeyHash(SplashScreen.this);
				myLog.e(TAG, "*** HashKey: "+hashKey);

				if(response!=null && !TextUtils.isEmpty(response.getMessage())) {
					Toast.makeText(SplashScreen.this, response.getMessage(), Toast.LENGTH_SHORT).show();
				}

				if(response.getMemberEntry()!=null) {
					App.setMemberEntry(response.getMemberEntry());

					if(!TextUtils.isEmpty(App.getMemberEntry().getTopic())) {
						MyFirebaseMessagingService.setTopicSubscribe(SplashScreen.this, App.getMemberEntry().getTopic());
					} else {
						myLog.d(TAG, "TOPIC IS EMPTY!");
					}
				}

				// 계속사용자인경우 온보딩 SKIP
//				if("Y".equalsIgnoreCase(response.getContinueUserYn())) {
//					Common.saveSharedPreferencesString(TutorialsActivity.ONCE_ONBOARDING_SHOW_KEY, "1", SplashScreen.this);
//				}

				// 디바이스에 연결된 회원이 없는경우
				if("Y".equalsIgnoreCase(response.getContinueUserYn())) {
					loadHandler.sendEmptyMessage(InitialApplication.NORMAL_USER_CONDITION);
				} else if("N".equalsIgnoreCase(response.getContinueUserYn()) && App.hasMember()) {
					loadHandler.sendEmptyMessage(InitialApplication.NEED_LOGIN_CONDITION);
				} else {
					loadHandler.sendEmptyMessage(InitialApplication.NEW_USER_CONDITION);
				}
			}

			@Override
			public void responseFailure(CheckUpdateResponse response) {
				String message = response != null && !TextUtils.isEmpty(response.getMessage()) ? response.getMessage() : getResources().getString(R.string.update_check_error);

				myLog.e(TAG, "*** responseFailure: " + message);

				loadHandler.sendEmptyMessage(InitialApplication.ERROR_RESPONSE_CONDITION);

//				Common.alertMessage(SplashScreen.this,
//						getResources().getString(R.string.app_name),
//						message,
//						getResources().getString(R.string.btn_ok),
//						new Handler(msg -> {
//							// API 23이상 퍼미션 체크!
//							loadHandler.sendEmptyMessage(InitialApplication.ERROR_RESPONSE_CONDITION);
//							return true;
//						}));
			}

			@Override
			public void responseTimeout(CheckUpdateResponse response) {
				String message = response != null && !TextUtils.isEmpty(response.getMessage()) ? response.getMessage() : getResources().getString(R.string.update_check_error);

				myLog.e(TAG, "*** responseTimeout: " + message);

				loadHandler.sendEmptyMessage(InitialApplication.ERROR_RESPONSE_CONDITION);

//				Common.alertMessage(SplashScreen.this,
//						getResources().getString(R.string.app_name),
//						message,
//						getResources().getString(R.string.btn_ok),
//						new Handler(msg -> {
//							// API 23이상 퍼미션 체크!
//							loadHandler.sendEmptyMessage(InitialApplication.ERROR_RESPONSE_CONDITION);
//							return true;
//						}));
			}
		});

		initialApplication.initGoogleAppId();
	}

	private void checkSensors(CallMethodObject callMethodObject) {

		if(!Common.checkGpsOn(this)) {
			// GPS 꺼짐..
			Common.alertMessage(SplashScreen.this,
					getResources().getString(R.string.app_name),
					getResources().getString(R.string.setting_check_gps),
					getResources().getString(R.string.btn_setting),
					getResources().getString(R.string.btn_no),
					new Handler(msg -> {
						switch (msg.what) {
							case Constant.ALERTDIALOG_RESULT_YES:
								Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								SettingResult.launch(intent);
								break;
							case Constant.ALERTDIALOG_RESULT_NO:
							case Constant.ALERTDIALOG_RESULT_NUTRUAL:
								try {
									callMethodObject.call();
								} catch(Exception e) {
									e.printStackTrace();
								}

								break;
						}
						return true;
					}));


		} else {
			try {
				callMethodObject.call();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initStart() {
		initPosition();
	}

	/**
	 * 사용자 환경 검사 (인터넷, 블루투스, GPS)
	 *
	 * @return
	 */
	private boolean checkDeviceEnvironment(int what) {
		boolean isCheck = false;

		if("Y".equals(Common.getConfig(this, Constant.REQUIRE_JOIN))) {
			joinMember();
		} else
		if ("Y".equals(Common.getConfig(this, Constant.CONFIG_HAVEUPDATE))) {
			isCheck = true;
			// 업데이트가 존재한다!
			Common.alertMessage(this,
					getResources().getString(R.string.app_name),
					getResources().getString(R.string.server_have_update),
					getResources().getString(R.string.btn_yes),
					getResources().getString(R.string.btn_no),
					new Handler(msg -> {
						switch (msg.what) {
							case Constant.ALERTDIALOG_RESULT_YES:
								Common.appDownload(SplashScreen.this, getPackageName());
								break;
							default:
								// 아니오를 눌렀다.
								Common.saveSharedPreferencesString(Constant.CONFIG_HAVEUPDATE, "P", SplashScreen.this);
								loadMainActivity(what, true);
								break;
						}
						return true;
					}));

		}

		return isCheck;
	}

	private void doCheckUpdate() {

		if(initialApplication!=null) {
			CheckUpdateRequest checkUpdateRequest = new CheckUpdateRequest(this);
			if(App.getCurrentLocation()!=null) {
				checkUpdateRequest.setLat(App.getCurrentLocation().getLatitude());
				checkUpdateRequest.setLon(App.getCurrentLocation().getLongitude());
			}

			checkUpdateRequest.setFcmToken(App.getFcmToken());

			initialApplication.checkAppUpdate(checkUpdateRequest);
		} else {
			Toast.makeText(this, "APPLICATION IS NOT Initialized!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private void joinMember() {

		Common.removeSharedPreferences(Constant.CONFIG_USERLANGUAGE, this);
		Common.removeSharedPreferences(Constant.CONFIG_AGREE, this);

		Common.removeSharedPreferences(Constant.REQUIRE_JOIN, this);

	}

	private void loadMainActivity(int what, boolean skipEnvironment) {
		loadMainActivity(what, skipEnvironment, false);
	}

	private void loadMainActivity(int what, final boolean skipEnvironment, boolean runMode) {
		Intent i;

		if (!checkDeviceEnvironment(what) && !skipEnvironment) {

			if(schemeData!=null && !TextUtils.isEmpty(schemeData.callbackType)) {
				setResultData();
				return;
			}

			if(!App.hasMember()) {
				what = InitialApplication.NEED_LOGIN_CONDITION;
			}

			switch (what) {
				case InitialApplication.NORMAL_USER_CONDITION: {
					i = new Intent(SplashScreen.this, WebContentActivity.class);
					break;
				}
				case InitialApplication.NEED_LOGIN_CONDITION:
				default: {
					i = new Intent(SplashScreen.this, WebContentActivity.class);
					break;
				}
			}

			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			startActivity(i); //, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());

			finish();
		} else {
			// 다른 액션으로 진입!
		}
	}

	private void setResultData() {
		Intent intent = new Intent();

		intent.putExtra("callbackType", schemeData.callbackType);
		intent.putExtra("seq", schemeData.seq);

		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * 메인화면 호출전 처리
	 *
	 * 1. 메타데이터 리스토어
	 * 2. 단말 환경 검사
	 * 3. 외부유입 처리
	 * 4. 설정값 유무 확인 후 설정화면 호출
	 */
	private void loadMain() {

		// 센서 테스트할 때.
//		loadSensorMain();
//		finish();

		if(Common.checkNetworkConnect(SplashScreen.this)) {
			doCheckUpdate();
		} else {
			// 인터넷 연결이 안되고 있음.
			if(!isBackPressed) {
				Intent i = new Intent(SplashScreen.this, CheckInternetConnection.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

				startActivity(i);
			}

			finish();
		}

	}

	/**
	 * 애니메이션 초기화
	 */
	private void setAnimationInit() {
		animFadeIn = new AlphaAnimation(0,1);
		animFadeOut = new AlphaAnimation(1,0);
		
		animFadeIn.setDuration((long)fi);
		animFadeOut.setDuration((long)fo);
		
		animFadeIn.setFillAfter(true);
		
		animFadeIn.setInterpolator(new AccelerateInterpolator());
		animFadeOut.setInterpolator(new DecelerateInterpolator());
	}

	/**
	 * 첫화면 Fadein
	 *
	 * @param view
	 */
	private void fadeinView(final View view) {
		
		AlphaAnimation anim = new AlphaAnimation(0,1);
		anim.setDuration((long)fi);
		anim.setFillAfter(true);
		anim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) { }

			@Override
			public void onAnimationStart(Animation animation) { }
			
		});
		
		anim.setInterpolator(new AccelerateInterpolator());
		
		view.startAnimation(anim);

	}

	/**
	 * 인트로 애니메이션 처리 (복수화면 가능)
	 *
	 * @param imageView
	 * @param images
	 * @param imageIndex
	 * @param forever
	 */
	private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

		myLog.v(TAG, "########## images = " + images[imageIndex] + ", number = "+ imageIndex);

		imageView.setVisibility(View.INVISIBLE);
		imageView.setImageResource(images[imageIndex]);

		animFadeOut.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				myLog.e(TAG, "########### animFadeOut onAnimationEnd");
				if (images.length - 1 > imageIndex) {
					animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
				} else {
					if (forever == true) {
						animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
					}
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {
				myLog.d(TAG, "################## animFadeOut  onAnimationStart ");
			}

		});

		animFadeIn.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				myLog.e(TAG, "########### animFadeIn onAnimationEnd");

				if(animation == animFadeIn) {
					myLog.d(TAG, "############ go animFadeOut!");
					//imageView.setAnimation(animFadeOut);
					new Handler().postDelayed(new Runnable() {
						public void run() {
							if (images.length - 1 > imageIndex)
								imageView.startAnimation(animFadeOut);

						}
					}, (long)fd);

				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				myLog.d(TAG, "################## animFadeIn  onAnimationStart ");
			}

		});

		imageView.startAnimation(animFadeIn);

	}
	
	@Override
	public void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}
	
	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean isBackPressed = false;

	/**
	 * 백버튼 핸들 Twice처리
	 */
	@Override
	public void onBackPressed() {

		if(!isBackPressed) {
			isBackPressed = true;
			Toast.makeText(this, getResources().getString(R.string.twicback_to_exit_message), Toast.LENGTH_SHORT).show();

		} else {
			super.onBackPressed();
		}
	}

	private void initPosition() {

		myLog.d(TAG, "*** initPosition ");

		GPSTracker.getInstance(this).requestUpdateGPS();
		updateLocation(GPSTracker.getInstance(this).getFastLocation());
	}

	private void updateLocation(Location location) {

		myLog.d(TAG, "*** updateLocation: "+(location!=null ? location.toString() : "null"));

		GPSTracker.getInstance(this).stopUsingGPS();

		if(location!=null) {
			App.setCurrentLocation(location);
			loadMain();
		} else {
			loadMain();
		}
	}
}
