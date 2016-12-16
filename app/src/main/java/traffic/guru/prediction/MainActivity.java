package traffic.guru.prediction;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String rain, date;
    Calendar morningRushHour, EndofMorningRushHour, eveningRushHour, EndofeveningRushHour, finalTime;
    boolean rushhour = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getRushHourAndWeather();
       /* if (!MyShortcuts.checkDefaults("today_date", this)) {
            MyShortcuts.setDefaults("today_date", date, getBaseContext());

            if (MyShortcuts.hasInternetConnected(this)) {
//                TODO getting weather condition here
                getWeather();
            } else {
                Toast.makeText(getBaseContext(), "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_SHORT);
            }
            Log.e("weather", "No existing weather");


        } else {
           useWRH();
        }*/

        if (MyShortcuts.checkDefaults("weather_today", this)) {
            useWRH();
        } else {
            getWeather();
        }


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
                    Log.e("save is", save.toString());
                    useWRH();//Using the rush hour and weather at our curent particular time

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

//TODO Call start alert to start the notification
    public void startAlert(int minutes) {

        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, minutes*60*10*1000, pendingIntent);
       /* alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (10 * 1000), (10 * 1000), pendingIntent);*/
       /* alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (10 * 1000), 8000, pendingIntent);*/
        Toast.makeText(this, "Starting alarm in " + 10 + " seconds",
                Toast.LENGTH_LONG).show();
    }

    public static Notification getNotification(String content, Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        return builder.build();
    }

    /*public void getRushHour() {
        Calendar finalTime = Calendar.getInstance();


        Calendar firstLimit = Calendar.getInstance();
        firstLimit.set(Calendar.HOUR, 20);
        firstLimit.set(Calendar.MINUTE, 30);
        firstLimit.set(Calendar.SECOND, 30);

        Calendar secondLimit = Calendar.getInstance();
        secondLimit.set(Calendar.HOUR, 21);
        secondLimit.set(Calendar.MINUTE, 30);
        secondLimit.set(Calendar.SECOND, 30);


        if (finalTime.after(firstLimit) && finalTime.before(secondLimit)) {
            System.out.println("Its dinner time between 8:30 pm and 9:30pm");
        } else {
            System.out.println("No dinner right now");
        }


    }*/

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

    private void RushHour() {
        setMorningRushHourTimes();
        setEveningRushHourTimes();
        MyShortcuts.setDefaults("rush_hour", "false", getBaseContext());


        SimpleDateFormat dfa = new SimpleDateFormat("HH:mm");
        finalTime = Calendar.getInstance();

        if (finalTime.after(morningRushHour) && finalTime.before(EndofMorningRushHour)) {
            System.out.println("Its rush hour time between 6 am and 9am " + dfa.format(morningRushHour.getTime())
                    + " and " + dfa.format(EndofMorningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
            rushhour = true;
            MyShortcuts.setDefaults("rush_hour", "true", getBaseContext());

        } else {
            System.out.println("Not morning rush hour time "
                    + dfa.format(morningRushHour.getTime())
                    + " and " + dfa.format(EndofMorningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
        }

        if (finalTime.after(eveningRushHour) && finalTime.before(EndofeveningRushHour)) {
            System.out.println("Its rush hour time between 4pm and 8pm " + dfa.format(eveningRushHour.getTime())
                    + " and " + dfa.format(EndofeveningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
            rushhour = true;
            MyShortcuts.setDefaults("rush_hour", "true", getBaseContext());


        } else {
            System.out.println("Not evening rush hour time "
                    + dfa.format(eveningRushHour.getTime())
                    + " and " + dfa.format(EndofeveningRushHour.getTime()) + " now is " + dfa.format(finalTime.getTime()));
        }
    }


    public void getRushHourAndWeather() {
        Calendar c = Calendar.getInstance();
//        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        Log.e("date  is", formattedDate);
        String[] parts = formattedDate.split(" ");
        date = parts[0];
       /* System.out.println("Date: " + parts[0]);
        System.out.println("Time: " + parts[1]);*/


//        getRushHour();
        RushHour();

        if (!MyShortcuts.checkDefaults("today_date", this)) {

            if (MyShortcuts.hasInternetConnected(this)) {
//                TODO getting weather condition here
                MyShortcuts.setDefaults("today_date", date, getBaseContext());
                getWeather();
            } else {
                Log.e("No internet","no internet");
                Toast.makeText(getBaseContext(), "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_SHORT);
            }
            Log.e("weather", "No existing weather");


        } else {
            if (MyShortcuts.getDefaults("today_date", getBaseContext()).equals(date)) {
                Log.e("weather", "weather already saved");
            } else {

                if (MyShortcuts.hasInternetConnected(this)) {
//                TODO getting weather condition here
                    getWeather();
                } else {
                    Toast.makeText(getBaseContext(), "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_SHORT);
                }
                MyShortcuts.setDefaults("today_date", date, getBaseContext());
            }
        }
//        useWRH();

       /* if (!MyShortcuts.checkDefaults("weather_today", this)) {

            if (MyShortcuts.hasInternetConnected(this)) {

                getWeather();
            } else {
                Toast.makeText(getBaseContext(), "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_SHORT);
            }
        } else {
            MyShortcuts.showToast("Weather already saved", this);
        }*/
    }

    public void useWRH() {
        Date nearestDate = null;
        String rain = null;
        Boolean rush_hour = false;
//        TODO getting the weather condtion of my particular time
        String jsonWeather = MyShortcuts.getDefaults("weather_today", getBaseContext());
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

//            Getting current time in hours and minutes

            try {
                targetTS = df.parse(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

//Looping through saved weather objects to get the one closest to our current time
            JSONArray jsonArray = new JSONArray(jsonWeather);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String time = jsonObject.getString("time");
                time=date+" "+time;
                Date date2 = null;
//                SimpleDateFormat dateFormat = new SimpleDateFormat("hmmaa");
//TODO CHECK OUT THIS FAULTY ALGORITHM
//                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
                try {

                    date2 = df.parse(time);
                    targetTS = df.parse(formattedDate);
                    Log.e("date2 and target",date2+" and  "+targetTS );
                    long currDiff = Math.abs(date2.getTime() - targetTS.getTime());
                    if (prevDiff == -1 || currDiff < prevDiff) {
                        prevDiff = currDiff;
                        nearestDate = date2;
                        index = i;
                    }

                } catch (ParseException e) {
                    Log.e("parse", e.getMessage().toString());
                }
                /*long currDiff = Math.abs(date2.getTime() - targetTS.getTime());
                if (prevDiff == -1 || currDiff < prevDiff) {
                    prevDiff = currDiff;
                    nearestDate = date2;
                    index = i;
                }*/
            }
            System.out.println("Nearest Date: " + nearestDate);
            System.out.println("Index: " + index);

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
            String formatted=df.format(nearestDate);
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
                    nearestDate=dateFormat.parse(nearest_time);

                    if (date2.equals(nearestDate)) {
                        rain = jsonObject.getString("rain");
                    }
                } catch (ParseException e) {
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//       TODO Getting rush hour boolean
        String rush = MyShortcuts.getDefaults("rush_hour", getBaseContext());
        if (rush.equals("true")) {
//            TODO assign a value that I would use while calculating the prediction
            rush_hour = true;
        }

        Log.e("rain/rush hour:", rain + "/ " + rush_hour);

    }



}
