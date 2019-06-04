import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class MainRoteador {
    public static DatagramSocket datagramSocket;
    public static String porta;

    public static void main(String args[]){
        System.out.println("Inicializando Roteador");
        Scanner sc1 = new Scanner(System.in);

        System.out.println("Digite a porta desejada: ");
        porta = sc1.next();

        if(porta.length() != 4 || !porta.matches("[0-9]+")){
            do {
                System.out.println("Inserir numero de porta valido: ");
                porta = sc1.next();
            }while (porta.length() != 4 || !porta.matches("[0-9]+"));
        }

        try {
            datagramSocket = Roteador.getUDPSocket(porta);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        System.out.println("Rodando na porta: " + datagramSocket.getLocalPort());

        inServer();

        outServer();

    }

    public static void inServer(){
        new Thread(() -> {
            do {
                byte[] receiveData = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                int numConn = 1;
                try {
                    System.out.println("Esperando por datagrama UDP na porta " + datagramSocket.getLocalPort());
                    datagramSocket.receive(datagramPacket);
                    System.out.println("Datagrama UDP [" + numConn + "] recebido...");
                    ByteArrayInputStream bis = new ByteArrayInputStream(datagramPacket.getData());
                    ObjectInput in = null;
                    Header header;
                    byte[] fileContent;
                    try {
                        in = new ObjectInputStream(bis);
                        header = (Header) in.readObject();
                        fileContent = (byte[]) in.readObject();
                        InetAddress inetAddress = InetAddress.getLocalHost();
                        if((inetAddress.getHostAddress().equalsIgnoreCase(header.hostDestination) && header.portDestination == datagramSocket.getLocalPort()) ) {
                            try (FileOutputStream stream = new FileOutputStream("OutFiles/" + header.fileName)) {
                                stream.write(fileContent);
                            }
                            System.out.println(header.toString());
                        }else{
                            System.out.println("Roteando:\n" + header.toString());
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutput out = null;
                            byte[] yourBytes = null;
                            try {
                                out = new ObjectOutputStream(bos);
                                out.writeObject(header);
                                out.writeObject(fileContent);
                                out.flush();
                                yourBytes = bos.toByteArray();
                            } finally {
                                try {
                                    bos.close();
                                } catch (IOException ex) {
                                    System.out.println("ERRO");
                                }
                            }

                            DatagramSocket clientSocket = new DatagramSocket();
                            InetAddress IPAddress = InetAddress.getByName(header.hostDestination);
                            DatagramPacket sendPacket = null;
                            if(porta.equals("3000")){
                                if(inetAddress.getHostAddress().equalsIgnoreCase(header.hostDestination)){

                                    sendPacket = new DatagramPacket(yourBytes,
                                            yourBytes.length, IPAddress, header.portDestination);
                                }else{
                                    sendPacket = new DatagramPacket(yourBytes,
                                            yourBytes.length, IPAddress, 3000);
                                }
                            }
                            clientSocket.send(sendPacket);

                            clientSocket.close();

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }while (true);
        }).start();
    }

    public static void outServer(){
        new Thread(() -> {
            do {
                try {
                    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
                            System.in));

                    DatagramSocket clientSocket = new DatagramSocket();

                    System.out.println("Digite o IP: ");
                    String servidor = inFromUser.readLine();

                    System.out.println("Digite a Porta: ");
                    String sPorta = inFromUser.readLine();
                    int iPorta = Integer.parseInt(sPorta);

                    InetAddress IPAddress = InetAddress.getByName(servidor);

                    byte[] sendData = new byte[1024];

                    System.out.println("Digite o nome do arquivo: ");
                    String sentence = inFromUser.readLine();
                    sendData = sentence.getBytes();

                    InetAddress inetAddress = InetAddress.getLocalHost();

                    byte[] yourBytes = null;
                    File file = new File("In/" + sentence);
                    Header header = new Header(datagramSocket.getLocalPort(),iPorta,inetAddress.getHostAddress(),IPAddress.getHostAddress(),sentence);
                    byte[] fileContent = Files.readAllBytes(file.toPath());

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = null;

                    try {
                        out = new ObjectOutputStream(bos);
                        out.writeObject(header);
                        out.writeObject(fileContent);
                        out.flush();
                        yourBytes = bos.toByteArray();
                    } finally {
                        try {
                            bos.close();
                        } catch (IOException ex) {
                            // ignore close exception
                        }
                    }
                    DatagramPacket sendPacket;
                    if(IPAddress.isAnyLocalAddress() || IPAddress.isLoopbackAddress()){
                        sendPacket = new DatagramPacket(yourBytes,
                                yourBytes.length, IPAddress, iPorta);
                    }else if(porta.equalsIgnoreCase("3000")){
                        sendPacket = new DatagramPacket(yourBytes,
                                yourBytes.length, IPAddress, 3000);
                    }else{
                        sendPacket = new DatagramPacket(yourBytes,
                                yourBytes.length, inetAddress, 3000);
                    }
                    clientSocket.send(sendPacket);

                    clientSocket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }while (true);
        }).start();
    }
}
