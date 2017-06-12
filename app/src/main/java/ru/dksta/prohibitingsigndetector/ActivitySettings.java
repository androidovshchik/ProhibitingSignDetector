package ru.dksta.prohibitingsigndetector;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import ru.dksta.prohibitingsigndetector.utils.Prefs;

public class ActivitySettings extends Activity implements View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private Prefs prefs;

    private CheckBox allowChanges;

    private View minThreshold;
    private View upperThreshold;
    private View lowerThreshold;
    private View maxThreshold;

    private TextView lowerHue;
    private TextView upperHue;
    private TextView saturation;
    private TextView blur;

    private int lowerHueValue;
    private int upperHueValue;
    private int saturationValue;
    private int blurValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = new Prefs(getApplicationContext());
        minThreshold = findViewById(R.id.minThreshold);
        lowerThreshold = findViewById(R.id.lowerThreshold);
        upperThreshold = findViewById(R.id.upperThreshold);
        maxThreshold = findViewById(R.id.maxThreshold);
        lowerHue = (TextView) findViewById(R.id.lowerHue);
        upperHue = (TextView) findViewById(R.id.upperHue);
        saturation = (TextView) findViewById(R.id.saturation);
        blur = (TextView) findViewById(R.id.blur);
        lowerHueValue = prefs.getInteger(Prefs.LOWER_HUE, 12);
        upperHueValue = prefs.getInteger(Prefs.UPPER_HUE, 168);
        saturationValue = prefs.getInteger(Prefs.SATURATION, 50);
        blurValue = prefs.getInteger(Prefs.BLUR, 3);
        setCaptions();
        setColors();
        allowChanges = (CheckBox) findViewById(R.id.allowChanges);
        findViewById(R.id.lowerHueUp).setOnClickListener(this);
        findViewById(R.id.lowerHueDown).setOnClickListener(this);
        findViewById(R.id.upperHueUp).setOnClickListener(this);
        findViewById(R.id.upperHueDown).setOnClickListener(this);
        findViewById(R.id.saturationUp).setOnClickListener(this);
        findViewById(R.id.saturationDown).setOnClickListener(this);
        findViewById(R.id.blurUp).setOnClickListener(this);
        findViewById(R.id.blurDown).setOnClickListener(this);
        CheckBox rotation = (CheckBox) findViewById(R.id.rotation);
        rotation.setChecked(prefs.getBoolean(Prefs.ROTATE_MAT));
        rotation.setOnCheckedChangeListener(this);
        CheckBox showInfo = (CheckBox) findViewById(R.id.showInfo);
        showInfo.setChecked(prefs.getBoolean(Prefs.SHOW_INFO, true));
        showInfo.setOnCheckedChangeListener(this);
    }

    private void setCaptions() {
        lowerHue.setText(getString(R.string.button_lower_hue, lowerHueValue));
        upperHue.setText(getString(R.string.button_upper_hue, upperHueValue));
        saturation.setText(getString(R.string.button_saturation, saturationValue));
        blur.setText(getString(R.string.button_blur, blurValue));
    }

    private void setColors() {
        float saturation = 1f * saturationValue / 255;
        minThreshold.setBackgroundColor(Color.HSVToColor(new float[] {0, saturation, 1}));
        lowerThreshold.setBackgroundColor(Color.HSVToColor(new float[] {lowerHueValue * 2,
                saturation, 1}));
        upperThreshold.setBackgroundColor(Color.HSVToColor(new float[] {upperHueValue * 2,
                saturation, 1}));
        maxThreshold.setBackgroundColor(Color.HSVToColor(new float[] {358, saturation, 1}));
    }

    @Override
    public void onClick(View view) {
        if (!allowChanges.isChecked()) {
            return;
        }
        switch (view.getId()) {
            case R.id.lowerHueUp:
                lowerHueValue += lowerHueValue < 179 ? 1 : 0;
                break;
            case R.id.lowerHueDown:
                lowerHueValue -= lowerHueValue > 0 ? 1 : 0;
                break;
            case R.id.upperHueUp:
                upperHueValue += upperHueValue < 179 ? 1 : 0;
                break;
            case R.id.upperHueDown:
                upperHueValue -= upperHueValue > 0 ? 1 : 0;
                break;
            case R.id.saturationUp:
                saturationValue += saturationValue < 255 ? 1 : 0;
                break;
            case R.id.saturationDown:
                saturationValue -= saturationValue > 0 ? 1 : 0;
                break;
            case R.id.blurUp:
                blurValue += 2;
                break;
            case R.id.blurDown:
                blurValue -= blurValue > 3 ? 2 : 0;
                break;
            default:
                break;
        }
        setCaptions();
        setColors();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rotation:
                prefs.putBoolean(Prefs.ROTATE_MAT, isChecked);
                break;
            case R.id.showInfo:
                prefs.putBoolean(Prefs.SHOW_INFO, isChecked);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.putInteger(Prefs.LOWER_HUE, lowerHueValue);
        prefs.putInteger(Prefs.UPPER_HUE, upperHueValue);
        prefs.putInteger(Prefs.SATURATION, saturationValue);
        prefs.putInteger(Prefs.BLUR, blurValue);
    }
}
