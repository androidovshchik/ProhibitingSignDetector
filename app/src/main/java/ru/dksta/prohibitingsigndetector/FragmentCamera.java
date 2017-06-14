package ru.dksta.prohibitingsigndetector;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;

public class FragmentCamera extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;

    private long lastSecondTime;
    private int framesTempCount;
    private int framesPerSecond = 0;

    public FragmentCamera() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        javaCameraView = (JavaCameraView) root.findViewById(R.id.javaCameraView);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setMaxFrameSize(640, 480);
        return root;
    }

    public void onPlayEvent(boolean play) {
        if (play) {
            javaCameraView.enableView();
        } else {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        lastSecondTime = System.currentTimeMillis();
        framesTempCount = 0;
    }

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        int fpsCount = countFPS();
        Mat mat = inputFrame.rgba();
        long matAddress = mat.getNativeObjAddr();
        if (getActivityMain().rotateMat) {
            getActivityMain().rotation(matAddress, 180);
        }
        int[] circlesArray = getActivityMain().search(matAddress, getActivityMain().layerType,
                getActivityMain().lowerHue, getActivityMain().upperHue,
                getActivityMain().minSaturation, getActivityMain().minValue,
                getActivityMain().blur, getActivityMain().minArea, getActivityMain().minCircularity,
                getActivityMain().minInertiaRatio);
        if (circlesArray != null) {
            getActivityMain().selection(matAddress, circlesArray);
        }
        if (getActivityMain().showInfo) {
            getActivityMain().information(matAddress, fpsCount,
                    getActivityMain().layerType, circlesArray);
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

    private ActivityMain getActivityMain() {
        return (ActivityMain) getActivity();
    }
}
