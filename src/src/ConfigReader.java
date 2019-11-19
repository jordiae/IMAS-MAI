
public class ConfigReader {
    private String title;
    private String algorithm;
    private String classifiers;
    private String trainingSettings;
    private String classifierInstances;
    private String file;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(String classifiers) {
        this.classifiers = classifiers;
    }

    public String getTrainingSettings() {
        return trainingSettings;
    }

    public void setTrainingSettings(String trainingSettings) {
        this.trainingSettings = trainingSettings;
    }

    public String getClassifierInstances() {
        return classifierInstances;
    }

    public void setClassifierInstances(String classifierInstances) {
        this.classifierInstances = classifierInstances;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
