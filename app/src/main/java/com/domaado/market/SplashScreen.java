package com.domaado.market;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.iid.FirebaseInstanceId;
import com.domaado.market.data.CheckUpdateResponse;
import com.domaado.market.data.QueryParams;
import com.domaado.market.firebase.MyFirebaseMessagingService;
import com.domaado.market.locale.LocaleUtils;
import com.domaado.market.network.UrlManager;
import com.domaado.market.sensors.GPSTracker;
import com.domaado.market.submenus.WebContentActivity;
import com.domaado.market.task.CheckUpdateTask;
import com.domaado.market.widget.CustomAlertDialog;
import com.domaado.market.widget.myLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Intro animation
 */
public class SplashScreen extends AppCompatActivity {

	private Context mContext;
	
	private final static String TAG = SplashScreen.class.getSimpleName();
	private final static int fi = 500, fo = 500, fd = 1000;
	private static int SPLASH_TIME_OUT;

	public final static int REQUEST_SETTING_GPS 	= 98;
	public final static int REQUEST_ENABLE_BT 		= 99;

	AlphaAnimation animFadeIn, animFadeOut;
	
	private final int MY_PERMISSION_REQUEST = 100;
    private Handler permissionHandler;

	private String[] perms;
    private String[] permsMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intro);

		//Common.setTaskBarColored(this, 0);

		LocaleUtils.initialize(this);

		mContext = getBaseContext();

		FirebaseInstanceId.getInstance().getToken();

		ImageView demoImage = (ImageView) findViewById(R.id.intro_logo);
		int imagesToShow[] = { R.drawable.screen };

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		setAnimationInit();
		animate(demoImage, imagesToShow, 0, false);

		SPLASH_TIME_OUT = (fi + fo + fd) * imagesToShow.length + 300;

		checkSensors();

	}

	@Override
	protected void onResume() {
		super.onResume();

		myLog.d(TAG, "*** onResume!");

	}

	@Override
	protected void onStart() {
		super.onStart();

        myLog.d(TAG, "*** onStart!");

	}

	private void checkSensors() {
		if(!Common.checkGpsOn(this)) {
			// GPS 꺼짐..

			Common.alertMessage(SplashScreen.this,
					getResources().getString(R.string.app_name),
					getResources().getString(R.string.setting_check_gps),
					getResources().getString(R.string.btn_setting),
					getResources().getString(R.string.btn_no),
					new Handler() {
						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);

							switch (msg.what) {
								case Constant.ALERTDIALOG_RESULT_YES:
									Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivityForResult(intent, REQUEST_SETTING_GPS);
									break;
								case Constant.ALERTDIALOG_RESULT_NO:
								case Constant.ALERTDIALOG_RESULT_NUTRUAL:
									SplashScreen.this.finish();
									break;
							}
						}
					});


		} else {

			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

				@Override
				public void run() {

					checkPermission(new Handler() {

						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);

//							loadMain();
							initPosition();
						}
					});

				}

				// loading progress add
			}, SPLASH_TIME_OUT);

		}
	}

	/**
	 * 외부링크 유입 처리
	 */
	private void checkOutsideLink() {

		myLog.d(TAG, "*** checkOutsideLink");

		if(getIntent()!=null) {

			String title = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_TITLE);
			String message = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_MESSAGE);
			String responseId = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_RESPONSE_ID);
			String action = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_ACTION);

			String lat = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_LAT);
			String lon = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_LON);
			String url = getIntent().getStringExtra(MyFirebaseMessagingService.FCM_DATA_URL);

			if(!TextUtils.isEmpty(action)) {
				App.setQueryParam(new QueryParams(title, message, responseId, action, lat, lon, url));

				myLog.d(TAG, "*** checkOutsideLink getIntent().getAction(): "+getIntent().getAction()+", "
						+MyFirebaseMessagingService.FCM_DATA_RESPONSE_ID+": "+responseId+", "
						+MyFirebaseMessagingService.FCM_DATA_TITLE+": "+title+", "
						+MyFirebaseMessagingService.FCM_DATA_ACTION+": "+action+", "
						+MyFirebaseMessagingService.FCM_DATA_URL+": "+url);
			} else {
				// intent로 값이 전달되지 않았다면, uri에 있는지 확인한다.
				Uri uri = getIntent().getData();

				if(uri!=null && !TextUtils.isEmpty(uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_ACTION))) {

					String host = uri.getHost();
					responseId = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_RESPONSE_ID);
					title = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_TITLE);
					message = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_MESSAGE);
					action = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_ACTION);

					lat = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_LAT);
					lon = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_LON);
					url = uri.getQueryParameter(MyFirebaseMessagingService.FCM_DATA_URL);

					myLog.d(TAG, "*** checkOutsideLink getIntent().getData(): "+getIntent().getAction()+", "
							+MyFirebaseMessagingService.FCM_DATA_RESPONSE_ID+": "+responseId+", "
							+MyFirebaseMessagingService.FCM_DATA_TITLE+": "+title+", "
							+MyFirebaseMessagingService.FCM_DATA_ACTION+": "+action+", "
							+MyFirebaseMessagingService.FCM_DATA_URL+": "+url);

					App.setQueryParam(new QueryParams(title, message, responseId, action, lat, lon, url));

				}
			}
		}
	}

	/**
	 * 업데이트 확인 후 퍼미션 확인!
	 *
	 * @param handler
	 */
	private void checkAppUpdate(final Handler handler) {

		// 우선 추천인을 받지 않는것으로 설정한다.
		Common.saveSharedPreferencesString(Constant.CONFIG_RECOMMENDER, Constant.CONFIG_RECOMMENDER_VALUE[0], SplashScreen.this);
		Common.removeSharedPreferences(Constant.CONFIG_HAVEUPDATE, SplashScreen.this);

		CheckUpdateTask checkUpdateTask = new CheckUpdateTask(this, false, new Handler(msg -> {

			CheckUpdateResponse response = (CheckUpdateResponse) msg.obj;

			switch (msg.what) {
				case Constant.RESPONSE_SUCCESS: {
					// 추천인을 받을것인지 확인!
					if (response != null && "Y".equals(response.getRecommenderYn())) {
						// 추천인을 받는다!
						Common.saveSharedPreferencesString(Constant.CONFIG_RECOMMENDER, Constant.CONFIG_RECOMMENDER_VALUE[1], SplashScreen.this);
					}

					if (response != null && !"Y".equals(Common.toUpperString(response.getMemberYn()))) {
						Common.saveSharedPreferencesString(Constant.REQUIRE_JOIN, "Y", SplashScreen.this);
					}

					// 업데이트가 존재하는지!! Y면 있다!
					if (response != null && "Y".equals(Common.toUpperString(response.getUpdateYn()))) {
						Common.saveSharedPreferencesString(Constant.CONFIG_HAVEUPDATE, "Y", SplashScreen.this);
					}

					if (response != null && !TextUtils.isEmpty(response.getCallStatus())) {
						setStatusData(response);
					}

					// 기사구분값 저장
					Common.saveSharedPreferencesString(Constant.DRIVER_GRADE, response.getDriverGrade(), SplashScreen.this);

					if (response != null && !TextUtils.isEmpty(response.getCallcenterTel())) {
						App.setCallCenterTel("tel:" + response.getCallcenterTel());
					} else {
						App.setCallCenterTel(getResources().getString(R.string.cscenter_phone_number));
					}

					String hashKey = Common.getKeyHash(SplashScreen.this);
					myLog.e(TAG, "*** HashKey: " + hashKey);

					// 출근상태인지 확인
					//if("on".equalsIgnoreCase(response.getRecommenderYn())) hasLogin = true;

					// API 23이상 퍼미션 체크!
					//checkPermission(handler);

					if (handler != null) handler.sendEmptyMessage(0);

					break;
				}
				case Constant.RESPONSE_FAILURE:
				case Constant.RESPONSE_TIMEOUT: {
					String message = response != null && !TextUtils.isEmpty(response.getMessage()) ? response.getMessage() : "Update check error!";

					Toast.makeText(SplashScreen.this, message, Toast.LENGTH_SHORT).show();

					if (handler != null) handler.sendEmptyMessage(0);

//					Common.alertMessage(SplashScreen.this,
//							getResources().getString(R.string.app_name),
//							message,
//							getResources().getString(R.string.btn_ok),
//							new Handler(msg2 -> {
//								// API 23이상 퍼미션 체크!
//								//checkPermission(handler);
//
//								if (handler != null) handler.sendEmptyMessage(0);
//
//								return true;
//							}));
					break;
				}
			}

			return true;

		}));

		checkUpdateTask.execute(UrlManager.getServerUrl(this), UrlManager.getCheckUpdate(this));
	}

	private void setStatusData(CheckUpdateResponse response) {
		String data = response.getCallStatus();

		try {
			data = new String(Common.getBase64decode(data));
			JSONObject json = new JSONObject(data).getJSONObject("data");

			myLog.d(TAG, "*** json: "+json.toString());

			String responseId = json.has("response_id") ? json.getString("response_id") : response.getResponseId();
			String action = json.has("action") ? json.getString("action") : response.getCallStatus();
			String url = json.has("url") ? json.getString("url") : "";

			App.setQueryParam(new QueryParams("", "", responseId, action, response.getCustomerLat(), response.getCustomerLon(), url));

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 사용자 환경 검사 (인터넷, 블루투스, GPS)
	 *
	 * @return
	 */
	private boolean checkDeviceEnvironment() {
		boolean isCheck = false;

		if("Y".equals(Common.getConfig(this, Constant.REQUIRE_JOIN))) {
			joinMember();
		} else
		if ("Y".equals(Common.getConfig(this, Constant.CONFIG_HAVEUPDATE))) {
			isCheck = true;
			// 업데이트가 존재한다!
			alertMessage(getResources().getString(R.string.app_name), getResources().getString(R.string.server_have_update), true, new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);

					if(msg.what == 0) {
						Common.appDownload(SplashScreen.this, getPackageName());
					} else {
						// 아니오를 눌렀다.
						Common.saveSharedPreferencesString(Constant.CONFIG_HAVEUPDATE, "P", SplashScreen.this);

//						loadMain();
						initPosition();
					}
				}
			});
//
////		} else
////		if(!Common.checkNetworkStatus(SplashScreen.this)) {
////			isCheck = true;
////			// 인터넷 사용불가!
////			alertMessage(getResources().getString(R.string.app_name), getResources().getString(R.string.can_not_connect_server), false, new Handler() {
////				@Override
////				public void handleMessage(Message msg) {
////					super.handleMessage(msg);
////
////					SplashScreen.this.finish();
////				}
////			});
//		} else
////		if(!Common.checkBluetoothOn(this)) {
////			isCheck = true;
////			// 블루투스 꺼짐.
////			alertMessage(getResources().getString(R.string.app_name), getResources().getString(R.string.setting_check_bluetooth), true, new Handler() {
////				@Override
////				public void handleMessage(Message msg) {
////					super.handleMessage(msg);
////
////					if(msg.what == 0) {
////						Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
////						SplashScreen.this.startActivityForResult(intent, REQUEST_ENABLE_BT);
////					} else {
////						SplashScreen.this.finish();
////					}
////				}
////			});
////		} else
//		if(!Common.checkGpsOn(this)) {
//			isCheck = true;
//			// GPS 꺼짐..
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					Common.alertMessage(SplashScreen.this,
//							getResources().getString(R.string.setting_check_gps),
//							getResources().getString(R.string.btn_setting),
//							getResources().getString(R.string.btn_no),
//							new Handler() {
//								@Override
//								public void handleMessage(Message msg) {
//									super.handleMessage(msg);
//
//									switch(msg.what) {
//										case Constant.ALERTDIALOG_RESULT_YES:
//											Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//											startActivityForResult(intent, REQUEST_SETTING_GPS);
//											break;
//										case Constant.ALERTDIALOG_RESULT_NO:
//										case Constant.ALERTDIALOG_RESULT_NUTRUAL:
//											SplashScreen.this.finish();
//											break;
//									}
//								}
//							});
//				}
//			});
//
////			alertMessage(getResources().getString(R.string.app_name), getResources().getString(R.string.setting_check_gps), true, new Handler() {
////				@Override
////				public void handleMessage(Message msg) {
////					super.handleMessage(msg);
////
////					if(msg.what == 0) {
////						//Common.callGPSSetting(mContext);
////
////						//loadMain();
////						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////						startActivityForResult(intent, REQUEST_SETTING_GPS);
////					} else {
////						SplashScreen.this.finish();
////					}
////				}
////			});
		}

		return isCheck;
	}

	private void joinMember() {

		Common.removeSharedPreferences(Constant.CONFIG_USERLANGUAGE, this);
		Common.removeSharedPreferences(Constant.CONFIG_AGREE, this);

		Common.removeSharedPreferences(Constant.REQUIRE_JOIN, this);

	}

	private void alertMessage(String title, String message, boolean isCancel, final Handler handler) {
		final CustomAlertDialog mesgBox = new CustomAlertDialog(this);
		myLog.d(TAG, "Create alterDialog box!");

		mesgBox.setMTitle(title); // 팝업 타이틀
		mesgBox.setMessage(message); // 팝업 내용
		mesgBox.setCancelable(false);

		if(isCancel) {
			mesgBox.setYesButton((isCancel ? mContext.getString(R.string.btn_setting) : mContext.getString(R.string.btn_close)), new View.OnClickListener() {
				public void onClick(View v) {

					if (mesgBox != null) {

						mesgBox.dismiss();

						if(handler != null) {
							handler.sendEmptyMessage(0);
						}
					}
					return;
				}

			});

			mesgBox.setNoButton(mContext.getString(R.string.btn_no), new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					if (mesgBox != null) {

						mesgBox.dismiss();

						if(handler != null) {
							handler.sendEmptyMessage(1);
						}
					}
				}
			});
		} else {
			mesgBox.setCloseButton(mContext.getString(R.string.btn_neutral), new View.OnClickListener() {
				public void onClick(View v) {

					if (mesgBox != null) {

						mesgBox.dismiss();

						if(handler != null) {
							handler.sendEmptyMessage(0);
						}
					}
					return;
				}

			});
		}

		mesgBox.setOnKeyListener( // 백버튼을 눌렀을 때
				new AlertDialog.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							dialog.dismiss();
							dialog = null;
						}
						return false;
					}
				});

		mesgBox.setCancelable(false);

		try {

			if (mesgBox != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					mesgBox.create();
				}

				mesgBox.show();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
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

		checkOutsideLink();

		if(Common.checkNetworkConnect(SplashScreen.this)) {

			if (!checkDeviceEnvironment()) {

				// check update!
				checkAppUpdate(new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						super.handleMessage(msg);

						switch(msg.what){
							case 0: {
								Intent i = new Intent(SplashScreen.this, WebContentActivity.class);
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

								startActivity(i);

								finish();

								break;
							}
							case 1: {
								//미허용된 권한이 있다.!
//								CustomToast toast = new CustomToast(SplashScreen.this);
//								toast.showToast(getResources().getString(R.string.permission_not_allowed), Toast.LENGTH_LONG);
//								finish();

								break;
							}
						}

					}
				});



/**
				if (Common.isSetConfig(this, Constant.CONFIG_USERLANGUAGE) && Common.isSetConfig(this, Constant.CONFIG_AGREE) && Common.isSetConfig(this, Constant.CONFIG_RECOMMENDER)) {

					if(hasLogin) {
						Intent i = new Intent(SplashScreen.this, SubContent.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

						startActivity(i);
					} else {
						Intent i = new Intent(SplashScreen.this, LoginActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

						startActivity(i);
					}

				} else {
					// 언어설정없이 기본언어로 지정!
					if (!Common.isSetConfig(this, Constant.CONFIG_USERLANGUAGE)) {
						Locale locale = Common.getConfigLanguageLocale(this);
						Common.saveSharedPreferencesString(Constant.CONFIG_USERLANGUAGE, locale.getLanguage(), this);
					}

					int step = !Common.isSetConfig(this, Constant.CONFIG_USERLANGUAGE) ? 0 : !Common.isSetConfig(this, Constant.CONFIG_AGREE) ? 1 :
							!Common.isSetConfig(this, Constant.CONFIG_RECOMMENDER) ? 2 : 3;

					Intent i = new Intent(SplashScreen.this, Agreement.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					i.putExtra("step", step);

					startActivity(i);
				}
 **/

//				finish();
			}
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
	                } else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
								if(progressBar!=null) {
									progressBar.setVisibility(View.VISIBLE);
								}
							}
						});
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
					    	fadeinView(findViewById(R.id.introMessage));
							if (images.length - 1 > imageIndex)
								imageView.startAnimation(animFadeOut);
							else
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
										if(progressBar!=null) {
											progressBar.setVisibility(View.VISIBLE);
										}
									}
								});
					    	
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

//	private void checkPermission(Handler handler) {
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			PermissionManager permissionManager = new PermissionManager(this, handler);
//			permissionManager.checkPermissionDefault();
//		} else {
//			handler.sendEmptyMessage(0);
//		}
//
//	}

	/**
	 * 마시멜로우이상 버전을 위해 퍼미션 관련 처리
	 *
	 * @param perm
	 * @return
	 */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isGrantPermission(String perm) {
    	if(Build.VERSION.SDK_INT < 23) return true;

        return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
	}

	@SuppressLint("NewApi")
	private void checkPermission(Handler handler) {

		if(Build.VERSION.SDK_INT < 23) { // 마시멜로우이상이면...
			myLog.i(TAG, "*** Not need checkPermissions ***");
			if(permissionHandler != null) permissionHandler.sendEmptyMessage(0);
		}

		myLog.i(TAG, "*** CheckPermissions ***");

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			perms = new String[]{
					Manifest.permission.READ_PHONE_NUMBERS,
//			Manifest.permission.GET_ACCOUNTS,
					Manifest.permission.CAMERA,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.ACCESS_FINE_LOCATION
			};
		} else {
			perms = new String[]{
					Manifest.permission.READ_PHONE_STATE,
//			Manifest.permission.GET_ACCOUNTS,
					Manifest.permission.CAMERA,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.ACCESS_FINE_LOCATION
			};
		}

		permsMessage = new String[]{
				getResources().getString(R.string.permission_phonenumber_message),
//				getResources().getString(R.string.permission_account_message),
				getResources().getString(R.string.permission_camera_message),
				getResources().getString(R.string.permission_storage_message),
				getResources().getString(R.string.permission_gps_message)
		};

		ArrayList<String> misPerms = new ArrayList<String>();

		permissionHandler = handler;
		StringBuffer sb = new StringBuffer("");

		for(int i=0; i < perms.length; i++){
			if(!isGrantPermission(perms[i])) {
				misPerms.add(perms[i]);

	            // Should we show an explanation?
				if (shouldShowRequestPermissionRationale(perms[i])) {
					if(sb.length()>0) sb.append("\n");
					sb.append(permsMessage[i]);
				}
			}
		}

		// 미허용된 퍼미션들이 있다면..
		if(misPerms.size()>0) {
			String[] mStringArray = new String[misPerms.size()];
			mStringArray = misPerms.toArray(mStringArray);
			requestPermissions(mStringArray, MY_PERMISSION_REQUEST);
		} else {
			myLog.d(TAG, "permissions allow");
			if(permissionHandler != null) permissionHandler.sendEmptyMessage(0);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		boolean isFailGranted = false;

		if(requestCode == MY_PERMISSION_REQUEST) {
			for(int i=0; i < grantResults.length; i++) {
				if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
					myLog.d(TAG, "*** onRequestPermissionsResult: "+permissions[i]+", i="+i);
					isFailGranted = true;
					break;
				}
			}

			if(isFailGranted) {
				if(permissionHandler != null) permissionHandler.sendEmptyMessage(1);
			} else {
				if(permissionHandler != null) permissionHandler.sendEmptyMessage(0);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode) {
			case REQUEST_SETTING_GPS:
				checkSensors();
				break;
			case REQUEST_ENABLE_BT:
				checkSensors();

				break;
		}
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

	private GPSTracker gpsTracker;

	private void initPosition() {

		myLog.d(TAG, "*** initPosition ");

		gpsTracker = new GPSTracker(this, "");

		gpsTracker.requestUpdateGPS();
		updateLocation(gpsTracker.getFastLocation());
	}

	private void updateLocation(Location location) {

		if(gpsTracker!=null) {
			gpsTracker.stopUsingGPS();
		}

//		loadMain();

		if(location!=null) {

			App.setCurrentLocation(location);

			loadMain();
		} else {

		}
	}
}
