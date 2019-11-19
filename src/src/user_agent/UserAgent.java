package user_agent

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.io.File;
import jade.util.Logger;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;


const String CONFIG_FILE_PATH = "../config/imas.settings"

public class UserAgent extends Agent {
    private Logger logger = Logger.getMyLogger(getClass().getName());
    private SimulationSettings simulationSettings;

    public UserAgent() {
        // Load the simulation parameters
        simulationSettings = readConfigurationFile();
        
    }

    private class WaitUserInputBehaviour extends CyclicBehaviour {

        public WaitUserInputBehaviour(Agent a) {
            super(a);
        }

        public void action() {
//            String action = read_config_file();
            if (action() != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent(action);
                msg.addReceiver(new AID("ManagerAgent", AID.ISLOCALNAME));
                send(msg);
            }
        }
    }

    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("UserAgent");
        sd.setName(getName());
        sd.setOwnership("IMAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            WaitUserInputBehaviour UserBehaviour = new WaitUserInputBehaviour(this);
            addBehaviour(UserBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }
    }

    protected void takeDown() {
        //DF unregistration
        //Close any open/required resources
    }

    protected string read_user_input() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String action = reader.readLine();
        if(action == "T") {

            return "T";
        }
        else if (action == "P") {
            return "P";
        }

        else {
            System.out.println("Wrong action");
            System.out.println("USAGE: T <config_file> | P");
            return null;
        }
    }

    /**
     * This function handles the parsing of the configuration file in XML format to a SimulationSettings instance
     * @return SimulationSettings instance.
     */
    protected SimulationSettings readConfigurationFile() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SimulationSettings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SimulationSettings sim_settings = (SimulationSettings) jaxbUnmarshaller.unmarshal(new File(CONFIG_FILE_PATH));
        }catch (Exception e){
            System.out.println("Error while loading config file: ")
        }
        return sim_settings
    }
}