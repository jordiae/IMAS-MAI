package agents;

import behaviours.ClassifierBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import utils.Config;
import utils.Transformer;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import javafx.util.Pair;

public class ClassifierAgent extends Agent {
    private Classifier myClassifier = null;
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private Instances data;
    private String algorithm;

    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ClassifierAgent");
        sd.setName(getName());
        sd.setOwnership("IMAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            addBehaviour(new ClassifierBehaviour(this));
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "[" + getLocalName() + "] - Cannot register with DF", e);
            doDelete();
        }
    }

    public String checkAction(String config) throws IOException, ClassNotFoundException {
        if ((config == null) || ((config.charAt(0) != 'T') && (config.charAt(0) != 'P')) || (config.charAt(1) != '_')) {
            return "Configuration in classifier is not well defined";
        }
        else if (config.charAt(0) == 'T'){
            algorithm = config.substring(2,5);
            String strInstances = config.substring(6);
            data = Transformer.toInst(strInstances);
        }
        else if (config.charAt(0) == 'P'){
            String strInstances = config.substring(2);
            data = Transformer.toInst(strInstances);
        }
        return "";
    }

    public Pair<Boolean, Object> performAction(String action) throws IOException, ClassNotFoundException {
        if (action.equals("T")) {
            try {
                if (algorithm.equals("J48")) {
                    myClassifier = new J48();
                }
                else if (algorithm.equals("IBk")) {
                    myClassifier = new IBk();
                }
                else if (algorithm.equals("MLP")) {
                    myClassifier = new MultilayerPerceptron();
                }
                else {
                    return new Pair<>(false, "Algorithm not supported");
                }
                myClassifier.buildClassifier(data);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (action.equals("P")){
            try {
                int numInstances = data.numInstances();
                Collection<Double> results = new ArrayList<Double>();
                for (int instIdx = 0; instIdx < numInstances; instIdx++) {
                    Instance currInst = data.instance(instIdx);
                    Double y = myClassifier.classifyInstance(currInst);
                    results.add(y);
                }
                Serializable serResults = (Serializable) results;
                return new Pair<>(true, serResults);
            } catch (Exception e) {
                return new Pair<>(false, e.getMessage());
            }
        }
        return new Pair<>(false, "Action not supported");
    }
}
