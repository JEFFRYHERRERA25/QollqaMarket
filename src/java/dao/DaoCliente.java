package dao;

import model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoCliente {
    
    // ==================== INSERTAR ====================
    public void insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (id, nombre, dni, telefono, direccion) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cliente.getId());
            pstmt.setString(2, cliente.getNombre());
            pstmt.setString(3, cliente.getDni());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getDireccion());
            
            pstmt.executeUpdate();
        }
    }
    
    // ==================== LISTAR TODOS ====================
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nombre";
        
        try (Connection conn = AccesoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getString("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDni(rs.getString("dni"));
                c.setTelefono(rs.getString("telefono"));
                c.setDireccion(rs.getString("direccion"));
                clientes.add(c);
            }
        }
        return clientes;
    }
    
    // ==================== BUSCAR POR ID ====================
    public Cliente buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE id = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getString("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDni(rs.getString("dni"));
                c.setTelefono(rs.getString("telefono"));
                c.setDireccion(rs.getString("direccion"));
                return c;
            }
        }
        return null;
    }
    
    // ==================== NUEVO MÉTODO: ACTUALIZAR CLIENTE ====================
    /**
     * Actualizar los datos de un cliente existente
     * @param cliente Objeto Cliente con los datos actualizados
     * @throws SQLException Si ocurre un error en la base de datos
     */
    public void actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nombre = ?, dni = ?, telefono = ?, direccion = ? WHERE id = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getDni());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setString(4, cliente.getDireccion());
            pstmt.setString(5, cliente.getId());
            
            int filasActualizadas = pstmt.executeUpdate();
            
            if (filasActualizadas == 0) {
                throw new SQLException("No se encontró el cliente con ID: " + cliente.getId());
            }
            
            System.out.println("✅ Cliente actualizado correctamente: " + cliente.getId());
        }
    }
    // ==================== FIN MÉTODO ACTUALIZAR CLIENTE ====================
    
    // ==================== ELIMINAR ====================
    public void eliminar(String id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            int filasEliminadas = pstmt.executeUpdate();
            
            if (filasEliminadas == 0) {
                throw new SQLException("No se encontró el cliente con ID: " + id);
            }
            
            System.out.println("✅ Cliente eliminado: " + id);
        }
    }
    
    // ==================== ELIMINAR TODOS LOS CLIENTES ====================
    public void eliminarTodos() throws SQLException {
        String sql = "DELETE FROM cliente";
        try (Connection conn = AccesoDB.getConnection();
             Statement stmt = conn.createStatement()) {
            int eliminados = stmt.executeUpdate(sql);
            System.out.println("✅ Se eliminaron " + eliminados + " clientes");
        }
    }
    
    // ==================== CONTAR CLIENTES ====================
    public int contarClientes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente";
        
        try (Connection conn = AccesoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // ==================== BUSCAR POR TEXTO ====================
    public List<Cliente> buscarPorTexto(String texto) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente WHERE nombre LIKE ? OR dni LIKE ? OR telefono LIKE ?";
        
        try (Connection conn = AccesoDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String busqueda = "%" + texto + "%";
            pstmt.setString(1, busqueda);
            pstmt.setString(2, busqueda);
            pstmt.setString(3, busqueda);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getString("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDni(rs.getString("dni"));
                c.setTelefono(rs.getString("telefono"));
                c.setDireccion(rs.getString("direccion"));
                clientes.add(c);
            }
        }
        return clientes;
    }
}