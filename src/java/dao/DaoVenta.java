package dao;

import model.DetalleVenta;
import model.Venta;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DaoVenta {
    
    private Connection getConnection() throws SQLException {
        return AccesoDB.getConnection();
    }
    
    // ==================== INSERTAR VENTA ====================
    public Long insertar(Venta venta) throws SQLException {
        String sql = "INSERT INTO venta (fecha, cliente, cliente_id, total, metodo, vendedor) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setDate(1, Date.valueOf(venta.getFecha()));
            pstmt.setString(2, venta.getCliente());
            pstmt.setString(3, venta.getClienteId());
            pstmt.setBigDecimal(4, venta.getTotal());
            pstmt.setString(5, venta.getMetodo());
            pstmt.setString(6, venta.getVendedor());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                venta.setId(id);
                insertarDetalles(conn, id, venta.getItems());
                return id;
            }
        }
        return null;
    }
    
    private void insertarDetalles(Connection conn, Long ventaId, List<DetalleVenta> detalles) throws SQLException {
        String sql = "INSERT INTO detalle_venta (venta_id, codigo, nombre, cantidad, precio, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (DetalleVenta d : detalles) {
                pstmt.setLong(1, ventaId);
                pstmt.setString(2, d.getCodigo());
                pstmt.setString(3, d.getNombre());
                pstmt.setInt(4, d.getCantidad());
                pstmt.setBigDecimal(5, d.getPrecio());
                pstmt.setBigDecimal(6, d.getSubtotal());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    // ==================== LISTAR TODAS LAS VENTAS ====================
    public List<Venta> listarTodas() throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT * FROM venta ORDER BY fecha DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getLong("id"));
                v.setFecha(rs.getDate("fecha").toLocalDate());
                v.setCliente(rs.getString("cliente"));
                v.setClienteId(rs.getString("cliente_id"));
                v.setTotal(rs.getBigDecimal("total"));
                v.setMetodo(rs.getString("metodo"));
                v.setVendedor(rs.getString("vendedor"));
                v.setItems(cargarDetalles(conn, v.getId()));
                ventas.add(v);
            }
        }
        return ventas;
    }
    
    private List<DetalleVenta> cargarDetalles(Connection conn, Long ventaId) throws SQLException {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT * FROM detalle_venta WHERE venta_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, ventaId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setId(rs.getLong("id"));
                d.setVentaId(rs.getLong("venta_id"));
                d.setCodigo(rs.getString("codigo"));
                d.setNombre(rs.getString("nombre"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecio(rs.getBigDecimal("precio"));
                d.setSubtotal(rs.getBigDecimal("subtotal"));
                detalles.add(d);
            }
        }
        return detalles;
    }
    
    // ==================== BUSCAR VENTA POR ID ====================
    public Venta buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM venta WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getLong("id"));
                v.setFecha(rs.getDate("fecha").toLocalDate());
                v.setCliente(rs.getString("cliente"));
                v.setClienteId(rs.getString("cliente_id"));
                v.setTotal(rs.getBigDecimal("total"));
                v.setMetodo(rs.getString("metodo"));
                v.setVendedor(rs.getString("vendedor"));
                v.setItems(cargarDetalles(conn, v.getId()));
                return v;
            }
        }
        return null;
    }
    
    // ==================== TOTAL VENTAS (para dashboard) ====================
    public BigDecimal getTotalVentas() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM venta";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }
    
    // ==================== CONTAR VENTAS (para dashboard) ====================
    public int contarVentas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM venta";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // ==================== ELIMINAR TODAS LAS VENTAS ====================
    public void eliminarTodas() throws SQLException {
        String sqlDetalle = "DELETE FROM detalle_venta";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlDetalle);
        }
        
        String sqlVenta = "DELETE FROM venta";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            int eliminadas = stmt.executeUpdate(sqlVenta);
            System.out.println("✅ Se eliminaron " + eliminadas + " ventas");
        }
    }
    
    // ==================== ELIMINAR VENTA POR ID ====================
    public void eliminarPorId(Long id) throws SQLException {
        String sql = "DELETE FROM venta WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
}