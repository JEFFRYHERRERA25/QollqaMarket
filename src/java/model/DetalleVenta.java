package model;

import java.math.BigDecimal;

public class DetalleVenta {
    
    private Long id;
    private Long ventaId;
    private String codigo;
    private String nombre;
    private int cantidad;
    private BigDecimal precio;
    private BigDecimal subtotal;
    
    // ==================== CONSTRUCTORES ====================
    
    public DetalleVenta() {}
    
    public DetalleVenta(String codigo, String nombre, int cantidad, BigDecimal precio, BigDecimal subtotal) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = subtotal;
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getVentaId() {
        return ventaId;
    }
    
    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();  // Auto-calcular cuando cambia cantidad
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
        calcularSubtotal();  // Auto-calcular cuando cambia precio
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    // ==================== MÉTODOS UTILITARIOS ====================
    
    /**
     * Calcula el subtotal automáticamente (cantidad * precio)
     */
    public void calcularSubtotal() {
        if (cantidad > 0 && precio != null) {
            this.subtotal = precio.multiply(new BigDecimal(cantidad));
        }
    }
    
    /**
     * Verifica si el detalle es válido
     */
    public boolean isValid() {
        return codigo != null && !codigo.isEmpty() 
            && nombre != null && !nombre.isEmpty()
            && cantidad > 0 
            && precio != null && precio.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public String toString() {
        return "DetalleVenta{" +
                "id=" + id +
                ", ventaId=" + ventaId +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", subtotal=" + subtotal +
                '}';
    }
}