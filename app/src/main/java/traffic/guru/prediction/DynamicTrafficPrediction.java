package traffic.guru.prediction;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
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

/**
 * Created by stephineosoro on 06/12/2016.
 */

public class DynamicTrafficPrediction {

    Context context;
    Calendar the_date;
    public static String return_data;

    public DynamicTrafficPrediction(Context context,Calendar date) {
        this.context = context;
        this.the_date=date;
    }


    private static String day_of_the_week, busy_road, current_rain, rush_, traffic, traff;
    private Calendar morningRushHour;
    private Calendar EndofMorningRushHour;
    private Calendar eveningRushHour;
    private Calendar EndofeveningRushHour;
    boolean rushhour = false;
    private String date, rain;
    private static String i_rain;
    private static boolean rush, wrain, rbusy;
    private static double Rain_P, Rain_A, Rain_N, Rush_P, Rush_N, Road_P, Road_N;
    private static double[] Traffic = new double[24];

/*    public String getPrediction() {
        getData();
        return return_data;
    }

    private void getData() {
        rush_ = getRushHour();
        current_rain = getCurrentWeather();
        onlinedata(new ServerCallback(){
            @Override
            public void onSuccess(String result){

            }
        });
       *//* if (MyShortcuts.hasInternetConnected(context)) {
            onlinedata();
        } else {

//          TODO  implement this offline data retrieval
            *//**//*JSONArray jsonArray = jsonObject.getJSONArray("data");
//                    Calling process data to process the actual data
            processData(jsonArray);
            //        After finishing processing, I call the main function which is responsible for doing prediction on the actual data
            try {
                main(true, true, true);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InconsistentEvidenceException e) {
                e.printStackTrace();
            }*//**//*
        }*//*
        rush = Boolean.parseBoolean(getRushHour());
        wrain = Boolean.parseBoolean(getCurrentWeather());

//        TODO get busy road from the data but for now am giving it a one
        rbusy = true;


    }*/

    public void onlinedata(final ServerCallback callback) {
        rush_ = getRushHour();
        current_rain = getCurrentWeather();
        rush = Boolean.parseBoolean(getRushHour());
        wrain = Boolean.parseBoolean(getCurrentWeather());
        //        TODO get busy road from the data but for now am giving it a one
        rbusy = true;
        Post.getData(MyShortcuts.dataURL() + "Traffic.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("All");
//                    Log.e("Array", jsonArray.toString());
//                    Calling process data to process the actual data
                    processData(jsonArray);
                    //        After finishing processing, I call the main function which is responsible for doing prediction on the actual data
                    try {
                       String val= main(true, true, true);
                        callback.onSuccess(val);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InconsistentEvidenceException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void processData(JSONArray array, boolean rh_b, boolean road_b, boolean rain_b) {
//        TODO obtain the data and process to get results of the probabilities and joint probabilites to be used by the Bayesian Network
        int rainP = 0, rainN = 0, rainA = 0, rushP = 0, rushN = 0, roadP = 0, roadN = 0, rttt = 0, rttf = 0, rtft = 0, rtff = 0, rftt = 0, rftf = 0, rfft = 0, rfff = 0, attt = 0, attf = 0, atft = 0, atff = 0, aftt = 0, aftf = 0, afft = 0, afff = 0, nttt = 0, nttf = 0, ntft = 0, ntff = 0, nftt = 0, nftf = 0, nfft = 0, nfff = 0;
        for (int i = 0; i < array.length(); i++) {
            String rain = null;
            String rush = null;
            String road = null;
            String traffic = null;
            try {
                JSONObject json = array.getJSONObject(i);
                rain = json.getString("rain");
                rush = json.getString("rush_hour");
                road = json.getString("road_busy");
                traffic = json.getString("traffic");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (rain.equals("strong")) {
                rainP++;
            } else if (rain.equals("average")) {
                rainA++;
            } else {
                rainN++;
            }

            if (rush.equals("true")) {
                rushP++;
            } else {
                rushN++;
            }

            if (road.equals("true")) {
                roadP++;
            } else {
                roadN++;
            }

            if (rain.equals("strong") && road.equals("true") && rush.equals("true") && traffic.equals("true")) {
                rttt++;
            } else if (rain.equals("strong") && road.equals("true") && rush.equals("true") && traffic.equals("false")) {
                rttf++;
            } else if (rain.equals("strong") && road.equals("true") && rush.equals("false") && traffic.equals("true")) {
                rtft++;
            } else if (rain.equals("strong") && road.equals("true") && rush.equals("false") && traffic.equals("false")) {
                rtff++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("true") && traffic.equals("true")) {
                rftt++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("true") && traffic.equals("false")) {
                rftf++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("false") && traffic.equals("true")) {
                rfft++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("false") && traffic.equals("false")) {
                rfff++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("true") && traffic.equals("true")) {
                attt++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("true") && traffic.equals("false")) {
                attf++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("false") && traffic.equals("true")) {
                atft++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("false") && traffic.equals("false")) {
                atff++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("true") && traffic.equals("true")) {
                aftt++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("true") && traffic.equals("false")) {
                aftf++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("false") && traffic.equals("true")) {
                afft++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("false") && traffic.equals("false")) {
                afff++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("true") && traffic.equals("true")) {
                nttt++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("true") && traffic.equals("false")) {
                nttf++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("false") && traffic.equals("true")) {
                ntft++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("false") && traffic.equals("false")) {
                ntff++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("true") && traffic.equals("true")) {
                nftt++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("true") && traffic.equals("false")) {
                nftf++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("false") && traffic.equals("true")) {
                nfft++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("false") && traffic.equals("false")) {
                nfff++;
            }


        }

//        Getting probability ditributions
        Rain_P = (double) rainP / array.length();
        Rain_N = (double) rainN / array.length();
        Rain_A = (double) rainA / array.length();
        Rush_P = (double) rushP / array.length();
        Rush_N = (double) rushN / array.length();
        Road_P = (double) roadP / array.length();
        Road_N = (double) roadN / array.length();
        double a = (double) rttt / (rttt + rttf);
        double b = (double) rttf / (rttt + rttf);
        double c = (double) rtft / (rtft + rtff);
        double d = (double) rtff / (rtft + rtff);
        double y = (double) rftt / (rftt + rftf);
        double f = (double) rftf / (rftt + rftf);
        double g = (double) rfft / (rfft + rfff);
        double h = (double) rfff / (rfft + rfff);
        double i = (double) attt / (attt + attf);
        double j = (double) attf / (attt + attf);
        double k = (double) atft / (atft + atff);
        double l = (double) atff / (atft + atff);
        double m = (double) aftt / (aftt + aftf);
        double n = (double) aftf / (aftt + aftf);
        double o = (double) afft / (afft + afff);
        double p = (double) afff / (afft + afff);
        double q = (double) nttt / (nttt + nttf);
        double r = (double) nttf / (nttt + nttf);
        double s = (double) ntft / (ntft + ntff);
        double t = (double) ntff / (ntft + ntff);
        double u = (double) nftt / (nftt + nftf);
        double v = (double) nftf / (nftt + nftf);
        double w = (double) nfft / (nfft + nfff);
        double x = (double) nfff / (nfft + nfff);


        double[] Traffi = new double[]{a, b, c, d, y, f, g, h,
                i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x};
        checkNaN(Traffi);

        Log.e("data", Rain_P + " " + Rain_P + " " + Rush_P + " " + Road_P + " " + Rain_N + " ");

        Log.e("rttt", Traffic[3] + "" + Traffic[0] + " ");

        /*try {
            main(rh_b, rain_b, road_b);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistentEvidenceException e) {
            e.printStackTrace();
        }*/
    }

    public void processData(JSONArray array) {
//        TODO obtain the data and process to get results of the probabilities and joint probabilites to be used by the Bayesian Network
        int rainP = 0, rainN = 0, rainA = 0, rushP = 0, rushN = 0, roadP = 0, roadN = 0, rttt = 0, rttf = 0, rtft = 0, rtff = 0, rftt = 0, rftf = 0, rfft = 0, rfff = 0, attt = 0, attf = 0, atft = 0, atff = 0, aftt = 0, aftf = 0, afft = 0, afff = 0, nttt = 0, nttf = 0, ntft = 0, ntff = 0, nftt = 0, nftf = 0, nfft = 0, nfff = 0;
        for (int i = 0; i < array.length(); i++) {
            String rain = null;
            String rush = null;
            String road = null;
            String traffic = null;
            try {
                JSONObject json = array.getJSONObject(i);
                rain = json.getString("rain");
                rush = json.getString("rushhour");
                road = json.getString("busyroad");
                traffic = json.getString("traffic");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (rain.equals("strong")) {
                rainP++;
            } else if (rain.equals("average")) {
                rainA++;
            } else {
                rainN++;
            }

            if (rush.equals("true")) {
                rushP++;
            } else {
                rushN++;
            }

            if (road.equals("true")) {
                roadP++;
            } else {
                roadN++;
            }

            if (rain.equals("strong") && road.equals("true") && rush.equals("true") && traffic.equals("true")) {
                rttt++;
            } else if (rain.equals("strong") && road.equals("true") && rush.equals("true") && traffic.equals("false")) {
                rttf++;
            } else if (rain.equals("strong") && road.equals("true") && rush.equals("false") && traffic.equals("true")) {
                rtft++;
            } else if (rain.equals("strong") && road.equals("true") && rush.equals("false") && traffic.equals("false")) {
                rtff++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("true") && traffic.equals("true")) {
                rftt++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("true") && traffic.equals("false")) {
                rftf++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("false") && traffic.equals("true")) {
                rfft++;
            } else if (rain.equals("strong") && road.equals("false") && rush.equals("false") && traffic.equals("false")) {
                rfff++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("true") && traffic.equals("true")) {
                attt++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("true") && traffic.equals("false")) {
                attf++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("false") && traffic.equals("true")) {
                atft++;
            } else if (rain.equals("average") && road.equals("true") && rush.equals("false") && traffic.equals("false")) {
                atff++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("true") && traffic.equals("true")) {
                aftt++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("true") && traffic.equals("false")) {
                aftf++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("false") && traffic.equals("true")) {
                afft++;
            } else if (rain.equals("average") && road.equals("false") && rush.equals("false") && traffic.equals("false")) {
                afff++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("true") && traffic.equals("true")) {
                nttt++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("true") && traffic.equals("false")) {
                nttf++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("false") && traffic.equals("true")) {
                ntft++;
            } else if (rain.equals("no_rain") && road.equals("true") && rush.equals("false") && traffic.equals("false")) {
                ntff++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("true") && traffic.equals("true")) {
                nftt++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("true") && traffic.equals("false")) {
                nftf++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("false") && traffic.equals("true")) {
                nfft++;
            } else if (rain.equals("no_rain") && road.equals("false") && rush.equals("false") && traffic.equals("false")) {
                nfff++;
            }


        }

//        Getting probability ditributions
        Rain_P = (double) rainP / array.length();
        Rain_N = (double) rainN / array.length();
        Rain_A = (double) rainA / array.length();
        Rush_P = (double) rushP / array.length();
        Rush_N = (double) rushN / array.length();
        Road_P = (double) roadP / array.length();
        Road_N = (double) roadN / array.length();
        double a = (double) rttt / (rttt + rttf);
        double b = (double) rttf / (rttt + rttf);
        double c = (double) rtft / (rtft + rtff);
        double d = (double) rtff / (rtft + rtff);
        double y = (double) rftt / (rftt + rftf);
        double f = (double) rftf / (rftt + rftf);
        double g = (double) rfft / (rfft + rfff);
        double h = (double) rfff / (rfft + rfff);
        double i = (double) attt / (attt + attf);
        double j = (double) attf / (attt + attf);
        double k = (double) atft / (atft + atff);
        double l = (double) atff / (atft + atff);
        double m = (double) aftt / (aftt + aftf);
        double n = (double) aftf / (aftt + aftf);
        double o = (double) afft / (afft + afff);
        double p = (double) afff / (afft + afff);
        double q = (double) nttt / (nttt + nttf);
        double r = (double) nttf / (nttt + nttf);
        double s = (double) ntft / (ntft + ntff);
        double t = (double) ntff / (ntft + ntff);
        double u = (double) nftt / (nftt + nftf);
        double v = (double) nftf / (nftt + nftf);
        double w = (double) nfft / (nfft + nfff);
        double x = (double) nfff / (nfft + nfff);


        double[] Traffi = new double[]{a, b, c, d, y, f, g, h,
                i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x};
        checkNaN(Traffi);

        Log.e("data", Rain_P + " " + Rain_P + " " + Rush_P + " " + Road_P + " " + Rain_N + " ");

        Log.e("rttt", Traffic[3] + "" + Traffic[0] + " ");

       /* try {
            main(true, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistentEvidenceException e) {
            e.printStackTrace();
        }*/
    }

    private void checkNaN(double[] Traf) {

        for (int i = 0; i < Traf.length; i++) {
            if (Double.isNaN(Traf[i])) {
                Traffic[i] = 0;
            } else {
                Traffic[i] = Traf[i];
            }
        }

        Log.e("Traffic 3 is", Traffic[3] + "" + Traffic[1]);
    }

    public String main(boolean rush_hour_boolean, boolean rain_boolean, boolean busy_road_boolean) throws IOException, InconsistentEvidenceException {

        // In context example we programatically create a simple Bayesian network.
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

        // at context point we have fully specified the structural (graphical) specification of the Bayesian Network.

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
        tableA.set(Rain_P, rTrue);
        tableA.set(Rain_A, rAverage);
        tableA.set(Rain_N, rFalse);

        // now tableA is correctly specified we can assign it to Node A;
        rain.setDistribution(tableA);


        // node B has node A as a parent, therefore its distribution will be P(B)

        Table tableB = road_busy.newDistribution().getTable();
        tableB.set(Road_P, rbTrue);
        tableB.set(Road_N, rbFalse);
        /*tableB.set(0.2, rTrue, rbTrue);
        tableB.set(0.8, rTrue, rbFalse);
        tableB.set(0.15, rFalse, rbTrue);
        tableB.set(0.85, rFalse, rbFalse);*/
        road_busy.setDistribution(tableB);


        // specify P(C)
        Table tableC = rush_hour.newDistribution().getTable();
        tableC.set(Rush_P, rhTrue);
        tableC.set(Rush_N, rhFalse);
        rush_hour.setDistribution(tableC);


        // specify P(D|A,B,C)
        Table tableD = traffic.newDistribution().getTable();

       /* // we could specify the values individually as above, or we can use a TableIterator as follows
        TableIterator iteratorD = new TableIterator(tableD, new Node[]{road_busy, rush_hour, traffic});
        iteratorD.copyFrom(new double[]{0.4, 0.6, 0.55, 0.45, 0.32, 0.68, 0.01, 0.99});
        traffic.setDistribution(tableD);*/

        // we could specify the values individually as above, or we can use a TableIterator as follows
        TableIterator iterator_D = new TableIterator(tableD, new Node[]{rain, road_busy, rush_hour, traffic});
        iterator_D.copyFrom(Traffic);
        traffic.setDistribution(tableD);


        // The network is now fully specified

        // If required the network can be saved...

       /* if (false)   // change context to true to save the network
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
        // context makes it easier to switch between them

        if (wrain) {
            inference.getEvidence().setState(rTrue);  // set Traffic = True
        } else {
            inference.getEvidence().setState(rFalse);  // set Traffic = False

        }

     /*   inference.getEvidence().setState(rTrue);
        inference.getEvidence().setState(rhTrue);
        */

        Table queryA = new Table(traffic);
        inference.getQueryDistributions().add(queryA);
        inference.query(queryOptions, queryOutput); // note that context can raise an exception (see help for details)

        System.out.println(String.format("P(traffic|rain=%s) = {" + queryA.get(tTrue) + "," + queryA.get(tFalse) + "}.", current_rain));


        // now lets calculate P(A|traffic=True, rush_hour=True)

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


        System.out.println(String.format("P(traffic|rain=%s, rush_hour=%s, road_busy=true) = {%s,%s}, log-likelihood = %s.", current_rain, rush_, queryA.get(tTrue), queryA.get(tFalse), queryOutput.getLogLikelihood()));
        return_data =queryA.get(tTrue)+"";

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

                    MyShortcuts.setDefaults("weather_today", save.toString(), context);
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

        if (!MyShortcuts.checkDefaults("today_date", context)) {

            if (MyShortcuts.hasInternetConnected(context)) {
//                TODO getting weather condition here
                MyShortcuts.setDefaults("today_date", date, context);
                getWeather();
            } else {
                Log.e("No internet", "no internet");
                Toast.makeText(context, "cannot predict traffic since today's weather information is not loaded yet", Toast.LENGTH_LONG);
                Log.e("weather", "No existing weather");
            }


        } else {
//            Log.e("dates sp and now", MyShortcuts.getDefaults("today_date", context) + " and " + date);
            if (MyShortcuts.getDefaults("today_date", context).equals(date)) {
//                Log.e("weather", "weather already saved");
                rain = GetRain();
            } else {

                if (MyShortcuts.hasInternetConnected(context)) {
//                TODO getting weather condition here
                    getWeather();
                    MyShortcuts.setDefaults("today_date", date, context);
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
