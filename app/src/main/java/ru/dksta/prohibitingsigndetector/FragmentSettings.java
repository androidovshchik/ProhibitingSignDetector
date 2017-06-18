package ru.dksta.prohibitingsigndetector;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
    private static final int LIGHT = Color.parseColor("#b5b5b5");
    private static final int DARK = Color.parseColor("#202020");

    private Prefs prefs;

    private View lowerThreshold;
    private View upperThreshold;

    private float[] lowerHSV;
    private float[] upperHSV;

    private TextView lowerHue;
    private TextView upperHue;
    private TextView minSaturation;
    private TextView minValue;
    private TextView blur;
    private TextView minArea;
    private TextView minCircularity;
    private TextView minInertiaRatio;

    private HorizontalWheelView lowerHueWheel;
    private HorizontalWheelView upperHueWheel;
    private HorizontalWheelView minSaturationWheel;
    private HorizontalWheelView minValueWheel;
    private HorizontalWheelView blurWheel;
    private HorizontalWheelView minAreaWheel;
    private HorizontalWheelView minCircularityWheel;
    private HorizontalWheelView minInertiaRatioWheel;

    private ImageView play;
    private ImageView rotate;
    private ImageView showInfo;
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

        lowerHSV = new float[3];
        lowerHSV[1] = 1f;
        lowerHSV[2] = 1f;
        upperHSV = new float[3];
        upperHSV[1] = 1f;
        upperHSV[2] = 1f;
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

        minArea = (TextView) root.findViewById(R.id.minArea);
        minAreaWheel = (HorizontalWheelView) root.findViewById(R.id.minAreaWheel);
        minAreaWheel.setEndLock(true);
        minAreaWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                getActivityMain().minArea = (int) Math.round(radians / PI2 * 480);
                setTextValues();
                onChanges();
            }
        });

        minCircularity = (TextView) root.findViewById(R.id.minCircularity);
        minCircularityWheel = (HorizontalWheelView) root.findViewById(R.id.minCircularityWheel);
        minCircularityWheel.setEndLock(true);
        minCircularityWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                radians = radians / PI2 * 100;
                radians = Math.round(radians);
                getActivityMain().minCircularity = (float) radians / 100;
                setTextValues();
                onChanges();
            }
        });

        minInertiaRatio = (TextView) root.findViewById(R.id.minInertiaRatio);
        minInertiaRatioWheel = (HorizontalWheelView) root.findViewById(R.id.minInertiaRatioWheel);
        minInertiaRatioWheel.setEndLock(true);
        minInertiaRatioWheel.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                radians = radians / PI2 * 100;
                radians = Math.round(radians);
                getActivityMain().minInertiaRatio = (float) radians / 100;
                setTextValues();
                onChanges();
            }
        });

        setTextValues();
        setupWheels();

        play = (ImageView) root.findViewById(R.id.play);
        play.setTag(false);
        play.setOnClickListener(this);
        View layers = root.findViewById(R.id.layers);
        layers.setTag(Constants.LAYER_RGBA);
        layers.setOnClickListener(this);
        View noise = root.findViewById(R.id.noise);
        noise.setTag(Constants.NOISE_NONE);
        noise.setOnClickListener(this);
        rotate = (ImageView) root.findViewById(R.id.rotate);
        rotate.setOnClickListener(this);
        showInfo = (ImageView) root.findViewById(R.id.showInfo);
        showInfo.setOnClickListener(this);
        save = (ImageView) root.findViewById(R.id.save);
        save.setTag(false);
        save.setColorFilter(LIGHT, PorterDuff.Mode.MULTIPLY);
        save.setOnClickListener(this);
        save.setOnLongClickListener(this);

        setupButtons();

        return root;
    }

    public void onPlayEvent(boolean playEvent) {
        play.setTag(playEvent);
        play.setImageResource(playEvent ? R.drawable.ic_pause_black_36dp :
                R.drawable.ic_play_arrow_36dp);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                if (getActivityMain().triggerPlayEvent(!((boolean) view.getTag()))) {
                    play.setTag(!((boolean) view.getTag()));
                    play.setImageResource((boolean) view.getTag() ? R.drawable.ic_pause_black_36dp :
                            R.drawable.ic_play_arrow_36dp);
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
                    default:
                        break;
                }
                break;
            case R.id.noise:
                switch ((int) view.getTag()) {
                    case Constants.NOISE_SALT_PEPPER:
                        setNoiseImage(view, R.drawable.ic_photo_36dp, Constants.NOISE_NONE);
                        break;
                    case Constants.NOISE_NONE:
                        setNoiseImage(view, R.drawable.ic_looks_one_36dp, Constants.NOISE_SALT_PEPPER);
                        break;
                    default:
                        break;
                }
                break;
            case R.id.rotate:
                onChanges();
                getActivityMain().rotateMat = !getActivityMain().rotateMat;
                setupButtons();
                break;
            case R.id.showInfo:
                onChanges();
                getActivityMain().showInfo = !getActivityMain().showInfo;
                setupButtons();
                break;
            case R.id.save:
                if (((boolean) save.getTag())) {
                    save.setTag(false);
                    save.setColorFilter(LIGHT, PorterDuff.Mode.MULTIPLY);
                    prefs.putBoolean(Prefs.ROTATE_MAT, getActivityMain().rotateMat);
                    prefs.putBoolean(Prefs.SHOW_INFO, getActivityMain().showInfo);
                    prefs.putInteger(Prefs.LOWER_HUE, getActivityMain().lowerHue);
                    prefs.putInteger(Prefs.UPPER_HUE, getActivityMain().upperHue);
                    prefs.putInteger(Prefs.MIN_SATURATION, getActivityMain().minSaturation);
                    prefs.putInteger(Prefs.MIN_VALUE, getActivityMain().minValue);
                    prefs.putInteger(Prefs.BLUR, getActivityMain().blur);
                    prefs.putInteger(Prefs.MIN_AREA, getActivityMain().minArea);
                    prefs.putFloat(Prefs.MIN_CIRCULARITY, getActivityMain().minCircularity);
                    prefs.putFloat(Prefs.MIN_INERTIA_RATIO, getActivityMain().minInertiaRatio);
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
                    restoreVars();
                    setTextValues();
                    setLowerThreshold();
                    setUpperThreshold();
                    setupWheels();
                    setupButtons();
                    save.setTag(false);
                    save.setColorFilter(LIGHT, PorterDuff.Mode.MULTIPLY);
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

    private void setNoiseImage(View view, @DrawableRes int id, int noiseType) {
        getActivityMain().noiseType = noiseType;
        view.setTag(noiseType);
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
        minArea.setText(getString(R.string.text_min_area,
                getActivityMain().minArea));
        minCircularity.setText(getString(R.string.text_min_circularity,
                getActivityMain().minCircularity));
        minInertiaRatio.setText(getString(R.string.text_min_inertia_ratio,
                getActivityMain().minInertiaRatio));
    }

    private void setupWheels() {
        lowerHueWheel.setRadiansAngle(getAngle(getActivityMain().lowerHue, 179));
        upperHueWheel.setRadiansAngle(getAngle(getActivityMain().upperHue, 179));
        minSaturationWheel.setRadiansAngle(getAngle(getActivityMain().minSaturation, 255));
        minValueWheel.setRadiansAngle(getAngle(getActivityMain().minValue, 255));
        blurWheel.setRadiansAngle(getAngle((getActivityMain().blur - 3) / 2, 9));
        minAreaWheel.setRadiansAngle(getAngle(getActivityMain().minArea, 480));
        minCircularityWheel.setRadiansAngle(getActivityMain().minCircularity * PI2);
        minInertiaRatioWheel.setRadiansAngle(getActivityMain().minInertiaRatio * PI2);
    }

    private void setLowerThreshold() {
        lowerHSV[0] = getActivityMain().lowerHue * 2;
        lowerThreshold.setBackgroundColor(Color.HSVToColor(lowerHSV));
    }

    private void setUpperThreshold() {
        upperHSV[0] = getActivityMain().upperHue * 2;
        upperThreshold.setBackgroundColor(Color.HSVToColor(upperHSV));
    }

    private void setupButtons() {
        rotate.setTag(getActivityMain().rotateMat);
        rotate.setColorFilter(getActivityMain().rotateMat ? DARK : LIGHT,
                PorterDuff.Mode.MULTIPLY);
        showInfo.setTag(getActivityMain().showInfo);
        showInfo.setColorFilter(getActivityMain().showInfo ? DARK : LIGHT,
                PorterDuff.Mode.MULTIPLY);
    }

    private void restoreVars() {
        getActivityMain().rotateMat = prefs.getBoolean(Prefs.ROTATE_MAT, false);
        getActivityMain().showInfo = prefs.getBoolean(Prefs.SHOW_INFO, true);
        getActivityMain().lowerHue = prefs.getInteger(Prefs.LOWER_HUE, 4);
        getActivityMain().upperHue = prefs.getInteger(Prefs.UPPER_HUE, 165);
        getActivityMain().minSaturation = prefs.getInteger(Prefs.MIN_SATURATION, 80);
        getActivityMain().minValue = prefs.getInteger(Prefs.MIN_VALUE, 32);
        getActivityMain().blur = prefs.getInteger(Prefs.BLUR, 3);
        getActivityMain().minArea = prefs.getInteger(Prefs.MIN_AREA, 80);
        getActivityMain().minCircularity = prefs.getFloat(Prefs.MIN_CIRCULARITY, 0.7f);
        getActivityMain().minInertiaRatio = prefs.getFloat(Prefs.MIN_INERTIA_RATIO, 0.3f);
    }

    private void onChanges() {
        if (save != null && !((boolean) save.getTag())) {
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
