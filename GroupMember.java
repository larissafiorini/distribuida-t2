import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GroupMember{

    /**
    *  Se o coordenador não confirmar (a confirmação de
    *  recebimento deve ser feita mesmo quando negada a entrada na região crítica), então o deve-se
    *  iniciar uma eleição, de acordo com o algoritmo do valentão. Quando um novo coordenador for
    *  selecionado, os nodos devem começar a enviar as solicitações de entrada e saída de região
    *  crítica para o mesmo. É importante que o mecanismo seja validado eliminando-se o
    *  coordenador em algum momento. O coordenador deve ser o primeiro processo a ser carregado
    *  no ambiente para que o mecanismo funcione.
    */

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
                        Consumer.execute(coordId);
                    }
                    else{ // serei produtor
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