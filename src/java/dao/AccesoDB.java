package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AccesoDB {
    // Configuración para MySQL Workbench
    // Si tienes contraseña, escríbela aquí
    private static final String URL = "jdbc:mysql://reseau.proxy.rlwy.net:38042/qollqa_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "RtiIToRQhFTNzMnIjMNEHDXQtGvdsWhR";  // ← ¡CAMBIA ESTO!
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado", e);
        }
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conexión exitosa a MySQL Workbench!");
            System.out.println("Base de datos: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}