package ru.dksta.prohibitingsigndetector;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import ru.dksta.prohibitingsigndetector.utils.Prefs;

public class FragmentSettings extends Fragment {

    private static final double PI2 = Math.PI * 2;

    private Prefs prefs;

    private View lowerThreshold;
    private View upperThreshold;

    private int[] minColors;
    private float[] lowerHSV;
    private float[] upperHSV;
    private int[] maxColors;
    private GradientDrawable lowerGradient;
    private GradientDrawable upperGradient;

    public TextView lowerHue;
    public TextView upperHue;
    public TextView minSaturation;
    public TextView minValue;
    public TextView blur;

    public FragmentSettings() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = new Prefs(getActivity().getApplicationContext());
        getActivityMain().lowerHue = prefs.getInteger(Prefs.LOWER_HUE, 12);
        getActivityMain().upperHue = prefs.getInteger(Prefs.UPPER_HUE, 168);
        getActivityMain().minSaturation = prefs.getInteger(Prefs.MIN_SATURATION, 50);
        getActivityMain().minValue = prefs.getInteger(Prefs.MIN_VALUE, 100);
        getActivityMain().blur = prefs.getInteger(Prefs.BLUR, 3);

        lowerThreshold = root.findViewById(R.id.lowerThreshold);
        upperThreshold = root.findViewById(R.id.upperThreshold);

        minColors = new int[2];
        minColors[0] = Color.HSVToColor(new float[] { 0f, 1f, 1f });
        minColors[1] = Color.parseColor("#f5f5f5");
        lowerHSV = new float[3];
        upperHSV = new float[3];
        maxColors = new int[2];
        maxColors[0] = Color.parseColor("#f5f5f5");
        maxColors[1] = Color.HSVToColor(new float[] { 358f, 1f, 1f });
        lowerGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, minColors);
        upperGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, maxColors);
        setThresholds();

        lowerHue = (TextView) root.findViewById(R.id.lowerHue);
        HorizontalWheelView lowerHueWheel = (HorizontalWheelView)
                root.findViewById(R.id.lowerHueWheel);
        lowerHueWheel.setRadiansAngle(getAngle(getActivityMain().lowerHue, 179));
        lowerHueWheel.setEndLock(true);
        lowerHueWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().lowerHue = (int) Math.round(radians / PI2 * 179);
                setTextValues();
                setThresholds();
            }
        });

        upperHue = (TextView) root.findViewById(R.id.upperHue);
        HorizontalWheelView upperHueWheel = (HorizontalWheelView)
                root.findViewById(R.id.upperHueWheel);
        upperHueWheel.setRadiansAngle(getAngle(getActivityMain().upperHue, 179));
        upperHueWheel.setEndLock(true);
        upperHueWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().upperHue = (int) Math.round(radians / PI2 * 179);
                setTextValues();
                setThresholds();
            }
        });

        minSaturation = (TextView) root.findViewById(R.id.minSaturation);
        HorizontalWheelView minSaturationWheel = (HorizontalWheelView)
                root.findViewById(R.id.minSaturationWheel);
        minSaturationWheel.setRadiansAngle(getAngle(getActivityMain().minSaturation, 255));
        minSaturationWheel.setEndLock(true);
        minSaturationWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().minSaturation = (int) Math.round(radians / PI2 * 255);
                setTextValues();
                setThresholds();
            }
        });

        minValue = (TextView) root.findViewById(R.id.minValue);
        HorizontalWheelView minValueWheel = (HorizontalWheelView)
                root.findViewById(R.id.minValueWheel);
        minValueWheel.setRadiansAngle(getAngle(getActivityMain().minValue, 255));
        minValueWheel.setEndLock(true);
        minValueWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().minValue = (int) Math.round(radians / PI2 * 255);
                setTextValues();
                setThresholds();
            }
        });

        blur = (TextView) root.findViewById(R.id.blur);
        HorizontalWheelView blurWheel = (HorizontalWheelView)
                root.findViewById(R.id.blurWheel);
        blurWheel.setRadiansAngle(getAngle((getActivityMain().blur - 3) / 2, 9));
        blurWheel.setEndLock(true);
        blurWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().blur = (int) Math.round(radians / PI2 * 9) * 2 + 3;
                setTextValues();
            }
        });

        setTextValues();

        return root;
    }

    private void setTextValues() {
        lowerHue.setText(getString(R.string.text_lower_hue,
                getActivityMain().lowerHue));
        upperHue.setText(getString(R.string.text_upper_hue,
                getActivityMain().upperHue));
        minSaturation.setText(getString(R.string.text_min_saturation,
                getActivityMain().minSaturation));
        minValue.setText(getString(R.string.text_min_value,
                getActivityMain().minValue));
        blur.setText(getString(R.string.text_blur,
                getActivityMain().blur));
    }

    private void setThresholds() {
        float minSaturation = 1f * getActivityMain().minSaturation / 255;
        float minValue = 1f * getActivityMain().minValue / 255;
        lowerHSV[0] = getActivityMain().lowerHue * 2;
        lowerHSV[1] = minSaturation;
        lowerHSV[2] = minValue;
        upperHSV[0] = getActivityMain().upperHue * 2;
        upperHSV[1] = minSaturation;
        upperHSV[2] = minValue;
        minColors[1] = Color.HSVToColor(lowerHSV);
        maxColors[0] = Color.HSVToColor(upperHSV);
        lowerGradient.setColors(minColors);
        upperGradient.setColors(maxColors);
        lowerThreshold.setBackground(lowerGradient);
        upperThreshold.setBackground(upperGradient);
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.putInteger(Prefs.LOWER_HUE, getActivityMain().lowerHue);
        prefs.putInteger(Prefs.UPPER_HUE, getActivityMain().upperHue);
        prefs.putInteger(Prefs.MIN_SATURATION, getActivityMain().minSaturation);
        prefs.putInteger(Prefs.MIN_VALUE, getActivityMain().minValue);
        prefs.putInteger(Prefs.BLUR, getActivityMain().blur);
    }

    private double getAngle(int value, int range) {
        return PI2 * value / range;
    }

    private ActivityMain getActivityMain() {
        return (ActivityMain) getActivity();
    }
}
