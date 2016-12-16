package traffic.guru.prediction;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class SaveActivity extends AppCompatActivity {
    private int save;
    String day_of_the_week, busy_road, current_rain, rush_hour, traffic, traff;
    Calendar morningRushHour, EndofMorningRushHour, eveningRushHour, EndofeveningRushHour, finalTime;
    boolean rushhour = false;
    String date, rain;
    static String i_rain;
    public static LocationGooglePlayServicesProvider provider;
    private Intent intent;
    private static final int REQUEST_FINE_LOCATION = 0;
    String location = "unknown";
    Boolean start = false;
    private static final int REQUEST_CODE_LOCATION = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showPermissionDialog();


        /*Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        Log.e("date  is", formattedDate);
        String[] parts = formattedDate.split(" ");
        String time = parts[1];
        Log.e("time",time);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date Date1 = null, Date2 = null;
        try {
            Date1 = format.parse("08:00:12");
            Date2 = format.parse("05:30:12");
            long millse = Date1.getTime() - Date2.getTime();
            long millis = Math.abs(millse);
            int Hours = (int) (millis / (1000 * 60 * 60));
            int Mins = (int) (millis % (1000 * 60 * 60));

            String diff = Hours + ":" + Mins;
            Log.e("time difference",diff);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/


        /*day_of_the_week = getday();
        rush_hour = getRushHour();
        current_rain = getCurrentWeather();*/

       /* if (MyShortcuts.checkDefaults("weather_today", this)) {
            current_rain = getCurrentWeather();
        } else {
            getWeather();
        }
        current_rain = getCurrentWeather();
*/


//        getCurrentWeather();
        intent = new Intent(this, BroadcastService.class);
        intent = new Intent(this, BroadcastService.class);

//        TODO set these variables after detecting traffic using activity recognition. If traffic and not rush hour,set busy road to true
      /*  busy_road=;
        traffic=;
*/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    //    Getting weather from open weather website API
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
//        useWRH();

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


//   TODO Traffic Data


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (provider != null) {
            Log.e("provider", "provider");
            provider.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
//        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("permissions", "permissions not granted");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            // Location permission has been granted, continue as usual.
            Log.e("permissions", "permissions granted");
            startService(intent);
            registerReceiver(broadcastReceiver, new IntentFilter(BroadcastService.BROADCAST_ACTION));

        }

       /* if (start) {
            Log.e("permissions", "permissions granted");
            startService(intent);
            registerReceiver(broadcastReceiver, new IntentFilter(BroadcastService.BROADCAST_ACTION));
        } else {
            Log.e("permissions", "permissions not granted");
            Toast.makeText(getBaseContext(), "Cannot make traffic prediction as per now, Kindly accept the location permission", Toast.LENGTH_LONG);
            showPermissionDialog();

        }*/

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
//        stopService(intent);
    }


    private void updateUI(Intent intent) {
        String counter = intent.getStringExtra("counter");
        String time = intent.getStringExtra("time");
        String coordinate = intent.getStringExtra("coordinates");
        String locationname = intent.getStringExtra("locationname");
        String activity = intent.getStringExtra("activity");
        String start = intent.getStringExtra("start");
        String end = intent.getStringExtra("end");
        location = locationname;
        traff = intent.getStringExtra("traffic");


//         Log.e("activity", activity+" "+location);
//        Log.d(TAG, time);

       /* TextView txtDateTime = (TextView) findViewById(R.id.txtDateTime);
        TextView txtCounter = (TextView) findViewById(R.id.txtCounter);
        TextView location = (TextView) findViewById(R.id.location_text);
//        TextView coordinates = (TextView) findViewById(R.id.activity_text);
        TextView starttime = (TextView) findViewById(R.id.startfoot);
        TextView endtime = (TextView) findViewById(R.id.endfoot);
        TextView activitytext = (TextView) findViewById(R.id.activity_text);*//*

        txtDateTime.setText(time);
        txtCounter.setText(counter);
        location.setText(locationname + "\n" + coordinate);
        activitytext.setText(activity);

//        coordinates.setText(coordinate);
        starttime.setText(start);
        endtime.setText(end);*/
    }


    @Override
    protected void onStop() {

        super.onStop();
    }


    private void loadPermissions(String perm, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, requestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    start = true;
                    startService(intent);

                    Log.e("inside", "inside");
                } else {
                    // no granted
                    start = false;
                }
                return;
            }

        }

    }

    private void showPermissionDialog() {
        if (!BroadcastService.checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }
}
/*
INSERT INTO traffics (dayofweek,rain,busyroad,rushhour,traffic) VALUES
        ('Monday','strong','true','true','true'),
        ('Tuesday','strong','true','false','true'),
        ('Monday','strong','false','true','true'),
        ('Monday','strong','false','false','false'),
        ('Wednesday','average','true','true','true'),
        ('Thursday','average','true','false','true'),
        ('Wednesday','average','false','true','true'),
        ('Monday','average','false','false','false'),
        ('Monday','no_rain','true','true','true'),
        ('Friday','no_rain','true','false','false'),
        ('Monday','no_rain','false','true','false'),
        ('Monday','no_rain','false','false','false');
*/




