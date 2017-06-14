package ru.dksta.prohibitingsigndetector;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import ru.dksta.prohibitingsigndetector.utils.Prefs;

public class FragmentSettings extends Fragment implements View.OnClickListener,
        View.OnLongClickListener {

    private static final double PI2 = Math.PI * 2;
    private static final int BACKGROUND = Color.parseColor("#f5f5f5");
    private static final int LIGHT = Color.parseColor("#b5b5b5");
    private static final int DARK = Color.parseColor("#202020");

    private Prefs prefs;

    private View lowerThreshold;
    private View upperThreshold;

    private int[] minColors;
    private float[] lowerHSV;
    private float[] upperHSV;
    private int[] maxColors;
    private GradientDrawable lowerGradient;
    private GradientDrawable upperGradient;

    private TextView lowerHue;
    private TextView upperHue;
    private TextView minSaturation;
    private TextView minValue;
    private TextView blur;

    private HorizontalWheelView lowerHueWheel;
    private HorizontalWheelView upperHueWheel;
    private HorizontalWheelView minSaturationWheel;
    private HorizontalWheelView minValueWheel;
    private HorizontalWheelView blurWheel;

    private ImageView save;

    public FragmentSettings() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = new Prefs(getActivity().getApplicationContext());
        restoreVars();

        lowerThreshold = root.findViewById(R.id.lowerThreshold);
        upperThreshold = root.findViewById(R.id.upperThreshold);

        minColors = new int[2];
        minColors[0] = Color.HSVToColor(new float[] { 0f, 1f, 1f });
        minColors[1] = BACKGROUND;
        lowerHSV = new float[3];
        upperHSV = new float[3];
        maxColors = new int[2];
        maxColors[0] = BACKGROUND;
        maxColors[1] = Color.HSVToColor(new float[] { 358f, 1f, 1f });
        lowerGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, minColors);
        upperGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, maxColors);
        setLowerThreshold();
        setUpperThreshold();

        lowerHue = (TextView) root.findViewById(R.id.lowerHue);
        lowerHueWheel = (HorizontalWheelView) root.findViewById(R.id.lowerHueWheel);
        lowerHueWheel.setEndLock(true);
        lowerHueWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().lowerHue = (int) Math.round(radians / PI2 * 179);
                setTextValues();
                setLowerThreshold();
                onChanges();
            }
        });

        upperHue = (TextView) root.findViewById(R.id.upperHue);
        upperHueWheel = (HorizontalWheelView) root.findViewById(R.id.upperHueWheel);
        upperHueWheel.setEndLock(true);
        upperHueWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().upperHue = (int) Math.round(radians / PI2 * 179);
                setTextValues();
                setUpperThreshold();
                onChanges();
            }
        });

        minSaturation = (TextView) root.findViewById(R.id.minSaturation);
        minSaturationWheel = (HorizontalWheelView) root.findViewById(R.id.minSaturationWheel);
        minSaturationWheel.setEndLock(true);
        minSaturationWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().minSaturation = (int) Math.round(radians / PI2 * 255);
                setTextValues();
                setLowerThreshold();
                setUpperThreshold();
                onChanges();
            }
        });

        minValue = (TextView) root.findViewById(R.id.minValue);
        minValueWheel = (HorizontalWheelView) root.findViewById(R.id.minValueWheel);
        minValueWheel.setEndLock(true);
        minValueWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().minValue = (int) Math.round(radians / PI2 * 255);
                setTextValues();
                setLowerThreshold();
                setUpperThreshold();
                onChanges();
            }
        });

        blur = (TextView) root.findViewById(R.id.blur);
        blurWheel = (HorizontalWheelView) root.findViewById(R.id.blurWheel);
        blurWheel.setEndLock(true);
        blurWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().blur = (int) Math.round(radians / PI2 * 9) * 2 + 3;
                setTextValues();
                onChanges();
            }
        });

        View layers = root.findViewById(R.id.layers);
        layers.setTag(Constants.LAYER_RGBA);
        layers.setOnClickListener(this);
        save = (ImageView) root.findViewById(R.id.save);
        save.setTag(false);
        save.setColorFilter(LIGHT, PorterDuff.Mode.MULTIPLY);
        save.setOnClickListener(this);

        setTextValues();
        setupWheels();

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                /*if ((int) view.getTag() == R.drawable.ic_play_arrow_36dp) {
                    javaCameraView.enableView();
                } else {
                    javaCameraView.disableView();
                }*/
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
                        setLayerImage(view, R.drawable.ic_filter_6_36dp, Constants.LAYER_VALUE);
                        break;
                    case Constants.LAYER_VALUE:
                        setLayerImage(view, R.drawable.ic_filter_7_36dp, Constants.LAYER_RED_FILTERED);
                        break;
                    case Constants.LAYER_RED_FILTERED:
                        setLayerImage(view, R.drawable.ic_filter_8_36dp, Constants.LAYER_BLUR);
                        break;
                    case Constants.LAYER_BLUR:
                        setLayerImage(view, R.drawable.ic_filter_36dp, Constants.LAYER_RGBA);
                        break;
                    case Constants.LAYER_RGBA:
                        setLayerImage(view, R.drawable.ic_filter_1_36dp, Constants.LAYER_HSV);
                        break;
                }
                break;
            case R.id.save:
                if (((boolean) save.getTag())) {
                    save.setTag(false);
                    save.setColorFilter(LIGHT, PorterDuff.Mode.MULTIPLY);
                    prefs.putInteger(Prefs.LOWER_HUE, getActivityMain().lowerHue);
                    prefs.putInteger(Prefs.UPPER_HUE, getActivityMain().upperHue);
                    prefs.putInteger(Prefs.MIN_SATURATION, getActivityMain().minSaturation);
                    prefs.putInteger(Prefs.MIN_VALUE, getActivityMain().minValue);
                    prefs.putInteger(Prefs.BLUR, getActivityMain().blur);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                if (((boolean) save.getTag())) {
                    save.setTag(false);
                    save.setColorFilter(LIGHT, PorterDuff.Mode.MULTIPLY);
                    restoreVars();
                    setTextValues();
                    setLowerThreshold();
                    setUpperThreshold();
                    setupWheels();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void setLayerImage(View view, @DrawableRes int id, int layerType) {
        getActivityMain().layerType = layerType;
        view.setTag(layerType);
        ((ImageView) view).setImageResource(id);
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

    private void setupWheels() {
        lowerHueWheel.setRadiansAngle(getAngle(getActivityMain().lowerHue, 179));
        upperHueWheel.setRadiansAngle(getAngle(getActivityMain().upperHue, 179));
        minSaturationWheel.setRadiansAngle(getAngle(getActivityMain().minSaturation, 255));
        minValueWheel.setRadiansAngle(getAngle(getActivityMain().minValue, 255));
        blurWheel.setRadiansAngle(getAngle((getActivityMain().blur - 3) / 2, 9));
    }

    private void setLowerThreshold() {
        float minSaturation = 1f * getActivityMain().minSaturation / 255;
        float minValue = 1f * getActivityMain().minValue / 255;
        lowerHSV[0] = getActivityMain().lowerHue * 2;
        lowerHSV[1] = minSaturation;
        lowerHSV[2] = minValue;
        minColors[1] = Color.HSVToColor(lowerHSV);
        lowerGradient.setColors(minColors);
        lowerThreshold.setBackground(lowerGradient);
    }

    private void setUpperThreshold() {
        float minSaturation = 1f * getActivityMain().minSaturation / 255;
        float minValue = 1f * getActivityMain().minValue / 255;
        upperHSV[0] = getActivityMain().upperHue * 2;
        upperHSV[1] = minSaturation;
        upperHSV[2] = minValue;
        maxColors[0] = Color.HSVToColor(upperHSV);
        upperGradient.setColors(maxColors);
        upperThreshold.setBackground(upperGradient);
    }

    private void restoreVars() {
        getActivityMain().lowerHue = prefs.getInteger(Prefs.LOWER_HUE, 12);
        getActivityMain().upperHue = prefs.getInteger(Prefs.UPPER_HUE, 168);
        getActivityMain().minSaturation = prefs.getInteger(Prefs.MIN_SATURATION, 50);
        getActivityMain().minValue = prefs.getInteger(Prefs.MIN_VALUE, 100);
        getActivityMain().blur = prefs.getInteger(Prefs.BLUR, 3);
    }

    private void onChanges() {
        if (!((boolean) save.getTag())) {
            save.setTag(true);
            save.setColorFilter(DARK, PorterDuff.Mode.MULTIPLY);
        }
    }

    private double getAngle(int value, int range) {
        return PI2 * value / range;
    }

    private ActivityMain getActivityMain() {
        return (ActivityMain) getActivity();
    }
}
