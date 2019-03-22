/*
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.seu.magicfilter.widget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.megvii.facepp.api.FacePPApi;
import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.DetectResponse;
import com.megvii.facepp.api.bean.Face;
import com.seu.magicfilter.camera.CameraEngine;
import com.seu.magicfilter.camera.utils.CameraInfo;
import com.seu.magicfilter.encoder.video.TextureMovieEncoder;
import com.seu.magicfilter.filter.advanced.MagicBeautyFilter;
import com.seu.magicfilter.filter.base.MagicCameraInputFilter;
import com.seu.magicfilter.filter.helper.MagicFilterType;
import com.seu.magicfilter.helper.SavePictureTask;
import com.seu.magicfilter.utils.MagicParams;
import com.seu.magicfilter.utils.OpenGlUtils;
import com.seu.magicfilter.utils.Rotation;
import com.seu.magicfilter.utils.TextureRotationUtil;
import com.seu.magicfilter.widget.base.MagicBaseView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import duxiaoman.guofeng.myapplicationbeauty.R;

/**
 * Created by why8222 on 2016/2/25.
 */
public class MagicCameraView extends MagicBaseView {

    private MagicCameraInputFilter cameraInputFilter;
    private MagicBeautyFilter beautyFilter;

    private SurfaceTexture surfaceTexture;

    public MagicCameraView(Context context) {
        this(context, null);
    }

    private boolean recordingEnabled;
    private int recordingStatus;

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;
    private static final String TAG = "MagicCameraView";
    private static TextureMovieEncoder videoEncoder = new TextureMovieEncoder();

    private File outputFile;
    private CascadeClassifier classifier;
    public static ArrayList<double[]> faces = new ArrayList<>();
    private FacePPApi faceppApi = new FacePPApi("T5gSCLVBrjlSNSCjNghJKALaQBcX9pBg", "E9HE2nhuxgjhtZJLtdn6YYszViOFh2gg");

    public MagicCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
        outputFile = new File(MagicParams.videoPath,MagicParams.videoName);
        recordingStatus = -1;
        recordingEnabled = false;
        scaleType = ScaleType.CENTER_CROP;

        loadCascadeClassifier();
        //contourDetector = new ContourDetector(false);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        recordingEnabled = videoEncoder.isRecording();
        if (recordingEnabled)
            recordingStatus = RECORDING_RESUMED;
        else
            recordingStatus = RECORDING_OFF;
        if(cameraInputFilter == null)
            cameraInputFilter = new MagicCameraInputFilter();
        cameraInputFilter.init();
        // this textureId represents the camera stream
        if (textureId == OpenGlUtils.NO_TEXTURE) {
            textureId = OpenGlUtils.getExternalOESTextureID();
            if (textureId != OpenGlUtils.NO_TEXTURE) {
                surfaceTexture = new SurfaceTexture(textureId);
                surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        openCamera();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i("MagicCameraView", "onDrawFrame");
        super.onDrawFrame(gl);
        if(surfaceTexture == null)
            return;
        surfaceTexture.updateTexImage();
        if (recordingEnabled) {
            switch (recordingStatus) {
                case RECORDING_OFF:
                    CameraInfo info = CameraEngine.getCameraInfo();
                    videoEncoder.setPreviewSize(info.previewWidth, info.pictureHeight);
                    videoEncoder.setTextureBuffer(gLTextureBuffer);
                    videoEncoder.setCubeBuffer(gLCubeBuffer);
                    videoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                            outputFile, info.previewWidth, info.pictureHeight,
                            1000000, EGL14.eglGetCurrentContext(),
                            info));
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_RESUMED:
                    videoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    recordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        } else {
            switch (recordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    videoEncoder.stopRecording();
                    recordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    break;
                default:
                    throw new RuntimeException("unknown status " + recordingStatus);
            }
        }
        float[] mtx = new float[16];
        surfaceTexture.getTransformMatrix(mtx);
        cameraInputFilter.setTextureTransformMatrix(mtx);
        int id = textureId;
        final byte[] array;
        if(filter == null){
            // textureId -> camera stream;
            // gLCubeBuffer -> Coordinate defined from TextureRotationUtil.CUBE [8]
            // gLTextureBuffer -> Coordinate defined from TextureRotationUtil.TEXTURE_NO_ROTATION [8]
            cameraInputFilter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
            array = createByteArrayFromGLSurface(0, 0, surfaceWidth, surfaceHeight, gl);
        }else{
            id = cameraInputFilter.onDrawToTexture(textureId);
            filter.onDrawFrame(id, gLCubeBuffer, gLTextureBuffer);
            array = createByteArrayFromGLSurface(0, 0, surfaceWidth, surfaceHeight, gl);

        }
        videoEncoder.setTextureId(id);
        videoEncoder.frameAvailable(surfaceTexture);

        final Map<String, String> params = new HashMap<>();
        params.put("return_landmark", "2");
        params.put("return_attributes", "gender,age,smiling,emotion,ethnicity,beauty");
        new Runnable() {
            @Override
            public void run() {
                faceppApi.detect(params, array,
                        new IFacePPCallBack<DetectResponse>() {
                            @Override
                            public void onSuccess(DetectResponse detectResponse) {
                                ArrayList<double[]> trans = new ArrayList<>();
                                for (Face face : detectResponse.getFaces()) {
                                    double leX = face.getLandmark().getLeft_eye_center().getX();
                                    double leY = face.getLandmark().getLeft_eye_center().getY();
                                    double reX = face.getLandmark().getRight_eye_center().getX();
                                    double reY = face.getLandmark().getRight_eye_center().getY();
                                    double ecX = (leX + reX) / 2;
                                    double ecY = (leY + reY) / 2;
                                    double D = 2.2 * Math.sqrt(Math.pow(leX - reX, 2) + Math.pow(leY - reY, 2));
                                    double x1 = ecX - 0.5 * D;
                                    double y1 = ecY - D;
                                    double x2 = ecX + 0.5 * D;
                                    double y2 = ecY;
                                    Log.d(TAG, "( " + leX + " , " + leY + " ) , ( " + reX + " , " + reY + " )");
                            /*double x1 = face.getFace_rectangle().getLeft();
                            double y1 = face.getFace_rectangle().getTop();
                            double x2 = face.getFace_rectangle().getLeft() + face.getFace_rectangle().getWidth();
                            double y2 = face.getFace_rectangle().getTop() + face.getFace_rectangle().getHeight();*/
                                    double[] points = new double[4];
                                    Log.d(TAG, "( " + x1 + " , " + y1 + " ) , ( " + x2 + " , " + y2 + " )");
                                    points[0] = -x1 / (x2 - x1);
                                    points[1] = (surfaceHeight - y1) / (y2 - y1);
                                    points[2] = (surfaceWidth - x1) / (x2 - x1);
                                    points[3] = -y1 / (y2 - y1);
                                    trans.add(points);
                                }
                                if (trans.size() != 0) {
                                    faces = trans;
                                }
                            }
                            @Override
                            public void onFailed(String s) {
                                Log.e(TAG, "Fail to get faces");
                            }
                        });
            }
        }.run();

        /*Bitmap bmp = createBitmapFromGLSurface(0, 0, surfaceWidth, surfaceHeight, gl);
        Mat imgMat = new Mat(surfaceHeight, surfaceWidth, CvType.CV_8UC2, new Scalar(0));
        Utils.bitmapToMat(bmp, imgMat);
        MatOfRect faceRectangles = new MatOfRect();
        ArrayList<double[]> trans = new ArrayList<>();
        classifier.detectMultiScale(imgMat, faceRectangles, 1.3, 3, 2, new Size(250, 250), new Size());
        for (Rect rect : faceRectangles.toArray()) {
            double x1 = rect.tl().x;
            double y1 = rect.tl().y;
            double x2 = rect.br().x;
            double y2 = rect.br().y;
            double[] points = new double[4];
            points[0] = -x1 / (x2 - x1);
            points[1] = (surfaceHeight - y1) / (y2 - y1) + 0.45;
            points[2] = (surfaceWidth - x1) / (x2 - x1);
            points[3] = -y1 / (y2 - y1) + 0.45;
            trans.add(points);
        }
        if (trans.size() != 0) {
            faces = trans;
        }

        Log.d(TAG, "detect " + faceRectangles.toArray().length + " faces");
        for (Rect rect : faceRectangles.toArray()) {
            Log.d(TAG, "( " + rect.tl().x + " , " + rect.tl().y + " ) , ( " + rect.br().x + " , " + rect.br().y + " )");
        }*/
        /*contourDetector.detect(bmp);
        ArrayList<double[]> trans = new ArrayList<>();
        for (double[] rect : contourDetector.getRects()) {
            double x1 = rect[0];
            double y1 = rect[1];
            double x2 = rect[2];
            double y2 = rect[3];
            double[] points = new double[4];
            points[0] = -x1 / (x2 - x1);
            points[1] = (y1 - surfaceHeight) / (y2 - y1);
            points[2] = (surfaceWidth - x1) / (x2 - x1);
            points[3] = y1 / (y2 - y1);
            trans.add(points);

        }
        faces = trans;*/


    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
        }
    };

    @Override
    public void setFilter(MagicFilterType type) {
        super.setFilter(type);
        videoEncoder.setFilter(type);
    }

    private void openCamera(){
        if(CameraEngine.getCamera() == null)
            CameraEngine.openCamera();
        CameraInfo info = CameraEngine.getCameraInfo();
        if(info.orientation == 90 || info.orientation == 270){
            imageWidth = info.previewHeight;
            imageHeight = info.previewWidth;
        }else{
            imageWidth = info.previewWidth;
            imageHeight = info.previewHeight;
        }
        cameraInputFilter.onInputSizeChanged(imageWidth, imageHeight);
        adjustSize(info.orientation, info.isFront, true);
        if(surfaceTexture != null)
            CameraEngine.startPreview(surfaceTexture);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        CameraEngine.releaseCamera();
    }

    public void changeRecordingState(boolean isRecording) {
        recordingEnabled = isRecording;
    }

    protected void onFilterChanged(){
        super.onFilterChanged();
        cameraInputFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
        if(filter != null)
            cameraInputFilter.initCameraFrameBuffer(imageWidth, imageHeight);
        else
            cameraInputFilter.destroyFramebuffers();
    }

    @Override
    public void savePicture(final SavePictureTask savePictureTask) {
        CameraEngine.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                CameraEngine.stopPreview();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap photo = drawPhoto(bitmap,CameraEngine.getCameraInfo().isFront);
                        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
                        if (photo != null)
                            savePictureTask.execute(photo);
                    }
                });
                CameraEngine.startPreview();
            }
        });
    }

    private Bitmap drawPhoto(Bitmap bitmap,boolean isRotated){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] mFrameBuffers = new int[1];
        int[] mFrameBufferTextures = new int[1];
        if(beautyFilter == null)
            beautyFilter = new MagicBeautyFilter();
        beautyFilter.init();
        beautyFilter.onDisplaySizeChanged(width, height);
        beautyFilter.onInputSizeChanged(width, height);

        if(filter != null) {
            filter.onInputSizeChanged(width, height);
            filter.onDisplaySizeChanged(width, height);
        }
        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

        GLES20.glViewport(0, 0, width, height);
        int textureId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, true);

        FloatBuffer gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        FloatBuffer gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
        if(isRotated)
            gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, false)).position(0);
        else
            gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);


        if(filter == null){
            beautyFilter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
        }else{
            beautyFilter.onDrawFrame(textureId);
            //filter.onDrawFrame(mFrameBufferTextures[0], gLCubeBuffer, gLTextureBuffer);
            filter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
            //filter.onDrawFrame(textureId, this.gLCubeBuffer, this.gLTextureBuffer);
        }
        IntBuffer ib = IntBuffer.allocate(width * height);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.copyPixelsFromBuffer(ib);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
        GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);

        beautyFilter.destroy();
        beautyFilter = null;
        if(filter != null) {
            filter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
            filter.onInputSizeChanged(imageWidth, imageHeight);
        }
        return result;
    }

    public void onBeautyLevelChanged() {
        cameraInputFilter.onBeautyLevelChanged();
    }

    private void loadCascadeClassifier() {
        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
        File cascadeDir = getContext().getDir("cascade", Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
        try {
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            classifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (classifier.empty()) {
                Log.e(TAG, "fail to load cascade classifier");
                classifier = null;
            } else {
                Log.d(TAG, "successfully load cascade classifier");
            }
            cascadeDir.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
                    intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    /*int blue = (texturePixel >> 16) & 0xff; (use green channel)
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;*/
                    int red = (texturePixel << 16) & 0x00ff0000;
                    bitmapSource[offset2 + j] = 0xff000000 | red | red >> 8 | red >> 16;
                }
            }
        } catch (GLException e) {
            return null;
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    private byte[] createByteArrayFromGLSurface(int x, int y, int w, int h, GL10 gl) {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
                    intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        } catch (GLException e) {
            return null;
        }
        return baos.toByteArray();
    }

}
