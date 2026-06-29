package controller;

import com.google.gson.Gson;
import model.Cliente;
import service.ServicioCliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/clientes")
public class ControlCliente extends HttpServlet {
    
    private ServicioCliente servicioCliente = new ServicioCliente();
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        String action = req.getParameter("action");
        
        if ("listar".equals(action)) {
            resp.setContentType("application/json");
            try {
                List<Cliente> clientes = servicioCliente.listarClientes();
                resp.getWriter().print(gson.toJson(clientes));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
        } else {
            req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        String action = req.getParameter("action");
        
        if ("registrar".equals(action)) {
            registrarCliente(req, resp);
        } else if ("editar".equals(action)) {
            editarCliente(req, resp);
        } else if ("eliminar".equals(action)) {
            eliminarCliente(req, resp);
        } else {
            // Si no hay action, procesar como registro normal (por compatibilidad)
            registrarCliente(req, resp);
        }
    }
    
    private void registrarCliente(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String nombre = req.getParameter("nombre");
        String dni = req.getParameter("dni");
        String telefono = req.getParameter("telefono");
        String direccion = req.getParameter("direccion");
        
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setDni(dni);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        
        try {
            servicioCliente.registrarCliente(cliente);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Cliente registrado\"}");
            } else {
                req.setAttribute("mensaje", "✅ Cliente registrado exitosamente");
                req.setAttribute("tipoMensaje", "success");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
            
        } catch (ServicioCliente.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al registrar cliente");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
        }
    }
    
    // ==================== NUEVO MÉTODO: EDITAR CLIENTE ====================
    private void editarCliente(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String id = req.getParameter("id");
        String nombre = req.getParameter("nombre");
        String dni = req.getParameter("dni");
        String telefono = req.getParameter("telefono");
        String direccion = req.getParameter("direccion");
        
        try {
            servicioCliente.actualizarCliente(id, nombre, dni, telefono, direccion);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Cliente actualizado correctamente\"}");
            } else {
                req.setAttribute("mensaje", "✅ Cliente actualizado exitosamente");
                req.setAttribute("tipoMensaje", "success");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
            
        } catch (ServicioCliente.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno: " + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al actualizar cliente");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
        }
    }
    // ==================== FIN MÉTODO EDITAR CLIENTE ====================
    
    // ==================== NUEVO MÉTODO: ELIMINAR CLIENTE ====================
    private void eliminarCliente(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String id = req.getParameter("id");
        
        try {
            servicioCliente.eliminarCliente(id);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Cliente eliminado\"}");
            } else {
                req.setAttribute("mensaje", "✅ Cliente eliminado exitosamente");
                req.setAttribute("tipoMensaje", "success");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
            
        } catch (ServicioCliente.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al eliminar cliente");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/clientes.jsp").forward(req, resp);
            }
        }
    }
    // ==================== FIN MÉTODO ELIMINAR CLIENTE ====================
    
    private boolean isAjaxRequest(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
    }
}