package traffic.guru.prediction;

/**
 * Created by Stephine on 4/8/16.
 */


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class BroadcastService extends Service implements OnActivityUpdatedListener, OnGeofencingTransitionListener, OnLocationUpdatedListener {
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "traffic.guru.prediction";
    private final Handler handler = new Handler();
    Intent intent;
    boolean jam_recorded = false;
    int counter = 0;
    long starttime = 0, starttimefoot = 0;
    boolean jam = false;
    String traffic = "0";
    String day_of_the_week, busy_road, current_rain, rush_hour, traff;
    Calendar morningRushHour, EndofMorningRushHour, eveningRushHour, EndofeveningRushHour, finalTime;
    boolean rushhour = false;
    String date, rain;
    static String i_rain = "no_rain";

    private String coordinates = "", locationname = "", activity, start, end, startjourney = "", startjourneyfoot = "", start_jam, end_jam;
//    private LocationGooglePlayServicesProvider provider;


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // We want this service to continue running until it is explicitly
//        // stopped, so return sticky.
//        return START_STICKY;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        setDefaults("previous", "null", getBaseContext());
        setDefaults("matatustartend", "true", getBaseContext());
        setDefaults("footstartend", "true", getBaseContext());
        setDefaults("locationname", "true", getBaseContext());


        getCurrentWeather();
        intent = new Intent(BROADCAST_ACTION);
//        if (checkPermission(getBaseContext())) {
        startLocation();

//        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            DisplayLoggingInfo();
            handler.postDelayed(this, 1000); // 60 seconds
        }
    };

    private void DisplayLoggingInfo() {
//        Log.d(TAG, "entered DisplayLoggingInfo");

        intent.putExtra("time", new Date().toLocaleString());
        intent.putExtra("counter", String.valueOf(++counter));
        intent.putExtra("coordinates", coordinates);
        intent.putExtra("activity", activity);
        intent.putExtra("locationname", locationname);
        intent.putExtra("start", start);
        intent.putExtra("end", end);
        intent.putExtra("traffic", traffic);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();

    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    private void startLocation() {
        Log.e("inside broadcastservice","inside");
        SaveActivity.provider = new LocationGooglePlayServicesProvider();
        SaveActivity.provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        smartLocation.location(SaveActivity.provider).start(this);
        smartLocation.activity().start(this);

        // Create some geofences -1.288999, 36.888550
        GeofenceModel Nairobi = new GeofenceModel.Builder("1").setTransition(Geofence.GEOFENCE_TRANSITION_ENTER).setLatitude(-1.292065).setLongitude(36.821946).setRadius(500000).build();

        smartLocation.geofencing().add(Nairobi).start(this);
    }


    private void showLocation(final Location location) {
        Log.e("inside location","inside");

        if (location != null) {
            final String text = String.format("Your coordinates currently are: \n Latitude %.6f, Longitude %.6f",
                    location.getLatitude(),
                    location.getLongitude());
//            locationText.setText(text);
            coordinates = "" + location.getLatitude() + ", " + location.getLongitude();

            Log.e("coordinates", coordinates);

            // We are going to get the address for the current position
            SmartLocation.with(this).geocoding().reverse(location, new OnReverseGeocodingListener() {
                @Override
                public void onAddressResolved(Location original, List<Address> results) {
                    if (results.size() > 0) {
                        Address result = results.get(0);
                        StringBuilder builder = new StringBuilder(text);
                        builder.append("\nNever get lost!, your current location is: \n\n ");
                        List<String> addressElements = new ArrayList<>();
                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                            addressElements.add(result.getAddressLine(i));
                        }
                        builder.append(TextUtils.join(", ", addressElements));
//                        locationText.setText(builder.toString());
                        locationname = builder.toString();
                        setDefaults("locationname", locationname, getBaseContext());
//        Log.e("location",locationname);
                    }
                }
            });
//            Toast.makeText(getBaseContext(), "new location ", Toast.LENGTH_SHORT).show();
        } else {
//            locationText.setText("Null location");
            locationname = "Null location";
        }
    }

    private void showActivity(DetectedActivity detectedActivity) {
        if (detectedActivity != null) {
//            activityText.setText(
//                    String.format("We have detected you are currently \n %s with %d%% confidence",
//                            getNameFromType(detectedActivity),
//                            detectedActivity.getConfidence())
//            );
            activity = String.format("You are currently \n %s with %d%% confidence",
                    getNameFromType(detectedActivity),
                    detectedActivity.getConfidence());
//            Log.e("activity", "You are currently \n %s with %d%% confidence"+
//                    getNameFromType(detectedActivity) +
//                            detectedActivity.getConfidence() + "");

            if (getNameFromType(detectedActivity).equals("still") && getDefaults("previous", getBaseContext()).equals("still")) {
//                Toast.makeText(getBaseContext(), "remained same from " + getDefaults("previous", getBaseContext()), Toast.LENGTH_SHORT).show();

//                Toast.makeText(getBaseContext(), "!!!!!!!!!! ", Toast.LENGTH_SHORT).show();


            }

        } else {
//            activityText.setText("Null activity");
            activity = "NUll activity";
        }
    }

    private void showGeofence(Geofence geofence, int transitionType) {
        if (geofence != null) {
//            geofenceText.setText("Transition " + getTransitionNameFromType(transitionType) + " for Geofence with id = " + geofence.getRequestId());
        } else {
//            geofenceText.setText("Null geofence");
        }
    }


//    @Override
//    public void onLocationUpdated(Location location) {
//        showLocation(location);
////        setDefaults("locationname", location.toString(), getBaseContext());
//        SmartLocation.with(this).geocoding().reverse(location, new OnReverseGeocodingListener() {
//            @Override
//            public void onAddressResolved(Location original, List<Address> results) {
//                if (results.size() > 0) {
//                    Address result = results.get(0);
//                    StringBuilder builder = new StringBuilder("");
//                    builder.append("\ncurrent location: \n ");
//                    List<String> addressElements = new ArrayList<>();
//                    for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
//                        addressElements.add(result.getAddressLine(i));
//                    }
//                    builder.append(TextUtils.join(", ", addressElements));
////                    locationText.setText(builder.toString());
//                    locationname = builder.toString();
//                    setDefaults("locationname", locationname, getBaseContext());
//
//
//                }
//            }
//        });
//    }

    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        showActivity(detectedActivity);


//        Checking if user has entered into a bus/train/vehicle and triggering function to start the journey automatically
        if (getNameFromType(detectedActivity).equals("in_vehicle/matatu") && detectedActivity.getConfidence() > 69 && getDefaults("matatustartend", getBaseContext()).equals("true")) {
            Toast.makeText(getBaseContext(), "Now moving!", Toast.LENGTH_SHORT).show();

            Log.e("New location is", getDefaults("locationname", getBaseContext()));
            starttime = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
            Date currentLocalTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("HH:mm a");
            setDefaults("matatustartend", "false", getBaseContext());
            Log.e("startend start matatu", getDefaults("matatustartend", getBaseContext()));

            date.setTimeZone(TimeZone.getTimeZone("GMT+3:00"));
// Get current time
            String localTime = date.format(currentLocalTime);
//            startfoot.setText("Started my ride at: " + localTime);
            start = "Started my ride at: " + localTime;
//            endfoot.setText("ended my ride at: " + " -----");
            end = "ended my ride at: " + " -----";
            startjourney = localTime;
            SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                    });

        }

        if (!(getNameFromType(detectedActivity).equals("in_vehicle/matatu")) && detectedActivity.getConfidence() > 69 && getDefaults("matatustartend", getBaseContext()).equals("false") && getNameFromType(detectedActivity).equals("on_foot")) {
//            Toast.makeText(getBaseContext(), "on foot!", Toast.LENGTH_SHORT).show();
//                Insert into database end journey
//            PostLocation(getDefaults("locationname", getBaseContext()), getNameFromType(detectedActivity), detectedActivity.getConfidence() + "", "end");
//            Toast.makeText(getBaseContext(), "Ending your vehicle ride!", Toast.LENGTH_SHORT).show();
            if (!jam_recorded) {
                postServer();
            }
            Log.e("location ending", getDefaults("locationname", getBaseContext()));
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
            Date currentLocalTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("HH:mm a");
// you can get seconds by adding  "...:ss" to it
            date.setTimeZone(TimeZone.getTimeZone("GMT+3:00"));
            String localTime = date.format(currentLocalTime);
            setDefaults("matatustartend", "true", getBaseContext());
            Log.e("startend end matatu", getDefaults("matatustartend", getBaseContext()));
            long elapsedTimeMillis = System.currentTimeMillis() - starttime;
// Get elapsed time in seconds
            float elapsedTimeSec = elapsedTimeMillis / 1000F;
            int hours = 0;
            int min = 0;
// Get elapsed time in minutes
            float elapsedTimeMin = elapsedTimeMillis / (60 * 1000F);
            int days = (int) (elapsedTimeMillis / (1000 * 60 * 60 * 24));
            hours = (int) ((elapsedTimeMillis - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
            min = (int) (elapsedTimeMillis - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
            int seconds = (int) (elapsedTimeMillis - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000);

            String start = getDefaults("startonfoot", getBaseContext());
//            endfoot.setText("ended my ride at " + localTime + "Time taken is a " +
//                    "total of " + hours + " hours " + " and " + min + " minutes" + seconds + " seconds");
            end = "ended my ride at " + localTime + "\nTime taken is a " +
                    "total of " + hours + " hours " + " and " + min + " minutes";
            String Journey = "" + elapsedTimeMillis;

//            PostJourney(Journey, startjourney, localTime);
            SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                    });


        }


//        TODO .. DETECTED TRAFFIC JAM NOW
        if (getNameFromType(detectedActivity).equals("still") && detectedActivity.getConfidence() > 69 && getDefaults("matatustartend", getBaseContext()).equals("false")) {
//       TODO  I can start recording the length of traffic jam and post it to server if possible
            if (jam) {
/*
                Start calculating traffic period from the start_time to current time.
                If I have been still for 2 minutes. Post traffic jam condition and location to
                the server to be notified to other people. This can later be notified to radio presenters.

                I can also set thresholds. if still for 10 minutes notify the traffic is critical, if 2 minutes
                the traffic is average, if more than 10 minutes the traffic is severe and one should avoid it at
                 all costs.

                 I shoould do this by updating only one field called traffic status in the traffic table once I have started.
                 I can do this by returning the id of that entry and keep on updating it till the traffic condition ends
*/
                SmartLocation.with(getBaseContext()).location()
                        .oneFix()
                        .start(new OnLocationUpdatedListener() {
                            @Override
                            public void onLocationUpdated(Location location) {
                                showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                                SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                    @Override
                                    public void onAddressResolved(Location original, List<Address> results) {
                                        if (results.size() > 0) {
                                            Address result = results.get(0);
                                            StringBuilder builder = new StringBuilder("");
                                            builder.append("\ncurrent location: \n ");
                                            List<String> addressElements = new ArrayList<>();
                                            for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                                addressElements.add(result.getAddressLine(i));
                                            }
                                            builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                            locationname = builder.toString();
                                            setDefaults("locationname", locationname, getBaseContext());
                                            Log.e("Location is", locationname);

                                        }
                                    }
                                });
                            }

                        });
//              Getting current time to subtract it from
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                Log.e("date  is", formattedDate);
                String[] parts = formattedDate.split(" ");
                String time = parts[1];

//              current time - start time to get traffic time.
               /* SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                Date Date1 = null, Date2 = null;
                try {
                    Date1 = format.parse(time);
                    Date2 = format.parse(start_jam);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long millis = Date1.getTime() - Date2.getTime();
                int Hours = (int) (millis / (1000 * 60 * 60));
                int Mins = (int) (millis % (1000 * 60 * 60));

                String diff = Hours + ":" + Mins;
                Log.e("time difference", diff);
//                Toast.makeText(getBaseContext(), "Time difference is " + diff, Toast.LENGTH_SHORT);



*/
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");


                    Date systemDate = Calendar.getInstance().getTime();
                    String myDate = sdf.format(systemDate);
//                  txtCurrentTime.setText(myDate);
                    Log.e("date  is", myDate + " - " + "02:50:00");
                    Date Date1 = sdf.parse(myDate);
                    Date Date2 = sdf.parse(start_jam);

                    long millse = Date1.getTime() - Date2.getTime();
                    long mills = Math.abs(millse);

                    int Hours = (int) (mills / (1000 * 60 * 60));
                    int Mins = (int) (mills / (1000 * 60)) % 60;
                    long Secs = (int) (mills / 1000) % 60;

                    String diff = Hours + ":" + Mins + ":" + Secs; // updated value every1 second
                    Log.e("time difference", diff);

                    if (Mins == 2) {
//                    Toast.makeText(getBaseContext(), "You've been in a traffic jam for more than " + diff + " minutes. Traffic is now moving", Toast.LENGTH_SHORT);
//                    Storing the detail to the server
                    }

                    if (Mins == 10) {
                        Toast.makeText(getBaseContext(), "You've been for " + diff + " minutes now in traffic. This traffic seems critical! You have a choice to reroute", Toast.LENGTH_SHORT);
//                    Storing the detail to the server

                    }
                } catch (Exception e) {
                }


            } else {
                jam = true;
//              Setting the start time of the traffic condition
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                Log.e("date  is", formattedDate);
                String[] parts = formattedDate.split(" ");
                String time = parts[1];
                start_jam = time;
                traffic = "1";
            }
        }


        if (getNameFromType(detectedActivity).equals("in_vehicle/matatu") && detectedActivity.getConfidence() > 69) {
            getCurrentWeather();
            SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
                            Log.e("location",location.getLatitude()+", "+location.getLongitude());
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        Log.e("Location is", locationname);
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                    });
            if (jam) {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                Log.e("date  is", formattedDate);
                String[] parts = formattedDate.split(" ");
                String time = parts[1];
                end_jam = time;
                jam = false;
                traffic = "0";
                jam_recorded = true;
//                TODO calculate the total time. If more than 2min its legible to be sent to the database

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");


                    Date systemDate = Calendar.getInstance().getTime();
                    String myDate = sdf.format(systemDate);


                    Date Date1 = sdf.parse(time);
                    Date Date2 = sdf.parse(start_jam);

                    long millse = Date1.getTime() - Date2.getTime();
                    long mills = Math.abs(millse);

                    int Hours = (int) (mills / (1000 * 60 * 60));
                    int Mins = (int) (mills / (1000 * 60)) % 60;
                    long Secs = (int) (mills / 1000) % 60;

                    String diff = Hours + ":" + Mins + ":" + Secs; // updated value every1 second
                    Log.e("time difference", diff);
                    boolean upload = false;
                    if (Mins >= 2 && Mins < 10) {
                        Toast.makeText(getBaseContext(), "You've been in a traffic jam for more than " + diff + " minutes. Traffic is now moving", Toast.LENGTH_SHORT);
//                    TODO Storing the detail to the server of traffic length
                        upload = true;

                    }

                    if (Mins >= 10) {
                        Toast.makeText(getBaseContext(), "You've been for " + diff + " minutes now in traffic. This traffic seems critical though now moving! You have a choice to reroute", Toast.LENGTH_SHORT);
//                   TODO  Storing the detail to the server of traffic length to be used by presenters
                        upload = true;


                    }


                    if (upload) {
//                    TODO uploading the traffic jam scenarios. Remaining uploads is non traffic points
                        postServer();

                    }


                } catch (Exception e) {
                    Log.e("e", e.getMessage().toString());
                }


            }
        }


////        I Used this below commented code to test if data is being sent to the server when I start and end an activity  && detectedActivity.getConfidence() > 66 && getDefaults("previous", getBaseContext()).equals("tilting") || getDefaults("previous", getBaseContext()).equals("unknown")

        if (getNameFromType(detectedActivity).equals("still")) {
            SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                    });
          /*  //                    TODO uploading the traffic jam scenarios. Remaining uploads is non traffic points
            Map<String, String> params = new HashMap<String, String>();
            params.put("rain", i_rain);
            params.put("dayofweek", getday());
            params.put("location", coordinates);
            params.put("busyroad", busy_road);
            params.put("rushhour", rush_hour);
            params.put("traffic", traffic);
            params.put("userID", getDeviceId());

            Log.e("data sent",i_rain+" "+getday()+" "+coordinates+" "+busy_road+" "+rush_hour+" "+traffic+" "+getDeviceId());


            Post.PostString(MyShortcuts.dataURL() + "UploadTraffic.php", params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("res",response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("success").equals("success")) {
                            Log.e("success", "Successfully uploaded the data!");
                        }
                        {
                            Toast.makeText(getBaseContext(), "can't upload at the moment", Toast.LENGTH_LONG);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });*/

//            Toast.makeText(getBaseContext(), "Still mode!", Toast.LENGTH_SHORT).show();
//                Insert into database end journey
//            PostLocation(getDefaults("locationname", getBaseContext()), getNameFromType(detectedActivity), detectedActivity.getConfidence() + "", "start_still");

//            Log.e("GETDEF", getDefaults("locationname", getBaseContext()));

           /* SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                       });*/

        }

//
//        if (getNameFromType(detectedActivity).equals("tilting") && getDefaults("previous", getBaseContext()).equals("still")) {
//            Toast.makeText(getBaseContext(), "Still mode ended!", Toast.LENGTH_SHORT).show();
////                Insert into database end journey
////            PostLocation(getDefaults("locationname", getBaseContext()), getNameFromType(detectedActivity), detectedActivity.getConfidence() + "", "end_still");
//            Toast.makeText(getBaseContext(), "Phone stopped relaxing!", Toast.LENGTH_SHORT).show();
////            Log.e("GETDEF", getDefaults("locationname", getBaseContext()));
//
//        }


//       Responsible for starting foot journey's and posting them to our database
//        !(getDefaults("previous", getBaseContext()).equals("on_foot")) && !(getDefaults("previous", getBaseContext()).equals("tilting"))&&

//        TODO doesnt require on foot
       /* if (getNameFromType(detectedActivity).equals("on_foot") && detectedActivity.getConfidence() > 69 && getDefaults("footstartend", getBaseContext()).equals("true")) {
            Toast.makeText(getBaseContext(), "started footing!", Toast.LENGTH_SHORT).show();
//                Insert into database end journey
            PostLocation(getDefaults("locationname", getBaseContext()), getNameFromType(detectedActivity), detectedActivity.getConfidence() + "", "startfoot");
            setDefaults("startonfoot", getDefaults("locationname", getBaseContext()) + "start", getBaseContext());
            Log.e("GETDEF", getDefaults("locationname", getBaseContext()));

            setDefaults("footstartend", "false", getBaseContext());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
            Date currentLocalTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("HH:mm a");
// you can get seconds by adding  "...:ss" to it
            date.setTimeZone(TimeZone.getTimeZone("GMT+3:00"));
// Get current time
            starttimefoot = System.currentTimeMillis();
            String localTime = date.format(currentLocalTime);
            startjourneyfoot = localTime;
//            startfoot.setText("Started footing at " + localTime);
            start = "Started footing at " + localTime;
//            endfoot.setText("ended footing at " + "still unknown");
            end = "ended footing at " + "still unknown";
            Log.e("startend start footing", getDefaults("matatustartend", getBaseContext()));
            SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                    });


        }

//       Responsible for ending foot journey's and posting them to our database

        if (!(getNameFromType(detectedActivity).equals("on_foot")) && detectedActivity.getConfidence() > 80 && !(getNameFromType(detectedActivity).equals("tilting")) && getDefaults("footstartend", getBaseContext()).equals("false")) {
            Toast.makeText(getBaseContext(), "end footing!", Toast.LENGTH_SHORT).show();
//                Insert into database end foot journey
            PostLocation(getDefaults("locationname", getBaseContext()), getNameFromType(detectedActivity), detectedActivity.getConfidence() + "", "endfoot");

            Log.e("ending journey time", getDefaults("locationname", getBaseContext()));
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
            Date currentLocalTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("HH:mm a");
// you can get seconds by adding  "...:ss" to it
            date.setTimeZone(TimeZone.getTimeZone("GMT+3:00"));
            String localTime = date.format(currentLocalTime);

            setDefaults("footstartend", "true", getBaseContext());
            Log.e("startend end footing", getDefaults("matatustartend", getBaseContext()));
            long elapsedTimeMillis = System.currentTimeMillis() - starttimefoot;

// Get elapsed time in seconds
            float elapsedTimeSec = elapsedTimeMillis / 1000F;
            int hours = 0;
            int min = 0;
// Get elapsed time in minutes
            float elapsedTimeMin = elapsedTimeMillis / (60 * 1000F);
            int days = (int) (elapsedTimeMillis / (1000 * 60 * 60 * 24));
            hours = (int) ((elapsedTimeMillis - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
            min = (int) (elapsedTimeMillis - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
            int seconds = (int) (elapsedTimeMillis - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000);

            String start = getDefaults("startonfoot", getBaseContext());
//            endfoot.setText("ended footing at " + localTime + "Time taken is a " +
//                    "total of " + hours + " hours " + " and " + min + " minutes" + seconds + " seconds");
            end = "ended footing at " + localTime + " \nTime taken is a " +
                    "total of " + hours + " hours " + " and " + min + " minutes";
            String Journey = "" + elapsedTimeMillis;
            PostJourneyfoot(Journey, startjourneyfoot, localTime);
        }*/


        setDefaults("previous", getNameFromType(detectedActivity), getBaseContext());


        if (detectedActivity.getConfidence() == 100) {
            Log.e("inside broadcastservice","inside");
            SmartLocation.with(getBaseContext()).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            showLocation(location);
//        setDefaults("locationname", location.toString(), getBaseContext());
                            SmartLocation.with(getBaseContext()).geocoding().reverse(location, new OnReverseGeocodingListener() {
                                @Override
                                public void onAddressResolved(Location original, List<Address> results) {
                                    if (results.size() > 0) {
                                        Address result = results.get(0);
                                        StringBuilder builder = new StringBuilder("");
                                        builder.append("\ncurrent location: \n ");
                                        List<String> addressElements = new ArrayList<>();
                                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                                            addressElements.add(result.getAddressLine(i));
                                        }
                                        builder.append(TextUtils.join(", ", addressElements));
//                    locationText.setText(builder.toString());
                                        locationname = builder.toString();
                                        setDefaults("locationname", locationname, getBaseContext());
                                        Log.e("Location is", locationname);

                                    }
                                }
                            });
                        }

                    });
        }

    }

    @Override
    public void onGeofenceTransition(TransitionGeofence geofence) {
        showGeofence(geofence.getGeofenceModel().toGeofence(), geofence.getTransitionType());
    }

    private String getNameFromType(DetectedActivity activityType) {
        switch (activityType.getType()) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle/matatu";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            default:
                return "unknown";
        }
    }

    private String getTransitionNameFromType(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            default:
                return "dwell";
        }
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();

    }


    private String time() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm a");
// you can get seconds by adding  "...:ss" to it
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

        String localTime = date.format(currentLocalTime);
        return localTime;
    }


    public String getDeviceId() {
        TelephonyManager tMgr = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getDeviceId();
    }

    private String getday() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        Log.e("day", dayOfTheWeek);
        return dayOfTheWeek;
    }

    public String getRushHour() {
        setMorningRushHourTimes();
        setEveningRushHourTimes();
        MyShortcuts.setDefaults("rush_hour", "false", getBaseContext());
        String rush_hour = "false";


        SimpleDateFormat dfa = new SimpleDateFormat("HH:mm");
        finalTime = Calendar.getInstance();

        if (finalTime.after(morningRushHour) && finalTime.before(EndofMorningRushHour)) {
            System.out.println("Its rush hour time between 6 am and 9am " + dfa.format(morningRushHour.getTime())
                    + " and " + dfa.format(EndofMorningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));

            rush_hour = "true";

        } else {
            System.out.println("Not morning rush hour time "
                    + dfa.format(morningRushHour.getTime())
                    + " and " + dfa.format(EndofMorningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
        }

        if (finalTime.after(eveningRushHour) && finalTime.before(EndofeveningRushHour)) {
            System.out.println("Its rush hour time between 4pm and 8pm " + dfa.format(eveningRushHour.getTime())
                    + " and " + dfa.format(EndofeveningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
            rush_hour = "true";


        } else {
            System.out.println("Not evening rush hour time "
                    + dfa.format(eveningRushHour.getTime())
                    + " and " + dfa.format(EndofeveningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
        }
        return rush_hour;
    }

    private void setMorningRushHourTimes() {
        morningRushHour = Calendar.getInstance();
        morningRushHour.set(Calendar.HOUR, 6);
        morningRushHour.set(Calendar.MINUTE, 30);
        morningRushHour.set(Calendar.AM_PM, Calendar.AM);

        EndofMorningRushHour = Calendar.getInstance();
        EndofMorningRushHour.set(Calendar.HOUR, 9);
        EndofMorningRushHour.set(Calendar.MINUTE, 00);
        EndofMorningRushHour.set(Calendar.AM_PM, Calendar.AM);
    }

    private void setEveningRushHourTimes() {
        eveningRushHour = Calendar.getInstance();
        eveningRushHour.set(Calendar.HOUR, 5);
        eveningRushHour.set(Calendar.MINUTE, 00);
        eveningRushHour.set(Calendar.AM_PM, Calendar.PM);

        EndofeveningRushHour = Calendar.getInstance();
        EndofeveningRushHour.set(Calendar.HOUR, 8);
        EndofeveningRushHour.set(Calendar.MINUTE, 00);
        EndofeveningRushHour.set(Calendar.AM_PM, Calendar.PM);
    }

    public String getCurrentWeather() {
        String rain = "";
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        Log.e("date  is", formattedDate);
        String[] parts = formattedDate.split(" ");
        date = parts[0];

        if (!MyShortcuts.checkDefaults("today_date", this)) {

            if (MyShortcuts.hasInternetConnected(this)) {
//                TODO getting weather condition here
                MyShortcuts.setDefaults("today_date", date, getBaseContext());
                getWeather();
            } else {
                Log.e("No internet", "no internet");
                Toast.makeText(getBaseContext(), "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_LONG);
                Log.e("weather", "No existing weather");
            }


        } else {
            Log.e("dates sp and now", MyShortcuts.getDefaults("today_date", getBaseContext()) + " and " + date);
            if (MyShortcuts.getDefaults("today_date", getBaseContext()).equals(date)) {
                Log.e("weather", "weather already saved");
                rain = GetRain();
                i_rain = rain;
            } else {

                if (MyShortcuts.hasInternetConnected(this)) {
//                TODO getting weather condition here
                    getWeather();
                    MyShortcuts.setDefaults("today_date", date, getBaseContext());
                } else {
                    Log.e("No internet", "no internet");
                    Toast.makeText(getBaseContext(), "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_LONG);
                }

            }
        }
        return rain;
    }

    private void getWeather() {

        Post.getData(MyShortcuts.baseURL(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
//                    Log.e("response", response);
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                    JSONArray save = new JSONArray();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        JSONArray jsonArray1 = jsonObject1.getJSONArray("weather");
                        String dtime = jsonObject1.getString("dt_txt");
                        JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
                        String id = jsonObject2.getString("id");
                        int id2 = Integer.parseInt(id);
                        int id3 = id2 / 100;

                        String[] parts = dtime.split(" ");
                        String date_Get = parts[0];
                        String time = parts[1];

                        if (date_Get.equals(date)) {

                            if (id2 >= 502 && id2 <= 531) {
//                            Knowing the weather is  Heavy intensity rain
//                            indicate no rainy weather
                                rain = "strong";
                            } else if (id3 == 5) {
                                rain = "average";
                            } else {
                                rain = "no_rain";
                            }

                            JSONObject item = new JSONObject();
                            item.put("rain", rain);
                            item.put("time", time);
                            Log.e("json_object", item.toString());

                            save.put(item);
                        }

                    }

                    MyShortcuts.setDefaults("weather_today", save.toString(), getBaseContext());
//                    Log.e("save is", save.toString());
                    i_rain = GetRain();//Using the rush hour and weather at our curent particular time
//                    current_rain=GetRain();
//                    Log.e("cuurent_rain",current_rain);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String GetRain() {


        Date nearestDate = null;
        String rain = null;

//        TODO getting the weather condtion of my particular time
        String jsonWeather = MyShortcuts.getDefaults("weather_today", getApplication());
        Log.e("weather array", jsonWeather);
        try {
//Initialising variables
            Date targetTS = null;
            Calendar c = Calendar.getInstance();
            int index = 0;
            long prevDiff = -1;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            Log.e("date  is", formattedDate);
            String[] parts = formattedDate.split(" ");
            String date = parts[0];


//Looping through saved weather objects to get the one closest to our current time
            JSONArray jsonArray = new JSONArray(jsonWeather);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String time = jsonObject.getString("time");
                time = date + " " + time;
                Date date2 = null;

                try {

                    date2 = df.parse(time);
                    targetTS = df.parse(formattedDate);
//                    Log.e("date2 and target", date2 + " and  " + targetTS);
                    long currDiff = Math.abs(date2.getTime() - targetTS.getTime());
                    if (prevDiff == -1 || currDiff < prevDiff) {
                        prevDiff = currDiff;
                        nearestDate = date2;
                        index = i;
                    }

                } catch (ParseException e) {
                    Log.e("parse", e.getMessage().toString());
                }

            }
            System.out.println("Nearest Date: " + nearestDate);
            System.out.println("Index: " + index);

//            Splitting so as to get only the time part of the Date Time
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
            String formatted = df.format(nearestDate);
            String[] part = formatted.split(" ");
            String nearest_time = part[1];
            System.out.println("Nearest time: " + nearest_time);


            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String time = jsonObject.getString("time");
                Date date2 = null;
//                SimpleDateFormat dateFormat = new SimpleDateFormat("hmmaa");

                try {
                    date2 = dateFormat.parse(time);
                    nearestDate = dateFormat.parse(nearest_time);

                    if (date2.equals(nearestDate)) {
                        rain = jsonObject.getString("rain");
                    }
                } catch (ParseException e) {
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        day_of_the_week = getday();
        rush_hour = getRushHour();
        current_rain = rain;
        if (Integer.parseInt(traffic) == 1 && rush_hour.equals("false")) {
            busy_road = "true";
        } else {
            busy_road = "false";
        }
//TODO getting all ***INGREDIENTS*** and saving it to the database
        Log.e("Current", day_of_the_week + "  day And " + rush_hour +
                " rush hour And " + current_rain + " rain and " + busy_road + " road and traffic " + traff);
        return rain;


    }

    private void postServer() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("rain", i_rain);
        params.put("dayofweek", getday());
        params.put("location", coordinates);
        params.put("busyroad", busy_road);
        params.put("rushhour", rush_hour);
        params.put("traffic", traffic);
        params.put("userID", getDeviceId());


        Post.PostString(MyShortcuts.dataURL() + "UploadTraffic.php", params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("success").equals("success")) {
                        Log.e("success", "Successfully uploaded the data!");
                    }
                    {
//                        Toast.makeText(getBaseContext(), "can't upload at the moment", Toast.LENGTH_LONG);
                        Log.e("error", "error uploading");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onLocationUpdated(Location location) {
        showLocation(location);
    }
}

