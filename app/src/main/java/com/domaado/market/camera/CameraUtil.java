package com.domaado.market.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.domaado.market.Common;
import com.domaado.market.widget.myLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * CAMERA UTILITY
 */
public class CameraUtil {

    public String TAG = CameraUtil.class.getSimpleName();

    public Activity activity;

    public static final int GET_PHOTO 			= 1;
    public static final int GET_PHOTO_ACTION 	= 2;
    public static final int RETRY_DEFAULT_CAMERA = 3;
    public static final int RETRY_BASE_CAMERA	= 4;

    public static final int PICK_FROM_CAMERA 	= 1000;
    public static final int PICK_FROM_ALBUM 	= 1001;
    public static final int CROP_FROM_CAMERA 	= 1002;
    public static final int PICK_CROP_CAMERA 	= 1003;
    public static final int PICK_CURATION_DATA  = 1004;
    public static final int PICK_FROM_CAMERA_BASE = 1005;

    private Uri mImageCaptureUri, mProductPhotoUri;

    private String FILE_PROVIDER_NAME   = "com.quickdriver.provider";
    private String FILE_PREFIX_TEMP     = "tmp_";
    private String FILE_PREFIX_ENCRYPT  = "IMG_";

    private String FILE_EXTENSTION      = ".jpg";

    public HashMap<String, String> photos;

    public CameraUtil(Activity activity) {
        this.activity = activity;

        initDefaultPath();

        photos = new HashMap<>();
    }

    public void initDefaultPath() {
        // 임시로 사용할 파일의 경로를 생성
        String url = String.valueOf(System.currentTimeMillis()) + FILE_EXTENSTION;

        // 누가이상인경우!
        if (Build.VERSION.SDK_INT >= 24) {
            mImageCaptureUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_NAME, Common.getStoragePath(activity, FILE_PREFIX_TEMP + url));
            mProductPhotoUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_NAME, Common.getStoragePath(activity, FILE_PREFIX_ENCRYPT + url));

        } else {
            mImageCaptureUri = Uri.fromFile(Common.getStoragePath(activity, FILE_PREFIX_TEMP + url));
            mProductPhotoUri = Uri.fromFile(Common.getStoragePath(activity, FILE_PREFIX_ENCRYPT + url));

        }
    }

    public void updateFileName() {
        initDefaultPath();
    }

    public HashMap<String, String> getPhotos() {
        if(photos == null) photos = new HashMap<>();
        return photos;
    }

    public String getPhoto(String key) {
        if(this.photos==null) this.photos = new HashMap<>();

        return this.photos.get(key);
    }

    public void setPhotos(HashMap<String, String> photos) {
        this.photos = photos;
    }

    public void addPhoto(String key, String value) {
        if(this.photos==null) this.photos = new HashMap<>();

        this.photos.put(key, value);
    }

    public void doTakePhotoAction() {

        Intent intent = new Intent(activity, CameraViewOlderActivity.class);
        activity.startActivityForResult(intent, CameraUtil.GET_PHOTO);

    }

    public void doTakeBaseCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(Build.VERSION.SDK_INT >= 24) {
            activity.grantUriPermission("com.android.camera",
                    mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.grantUriPermission("com.android.camera",
                    mProductPhotoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

//            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//            for (ResolveInfo resolveInfo : resInfoList) {
//                String packageName = resolveInfo.activityInfo.packageName;
//                grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                grantUriPermission(packageName, mProductPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        //intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, CameraUtil.PICK_FROM_CAMERA_BASE);
    }

    public Uri getmImageCaptureUri() {
        return mImageCaptureUri;
    }

    public Uri getmProductPhotoUri() {
        return mProductPhotoUri;
    }

    @SuppressLint("NewApi")
    public int getSoftButtonsBarHeight() {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    public Bitmap cropCenterBitmap(Bitmap src, float w, float h, float topmargin, float ratio, float xx, float yy) {
        if(src == null || w <= 0 || h <= 0)
            return null;

        Display display = activity.getWindowManager().getDefaultDisplay();
        String displayName = display.getName();  // minSdkVersion=17+
        myLog.i(TAG, "displayName  = " + displayName);

        // display size in pixels
        Point size = new Point();
        display.getSize(size);
        int width1 = size.x;
        int height1 = size.y;
        myLog.i(TAG, "width1        = " + width1);
        myLog.i(TAG, "height1       = " + height1);

        int softButtonHeight = getSoftButtonsBarHeight();

        float width = (float)src.getWidth();
        float height = (float)src.getHeight() + ((float)softButtonHeight / ratio);

        if(width < w && height < h)
            return src;

        float x = 0;
        float y = 0;

        if(width > w) {
            x = (width - w)/2;
        }

        // topmargin = y : 직접 지정됨(센터가 아님.)
        if(height > h) {
            y = (height - h)/2;
        }

        float cw = w; // crop width
        float ch = h; // crop height

        if(w > width)
            cw = width;

        if(h > height)
            ch = height;
        if(yy>ch) ch = height;

        if(ratio >= 1) {
            xx = x;
            //yy = y;
        }

        myLog.d(TAG, "*** cropCenterBitmap: src.width="+src.getWidth()+", src.height="+src.getHeight());
        myLog.d(TAG, "*** cropCenterBitmap: xx="+xx+", yy="+yy);
        myLog.d(TAG, "*** cropCenterBitmap: w="+w+", h="+h);
        myLog.d(TAG, "*** cropCenterBitmap: cw="+cw+", ch="+ch);

        Bitmap bitmap = src;
        try {
            bitmap = Bitmap.createBitmap(src, (int)xx, (int)yy, (int)cw, (int)ch);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public int getDisplayRotation() {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 90;
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
        }
        return 0;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            myLog.d(TAG, "*** calculateInSampleSize inSampleSize: "+inSampleSize+", reqWidth x reqHeight:"+reqWidth+" x "+reqHeight);

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;

                myLog.d(TAG, "*** calculateInSampleSize inSampleSize: "+inSampleSize+", halfWidth x halfHeight:"+halfWidth+" x "+halfHeight);
            }
        }

        myLog.d(TAG, "*** calculateInSampleSize complete inSampleSize: "+inSampleSize);

        return inSampleSize;
    }

    private Bitmap getBitmap(int path, Canvas canvas) {

        Resources resource = null;
        try {
            final int IMAGE_MAX_SIZE = 1000000; // 1.2MP
            resource = activity.getResources();

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resource, path, options);

            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            myLog.d(TAG, "scale = " + scale + ", orig-width: " + options.outWidth + ", orig-height: " + options.outHeight);

            Bitmap pic = null;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                pic = BitmapFactory.decodeResource(resource, path, options);

                // resize to desired dimensions
                int height = canvas.getHeight();
                int width = canvas.getWidth();
                myLog.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(pic, (int) x, (int) y, true);
                pic.recycle();
                pic = scaledBitmap;

                System.gc();
            } else {
                pic = BitmapFactory.decodeResource(resource, path);
            }

            myLog.d(TAG, "bitmap size - width: " +pic.getWidth() + ", height: " + pic.getHeight());
            return pic;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Bitmap getResizedBitmap(int targetW, int targetH, String imagePath) throws FileNotFoundException {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //inJustDecodeBounds = true <-- will not load the bitmap into memory
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        return(bitmap);
    }

    public int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

    public void removeFile(Context context, Uri uri) throws IOException {
        File file = new File(uri.getPath());
        file.delete();
        if(file.exists()){
            file.getCanonicalFile().delete();
            if(file.exists()){
                context.getApplicationContext().deleteFile(file.getName());
            }
        }
    }
}
