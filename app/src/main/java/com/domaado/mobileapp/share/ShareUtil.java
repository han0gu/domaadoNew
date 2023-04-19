package com.domaado.mobileapp.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.domaado.mobileapp.widget.myLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by kbins(James Hong) on 2020,January,17
 */
public class ShareUtil {

    private String TAG = ShareUtil.class.getSimpleName();
    private Context context;

    public final static int SHARE_BAND_PKG 			= 6;
    public final static int SHARE_FACEBOOK_PKG 		= 7;
    public final static int SHARE_KSTORY_PKG 		= 8;
    public final static int SHARE_KTALK_PKG 		= 9;
    public final static int SHARE_LINE_PKG 			= 10;
    public final static int SHARE_PINTEREST_PKG 	= 11;
    public final static int SHARE_INSTAGRAM_PKG		= 12;
    public final static int SHARE_TWITTER_PKG		= 13;
    public final static int SHARE_TELEGRAM_PKG		= 14;
    public final static int SHARE_FBMESSENGER_PKG 	= 15;
    public final static int SHARE_EMAIL_PKG			= 16;

    public String[] shareAppPkgName = { "", "", "", "", "", "",
            "com.nhn.android.band",
            "com.facebook.katana",
            "com.kakao.story",
            "com.kakao.talk",
            "jp.naver.line.android",
            "com.pinterest",
            "com.instagram.android",
            "com.twitter.android",
            "org.telegram.messenger",
            "com.facebook.orca",
            "email"
    };

    public ShareUtil(Context context) {
        this.context = context;
    }

    public String getNameFromPackageName(String pkgname) {

        PackageManager packageManagers= context.getPackageManager();
        String appName = "unknown";

        try {
            appName = (String) packageManagers.getApplicationLabel(packageManagers.getApplicationInfo(pkgname, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appName;
    }

    public Drawable getIconfromPackageName(String pkgname) {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(pkgname);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return icon;
    }

    public int getApplicationID(String pkgname) {
        int seq = -1;

        if(TextUtils.isEmpty(pkgname)) return seq;

        for(int i=0; i < shareAppPkgName.length; i++) {
            if(pkgname.equals(shareAppPkgName[i]) && !"".equals(shareAppPkgName[i])) {
                seq = i;
                break;
            }
        }

        return seq;
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public Bitmap drawableToBitmap(PictureDrawable pd) {
        Bitmap bm = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        canvas.drawPicture(pd.getPicture());
        return bm;
    }

    public File getFileFromDrawable(int res, File dir, String filename) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), res);

        if(saveBitmapToFile(dir, filename, bm, Bitmap.CompressFormat.PNG, 100)) {
            return new File(dir, filename);
        } else {
            return null;
        }
    }

    public boolean saveBitmapToFile(File dir, String fileName, Bitmap bm, Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir,fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format,quality,fos);

            fos.close();

            return true;
        }
        catch (IOException e) {
            myLog.e(TAG, e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public void shareAppDl(String pkgName) {
        Uri uri = Uri.parse("market://details?id="+pkgName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public boolean appInstalled(String uri) {
        PackageManager pm = context.getPackageManager();

        try{
            pm.getPackageInfo(uri, 0); //PackageManager.GET_ACTIVITIES);
            return true;
        }catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public boolean isPackageInstalled(String packagename) {

        PackageManager packageManager = context.getPackageManager();

        try {
            return packageManager.getApplicationInfo(packagename, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public boolean sendTextToLine(String lineComment) {
        boolean ret = false;
        try {
            String lineString = "line://msg/text/" + lineComment;
            Intent intent = Intent.parseUri(lineString, Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean sendImageToLine(String localFileName) {
        boolean ret = false;
        try {
            String lineString = "line://msg/image/" + localFileName;
            Intent intent = Intent.parseUri(lineString, Intent.URI_INTENT_SCHEME);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getDefaultEmailPkg() {
        String result = "";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        PackageManager pkgManager = context.getPackageManager();
        List<ResolveInfo> activities = pkgManager.queryIntentActivities(intent, 0);

        for(ResolveInfo info : activities) {
            myLog.d(TAG, "-- getDefaultEmailPkg: "+info.activityInfo.packageName);
        }

        for(ResolveInfo info : activities) {
            if(!TextUtils.isEmpty(info.activityInfo.packageName) && info.activityInfo.packageName.contains("email")) {
                result = info.activityInfo.packageName;
                break;
            }
        }

        return result;
    }
}
