package ru.dksta.prohibitingsigndetector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    private int framesPerSecond;
    private boolean rotateMat;

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
        findViewById(R.id.fps).setOnClickListener(this);

        play = (ImageView) findViewById(R.id.play);
        play.setTag(R.drawable.ic_play_arrow_36dp);
        play.setOnClickListener(this);

        View layers = findViewById(R.id.layers);
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
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.layers:
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        javaCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        framesPerSecond = 0;
        rotateMat = prefs.getBoolean(Prefs.ROTATE_MAT);
        play.setTag(R.drawable.ic_play_arrow_36dp);
        play.setImageResource(R.drawable.ic_play_arrow_36dp);
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
        int[] circlesArray = search(matAddress);
        selection(matAddress, circlesArray);
        information(matAddress, fpsCount, circlesArray);
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

    public native int[] search(long matAddress);

    public native void selection(long matAddress, int[] circlesArray);

    public native void information(long matAddress, int fpsCount, int[] circlesArray);

    public native void rotation(long matAddress, int angle);
}