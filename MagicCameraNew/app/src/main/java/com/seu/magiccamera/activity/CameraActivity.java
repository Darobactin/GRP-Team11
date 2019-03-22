package com.seu.magiccamera.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.seu.magiccamera.R;
import com.seu.magiccamera.adapter.FilterAdapter;
import com.seu.magicfilter.MagicEngine;
import com.seu.magicfilter.filter.helper.MagicFilterType;
import com.seu.magicfilter.utils.MagicParams;
import com.seu.magicfilter.widget.MagicCameraView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by why8222 on 2016/3/17.
 */
public class CameraActivity extends Activity{
    private LinearLayout mFilterLayout;
    private RecyclerView mFilterListView;
    private FilterAdapter mAdapter;
    private MagicEngine magicEngine;
    private boolean isRecording = false;
    private final int MODE_PIC = 1;
    private final int MODE_VIDEO = 2;
    private int mode = MODE_PIC;

    private static final String TAG = "CameraActivity";
    private static final int WRITE_EXTERNAL_STORAGE_PERM = 130;

    private ImageView btn_shutter;
    private ImageView btn_mode;

    private ObjectAnimator animator;

    private final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.CATEAR,
            MagicFilterType.RABBITEAR,
            MagicFilterType.ANIMALEAR,
            MagicFilterType.FAIRYTALE,
            MagicFilterType.SUNRISE,
            MagicFilterType.SUNSET,
            MagicFilterType.WHITECAT,
            MagicFilterType.BLACKCAT,
            MagicFilterType.SKINWHITEN,
            MagicFilterType.HEALTHY,
            MagicFilterType.SWEETS,
            MagicFilterType.ROMANCE,
            MagicFilterType.SAKURA,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.NOSTALGIA,
            MagicFilterType.CALM,
            MagicFilterType.LATTE,
            MagicFilterType.TENDER,
            MagicFilterType.COOL,
            MagicFilterType.EMERALD,
            MagicFilterType.EVERGREEN,
            MagicFilterType.CRAYON,
            MagicFilterType.SKETCH,
            MagicFilterType.AMARO,
            MagicFilterType.BRANNAN,
            MagicFilterType.BROOKLYN,
            MagicFilterType.EARLYBIRD,
            MagicFilterType.FREUD,
            MagicFilterType.HEFE,
            MagicFilterType.HUDSON,
            MagicFilterType.INKWELL,
            MagicFilterType.KEVIN,
            MagicFilterType.LOMO,
            MagicFilterType.N1977,
            MagicFilterType.NASHVILLE,
            MagicFilterType.PIXAR,
            MagicFilterType.RISE,
            MagicFilterType.SIERRA,
            MagicFilterType.SUTRO,
            MagicFilterType.TOASTER2,
            MagicFilterType.VALENCIA,
            MagicFilterType.WALDEN,
            MagicFilterType.XPROII,

    };

    /**
     *  Initialisation method. This method is to bind and add listener to
     *  all widgets in current layout. MagicEngine refers to the controller
     *  of the filters and MagicCameraView is the view to display camera
     *  contents.
     *
     * @param savedInstanceState auto generated data structure
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        MagicEngine.Builder builder = new MagicEngine.Builder();
        magicEngine = builder
                .build((MagicCameraView)findViewById(R.id.glsurfaceview_camera));//写在对应资源文件中的一个自定义控件，控制器为MagicCameraView继承自MagicBaseView
        initView();
    }

    /**
     * This method is to initialise widgets and add listener to all widgets
     * in current layout. The parameters for camera preview is also determined.
     */

    private void initView(){
        mFilterLayout = (LinearLayout)findViewById(R.id.layout_filter);
        mFilterListView = (RecyclerView) findViewById(R.id.filter_listView);

        btn_shutter = (ImageView)findViewById(R.id.btn_camera_shutter);
        btn_mode = (ImageView)findViewById(R.id.btn_camera_mode);

        findViewById(R.id.btn_camera_filter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_closefilter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_shutter).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_switch).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_mode).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_beauty).setOnClickListener(btn_listener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this, types);
        mFilterListView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(onFilterChangeListener);

        animator = ObjectAnimator.ofFloat(btn_shutter,"rotation",0,360);
        animator.setDuration(500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        MagicCameraView cameraView = (MagicCameraView)findViewById(R.id.glsurfaceview_camera);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cameraView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.x * 4 / 3;
        cameraView.setLayoutParams(params);
    }

    private FilterAdapter.onFilterChangeListener onFilterChangeListener = new FilterAdapter.onFilterChangeListener(){

        @Override
        public void onFilterChanged(MagicFilterType filterType) {
            magicEngine.setFilter(filterType);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private View.OnClickListener btn_listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_camera_mode:
                    switchMode();
                    break;
                case R.id.btn_camera_shutter:
                    cameraShutterTask();
                    break;
                case R.id.btn_camera_filter:
                    showFilters();
                    break;
                case R.id.btn_camera_switch:
                    magicEngine.switchCamera();
                    break;
                case R.id.btn_camera_beauty:
                    new AlertDialog.Builder(CameraActivity.this)
                            .setSingleChoiceItems(new String[] { getString(R.string.close), "1", "2", "3", "4", "5"}, MagicParams.beautyLevel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        magicEngine.setBeautyLevel(which);
                                        dialog.dismiss();
                                    }
                                })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                    break;
                case R.id.btn_camera_closefilter:
                    hideFilters();
                    break;
            }
        }
    };

    /**
     * This method deals with the logic of camera shutter button. If storage permission
     * is granted, photos can videos can be taken. Otherwise the permission should
     * be requested.
     */

    @AfterPermissionGranted(WRITE_EXTERNAL_STORAGE_PERM)
    public void cameraShutterTask() {
        if (hasWriteStoragePermission()) {
            if(mode == MODE_PIC)
                takePhoto();
            else
                takeVideo();
        } else {
            Log.e(TAG, "Write external storage permission denied.");
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_write_external_storage),
                    WRITE_EXTERNAL_STORAGE_PERM,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Check if the permission of writing external storage is granted.
     *
     * @return a boolean value to show the result
     */

    private boolean hasWriteStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void switchMode(){
        if(mode == MODE_PIC){
            mode = MODE_VIDEO;
            btn_mode.setImageResource(R.drawable.icon_camera);
        }else{
            mode = MODE_PIC;
            btn_mode.setImageResource(R.drawable.icon_video);
        }
    }

    private void takePhoto(){
        magicEngine.savePicture(getOutputMediaFile(),null);
    }

    private void takeVideo(){
        if(isRecording) {
            animator.end();
            magicEngine.stopRecord();
        }else {
            animator.start();
            magicEngine.startRecord();
        }
        isRecording = !isRecording;
    }

    private void showFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", mFilterLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                findViewById(R.id.btn_camera_shutter).setClickable(false);
                mFilterLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0 ,  mFilterLayout.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_camera_shutter).setClickable(true);
            }
        });
        animator.start();
    }

    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MagicCamera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}
