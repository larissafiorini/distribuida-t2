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
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;

public class Bully {
  public static ArrayList<Stats> neighbours = new ArrayList<Stats>(); // lista de vizinhos
  public static Stats myStats; // meus dados de configuracao
  public static File criticalRegionFile = new File("/regiao-critica/arquivo.txt");
  public static ArrayList<Stats> entryQueue = new ArrayList<Stats>(); // fila de entrada na regiao critica

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
    // implementar um servidor aqui
  }
}