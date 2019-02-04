package com.seu.magiccamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.seu.magiccamera.MainActivity;
import com.seu.magiccamera.R;


/**
 * Created by why8222 on 2016/3/18.
 */
public class AlbumActivity extends Activity {

    private static final String TAG = "AlbumActivity";
    private static final int ALBUM_REQUEST_CODE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        findViewById(R.id.image_edit_back).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                backTask();
            }
        });
        getPicFromAlbum();
    }

    private void getPicFromAlbum() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);

    }

    private void backTask() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        Intent UriIntent = new Intent(this, EditActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Uri", uri.getEncodedPath());
                        UriIntent.putExtras(bundle);
                        startActivity(UriIntent);
                    }
                }
                break;
            default:
                break;
        }
    }
}
