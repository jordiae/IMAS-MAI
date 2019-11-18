//~ package examples.Manager;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
//~ import weka.core.Instance;
//~ import weka.core.Instances;
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
                    else if (content.charAt(0) == 'T') { // T_order: Train phase T_IMAS_complete_simulation+J48+5+100+200+300+400+500+10+segment-test.arff
                        try {
                            String config_file = content.substring(2);
                            System.out.println(config_file);
                            // get information from the content
                            String[] part = config_file.split("@");
                            for (int i = 0; i < part.length; ++i){
                                System.out.println(part[i]);
                            }
                            String title = part[0];
                            System.out.println(title);
                            String algorithm = part[1];
                            int num_classifiers = Integer.parseInt(part[2]);
                            int[] num_train_instances_for_classifier = new int[num_classifiers];
                            for (int i = 0; i < num_classifiers; ++i){
                                num_train_instances_for_classifier[i] = Integer.parseInt(part[3+i]);
                            }
                            int num_test_instances = Integer.parseInt(part[3+num_classifiers]);
                            String name_of_data_file = part[4+num_classifiers];

                            // load the arff file and separate the instances for the classifiers and for the test phase
                            // address_of_data_file = "src/data" + name_of_data_file;
                            // BufferedReader reader = new BufferedReader(new FileReader(address_of_data_file));
                            // ArffReader arff = new ArffReader(reader);
                            // Instances data = arff.getData();
                            // data.setClassIndex(data.numAttributes() - 1);
                            // int num_instances = Instances.sampleSize;

                            //debug INFORM message:
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" read message from "+msg.getSender().getLocalName());
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(name_of_data_file);
                            
                            // convert the instances to strings using the Transformer class


                            // send the str_train_instances to the classifiers to train with T_str_train_instances, different number of instances for each classifier
                            // wait until all classifiers have been trained
                            // send a INFORM message to the user: Trained successfully
                        } catch (Exception e) {
                            reply.setPerformative(ACLMessage.REFUSE);
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Got sent badly formatted Instances ["+content+"] received from "+msg.getSender().getLocalName());
                        }
                    } 
                    else { // P_order: Test phase
                        // No content inside the message (maybe)
                        // send the str_test_instances to the classifiers with P_str_test_instances.
                        // wait until all classifiers have classified the test instances (this time there are the same, and will be an instance of Serializable of new ArrayList<Double>())
                        // establish the winner class for each instances besed on our system
                        // return the winners in an INFORM message to the user: ((Serializable) ArrayList<Double>())
                        myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Not trained yet ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Classifiers not trained yet");
                    }
                }
                else {
                    myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
                }
                send(reply);
            }
            else {
                block();
            }
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
            WaitOrderAndActBehaviour ManagerBehaviour = new  WaitOrderAndActBehaviour(this);
            addBehaviour(ManagerBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }
    }
}

