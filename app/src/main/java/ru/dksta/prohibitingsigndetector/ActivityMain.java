package ru.dksta.prohibitingsigndetector;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class ActivityMain extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int REQUEST_CODE_CAMERA = 1;

    private CameraBridgeViewBase cameraBridgeViewBase;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    System.loadLibrary("native-lib");
                    cameraBridgeViewBase.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.CAMERA
        }, REQUEST_CODE_CAMERA);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setMaxFrameSize(640, 480);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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
        Mat mat = inputFrame.rgba();
        long matAddress = mat.getNativeObjAddr();
        int[] circlesArray = search(matAddress);
        selection(matAddress, circlesArray);
        return mat;
    }

    public native int[] search(long matAddress);

    public native void selection(long matAddress, int[] circlesArray);
}