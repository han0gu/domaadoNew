package com.domaado.mobileapp.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import com.domaado.mobileapp.BuildConfig;
import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.widget.FileInformation;
import com.domaado.mobileapp.widget.myLog;

/**
 * CAMERA UTILITY
 */
public class CameraUtil {

    public static String TAG = CameraUtil.class.getSimpleName();

    public Activity activity;

    public static final int GET_PHOTO 			    = 1;
    public static final int GET_PHOTO_ACTION 	    = 2;
    public static final int RETRY_DEFAULT_CAMERA    = 3;
    public static final int RETRY_BASE_CAMERA	    = 4;

    public static final int PICK_FROM_CAMERA 	= 1000;
    public static final int PICK_FROM_ALBUM 	= 1001;
    public static final int CROP_FROM_CAMERA 	= 1002;
    public static final int PICK_CROP_CAMERA 	= 1003;
    public static final int PICK_CURATION_DATA  = 1004;
    public static final int PICK_FROM_CAMERA_BASE = 1005;

    public static final int CAMERA_MODULE_RESULT	= 9000;

    // use UCrop SDK!
    public static final int FROM_IMAGE_PICKER               = 2000;
    public static final int FROM_IMAGE_PICKER_WITH_ACTION   = 2100;

    private Uri mImageCaptureUri, mProductPhotoUri;

    public String FILE_PROVIDER_NAME   = BuildConfig.APPLICATION_ID + ".provider";
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
            try {
                mImageCaptureUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_NAME, Common.getStoragePath(activity, FILE_PREFIX_TEMP + url));
                mProductPhotoUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_NAME, Common.getStoragePath(activity, FILE_PREFIX_ENCRYPT + url));
            } catch(Exception e) {
                e.printStackTrace();

                mImageCaptureUri = Uri.fromFile(Common.getStoragePath(activity, FILE_PREFIX_TEMP + url));
                mProductPhotoUri = Uri.fromFile(Common.getStoragePath(activity, FILE_PREFIX_ENCRYPT + url));
            }

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
        activity.startActivityForResult(intent, CAMERA_MODULE_RESULT);

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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        //intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, CameraUtil.PICK_FROM_CAMERA_BASE);
    }

    /**
     * Result: WebContentActivity.CAMERA_MODULE_RESULT
     *
     * @param activity
     * @return
     */
    public Intent getTakePhotoAction(Activity activity) {

        Intent intent = new Intent(activity, CameraViewOlderActivity.class);
        return intent;

    }

    /**
     * Result: CameraUtil.PICK_FROM_CAMERA_BASE
     *
     * @return
     */
    public Intent getTakeBaseCamera() {

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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        //intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);

        return intent;
    }

    public Uri getmImageCaptureUri() {
        if(mImageCaptureUri==null) {
            initDefaultPath();
        }
        return mImageCaptureUri;
    }

    public Uri getmProductPhotoUri() {
        if(mProductPhotoUri==null) {
            initDefaultPath();
        }
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

    public Bitmap getBitmapRotate(String photoPath, int reqWidth, int reqHeight) {
        Bitmap rotatedBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return rotatedBitmap;
    }

    public int getImageRotate(String photoPath) {
        int rotate = 0;

        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 0;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 270;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        myLog.d(TAG, "*** getImageRotate: "+rotate);

        return rotate;
    }

    public void rotateBitmapFile(String photoPath) {
        FileOutputStream out = null;

        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

            bitmap = rotateImage(bitmap, getImageRotate(photoPath));

            File file = new File(photoPath);
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out!=null) out.close();
            } catch(Throwable ignore) {}
        }
    }

    public Bitmap rotateImage(Bitmap source, float angle) {
        myLog.d(TAG, "*** rotateImage: angel: "+angle);

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
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
        if(data==null) return 0;

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

    public String SaveBitmapToFile(Bitmap image, String filename) {
        OutputStream outStream = null;
        String resultPath = "";

        if (image == null) {
            myLog.e(TAG, "*** SaveBitmapToFile - is not set Bitmap image!..");
            return resultPath;
        }

        try {
            myLog.e(TAG, "*** SaveBitmapToFile path: "+filename);
            // bitmap객체를 파일로 저장
            //Bitmap bitmap = rotateImage(image, getDisplayRotation());

            outStream = new FileOutputStream(new File(filename));
            if (image.compress(Bitmap.CompressFormat.JPEG, 100, outStream)) {
                resultPath = filename;

                //rotateBitmapFile(resultPath);
            }
            outStream.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outStream.close();
                outStream = null;
            } catch (Throwable t) {
            }
        }

        return resultPath;
    }

    public static Bitmap getBitmapFromPath(Context ctx, String filename) {
        Bitmap bitmap = null;

        File file = new File(filename);

        if (file.isFile() && file.exists()) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return bitmap;
    }

    public void launchCameraIntent(int resultCode) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, false);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        activity.startActivityForResult(intent, resultCode);
    }

    public void launchCameraIntent(ActivityResultLauncher<Intent> resultLauncher) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, false);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        resultLauncher.launch(intent);
    }

    public void launchGalleryIntent(int resultCode) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, false);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        activity.startActivityForResult(intent, resultCode);
    }

    public void launchGalleryIntent(ActivityResultLauncher<Intent> resultLauncher, int option) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, option);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, false);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        resultLauncher.launch(intent);
    }

    public static Uri getFileFullname(Context context, String filename) {
//        String filfullename = String.format(Locale.getDefault(), "%s_%d.%s", FILE_PREFIX_TEMP, System.currentTimeMillis(), FILE_EXTENSTION);
        File file = new File(context.getCacheDir(), filename);

        Uri uri = Uri.fromFile(file);

        return uri;
    }

    public static boolean savePic(Bitmap b, String strFileName) {

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                myLog.i(TAG, "*** Capture image save successful.");
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMimeType(Context context, Uri uri) {
        String type = "";
        try {
            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getExtensionFromMimeType(cR.getType(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }

    /**
     * Uri 파일을 targetFile로 저장한다.
     *
     * @param context
     * @param uri
     * @param targetFile
     */
    public static String copyfile(Context context, Uri uri, String targetFile) {
        //String sourceFilename = FileInformation.getPath(context, sourceuri); //sourceuri.getPath();
        String sourceFilename = FileInformation.getPath(context, uri);

        String destinationFilename = Common.getStoragePathString(context) + File.separator + targetFile;
        //android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+File.separatorChar+targetFile;

        File file = new File(sourceFilename);
        //Uri myUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);

        myLog.d(TAG, "*** copyFile: copy "+sourceFilename+" to "+destinationFilename);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        String result = "";

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
            result = e.getLocalizedMessage();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff000000;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = roundPixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap addWatermark(Bitmap source, Bitmap watermark, float ratio) {
        Canvas canvas;
        Paint paint;
        Bitmap bmp;
        Matrix matrix;
        RectF r;

        int width, height;
        float scale;

        try {

            width = source.getWidth();
            height = source.getHeight();

            // Create the new bitmap
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);

            // Copy the original bitmap into the new one
            canvas = new Canvas(bmp);
            canvas.drawBitmap(source, 0, 0, paint);

            // Scale the watermark to be approximately to the ratio given of the source image height
            scale = (float) (((float) height * ratio) / (float) watermark.getHeight());

            // Create the matrix
            matrix = new Matrix();
            matrix.postScale(scale, scale);

            // Determine the post-scaled size of the watermark
            r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
            matrix.mapRect(r);

            // Move the watermark to the bottom right corner
            //matrix.postTranslate(width - r.width(), height - r.height());
            matrix.postTranslate((width / 2) - (r.width() / 2), (height / 2) - (r.height() / 2));

            // Draw the watermark
            canvas.drawBitmap(watermark, matrix, paint);
        } catch(Exception e) {
            e.printStackTrace();

            bmp = source;
        }

        return bmp;
    }

    public static Bitmap addBlurEffect(Context context, Bitmap image, float radius) {
        float BITMAP_SCALE = 0.6f;
//        float BLUR_RADIUS = 15f;

        Bitmap outputBitmap;

        try {

            int width = Math.round(image.getWidth() * BITMAP_SCALE);
            int height = Math.round(image.getHeight() * BITMAP_SCALE);

            Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
            outputBitmap = Bitmap.createBitmap(inputBitmap);

            RenderScript rs = RenderScript.create(context);

            ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, android.renderscript.Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

            intrinsicBlur.setRadius(radius);
            intrinsicBlur.setInput(tmpIn);
            intrinsicBlur.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            outputBitmap = image;
        }

        return outputBitmap;
    }
}
