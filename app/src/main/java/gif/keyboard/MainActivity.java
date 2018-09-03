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
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private int doneColor;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doneColor = getResources().getColor(R.color.greenDone);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.settings));

        final Spinner qualitySpinner = findViewById(R.id.qualitySpinner);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int position;

        switch (pref.getString("quality", "downsized")) {
            case "downsized_small":
                position = 1;
                break;
            case "downsized_medium":
                position = 2;
                break;
            case "preview_gif":
                position = 3;
                break;
            case "original":
                position = 4;
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
                        quality = "downsized_small";
                        break;
                    case 2:
                        quality = "downsized_medium";
                        break;
                    case 3:
                        quality = "preview_gif";
                        break;
                    case 4:
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
    }

    public void openKeyboardSettings(View v) {
        startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
    }

    public void askForStorageReadPermission(View v) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onResume() {
        Button enableKeyboardBtn = findViewById(R.id.enableKeyboardBtn);

        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (im != null) {
            String list = im.getEnabledInputMethodList().toString();
            if (list.contains(BuildConfig.APPLICATION_ID)) {
                enableKeyboardBtn.setBackgroundColor(doneColor);
                enableKeyboardBtn.setEnabled(false);
            }
        }

        Button askForStorageReadPermissionBtn = findViewById(R.id.askForStorageReadPermissionBtn);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            askForStorageReadPermissionBtn.setBackgroundColor(doneColor);
            askForStorageReadPermissionBtn.setEnabled(false);
        }

        super.onResume();
    }
}
