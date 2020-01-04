package agents;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FIPARequestAgent extends Agent{
    public void agreed() {}
    public void refused() {}
    public void resultDone() {}
    public void failed() {}
    public boolean checkAction(ACLMessage msg) throws UnreadableException, IOException {
        return false;
    }
    public boolean performAction(ACLMessage msg) throws UnreadableException, IOException, StaleProxyException {
        return false;
    }
}
