import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Consumer{

    /**
    *  Se o coordenador não confirmar (a confirmação de
    *  recebimento deve ser feita mesmo quando negada a entrada na região crítica), então o deve-se
    *  iniciar uma eleição, de acordo com o algoritmo do valentão. Quando um novo coordenador for
    *  selecionado, os nodos devem começar a enviar as solicitações de entrada e saída de região
    *  crítica para o mesmo. É importante que o mecanismo seja validado eliminando-se o
    *  coordenador em algum momento. O coordenador deve ser o primeiro processo a ser carregado
    *  no ambiente para que o mecanismo funcione.
    */

    // Metodo que performa a logica de um consumidor
    public static void execute(int coordId){

        try{
            DatagramSocket serverSocket = new DatagramSocket(Bully.myStats.portNumber,InetAddress.getByName(Bully.myStats.ipAddress));
            DatagramSocket clientSocket = new DatagramSocket();
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            Stats myCoord = Bully.neighbours.get(coordId); // meu primeiro coord é o cara com id maior

            while(true){ // fico enviando pedidos de acesso e consumo de tempos em tempos
                // TODO
            }
        }
        catch(Exception exception){

        }
    }
}