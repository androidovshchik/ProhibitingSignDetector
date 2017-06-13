package ru.dksta.prohibitingsigndetector;

import android.app.Fragment;
import android.graphics.Color;
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

public class FragmentSettings extends Fragment implements View.OnClickListener {

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
        setLowerThreshold();
        setUpperThreshold();

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
                setLowerThreshold();
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
                setUpperThreshold();
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
                setLowerThreshold();
                setUpperThreshold();
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
                setLowerThreshold();
                setUpperThreshold();
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

        View layers = root.findViewById(R.id.layers);
        layers.setTag(Constants.LAYER_DEFAULT);
        layers.setOnClickListener(this);
        root.findViewById(R.id.save).setOnClickListener(this);

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
                        setLayerImage(view, R.drawable.ic_filter_36dp, Constants.LAYER_DEFAULT);
                        break;
                    case Constants.LAYER_DEFAULT:
                        setLayerImage(view, R.drawable.ic_filter_1_36dp, Constants.LAYER_HSV);
                        break;
                }
                break;
            case R.id.save:
                prefs.putInteger(Prefs.LOWER_HUE, getActivityMain().lowerHue);
                prefs.putInteger(Prefs.UPPER_HUE, getActivityMain().upperHue);
                prefs.putInteger(Prefs.MIN_SATURATION, getActivityMain().minSaturation);
                prefs.putInteger(Prefs.MIN_VALUE, getActivityMain().minValue);
                prefs.putInteger(Prefs.BLUR, getActivityMain().blur);
                break;
            default:
                break;
        }
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

    private double getAngle(int value, int range) {
        return PI2 * value / range;
    }

    private ActivityMain getActivityMain() {
        return (ActivityMain) getActivity();
    }
}
