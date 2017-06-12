package ru.dksta.prohibitingsigndetector;

import android.app.Activity;
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

    private TextView lowerVue;
    private TextView upperVue;
    private TextView saturation;
    private TextView blur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = new Prefs(getApplicationContext());
        lowerVue = (TextView) findViewById(R.id.lowerVue);
        upperVue = (TextView) findViewById(R.id.upperVue);
        saturation = (TextView) findViewById(R.id.saturation);
        blur = (TextView) findViewById(R.id.blur);
        setCaptions(prefs.getInteger(Prefs.LOWER_VUE, 0), prefs.getInteger(Prefs.UPPER_VUE, 0),
                prefs.getInteger(Prefs.SATURATION, 0), prefs.getInteger(Prefs.BLUR, 0));
        findViewById(R.id.lowerVueUp).setOnClickListener(this);
        findViewById(R.id.lowerVueDown).setOnClickListener(this);
        findViewById(R.id.upperVueUp).setOnClickListener(this);
        findViewById(R.id.upperVueDown).setOnClickListener(this);
        findViewById(R.id.saturationUp).setOnClickListener(this);
        findViewById(R.id.saturationDown).setOnClickListener(this);
        findViewById(R.id.blurUp).setOnClickListener(this);
        findViewById(R.id.blurDown).setOnClickListener(this);
        allowChanges = (CheckBox) findViewById(R.id.allowChanges);
        CheckBox rotation = (CheckBox) findViewById(R.id.rotation);
        rotation.setChecked(prefs.getBoolean(Prefs.ROTATE_MAT));
        rotation.setOnCheckedChangeListener(this);
        CheckBox showInfo = (CheckBox) findViewById(R.id.showInfo);
        showInfo.setChecked(prefs.getBoolean(Prefs.SHOW_INFO, true));
        showInfo.setOnCheckedChangeListener(this);
    }

    private void setCaptions(int lowerVue, int upperVue, int saturation, int blur) {
        this.lowerVue.setText(getString(R.string.button_lower_vue, lowerVue));
        this.upperVue.setText(getString(R.string.button_upper_vue, upperVue));
        this.saturation.setText(getString(R.string.button_saturation, saturation));
        this.blur.setText(getString(R.string.button_blur, blur));
    }

    @Override
    public void onClick(View view) {
        if (!allowChanges.isChecked()) {
            return;
        }
        switch (view.getId()) {
            default:
                break;
        }
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
}
