package service;

import dao.DaoCliente;
import model.Cliente;
import java.sql.SQLException;
import java.util.List;

public class ServicioCliente {
    
    private DaoCliente daoCliente = new DaoCliente();
    
    public void registrarCliente(Cliente cliente) throws SQLException, BusinessException {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del cliente es obligatorio");
        }
        
        cliente.setId(String.valueOf(System.currentTimeMillis()));
        daoCliente.insertar(cliente);
    }
    
    public List<Cliente> listarClientes() throws SQLException {
        return daoCliente.listarTodos();
    }
    
    public Cliente buscarCliente(String id) throws SQLException {
        return daoCliente.buscarPorId(id);
    }
    
    // ==================== NUEVO MÉTODO: ACTUALIZAR CLIENTE ====================
    /**
     * Actualizar los datos de un cliente existente
     * @param id ID del cliente
     * @param nombre Nuevo nombre
     * @param dni Nuevo DNI
     * @param telefono Nuevo teléfono
     * @param direccion Nueva dirección
     * @throws SQLException Error de base de datos
     * @throws BusinessException Error de negocio
     */
    public void actualizarCliente(String id, String nombre, String dni, String telefono, String direccion) 
            throws SQLException, BusinessException {
        
        // Validaciones
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("ID de cliente no válido");
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BusinessException("El nombre del cliente es obligatorio");
        }
        
        // Verificar si el cliente existe
        Cliente clienteExistente = daoCliente.buscarPorId(id);
        if (clienteExistente == null) {
            throw new BusinessException("Cliente no encontrado");
        }
        
        // Crear objeto cliente con los datos actualizados
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(id);
        clienteActualizado.setNombre(nombre);
        clienteActualizado.setDni(dni != null ? dni : "");
        clienteActualizado.setTelefono(telefono != null ? telefono : "");
        clienteActualizado.setDireccion(direccion != null ? direccion : "");
        
        // Actualizar en la base de datos
        daoCliente.actualizar(clienteActualizado);
    }
    // ==================== FIN MÉTODO ACTUALIZAR CLIENTE ====================
    
    // ==================== NUEVO MÉTODO: ELIMINAR CLIENTE ====================
    /**
     * Eliminar un cliente por su ID
     * @param id ID del cliente a eliminar
     * @throws SQLException Error de base de datos
     * @throws BusinessException Error de negocio (cliente no encontrado)
     */
    public void eliminarCliente(String id) throws SQLException, BusinessException {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("ID de cliente no válido");
        }
        
        // Verificar si el cliente existe
        Cliente clienteExistente = daoCliente.buscarPorId(id);
        if (clienteExistente == null) {
            throw new BusinessException("Cliente no encontrado");
        }
        
        // Eliminar de la base de datos
        daoCliente.eliminar(id);
    }
    // ==================== FIN MÉTODO ELIMINAR CLIENTE ====================
    
    public static class BusinessException extends Exception {
        public BusinessException(String message) {
            super(message);
        }
    }
}