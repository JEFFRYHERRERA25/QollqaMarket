package controller;

import com.google.gson.Gson;
import model.Usuario;
import service.ServicioUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/usuarios")
public class ControlUsuario extends HttpServlet {
    
    private ServicioUsuario servicioUsuario = new ServicioUsuario();
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
        
        // Verificar que sea administrador
        if (!"admin".equals(usuarioActual.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo administradores.");
            return;
        }
        
        String action = req.getParameter("action");
        
        if ("listar".equals(action)) {
            // Respuesta JSON para AJAX
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            
            try {
                List<Usuario> usuarios = servicioUsuario.listarUsuarios();
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(usuarios));
                
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
            
        } else if ("estadisticas".equals(action)) {
            // Respuesta JSON con estadísticas
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            
            try {
                var estadisticas = servicioUsuario.getEstadisticas();
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(estadisticas));
                
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
            
        } else {
            // Mostrar página JSP
            req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
        
        if (usuarioActual == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        // Verificar que sea administrador
        if (!"admin".equals(usuarioActual.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo administradores.");
            return;
        }
        
        String action = req.getParameter("action");
        
        if ("registrar".equals(action)) {
            registrarUsuario(req, resp);
        } else if ("eliminar".equals(action)) {
            eliminarUsuario(req, resp);
        } else if ("cambiar-estado".equals(action)) {
            cambiarEstado(req, resp);
        } else if ("cambiar-password".equals(action)) {
            cambiarPassword(req, resp);
        } else if ("editar".equals(action)) {
            editarUsuario(req, resp);
        } else {
            req.setAttribute("mensaje", "Acción no reconocida");
            req.setAttribute("tipoMensaje", "error");
            req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
        }
    }
    
    private void registrarUsuario(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String rol = req.getParameter("rol");
        String nombre = req.getParameter("nombre");
        
        try {
            servicioUsuario.registrarUsuario(username, password, rol, nombre);
            req.setAttribute("mensaje", "✅ Usuario registrado exitosamente");
            req.setAttribute("tipoMensaje", "success");
            
        } catch (ServicioUsuario.BusinessException e) {
            req.setAttribute("mensaje", "❌ " + e.getMessage());
            req.setAttribute("tipoMensaje", "error");
        } catch (Exception e) {
            req.setAttribute("mensaje", "❌ Error al registrar usuario: " + e.getMessage());
            req.setAttribute("tipoMensaje", "error");
        }
        
        // Cargar lista actualizada
        try {
            List<Usuario> usuarios = servicioUsuario.listarUsuarios();
            req.setAttribute("usuarios", usuarios);
        } catch (Exception e) {
            // Ignorar
        }
        
        req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
    }
    
    private void eliminarUsuario(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String username = req.getParameter("username");
        
        try {
            servicioUsuario.eliminarUsuario(username);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Usuario eliminado\"}");
            } else {
                req.setAttribute("mensaje", "✅ Usuario eliminado exitosamente");
                req.setAttribute("tipoMensaje", "success");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
            
        } catch (ServicioUsuario.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al eliminar usuario");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        }
    }
    
    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String username = req.getParameter("username");
        String estado = req.getParameter("estado");
        
        try {
            servicioUsuario.cambiarEstadoUsuario(username, estado);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Estado actualizado\"}");
            } else {
                req.setAttribute("mensaje", "✅ Estado actualizado");
                req.setAttribute("tipoMensaje", "success");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
            
        } catch (ServicioUsuario.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al cambiar estado");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        }
    }
    
    private void cambiarPassword(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String username = req.getParameter("username");
        String nuevaPassword = req.getParameter("nuevaPassword");
        
        try {
            servicioUsuario.cambiarPassword(username, nuevaPassword);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Contraseña actualizada\"}");
            } else {
                req.setAttribute("mensaje", "✅ Contraseña actualizada");
                req.setAttribute("tipoMensaje", "success");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
            
        } catch (ServicioUsuario.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al cambiar contraseña");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        }
    }
    
    // ==================== MÉTODO EDITAR USUARIO MODIFICADO ====================
    private void editarUsuario(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // Recibir los parámetros como los envía el frontend
        String oldUsername = req.getParameter("oldUsername");
        String newUsername = req.getParameter("newUsername");
        String nombre = req.getParameter("nombre");
        String password = req.getParameter("password");
        String rol = req.getParameter("rol");
        
        try {
            // Llamar al servicio con los nuevos parámetros
            servicioUsuario.actualizarUsuario(oldUsername, newUsername, nombre, password, rol);
            
            if (isAjaxRequest(req)) {
                resp.setContentType("application/json");
                resp.getWriter().print("{\"success\": true, \"message\": \"Usuario actualizado correctamente\"}");
            } else {
                req.setAttribute("mensaje", "✅ Usuario actualizado exitosamente");
                req.setAttribute("tipoMensaje", "success");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
            
        } catch (ServicioUsuario.BusinessException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                cargarListaUsuarios(req);
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"success\": false, \"error\": \"Error interno: " + e.getMessage() + "\"}");
            } else {
                req.setAttribute("mensaje", "❌ Error al actualizar usuario: " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/usuarios.jsp").forward(req, resp);
            }
        }
    }
    // ==================== FIN MÉTODO EDITAR USUARIO MODIFICADO ====================
    
    private void cargarListaUsuarios(HttpServletRequest req) {
        try {
            List<Usuario> usuarios = servicioUsuario.listarUsuarios();
            req.setAttribute("usuarios", usuarios);
        } catch (Exception e) {
            req.setAttribute("error", "Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    private boolean isAjaxRequest(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
    }
}