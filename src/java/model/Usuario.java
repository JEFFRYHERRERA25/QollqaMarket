package model;

public class Usuario {
    private String codUsuario;
    private String username;
    private String password;
    private String nombre;
    private String rol;
    private String estado;
    
    public Usuario() {}
    
    public Usuario(String codUsuario, String username, String nombre, String rol) {
        this.codUsuario = codUsuario;
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
    }
    
    // Getters y Setters
    public String getCodUsuario() { return codUsuario; }
    public void setCodUsuario(String codUsuario) { this.codUsuario = codUsuario; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
