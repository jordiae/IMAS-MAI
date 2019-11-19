import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConfigReader {
    private String title;
    private String algorithm;
    private int classifiers;
    private String trainingSettings;
    private int classifierInstances;
    private String file;

    public String getTrainingSettings() {
        return trainingSettings;
    }

    @XmlElement
    public void setTrainingSettings(String trainingSettings) {
        this.trainingSettings = trainingSettings;
    }


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
