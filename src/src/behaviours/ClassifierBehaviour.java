package behaviours;
import agents.ClassifierAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;
import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;

enum ClassifierState {
    IDLE,
    FAILED,
    SUCCESS
}


public class ClassifierBehaviour extends CyclicBehaviour {
    private ClassifierAgent myAgent;
    private ClassifierState state;
    private ACLMessage requestMsg;
    private Serializable result;

    public ClassifierBehaviour (ClassifierAgent a) {
        super(a);
        myAgent = a;
        state = ClassifierState.IDLE;
    }

    public void action() {
        ACLMessage msg;
        switch(state) {
            case IDLE:
                msg = myAgent.blockingReceive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            System.out.println("Classifier - Received REQUEST");
                            requestMsg = msg;
                            String config = msg.getContent();
                            if (myAgent.checkAction(config)) {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.AGREE);
                                myAgent.send(response);
                                System.out.println("Classifier - Sent AGREE");

                                Pair<Boolean, Object> result =  myAgent.performAction(config.substring(0,1));

                                if (result.getKey()) {
                                    state=ClassifierState.SUCCESS;

                                }
                                else {
                                    state=ClassifierState.FAILED;
                                }
                            }
                            else {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.REFUSE);
                                myAgent.send(response);
                                System.out.println("Classifier - Sent REFUSE");
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case FAILED:
                ACLMessage resultFailed = requestMsg.createReply();
                resultFailed.setPerformative(ACLMessage.FAILURE);
                myAgent.send(resultFailed);
                System.out.println("Classifier - Sent FAILED");
                state = ClassifierState.IDLE;
                break;

            case SUCCESS:
                try {
                    ACLMessage resultSuccess = requestMsg.createReply();
                    resultSuccess.setPerformative(ACLMessage.INFORM);
                    resultSuccess.setContentObject(result);
                    myAgent.send(resultSuccess);
                    System.out.println("Classifier - Sent INFORM");
                    state = ClassifierState.IDLE;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
