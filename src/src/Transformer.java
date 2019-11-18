import java.util.*;
import java.io.*;
import weka.core.Instances;

public class Transformer {
    public static Instances toInst(String a) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(a);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Instances o  = (Instances) ois.readObject();
        ois.close();
        return o;
    }

    public static String toString(Instances a) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(a);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}
