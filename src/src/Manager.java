import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.core.Instance;
import weka.core.Instances;
import java.util.*;
import java.io.*;



public class Manager extends Agent {

    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    // Behaviour
    private class WaitOrderAndActBehaviour extends CyclicBehaviour {
        public WaitOrderAndActBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage  msg = myAgent.receive(); // Wait for message
            if (msg != null) {
                ACLMessage reply = msg.createReply();

                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    String content = msg.getContent();
                    if ((content == null) || ((content.charAt(0) != 'T') && (content.charAt(0) != 'P')) || (content.charAt(1) != '_')) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Got sent badly formatted request header: ["+content+"] received from "+msg.getSender().getLocalName());
                    } 
                    else if (content.charAt(0) == 'T') { // T_order: Train phase
                        String config_file_name = content.substring(2);
                        // get information from the content
                        // load the arff file and separate the instances for the classifiers and for the test phase
                        // convert the instances to strings using the Transformer class
                        // send the str_train_instances to the classifiers to train with T_str_train_instances, different number of instances for each classifier
                        // wait until all classifiers have been trained
                        // send a INFORM message to the user: Trained successfully
                    } 
                    else { // P_order: Test phase
                        // No content inside the message (maybe)
                        // send the str_test_instances to the classifiers with P_str_test_instances.
                        // wait until all classifiers have classified the test instances (this time there are the same, and will be an instance of Serializable of new ArrayList<Double>())
                        // establish the winner class for each instances besed on our system
                        // return the winners in an INFORM message to the user: ((Serializable) ArrayList<Double>())
                    }
                }
                else {
                    myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
                }
            }
            else {
                block;
            }
        }


    // setup
    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ManagerAgent");
        sd.setName(getName());
        sd.setOwnership("IMAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
            WaitMSGAndActBehaviour ManagerBehaviour = new  WaitMSGAndActBehaviour(this);
            addBehaviour(ManagerBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }
    }
}

