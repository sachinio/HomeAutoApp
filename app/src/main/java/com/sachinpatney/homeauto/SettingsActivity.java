package com.sachinpatney.homeauto;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by spatney on 1/8/2016.
 */
public class SettingsActivity extends AppCompatActivity{
    public static final String PREFS_NAME = "homeuto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String external_ip = settings.getString("external_ip", "None");
        String local_ip = settings.getString("local_ip", "None");

        ((EditText)findViewById(R.id.local_ip_edit)).setText(local_ip);
        ((EditText)findViewById(R.id.external_ip_edit)).setText(external_ip);
        initEditTexts();
    }

    private void initEditTexts(){
        ((EditText)findViewById(R.id.local_ip_edit)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("local_ip", s.toString());
                editor.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        ((EditText)findViewById(R.id.external_ip_edit)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("external_ip", s.toString());
                editor.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
    }
}
