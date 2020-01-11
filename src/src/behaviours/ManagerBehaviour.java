package behaviours;
import agents.ManagerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;
import utils.Config;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

enum ManagerState {
    IDLE,
    WAITING_RESPONSES,
    WAITING_RESULTS,
    FAILED,
    SUCCESS
}

public class ManagerBehaviour extends CyclicBehaviour {
    private ManagerAgent myAgent;
    private ManagerState state = ManagerState.IDLE;
    private int numResponders = 0;
    private int numResults = 0;
    private ArrayList<Serializable> results;
    private ACLMessage requestMsg;
    private String finalResult;

    public ManagerBehaviour (ManagerAgent a) {
        super(a);
        myAgent = a;
        state = ManagerState.IDLE;
    }

    public void action() {
        ACLMessage msg;
        switch(state) {
            case IDLE:
                msg = myAgent.blockingReceive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            System.out.println("Manager - Received REQUEST");
                            requestMsg = msg;
                            results = new ArrayList<>(0);
                            Config config = (Config) msg.getContentObject();
							String error = myAgent.checkAction(config);
                            if (error.equals("")) {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.AGREE);
                                myAgent.send(response);
                                System.out.println("Manager - Sent AGREE");

                                ACLMessage[] messages = myAgent.performAction(config.getAction());
                                numResponders = messages.length;
                                numResults = messages.length;
                                state = ManagerState.WAITING_RESPONSES;
                                for (ACLMessage message : messages) {
                                    myAgent.send(message);
                                    System.out.println("Manager - Sent REQUEST");
                                }
                            }
                            else {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.REFUSE);
								response.setContent(error);
                                myAgent.send(response);
                                System.out.println("Manager - Sent Refuse");
                            }
                        }
                    }
                    catch (UnreadableException | IOException | StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case WAITING_RESPONSES:
                msg = myAgent.blockingReceive(3000);
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.AGREE) {
                        System.out.println("Manager - Received AGREE");
                        --numResponders;
                        if (numResponders == 0) {
                            state = ManagerState.WAITING_RESULTS;
                        }
                    }
                    else if (msg.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("Manager - Received REFUSE");
                        state = ManagerState.FAILED;
                    }
                }
                else {
                    state = ManagerState.FAILED;
                }
                break;
            case WAITING_RESULTS:
                msg = myAgent.blockingReceive(3000);
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        System.out.println("Manager - Received INFORM");
                        --numResults;
                        try {
                            results.add(msg.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        if (numResults == 0) {
                            state = ManagerState.SUCCESS;
                            finalResult = myAgent.treatResults(results);
                        }
                    } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                        System.out.println("Manager - Received FAILURE");
                        state = ManagerState.FAILED;
                    }
                }
                else {
                    state = ManagerState.FAILED;
                }

                break;
            case FAILED:
                ACLMessage resultFailed = requestMsg.createReply();
                resultFailed.setPerformative(ACLMessage.FAILURE);
                resultFailed.setContent(finalResult);
                myAgent.send(resultFailed);
                System.out.println("Manager - Sent FAILURE");
                state = ManagerState.IDLE;
                break;

            case SUCCESS:
                ACLMessage resultSuccess = requestMsg.createReply();
                resultSuccess.setPerformative(ACLMessage.INFORM);
                resultSuccess.setContent(finalResult);
                myAgent.send(resultSuccess);
                System.out.println("Manager - Sent INFORM");
                state = ManagerState.IDLE;
                break;
        }
    }
}
