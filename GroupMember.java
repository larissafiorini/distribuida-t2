import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Codigo implementado baseado na definicao do problema de produtores e consumidores 
 * disponivel em: http://www.ic.unicamp.br/~islene/mc514/prod-cons/prod-cons.pdf
 * 
 * @author Igor Sgorla Brehm, Larissa Fiorini e Rodrigo Mello
 */

public class GroupMember{

    // Metodo que performa a logica de um membro nao coordenador
    public static void execute(int coordId){

        try{

            DatagramSocket serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
            DatagramSocket clientSocket = new DatagramSocket();
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            while(true){

                // primeiro de tudo precisamos pedir ao coord que mande um ACK para sabermos que ele ja existe
                String message = "ACK-"+Bully.myStats.idNumber+"-"+Bully.myStats.ipAddress+"-"+Bully.myStats.portNumber;
                sendData = message.getBytes();
                Stats myCoord = Bully.neighbours.get(coordId); // meu primeiro coord é o cara com id maior

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(myCoord.ipAddress), myCoord.portNumber);
                clientSocket.send(sendPacket);
                // agora preciso receber a mensagem de retorno
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                String array[] = sentence.split("-");
                String command = array[0];
                String role = array[1];

                if(command.equals("ACK")){ // coord esta vivo, posso prosseguir
                    if(role.equals("CONSUMER")){ // serei consumidor
                        serverSocket.close();
                        clientSocket.close();
                        Consumer.execute(coordId);
                    }
                    else{ // serei produtor
                        serverSocket.close();
                        clientSocket.close();
                        Producer.execute(coordId);
                    }
                    break;
                }
                else{ // coord esta morto? Espero algum tempo e peço de novo
                    long lastPing = System.currentTimeMillis();
                    while(true){

                        long now = System.currentTimeMillis();

                        if(((now - lastPing)/1000) >= 10){
                            break;
                        }
                    }
                }
            }
        }
        catch(Exception exception){
            System.out.println("Excecao no groupMember: "+exception.getMessage());
            System.out.println(exception.getStackTrace());
        }
    }
}