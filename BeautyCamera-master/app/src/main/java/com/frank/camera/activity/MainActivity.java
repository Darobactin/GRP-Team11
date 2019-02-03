package com.frank.camera.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.frank.camera.manager.PermissionManager;
import com.frank.camera.R;

public class MainActivity extends BaseActivity {

    private PermissionManager mPermissionManager;
    // permissions we need <要校验的权限>
    private final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    // 识别请求码
    private final int REQUEST_CODE_DETECTION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // initialize the application layout <初始化界面>
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(R.layout.activity_main);

        // initialize the permission manager <初始化动态权限校验器>
        mPermissionManager = new PermissionManager(this) {

            @Override
            public void authorized(int requestCode) {
                // 权限通过
                switch (requestCode) {
                    case REQUEST_CODE_DETECTION:
                        startActivity(new Intent(MainActivity.this, FaceBeautyActivity.class));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void noAuthorization(int requestCode, String[] lacksPermissions) {
                // 缺少必要权限
                showPermissionDialog();
            }

            @Override
            public void ignore(int requestCode) {
                // Android 6.0 以下系统不校验
                authorized(requestCode);
            }
        };
    }

    /**
     * 复查权限
     *
     * @param requestCode  requestCode
     * @param permissions  permissions
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 用户做出选择以后复查权限，判断是否通过了权限申请
        mPermissionManager.recheckPermissions(requestCode, permissions, grantResults);
    }

    /**
     * 目标检测
     *
     * @param view view
     */
    public void onDetecting(View view) {
        // 检查权限
        mPermissionManager.checkPermissions(REQUEST_CODE_DETECTION, PERMISSIONS);
    }

    public void onPhotoSelecting(View view) {
        startActivity(new Intent(MainActivity.this, PhotoSelectActivity.class));
    }
}
