package com.domaado.mobileapp.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.CustomZoomableImageView;
import com.domaado.mobileapp.widget.myLog;

/**
 *
 */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = CameraActivity.class.getSimpleName();
    private Context mContext;

    //private String CAMERA_ORDER_TYPE;

    private ArrayList<Bitmap> photoPaths;

    private CameraUtil cameraUtil;

    private MyAdapter adapter;

//    private CancelOrderRequest orderRequest;
//    private CallListData callListData;
    private int action;


    private LinearLayout camera_add_buttons;

    private int REQUIRE_PHOTO_COUNT = 6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera);

        mContext = getBaseContext();

        photoPaths = new ArrayList<>();

        cameraUtil = new CameraUtil(this);

        setUI();
    }

    private void setUI() {

        if(getIntent()!=null) {
//            Serializable response = getIntent().getSerializableExtra("order_request");
//            orderRequest = (CancelOrderRequest) response;
//
//            Serializable data = getIntent().getSerializableExtra("calllistdata");
//            callListData = (CallListData) data;

            action = getIntent().getIntExtra("action", Constant.RESULT_SEND_ARRIVEDD_PHOTO);

            //CAMERA_ORDER_TYPE = getIntent().getStringExtra("orderType");
        }

        // 촬영된 데이터를 읽는다.

        // 커스텀 아답타 생성
        GridView gv = (GridView)findViewById(R.id.camera_body_box);

        adapter = new MyAdapter (
                getApplicationContext(),
                R.layout.camera_row,       // GridView 항목의 레이아웃 row.xml
                photoPaths,
                gv.getWidth());    // 데이터

        gv.setAdapter(adapter);  // 커스텀 아답타를 GridView 에 적용

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myLog.d(TAG, "*** position : " + position);

                showImageDetail(position);
            }
        });

        findViewById(R.id.sub_top_back_btn_box).setOnClickListener(this);
        findViewById(R.id.sub_top_right_cmd).setOnClickListener(this);

        //findViewById(R.id.camera_shot_btn).setOnClickListener(this);
        findViewById(R.id.camera_back_btn).setOnClickListener(this);
        findViewById(R.id.camera_complete_btn).setOnClickListener(this);

        findViewById(R.id.sub_top_back_btn_box).setVisibility(View.VISIBLE);

        findViewById(R.id.camera_detail_view_box).setOnClickListener(this);
        findViewById(R.id.camera_detail_view_close_btn).setOnClickListener(this);

        camera_add_buttons = findViewById(R.id.camera_add_buttons);

        // camera_detail_view_title
        // camera_detail_view_image

        initAddButtons();
    }

    private void initAddButtons() {
        findViewById(R.id.camera_add_btn1).setOnClickListener(this);
        findViewById(R.id.camera_add_btn2).setOnClickListener(this);
        findViewById(R.id.camera_add_btn3).setOnClickListener(this);
        findViewById(R.id.camera_add_btn4).setOnClickListener(this);
        findViewById(R.id.camera_add_btn5).setOnClickListener(this);
        findViewById(R.id.camera_add_btn6).setOnClickListener(this);
        findViewById(R.id.camera_add_btn7).setOnClickListener(this);
        findViewById(R.id.camera_add_btn8).setOnClickListener(this);
        findViewById(R.id.camera_add_btn9).setOnClickListener(this);
        findViewById(R.id.camera_add_btn10).setOnClickListener(this);
        findViewById(R.id.camera_add_btn11).setOnClickListener(this);
        findViewById(R.id.camera_add_btn12).setOnClickListener(this);

        findViewById(R.id.camera_cancel_btn).setOnClickListener(this); // 사용하지 않음.

        if(action==Constant.RESULT_SEND_PARKING_PHOTO) {
            // 주차장 사진인 경우 촬영포맷에 따르지 않는다.
            findViewById(R.id.camera_photo_row2).setVisibility(View.GONE);
            findViewById(R.id.camera_photo_row3).setVisibility(View.GONE);
            findViewById(R.id.camera_photo_row4).setVisibility(View.GONE);

            findViewById(R.id.camera_add_btn1_text).setVisibility(View.GONE);
            findViewById(R.id.camera_add_btn2_text).setVisibility(View.GONE);
            findViewById(R.id.camera_add_btn3_text).setVisibility(View.GONE);
        }
    }

    private void updatePartImage(Bitmap bitmap) {
        FrameLayout box = findViewById(targetRes);
        if(box!=null) {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            box.addView(imageView);
        }
    }

    private void showImageDetail(int position) {
        if(photoPaths!=null) {
            (findViewById(R.id.camera_detail_view_box)).setVisibility(View.VISIBLE);

            int count = photoPaths.size();

            Bitmap bitmap = photoPaths.get(position);
            CustomZoomableImageView camera_detail_view_image = findViewById(R.id.camera_detail_view_image);
            camera_detail_view_image.setImageBitmap(bitmap);
            camera_detail_view_image.setAdjustViewBounds(true);
            camera_detail_view_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            camera_detail_view_image.setScaleFactor(1f);

            //camera_detail_view_title
            TextView camera_detail_view_title = findViewById(R.id.camera_detail_view_title);
            camera_detail_view_title.setText(getResources().getString(R.string.camera_detail_view_title, String.valueOf(position+1), String.valueOf(count)));

        }
    }

    private void openCameraShot() {
        if(Build.VERSION.SDK_INT >= 21) {
            //Intent intent = new Intent(this, GetCameraActivity.class);
            //Intent intent = new Intent(this, Camera2BasicFragment.class);
            Intent intent = new Intent(this, CameraKitActivity.class);
            startActivityForResult(intent, CameraUtil.GET_PHOTO);
        } else {
            cameraUtil.doTakePhotoAction();
        }
    }

    private void setResultData() {
        Intent intent = new Intent();
        intent.putExtra("photos", photoPaths.size());
//        intent.putExtra("order_request", orderRequest);
//        intent.putExtra("calllistdata", callListData);
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int targetRes;

    private void setShot(int res) {
        targetRes = res;
        //camera_add_buttons.setVisibility(View.GONE);
        openCameraShot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_shot_btn:
                camera_add_buttons.setVisibility(View.VISIBLE);
                //openCameraShot();
                break;
            case R.id.camera_back_btn:
                finish();
                break;
            case R.id.camera_complete_btn:
                if(adapter!=null && adapter.getCount()>0 && action==Constant.RESULT_SEND_PARKING_PHOTO) {
                    setResultData();
                } else if(adapter!=null && adapter.getCount()>=REQUIRE_PHOTO_COUNT) {
                    setResultData();
                } else if(adapter!=null && adapter.getCount()>0) {
                    Common.alertMessage(
                            this,
                            getResources().getString(R.string.app_name),
                            getResources().getString(R.string.camera_shot_limit, String.format("%,d", REQUIRE_PHOTO_COUNT)),
                            getResources().getString(R.string.btn_ok),
                            new Handler() {

                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                }
                            }
                    );
                } else {
                    Common.alertMessage(
                            this,
                            getResources().getString(R.string.app_name),
                            getResources().getString(R.string.can_not_next_no_more_photo),
                            getResources().getString(R.string.btn_ok),
                            new Handler() {

                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                }
                            }
                    );
                }
                break;
            case R.id.sub_top_back_btn_box:
                finish();
                break;
            case R.id.sub_top_right_cmd:
                break;
            case R.id.camera_detail_view_close_btn:
                (findViewById(R.id.camera_detail_view_box)).setVisibility(View.GONE);
                break;

            case R.id.camera_cancel_btn:
                camera_add_buttons.setVisibility(View.GONE);
                break;

            case R.id.camera_add_btn1:
            case R.id.camera_add_btn2:
            case R.id.camera_add_btn3:
            case R.id.camera_add_btn4:
            case R.id.camera_add_btn5:
            case R.id.camera_add_btn6:
            case R.id.camera_add_btn7:
            case R.id.camera_add_btn8:
            case R.id.camera_add_btn9:
            case R.id.camera_add_btn10:
            case R.id.camera_add_btn11:
            case R.id.camera_add_btn12:
                setShot(view.getId());
                break;
        }
    }







//    public Bitmap resizeImage(Bitmap bitmap, int targetSizeByte) {
//        int height = bitmap.getHeight();
//        int width = bitmap.getWidth();
//
//        Bitmap resized = null;
//
//        while (sizeOf(bitmap) > targetSizeByte) {
//
//            resized = Bitmap.createScaledBitmap(bitmap, (width * targetHeight) / height, targetHeight, true);
//
//            height = resized.getHeight();
//            width = resized.getWidth();
//
//        }
//
//        return resized;
//    }

    private void processImage(String fname, String boxinfo) {

        Bitmap bitmap=null;
        File f= new File(fname);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(fname, options);

        options.inSampleSize = cameraUtil.calculateInSampleSize(options, 400, 400);

        try {

            bitmap = cameraUtil.getResizedBitmap(400, 400, f.getAbsolutePath());
            //bitmap = ImageUtils.getInstant().getCompressedBitmap(fname);
            Bitmap bitmap2 = BitmapFactory.decodeStream(new FileInputStream(f), null, options);

            myLog.d(TAG, "*** bitmap2 image size: "+String.format("%,d KB", cameraUtil.sizeOf(bitmap2)/1024));
            myLog.d(TAG, "*** bitmap image size: "+String.format("%,d KB", cameraUtil.sizeOf(bitmap)/1024));

            // 영역의 X,Y,WIDTH,HEIGHT값 알아내기!
            int bw = 0;
            int bh = 0;
            int width = 0;
            int height = 0;
            int topmargin = 0;
            float xx = 0;
            float yy = 0;

            myLog.d(TAG, "boxinfo=="+boxinfo);

            if(!TextUtils.isEmpty(boxinfo)) {
                String[] bi = boxinfo.split(",");
                if(bi != null && bi.length > 6) {
                    bw = Integer.parseInt(bi[0]);
                    bh = Integer.parseInt(bi[1]);
                    width = Integer.parseInt(bi[2]);
                    height = Integer.parseInt(bi[3]);
                    topmargin = Integer.parseInt(bi[4]);
                    xx = Float.parseFloat(bi[5]);
                    yy = Float.parseFloat(bi[6]);
                }
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(cameraUtil.getDisplayRotation());
            Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //Bitmap cropped = Bitmap.createBitmap(bitmap, x, y, width, height, matrix, true);

            if(cropped != null) {

                float ratio = topmargin > 0 ? (float)bw / (float)cropped.getWidth() : (float)bh / (float)cropped.getHeight();

                float ratioh = topmargin > 0 ? (float)cropped.getWidth()/(float)bw : (float)cropped.getHeight()/(float)bh;

                float mratio = ((float)height*ratio)/(float)bh;

                float rxx = (float)bw / (float)cropped.getWidth();
                float ryy = (float)bh / (float)cropped.getHeight();

//			            	String toastString = topmargin > 0 ? "W "+(float)cropped.getWidth()+" / "+ (float)bw+" = "+ratio : "H "+(float)cropped.getHeight()+" / "+ (float)bh+" = "+ratio;
//			            	Toast.makeText(this, toastString, Toast.LENGTH_SHORT);

                if(ratio <= 0) ratio = 1;

                myLog.d(TAG, "*** bitmap.getWidth():"+cropped.getWidth()+", bitmap.getHeight():"+cropped.getHeight()+", ratio="+ratio);

                Bitmap boximg = cameraUtil.cropCenterBitmap(cropped, ((float)width/ratio), ((float)height/ratio), ((float)topmargin/ratio), ratio, xx/rxx, yy/ryy);

                //myLog.d(TAG, "*** boximg.getWidth():"+boximg.getWidth()+", boximg.getHeight():"+boximg.getHeight());

//                            if(boximg!=null) {
//                                photoPaths.add(boximg);
//                            } else {
//                                photoPaths.add(cropped);
//                            }

                Bitmap resultBitmap = boximg != null ? boximg : cropped;

                //myLog.d(TAG, "*** imgdata: ["+Common.getBase64encodeImage(resultBitmap)+"]");

                myLog.d(TAG, "*** resultBitmap image size: "+String.format("%,d KB", cameraUtil.sizeOf(resultBitmap)/1024));
                myLog.d(TAG, "*** resultBitmap image width x height: "+resultBitmap.getWidth()+" x "+resultBitmap.getHeight());

                // 서버에 이미지 전송!!
                //sendParkingPhoto(resultBitmap);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        myLog.d(TAG, "*** onActivityResult: requestCode("+requestCode+"), resultCode("+resultCode+")");

        int result = 0;
        int action = 0;
        String boxinfo = "";

        if(data != null) {
            result = data.getIntExtra("result", 0);
            action = data.getIntExtra("action", 0);
            boxinfo = data.getStringExtra("boxinfo");
        }
        if(result == CameraUtil.RETRY_DEFAULT_CAMERA || result == CameraUtil.GET_PHOTO) requestCode = CameraUtil.GET_PHOTO;

        switch(requestCode) {

            case CameraUtil.PICK_FROM_CAMERA: {    // 기본카메라로 촬영한 경우!

                processImage(cameraUtil.getmImageCaptureUri().getPath(), boxinfo);

                break;
            }

            case CameraUtil.GET_PHOTO: {
                if(action == CameraUtil.RETRY_DEFAULT_CAMERA) {
                    cameraUtil.doTakePhotoAction();
                } else if(action == CameraUtil.RETRY_BASE_CAMERA) {
                    cameraUtil.doTakeBaseCamera();
                } else if(action == CameraUtil.GET_PHOTO_ACTION) {

                    String fname = data.getExtras().getString("photo");

                    processImage(fname, boxinfo);

                }

                break;
            } // switch

        }
    }
/**
    private void sendParkingPhoto(final Bitmap photo) {
        ParkingPhotoRequest parkingPhotoRequest = new ParkingPhotoRequest(this);
        if(orderRequest!=null) {
            parkingPhotoRequest.setCenterIdx(orderRequest.getCenterIdx());
            parkingPhotoRequest.setDriverIdx(orderRequest.getDriverIdx());
            parkingPhotoRequest.setResponseId(orderRequest.getResponseId());
            parkingPhotoRequest.setOrderType(orderRequest.getOrderType());
            //parkingPhotoRequest.setOrderType(CancelOrderRequest.CANCEL_ORDER_ORDERTYPE_PARKED);

            parkingPhotoRequest.setPhotoLat(currentLocation.getLatitude());
            parkingPhotoRequest.setPhotoLon(currentLocation.getLongitude());
        }

        myLog.d(TAG, "*** sendParkingPhoto(photo data without): "+parkingPhotoRequest.toString());
        if(photo!=null) {
            parkingPhotoRequest.setPhotoData(photo);

            //myLog.d(TAG, "*** "+parkingPhotoRequest.getPhotoData());

//            StringBuffer sb = new StringBuffer("");
//            for(int i=0; i<parkingPhotoRequest.getPhotoData().length(); i++) {
//                sb.append(parkingPhotoRequest.getPhotoData().charAt(i));
//                if(parkingPhotoRequest.getPhotoData().charAt(i) == 10) {
//                    myLog.d(TAG, "*** "+sb.toString());
//                    sb.delete(0, sb.length());
//                    sb.setLength(0);
//                }
//            }
//            if(sb.length()>0) {
//                myLog.d(TAG, "*** "+sb.toString());
//            }
        }

        ParkingPhotoTask parkingPhotoTask = new ParkingPhotoTask(this, parkingPhotoRequest, true, new Handler() {


            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                ParkingPhotoResponse response = (ParkingPhotoResponse)msg.obj;

                switch(msg.what) {
                    case Constant.RESPONSE_SUCCESS: {

                        photoPaths.add(photo);
                        adapter.notifyDataSetChanged();

                        updatePartImage(photo);

                        break;
                    }
                    case Constant.RESPONSE_FAILURE: {

                        String message = response!=null && !TextUtils.isEmpty(response.getMessage()) ? response.getMessage() : "Response error!";

                        Common.alertMessage(
                                CameraActivity.this,
                                getResources().getString(R.string.dialog_error_title),
                                message,
                                getResources().getString(R.string.btn_ok),
                                new Handler() {

                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);

                                    }
                                }
                        );

                        break;
                    }
                    case Constant.RESPONSE_TIMEOUT: {

                        Common.alertMessage(
                                CameraActivity.this,
                                getResources().getString(R.string.dialog_error_title),
                                getResources().getString(R.string.conn_issue_desc2),
                                getResources().getString(R.string.btn_ok),
                                new Handler() {

                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);

                                    }
                                }
                        );

                        break;
                    }
                }
            }
        });

        parkingPhotoTask.execute(getResources().getString(R.string.url_site), getResources().getString(R.string.url_request_photo));
        //parkingPhotoTask.execute("http://app.hongeuichan.com", "/saveImageJSON.php");
    } **/
}

class MyAdapter extends BaseAdapter {
    Context context;
    int layout;
    List<Bitmap> photos;
    LayoutInflater inf;
    int childWidth;

    public MyAdapter(Context context, int layout, List<Bitmap> _photos, int childWidth) {
        this.context = context;
        this.layout = layout;
        this.photos = _photos;
        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        this.childWidth = (childWidth-10)/2;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inf.inflate(layout, null);
        ImageView iv = (ImageView)convertView.findViewById(R.id.camera_row_img);

        ViewGroup.LayoutParams params = iv.getLayoutParams();
        params.width = childWidth;
        iv.setLayoutParams(params);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(photos.get(position), options);

        iv.setImageBitmap(photos.get(position));

        return convertView;
    }


}