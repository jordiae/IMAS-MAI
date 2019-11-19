import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import jade.util.Logger;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class UserAgent extends Agent {
    private String CONFIG_FILE_PATH = "src/config/";
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private ConfigReader configReader;

    private class WaitUserInputBehaviour extends CyclicBehaviour {

        public WaitUserInputBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            String user_input = read_user_input();
            String[] words = user_input.split(" ");

            if (!user_input.isEmpty()) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                if (words[0].equals("T")) {
                    // Load the simulation parameters
                    boolean paramsReady = readConfigFile(words[1]);

                    // Pass the simulation settings to the manager agent on creation, with a format:
                    // Title, algorithm, classifiers, training settings, classifier instances and database file.
                    // e.g. 'T_IMAS_complete_simulation@J48@5@100@200@300@400@500@10@segment-test.arff'
                    String arguments = words[0] + "_" + configReader.getTitle() + '@' + configReader.getAlgorithm() + '@' +
                            configReader.getClassifiers() + '@' +
                            configReader.getTrainingSettings().replace(',', '@') + '@' +
                            configReader.getClassifierInstances() + "@" +
                            configReader.getFile();
                    msg.setContent(arguments);
                }
                else {
                    msg.setContent(words[0]+"_");
                }
                msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
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

//        this.readConfigFile("imas.settings");

        try {
            DFService.register(this,dfd);
            WaitUserInputBehaviour UserBehaviour = new  WaitUserInputBehaviour(this);
            addBehaviour(UserBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }

        // Dynamic creation of the Manager Agent
        ContainerController cc = getContainerController();
        try {
            AgentController ac = cc.createNewAgent("manager", "ManagerAgent", null);
            try {
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        //DF unregistration
        //Close any open/required resources
    }

    private String read_user_input() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String action = reader.readLine();

            if(action.startsWith("T") || action.startsWith("P")) {
                return action;
            }

            else {
                myLogger.log(Logger.INFO, "Wrong action");
                myLogger.log(Logger.INFO, "USAGE: T <config_file> | P");
                return null;
            }
        }
        catch (IOException e) {
            myLogger.log(Logger.INFO, "Wrong action");
            myLogger.log(Logger.INFO, "USAGE: T <config_file> | P");
            e.printStackTrace();
            return null;
        }
    }

    private boolean readConfigFile(String file_name) {
        myLogger.log(Logger.INFO, "Loading simulation parameters from: " + this.CONFIG_FILE_PATH + '/' + file_name);

        configReader = new ConfigReader();

        File fXmlFile = new File(this.CONFIG_FILE_PATH + '/' + file_name);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(fXmlFile);
            document.getDocumentElement().normalize();

            //Here comes the root node
            Element root = document.getDocumentElement();
            NodeList nList = document.getElementsByTagName("SimulationSettings");

            System.out.println(nList.getLength());
            Node nNode = nList.item(0);
            Element rootElement = (Element) nNode;

            myLogger.log(Logger.INFO, "Node " + rootElement.getAttributes().getLength());
            configReader.setTitle(rootElement.getElementsByTagName("title").item(0).getTextContent());
            configReader.setAlgorithm(rootElement.getElementsByTagName("algorithm").item(0).getTextContent());
            configReader.setClassifiers(rootElement.getElementsByTagName("classifiers").item(0).getTextContent());
            configReader.setTrainingSettings(rootElement.getElementsByTagName("trainingSettings").item(0).getTextContent());
            configReader.setClassifierInstances(rootElement.getElementsByTagName("classifyInstances").item(0).getTextContent());
            configReader.setFile(rootElement.getElementsByTagName("file").item(0).getTextContent());
            myLogger.log(Logger.INFO, "Simulation " + configReader.getTitle() + " config loaded!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}