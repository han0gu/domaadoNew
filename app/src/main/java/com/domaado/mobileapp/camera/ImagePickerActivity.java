package com.domaado.mobileapp.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.domaado.mobileapp.BuildConfig;
import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.widget.ImagePicker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.myLog;

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = ImagePickerActivity.class.getSimpleName();
    public static final String INTENT_IMAGE_PICKER_OPTION = "image_picker_option";
    public static final String INTENT_ASPECT_RATIO_X = "aspect_ratio_x";
    public static final String INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y";
    public static final String INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio";
    public static final String INTENT_IMAGE_COMPRESSION_QUALITY = "compression_quality";
    public static final String INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT = "set_bitmap_max_width_height";
    public static final String INTENT_BITMAP_MAX_WIDTH = "max_width";
    public static final String INTENT_BITMAP_MAX_HEIGHT = "max_height";


    public static final int REQUEST_IMAGE_CAPTURE = 0;
    public static final int REQUEST_GALLERY_IMAGE = 1;
    public static final int REQUEST_SELECT_METHOD = 2;

    private boolean lockAspectRatio = false, setBitmapMaxWidthHeight = false;
    private int ASPECT_RATIO_X = 16, ASPECT_RATIO_Y = 9, bitmapMaxWidth = 1000, bitmapMaxHeight = 1000;
    private int IMAGE_COMPRESSION = 80;
    public static String fileName;

    CameraUtil cameraUtil;

    public ActivityResultLauncher<Intent> ActionImageCaptureResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();

            if(data!=null && data.getData()!=null) {
                Uri imageUri = data.getData();
                cropImage(imageUri);
            } else {
                cropImage(getCacheImagePath(fileName));
            }
        } else {
            setResultCancelled();
        }

    });

    public interface PickerOptionListener {
        void onTakeCameraSelected();

        void onChooseGallerySelected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_picker);

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } catch (IllegalStateException ignore) {
            ignore.printStackTrace();
        }

        WindowManager.LayoutParams lpWindow = getWindow().getAttributes();
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_image_intent_null), Toast.LENGTH_SHORT).show();
            return;
        }

        ASPECT_RATIO_X = intent.getIntExtra(INTENT_ASPECT_RATIO_X, ASPECT_RATIO_X);
        ASPECT_RATIO_Y = intent.getIntExtra(INTENT_ASPECT_RATIO_Y, ASPECT_RATIO_Y);
        IMAGE_COMPRESSION = intent.getIntExtra(INTENT_IMAGE_COMPRESSION_QUALITY, IMAGE_COMPRESSION);
        lockAspectRatio = intent.getBooleanExtra(INTENT_LOCK_ASPECT_RATIO, false);
        setBitmapMaxWidthHeight = intent.getBooleanExtra(INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, false);
        bitmapMaxWidth = intent.getIntExtra(INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth);
        bitmapMaxHeight = intent.getIntExtra(INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight);

        cameraUtil = new CameraUtil(this);

        int requestCode = intent.getIntExtra(INTENT_IMAGE_PICKER_OPTION, -1);
        switch(requestCode) {
            case REQUEST_SELECT_METHOD:
                selectPickerMethod();
                break;
            case REQUEST_IMAGE_CAPTURE:
                takeCameraImage();
                break;
            case REQUEST_GALLERY_IMAGE:
            default:
                chooseImageFromGallery();
                break;
        }
//        if (requestCode == REQUEST_IMAGE_CAPTURE) {
//            takeCameraImage();
//        } else {
//            chooseImageFromGallery();
//        }
    }

    private List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            myLog.d(TAG, "*** Intent: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }

    public static void showImagePickerOptions(Context context, PickerOptionListener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.pick_image_intent_text));

        // add a list
        String[] animals = {context.getString(R.string.lbl_take_camera_picture), context.getString(R.string.lbl_choose_from_gallery)};
        builder.setItems(animals, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onTakeCameraSelected();
                    break;
                case 1:
                    listener.onChooseGallerySelected();
                    break;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public Intent getPickImageIntent() {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);

        fileName = System.currentTimeMillis() + ".jpg";

        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName));
        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intentList = addIntentsToList(this, intentList, pickIntent);
        intentList = addIntentsToList(this, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), getResources().getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    public void selectPickerMethod() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent takePictureIntent = getPickImageIntent();
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                                ActionImageCaptureResult.launch(takePictureIntent);
                                startActivityForResult(takePictureIntent, REQUEST_SELECT_METHOD);
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void takeCameraImage() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                            fileName = System.currentTimeMillis() + ".jpg";
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName));
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                                      ActionImageCaptureResult.launch(takePictureIntent);
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
//                                }
//                            });
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void chooseImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    cropImage(getCacheImagePath(fileName));
                } else {
                    setResultCancelled();
                }
                break;
            case REQUEST_GALLERY_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    cropImage(imageUri);
                } else {
                    setResultCancelled();
                }
                break;
            case REQUEST_SELECT_METHOD:
                if (resultCode == RESULT_OK) {
                    if(data!=null && data.getData()!=null) {
                        Uri imageUri = data.getData();
                        cropImage(imageUri);
                    } else {
                        cropImage(getCacheImagePath(fileName));
                    }
                } else {
                    setResultCancelled();
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    handleUCropResult(data);
                } else {
                    setResultCancelled();
                }
                break;
            case UCrop.RESULT_ERROR:
                final Throwable cropError = UCrop.getError(data);
                myLog.e(TAG, "*** Crop error: " + cropError);
                setResultCancelled();
                break;
            default:
                setResultCancelled();
        }
    }

    private void cropImage(Uri sourceUri) {

        String filename = queryName(getContentResolver(), sourceUri);

        myLog.e(TAG, "*** cropImage: "+filename);

        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), filename));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(IMAGE_COMPRESSION);

        // applying UI theme
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorWhite));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));

        if (lockAspectRatio)
            options.withAspectRatio(ASPECT_RATIO_X, ASPECT_RATIO_Y);

        if (setBitmapMaxWidthHeight)
            options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight);

        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(this);
    }

    private void handleUCropResult(Intent data) {
        myLog.e(TAG, "*** handleUCropResult");

        if (data == null) {
            setResultCancelled();
            return;
        }
        final Uri resultUri = UCrop.getOutput(data);
        setResultOk(resultUri);
    }

    private void setResultOk(Uri imagePath) {
        myLog.e(TAG, "*** setResultOk");

        Intent intent = new Intent();
        intent.putExtra("path", imagePath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void setResultCancelled() {
        myLog.e(TAG, "*** setResultCancelled");

        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private Uri getCacheImagePath(String fileName) {
        Uri uri = cameraUtil.getmImageCaptureUri();

        myLog.d(TAG, "*** getCacheImagePath: "+ cameraUtil.FILE_PROVIDER_NAME);

        return uri;
    }

    public static String queryName(ContentResolver resolver, Uri uri) {
        if(uri!=null) {
            Cursor returnCursor =
                    resolver.query(uri, null, null, null, null);
            if(returnCursor!=null) {
//                assert returnCursor != null;
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String name = returnCursor.getString(nameIndex);
                returnCursor.close();
                return name;
            }
        }

        return "unknown";
    }

    /**
     * Calling this will delete the images from cache directory
     * useful to clear some memory
     */
    public static void clearCache(Context context) {
        File path = new File(context.getExternalCacheDir(), "camera");
        if (path.exists() && path.isDirectory()) {
            for (File child : path.listFiles()) {
                child.delete();
            }
        }
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        if(Build.VERSION_CODES.KITKAT >= Build.VERSION.SDK_INT) {
            return getRealPathFromURI_API19(context, uri);
        } else {
            return getRealPathFromURI_Over(context, uri);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        Cursor cursor = null;

        try {
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return filePath;
    }


    public static String getRealPathFromURI_Over(Context context, Uri contentUri) {
        String path = null;

        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndex(proj[0]);
            path = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

}
