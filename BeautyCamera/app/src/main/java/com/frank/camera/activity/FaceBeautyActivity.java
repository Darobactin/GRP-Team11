package com.frank.camera.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import com.frank.camera.R;
import com.frank.camera.listener.OnPhotoTakenListener;
import com.frank.camera.view.FaceBeautyView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FaceBeautyActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,View.OnClickListener{

    private final static String TAG = FaceBeautyActivity.class.getSimpleName();
    private FaceBeautyView faceBeautyView;
    private final static String PATH = Environment.getExternalStorageDirectory().getPath();
    private final static SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.CHINESE);
    private boolean hasInit;//是否初始化过
    private boolean doTaken;//拍照标志
    private final static int quality = 100;//图片质量
    private MediaPlayer mediaPlayer;

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(R.layout.activity_face_beauty);

        initView();
        initOpenCV();
    }

    private void initView(){
        faceBeautyView = (FaceBeautyView) findViewById(R.id.photograph_view);
        findViewById(R.id.btn_swap).setOnClickListener(this);
        findViewById(R.id.btn_snap).setOnClickListener(this);
        ((RadioGroup) findViewById(R.id.group_beauty)).setOnCheckedChangeListener(this);
    }

    private void initOpenCV(){
        boolean result = OpenCVLoader.initDebug();
        if(result){
            Log.i(TAG, "initOpenCV success...");
            //初始化人脸检测器
            faceBeautyView.initDetector(getApplicationContext());
            //打开camera
            faceBeautyView.setLoadSuccess(true);
            faceBeautyView.enableView();
        }else {
            Log.e(TAG, "initOpenCV fail...");
        }
    }

    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId){
        switch (checkedId){
            case R.id.btn_cat://猫耳朵
                faceBeautyView.selectBeauty(0);
                break;
            case R.id.btn_rabbit://兔耳朵
                faceBeautyView.selectBeauty(1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_swap://切换摄像头
                faceBeautyView.swapCamera();
                break;
            case R.id.btn_snap://拍照
                takePhoto();
                break;
            default:
                break;
        }
    }

    /**
     * 执行拍照
     */
    private void takePhoto(){
        if(!hasInit){
            initPhotoListener();
            hasInit = true;
        }
        doTaken = true;
        shootSound();//播放拍照声音
    }

    private void initPhotoListener(){
        faceBeautyView.setOnPhotoTakenListener(new OnPhotoTakenListener() {
            @Override
            public void onPhotoTaken(Mat frameData) {
                Log.i(TAG, "taking photo");
                if(doTaken && frameData != null){
                    Log.i(TAG, "get photo data");
                    doTaken = false;
                    savePicture(frameData);
                }
            }
        });
    }

    /**
     * 保存图片
     * @param frameData 帧数据
     */
    private void savePicture(Mat frameData){
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        String fileName = PATH + File.separator + dataFormat.format(new Date(System.currentTimeMillis())) + ".jpg";
        Log.i(TAG, fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //跳转到编辑界面
        Intent intent = new Intent(FaceBeautyActivity.this, MainEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", "This is from MainActivity!");//Map结构，接收时可以通过key来找到内容
        bundle.putString("filename", fileName);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     *   播放系统拍照声音
     */
    public void shootSound() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
        if (volume != 0) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            }
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
    }

}
