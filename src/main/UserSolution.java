package main;

/**
 * Created by Александр on 12.10.2017.
 * Решение пользователя, которое "прилетело на порт"
 */
public class UserSolution {

    private String userLogin; // имя пользователя
    private int taskNumber; // номер задачи
    private String fileName; // имя файла
    private int languageId; // id языка программирования в базе
    private String fileContents; // содержимое файла

    // конструктор
    public UserSolution(String userLogin, int taskNumber, String fileName, int languageId, String fileContents) {
        this.userLogin = userLogin;
        this.taskNumber = taskNumber;
        this.fileName = fileName;
        this.languageId = languageId;
        this.fileContents = fileContents;
    }

    // геттеры и сеттеры
    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public String getFileContents() {
        return fileContents;
    }

    public void setFileContents(String fileContents) {
        this.fileContents = fileContents;
    }
}
