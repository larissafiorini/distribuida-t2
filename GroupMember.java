import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;

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
        DatagramSocket serverSocket = new DatagramSocket(myStats.portNumber,InetAddress.getByName(myStats.ipAddress));
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        // primeiro de tudo precisamos pedir ao coord que mande um ACK para sabermos que ele ja existe
        String message = "ACK-"+myStats.idNumber+"-"+myStats.ipAddress+"-"+myStats.portNumber;
        sendData = message.getBytes();
        Stats myCoord = neighbours.get(coordId); // meu primeiro coord é o cara com id maior

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(), requesterPort);
        clientSocket.send(sendPacket);
        // agora preciso receber a mensagem de retorno
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        
        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
        String array[] = sentence.split("-");
    }
}