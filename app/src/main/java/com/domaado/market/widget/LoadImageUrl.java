package com.domaado.market.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.domaado.market.Common;
import com.domaado.market.R;


/**
 * TODO: URL image를 로딩하여 지정된 뷰에 올린다.
 */
public class LoadImageUrl {

    public String TAG = LoadImageUrl.class.getSimpleName();

    public static final int RESULT_BITMAP_IMAGE = 10;

    public Context mContext;

    private ProgressBar imageProgressBar;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private CustomToast toast;

    private Handler resultHandler;

    public static LoadImageUrl loadImageUrl;

    public LoadImageUrl(Context ctx) {
        this.mContext = ctx;

        this.toast = new CustomToast(mContext);
    }

    public static LoadImageUrl getInstance(Context ctx) {
        if(loadImageUrl==null) loadImageUrl = new LoadImageUrl(ctx);

        return loadImageUrl;
    }

    public void load(String url, Handler handler) {
        try {
            load(url, null, null, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(String url, ProgressBar progressBar, Handler handler) {
        try {
            load(url, null, progressBar, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(String url, ImageView view, ProgressBar imageProgressBar, Handler handler) throws Exception {

        this.imageProgressBar = imageProgressBar;
        this.resultHandler = handler;

        if(view==null) {
            view = new ImageView(mContext);
        }

        if(imageLoader==null) {
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_launcher)
                    .showImageOnFail(R.drawable.ic_launcher)
                    .resetViewBeforeLoading()
                    .cacheOnDisc()
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();
        }

        if(Common.isValidUrl(url))
            loadImage(view, url, imageProgressBar);
        else
            myLog.e(TAG, "*** Invalid url error: "+url);

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (resultHandler != null) resultHandler.sendEmptyMessage(0);
//            }
//        });

    }

    private void loadImage(ImageView imageView, String loadURL, final ProgressBar progress) {
        myLog.d(TAG, "*** URL:"+loadURL);

        imageLoader.displayImage(loadURL, imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(progress!=null) progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Input/Output error";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case DECODING_ERROR:
                        message = "Unsupported URI scheme";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }
                toast.showToast(mContext, message, Toast.LENGTH_SHORT);
            }

            @Override
            public void onLoadingComplete(final String imageUri, View view, final Bitmap loadedImage) {
                if(progress!=null) progress.setVisibility(View.GONE);

                if (resultHandler != null) {
                    Message msg = new Message();
                    msg.what = RESULT_BITMAP_IMAGE;
                    msg.obj = loadedImage;

                    resultHandler.sendMessage(msg);
                }
            }
        });
    }
}
