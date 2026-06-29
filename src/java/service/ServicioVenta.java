package service;

import dao.DaoProducto;
import dao.DaoVenta;
import model.DetalleVenta; 
import model.Venta;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ServicioVenta {
    
    private DaoVenta daoVenta = new DaoVenta();
    private DaoProducto daoProducto = new DaoProducto();
    private ServicioProducto servicioProducto = new ServicioProducto();
    
    public void registrarVenta(Venta venta) throws SQLException, ServicioProducto.BusinessException {
        // Validar stock de cada producto
        for (DetalleVenta item : venta.getItems()) {
            var producto = daoProducto.buscarPorCodigo(item.getCodigo());
            if (producto == null) {
                throw new ServicioProducto.BusinessException("Producto no encontrado: " + item.getCodigo());
            }
            if (producto.getCantidad() < item.getCantidad()) {
                throw new ServicioProducto.BusinessException(
                    "Stock insuficiente para " + producto.getNombre() + 
                    ". Disponible: " + producto.getCantidad()
                );
            }
        }
        
        // Registrar la venta
        daoVenta.insertar(venta);
        
        // Actualizar stock y registrar movimientos (con costo = precio de venta)
        for (DetalleVenta item : venta.getItems()) {
            // Convertir BigDecimal a double
            double costo = item.getPrecio() != null ? item.getPrecio().doubleValue() : 0;
            
            servicioProducto.registrarMovimiento(
                item.getCodigo(),
                "SALIDA",
                item.getCantidad(),
                costo,  // ✅ double
                venta.getFecha(),
                "VENTA - Cliente: " + venta.getCliente(),
                venta.getVendedor()
            );
        }
    }
    
    public List<Venta> listarVentas() throws SQLException {
        return daoVenta.listarTodas();
    }
    
    public BigDecimal getTotalVentas() throws SQLException {
        return daoVenta.getTotalVentas();
    }
    
    public int getCantidadVentas() throws SQLException {
        return daoVenta.contarVentas();
    }
}