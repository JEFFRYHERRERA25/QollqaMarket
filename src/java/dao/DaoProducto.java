package dao;

import model.Producto;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoProducto {
    
    private Connection getConnection() throws SQLException {
        return AccesoDB.getConnection();
    }
    
    // ==================== INSERTAR PRODUCTO ====================
    public void insertar(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, unidad, grupo, stock_min, cantidad, precio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getUnidad());
            pstmt.setString(4, producto.getGrupo());
            pstmt.setInt(5, producto.getStockMin());
            pstmt.setInt(6, producto.getCantidad());
            pstmt.setBigDecimal(7, producto.getPrecio() != null ? producto.getPrecio() : BigDecimal.ZERO);
            
            pstmt.executeUpdate();
        }
    }
    
    // ==================== LISTAR TODOS LOS PRODUCTOS ====================
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY nombre";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Producto p = new Producto();
                p.setCodigo(rs.getString("codigo"));
                p.setNombre(rs.getString("nombre"));
                p.setUnidad(rs.getString("unidad"));
                p.setGrupo(rs.getString("grupo"));
                p.setStockMin(rs.getInt("stock_min"));
                p.setCantidad(rs.getInt("cantidad"));
                p.setPrecio(rs.getBigDecimal("precio"));
                productos.add(p);
            }
        }
        return productos;
    }
    
    // ==================== BUSCAR PRODUCTO POR CÓDIGO ====================
    public Producto buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM producto WHERE codigo = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Producto p = new Producto();
                p.setCodigo(rs.getString("codigo"));
                p.setNombre(rs.getString("nombre"));
                p.setUnidad(rs.getString("unidad"));
                p.setGrupo(rs.getString("grupo"));
                p.setStockMin(rs.getInt("stock_min"));
                p.setCantidad(rs.getInt("cantidad"));
                p.setPrecio(rs.getBigDecimal("precio"));
                return p;
            }
        }
        return null;
    }
    
    // ==================== BUSCAR PRODUCTOS POR TEXTO ====================
    public List<Producto> buscarPorTexto(String texto) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE codigo LIKE ? OR nombre LIKE ? OR grupo LIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String busqueda = "%" + texto + "%";
            pstmt.setString(1, busqueda);
            pstmt.setString(2, busqueda);
            pstmt.setString(3, busqueda);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Producto p = new Producto();
                p.setCodigo(rs.getString("codigo"));
                p.setNombre(rs.getString("nombre"));
                p.setUnidad(rs.getString("unidad"));
                p.setGrupo(rs.getString("grupo"));
                p.setStockMin(rs.getInt("stock_min"));
                p.setCantidad(rs.getInt("cantidad"));
                p.setPrecio(rs.getBigDecimal("precio"));
                productos.add(p);
            }
        }
        return productos;
    }
    
    // ==================== LISTAR PRODUCTOS CON STOCK BAJO ====================
    public List<Producto> listarStockBajo() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE cantidad <= stock_min";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Producto p = new Producto();
                p.setCodigo(rs.getString("codigo"));
                p.setNombre(rs.getString("nombre"));
                p.setUnidad(rs.getString("unidad"));
                p.setGrupo(rs.getString("grupo"));
                p.setStockMin(rs.getInt("stock_min"));
                p.setCantidad(rs.getInt("cantidad"));
                p.setPrecio(rs.getBigDecimal("precio"));
                productos.add(p);
            }
        }
        return productos;
    }
    
    // ==================== ACTUALIZAR STOCK ====================
    public void actualizarStock(String codigo, int nuevaCantidad) throws SQLException {
        String sql = "UPDATE producto SET cantidad = ? WHERE codigo = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nuevaCantidad);
            pstmt.setString(2, codigo);
            pstmt.executeUpdate();
        }
    }
    
    // ==================== ACTUALIZAR PRODUCTO COMPLETO ====================
    public void actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, unidad = ?, grupo = ?, stock_min = ?, cantidad = ?, precio = ? WHERE codigo = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getUnidad());
            pstmt.setString(3, producto.getGrupo());
            pstmt.setInt(4, producto.getStockMin());
            pstmt.setInt(5, producto.getCantidad());
            pstmt.setBigDecimal(6, producto.getPrecio());
            pstmt.setString(7, producto.getCodigo());
            
            pstmt.executeUpdate();
        }
    }
    
    // ==================== ELIMINAR PRODUCTO ====================
    public void eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            pstmt.executeUpdate();
        }
    }
    
    // ==================== ELIMINAR TODOS LOS PRODUCTOS ====================
    public void eliminarTodos() throws SQLException {
        String sql = "DELETE FROM producto";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            int eliminados = stmt.executeUpdate(sql);
            System.out.println("✅ Se eliminaron " + eliminados + " productos");
        }
    }
    
    // ==================== CONTAR PRODUCTOS ====================
    public int contarProductos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int contarProductosAgotados() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE cantidad = 0";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int contarProductosConStock() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE cantidad > 0";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int contarProductosNormales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE cantidad > stock_min";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}