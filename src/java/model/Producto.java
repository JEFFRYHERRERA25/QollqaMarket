package model;

import java.math.BigDecimal;

public class Producto {
    private String codigo;
    private String nombre;
    private String unidad;
    private String grupo;
    private int stockMin;
    private int cantidad; // ESTE ES EL STOCK
    private BigDecimal precio; // Para ventas
    
    public Producto() {}
    
    public Producto(String codigo, String nombre, String unidad, String grupo, int stockMin, int cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidad = unidad;
        this.grupo = grupo;
        this.stockMin = stockMin;
        this.cantidad = cantidad;
    }
    
    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    
    public int getStockMin() { return stockMin; }
    public void setStockMin(int stockMin) { this.stockMin = stockMin; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    
    // NUEVO: Método para descontar stock
    public void descontarStock(int cantidadVendida) throws BusinessException {
        if (cantidadVendida > this.cantidad) {
            throw new BusinessException("Stock insuficiente. Disponible: " + this.cantidad);
        }
        this.cantidad -= cantidadVendida;
    }
    
    // NUEVO: Método para aumentar stock
    public void aumentarStock(int cantidadAgregada) {
        this.cantidad += cantidadAgregada;
    }
    
    // Método para verificar stock bajo
    public boolean isStockBajo() {
        return cantidad <= stockMin;
    }
    
    public boolean isAgotado() {
        return cantidad == 0;
    }
    
    public String getEstado() {
        if (isAgotado()) return "AGOTADO";
        if (isStockBajo()) return "BAJO";
        return "NORMAL";
    }
    
    @Override
    public String toString() {
        return nombre + " (Stock: " + cantidad + ")";
    }
    
    // Excepción personalizada
    public static class BusinessException extends Exception {
        public BusinessException(String message) {
            super(message);
        }
    }
}