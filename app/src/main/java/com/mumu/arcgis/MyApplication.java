package com.mumu.arcgis;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import lib.util.FileUtil;

/**
 * Created by mags on 2016/10/26.
 */

public class MyApplication extends MultiDexApplication {
    public static MyApplication myApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        MyCrashHandler handler = MyCrashHandler.getInstance();
        handler.init(this);
        new Thread() {
            public void run() {
            try {
                String[] files = MyApplication.this.getAssets().list("arcgis");
                for(String fileName:files){
                    Log.e("file",fileName+"");
                    File file = FileUtil.getFile(FileUtil.getDataCacheDir(FileUtil.Patch),fileName);
                    if(!file.exists()){
                        Log.e("file",fileName+"_create");
                        InputStream inputStream = MyApplication.this.getAssets().open("arcgis/"+fileName);
                        file.createNewFile();
                        boolean isSuccess = FileUtil.saveFileFromIS(inputStream, file);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        }.start();
    }
}
