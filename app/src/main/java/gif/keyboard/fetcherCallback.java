package gif.keyboard;

import org.json.JSONObject;

interface fetcherCallback {
    void onSuccess(JSONObject json);

    void onError(int code);
}