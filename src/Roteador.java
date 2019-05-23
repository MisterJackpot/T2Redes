import java.net.BindException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Roteador {

    public static DatagramSocket getUDPSocket(String port) throws SocketException {
        int porta = Integer.parseInt(port);
        boolean pos;
        DatagramSocket serverSocket = null;
        do {
            pos = false;
            try {
                serverSocket = new DatagramSocket(porta);
            } catch (BindException e) {
                System.out.println("!!Porta em Uso!!");
                porta++;
                System.out.println("Tentando Iniciar na porta: " + porta);
                pos = true;
            }
        }while(pos);
        return serverSocket;
    }


}
