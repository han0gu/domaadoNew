package com.domaado.mobileapp.widget;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;

import java.io.IOException;
import java.util.List;

/**
 * API 21 이하 카메라 지원용!
 * 
 * @author HongEuiChan
 *
 */
@SuppressWarnings("deprecation")
public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private String TAG = "ImageSufaceView";
	
	private Camera camera;
    private SurfaceHolder surfaceHolder;
    
    List<Size> mSupportedPreviewSizes;
    Size mPreviewSize;

    private int bottomMargin;
    private FrameLayout frame;

    public ImageSurfaceView(Context context, Camera camera, int _bottomMargin, FrameLayout _frame) {
        super(context);
        this.camera = camera;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        this.bottomMargin = _bottomMargin;
        this.frame = _frame;

        mSupportedPreviewSizes = this.camera.getParameters().getSupportedPreviewSizes();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {

        	//this.surfaceHolder.setFixedSize(1280, 960);
        	Parameters parameters = this.camera.getParameters();
        	parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        	parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        	if (parameters.getSupportedFocusModes().contains(
        		    Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
        		parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        		}
        	this.camera.setParameters(parameters);
        	this.camera.setDisplayOrientation(90);
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
        	e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
           //mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height - this.bottomMargin);
           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
        		   getResources().getDisplayMetrics().widthPixels,
        		   getResources().getDisplayMetrics().heightPixels);

        }

        if(mPreviewSize != null && frame != null) {

        	Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            camera.setParameters(parameters);
            camera.startPreview();

            // 90도 로데이션모드니깡~..
        	LayoutParams parms = (LayoutParams) frame.getLayoutParams();
        	parms.width = mPreviewSize.height;
            parms.height = mPreviewSize.width;
            // Set it back.
            frame.setLayoutParams(parms);

        	frame.requestLayout();

        }


    }

    public Size getCameraSize() {
    	return mPreviewSize;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio=(double) w/h;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
    		return;
    	}
	
    	// stop preview before making changes
    	try {
    		camera.stopPreview();
    	} catch (Exception e){
    		// ignore: tried to stop a non-existent preview
    	}

//    	try {
//	    	// set preview size and make any resize, rotate or
//	    	// reformatting changes here
//	    	Parameters parameters= camera.getParameters();
//	    	parameters.setPictureSize(width, height);
//	    	camera.setParameters(parameters);
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	}
    	
    	// start preview with new settings
    	try {
    		camera.setPreviewDisplay(surfaceHolder);
    		camera.startPreview();

    	} catch (Exception e){
    		myLog.d(TAG, "Error starting camera preview: " + e.getMessage());
    	}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.camera.stopPreview();
        this.camera.release();
    }
}
