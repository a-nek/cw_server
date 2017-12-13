package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Александр on 10.10.2017.
 * Поток, который будет слушать порт
 * и принимать решения пользователей,
 * чтобы в поместить их в очередь (DecisionQueue.java)
 * для проверки
 */
public class MainController extends Thread{
    // Создаем очередь задач на обработку
    private DecisionQueue decisionQueue;

    // конструктор
    public MainController() {
        // инициализируем очередь
        decisionQueue = new DecisionQueue();

        // запускаем поток
        decisionQueue.start();
    }

    public void run(){
        try {
            listener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // будет слушать порт
    public void listener() throws IOException {

        // создаем сокет
        ServerSocket serverSocket = new ServerSocket(7777);
        while(true){
            //System.out.println("Основной поток работает");
            Socket s = serverSocket.accept();//new Socket("127.0.0.1",1235);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write("q");
            bw.flush();

            String line = "";

            String userLogin = br.readLine(); // имя пользователя
            int taskNumber = Integer.parseInt(br.readLine()); // номер задачи
            String fileName = br.readLine(); // имя файла
            int languageId = Integer.parseInt(br.readLine()); // id языка программирования в базе
            String fileContents = ""; // содержимое файла решения

            // остальные строки будут содержать решение
            // пользователя. Читаем их
            while (( line = br.readLine()) != null){
                fileContents += line+ "\n";
            }

            // добавляем решение в очередь для проверки
            decisionQueue.addSolution( new UserSolution( userLogin, taskNumber, fileName, languageId, fileContents));

            bw.close();
            br.close();
            s.close();



        }
    }
}
