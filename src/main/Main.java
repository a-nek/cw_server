package main;

import java.io.*;

public class Main {

    private static MainController mainController;

    public static void main(String[] args) throws IOException, InterruptedException {

        // запускаем контроллер, который слушает порт
        mainController = new MainController();
        mainController.start();

    }
}
