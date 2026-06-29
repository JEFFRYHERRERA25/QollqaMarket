package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    
    private Long id;
    private LocalDate fecha;
    private String cliente;
    private String clienteId;
    private BigDecimal total;
    private String metodo;
    private String vendedor;
    private List<DetalleVenta> items;  // ← Esto está bien, solo importa la clase
    
    public Venta() {
        this.items = new ArrayList<>();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    
    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }
    
    public List<DetalleVenta> getItems() { return items; }
    public void setItems(List<DetalleVenta> items) { this.items = items; }
    
    public void agregarDetalle(DetalleVenta detalle) {
        this.items.add(detalle);
    }
}