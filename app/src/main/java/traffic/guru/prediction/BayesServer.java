package traffic.guru.prediction;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.bayesserver.*;
import com.bayesserver.inference.*;
import traffic.guru.prediction.ColorArcProgressBar;

//import javax.xml.stream.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import Interface.ServerCallback;

public class BayesServer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bayes_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ColorArcProgressBar colorArcProgressBar = (ColorArcProgressBar)findViewById(R.id.bar1);
//        int diameter = 5*getScreenWidth() / 10;
//        colorArcProgressBar.setDiameter(diameter);

        colorArcProgressBar.setCurrentValues(80);

        /*Calendar calendar = Calendar.getInstance();
        DynamicTrafficPrediction dynamicTrafficPrediction = new DynamicTrafficPrediction(this, calendar);
        dynamicTrafficPrediction.onlinedata(new ServerCallback() {
            @Override
            public void onSuccess(String result) {
                Log.e("Prediction now is", result);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.add(Calendar.HOUR, 3);

                DynamicTrafficPrediction dynamicTrafficPrediction2 = new DynamicTrafficPrediction(getBaseContext(), calendar2);
                dynamicTrafficPrediction2.onlinedata(new ServerCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e("Prediction three is", result);

                        Calendar calendar3 = Calendar.getInstance();
                        calendar3.add(Calendar.HOUR, 6);

                        DynamicTrafficPrediction dynamicTrafficPrediction3 = new DynamicTrafficPrediction(getBaseContext(), calendar3);
                        dynamicTrafficPrediction3.onlinedata(new ServerCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e("prediction six", result);
                            }
                        });
                    }
                });
            }
        });*/









        /* SimpleDateFormat dfa = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.e("calendar",dfa.format(calendar.getTime()));*/


        /*JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("rain","strong");
            jsonObject.put("rush_hour","true");
            jsonObject.put("road_busy","true");
            jsonObject.put("traffic","true");
            jsonArray.put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dynamicTrafficPrediction.processData(jsonArray);*/
        /*TrafficPredictions trafficPredictions= new TrafficPredictions();
        try {
            trafficPredictions.main(true,true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistentEvidenceException e) {
            e.printStackTrace();
        }*/

        /*NetworkExample networkExample=new NetworkExample();
        try {
            networkExample.main();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistentEvidenceException e) {
            e.printStackTrace();
        }*/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void predict(Context context, Calendar calendar) {
        DynamicTrafficPrediction dynamicTrafficPrediction = new DynamicTrafficPrediction(context, calendar);
//        return dynamicTrafficPrediction.getPrediction();
    }

    public  int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }


}
