package gif.keyboard;

import android.Manifest;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class keyboard extends InputMethodService {

    private LinearLayout imageHolder;
    private Boolean firstStart = true;
    private ArrayList<String> images = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public View onCreateInputView() {

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setPadding(5, 10, 5, 5);

        imageHolder = new LinearLayout(this);
        imageHolder.setOrientation(LinearLayout.HORIZONTAL);
        imageHolder.setHorizontalGravity(Gravity.START);
        imageHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 310));
        scrollView.addView(imageHolder);

        final LinearLayout dialogCharView = new LinearLayout(this);

        final HorizontalScrollView dialogCharScrollView = new HorizontalScrollView(this);
        dialogCharScrollView.addView(dialogCharView);

        final TextView searchQueryTxt = new TextView(this);
        searchQueryTxt.setTextColor(getResources().getColor(android.R.color.white));
        searchQueryTxt.setTextSize(22);
        searchQueryTxt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout dialogSearchLine = new LinearLayout(this);

        LinearLayout buttonHolder = new LinearLayout(this);

        final LinearLayout dialogView = new LinearLayout(this);
        dialogView.setVisibility(View.GONE);

        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.addView(searchQueryTxt);
        dialogView.addView(dialogSearchLine);

        Button backspace = new Button(this);
        backspace.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        backspace.setText("⌫");

        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchQueryTxt.getText().toString();

                if (query.length() > 0) {
                    searchQueryTxt.setText(query.subSequence(0, query.length() - 1));
                }
            }
        });

        backspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                searchQueryTxt.setText("");
                return true;
            }
        });

        ImageButton searchBtn = new ImageButton(this);
        searchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
        TypedValue searchButtonValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, searchButtonValue, true);
        searchBtn.setBackground(getResources().getDrawable(searchButtonValue.resourceId));
        searchBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchQueryTxt.getText().toString();
                imageHolder.removeAllViews();
                dialogView.setVisibility(View.VISIBLE);
                if (query.length() > 0) search(keyboard.this, imageHolder, query);
                else search(keyboard.this, imageHolder, ".");
            }
        });

        ImageButton favoriteBtn = new ImageButton(this);
        favoriteBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
        TypedValue favoriteBtnValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, favoriteBtnValue, true);
        favoriteBtn.setBackground(getResources().getDrawable(favoriteBtnValue.resourceId));
        favoriteBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.setVisibility(View.GONE);
                displayFavorites();
            }
        });

        ImageButton settingsBtn = new ImageButton(this);
        settingsBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings));
        TypedValue settingsBtnValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, settingsBtnValue, true);
        settingsBtn.setBackground(getResources().getDrawable(settingsBtnValue.resourceId));
        settingsBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(keyboard.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton localStorageBtn = new ImageButton(this);
        localStorageBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_storage));
        TypedValue localStorageBtnValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, localStorageBtnValue, true);
        localStorageBtn.setBackground(getResources().getDrawable(localStorageBtnValue.resourceId));
        localStorageBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        localStorageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.setVisibility(View.GONE);
                displayLocalImages(keyboard.this, images, imageHolder);
            }
        });

        buttonHolder.addView(settingsBtn);
        buttonHolder.addView(favoriteBtn);
        buttonHolder.addView(localStorageBtn);
        buttonHolder.addView(searchBtn);

        dialogSearchLine.addView(backspace);
        dialogSearchLine.addView(dialogCharScrollView);


        String chars = " ABCDEFGHIJKLNMOPQRSTUVWXYZ";
        for (int i = 0; i < chars.length(); i++) {
            final Button charBtn = new Button(this);
            charBtn.setText(String.valueOf(chars.charAt(i)));
            charBtn.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            dialogCharView.addView(charBtn);


            charBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String query = searchQueryTxt.getText().toString() + charBtn.getText();
                    searchQueryTxt.setText(query);
                }
            });
        }

        mainLayout.addView(scrollView);
        mainLayout.addView(dialogView);
        mainLayout.addView(buttonHolder);

        return mainLayout;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            images = indexImages();

            if (images.size() == 0) {
                Toast.makeText(this, R.string.noGifsFound, Toast.LENGTH_LONG).show();
            } else {
                System.out.print(images.size());
            }
        }

        if (firstStart) displayLocalImages(this, images, imageHolder);

        firstStart = false;

        super.onStartInputView(info, restarting);
    }

    private void displayLocalImages(final Context context, ArrayList<String> images, final LinearLayout layout) {

        layout.removeAllViews();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.noStoragePermission, Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < images.size(); i++) {

            final String filePath = images.get(i);

            final ImageView img = new ImageView(context);
            img.setPadding(10, 5, 10, 5);


            Glide.with(context).asGif().load("file://" + filePath).addListener(new RequestListener<GifDrawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                    float width = resource.getIntrinsicWidth();
                    float height = resource.getIntrinsicHeight();

                    img.setLayoutParams(new LinearLayout.LayoutParams((int) ((width / height) * 300), 300));

                    return false;
                }
            }).into(img);
            layout.addView(img);

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, new File(filePath));
                    commitGifImage(uri);
                }
            });
        }
    }

    private void commitGifImage(@NonNull Uri contentUri) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(contentUri, new ClipDescription("", new String[]{"image/gif"}), null);
        InputConnection inputConnection = getCurrentInputConnection();
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, null);
    }

    private ArrayList<String> indexImages() {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);
                if (absolutePathOfImage.endsWith(".gif"))
                    listOfAllImages.add(absolutePathOfImage);
            }

            cursor.close();
        }

        return listOfAllImages;
    }

    private void search(final Context context, final LinearLayout layout, String query) {
        new fetcher(getApplicationContext(), getResources().getString(R.string.api_search) + "?api_key=" + getResources().getString(R.string.apiKey) + "&q=" + query, "GET", new fetcherCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    final JSONArray data = json.getJSONArray("data");

                    if (data.length() == 0)
                        Toast.makeText(context, R.string.noGifsFoundOnline, Toast.LENGTH_LONG).show();

                    for (int i = 0; i < data.length(); i++) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                        String quality = pref.getString("quality", "downsized");

                        JSONObject item = data.getJSONObject(i);
                        JSONObject images = item.getJSONObject("images");
                        JSONObject imageItem = images.getJSONObject(quality);
                        final String url = imageItem.getString("url");

                        final ImageView img = new ImageView(context);

                        layout.addView(img);

                        img.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                addToFavorites(url);
                                Toast.makeText(context, "♥", Toast.LENGTH_LONG).show();
                                return true;
                            }
                        });

                        setAndLoadImage(context, layout, img, url, 0);
                    }
                } catch (JSONException err) {
                    // TO-DO
                }
            }

            @Override
            public void onError(int code) {

            }
        }).request();
    }

    private void displayFavorites() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> set = pref.getStringSet("favorites", null);

        if (set != null) {
            imageHolder.removeAllViews();

            List<String> list = new ArrayList<>(set);

            for (int i = 0; i < list.size(); i++) {
                final String url = list.get(i);
                final ImageView img = new ImageView(this);
                imageHolder.addView(img);

                img.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        imageHolder.removeView(img);
                        removeFromFavorites(url);
                        return true;
                    }
                });

                setAndLoadImage(this, imageHolder, img, url, 0);
            }
        }
    }

    private void removeFromFavorites(String url) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> set = pref.getStringSet("favorites", null);
        if (set != null) {
            List<String> list = new ArrayList<>(set);

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(url)) list.remove(i);
            }

            Set<String> newSet = new HashSet<>(list);

            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet("favorites", newSet);
            editor.apply();
        }
    }

    private void addToFavorites(String url) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> set = pref.getStringSet("favorites", null);
        if (set == null) {
            set = new HashSet<>();
        }

        set.add(url);

        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet("favorites", set);
        editor.apply();
    }

    private void setAndLoadImage(final Context context, final LinearLayout layout, final ImageView img, final String url, final int count) {
        img.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        img.setPadding(10, 5, 10, 5);

        Bitmap bm = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        bm.eraseColor(getResources().getColor(android.R.color.black));
        img.setImageBitmap(bm);

        Glide.with(getApplicationContext()).asFile().apply(new RequestOptions().timeout(30000)).load(url).listener(new RequestListener<File>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                if (count < 3) setAndLoadImage(context, layout, img, url, count + 1);
                else layout.removeView(img);
                return false;
            }

            @Override
            public boolean onResourceReady(final File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {

                Glide.with(context).asGif().load(resource).addListener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable drawable, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        float width = drawable.getIntrinsicWidth();
                        float height = drawable.getIntrinsicHeight();

                        img.setLayoutParams(new LinearLayout.LayoutParams((int) ((width / height) * 300), 300));

                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                File gifFile = new File(resource.getAbsolutePath() + ".gif");

                                if (resource.renameTo(gifFile) || gifFile.exists()) {
                                    Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, gifFile);
                                    commitGifImage(uri);
                                }
                            }
                        });

                        return false;
                    }
                }).into(img);

                return false;
            }
        }).submit();
    }


}