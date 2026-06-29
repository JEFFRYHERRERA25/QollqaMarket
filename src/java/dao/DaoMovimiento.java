package dao;

import model.Movimiento;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DaoMovimiento {

    private Connection getConnection() throws SQLException {
        return AccesoDB.getConnection();
    }

    // ==================== LISTAR TODOS ====================
    public List<Movimiento> listarTodos() {

        List<Movimiento> movimientos = new ArrayList<>();

        String sql =
            "SELECT m.id, " +
            "m.codigo, " +
            "COALESCE(p.nombre, m.nombre) AS nombre, " +
            "m.tipo, " +
            "m.cantidad, " +
            "m.costo, " +
            "m.fecha, " +
            "m.observaciones, " +
            "m.usuario " +
            "FROM movimiento m " +
            "LEFT JOIN producto p ON m.codigo = p.codigo " +
            "ORDER BY m.fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Movimiento m = mapearMovimiento(rs);
                movimientos.add(m);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en listarTodos: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // ==================== BUSCAR POR ID (NUEVO) ====================
    public Movimiento buscarPorId(Long id) {
        String sql = "SELECT m.id, m.codigo, COALESCE(p.nombre, m.nombre) AS nombre, " +
                     "m.tipo, m.cantidad, m.costo, m.fecha, m.observaciones, m.usuario " +
                     "FROM movimiento m " +
                     "LEFT JOIN producto p ON m.codigo = p.codigo " +
                     "WHERE m.id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapearMovimiento(rs);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ==================== ACTUALIZAR MOVIMIENTO (NUEVO) ====================
    public void actualizar(Movimiento movimiento) {
        String sql = "UPDATE movimiento SET codigo=?, nombre=?, tipo=?, cantidad=?, costo=?, fecha=?, observaciones=?, usuario=? WHERE id=?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movimiento.getCodigo());
            pstmt.setString(2, movimiento.getNombre());
            pstmt.setString(3, movimiento.getTipo());
            pstmt.setDouble(4, movimiento.getCantidad());
            pstmt.setDouble(5, movimiento.getCosto());
            pstmt.setDate(6, Date.valueOf(movimiento.getFecha()));
            pstmt.setString(7, movimiento.getObservaciones());
            pstmt.setString(8, movimiento.getUsuario());
            pstmt.setLong(9, movimiento.getId());
            
            pstmt.executeUpdate();
            System.out.println("✅ Movimiento actualizado - ID: " + movimiento.getId());
        } catch (Exception e) {
            System.err.println("❌ Error en actualizar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== FILTRAR POR TIPO ====================
    public List<Movimiento> filtrarPorTipo(String tipo) {

        List<Movimiento> movimientos = new ArrayList<>();

        if (tipo == null || tipo.trim().isEmpty()) {
            return listarTodos();
        }

        String sql =
            "SELECT m.id, " +
            "m.codigo, " +
            "COALESCE(p.nombre, m.nombre) AS nombre, " +
            "m.tipo, " +
            "m.cantidad, " +
            "m.costo, " +
            "m.fecha, " +
            "m.observaciones, " +
            "m.usuario " +
            "FROM movimiento m " +
            "LEFT JOIN producto p ON m.codigo = p.codigo " +
            "WHERE m.tipo = ? " +
            "ORDER BY m.fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movimiento m = mapearMovimiento(rs);
                movimientos.add(m);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en filtrarPorTipo: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // ==================== FILTRAR POR FECHA ====================
    public List<Movimiento> filtrarPorFecha(LocalDate desde, LocalDate hasta) {

        List<Movimiento> movimientos = new ArrayList<>();

        String sql =
            "SELECT m.id, " +
            "m.codigo, " +
            "COALESCE(p.nombre, m.nombre) AS nombre, " +
            "m.tipo, " +
            "m.cantidad, " +
            "m.costo, " +
            "m.fecha, " +
            "m.observaciones, " +
            "m.usuario " +
            "FROM movimiento m " +
            "LEFT JOIN producto p ON m.codigo = p.codigo " +
            "WHERE m.fecha BETWEEN ? AND ? " +
            "ORDER BY m.fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(desde));
            pstmt.setDate(2, Date.valueOf(hasta));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movimiento m = mapearMovimiento(rs);
                movimientos.add(m);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en filtrarPorFecha: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // ==================== FILTRAR POR FECHA Y TIPO ====================
    public List<Movimiento> filtrarPorFechaYTipo(LocalDate desde, LocalDate hasta, String tipo) {

        List<Movimiento> movimientos = new ArrayList<>();

        String sql =
            "SELECT m.id, " +
            "m.codigo, " +
            "COALESCE(p.nombre, m.nombre) AS nombre, " +
            "m.tipo, " +
            "m.cantidad, " +
            "m.costo, " +
            "m.fecha, " +
            "m.observaciones, " +
            "m.usuario " +
            "FROM movimiento m " +
            "LEFT JOIN producto p ON m.codigo = p.codigo " +
            "WHERE m.fecha BETWEEN ? AND ? " +
            "AND m.tipo = ? " +
            "ORDER BY m.fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(desde));
            pstmt.setDate(2, Date.valueOf(hasta));
            pstmt.setString(3, tipo);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movimiento m = mapearMovimiento(rs);
                movimientos.add(m);
            }

            System.out.println("✅ filtrarPorFechaYTipo - Encontrados: " + movimientos.size());

        } catch (Exception e) {
            System.err.println("❌ Error en filtrarPorFechaYTipo: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // ==================== INSERTAR ====================
    public void insertar(Movimiento movimiento) {

        String sql =
            "INSERT INTO movimiento " +
            "(codigo, nombre, tipo, cantidad, costo, fecha, observaciones, usuario) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, movimiento.getCodigo());
            pstmt.setString(2, movimiento.getNombre());
            pstmt.setString(3, movimiento.getTipo());
            pstmt.setDouble(4, movimiento.getCantidad());
            pstmt.setDouble(5, movimiento.getCosto());

            if (movimiento.getFecha() != null) {
                pstmt.setDate(6, Date.valueOf(movimiento.getFecha()));
            } else {
                pstmt.setDate(6, new Date(System.currentTimeMillis()));
            }

            pstmt.setString(7, movimiento.getObservaciones());
            pstmt.setString(8, movimiento.getUsuario());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                movimiento.setId(rs.getLong(1));
            }

            System.out.println("✅ Movimiento insertado - Código: " + movimiento.getCodigo() + ", Fecha: " + movimiento.getFecha());

        } catch (Exception e) {
            System.err.println("❌ Error en insertar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== ELIMINAR TODOS ====================
    public void eliminarTodos() {

        String sql = "DELETE FROM movimiento";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("❌ Error en eliminarTodos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== ELIMINAR POR ID ====================
    public void eliminarPorId(Long id) {

        String sql = "DELETE FROM movimiento WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Movimiento eliminado - ID: " + id);

        } catch (Exception e) {
            System.err.println("❌ Error en eliminarPorId: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== CONTAR ====================
    public int contarMovimientos() {

        String sql = "SELECT COUNT(*) FROM movimiento";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en contarMovimientos: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // ==================== MAPEAR ====================
    private Movimiento mapearMovimiento(ResultSet rs) throws SQLException {

        Movimiento m = new Movimiento();

        m.setId(rs.getLong("id"));
        m.setCodigo(rs.getString("codigo"));
        m.setNombre(rs.getString("nombre"));
        m.setTipo(rs.getString("tipo"));
        m.setCantidad(rs.getDouble("cantidad"));
        m.setCosto(rs.getDouble("costo"));

        Date fechaSql = rs.getDate("fecha");

        if (fechaSql != null) {
            m.setFecha(fechaSql.toLocalDate());
        } else {
            m.setFecha(null);
        }

        m.setObservaciones(rs.getString("observaciones"));
        m.setUsuario(rs.getString("usuario"));

        return m;
    }
}