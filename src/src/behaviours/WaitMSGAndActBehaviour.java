/*
package behaviours;

import agents.FIPARequestAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import utils.Transformer;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;



public class WaitMSGAndActBehaviour extends CyclicBehaviour {
    private FIPARequestAgent agent;

    public WaitMSGAndActBehaviour(FIPARequestAgent a) {
        super(a);
        agent = a;
    }

    public void action() {
        ACLMessage msg = agent.receive(); // Wait for message
        if(msg != null){
            ACLMessage reply = msg.createReply();

            if(msg.getPerformative()== ACLMessage.REQUEST){
                if
                String content = msg.getContent();
                AID manager_ID = msg.getSender();
                String conversationId = msg.getConversationId();

                    */
/*
                        Content is
                            T_(serialized instances object)
                        or
                            P_(serialized instances object)
                       response:
                            (train): Inform: agree
                                then: Inform: done
                                    OR:  Failure
                            (test): Inform: agree
                                then: Inform: results
                                  OR:  Failure
                     *//*

                if ((content == null) || ((content.charAt(0) != 'T') && (content.charAt(0) != 'P')) || (content.charAt(1) != '_')) {
                    myLogger.log(Logger.INFO, "[" + myAgent.getLocalName() + "] - Received badly formatted request header: ["+content+"] received from "+msg.getSender().getLocalName());
                    reply.setPerformative(ACLMessage.REFUSE);
                    myAgent.send(reply);
                } else if (content.charAt(0) == 'T') {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("agree");
                    myAgent.send(reply);
                    String inst_str = content.substring(2);
                    try {
                        Instances data = Transformer.toInst(inst_str);
                        myClassifier = new J48();
                        myClassifier.buildClassifier(data);
                        myLogger.log(Logger.INFO, "[" + myAgent.getLocalName() + "] trained classifier as requested by "+msg.getSender().getLocalName());
                        ACLMessage msg_cont = new ACLMessage(ACLMessage.INFORM);
                        msg_cont.setConversationId(conversationId);
                        msg_cont.addReceiver(manager_ID);
                        msg_cont.setContent("done");
                        myAgent.send(msg_cont);
                    } catch (Exception e) {
                        myLogger.log(Logger.INFO, "[" + myAgent.getLocalName() + "] - Received badly formatted Instances [" + content + "] received from " + msg.getSender().getLocalName());
                        ACLMessage msg_cont = new ACLMessage(ACLMessage.FAILURE);
                        msg_cont.setConversationId(conversationId);
                        msg_cont.addReceiver(manager_ID);
                        myAgent.send(msg_cont);
                    }
                } else {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("agree");
                    myAgent.send(reply);
                    String inst_str = content.substring(2);
                    try {
                        Instances data = Transformer.toInst(inst_str);
                        int numInstances = data.numInstances();
                        Collection<Double> results = new ArrayList<Double>();
                        for (int instIdx = 0; instIdx < numInstances; instIdx++) {
                            Instance currInst = data.instance(instIdx);
                            Double y = myClassifier.classifyInstance(currInst);
                            results.add(y);
                        }
                        myLogger.log(Logger.INFO, "[" + myAgent.getLocalName() + "] successfully performed prediction as requested by "+msg.getSender().getLocalName());
                        ACLMessage msg_cont = new ACLMessage(ACLMessage.INFORM);
                        msg_cont.setConversationId(conversationId);
                        msg_cont.addReceiver(manager_ID);
                        msg_cont.setContentObject((Serializable) results);
                        myAgent.send(msg_cont);
                    } catch (Exception e) {
                        myLogger.log(Logger.INFO, "[" + myAgent.getLocalName() + "] - Received badly formatted Instances ["+content+"] received from "+msg.getSender().getLocalName());
                        ACLMessage msg_cont = new ACLMessage(ACLMessage.FAILURE);
                        msg_cont.setConversationId(conversationId);
                        msg_cont.addReceiver(manager_ID);
                        myAgent.send(msg_cont);
                    }
                }
            }
            else {
                myLogger.log(Logger.INFO, "[" + super.myAgent.getLocalName() + "] - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
                myAgent.send(reply);
            }
        }
        else {
            block();
        }
    }
}
*/
