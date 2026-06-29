package dao;

import model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoUsuario {
    
    // ==================== VALIDAR LOGIN ====================
    public Usuario validarLogin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ? AND password = ? AND estado = 'ACTIVO'";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setCodUsuario(rs.getString("cod_usuario"));
                u.setUsername(rs.getString("username"));
                u.setNombre(rs.getString("nombre"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getString("estado"));
                return u;
            }
        }
        return null;
    }
    
    // ==================== LISTAR TODOS ====================
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        
        try (Connection conn = AccesoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setCodUsuario(rs.getString("cod_usuario"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setNombre(rs.getString("nombre"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getString("estado"));
                usuarios.add(u);
            }
        }
        return usuarios;
    }
    
    // ==================== INSERTAR USUARIO ====================
    public void insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (cod_usuario, username, password, nombre, rol, estado) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario.getCodUsuario());
            pstmt.setString(2, usuario.getUsername());
            pstmt.setString(3, usuario.getPassword());
            pstmt.setString(4, usuario.getNombre());
            pstmt.setString(5, usuario.getRol());
            pstmt.setString(6, usuario.getEstado() != null ? usuario.getEstado() : "ACTIVO");
            
            pstmt.executeUpdate();
            System.out.println("✅ Usuario insertado: " + usuario.getUsername());
        }
    }
    
    // ==================== ELIMINAR USUARIO ====================
    public void eliminar(String username) throws SQLException {
        String sql = "DELETE FROM usuario WHERE username = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            System.out.println("✅ Usuario eliminado: " + username);
        }
    }
    
    // ==================== BUSCAR POR USERNAME ====================
    public Usuario buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setCodUsuario(rs.getString("cod_usuario"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setNombre(rs.getString("nombre"));
                u.setRol(rs.getString("rol"));
                u.setEstado(rs.getString("estado"));
                return u;
            }
        }
        return null;
    }
    
    // ==================== ACTUALIZAR ESTADO ====================
    public void actualizarEstado(String username, String estado) throws SQLException {
        String sql = "UPDATE usuario SET estado = ? WHERE username = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
    
    // ==================== ACTUALIZAR PASSWORD ====================
    public void actualizarPassword(String username, String nuevaPassword) throws SQLException {
        String sql = "UPDATE usuario SET password = ? WHERE username = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nuevaPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
    
    // ==================== NUEVO: ACTUALIZAR USUARIO COMPLETO (CON POSIBILIDAD DE CAMBIAR USERNAME) ====================
    /**
     * Actualiza los datos de un usuario incluyendo la posibilidad de cambiar el username
     * @param oldUsername Username actual del usuario (para identificarlo en la BD)
     * @param newUsername Nuevo username (puede ser el mismo si no se cambia)
     * @param nombre Nuevo nombre del usuario
     * @param password Nueva contraseña (puede ser null o vacío para no actualizar)
     * @param rol Nuevo rol (admin, vendedor, etc.)
     * @param actualizarPassword Indica si se debe actualizar la contraseña
     * @throws SQLException Si ocurre un error en la base de datos
     */
    public void actualizarUsuarioCompleto(String oldUsername, String newUsername, String nombre, String password, String rol, boolean actualizarPassword) 
            throws SQLException {
        
        String sql;
        if (actualizarPassword && password != null && !password.trim().isEmpty()) {
            sql = "UPDATE usuario SET username = ?, nombre = ?, password = ?, rol = ? WHERE username = ?";
        } else {
            sql = "UPDATE usuario SET username = ?, nombre = ?, rol = ? WHERE username = ?";
        }
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (actualizarPassword && password != null && !password.trim().isEmpty()) {
                pstmt.setString(1, newUsername);
                pstmt.setString(2, nombre);
                pstmt.setString(3, password);
                pstmt.setString(4, rol);
                pstmt.setString(5, oldUsername);
                System.out.println("✅ Actualizando usuario con contraseña - old: " + oldUsername + " -> new: " + newUsername);
            } else {
                pstmt.setString(1, newUsername);
                pstmt.setString(2, nombre);
                pstmt.setString(3, rol);
                pstmt.setString(4, oldUsername);
                System.out.println("✅ Actualizando usuario SIN contraseña - old: " + oldUsername + " -> new: " + newUsername);
            }
            
            int filasActualizadas = pstmt.executeUpdate();
            
            if (filasActualizadas == 0) {
                throw new SQLException("No se encontró el usuario con username: " + oldUsername);
            }
            
            System.out.println("✅ Usuario actualizado correctamente: " + oldUsername + " -> " + newUsername);
        }
    }
    
    // ==================== MÉTODO ACTUALIZAR USUARIO (SIN CAMBIAR USERNAME) - MANTENIDO POR COMPATIBILIDAD ====================
    /**
     * Actualiza los datos de un usuario (nombre, rol y opcionalmente contraseña)
     * @param username Username del usuario a actualizar (no se puede cambiar)
     * @param nombre Nuevo nombre del usuario
     * @param password Nueva contraseña (puede ser null o vacío para no actualizar)
     * @param rol Nuevo rol (admin, vendedor, etc.)
     * @param actualizarPassword Indica si se debe actualizar la contraseña
     * @throws SQLException Si ocurre un error en la base de datos
     */
    public void actualizarUsuario(String username, String nombre, String password, String rol, boolean actualizarPassword) 
            throws SQLException {
        
        // Llamar al método completo con el mismo username para old y new
        actualizarUsuarioCompleto(username, username, nombre, password, rol, actualizarPassword);
    }
    
    // ==================== MÉTODO SOBRECARGADO PARA SIMPLIFICAR (OPCIONAL) ====================
    /**
     * Versión simplificada que solo actualiza nombre y rol (sin contraseña)
     */
    public void actualizarUsuario(String username, String nombre, String rol) throws SQLException {
        actualizarUsuario(username, nombre, null, rol, false);
    }
    
    // ==================== MÉTODO PARA ACTUALIZAR TODOS LOS CAMPOS (SIN CAMBIAR USERNAME) ====================
    /**
     * Actualiza todos los campos del usuario excepto el username
     * @param usuario Objeto Usuario con los datos actualizados
     * @param actualizarPassword Si es true, actualiza la contraseña; si es false, la mantiene
     */
    public void actualizarUsuarioCompleto(Usuario usuario, boolean actualizarPassword) throws SQLException {
        String username = usuario.getUsername();
        String nombre = usuario.getNombre();
        String password = usuario.getPassword();
        String rol = usuario.getRol();
        
        actualizarUsuario(username, nombre, password, rol, actualizarPassword);
    }
}