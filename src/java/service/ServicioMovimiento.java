package service;

import dao.DaoMovimiento;
import dao.DaoProducto;
import model.Movimiento;
import model.Producto;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que maneja la lógica de negocio para los movimientos de inventario
 * (Ingresos, Salidas, Ajustes)
 */
public class ServicioMovimiento {
    
    private DaoMovimiento daoMovimiento = new DaoMovimiento();
    private DaoProducto daoProducto = new DaoProducto();
    
    /**
     * Registrar un nuevo movimiento de inventario
     * @param codigo Código del producto
     * @param tipo Tipo de movimiento (INGRESO, SALIDA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO)
     * @param cantidad Cantidad del movimiento
     * @param costo Costo del producto
     * @param fecha Fecha del movimiento
     * @param observaciones Observaciones opcionales
     * @param usuario Usuario que realiza el movimiento
     * @throws SQLException Error de base de datos
     * @throws BusinessException Error de negocio (stock insuficiente, producto no encontrado)
     */
    public void registrarMovimiento(String codigo, String tipo, double cantidad, double costo,
                                    LocalDate fecha, String observaciones, String usuario) 
                                    throws SQLException, BusinessException {
        
        // Validar que el producto existe
        Producto producto = daoProducto.buscarPorCodigo(codigo);
        if (producto == null) {
            throw new BusinessException("Producto no encontrado: " + codigo);
        }
        
        // Validar cantidad
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero");
        }
        
        int stockActual = producto.getCantidad();
        int nuevoStock = stockActual;
        
        // Calcular nuevo stock según el tipo de movimiento
        switch (tipo) {
            case "INGRESO":
                nuevoStock = stockActual + (int) cantidad;
                break;
                
            case "SALIDA":
                if (cantidad > stockActual) {
                    throw new BusinessException("Stock insuficiente. Disponible: " + stockActual + " " + producto.getUnidad());
                }
                nuevoStock = stockActual - (int) cantidad;
                break;
                
            case "AJUSTE_POSITIVO":
                nuevoStock = stockActual + (int) cantidad;
                break;
                
            case "AJUSTE_NEGATIVO":
                if (cantidad > stockActual) {
                    throw new BusinessException("No se puede ajustar. Stock actual: " + stockActual);
                }
                nuevoStock = stockActual - (int) cantidad;
                break;
                
            default:
                throw new BusinessException("Tipo de movimiento inválido: " + tipo);
        }
        
        // Validar que el nuevo stock no sea negativo
        if (nuevoStock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }
        
        // Actualizar stock del producto
        daoProducto.actualizarStock(codigo, nuevoStock);
        
        // Crear y guardar el movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCodigo(codigo);
        movimiento.setNombre(producto.getNombre());
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setCosto(costo);
        movimiento.setFecha(fecha);
        movimiento.setObservaciones(observaciones != null ? observaciones : "");
        movimiento.setUsuario(usuario);
        
        daoMovimiento.insertar(movimiento);
    }
    
    /**
     * Listar todos los movimientos
     * @return Lista de movimientos
     * @throws SQLException Error de base de datos
     */
    public List<Movimiento> listarMovimientos() throws SQLException {
        return daoMovimiento.listarTodos();
    }
    
    /**
     * Buscar movimientos por rango de fechas
     * @param desde Fecha inicial
     * @param hasta Fecha final
     * @return Lista de movimientos en el rango
     * @throws SQLException Error de base de datos
     */
    public List<Movimiento> buscarPorFecha(LocalDate desde, LocalDate hasta) throws SQLException {
        if (desde == null || hasta == null) {
            return daoMovimiento.listarTodos();
        }
        return daoMovimiento.filtrarPorFecha(desde, hasta);
    }
    
    /**
     * Buscar movimientos por tipo
     * @param tipo Tipo de movimiento
     * @return Lista de movimientos del tipo especificado
     * @throws SQLException Error de base de datos
     */
    public List<Movimiento> buscarPorTipo(String tipo) throws SQLException {
        if (tipo == null || tipo.isEmpty()) {
            return daoMovimiento.listarTodos();
        }
        return daoMovimiento.filtrarPorTipo(tipo);
    }
    
    /**
     * Buscar movimientos con filtros combinados
     * @param desde Fecha inicial
     * @param hasta Fecha final
     * @param tipo Tipo de movimiento
     * @return Lista de movimientos filtrados
     * @throws SQLException Error de base de datos
     */
    public List<Movimiento> buscarMovimientos(LocalDate desde, LocalDate hasta, String tipo) throws SQLException {
        List<Movimiento> movimientos = daoMovimiento.listarTodos();
        
        // Filtrar por fecha
        if (desde != null && hasta != null) {
            movimientos = movimientos.stream()
                .filter(m -> !m.getFecha().isBefore(desde) && !m.getFecha().isAfter(hasta))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Filtrar por tipo
        if (tipo != null && !tipo.isEmpty()) {
            movimientos = movimientos.stream()
                .filter(m -> m.getTipo().equals(tipo))
                .collect(java.util.stream.Collectors.toList());
        }
        
        return movimientos;
    }
    
    /**
     * Obtener el total de movimientos registrados
     * @return Cantidad de movimientos
     * @throws SQLException Error de base de datos
     */
    public int getTotalMovimientos() throws SQLException {
        return daoMovimiento.listarTodos().size();
    }
    
    /**
     * Excepción personalizada para errores de negocio
     */
    public static class BusinessException extends Exception {
        public BusinessException(String message) {
            super(message);
        }
    }
}