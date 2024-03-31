package es.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import es.config.Config;

public class DatabaseUtil {
  static {
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        throw new ExceptionInInitializerError(e);
      }
    }

    public static Connection getConnection() throws SQLException {
      String url = Config.getDbUrl();
      String user = Config.getDbUser();
      String password = Config.getDbPassword();
      return DriverManager.getConnection(url, user, password);
    }
}
