import java.io.Serializable;

public class Header implements Serializable {
    int portOrigin, portDestination;
    String hostOrigin, hostDestination, fileName;

    public Header(int portOrigin, int portDestination, String hostOrigin, String hostDestination, String fileName) {
        this.portOrigin = portOrigin;
        this.portDestination = portDestination;
        this.hostOrigin = hostOrigin;
        this.hostDestination = hostDestination;
        this.fileName = fileName;
    }
}
