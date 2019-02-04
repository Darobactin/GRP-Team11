package com.frank.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import com.frank.camera.detector.ObjectDetector;
import com.frank.camera.R;
import com.frank.camera.listener.OnPhotoTakenListener;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.FONT_HERSHEY_COMPLEX;

public class FaceBeautyView extends BaseCameraView {

    private static final String TAG = "FaceDetectingView";
    private ObjectDetector mFaceDetector;
    private MatOfRect mObject;
    private Mat beauty;
    private List<Mat> beautyList = new ArrayList<>();
    private OnPhotoTakenListener onPhotoTakenListener;

    public void setOnPhotoTakenListener(OnPhotoTakenListener onPhotoTakenListener){
        this.onPhotoTakenListener = onPhotoTakenListener;
    }

    /**
     * 初始化人脸检测器
     * @param context context
     */
    public void initDetector(Context context){
        mObject = new MatOfRect();
        mFaceDetector = new ObjectDetector(context, R.raw.lbpcascade_frontalface, 6, 0.2F, 0.2F, new Scalar(255, 255, 255, 0));
    }

    @Override
    public void onOpenCVLoadSuccess() {
        Log.i(TAG, "onOpenCVLoadSuccess...");
    }

    @Override
    public void onOpenCVLoadFail() {
        Log.i(TAG, "onOpenCVLoadFail...");
    }

    public FaceBeautyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        // 检测人脸
        Rect[] object = mFaceDetector.detectObject(mGray, mObject);
        if(object != null && object.length > 0){
            for (Rect rect : object) {
                //检测到人脸矩形
                //矩形标识
                Imgproc.rectangle(mRgba, rect.tl(), rect.br(), mFaceDetector.getRectColor(), 3);
                //Imgproc.circle(mRgba, rect.tl(), 4, new Scalar(255, 0, 0, 255));
                //Imgproc.putText(mRgba, "(" + rect.tl().x +" , " + rect.tl().y + ")", rect.tl(), 3, 1, new Scalar(255, 0, 0, 255), 2);
                //Imgproc.circle(mRgba, rect.br(), 4, new Scalar(0, 255, 255, 255));
                //Imgproc.putText(mRgba, "(" + rect.br().x +" , " + rect.br().y + ")", rect.br(), 3, 1, new Scalar(0, 255, 255, 255), 2);

                if (beauty != null) {
                    //添加宠萌妆饰
                    //Imgproc.circle(mRgba, new Point((int)rect.tl().y, (int)(rect.tl().x+rect.br().x-beauty.cols())/2), 4, new Scalar(255, 255, 255, 255));
                    //Imgproc.putText(mRgba, "(" + (int)rect.tl().y +" , " + (int)(rect.tl().x+rect.br().x-beauty.cols())/2 + ")", new Point((int)rect.tl().y, (int)(rect.tl().x+rect.br().x-beauty.cols())/2), 3, 1, new Scalar(255, 255, 255, 255), 2);
                    addBeauty(rect, (int) rect.tl().y, (int) (rect.tl().x + rect.br().x) / 2);
                    //addBeauty(rect, (int)rect.tl().y, (int)(rect.tl().x+rect.br().x-beauty.cols())/2);

                }
            }
        }


        //拍照一帧数据回调
        if(onPhotoTakenListener != null){
            onPhotoTakenListener.onPhotoTaken(mRgba);
        }
        return mRgba;
    }

    /**
     * 添加宠萌效果
     * @param offsetX x坐标偏移量
     * @param offsetY y坐标偏移量
     */
    private void addBeauty(Rect rect, int offsetX, int offsetY){
        double scale = 1.5 * rect.width / 320;
        offsetX -= 150 * scale;//高度校正
        if(offsetX < 0){
            offsetX = 0;
        }
        Size dsize = new Size(beauty.width() * scale, beauty.height() * scale);
        Mat beauty_resize = new Mat(dsize, CvType.CV_16S);
        Imgproc.resize(beauty, beauty_resize, dsize);
        for (int x=0; x<beauty_resize.rows(); x++){
            for (int y=0; y<beauty_resize.cols(); y++){
                double[] array = beauty_resize.get(x, y);
                if(array[0] != 0) {//过滤全黑像素
                    mRgba.put(x+offsetX, y+offsetY-beauty_resize.cols()/2, array);
                }
            }
        }
        //Imgproc.putText(mRgba, "=====OPENCV PUTTEXT TEST=====", new Point(20, 40), 3, 1, new Scalar(0, 255, 0, 255), 2);
        //Imgproc.putText(mRgba, "width = " + rect.width + ", height = " + rect.height, new Point(20, 40), 3, 1, new Scalar(0, 255, 0, 255), 2);
        //Imgproc.putText(mRgba, "20,100", new Point(20, 100), 3, 1, new Scalar(0, 255, 0, 255), 2);
        //Imgproc.putText(mRgba, "40,100", new Point(40, 100), 3, 1, new Scalar(0, 255, 0, 255), 2);

    }

    /**
     * 选择宠萌妆饰
     * @param index index
     */
    public void selectBeauty(int index){
        if (beautyList.size() == 0){
            getBeauty();
        }
        beauty = beautyList.get(index);
    }

    /**
     * 获取宠萌妆饰list集合
     */
    private void getBeauty(){
        Drawable drawable1 = getResources().getDrawable(R.drawable.cat, null);
        Bitmap bitmap1 = ((BitmapDrawable) drawable1).getBitmap();
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, 320, 320, true);
        Mat beauty1 = new Mat();
        Utils.bitmapToMat(bitmap1, beauty1);
        beautyList.add(beauty1);
        Drawable drawable2 = getResources().getDrawable(R.drawable.rabbit, null);
        Bitmap bitmap2 = ((BitmapDrawable) drawable2).getBitmap();
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, 320, 320, true);
        Mat beauty2 = new Mat();
        Utils.bitmapToMat(bitmap2, beauty2);
        beautyList.add(beauty2);
    }

}
