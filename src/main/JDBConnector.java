package main;

import java.sql.*;

/**
 * Created by Александр on 16.10.2017.
 *
 * Будет подключаться к базе данных
 * и выполнять запросы
 */
public class JDBConnector {
    // переменные для подключения к базе
    private String url;
    private String userName;
    private String password;

    // переменные для соединения с бд
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public JDBConnector(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;

        openConnection();
    }

    // выполняем запрос
    public ResultSet executeQuery(String query) throws SQLException {
        try{
            //выполянем запрос
            resultSet = statement.executeQuery(query);
        } catch (SQLException e){
            e.printStackTrace();
        }

        // возвращаем результат
        return resultSet;
    }

    // выполняем запрос
    public ResultSet executeUpdate(String query) throws SQLException {
        try{
            //выполянем запрос
            statement.executeUpdate(query);
        } catch (SQLException e){
            e.printStackTrace();
        }

        // возвращаем результат
        return resultSet;
    }

    public Statement getStatement(){
        return statement;
    }

    // соединяемся с базой
    private void openConnection(){
        try{
            // открывем соединение с базой данных
            connection = DriverManager.getConnection(url, userName, password);

            statement = connection.createStatement();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Connection getConnection(){
        return connection;
    }

    // закрываем соединение
    public void closeConnection(){
        try{
            connection.close();
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
