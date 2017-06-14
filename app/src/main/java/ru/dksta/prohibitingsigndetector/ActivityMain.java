package ru.dksta.prohibitingsigndetector;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import ru.dksta.prohibitingsigndetector.utils.PermissionsUtil;

public class ActivityMain extends Activity {

    private static final int REQUEST_CODE_CAMERA = 1;

    public boolean allowWork = false;

    public int layerType = Constants.LAYER_RGBA;
    public boolean rotateMat;
    public boolean showInfo;

    public int lowerHue;
    public int upperHue;
    public int minSaturation;
    public int minValue;
    public int blur;

    public int minArea;
    public float minCircularity;
    public float minInertiaRatio;

    private FragmentSettings settings;
    private FragmentCamera camera;

    private BaseLoaderCallback baseLoaderCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        settings = (FragmentSettings) getFragmentManager().findFragmentById(R.id.settingFragment);
        camera = (FragmentCamera) getFragmentManager().findFragmentById(R.id.cameraFragment);
        baseLoaderCallback = new BaseLoaderCallback(getApplicationContext()) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        System.loadLibrary("native-lib");
                        break;
                    default:
                        break;
                }
            }
        };
        allowWork = PermissionsUtil.hasAllPermissions(getApplicationContext());
        if (!allowWork) {
            ActivityCompat.requestPermissions(this, PermissionsUtil.ALL_PERMISSIONS,
                    REQUEST_CODE_CAMERA);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, getApplicationContext(),
                    baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        camera.onPlayEvent(false);
        settings.onPlayEvent(false);
    }

    public boolean triggerPlayEvent(boolean play) {
        if (allowWork) {
            camera.onPlayEvent(play);
        } else {
            ActivityCompat.requestPermissions(this, PermissionsUtil.ALL_PERMISSIONS,
                    REQUEST_CODE_CAMERA);
        }
        return allowWork;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                allowWork = PermissionsUtil.hasAllPermissions(getApplicationContext());
                break;
        }
    }

    public native int[] search(long matAddress, int layerType, int lowerHue, int upperHue,
                               int minSaturation, int minValue, int blur, int minArea,
                               float minCircularity, float minInertiaRatio);

    public native void selection(long matAddress, int[] circlesArray);

    public native void information(long matAddress, int fpsCount, int layerType, int[] circlesArray);

    public native void rotation(long matAddress, int angle);
}