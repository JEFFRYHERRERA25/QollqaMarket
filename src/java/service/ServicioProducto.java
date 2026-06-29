package service;

import dao.DaoProducto;
import dao.DaoMovimiento;
import model.Movimiento;
import model.Producto;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ServicioProducto {
    
    private DaoProducto daoProducto = new DaoProducto();
    private DaoMovimiento daoMovimiento = new DaoMovimiento();
    
    // Registrar nuevo producto (stock inicial 0)
    public void registrarProducto(Producto producto, String usuario) throws SQLException, BusinessException {
        // Validaciones
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new BusinessException("El código del producto es obligatorio");
        }
        
        if (daoProducto.buscarPorCodigo(producto.getCodigo()) != null) {
            throw new BusinessException("El código de producto ya existe");
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }
        
        // El stock inicial siempre es 0
        producto.setCantidad(0);
        
        // Guardar producto
        daoProducto.insertar(producto);
    }
    
    // Registrar movimiento (ingreso, salida, ajuste)
    public void registrarMovimiento(String codigo, String tipo, double cantidad, double costo,
                                    LocalDate fecha, String observaciones, String usuario) 
                                    throws SQLException, BusinessException {
        
        Producto producto = daoProducto.buscarPorCodigo(codigo);
        if (producto == null) {
            throw new BusinessException("Producto no encontrado");
        }
        
        int stockActual = producto.getCantidad();
        int nuevoStock = stockActual;
        
        switch (tipo) {
            case "INGRESO":
                nuevoStock = stockActual + (int) cantidad;
                break;
            case "SALIDA":
                if (cantidad > stockActual) {
                    throw new BusinessException("Stock insuficiente. Disponible: " + stockActual);
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
                throw new BusinessException("Tipo de movimiento inválido");
        }
        
        // Actualizar stock
        daoProducto.actualizarStock(codigo, nuevoStock);
        
        // Registrar movimiento (sin cantidadAnterior ni cantidadNueva)
        Movimiento movimiento = new Movimiento();
        movimiento.setCodigo(codigo);
        movimiento.setNombre(producto.getNombre());
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setCosto(costo);
        movimiento.setFecha(fecha);
        movimiento.setObservaciones(observaciones);
        movimiento.setUsuario(usuario);
        
        daoMovimiento.insertar(movimiento);
    }
    
    // Listar todos los productos
    public List<Producto> listarProductos() throws SQLException {
        return daoProducto.listarTodos();
    }
    
    // Buscar productos por texto
    public List<Producto> buscarProductos(String texto) throws SQLException {
        if (texto == null || texto.trim().isEmpty()) {
            return daoProducto.listarTodos();
        }
        return daoProducto.buscarPorTexto(texto);
    }
    
    // Obtener alertas de stock
    public List<Producto> obtenerAlertasStock() throws SQLException {
        return daoProducto.listarStockBajo();
    }
    
    // Estadísticas para dashboard
    public int getTotalProductos() throws SQLException {
        return daoProducto.contarProductos();
    }
    
    public int getProductosAgotados() throws SQLException {
        return daoProducto.contarProductosAgotados();
    }
    
    // Excepción personalizada
    public static class BusinessException extends Exception {
        public BusinessException(String message) {
            super(message);
        }
    }
}
