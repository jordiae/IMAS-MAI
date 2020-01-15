package behaviours;
import agents.ClassifierAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;

enum ReceiverState {
    IDLE,
    FAILED,
    SUCCESS
}


public class FIPAReceiverBehaviour extends CyclicBehaviour {
    private ClassifierAgent myAgent;
    private ReceiverState state;
    private ACLMessage requestMsg;
    private Serializable result;

    public FIPAReceiverBehaviour(ClassifierAgent a) {
        super(a);
        myAgent = a;
        state = ReceiverState.IDLE;
    }

    public void action() {
        ACLMessage msg;
        switch(state) {
            case IDLE:
                msg = myAgent.blockingReceive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
//                            System.out.println("Classifier - Received REQUEST");
                            requestMsg = msg;
                            String config = msg.getContent();

                            String error = myAgent.checkAction(config);
                            if (error.equals("")) {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.AGREE);
                                myAgent.send(response);
//                                System.out.println("Classifier - Sent AGREE");

                                Pair<Boolean, Object> res =  myAgent.performAction(config.substring(0,1));

                                if (res.getKey()) {
                                    state = ReceiverState.SUCCESS;
                                    result = (Serializable) res;
                                }
                                else {
                                    state = ReceiverState.FAILED;
                                }
                            }
                            else {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.REFUSE);
                                response.setContent(error);
                                myAgent.send(response);
//                                System.out.println("Classifier - Sent REFUSE");
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
//                System.out.println("Classifier - Sent FAILED");
                state = ReceiverState.IDLE;
                break;

            case SUCCESS:
                try {
                    ACLMessage resultSuccess = requestMsg.createReply();
                    resultSuccess.setPerformative(ACLMessage.INFORM);
                    resultSuccess.setContentObject(result);
                    myAgent.send(resultSuccess);
//                    System.out.println("Classifier - Sent INFORM");
                    state = ReceiverState.IDLE;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
