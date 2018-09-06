package gif.keyboard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.settings));

        final Spinner qualitySpinner = findViewById(R.id.qualitySpinner);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        int position;

        switch (pref.getString("quality", "downsized")) {
            case "downsized_medium":
                position = 1;
                break;
            case "preview_gif":
                position = 2;
                break;
            case "original":
                position = 3;
                break;
            default:
                position = 0;
        }

        qualitySpinner.setSelection(position);

        qualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String quality;
                switch (position) {
                    case 1:
                        quality = "downsized_medium";
                        break;
                    case 2:
                        quality = "preview_gif";
                        break;
                    case 3:
                        quality = "original";
                        break;
                    default:
                        quality = "downsized";
                }

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("quality", quality);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RadioButton blueThemeBtn = findViewById(R.id.blueThemeBtn);
        RadioButton pinkThemeBtn = findViewById(R.id.pinkThemeBtn);
        RadioButton greenThemeBtn = findViewById(R.id.greenThemeBtn);
        RadioButton blackThemeBtn = findViewById(R.id.blackThemeBtn);

        blueThemeBtn.setChecked(false);
        pinkThemeBtn.setChecked(false);
        greenThemeBtn.setChecked(false);
        blackThemeBtn.setChecked(false);

        switch (pref.getInt("style", 0)) {
            case 0:
                blueThemeBtn.setChecked(true);
                break;
            case 1:
                pinkThemeBtn.setChecked(true);
                break;
            case 2:
                greenThemeBtn.setChecked(true);
                break;
            case 3:
                blackThemeBtn.setChecked(true);
                break;
        }

        blueThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("style", 0);
                editor.apply();

            }
        });

        pinkThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("style", 1);
                editor.apply();
            }
        });

        greenThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("style", 2);
                editor.apply();
            }
        });

        blackThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("style", 3);
                editor.apply();
            }
        });
    }

    public void openKeyboardSettings(View v) {
        startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
    }

    public void askForStorageReadPermission(View v) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onResume() {
        LinearLayout firstStepView = findViewById(R.id.firstStepView);
        LinearLayout secondStepView = findViewById(R.id.secondStepView);

        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (im != null) {
            String list = im.getEnabledInputMethodList().toString();
            if (list.contains(BuildConfig.APPLICATION_ID)) {
                firstStepView.setVisibility(View.GONE);
                secondStepView.setVisibility(View.VISIBLE);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            secondStepView.setVisibility(View.GONE);
        }

        super.onResume();
    }
}
