package com.seu.magicfilter.filter.advanced;

import android.opengl.GLES20;
import android.util.Log;

import com.seu.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.seu.magicfilter.utils.MagicParams;
import com.seu.magicfilter.utils.OpenGlUtils;
import com.seu.magicfilter.widget.MagicCameraView;

import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import duxiaoman.guofeng.myapplicationbeauty.R;

public class MagicRabbitEarFilter extends GPUImageFilter {
    private int[] inputTextureHandles = {-1,-1,-1,-1};
    private int[] inputTextureUniformLocations = {-1,-1,-1,-1};
    private int mGLStrengthLocation;
    private FloatBuffer mGLTextureBuffer2;
    private FloatBuffer mGLTextureBuffer3;
    private FloatBuffer mGLTextureBuffer4;
    private FloatBuffer mGLTextureBuffer5;
    private int mGLAttribTextureCoordinate2;
    private int mGLAttribTextureCoordinate3;
    private int mGLAttribTextureCoordinate4;
    private int mGLAttribTextureCoordinate5;
    private static final String TAG = "MagicRabbitEarFilter";

    public MagicRabbitEarFilter(){
        super(OpenGlUtils.readShaderFromRawResource(R.raw.cat_vertex), OpenGlUtils.readShaderFromRawResource(R.raw.cat_fragment));
    }

    protected void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(inputTextureHandles.length, inputTextureHandles, 0);
        for(int i = 0; i < inputTextureHandles.length; i++)
            inputTextureHandles[i] = -1;
    }

    protected void onDrawArraysAfter(){
        for(int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }
    }

    //调用顺序3：提交给shader
    protected void onDrawArraysPre(){
        for(int i = 0; i < inputTextureHandles.length
                && inputTextureHandles[i] != OpenGlUtils.NO_TEXTURE; i++){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3) );
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i]);
            GLES20.glUniform1i(inputTextureUniformLocations[i], (i+3));
        }
    }

    //调用顺序2：载入程序
    protected void onInit(){
        super.onInit();
        for(int i=0; i < inputTextureUniformLocations.length; i++)
            // Set program handles
            inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture"+(2+i));
        mGLStrengthLocation = GLES20.glGetUniformLocation(mGLProgId, "strength");
        mGLAttribTextureCoordinate2 = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate2");
        mGLAttribTextureCoordinate3 = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate3");
        mGLAttribTextureCoordinate4 = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate4");
        mGLAttribTextureCoordinate5 = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate5");


    }

    //调用顺序1：载入图片，然后将图片绑定给纹理，获取纹理索引
    protected void onInitialized(){
        super.onInitialized();
        setFloat(mGLStrengthLocation, 1.0f);//强度设定
        runOnDraw(new Runnable(){
            public void run(){
                inputTextureHandles[0] = OpenGlUtils.loadTexture(MagicParams.context, "filter/rabbit.png");
                inputTextureHandles[1] = OpenGlUtils.loadTexture(MagicParams.context, "filter/rabbit.png");
                inputTextureHandles[2] = OpenGlUtils.loadTexture(MagicParams.context, "filter/rabbit.png");
                inputTextureHandles[3] = OpenGlUtils.loadTexture(MagicParams.context, "filter/rabbit.png");
            }
        });
    }

    @Override
    public int onDrawFrame(final int textureId, final FloatBuffer cubeBuffer,
                           final FloatBuffer textureBuffer) {
        int numFaces = 4 < MagicCameraView.faces.size() ? 4 : MagicCameraView.faces.size();
        float[][] sPos = new float[4][8];
        if (numFaces == 0) {
            return super.onDrawFrame(textureId, cubeBuffer, textureBuffer);
        } else {
            for (int i = 0; i < numFaces; i++) {
                sPos[i][0] = sPos[i][4] = (float) MagicCameraView.faces.get(i)[0];
                sPos[i][1] = sPos[i][3] = (float) MagicCameraView.faces.get(i)[1] + 0.1f;
                sPos[i][2] = sPos[i][6] = (float) MagicCameraView.faces.get(i)[2];
                sPos[i][5] = sPos[i][7] = (float) MagicCameraView.faces.get(i)[3] + 0.1f;
            }

            for (int i = numFaces; i < 4; i++) {
                for (int j = 0; j < 8; j++) {
                    sPos[i][j] = 0;
                }
            }

        }
        //Log.d(TAG, sPos[0] + "," + sPos[1] + "," + sPos[2] + "," + sPos[3]+ "," + sPos[4] + "," + sPos[5] + "," + sPos[6] + "," + sPos[7]);
        mGLTextureBuffer2 = ByteBuffer.allocateDirect(sPos[0].length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer2.put(sPos[0]).position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate2, 2, GLES20.GL_FLOAT, false, 0,
                mGLTextureBuffer2);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate2);
        mGLTextureBuffer3 = ByteBuffer.allocateDirect(sPos[1].length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer3.put(sPos[1]).position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate3, 2, GLES20.GL_FLOAT, false, 0,
                mGLTextureBuffer3);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate3);
        mGLTextureBuffer4 = ByteBuffer.allocateDirect(sPos[2].length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer4.put(sPos[2]).position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate4, 2, GLES20.GL_FLOAT, false, 0,
                mGLTextureBuffer4);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate4);
        mGLTextureBuffer5 = ByteBuffer.allocateDirect(sPos[3].length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer5.put(sPos[3]).position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate5, 2, GLES20.GL_FLOAT, false, 0,
                mGLTextureBuffer5);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate5);
        int returnValue = super.onDrawFrame(textureId, cubeBuffer, textureBuffer);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate2);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate3);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate4);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate5);
        return returnValue;
    }
}
