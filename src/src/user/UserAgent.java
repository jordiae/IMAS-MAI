import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

// XML Reading imports
import java.io.File;

const String CONFIG_FILE_PATH = "../config/imas.settings"

public class ClassifierAgent extends Agent {

    private Logger logger = Logger.getMyLogger(getClass().getName());
    private SimulationSettings simulationSettings;

    public ClassifierAgent() {
        // To-Do: load simulation settings from XML as class object
    }

    protected void read_config_file() {
        JAXBContext jaxbContext = JAXBContext.newInstance(SimulationSettings.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Customer customer = (Customer) jaxbUnmarshaller.unmarshal(new File(CONFIG_FILE_PATH));
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
            logger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }
    }
}
