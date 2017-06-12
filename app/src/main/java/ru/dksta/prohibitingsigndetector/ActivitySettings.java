package ru.dksta.prohibitingsigndetector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import ru.dksta.prohibitingsigndetector.utils.Prefs;

public class ActivitySettings extends Activity implements View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = new Prefs(getApplicationContext());
        CheckBox rotation = (CheckBox) findViewById(R.id.rotation);
        rotation.setChecked(prefs.getBoolean(Prefs.ROTATE_MAT));
        rotation.setOnCheckedChangeListener(this);
        CheckBox showInfo = (CheckBox) findViewById(R.id.showInfo);
        showInfo.setChecked(prefs.getBoolean(Prefs.SHOW_INFO, true));
        showInfo.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
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
