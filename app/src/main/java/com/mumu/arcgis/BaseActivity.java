package com.mumu.arcgis;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * Manifest.permission. READ_PHONE_STATE
     * Manifest.permission. WRITE_EXTERNAL_STORAGE
     * Manifest.permission.ACCESS_FINE_LOCATION
     * Manifest.permission.ACCESS_COARSE_LOCATION
     * Manifest.permission.CAMERA
     * Manifest.permission.RECORD_AUDIO
     * Manifest.permission.READ_PHONE_STATE
     * Manifest.permission.WRITE_EXTERNAL_STORAGE
     * 权限检查
     */
    public final static int REQ_CAMERA_PMS = 0x001;
    public final static int REQ_RECORD_PMS = 0x002;
    public final static int REQ_lOCATION_PMS = 0x003;
    public final static int REQ_lOCATION_B_PMS = 0x004;
    public final static int REQ_READ_PHONE_STATE_PMS = 0x005;
    public final static int REQ_WRITE_EXTERNAL_STORAGE_PMS = 0x006;

    public boolean permissionIsGet(int reqCode, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, reqCode);
            return false;
        } else {
            return true;
        }
    }

    //并监听用户权限选择返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_RECORD_PMS:
                if (grantResults != null && grantResults.length != 0)
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    }
                break;
            case REQ_CAMERA_PMS:
                if (grantResults != null && grantResults.length != 0)
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    }
                break;
            case REQ_lOCATION_PMS :
            case REQ_lOCATION_B_PMS:
                if (grantResults != null && grantResults.length == 1) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "请开启定位权限,才能正常使用", Toast.LENGTH_LONG).show();
                    } else {
                        permissionLocGeted();
                    }
                }
                break;
            case REQ_READ_PHONE_STATE_PMS:
                if (grantResults != null && grantResults.length > 1)
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    } else {
                        permissionLocGeted();
                    }
                break;
            case REQ_WRITE_EXTERNAL_STORAGE_PMS:
                if (grantResults != null && grantResults.length > 1)
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    } else {
                        permissionLocGeted();
                    }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 定位权限获取到时候的处理
     */
    public void permissionLocGeted() {

    }

}