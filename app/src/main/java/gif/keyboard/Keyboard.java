package gif.keyboard;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.SpannableStringBuilder;
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
import com.bumptech.glide.request.Request;
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

public class Keyboard extends InputMethodService {

    private LinearLayout imageHolder;
    private Boolean firstStart = true;
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<Request> requestQueue = new ArrayList<>();

    @Override
    public void onCreate() {
        int style = R.style.AppTheme;


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        switch (pref.getInt("style", 0)) {
            case 0:
                style = R.style.AppTheme;
                break;
            case 1:
                style = R.style.AppThemePink;
                break;
            case 2:
                style = R.style.AppThemeGreen;
                break;
            case 3:
                style = R.style.AppThemeBlack;
                break;
        }

        setTheme(style);

        super.onCreate();
    }

    @Override
    public View onCreateInputView() {

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setBackgroundColor(colorPrimary);
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
        searchQueryTxt.setTextColor(colorPrimary);
        searchQueryTxt.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        searchQueryTxt.setPadding(150, 10, 20, 10);
        searchQueryTxt.setTextSize(22);
        searchQueryTxt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout searchQueryLayout = new LinearLayout(this);
        searchQueryLayout.setOrientation(LinearLayout.HORIZONTAL);
        searchQueryLayout.setBackground(getResources().getDrawable(R.drawable.background_round));
        searchQueryLayout.addView(searchQueryTxt);

        LinearLayout searchQueryWrapperLayout = new LinearLayout(this);
        searchQueryLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        searchQueryWrapperLayout.setPadding(20, 10, 20, 10);
        searchQueryWrapperLayout.addView(searchQueryLayout);

        LinearLayout dialogSearchLine = new LinearLayout(this);

        LinearLayout buttonHolder = new LinearLayout(this);

        final LinearLayout dialogView = new LinearLayout(this);
        dialogView.setVisibility(View.GONE);
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.addView(searchQueryWrapperLayout);
        dialogView.addView(dialogSearchLine);

        Button backspaceBtn = new Button(this);
        backspaceBtn.setLayoutParams(new LinearLayout.LayoutParams(100, 80));
        backspaceBtn.setTextSize(18);
        backspaceBtn.setPadding(10, 0, 10, 20);
        backspaceBtn.setTextColor(colorPrimary);
        backspaceBtn.setBackground(null);
        backspaceBtn.setText("⌫");

        backspaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchQueryTxt.getText().toString();

                if (query.length() > 0) {
                    clearAllRequests();
                    searchQueryTxt.setText(query.subSequence(0, query.length() - 1));
                }
            }
        });

        backspaceBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                searchQueryTxt.setText("");
                return true;
            }
        });

        searchQueryLayout.addView(backspaceBtn);

        ImageButton searchBtn = new ImageButton(this);
        searchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
        searchBtn.setBackgroundResource(R.drawable.background_button);
        searchBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchQueryTxt.getText().toString().replace("␣", " ");
                imageHolder.removeAllViews();
                dialogView.setVisibility(View.VISIBLE);
                if (query.length() > 0) search(Keyboard.this, imageHolder, query, 1);
            }
        });

        ImageButton favoriteBtn = new ImageButton(this);
        favoriteBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
        favoriteBtn.setBackgroundResource(R.drawable.background_button);
        favoriteBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.setVisibility(View.GONE);
                displayFavorites();
            }
        });

        ImageButton recommendedBtn = new ImageButton(this);
        recommendedBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_star));
        recommendedBtn.setBackgroundResource(R.drawable.background_button);
        recommendedBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));
        recommendedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.setVisibility(View.GONE);
                imageHolder.removeAllViews();
                clearAllRequests();
                SpannableStringBuilder builder = new SpannableStringBuilder(getCurrentInputConnection().getTextBeforeCursor(100, 1));
                search(Keyboard.this, imageHolder, builder.toString().replace(" ", "+"), 1);
            }
        });

        ImageButton localStorageBtn = new ImageButton(this);
        localStorageBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_storage));
        localStorageBtn.setBackgroundResource(R.drawable.background_button);
        localStorageBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

        localStorageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.setVisibility(View.GONE);
                displayLocalImages(Keyboard.this, images, imageHolder);
            }
        });

        buttonHolder.addView(recommendedBtn);
        buttonHolder.addView(favoriteBtn);
        buttonHolder.addView(localStorageBtn);
        buttonHolder.addView(searchBtn);
        dialogSearchLine.addView(dialogCharScrollView);


        String chars = getResources().getString(R.string.chars);
        for (int i = 0; i < chars.length(); i++) {
            final Button charBtn = new Button(this);
            charBtn.setText(String.valueOf(chars.charAt(i)));
            charBtn.setTextColor(getResources().getColor(android.R.color.white));
            charBtn.setBackgroundResource(R.drawable.background_button);
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
                Toast.makeText(this, R.string.noGifsFound, Toast.LENGTH_SHORT).show();
            } else {
                System.out.print(images.size());
            }
        }

        if (firstStart) displayLocalImages(this, images, imageHolder);

        firstStart = false;

        super.onStartInputView(info, restarting);
    }

    private void displayLocalImages(final Context context, ArrayList<String> images, final LinearLayout layout) {

        clearAllRequests();
        layout.removeAllViews();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.noStoragePermission, Toast.LENGTH_SHORT).show();
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

    private void search(final Context context, final LinearLayout layout, final String query, final int page) {
        new Fetcher(getApplicationContext(), getResources().getString(R.string.api_search) + "?api_key=" + getResources().getString(R.string.apiKey) + "&q=" + query + "&offset=" + (page - 1) * 25, "GET", new FetcherCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    final JSONArray data = json.getJSONArray("data");
                    final JSONObject pagination = json.getJSONObject("pagination");

                    if (data.length() == 0) {
                        Toast.makeText(context, getResources().getString(R.string.noGifsFoundOnline).replace("{query}", query), Toast.LENGTH_SHORT).show();
                    } else {
                        int totalItems = pagination.getInt("total_count");
                        int maxPage = (totalItems / 25);
                        if (totalItems % 25 > 0) maxPage++;

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
                                    if (Build.VERSION.SDK_INT > 22) {
                                        Drawable favoriteDrawableRaw = getDrawable(R.drawable.ic_favorite);
                                        if (favoriteDrawableRaw != null) {
                                            Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);


                                            bitmap.eraseColor(Color.argb(80, 0, 0, 0));

                                            Canvas canvas = new Canvas(bitmap);

                                            float h = 100;
                                            float w = ((float) img.getDrawable().getIntrinsicHeight() / (float) img.getDrawable().getIntrinsicWidth()) * h;
                                            int cw = canvas.getWidth();
                                            int ch = canvas.getHeight();

                                            favoriteDrawableRaw.setBounds((int) (cw / 2 - w / 2), (int) (ch / 2 - h / 2), (int) (cw / 2 + w / 2), (int) (ch / 2 + h / 2));
                                            favoriteDrawableRaw.draw(canvas);
                                            Drawable favoriteDrawable = new BitmapDrawable(getResources(), bitmap);
                                            favoriteDrawable.setColorFilter(getColor(R.color.pink), PorterDuff.Mode.MULTIPLY);

                                            img.setForeground(favoriteDrawable);

                                            new android.os.Handler().postDelayed(new Runnable() {
                                                @TargetApi(23)
                                                public void run() {
                                                    img.setForeground(null);
                                                }
                                            }, 1000);
                                        }
                                    } else {
                                        Toast.makeText(Keyboard.this, "♥", Toast.LENGTH_SHORT).show();
                                    }
                                    return true;
                                }
                            });

                            setAndLoadImage(context, layout, img, url, 0);
                        }

                        if (maxPage > page) {

                            final Button nextPageButton = new Button(context);
                            nextPageButton.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
                            nextPageButton.setText(getResources().getString(R.string.more));
                            nextPageButton.setBackground(getResources().getDrawable(R.drawable.background_button));
                            nextPageButton.setTextColor(getResources().getColor(android.R.color.white));
                            nextPageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    nextPageButton.setText(getResources().getString(R.string.page).replace("{page}", String.valueOf(page + 1)));
                                    nextPageButton.setEnabled(false);
                                    search(context, layout, query, page + 1);
                                }
                            });

                            layout.addView(nextPageButton);
                        }
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

        clearAllRequests();
        imageHolder.removeAllViews();

        if (set != null) {

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

    private void clearAllRequests() {
        for (int i = 0; i < requestQueue.size(); i++) {
            requestQueue.get(i).clear();
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

        Request request = Glide.with(getApplicationContext()).asFile().apply(new RequestOptions().timeout(30000)).load(url).listener(new RequestListener<File>() {
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
        }).submit().getRequest();

        requestQueue.add(request);
    }
}