//~ package examples.Manager;
import jade.core.*;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import java.util.*;
import java.io.*;


//~ To compile an agent, located at IMAS-MAI/src/src/ do: where:
//~     %d is the path of the directory, 
//~     %f is the name without the path
//~     %e is the name without the path and without the extension:
//~ javac -cp %d/../../lib/jade.jar:%d/../../lib/weka.jar:%d/../out/production/IMAS_test  -d %d/../out/production/IMAS_test  %f
//~ java -cp %d/../../lib/jade.jar:%d/../../lib/weka.jar:%d/../out/production/IMAS_test/ jade.Boot -gui -agents %e:%e



public class ManagerAgent extends Agent {

    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private boolean trained = false;
    private Instances data;
    private int num_test_instances;
    private int num_classifiers;
    private int wainting_classifiers = 0;

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
                    else if (content.charAt(0) == 'T') { // T_order: Train phase T_IMAS_complete_simulation@J48@5@100@200@300@400@500@10@../data/segment-test.arff
                        try {
                            String config_file = content.substring(2);
                            System.out.println(config_file);
                            // get information from the content
                            String[] part = config_file.split("@");
                                //~ for (int i = 0; i < part.length; ++i){
                                    //~ System.out.println(part[i]);
                                //~ }
                            String title = part[0];
                                //~ System.out.println(title);
                            String algorithm = part[1];
                            num_classifiers = Integer.parseInt(part[2]);
                            int[] num_train_instances_for_classifier = new int[num_classifiers];
                            for (int i = 0; i < num_classifiers; ++i){
                                num_train_instances_for_classifier[i] = Integer.parseInt(part[3+i]);
                            }
                            num_test_instances = Integer.parseInt(part[3+num_classifiers]);
                            String name_of_data_file = "src/data/" + part[4+num_classifiers];
                            //~ System.out.println(name_of_data_file);
                            
                            //~ Create the classifiers (num_classifiers) in a container named ManagerController
                            
                            //~ New controller:##############################################
                                //~ jade.core.Runtime runtime = jade.core.Runtime.instance();
                                //~ Profile profile = new ProfileImpl();
                                //~ profile.setParameter(Profile.MAIN_HOST, "Managerhost");
                                //~ profile.setParameter(Profile.GUI, "true");
                                //~ AgentContainer ManagerController = runtime.createMainContainer(profile);
                            
                            //~ same old controller: ########################################
                            ContainerController ManagerController = getContainerController();

                            for(int i=0; i<num_classifiers; i++){
                               AgentController new_agent;
                               try {
                                   new_agent = ManagerController.createNewAgent("Classifier"+i, "ClassifierAgent", null);
                                   new_agent.start();    
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                            }

                            // load the arff file and separate the instances for the classifiers and for the test phase
                            BufferedReader reader = new BufferedReader(new FileReader(name_of_data_file));
                            ArffReader arff = new ArffReader(reader);
                            data = arff.getData();
                            data.setClassIndex(data.numAttributes() - 1);
                            String[] classifier_instances = new String[num_classifiers];
                            
                            // convert the instances to strings using the Transformer class
                            for(int i=0; i<num_classifiers; i++){
                                data.randomize(new java.util.Random(0));
                                classifier_instances[i] = Transformer.toString(new Instances(data, 0, num_train_instances_for_classifier[i]));
                            }
                            //~ String serialized_instances = Transformer.toString(data);
                            
                            
                            for(int i=0; i<num_classifiers; i++){
                                ACLMessage msg_to_classifiers = new ACLMessage( ACLMessage.REQUEST );
                                msg_to_classifiers.setContent("T_" + classifier_instances[i]);
                                AID a = new AID("Classifier"+i, false);
                                msg_to_classifiers.addReceiver(a);
                                send(msg_to_classifiers);
                            }
                            
                            // wait until all classifiers have been trained: to do so i think we should use a class variable and count the INFORM qith trained_successfully from a classifier.
                            // send a INFORM message to the user: Trained successfully: same way as sending messages to classifiers changing the dest.
                            
                            //debug INFORM message:
                            trained = true;
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" read message from "+msg.getSender().getLocalName());
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("Classifiers have been trained");
                            
                        } catch (Exception e) {
                            trained = false;
                            reply.setPerformative(ACLMessage.REFUSE);
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Got sent badly formatted Instances ["+content+"] received from "+msg.getSender().getLocalName());
                        }
                    } 
                    else {
                        if (trained) {
                            try {
                                // P_order: Test phase
                                // No content inside the message (maybe)
                                data.randomize(new java.util.Random(0));
                                String s_instances_to_classify = Transformer.toString(new Instances(data, 0, num_test_instances));
                                ACLMessage msg_to_classifiers = new ACLMessage(ACLMessage.REQUEST);
                                msg_to_classifiers.setContent("P_" + s_instances_to_classify);
                                for(int i=0; i<num_classifiers; i++){
                                    AID a = new AID("Classifier"+i, false);
                                    msg_to_classifiers.addReceiver(a);
                                    wainting_classifiers = wainting_classifiers + 1;
                                }
                                send(msg_to_classifiers);
                                
                                myLogger.log(Logger.INFO, "Agent "+getLocalName()+" read message from "+msg.getSender().getLocalName());
                                reply.setPerformative(ACLMessage.INFORM);
                                reply.setContent("test sent to classifiers");
                                // send the str_test_instances to the classifiers with P_str_test_instances.
                                // wait until all classifiers have classified the test instances (this time there are the same, and will be an instance of Serializable of new ArrayList<Double>())
                                // establish the winner class for each instances besed on our system
                                // return the winners in an INFORM message to the user: ((Serializable) ArrayList<Double>())
                            } catch (Exception e) {
                                reply.setPerformative(ACLMessage.REFUSE);
                                myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - extrange problem with Transformer ["+content+"] received from "+msg.getSender().getLocalName());
                            }                            
                        } else {
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Not trained yet ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent("Classifiers not trained yet");
                        }
                    }
                    send(reply);
                }
                else if (msg.getPerformative() == ACLMessage.INFORM) {
                    if (trained){
                        if (wainting_classifiers > 0){
                            try {
                                ACLMessage answer_to_user = new ACLMessage(ACLMessage.INFORM);
                                answer_to_user.setContent(msg.getContent());
                                AID u = new AID("UserAgent", false);
                                myLogger.log(Logger.INFO, "Agent "+getLocalName()+" returning classified instances to user");
                                answer_to_user.addReceiver(u);
                                wainting_classifiers = wainting_classifiers - 1;
                                send(answer_to_user);
                            } catch (Exception e) {
                                System.out.println("error in info" + msg.getContent());
                            }
                        } else { // trained ok from classifier
                            System.out.println(msg.getContent()); 
                        }
                    } else {
                        System.out.println("Not trained yet" + msg.getContent());
                    }
                }
                else {
                    //~ myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
                    System.out.println("The discussion ends here.");
                    //~ reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    //~ reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
                }
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

