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
import weka.core.Instances;
import java.util.*;
import java.io.*;

public class ClassifierAgent extends Agent {
    private Classifier myClassifier = null;
    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    private class WaitMSGAndActBehaviour extends CyclicBehaviour {

        public WaitMSGAndActBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage  msg = myAgent.receive(); // Wait for message
            if(msg != null){
                ACLMessage reply = msg.createReply();

                if(msg.getPerformative()== ACLMessage.REQUEST){
                    String content = msg.getContent();

                    /*
                    Current idea would be:
                        Content is
                            T_(serialized instances object)
                        or
                            P_(serialized instances object)
                       response:
                            (train): Inform, content: ACK
                            (test): Inform, content: ?(Still to define)
                     */
                    if ((content == null) || ((content.charAt(0) != 'T') && (content.charAt(0) != 'T')) || (content.charAt(1) != '_')) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Got sent badly formatted request header: ["+content+"] received from "+msg.getSender().getLocalName());
                    } else if (content.charAt(0) == 'T') {
                        String inst_str = content.substring(2);
                        try {
                            Instances data = Transformer.toInst(content.substring(2));
                            myClassifier = new J48();
                            myClassifier.buildClassifier(data);
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" trained classifier as per request from "+msg.getSender().getLocalName());
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("Trained successfully");
                        } catch (Exception e) {
                            reply.setPerformative(ACLMessage.REFUSE);
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Got sent badly formatted Instances ["+content+"] received from "+msg.getSender().getLocalName());
                        }
                    } else {
                        String inst_str = content.substring(2);
                        try {
                            Instances data = Transformer.toInst(content.substring(2));
                            for(element in data){

                            }
                            myClassifier.classifyInstance()
                            myClassifier.buildClassifier(data);
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" trained classifier as per request from "+msg.getSender().getLocalName());
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("Trained successfully");
                        } catch (Exception e) {
                            reply.setPerformative(ACLMessage.REFUSE);
                            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Got sent badly formatted Instances ["+content+"] received from "+msg.getSender().getLocalName());
                        }
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
            WaitMSGAndActBehaviour PingBehaviour = new  WaitMSGAndActBehaviour(this);
            addBehaviour(PingBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }
    }
}