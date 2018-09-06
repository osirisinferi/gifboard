package gif.keyboard;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

class Fetcher {

    private Context context;
    private String url;
    private String method;
    private FetcherCallback callback;


    Fetcher(Context context, String url, String method, FetcherCallback callback) {
        this.method = method;
        this.context = context;
        this.url = url;
        this.callback = callback;
    }

    protected void request() {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        int method = Request.Method.GET;
        if (this.method.equals("POST")) method = Request.Method.POST;

        StringRequest stringRequest = new StringRequest(method, this.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);

                            if (json.getJSONObject("meta").getInt("status") != 200) {
                                callback.onError(400);
                            } else {
                                callback.onSuccess(json);
                            }
                        } catch (JSONException err) {
                            callback.onError(500);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(500);
            }
        }
        );

        queue.add(stringRequest);
    }
}