package traffic.guru.prediction;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geocoding.utils.LocationAddress;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class ProcessLocation extends AppCompatActivity implements
        OnLocationUpdatedListener {
    private LocationGooglePlayServicesProvider provider;
    private static final int REQUEST_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (MyShortcuts.hasInternetConnected(this)) {
            if (checkPermission(getBaseContext())) {


                startLocation();
                getData();
                SmartLocation.with(getBaseContext()).geocoding()
                        .direct(" Manyanja Rd, Nairobi, Kenya", new OnGeocodingListener() {
                            @Override
                            public void onLocationResolved(String name, List<LocationAddress> results) {
                                // name is the same you introduced in the parameters of the call
                                // results could come empty if there is no match, so please add some checks around that
                                // LocationAddress is a wrapper class for Address that has a Location based on its data
                                if (results.size() > 0) {
                                    Location res = results.get(0).getLocation();

                                    Log.e("Resolve", " resolved manyanja coordinates as "+name);
                                    }

                                }
                        });

            } else {
                showPermissionDialog();
            }

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
    }


    private void startLocation() {

        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        smartLocation.location(provider).start(this);




    }


    @Override
    public void onLocationUpdated(Location location) {
        Log.e(location.getLatitude() + "", location.getLongitude() + "");

    }

    private void showPermissionDialog() {
        if (!checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted

                    Looper looper = null;
                    if (checkPermission(getBaseContext())) {

//                        TODO Commented this location code

                        if (MyShortcuts.hasInternetConnected(getBaseContext())) {
                            startLocation();
                        }
                    }
                    Log.e("inside", "inside");
                } else {
                    // not granted
                    MyShortcuts.showToast("You cannot read meter details without accepting this permission!", getBaseContext());
                    showPermissionDialog();
                }
                return;
            }

        }

    }

    /**
     * Used to check the result of the check of the user location settings
     *
     * @param requestCode code of the request made
     * @param resultCode  code of the result of that request
     * @param intent      intent with further information
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        if (provider != null) {
            provider.onActivityResult(requestCode, resultCode, intent);
        }
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }


    private void convertTheCoordinates(String Response){
        try {
            JSONObject jsonObject = new JSONObject(Response);
            JSONArray jsonArray=jsonObject.getJSONArray("All");

            final JSONArray processed = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json= jsonArray.getJSONObject(i);
                String location=json.getString("location");
                String strOut = location.substring(18);
//                Log.e("strOUt",strOut);


                SmartLocation.with(getBaseContext()).geocoding()
                        .direct(strOut, new OnGeocodingListener() {
                            @Override
                            public void onLocationResolved(String name, List<LocationAddress> results) {
                                // name is the same you introduced in the parameters of the call
                                // results could come empty if there is no match, so please add some checks around that
                                // LocationAddress is a wrapper class for Address that has a Location based on its data
                                if (results.size() > 0) {
                                    Location res = results.get(0).getLocation();

                                    if(!res.equals(null)){
                                        JSONObject jsonObject=new JSONObject();
                                        try {
                                            jsonObject.put("longitude",res.getLongitude()+"");
                                            jsonObject.put("latitude",res.getLatitude()+"");
                                            jsonObject.put("name",name);
                                            processed.put(jsonObject);
                                            Log.e("long",res.getLongitude()+"");
                                            Map<String, String> params=new HashMap<String, String>();
                                            params.put("longitude", res.getLongitude()+"");
                                            params.put("latitude",res.getLatitude()+"");
                                            params.put("name",name);
                                            Post.PostString(MyShortcuts.dataURL() + "PostLocation.php", params, new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    Log.e("response",response.toString());
                                                }
                                            });

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        Log.e("Resolve error", "could not resolve "+name);
                                    }

                                }
                            }
                        });

            }

            Log.e("Processed array",processed.toString());
            MyShortcuts.setDefaults("data",processed.toString(),getBaseContext());
//            Saving the preprocessed data
            saveData(processed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getData(){
        Log.e("inside","inside get data");
        Post.getData(MyShortcuts.dataURL() + "GetLocation.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                convertTheCoordinates(response);
            }
        });
    }

    private void saveData(JSONArray json){
        Map<String, String> params=new HashMap<String, String>();;

        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject jsono= json.getJSONObject(i);

                params.put("longitude", jsono.getString("longitude"));
                params.put("latitude",jsono.getString("latitude"));

                Post.PostString(MyShortcuts.dataURL() + "PostLocation.php", params, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("response",response.toString());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }



        /*Post.PostString(MyShortcuts.dataURL() + "PostLocation.php", params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("response",response.toString());
            }
        });*/

    }


}
