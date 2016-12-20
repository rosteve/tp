package traffic.guru.prediction;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.bayesserver.Link;
import com.bayesserver.Network;
import com.bayesserver.Node;
import com.bayesserver.State;
import com.bayesserver.Table;
import com.bayesserver.TableIterator;
import com.bayesserver.inference.InconsistentEvidenceException;
import com.bayesserver.inference.Inference;
import com.bayesserver.inference.InferenceFactory;
import com.bayesserver.inference.QueryOptions;
import com.bayesserver.inference.QueryOutput;
import com.bayesserver.inference.RelevanceTreeInferenceFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Interface.ServerCallback;

//import javax.xml.stream.*;

/**
 * Created by stephineosoro on 06/12/2016.
 */

public class TrafficPrediction {

    Context context;
    Calendar the_date;
    public static String return_data;

    public TrafficPrediction(Context context, Calendar date) {
        this.context = context;
        this.the_date = date;
    }


    private static String day_of_the_week, busy_road, current_rain, rush_, traffic, traff;
    private Calendar morningRushHour;
    private Calendar EndofMorningRushHour;
    private Calendar eveningRushHour;
    private Calendar EndofeveningRushHour;
    private static boolean rush, wrain, rbusy;
    private String date, rain;
    private static String i_rain;

   /* public String onlinedata() {
        rush_ = getRushHour();
        current_rain = getCurrentWeather();
        rush = Boolean.parseBoolean(getRushHour());
        wrain = Boolean.parseBoolean(getCurrentWeather());
        //        TODO get busy road from the data but for now am giving it a one
        rbusy = true;

        String val="";
        try {
             val = main(true, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistentEvidenceException e) {
            e.printStackTrace();
        }
        return  val;

    }*/

    public void onlinedata(final ServerCallback callback) {
        rush_ = getRushHour();
        current_rain = getCurrentWeather();
        rush = Boolean.parseBoolean(getRushHour());
        wrain = Boolean.parseBoolean(getCurrentWeather());
        //        TODO get busy road from the data but for now am giving it a one
        rbusy = true;

        try {
            String val = main(true, true);
            callback.onSuccess(val);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistentEvidenceException e) {
            e.printStackTrace();
        }

    }


    public static String main(boolean rush_hour_boolean, boolean rain_boolean) throws IOException, InconsistentEvidenceException {

        // In this example we programatically create a simple Bayesian network.
        // Note that you can automatically define nodes from data using
        // classes in BayesServer.Data.Discovery,
        // and you can automatically learn the parameters using classes in
        // BayesSerer.Learning.Parameters,
        // however here we build a Bayesian network from scratch.

        Network network = new Network("traffic");

        // add the nodes (variables)

       /* State rTrue = new State("True");
        State rFalse = new State("False");
        Node a = new Node("A", rTrue, rFalse);


        State rbTrue = new State("True");
        State rbFalse = new State("False");
        Node b = new Node("B", rbTrue, rbFalse);

        State rhTrue = new State("True");
        State rhFalse = new State("False");
        Node c = new Node("C", rhTrue, rhFalse);

       State tTrue = new State("True");
        State tFalse = new State("False");
        Node d = new Node("D", tTrue, tFalse);*/

        State rTrue = new State("True");
        State rAverage = new State("average");
        State rFalse = new State("False");
        Node rain = new Node("A", rTrue, rAverage, rFalse);

        State rbTrue = new State("True");
        State rbFalse = new State("False");
        Node road_busy = new Node("B", rbTrue, rbFalse);

        State rhTrue = new State("True");
        State rhFalse = new State("False");
        Node rush_hour = new Node("C", rhTrue, rhFalse);

        State tTrue = new State("True");
        State tFalse = new State("False");
        Node traffic = new Node("D", tTrue, tFalse);

        network.getNodes().add(rain);
        network.getNodes().add(road_busy);
        network.getNodes().add(rush_hour);
        network.getNodes().add(traffic);

        // add some directed links

//        network.getLinks().add(new Link(rain, road_busy));
        network.getLinks().add(new Link(rain, traffic));
        network.getLinks().add(new Link(road_busy, traffic));
        network.getLinks().add(new Link(rush_hour, traffic));

        // at this point we have fully specified the structural (graphical) specification of the Bayesian Network.

        // We must define the necessary probability distributions for each node.

        // Each node in a Bayesian Network requires a probability distribution conditioned on it's parents.

        // NewDistribution() can be called on a Node to create the appropriate probability distribution for a node
        // or it can be created manually.

        // The interface IDistribution has been designed to represent both discrete and continuous variables,

        // As we are currently dealing with discrete distributions, we will use the
        // Table class.

        // To access the discrete part of a distribution, we use IDistribution.Table.

        // The Table class is used to define distributions over a number of discrete variables.

        Table tableA = rain.newDistribution().getTable();     // access the table property of the Distribution

        // IMPORTANT
        // Note that calling Node.NewDistribution() does NOT assign the distribution to the node.
        // A distribution cannot be assigned to a node until it is correctly specified.
        // If a distribution becomes invalid  (e.g. a parent node is added), it is automatically set to null.

        // as node A has no parents there is no ambiguity about the order of variables in the distribution
        tableA.set(0.4, rTrue);
        tableA.set(0.3, rAverage);
        tableA.set(0.3, rFalse);

        // now tableA is correctly specified we can assign it to Node A;
        rain.setDistribution(tableA);


        // node B has node A as a parent, therefore its distribution will be P(B)

        Table tableB = road_busy.newDistribution().getTable();
        tableB.set(0.7, rbTrue);
        tableB.set(0.3, rbFalse);
        /*tableB.set(0.2, rTrue, rbTrue);
        tableB.set(0.8, rTrue, rbFalse);
        tableB.set(0.15, rFalse, rbTrue);
        tableB.set(0.85, rFalse, rbFalse);*/
        road_busy.setDistribution(tableB);


        // specify P(C)
        Table tableC = rush_hour.newDistribution().getTable();
        tableC.set(0.8, rhTrue);
        tableC.set(0.2, rhFalse);
        rush_hour.setDistribution(tableC);


        // specify P(D|A,B,C)
        Table tableD = traffic.newDistribution().getTable();

       /* // we could specify the values individually as above, or we can use a TableIterator as follows
        TableIterator iteratorD = new TableIterator(tableD, new Node[]{road_busy, rush_hour, traffic});
        iteratorD.copyFrom(new double[]{0.4, 0.6, 0.55, 0.45, 0.32, 0.68, 0.01, 0.99});
        traffic.setDistribution(tableD);*/

        // we could specify the values individually as above, or we can use a TableIterator as follows
        TableIterator iterator_D = new TableIterator(tableD, new Node[]{rain, road_busy, rush_hour, traffic});
        iterator_D.copyFrom(new double[]{0.9, 0.1, 0.6, 0.4, 0.9, 0.1, 0.5, 0.5, 0.9, 0.1, 0.7, 0.3, 0.7, 0.3, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7, 0.4, 0.6, 0.1, 0.9});
        traffic.setDistribution(tableD);


        // The network is now fully specified

        // If required the network can be saved...

       /* if (false)   // change this to true to save the network
        {
            network.save("fileName.bayes");  // replace 'fileName.bayes' with your own path
        }*/

        // Now we will calculate P(A|D=True), i.e. the probability of A given the evidence that D is true

        // use the factory design pattern to create the necessary inference related objects
        InferenceFactory factory = new RelevanceTreeInferenceFactory();
        Inference inference = factory.createInferenceEngine(network);
        QueryOptions queryOptions = factory.createQueryOptions();
        QueryOutput queryOutput = factory.createQueryOutput();

        // we could have created these objects explicitly instead, but as the number of algorithms grows
        // this makes it easier to switch between them

//        inference.getEvidence().setState(rTrue);  // set Traffic = True

        if (wrain) {
            inference.getEvidence().setState(rTrue);  // set Traffic = True
        } else {
            inference.getEvidence().setState(rFalse);  // set Traffic = False

        }
        Table queryA = new Table(traffic);
        inference.getQueryDistributions().add(queryA);
        inference.query(queryOptions, queryOutput); // note that this can raise an exception (see help for details)

        System.out.println("P(traffic|rain=Average) = {" + queryA.get(tTrue) + "," + queryA.get(tFalse) + "}.");

        // Expected output ...
        // P(A|D=True) = {0.0980748663101604,0.90192513368984}

        // to perform another query we reuse all the objects

        // now lets calculate P(A|traffic=True, rush_hour=True)
//        inference.getEvidence().setState(rhTrue);
        if (rush) {
            inference.getEvidence().setState(rhTrue);
        } else {
            inference.getEvidence().setState(rhFalse);
        }

//        TODO find a way of getting to know if a road is busy
        inference.getEvidence().setState(rbTrue);

        // we will also return the log-likelihood of the case
        queryOptions.setLogLikelihood(true); // only request the log-likelihood if you really need it, as extra computation is involved

        inference.query(queryOptions, queryOutput);
        System.out.println(String.format("P(traffic|rain=True, rush_hour=True, road_busy=true) = {%s,%s}, log-likelihood = %s.", queryA.get(tTrue), queryA.get(tFalse), queryOutput.getLogLikelihood()));

        // Expected output ...
        // P(A|D=True, C=True) = {0.0777777777777778,0.922222222222222}, log-likelihood = -2.04330249506396.

        // Note that we can also calculate joint queries such as P(A,B|D=True,C=True)

        return_data = queryA.get(tTrue) + "";

        return return_data;
    }


    public String getRushHour() {
        setMorningRushHourTimes();
        setEveningRushHourTimes();
        MyShortcuts.setDefaults("rush_hour", "false", context);
        String rush_hour = "false";


        SimpleDateFormat dfa = new SimpleDateFormat("HH:mm");
//        Calendar finalTime = Calendar.getInstance();
        Calendar finalTime = the_date;

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
//                            Log.e("json_object", item.toString());

                            save.put(item);
                        }

                    }

                    MyShortcuts.Delete(context);
                    MyShortcuts.setDefaults("weather_today", save.toString(), context);
                    MyShortcuts.setDefaults("today_date", date, context);
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
        eveningRushHour.set(Calendar.HOUR, 4);
        eveningRushHour.set(Calendar.MINUTE, 45);
        eveningRushHour.set(Calendar.AM_PM, Calendar.PM);

        EndofeveningRushHour = Calendar.getInstance();
        EndofeveningRushHour.set(Calendar.HOUR, 7);
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

        Log.e("date  is", date);

        if (!MyShortcuts.checkDefaults("today_date", context)) {

            if (MyShortcuts.hasInternetConnected(context)) {
//                TODO getting weather condition here
//                MyShortcuts.setDefaults("today_date", date, context);
                getWeather();
            } else {
                Log.e("No internet", "no internet");
                Toast.makeText(context, "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_LONG);
                Log.e("weather", "No existing weather");
            }


        } else {
//            Log.e("dates sp and now", MyShortcuts.getDefaults("today_date", context) + " and " + date);
            if (MyShortcuts.getDefaults("today_date", context).equals(date) && MyShortcuts.checkDefaults("weather_today", context)) {
//                Log.e("weather", "weather already saved");
                rain = GetRain();
            } else {

                if (MyShortcuts.hasInternetConnected(context)) {
//                TODO getting weather condition here
                    Log.e("Yeei", "yeeiy");
                    getWeather();

                } else {
                    Log.e("No internet", "no internet");
                    Toast.makeText(context, "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_LONG);
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
        String jsonWeather = MyShortcuts.getDefaults("weather_today", context);
        Log.e("weather array", jsonWeather);
        try {
//Initialising variables
            Date targetTS = null;
//            Calendar c = Calendar.getInstance();
            Calendar c = the_date;
            int index = 0;
            long prevDiff = -1;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            Log.e("the_date  is", formattedDate);
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
//            System.out.println("Index: " + index);

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

//        day_of_the_week = getday();
        rush_ = getRushHour();
        current_rain = rain;
       /* if (Integer.parseInt(traffic) == 1 && rush_.equals("false")) {
            busy_road = "true";
        } else {
            busy_road = "false";
        }*/
//TODO getting all ***INGREDIENTS*** and saving it to the database
        Log.e("Current", day_of_the_week + "  day And " + rush_ +
                " rush hour And " + current_rain + " rain and " + busy_road + " road and traffic " + traff);
        return rain;


    }

}
