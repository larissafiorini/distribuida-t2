import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * disponivel em: http://www.ic.unicamp.br/~islene/mc514/prod-cons/prod-cons.pdf
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class Producer{

    // Metodo que performa a logica de um produtor
    public static void execute(int coordId){

        try{ // TODO CODIGO ESTA QUEBRADO DAQUI EM DIANTE!!!!!!!!!!
            DatagramSocket serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
            DatagramSocket clientSocket = new DatagramSocket();
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            Stats myCoord = Bully.neighbours.get(coordId); // meu primeiro coord é o cara com id maior
            long lastPing = System.currentTimeMillis();
            while(true){ // fico enviando pedidos de acesso e producao de tempos em tempos
                long now = System.currentTimeMillis();

                if(((now - lastPing)/1000) >= 10){ // a cada dez segundos eu tento entrar
                    lastPing = System.currentTimeMillis();
                    String message = "ENTER-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                    sendData = message.getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                    clientSocket.send(sendPacket);
                    // agora preciso receber a mensagem de retorno
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String array[] = sentence.split("-");
                    String command = array[0];
                    String value = array[1];

                    if(command.equals("STATUS")){
                        if(value.equals("HASACCESS")){ // ganhei acesso, agora devo pedir para produzir e entao sair
                            message = "PRODUCE-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                            sendData = message.getBytes();

                            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                            clientSocket.send(sendPacket);
                            // agora preciso receber a mensagem de retorno
                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            command = array[0];
                            value = array[1];

                            // produzi, agora saio
                            message = "LEAVE-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                            sendData = message.getBytes();

                            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                            clientSocket.send(sendPacket);
                            // agora preciso receber a mensagem de retorno
                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.receive(receivePacket);
                            
                            sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                            array = sentence.split("-");
                            command = array[0];
                            value = array[1];
                        } 
                    }
                    else{ // se eu receber uma mensagem que nao reconheço 
                        throw new Exception("Mensagem Invalida Recebida: "+command); // lanco uma nova excessao
                    }
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no producer: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}