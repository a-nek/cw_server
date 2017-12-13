package robots;

import main.JDBConnector;
import main.UserSolution;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by Александр on 10.10.2017.
 * Тестирует решения на Java
 */
public class JavaRobot {

    // путь к файлу конфигураций БД
    private String pathToDataBaseConfigurationFile = "settings/database.properties";

    // инициализируем объект
    // для работы с файлами конфигурций
    private Properties properties = new Properties();

    // относительный путь к директории, в которой будет компилироваться и запускаться
    // решение пользователя
    private String workDirectory;

    // полный путь к директории
    private String fullPathToWorkDirectory;

    // файл с решением пользователя
    private File file;

    // колв-во тестов, которые прошло решение пользователя
    private int goodTestCount= 0;
    private int countOfAllTests;
    private int userId = 0;

    // решение пользователя (имя пользователя, номер задачи ...)
    private main.UserSolution userSolution;

    // URL, username, password для сервера MySql
    private String url;
    private String userName;
    private String password;

    private JDBConnector jdbConnector;

    // название батника( .bat), который
    // будет запускать скомпилированное решение
    private String runBatName = "run.bat";


    //время, в течение которого задача должна завершить работу
    private long timeLimit;

    // конструктор
    public JavaRobot(UserSolution userSolution) throws IOException, SQLException {

        this.userSolution = userSolution;

        // загружаем файл конфигурации базы данных
        properties.load(new FileInputStream(pathToDataBaseConfigurationFile));

        // загружаем из файла данные подключения к БД
        url = properties.getProperty("url");
        userName = properties.getProperty("username");
        password = properties.getProperty("password");


        // рабочая директория
        workDirectory = "working_directories\\java_working_directory\\";
        fullPathToWorkDirectory = System.getProperty("user.dir") + "\\" + workDirectory;

        // подключаемся к базе, чтобы узнать
        // ограничения по времени и памяти
        JDBConnector JDBConnector = new JDBConnector(url, userName, password);
        ResultSet r = JDBConnector.executeQuery("SELECT problems.time_limit FROM problems WHERE idproblems =" + userSolution.getTaskNumber());

        if (r.next()) {
            timeLimit = Integer.parseInt(r.getString("time_limit")) * (int) Math.pow(10, 9); // сразу в наносекунды переводим
        }

        // посчитаем кол-во тестов для этой задачи
        r = JDBConnector.executeQuery("SELECT COUNT(*) FROM tests WHERE problem_id =" + userSolution.getTaskNumber());
        if(r.next()){
            countOfAllTests = Integer.parseInt(r.getString("COUNT(*)"));
        }

    }


    // создает файл, компилируеут,проверяет ответ, удаляет все из рабочей директории
    public void checkSolution() throws IOException, InterruptedException, SQLException {

        // создаем файл с решением, который будем компилировать
        createFile();

        // компилируем
        if (compile()) {// если ошибок нет

            // загружаем тесты из базы
            jdbConnector = new JDBConnector(url, userName, password);

            ResultSet tests = jdbConnector.executeQuery("SELECT tests.id_test, tests.input_test_data, tests.output_test_data FROM tests WHERE problem_id =" + userSolution.getTaskNumber());

            // нужно получить id пользователя, т.к.мы знаем только имя.
            ResultSet r = jdbConnector.getConnection().createStatement().executeQuery("SELECT `idusers` FROM users WHERE `users`.`login` = '"+ userSolution.getUserLogin() +"';");

            if(r.next()){
                userId = Integer.parseInt(r.getString("idusers"));
            }

            // создаем батник для запуска
            // решения в рабочей
            createRunBat();

            // пока в выборке из базы есть тесты
            while (tests.next()) {
                // создаем файл input
                // записываем в файл входные данные
                PrintWriter printWriter = new PrintWriter(new FileWriter(new File(workDirectory, "input.txt")));
                printWriter.write(tests.getString("input_test_data"));
                printWriter.close();

                // правильное решение
                String correctAnswer = new String(tests.getString("output_test_data"));

                // запускаем скомпилированное решение
                runSolution();

                // пытаемся прочитать файл с решением. Если файл присутствует, значит решение успело отработать в течение указанного времени
                boolean testResult = tryReadSolutionFileAndCheck(correctAnswer);
                if(!testResult){
                    // добавляем запись, что решение не прошло
                    jdbConnector.executeUpdate("INSERT INTO `data_base`.`status` " +
                            "(`date`, `user_id`, `problem_id`, `result_id`, `test_number`, `language`) " +
                            "VALUES (now(), '"+ userId +"', '"+ userSolution.getTaskNumber() +"', '2', '"+ goodTestCount +"', '1')");

                    // прерываем проверку
                    break;
                }
                goodTestCount++;

            }
            // если цикл закончился, значит все тест пройдены
            if(goodTestCount == countOfAllTests){
                jdbConnector.executeUpdate("INSERT INTO `data_base`.`status` " +
                        "(`date`, `user_id`, `problem_id`, `result_id`, `test_number`, `language`) " +
                        "VALUES (now(), '"+ userId +"', '"+ userSolution.getTaskNumber() +"', '1', '"+ goodTestCount +"', '1');");

                jdbConnector.executeUpdate("UPDATE `data_base`.`problems` SET `successful_attempts`=`successful_attempts` + 1 WHERE `idproblems`='" + userSolution.getTaskNumber() + "';");
            }



        } else { // если скомпилировалось с ошибками
            jdbConnector.getStatement().executeUpdate("INSERT INTO `data_base`.`status` " +
                    "(`date`, `user_id`, `problem_id`, `result_id`, `test_number`, `language`) " +
                    "VALUES (now(), '"+ userId +"', '"+ userSolution.getTaskNumber() +"', '4', '-1', '1');"); ;
        }

        // увеличиваем у задачи кол-во попыток её решения
        jdbConnector.executeUpdate("UPDATE `data_base`.`problems` SET `attempts`=`attempts` + 1 WHERE `idproblems`='" + userSolution.getTaskNumber() + "';");

        // закрываем соединение
        jdbConnector.closeConnection();

        // удаляем файлы из рабочей директории
        deleteAllFilesFolder(workDirectory);
    }

    // создаем файл решения
    private void createFile() throws IOException {
        // создаем файл в рабочей директории
        file = new File(workDirectory, userSolution.getFileName());

        // записываем в файл решение
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.write(userSolution.getFileContents());
        printWriter.close();

    }

    // компилируем файл
    private boolean compile() throws IOException, InterruptedException {

        // компилируем , используя javac
        ProcessBuilder procBuilder = new ProcessBuilder("javac", workDirectory + "/" + userSolution.getFileName());

        // перенаправляем стандартный поток ошибок на
        // стандартный вывод
        procBuilder.redirectErrorStream(true);

        // запуск программы
        Process process = procBuilder.start();

        // читаем стандартный поток вывода
        // и выводим на экран
        InputStream stdout = process.getInputStream();
        InputStreamReader isrStdout = new InputStreamReader(stdout);
        BufferedReader brStdout = new BufferedReader(isrStdout);

        String line = null;
        while ((line = brStdout.readLine()) != null) {
            System.out.println(line);
        }

        // ждем пока завершится вызванная программа
        // и сохраняем код, с которым она завершилась в
        // в переменную exitVal
        int exitVal = process.waitFor();

        // если процесс компиляции завершился с ошибкой
        if (exitVal != 0) {
            return false;
        } else {

            // если ошибок не было
            return true;
        }
    }

    // запускаем скомпилированное решение
    private String runSolution() throws IOException, InterruptedException {


        // запускаем
        ProcessBuilder processBuilder = new ProcessBuilder(fullPathToWorkDirectory + "\\run.bat");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // ждем пока завершится вызванная программа
        // и сохраняем код, с которым она завершилась в
        // в переменную exitVal
        long startTime = System.nanoTime();

        String line = null; // временная строка
        String line2 = new String("");// будет хранить информацию из потока вывода.

        while (/*process.isAlive()*/ true) { // пока процесс жив

            // читаем стандартный поток вывода
            // и выводим на экран
            InputStream stdout = process.getInputStream();
            InputStreamReader isrStdout = new InputStreamReader(stdout);
            BufferedReader brStdout = new BufferedReader(isrStdout);


            while ((line = brStdout.readLine()) != null) {
                line2 += line;
            }

            // если превысили ограничение по времени
            if (System.nanoTime() - startTime > timeLimit) {
                process.destroy(); // уничтожаем процесс
                //System.out.println("kill at" + (System.nanoTime() - startTime) / Math.pow(10, 9));
                break;

            }



        }
        //System.out.println("exit after" + (System.nanoTime() - startTime) / Math.pow(10, 9) + process.isAlive());

        int exitVal = process.waitFor();
        //System.out.println("exitVal = " + process.exitValue() + " " + exitVal);
        return line2; //
    }

    // создает .bat файл для запуска
    // скомпилированного решения
    private void createRunBat() throws IOException {

        // убираем расширение файла , так для запуска нужно только имя
        String fileName = userSolution.getFileName();
        fileName = fileName.replace(".java", "");

        file = new File(workDirectory, runBatName);

        // записываем в файл решение
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.write("cd " + fullPathToWorkDirectory + "\n"); // записываем команду перехода в рабочую директорию
        printWriter.write("java " + fileName);
        printWriter.close();

    }

    // пытается прочитать файл с результатом решения пользователя
    private boolean tryReadSolutionFileAndCheck(String correctAnswer) {

        try {

            Scanner scanner = new Scanner(new FileReader(new File(workDirectory, "output.txt")));

            String s = "";
            while (scanner.hasNextLine()){
                s += scanner.next();
            }

            if(correctAnswer.equals(s)){ // если результат совпал с правильным ответом
                return true;

            }

        } catch (FileNotFoundException e) { //  файл не найден, значит решение пользователя не успело отработать
            return false;
        }

        return false;
    }

    //удаляет файлы из директории
    private void deleteAllFilesFolder(String path) {
        for (File myFile : new File(path).listFiles())
            if (myFile.isFile()){
                myFile.delete();
            }
    }
}
