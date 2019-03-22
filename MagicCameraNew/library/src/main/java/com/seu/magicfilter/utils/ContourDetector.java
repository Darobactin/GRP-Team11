package com.seu.magicfilter.utils;
/*
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;
*/
public class ContourDetector {/*
    private boolean isRealTime = true;
    private FirebaseVisionFaceDetectorOptions options;
    private FirebaseVisionImage image = null;
    private FirebaseVisionFaceDetector detector;
    private ArrayList<double[]> rects = new ArrayList<>();

    public ContourDetector(boolean isRealTime) {
        if (isRealTime) {
            this.options = new FirebaseVisionFaceDetectorOptions.Builder()
                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                    .build();
        } else {
            this.options = new FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                    .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                    .build();
        }
        this.detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    public void detect(Bitmap bitmap) {
        image = FirebaseVisionImage.fromBitmap(bitmap);
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        ArrayList<double[]> temp = new ArrayList<>();
                                        for (FirebaseVisionFace face : faces) {
                                            Rect bounds = face.getBoundingBox();
                                            temp.add(new double[]{bounds.left, bounds.top, bounds.right, bounds.bottom});
                                        }
                                        rects = temp;
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

    public ArrayList<double[]> getRects() {
        return this.rects;
    }
*/
}
