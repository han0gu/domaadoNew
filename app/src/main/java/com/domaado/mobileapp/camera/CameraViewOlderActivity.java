package com.domaado.mobileapp.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.domaado.mobileapp.Common;

import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.ImageSurfaceView;
import com.domaado.mobileapp.widget.myLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 *
 */
public class CameraViewOlderActivity extends AppCompatActivity implements OnClickListener {

	private String TAG = "CameraViewOlderActivity";
	
	private Context mContext;
	
	private ImageSurfaceView mImageSurfaceView;
    private Camera camera;

    private FrameLayout cameraPreviewLayout;
    //private ImageView capturedImageHolder;
    
    private ImageView camera_shot_area;
    private int boxWidth, boxHeight;
    
    public static final int frameBottomMargin = 0;
    
    private int bottomMargin;

    private HashMap<String, String> fnames;
    private int fnameSeq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view_older);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mContext = getBaseContext();
        
        myLog.d(TAG, "--- onCreate");

        cameraPreviewLayout = (FrameLayout)findViewById(R.id.cameraFrame);
        //capturedImageHolder = (ImageView)findViewById(R.id.captured_image);
        
        bottomMargin = (int) Common.convertDpToPixel(mContext, frameBottomMargin);

        camera = checkDeviceCamera();

        fnames = new HashMap<>();
        fnameSeq = 0;
        
        if(camera == null) {
        	Common.alertMessage(this,
					getResources().getString(R.string.app_name),
					getResources().getString(R.string.can_not_use_to_camera),
					getResources().getString(R.string.btn_ok),
					new Handler() {

						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
						}
					});
        } else {
	        
	        mImageSurfaceView = new ImageSurfaceView(CameraViewOlderActivity.this, camera, bottomMargin, cameraPreviewLayout);
	        cameraPreviewLayout.addView(mImageSurfaceView);
	        
//	        Camera.Size size = mImageSurfaceView.getCameraSize();
//	        int width = size.width;
//	        int height = size.height;
//	        myLog.d(TAG, "--- mImageSurfaceView width:"+width+", height:"+height);
	        
	        camera_shot_area = (ImageView)findViewById(R.id.camera_shot_area);
	        if(camera_shot_area!=null) {
	        	boxWidth = camera_shot_area.getMeasuredWidth();
	        	boxHeight = camera_shot_area.getMeasuredHeight();
	        } else {
	        	boxWidth = cameraPreviewLayout.getMeasuredWidth();
	        	boxHeight = cameraPreviewLayout.getMeasuredHeight();
	        }
	        
	        myLog.d(TAG, "--- boxWidth:"+boxWidth+", boxHeight:"+boxHeight);
	
	        Button captureButton = (Button)findViewById(R.id.camera_shot_btn);
	        captureButton.setOnClickListener(this);
	        
	        Button camera_cancel_btn = (Button)findViewById(R.id.camera_cancel_btn);
	        camera_cancel_btn.setOnClickListener(this);
	        
	        Button camera_base_btn = (Button)findViewById(R.id.camera_base_btn);
	        camera_base_btn.setOnClickListener(this);
        }
    }
    
    private Camera checkDeviceCamera(){
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
//            mCamera.autoFocus(new AutoFocusCallback() {
//
//				@Override
//				public void onAutoFocus(boolean success, Camera camera) {
//					// TODO Auto-generated method stub
//					
//				}
//            	
//            });
            Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {//对焦成功

                    	camera.takePicture(null, null, pictureCallback);
                    	
//                        camera.takePicture(new Camera.ShutterCallback() {//按下快门
//                            @Override
//                            public void onShutter() { 
//                            	
//                            }
//                        }, new Camera.PictureCallback() {
//                            @Override
//                            public void onPictureTaken(byte[] data, Camera camera) {
//
//                            }
//                        }, pictureCallback);
                    }
                }
            };
            
            mCamera.autoFocus(autoFocusCallback);

        } catch (Exception e) {
            e.printStackTrace();
            
        }
        return mCamera;
    }
    
//    private void alert(String message, final boolean isFinish, final Handler handler, String btnstring) {
//
//		final CustomAlertDialog myDialog = new CustomAlertDialog(this);
//
//		if(TextUtils.isEmpty(btnstring)) btnstring = getResources().getString(R.string.btn_yes);
//
//		myDialog.setMessage(message);
//
//		if(handler != null) {
//			myDialog.setPositiveButton(getResources().getString(R.string.btn_cancel), new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					myDialog.dismiss();
//
//				}
//			});
//
//			myDialog.setNegativeButton(btnstring, new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					myDialog.dismiss();
//
//					handler.sendEmptyMessage(0);
//
//					if(isFinish) finish();
//				}
//			});
//		} else {
//			myDialog.setPositiveButton(getResources().getString(R.string.btn_ok), new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					myDialog.dismiss();
//
//					if(isFinish) finish();
//				}
//			});
//		}
//
//		myDialog.show();
//
//	}

    PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

        	try {
				save(data, new Handler() {

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						super.handleMessage(msg);

						String fname = (String)msg.obj;

						addPhoto(fnameSeq, fname);

						if(fnameSeq>0) savePhoto();
						else {
							fnameSeq++;
						}
					}
					
				});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				Toast.makeText(CameraViewOlderActivity.this, "Captured image is empty", Toast.LENGTH_LONG).show();
				finish();
			}
        	
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            if(bitmap==null){
//                Toast.makeText(CameraViewOlderActivity.this, "Captured image is empty", Toast.LENGTH_LONG).show();
//                return;
//            }
//            resultImage = scaleDownBitmapImage(bitmap, boxWidth, boxHeight);
//            //capturedImageHolder.setImageBitmap(scaleDownBitmapImage(bitmap, boxWidth, boxHeight));
//            
//            savePhoto();
        }
    };

//    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//        return resizedBitmap;
//    }

	private void addPhoto(int fseq, String fname) {
		if(fnames==null) fnames = new HashMap<>();

		fnames.put(String.valueOf(fseq), fname);
	}
    
    private boolean areWeFocused = false;
    private static final int MILLISINFUTURE = 11*1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private CountDownTimer countDownTimer;
    
    public void countDownTimer(){
    	cancelAlarm();
    	
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
            	//
            }
            public void onFinish() {
                // alert!
            	timerAlert();
            }
        };
    }
	
	private void setAlarm() {
		countDownTimer();
        countDownTimer.start();
	}
	
	private void cancelAlarm() {
		 try {
			 countDownTimer.cancel();
		 } catch (Exception e) {}
		 	
		 countDownTimer=null;
	}
    
    private void timerAlert() {
    	Common.alertMessage(this,
				getResources().getString(R.string.app_name),
				getResources().getString(R.string.retry_base_camera),
				getResources().getString(R.string.btn_ok),
				new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				if(msg.what == 0) {
					runBaseCamera();
				}
			}
			
		});
	}
	
	private void toggleShootButton() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Button camera_get_shot_btn = (Button)findViewById(R.id.camera_get_shot_btn);
				
				if(areWeFocused) {
					camera_get_shot_btn.setBackgroundResource(R.drawable.rounded_red_bg_ov);
					camera_get_shot_btn.setEnabled(true);
					cancelAlarm();
				} else {
					camera_get_shot_btn.setBackgroundResource(R.drawable.rounded_gray_bg);
					camera_get_shot_btn.setEnabled(false);
					setAlarm();
				}
			}
		});
	}
    
    private void runBaseCamera() {
		Intent intent = new Intent();
    	intent.putExtra("action", CameraUtil.RETRY_BASE_CAMERA);
    	//intent.putExtra("photo", fname);
    	intent.putExtra("boxinfo", getDotBoxInfo());
    	intent.putExtra("result",  CameraUtil.GET_PHOTO);
    	setResult(RESULT_OK, intent);
    	
    	CameraViewOlderActivity.this.finish();
	}
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.camera_cancel_btn:
			
			finish();
			
			break;
		case R.id.camera_base_btn:
			runBaseCamera();
			break;
		case R.id.camera_shot_btn:
			
			camera.takePicture(null, null, pictureCallback);
			
			break;
		}
	}
	
	private String getDotBoxInfo() {
    	FrameLayout camera_preview = (FrameLayout)findViewById(R.id.cameraFrame);
    	ImageView dotbox = (ImageView)findViewById(R.id.camera_shot_area);
    	myLog.d(TAG, "getDotBoxInfo: view w:"+ camera_preview.getMeasuredWidth()+", h:"+camera_preview.getMeasuredHeight());
    	myLog.d(TAG, "getDotBoxInfo: box w:"+ dotbox.getMeasuredWidth()+", h:"+dotbox.getMeasuredHeight());
    	
    	//int[] locations = new int[2];
    	//dotbox.getLocationInWindow(locations);
    	    	
    	int x = camera_preview.getMeasuredWidth(); //(textureView.getMeasuredWidth() - dotbox.getMeasuredWidth()) / 2;
		int y = camera_preview.getMeasuredHeight(); //(textureView.getMeasuredHeight() - dotbox.getMeasuredHeight()) / 2;
		int ex = dotbox.getMeasuredWidth(); //x + dotbox.getMeasuredWidth();
		int ey = dotbox.getMeasuredHeight(); //y + dotbox.getMeasuredHeight();
		
		//bottomMargin = bottomMargin + getSoftButtonsBarHeight();
		float xx = dotbox.getX();
		float yy = dotbox.getY();
	
		String result = x+","+y+","+ex+","+ey+","+bottomMargin+","+xx+","+yy;
		//String result = x+","+y+","+ex+","+ey+","+bottomMargin;
		//String result = "0,0,"+boxWidth+","+boxHeight+",0";
		
		myLog.d(TAG, "getDotBoxInfo: "+ result);
		
		return result;
    }
	
	@SuppressLint("NewApi")
	private int getSoftButtonsBarHeight() {
	    // getRealMetrics is only available with API 17 and +
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        DisplayMetrics metrics = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(metrics);
	        int usableHeight = metrics.heightPixels;
	        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
	        int realHeight = metrics.heightPixels;
	        if (realHeight > usableHeight)
	            return realHeight - usableHeight;
	        else
	            return 0;
	    }
	    return 0;
	}
	
	private void save(byte[] bytes, Handler handler) throws IOException {

		String fname = Environment.getExternalStorageDirectory()+"/imsi_"+System.currentTimeMillis()+".jpg";

		File file = new File(fname);
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (null != output) {
                output.close();
            }
            
        }
        
        Message msg = new Message();
    	msg.what = 0;
    	msg.obj = fname;
    	
    	myLog.d(TAG, "*** takePicture photo "+bytes.length+"byte saved.");
    	
    	handler.sendMessage(msg);
    }
	
	private void savePhoto(String fname) {
    	myLog.d(TAG, "*** savePhoto: fname: "+fname);

    	Intent intent = new Intent();
    	intent.putExtra("action", CameraUtil.GET_PHOTO_ACTION);
    	intent.putExtra("photo", fname);
    	intent.putExtra("boxinfo", getDotBoxInfo());
    	setResult(Activity.RESULT_OK, intent);
    	
    	finish();
    }

	private void savePhoto() {
		myLog.d(TAG, "*** savePhoto: fnames count: "+fnames.size());

		Intent intent = new Intent();
		intent.putExtra("action", CameraUtil.GET_PHOTO_ACTION);
		intent.putExtra("photo1", fnames.get("0"));
		intent.putExtra("photo2", fnames.get("1"));
		intent.putExtra("boxinfo", getDotBoxInfo());
		setResult(Activity.RESULT_OK, intent);

		finish();
	}
	
	@Override
	public void finish() {
		super.finish();
	}
}
