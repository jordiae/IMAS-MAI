



@XmlRootElement
public class SimulationSettings {

    String title;
    String algorithm;
    int classifiers;
    int[] trainingSettings;

    public int[] getTrainingSettings() {
        return trainingSettings;
    }

    @XmlElement
    public void setTrainingSettings(int[] trainingSettings) {
        this.trainingSettings = trainingSettings;
    }

    int classifierInstances;
    String file;

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    @XmlElement
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getClassifiers() {
        return classifiers;
    }

    @XmlElement
    public void setClassifiers(int classifiers) {
        this.classifiers = classifiers;
    }

    public int getClassifierInstances() {
        return classifierInstances;
    }

    @XmlElement
    public void setClassifierInstances(int classifierInstances) {
        this.classifierInstances = classifierInstances;
    }

    public String getFile() {
        return file;
    }

    @XmlElement
    public void setFile(String file) {
        this.file = file;
    }
}
