package traffic.guru.prediction;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;
import com.codetroopers.betterpickers.timepicker.TimePickerDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Interface.ServerCallback;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardGridView;

public class TrafficPredictionActivity extends AppCompatActivity implements TimePickerDialogFragment.TimePickerDialogHandler,NavigationView.OnNavigationItemSelectedListener {
    static CardGridArrayAdapter mCardArrayAdapter;
    ArrayList<Card> cards;
    public static LocationGooglePlayServicesProvider provider;
    private Intent intent;
    private static final int REQUEST_FINE_LOCATION = 0;
    String location = "unknown", traff;
    Boolean start = false;
    private static final int REQUEST_CODE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_traffic_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showPermissionDialog();
        setTitle("Traffic Predictions");

        intent = new Intent(this, BroadcastService.class);
//        intent = new Intent(this, BroadcastService.class);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add Reminder, Please set time", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                TimePickerBuilder tpb = new TimePickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment);
                tpb.show();
            }
        });

        cards = new ArrayList<Card>();
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dfa = new SimpleDateFormat("HH:mm");

        /*DynamicTrafficPrediction dynamicTrafficPrediction = new DynamicTrafficPrediction(this, calendar);
        dynamicTrafficPrediction.onlinedata(new ServerCallback() {
            @Override
            public void onSuccess(String result) {
                Log.e("Prediction now is", result);
                GplayGridCard card = new GplayGridCard(getBaseContext());
                card.headerTitle = dfa.format(calendar.getTime());
                card.secondaryTitle = dfa.format(calendar.getTime());
                ;
                card.rating = result;

                card.init();
                cards.add(card);

                mCardArrayAdapter = new CardGridArrayAdapter(getBaseContext(), cards);

                CardGridView listView = (CardGridView) findViewById(R.id.card_grid);
                if (listView != null) {
                    listView.setAdapter(mCardArrayAdapter);
                }

                final Calendar calendar2 = Calendar.getInstance();
                calendar2.add(Calendar.HOUR, 3);

                DynamicTrafficPrediction dynamicTrafficPrediction2 = new DynamicTrafficPrediction(getBaseContext(), calendar2);
                dynamicTrafficPrediction2.onlinedata(new ServerCallback() {
                    @Override
                    public void onSuccess(String results) {
                        Log.e("Prediction three is", results);
                        GplayGridCard card2 = new GplayGridCard(getBaseContext());

                        card2.secondaryTitle = dfa.format(calendar2.getTime());
                        ;
                        card2.rating = results;

                        card2.init();
                        cards.add(card2);
                        final Calendar calendar3 = Calendar.getInstance();
                        calendar3.add(Calendar.HOUR, 6);
                        mCardArrayAdapter.notifyDataSetChanged();

                        DynamicTrafficPrediction dynamicTrafficPrediction3 = new DynamicTrafficPrediction(getBaseContext(), calendar3);
                        dynamicTrafficPrediction3.onlinedata(new ServerCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e("prediction six", result);

                                GplayGridCard card = new GplayGridCard(getBaseContext());

                                card.secondaryTitle = dfa.format(calendar3.getTime());
                                ;
                                card.rating = result;

                                card.init();
                                cards.add(card);
                                Calendar calendar3 = Calendar.getInstance();
                                calendar3.add(Calendar.HOUR, 6);
                                mCardArrayAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
*/
       TrafficPrediction TrafficPrediction = new TrafficPrediction(this, calendar);
        TrafficPrediction.onlinedata(new ServerCallback() {
            @Override
            public void onSuccess(String result) {
                Log.e("Prediction now is", result);
                GplayGridCard card = new GplayGridCard(getBaseContext());
                card.headerTitle = dfa.format(calendar.getTime());
                card.secondaryTitle = dfa.format(calendar.getTime());
                ;
                card.rating = result;

                card.init();
                cards.add(card);

                mCardArrayAdapter = new CardGridArrayAdapter(getBaseContext(), cards);

                CardGridView listView = (CardGridView) findViewById(R.id.card_grid);
                if (listView != null) {
                    listView.setAdapter(mCardArrayAdapter);
                }

                final Calendar calendar2 = Calendar.getInstance();
                calendar2.add(Calendar.HOUR, 3);

                TrafficPrediction dynamicTrafficPrediction2 = new TrafficPrediction(getBaseContext(), calendar2);
                dynamicTrafficPrediction2.onlinedata(new ServerCallback() {
                    @Override
                    public void onSuccess(String results) {
                        Log.e("Prediction three is", results);
                        GplayGridCard card2 = new GplayGridCard(getBaseContext());

                        card2.secondaryTitle = dfa.format(calendar2.getTime());
                        ;
                        card2.rating = results;

                        card2.init();
                        cards.add(card2);
                        final Calendar calendar3 = Calendar.getInstance();
                        calendar3.add(Calendar.HOUR, 6);
                        mCardArrayAdapter.notifyDataSetChanged();

                        TrafficPrediction dynamicTrafficPrediction3 = new TrafficPrediction(getBaseContext(), calendar3);
                        dynamicTrafficPrediction3.onlinedata(new ServerCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e("prediction six", result);

                                GplayGridCard card = new GplayGridCard(getBaseContext());

                                card.secondaryTitle = dfa.format(calendar3.getTime());
                                ;
                                card.rating = result;

                                card.init();
                                cards.add(card);
                                Calendar calendar3 = Calendar.getInstance();
                                calendar3.add(Calendar.HOUR, 6);
                                mCardArrayAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    public class GplayGridCard extends Card {

        protected String mTitle;
        protected TextView mSecondaryTitle;
        protected RatingBar mRatingBar;
        protected int resourceIdThumbnail = -1;
        protected int count;
        protected String url;

        protected String headerTitle;
        protected String secondaryTitle;
        protected String rating;

        public GplayGridCard(Context context) {
            super(context, R.layout.inner_content_detail);
        }


        public GplayGridCard(Context context, int innerLayout) {
            super(context, innerLayout);
        }

        private void init() {

            CardHeader header = new CardHeader(getContext());
            header.setButtonOverflowVisible(true);
            header.setTitle(headerTitle);
            header.setPopupMenu(R.menu.popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    String selected = card.getId();
//                    ID = card.getId();
                    if (card.getTitle().equals("Info")) {
                        Toast.makeText(getContext(), "No info currently!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            addCardHeader(header);
          /*  OnCardClickListener clickListener = new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    //Do something
                }
            };

            addPartialOnClickListener(Card.CLICK_LISTENER_CONTENT_VIEW, clickListener);*/
         /*   setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
//                    Do something
                    String selected= card.getId();
                    Toast.makeText(getBaseContext(), "Item ID is" + selected, Toast.LENGTH_LONG).show();
                   *//* Intent intent =new Intent(getBaseContext(),ProductDetail.class);
                    intent.putExtra("id",selected);
                    intent.putExtra("product_name",card.getTitle());
                    startActivity(intent);*//*
                }
            });*/


        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            TextView title = (TextView) view.findViewById(R.id.carddemo_gplay_main_inner_title);
            title.setText(secondaryTitle);
            float val = Float.parseFloat(rating) * 100;
            String sub = "";
            if (val >= 80) {
                sub = " Critical Traffic!!";
            } else if (val > 60 && val < 80) {
                sub = "Heavy Traffic!";
            } else if (val > 30 && val <= 60) {
                sub = " Mild Traffic";
            } else if (val > 10 && val <= 30) {
                sub = "Moving Traffic";
            } else {
                sub = "minimal traffic";
            }
            final TextView subtitle = (TextView) view.findViewById(R.id.carddemo_gplay_main_inner_subtitle);
            subtitle.setText(sub);
//            subtitle.setTextIsSelectable(true);


            ColorArcProgressBar colorArcProgressBar = (ColorArcProgressBar) view.findViewById(R.id.bar1);

            colorArcProgressBar.setCurrentValues(val);


        }
    }

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
//                    start = true;
                    startService(intent);

                    Log.e("inside", "inside");
                } else {
                    // no granted
                    start = true;
                    MyShortcuts.showToast("Can't predict without this permission accepted. Kindly accept", getBaseContext());
                    showPermissionDialog();
                }
                return;
            }

        }

    }

    private void showPermissionDialog() {
        if (!start) {
            if (!BroadcastService.checkPermission(this)) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_alarm:
//                TODO set login for inputing time to set the alarm so that the alarm is set to fire

                TimePickerBuilder tpb = new TimePickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment);
                tpb.show();
//                startAlert(1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //    TODO Call start alert to start the notification
    public void startAlert(int minutes) {

        Intent intent = new Intent(this, MyBroadcastReceiver.class);
       /* PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, minutes * 60 * 10 * 1000, pendingIntent);*/

        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add 30 seconds to the calendar object
        int sec=minutes*60;
        Log.e("sec",sec+" sec");
        cal.add(Calendar.SECOND,sec);
        PendingIntent sender = PendingIntent.getBroadcast(getBaseContext(), 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getBaseContext().getSystemService(getBaseContext().ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

       /* PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, minutes*60*1000 + (10 * 1000), pendingIntent);*/
//        Toast.makeText(this, "Alarm set", Toast.LENGTH_LONG).show();


       /* alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (10 * 1000), (10 * 1000), pendingIntent);*/
       /* alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (10 * 1000), 8000, pendingIntent);*/
        Toast.makeText(this, "Reminder set!",
                Toast.LENGTH_LONG).show();
    }


    public static Notification getNotification(String content, Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Traffic reminder");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.jam);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);
        return builder.build();
    }


    protected void showDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TrafficPredictionActivity.this);

        alertDialogBuilder.setTitle("Set Reminder");
        alertDialogBuilder.setMessage("set time in this format hh:mm am/pm");

        LinearLayout layout = new LinearLayout(getBaseContext());
        layout.setOrientation(LinearLayout.VERTICAL);


        EditText input = new EditText(getBaseContext());
        input.setHint("Choose time");
        layout.addView(input);


        Spinner spinner = new Spinner(getBaseContext());
        List<String> dimension = new ArrayList<String>();
        dimension.add("100x33x20");

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_spinner_item, dimension);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        layout.addView(spinner);

        alertDialogBuilder.setView(layout);

        alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

// TODO Will do the variance checking here
//                startAlert(minutes);
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();

    }

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        Log.e(String.format("%02d", hourOfDay), String.format("%02d", minute));
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(""+hourOfDay+":"+minute);
            String formattedDate = format.format(c.getTime());
            d2=format.parse(formattedDate);
            Log.e("date  is", formattedDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = d1.getTime() - d2.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) ;
        int min= (int) diffMinutes;
        Log.e("minutes",min+" minutes");
        startAlert(min);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.prediction) {
            Intent intent = new Intent(getBaseContext(),TrafficPredictionActivity.class);
            startActivity(intent);
        } else if (id == R.id.twitter) {
            Intent intent = new Intent(getBaseContext(),Tweet.class);
            startActivity(intent);

        } else if (id == R.id.graph) {
            Intent intent = new Intent(getBaseContext(),Graph.class);
            startActivity(intent);

        }

       /* } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}












