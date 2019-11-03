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
import java.util.Scanner;
import java.util.ArrayList;

public class Bully {
  public static ArrayList<String> neighbours = new ArrayList<String>();
  public static String myStats;
  public static void main(String args[]) throws FileNotFoundException {
    File configFile = new File(args[0]);
    Scanner configReader = new Scanner(configFile);

    int myConfigLine = Integer.parseInt(args[1]);
    int aux = 1;
    while(configReader.hasNextLine()){
      if(aux == myConfigLine){
        myStats = configReader.nextLine();
      }
      else{
        neighbours.add(configReader.nextLine());
      }
      aux++;
    }
    
    boolean kingOfTheHill = callElection();
    if(kingOfTheHill){
      // codigo de coordenador
    }
    else{
      // codigo de membro usuario
    }
  }

  // Metodo que performa uma eleicao
  public static boolean callElection(){
    return true;
  }
}