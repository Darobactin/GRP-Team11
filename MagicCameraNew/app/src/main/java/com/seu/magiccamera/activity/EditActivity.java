package com.seu.magiccamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.seu.magiccamera.MainActivity;
import com.seu.magiccamera.R;
import com.seu.magicfilter.MagicEngine;
import com.seu.magicfilter.widget.MagicImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditActivity extends Activity {
    private static final String TAG = "EditActivity";

    private MagicEngine magicEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initMagicPreview();
        initFragments();
        initRadioButtons();

        MagicEngine.Builder builder = new MagicEngine.Builder();
        magicEngine = builder.build((MagicImageView)findViewById(R.id.glsurfaceview_image));
        findViewById(R.id.image_edit_back).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                backTask();
            }
        });
        findViewById(R.id.image_edit_save).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void backTask() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void initRadioButtons() {

    }

    private void initFragments() {

    }

    private void initMagicPreview() {
        // TODO Auto-generated method stub
        Bundle bundle = getIntent().getExtras();
        try {
            String uri = bundle.getString("Uri");
            uri = uri.replaceAll("%2F", "/").replaceAll("/raw/", "");
            Toast.makeText(this, uri, Toast.LENGTH_LONG).show();
            Log.d(TAG, uri);

            MagicImageView imageView = (MagicImageView)findViewById(R.id.glsurfaceview_image);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
            Point screenSize = new Point();
            getWindowManager().getDefaultDisplay().getSize(screenSize);
            params.width = screenSize.x;
            params.height = screenSize.x * 4 / 3;
            imageView.setLayoutParams(params);

            File file = new File(uri);
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
