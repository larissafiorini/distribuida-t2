
/**
 *  O nodo com o maior ID é o coordenador inicial, e será responsável por manter o estado de
 *  acesso aos semáforos. Os outros nodos enviam mensagens ao coordenador sempre que
 *  desejarem entrar e sair de uma região crítica. Se o coordenador não confirmar (a confirmação de
 *  recebimento deve ser feita mesmo quando negada a entrada na região crítica), então o deve-se
 *  iniciar uma eleição, de acordo com o algoritmo do valentão. Quando um novo coordenador for
 *  selecionado, os nodos devem começar a enviar as solicitações de entrada e saída de região
 *  crítica para o mesmo. É importante que o mecanismo seja validado eliminando-se o
 *  coordenador em algum momento. O coordenador deve ser o primeiro processo a ser carregado
 *  no ambiente para que o mecanismo funcione.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;

public class Bully {
  public static ArrayList<Stats> neighbours = new ArrayList<Stats>(); // lista de vizinhos
  public static Stats myStats; // meus dados de configuracao
  public static File criticalRegionFile = new File("/regiao-critica/arquivo.txt");
  public static ArrayList<Stats> entryQueue = new ArrayList<Stats>(); // fila de entrada na regiao critica
  public static boolean locked = true; // file lock variable
  public static Stats lockOwner = null; // current owner of the lock

  public static void main(String args[]) throws FileNotFoundException {
    File configFile = new File(args[0]);
    Scanner configReader = new Scanner(configFile);

    int myConfigLine = Integer.parseInt(args[1]);
    int aux = 1;
    int biggestId = 0;
    while(configReader.hasNextLine()){

      String helper = configReader.nextLine();
      String[] array = helper.split(" ");
      int id = Integer.parseInt(array[0]);
      String ip = array[1];
      int port = Integer.parseInt(array[2]);

      if(id > biggestId){
        biggestId = id;
      }
      if(aux == myConfigLine){
        myStats = new Stats(id,ip,port);
      }
      else{
        neighbours.add(new Stats(id,ip,port));
      }
      aux++;
    }
    
    if(myStats.idNumber == biggestId){ // na inicializacao do sistema o membro com id maior e o primeiro coord
      groupCoord();
    }
    else{
      groupMember();
    }
  }

  // Metodo que performa uma eleicao
  public static void callElection(){
    
    boolean kingOfTheHill = true;
    for(int i = 0; i < neighbours.size(); i++ ){
      /**
        • Processo P convoca uma eleição:
        • P envia msg de eleição para todos os processos com IDs maiores que o dele
        • Se ninguém responde, P vence eleição e torna-se coordenador
        • Se algum processo com ID maior responde, ele desiste
        • Quando processo recebe msg de eleição de membros com ID mais baixa
        • Envia OK para remetente para indicar que está vivo e convoca eleição
      */
    }

    if(kingOfTheHill){ // ganhou eleicao
      groupCoord();
    }
    else{ // perdeu eleicao
      groupMember();
    }
  }

  // Metodo que performa a logica de um membro nao coordenador
  public static void groupMember(){
    // primeiro de tudo precisamos pedir ao coord que mande um ACK para sabermos que ele ja existe
  }

  // Metodo que performa a logica de um membro coordenador
  public static void groupCoord(){

    try{
      DatagramSocket serverSocket = new DatagramSocket(myStats.portNumber,InetAddress.getByName(myStats.ipAddress));
      DatagramSocket clientSocket = new DatagramSocket();
      byte[] receiveData = new byte[1024];
      byte[] sendData = new byte[1024];

      while(true){
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        
        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
        String array[] = sentence.split("-");
        String request = array[0];
        int requesterId = Integer.parseInt(array[1]);
        String requesterIp = array[2];
        int requesterPort = Integer.parseInt(array[3]);
        Stats newRequester = new Stats(requesterId,requesterIp,requesterPort);
        // acho que segundo o algoritmo eu deveria mandar uma mensagem de ACK aqui para o requisitor

        if(request.equals("ENTER")){ // se o pedido for de entrada na area critica
          if(entryQueue.size() > 0 || (locked == true)){ // e a fila nao esta vazia ou o arquivo trancado
            entryQueue.add(newRequester); // entra na fila
            // enviar mensagem avisando que ele entrou na fila
            //String message = "";
            //sendData = message.getBytes();
            
            //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(destination_ip), destination_port);
            //clientSocket.send(sendPacket);
          }
          else{ // senao
            locked = true; // ganha o acesso a area critica
            lockOwner = newRequester;
            // enviar mensagem avisando que ele ganhou acesso
          }
        }
        else if(request.equals("LEAVE")){ // se o pedido for de saida da area critica
          if(lockOwner.idNumber == newRequester.idNumber){ // e ele for o dono atual do acesso ao arquivo
            locked = false; // libero o arquivo
            lockOwner = null; // removo o dono do acesso ao arquivo
            // envio mensagem avisando que liberei a tranca conforme ele pediu
          }
          else{
            // envio mensagem avisando que ele nao e o dono do acesso ao arquivo e algo esta errado
          }
        }
      }
      
    }
    catch(Exception exception){
      System.out.println("Excecao no coord: "+exception.getStackTrace());
    }
  }
}