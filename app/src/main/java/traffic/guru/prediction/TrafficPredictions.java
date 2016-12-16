package traffic.guru.prediction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bayesserver.*;
import com.bayesserver.inference.*;

//import javax.xml.stream.*;
import java.io.*;

/**
 * Created by stephineosoro on 06/12/2016.
 */

public class TrafficPredictions {
    public static void main(boolean rush_hour_boolean, boolean rain_boolean) throws IOException, InconsistentEvidenceException {

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

        inference.getEvidence().setState(rTrue);  // set Traffic = True

        Table queryA = new Table(traffic);
        inference.getQueryDistributions().add(queryA);
        inference.query(queryOptions, queryOutput); // note that this can raise an exception (see help for details)

        System.out.println("P(traffic|rain=Average) = {" + queryA.get(tTrue) + "," + queryA.get(tFalse) + "}.");

        // Expected output ...
        // P(A|D=True) = {0.0980748663101604,0.90192513368984}

        // to perform another query we reuse all the objects

        // now lets calculate P(A|traffic=True, rush_hour=True)
        inference.getEvidence().setState(rhTrue);
        inference.getEvidence().setState(rbTrue);

        // we will also return the log-likelihood of the case
        queryOptions.setLogLikelihood(true); // only request the log-likelihood if you really need it, as extra computation is involved

        inference.query(queryOptions, queryOutput);
        System.out.println(String.format("P(traffic|rain=True, rush_hour=True, road_busy=true) = {%s,%s}, log-likelihood = %s.", queryA.get(tTrue), queryA.get(tFalse), queryOutput.getLogLikelihood()));

        // Expected output ...
        // P(A|D=True, C=True) = {0.0777777777777778,0.922222222222222}, log-likelihood = -2.04330249506396.


        // Note that we can also calculate joint queries such as P(A,B|D=True,C=True)

    }

}
