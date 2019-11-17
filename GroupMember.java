import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class GroupMember{

    // Metodo que performa a logica de um membro nao coordenador
    public static void execute(int coordId){
        DatagramSocket serverSocket = null;
        DatagramSocket clientSocket = null;
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        try{
            serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
            serverSocket.setSoTimeout(10000); // 10 segundos acontece timeout
            clientSocket = new DatagramSocket();
        }
        catch(Exception exception){
            System.out.println("Excecao ao criar Datagrama: "+exception.toString());
            exception.printStackTrace();
        }

        try{
            while(true){

                System.out.println("Requisitando meu papel para coord...");
                // primeiro de tudo precisamos pedir ao coord que mande um ACK para sabermos que ele ja existe
                String message = "ACK-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                
                sendData = message.getBytes();
                Stats myCoord = Bully.neighbours.get(Bully.findMemberById(coordId)); // meu primeiro coord é o cara com id maior

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                clientSocket.send(sendPacket);

                // agora preciso receber a mensagem de retorno
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String array[] = sentence.split("-");
                String command = array[0];
                String role = array[1];

                if(command.equals("ACK")){  
                    if(role.equals("CONSUMER")){ // serei consumidor
                        System.out.println("Sou CONSUMER");
                        serverSocket.close();
                        clientSocket.close();
                        Consumer.execute(coordId);
                        return;
                    }
                    else{ // serei produtor
                        System.out.println("Sou PRODUCER");
                        serverSocket.close();
                        clientSocket.close();
                        Producer.execute(coordId);
                        return;
                    }
                }
            }
        }
        catch(SocketTimeoutException e){
            // coord esta morto? Espero algum tempo e peço de novo
            long lastPing = System.currentTimeMillis();
            while(true){

                long now = System.currentTimeMillis();

                if(((now - lastPing)/1000) >= 10){
                    execute(coordId);
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no groupMember: "+exception.toString());
            exception.printStackTrace();
        }
    }
}