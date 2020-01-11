package behaviours;

import agents.UserAgent;
import jade.core.behaviours.CyclicBehaviour;
import utils.Config;

public class ReadUserInputBehaviour extends CyclicBehaviour {
    private UserAgent myAgent;
    private String CONFIG_FILE_PATH = "src/config/";

    public ReadUserInputBehaviour(UserAgent a) {
        super(a);
        myAgent = a;
    }

    public void action() {
        String userInput = myAgent.readUserInput();

        String[] words = userInput.split(" ");

        String defaultConfigFile = "imas.settings";
        String action = words[0];
        if (words.length == 2) {
            defaultConfigFile = words[1];
        }

        Config config = new Config(action, CONFIG_FILE_PATH + "/" + defaultConfigFile);
        myAgent.startAction(config);
    }
}