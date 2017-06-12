package ru.dksta.prohibitingsigndetector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import ru.dksta.prohibitingsigndetector.utils.Prefs;

public class ActivityMain extends Activity implements CameraBridgeViewBase.CvCameraViewListener2,
    View.OnClickListener, View.OnLongClickListener {

    private static final int REQUEST_CODE_CAMERA = 1;

    private Prefs prefs;

    private JavaCameraView javaCameraView;

    private ImageView play;

    private long lastSecondTime;
    private int framesTempCount;
    private int framesPerSecond = 0;
    private boolean rotateMat;
    private boolean showInfo;
    private int layerType = Constants.LAYER_DEFAULT;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    System.loadLibrary("native-lib");
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

        findViewById(R.id.settings).setOnClickListener(this);

        play = (ImageView) findViewById(R.id.play);
        play.setTag(R.drawable.ic_play_arrow_36dp);
        play.setOnClickListener(this);

        View layers = findViewById(R.id.layers);
        layers.setTag(Constants.LAYER_DEFAULT);
        layers.setOnClickListener(this);
        layers.setOnLongClickListener(this);

        prefs = new Prefs(getApplicationContext());

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.CAMERA
        }, REQUEST_CODE_CAMERA);

        javaCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setMaxFrameSize(640, 480);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), ActivitySettings.class));
                break;
            case R.id.play:
                if ((int) view.getTag() == R.drawable.ic_play_arrow_36dp) {
                    javaCameraView.enableView();
                } else {
                    javaCameraView.disableView();
                }
                break;
            case R.id.layers:
                switch ((int) view.getTag()) {
                    case Constants.LAYER_HSV:
                        setLayerImage(view, R.drawable.ic_filter_2_36dp, Constants.LAYER_HUE_LOWER);
                        break;
                    case Constants.LAYER_HUE_LOWER:
                        setLayerImage(view, R.drawable.ic_filter_3_36dp, Constants.LAYER_HUE_UPPER);
                        break;
                    case Constants.LAYER_HUE_UPPER:
                        setLayerImage(view, R.drawable.ic_filter_4_36dp, Constants.LAYER_HUE);
                        break;
                    case Constants.LAYER_HUE:
                        setLayerImage(view, R.drawable.ic_filter_5_36dp, Constants.LAYER_SATURATION);
                        break;
                    case Constants.LAYER_SATURATION:
                        setLayerImage(view, R.drawable.ic_filter_6_36dp,
                                Constants.LAYER_COLOR_FILTERED);
                        break;
                    case Constants.LAYER_COLOR_FILTERED:
                        setLayerImage(view, R.drawable.ic_filter_7_36dp, Constants.LAYER_BLUR);
                        break;
                    case Constants.LAYER_BLUR:
                        setLayerImage(view, R.drawable.ic_filter_36dp, Constants.LAYER_DEFAULT);
                        break;
                    case Constants.LAYER_DEFAULT:
                        setLayerImage(view, R.drawable.ic_filter_1_36dp, Constants.LAYER_HSV);
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.layers:
                setLayerImage(view, R.drawable.ic_filter_36dp, Constants.LAYER_DEFAULT);
                break;
            default:
                break;
        }
        return true;
    }

    private void setLayerImage(View view, @DrawableRes int id, int layerType) {
        this.layerType = layerType;
        view.setTag(layerType);
        ((ImageView) view).setImageResource(id);
    }

    @Override
    public void onPause() {
        super.onPause();
        javaCameraView.disableView();
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
        lastSecondTime = System.currentTimeMillis();
        framesTempCount = 0;
        rotateMat = prefs.getBoolean(Prefs.ROTATE_MAT);
        showInfo = prefs.getBoolean(Prefs.SHOW_INFO, true);
        play.setTag(R.drawable.ic_pause_36dp);
        play.setImageResource(R.drawable.ic_pause_36dp);
    }

    @Override
    public void onCameraViewStopped() {
        play.setTag(R.drawable.ic_play_arrow_36dp);
        play.setImageResource(R.drawable.ic_play_arrow_36dp);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        int fpsCount = countFPS();
        Mat mat = inputFrame.rgba();
        long matAddress = mat.getNativeObjAddr();
        if (rotateMat) {
            rotation(matAddress, 180);
        }
        int[] circlesArray = search(matAddress, layerType);
        selection(matAddress, circlesArray);
        if (showInfo) {
            information(matAddress, fpsCount, layerType, circlesArray);
        }
        return mat;
    }

    private int countFPS() {
        long currentFrameTime = System.currentTimeMillis();
        if (currentFrameTime - lastSecondTime >= 1000) {
            lastSecondTime = currentFrameTime;
            framesPerSecond = framesTempCount;
            framesTempCount = 0;
        }
        framesTempCount++;
        return framesPerSecond;
    }

    public native int[] search(long matAddress, int layerType);

    public native void selection(long matAddress, int[] circlesArray);

    public native void information(long matAddress, int fpsCount, int layerType, int[] circlesArray);

    public native void rotation(long matAddress, int angle);
}