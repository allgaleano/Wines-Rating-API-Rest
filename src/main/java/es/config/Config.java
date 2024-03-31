package sos.config;

// Conexion a la base de datos hardcodeada
public class Config {
  
  public static String getDbUrl() {
    return "jdbc:mysql://localhost:3306/WineCommunity";
  }

  public static String getDbUser() {
    return "root";
  }

  public static String getDbPassword() {
    return "root";
  }
}
