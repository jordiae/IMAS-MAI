package agents;

import behaviours.WaitMSGAndActBehaviour;
import utils.Transformer;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import java.util.*;
import java.io.*;

public class ClassifierAgent extends Agent {
    private Classifier myClassifier = null;
    private Logger myLogger = Logger.getMyLogger(getClass().getName());




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
            DFService.register(this,dfd);
            WaitMSGAndActBehaviour PingBehaviour = new WaitMSGAndActBehaviour(this, myLogger, myClassifier);
            addBehaviour(PingBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "[" + getLocalName() + "] - Cannot register with DF", e);
            doDelete();
        }
    }
}
