package traffic.guru.prediction;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephineosoro on 20/06/16.
 */
public class Post {

    public static String baseURL = "http://169.254.85.197";

    public static void PostData(String url, JSONObject parameter, Response.Listener<JSONObject> response) {

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, baseURL + url, parameter,
                response, new Error()) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
                setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
                headers.put("Content-Type", "application/json; charset=utf-8");

                String creds = String.format("%s:%s","odhiamborobinson@hotmail.com","powerpoint1994");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
//                Log.e("category id", getIntent().getStringExtra("category_id"));
//                params.put("categoryId", 2 + "");


                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
        Log.e("request is", req.toString());

    }

    public static void PostString(String url, final Map<String, String> params,Response.Listener<String> response) {

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response, new Error()) {

            @Override
            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                Log.e("category id", getIntent().getStringExtra("category_id"));
//                params.put("id",parameter);


                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
        Log.e("request is", req.toString());

    }

    public static void getData(String url, Response.Listener<String> response) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response, new Error());
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest);
        Log.e("request is", stringRequest.toString());
    }

    private static class Error implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && response != null) {
                try {
                    String res = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    // Now you can use any deserializer to make sense of data
                    JSONObject obj = new JSONObject(res);
                    Log.e("obj", obj.toString());
                } catch (UnsupportedEncodingException e1) {
                    // Couldn't properly decode data to string
                    Log.e("e1", e1.toString());
                    e1.printStackTrace();
                } catch (JSONException e2) {
                    // returned data is not JSONObject?
                    e2.printStackTrace();
                    Log.e("e2", e2.toString());
                }
            }

        }
    }
}
