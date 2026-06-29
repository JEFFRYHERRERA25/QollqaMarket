package model;

import java.time.LocalDate;

public class Movimiento {
    private Long id;
    private String codigo;
    private String nombre;
    private String tipo;
    private double cantidad;
    private double costo;
    private LocalDate fecha;
    private String observaciones;
    private String usuario;
    
    public Movimiento() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    
    public String getIconoTipo() {
        switch(tipo) {
            case "INGRESO": return "📥";
            case "SALIDA": return "📤";
            case "AJUSTE_POSITIVO": return "➕";
            case "AJUSTE_NEGATIVO": return "➖";
            default: return "🔄";
        }
    }
}