import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;

public class MainRoteador {
    public static DatagramSocket datagramSocket;
    public static String porta;
    public static boolean roteador;

    public static void main(String args[]){
        System.out.println("Inicializando Roteador");
        Scanner sc1 = new Scanner(System.in);

        System.out.println("Digite a porta desejada (3000 se este for o roteador): ");
        porta = sc1.next();

        if(porta.length() != 4 || !porta.matches("[0-9]+")){
            do {
                System.out.println("Inserir numero de porta valido: ");
                porta = sc1.next();
            }while (porta.length() != 4 || !porta.matches("[0-9]+"));
        }

        roteador = porta.equalsIgnoreCase("3000");

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
                    boolean achou = false;
                    ObjectInput in = null;
                    Header header;
                    byte[] fileContent;
                    try {
                        in = new ObjectInputStream(bis);
                        header = (Header) in.readObject();
                        fileContent = (byte[]) in.readObject();
                        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                        InetAddress inetAddress;
                        for (NetworkInterface nif: Collections.list(nets)) {
                            Enumeration<InetAddress> e = nif.getInetAddresses();
                            for (InetAddress inet:Collections.list(e)) {
                                if(header.hostDestination.equalsIgnoreCase(inet.getHostAddress())){
                                    inetAddress = inet;
                                    achou = true;
                                    break;
                                }
                                if (achou) break;
                            }
                        }

                        if(achou && header.portDestination == datagramSocket.getLocalPort()) {
                            try (FileOutputStream stream = new FileOutputStream("OutFiles/" + header.fileName)) {
                                stream.write(fileContent);
                            }
                            System.out.println(header.toString());
                        }else if(roteador){
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
                                if(achou){

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
                        else System.out.println("Ó MEU DEUS ALGO DEU MUITO ERRADO");
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

                    System.out.println("Digite o IP de Destino: ");
                    String servidor = inFromUser.readLine();

                    System.out.println("Digite a Porta de Destino: ");
                    String sPorta = inFromUser.readLine();
                    int iPorta = Integer.parseInt(sPorta);

                    InetAddress IPAddress = InetAddress.getByName(servidor);
                    InetAddress IPAddressHeader = InetAddress.getByName(servidor);
                    if (IPAddress.getHostAddress().equalsIgnoreCase(InetAddress.getLocalHost().getHostAddress())) IPAddress = InetAddress.getByName("localhost");
                    if (IPAddress.getHostAddress().equalsIgnoreCase("127.0.0.1")) IPAddressHeader = InetAddress.getLocalHost();

                    byte[] sendData = new byte[1024];

                    System.out.println("Digite o nome do arquivo: ");
                    String sentence = inFromUser.readLine();
                    sendData = sentence.getBytes();

                    InetAddress inetAddress = InetAddress.getLocalHost();

                    byte[] yourBytes = null;
                    File file = new File("In/" + sentence);
                    System.out.println("Enviado arquivo " + sentence + " para o destino " + IPAddressHeader.getHostAddress() + ":" + iPorta);
                    Header header = new Header(datagramSocket.getLocalPort(),iPorta,inetAddress.getHostAddress(),IPAddressHeader.getHostAddress(),sentence);
                    System.out.println("Header enviado: " + header.toString());

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

                    boolean achou = false;
                    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                    InetAddress inetA = null;
                    for (NetworkInterface nif: Collections.list(nets)) {
                        Enumeration<InetAddress> e = nif.getInetAddresses();
                        for (InetAddress inet:Collections.list(e)) {
                            if(header.hostDestination.equalsIgnoreCase(inet.getHostAddress())){
                                inetA = inet;
                                achou = true;
                                break;
                            }
                            if (achou) break;
                        }
                    }

                    DatagramPacket sendPacket;
                    if(achou){
                        sendPacket = new DatagramPacket(yourBytes,
                                yourBytes.length, inetA, iPorta);
                    }else if(porta.equalsIgnoreCase("3000")){
                        System.out.println("Enviando para roteador");
                        System.out.println(datagramSocket.getInetAddress().getHostAddress() + " -> " + IPAddress.getHostAddress());
                        sendPacket = new DatagramPacket(yourBytes,
                                yourBytes.length, IPAddress, 3000);
                    }else{
                        System.out.println("Enviando para roteador local");
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
