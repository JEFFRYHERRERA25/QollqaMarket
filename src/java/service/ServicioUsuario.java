package service;

import dao.DaoUsuario;
import model.Usuario;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.security.MessageDigest;

/**
 * Servicio que maneja la lógica de negocio para la gestión de usuarios
 * (Solo accesible por administradores)
 */
public class ServicioUsuario {
    
    private DaoUsuario daoUsuario = new DaoUsuario();
    
    // ==================== POLÍTICAS DE PASSWORD ====================
    private void validarPassword(String password) throws BusinessException {
        if (password == null || password.length() < 8) {
            throw new BusinessException("La contraseña debe tener mínimo 8 caracteres");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new BusinessException("La contraseña debe incluir al menos un número");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            throw new BusinessException("La contraseña debe incluir al menos un carácter especial (!@#$%^&*...)");
        }
    }


     /* Encripta la contraseña usando MD5*/
    
    private String encriptarPassword(String password) throws BusinessException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("Error al encriptar la contraseña");
        }
    }
    // ==================== FIN POLÍTICAS DE PASSWORD ====================

    public void registrarUsuario(String username, String password, String rol, String nombre) 
            throws SQLException, BusinessException {
        
        // Validaciones básicas
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("El nombre de usuario es obligatorio");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria");
        }
        
        // ✅ POLÍTICA DE PASSWORD APLICADA
        validarPassword(password);
        
        if (rol == null || rol.trim().isEmpty()) {
            throw new BusinessException("El rol es obligatorio");
        }
        
        // Validar roles permitidos
        if (!rol.equals("admin") && !rol.equals("vendedor") && !rol.equals("almacenero")) {
            throw new BusinessException("Rol inválido. Use: admin, vendedor o almacenero");
        }
        
        // Verificar si el usuario ya existe
        List<Usuario> usuarios = daoUsuario.listarTodos();
        boolean existe = usuarios.stream().anyMatch(u -> u.getUsername().equals(username));
        
        if (existe) {
            throw new BusinessException("El usuario '" + username + "' ya existe");
        }
        
        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCodUsuario(generarCodigo());
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(encriptarPassword(password)); // ✅ ENCRIPTADO CON MD5
        nuevoUsuario.setNombre(nombre != null ? nombre : username);
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setEstado("ACTIVO");
        
        daoUsuario.insertar(nuevoUsuario);
    }
    
    /**
     * Listar todos los usuarios del sistema
     */
    public List<Usuario> listarUsuarios() throws SQLException {
        return daoUsuario.listarTodos();
    }
    
    /**
     * Buscar un usuario por su nombre de usuario
     */
    public Usuario buscarPorUsername(String username) throws SQLException {
        return daoUsuario.listarTodos().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Eliminar un usuario del sistema
     */
    public void eliminarUsuario(String username) throws SQLException, BusinessException {
        if (username.equals("admin")) {
            throw new BusinessException("No se puede eliminar al usuario administrador principal");
        }
        
        Usuario usuario = buscarPorUsername(username);
        if (usuario == null) {
            throw new BusinessException("Usuario no encontrado");
        }
        
        daoUsuario.eliminar(username);
    }
    
    /**
     * Cambiar el estado de un usuario (ACTIVO/INACTIVO)
     */
    public void cambiarEstadoUsuario(String username, String estado) throws SQLException, BusinessException {
        if (username.equals("admin") && estado.equals("INACTIVO")) {
            throw new BusinessException("No se puede desactivar al administrador principal");
        }
        // TODO: Implementar actualización de estado en DAO
        // daoUsuario.actualizarEstado(username, estado);
    }
    
    /**
     * Cambiar la contraseña de un usuario
     */
    public void cambiarPassword(String username, String nuevaPassword) throws SQLException, BusinessException {
        
        // ✅ POLÍTICA DE PASSWORD APLICADA
        validarPassword(nuevaPassword);
        
        Usuario usuario = buscarPorUsername(username);
        if (usuario == null) {
            throw new BusinessException("Usuario no encontrado");
        }
        
        // ✅ ENCRIPTADO CON MD5
        daoUsuario.actualizarPassword(username, encriptarPassword(nuevaPassword));
    }
    
    /**
     * Actualizar los datos de un usuario
     */
    public void actualizarUsuario(String oldUsername, String newUsername, String nombre, String password, String rol) 
            throws SQLException, BusinessException {
        
        // Validaciones
        if (oldUsername == null || oldUsername.trim().isEmpty()) {
            throw new BusinessException("Nombre de usuario actual no válido");
        }
        
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new BusinessException("El nuevo nombre de usuario es obligatorio");
        }
        
        if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException("Nombre de usuario no válido. Use solo letras, números y guión bajo");
        }
        
        if (oldUsername.equals("admin")) {
            throw new BusinessException("No se puede modificar el usuario administrador principal");
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BusinessException("El nombre es obligatorio");
        }
        
        if (rol == null || rol.trim().isEmpty()) {
            throw new BusinessException("El rol es obligatorio");
        }
        
        if (!rol.equals("admin") && !rol.equals("vendedor") && !rol.equals("almacenero")) {
            throw new BusinessException("Rol inválido. Use: admin, vendedor o almacenero");
        }
        
        Usuario usuarioExistente = buscarPorUsername(oldUsername);
        if (usuarioExistente == null) {
            throw new BusinessException("Usuario no encontrado: " + oldUsername);
        }
        
        if (!oldUsername.equals(newUsername)) {
            Usuario usuarioConNuevoUsername = buscarPorUsername(newUsername);
            if (usuarioConNuevoUsername != null) {
                throw new BusinessException("El nombre de usuario '" + newUsername + "' ya está en uso");
            }
        }
        
        boolean actualizarPassword = (password != null && !password.trim().isEmpty());
        if (actualizarPassword) {
            // ✅ POLÍTICA DE PASSWORD APLICADA AL EDITAR
            validarPassword(password);
            password = encriptarPassword(password); // ✅ ENCRIPTADO CON MD5
        }
        
        daoUsuario.actualizarUsuarioCompleto(oldUsername, newUsername, nombre, password, rol, actualizarPassword);
    }
    
    /**
     * Actualizar solo nombre y rol de un usuario
     */
    public void actualizarUsuario(String username, String nombre, String rol) 
            throws SQLException, BusinessException {
        actualizarUsuario(username, username, nombre, null, rol);
    }
    
    /**
     * Actualizar un usuario completo desde un objeto Usuario
     */
    public void actualizarUsuarioCompleto(Usuario usuario, boolean actualizarPassword) 
            throws SQLException, BusinessException {
        
        if (usuario == null) {
            throw new BusinessException("Usuario no puede ser nulo");
        }
        
        actualizarUsuario(
            usuario.getUsername(),
            usuario.getUsername(),
            usuario.getNombre(),
            actualizarPassword ? usuario.getPassword() : null,
            usuario.getRol()
        );
    }
    
    /**
     * Validar si un usuario tiene un rol específico
     */
    public boolean tieneRol(String username, String rol) throws SQLException {
        Usuario usuario = buscarPorUsername(username);
        return usuario != null && usuario.getRol().equals(rol);
    }
    
    /**
     * Validar si un usuario es administrador
     */
    public boolean esAdministrador(String username) throws SQLException {
        return tieneRol(username, "admin");
    }
    
    /**
     * Generar un código único para el usuario
     */
    private String generarCodigo() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "U" + uuid;
    }
    
    /**
     * Obtener estadísticas de usuarios
     */
    public java.util.Map<String, Integer> getEstadisticas() throws SQLException {
        List<Usuario> usuarios = daoUsuario.listarTodos();
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        stats.put("total", usuarios.size());
        stats.put("admin", (int) usuarios.stream().filter(u -> u.getRol().equals("admin")).count());
        stats.put("vendedor", (int) usuarios.stream().filter(u -> u.getRol().equals("vendedor")).count());
        stats.put("almacenero", (int) usuarios.stream().filter(u -> u.getRol().equals("almacenero")).count());
        stats.put("activos", (int) usuarios.stream().filter(u -> u.getEstado().equals("ACTIVO")).count());
        
        return stats;
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