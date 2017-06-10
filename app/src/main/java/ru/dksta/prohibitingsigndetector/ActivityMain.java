package ru.dksta.prohibitingsigndetector;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class ActivityMain extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int REQUEST_CODE_CAMERA = 1;

    private CameraBridgeViewBase cameraBridgeViewBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.CAMERA
        }, REQUEST_CODE_CAMERA);

        System.loadLibrary("native-lib");

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setMaxFrameSize(640, 480);
        cameraBridgeViewBase.enableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                break;
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat matGray = inputFrame.rgba();
        mask(matGray.getNativeObjAddr());
        return matGray;
    }

    public native void mask(long matAddress);
}