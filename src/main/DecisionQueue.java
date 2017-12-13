package main;

import robots.JavaRobot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Александр on 10.10.2017.
 * Очередь решений
 */
public class DecisionQueue extends Thread{

    // список решений пользователей.
    // из этого списка будем "доставать" решения и
    // отправлять их роботам на проверку
    private ArrayList<UserSolution> userSolutions = new ArrayList<>();

    // Будет тестировать решения
    JavaRobot javaRobot;

    public void run(){
        while (true){

            // если список не пуст
            if(!userSolutions.isEmpty()){

                // userSolution присваиваем первое решение в списке
                UserSolution userSolution = userSolutions.get(0);

                // удаляем решение из списка
                userSolutions.remove(0);

                // По id языка определяем какой робот будет проверять решение
                switch (userSolution.getLanguageId()){

                    // у Java id = 1
                    case 1 : {

                        try {

                            javaRobot = new JavaRobot(userSolution);
                            javaRobot.checkSolution();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }break;
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    // добавляет решение в список userSolutions
    public void addSolution(UserSolution userSolution){
        userSolutions.add(userSolution);
    }
}
