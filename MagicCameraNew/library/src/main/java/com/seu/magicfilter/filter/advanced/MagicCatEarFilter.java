package com.seu.magicfilter.filter.advanced;

import android.opengl.GLES20;

import com.seu.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.seu.magicfilter.utils.MagicParams;
import com.seu.magicfilter.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import duxiaoman.guofeng.myapplicationbeauty.R;

public class MagicCatEarFilter extends GPUImageFilter {
    private int[] inputTextureHandles = {-1,-1,-1,-1};
    private int[] inputTextureUniformLocations = {-1,-1,-1,-1};
    private int mGLStrengthLocation;
    private FloatBuffer mGLTextureBuffer2;
    private int mGLAttribTextureCoordinate2;

    public MagicCatEarFilter(){
        //super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(R.raw.cat_fragment));
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

    }

    //调用顺序1：载入图片，然后将图片绑定给纹理，获取纹理索引
    protected void onInitialized(){
        super.onInitialized();
        setFloat(mGLStrengthLocation, 1.0f);//强度设定
        runOnDraw(new Runnable(){
            public void run(){
                float[] sPos={
                        -0.5f,  0.5f, 0.0f, // top left
                        -0.5f, -0.5f, 0.0f, // bottom left
                        0.5f, 0.5f, 0.0f, // bottom right
                        0.5f, -0.5f, 0.0f  // top right
                };
                mGLTextureBuffer2 = ByteBuffer.allocateDirect(sPos.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                mGLTextureBuffer2.put(sPos).position(0);
                GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate2, 2, GLES20.GL_FLOAT, false, 0,
                        mGLTextureBuffer2);
                GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate2);
                inputTextureHandles[0] = OpenGlUtils.loadTexture(MagicParams.context, "filter/cat.png");
                inputTextureHandles[1] = OpenGlUtils.loadTexture(MagicParams.context, "filter/cat.png");
                inputTextureHandles[2] = OpenGlUtils.loadTexture(MagicParams.context, "filter/cat.png");
                inputTextureHandles[3] = OpenGlUtils.loadTexture(MagicParams.context, "filter/cat.png");
            }
        });
    }
}
