package gif.keyboard;

import org.json.JSONObject;

interface FetcherCallback {
    void onSuccess(JSONObject json);

    void onError(int code);
}

interface ImageCallback {
    void onLoaded();
}