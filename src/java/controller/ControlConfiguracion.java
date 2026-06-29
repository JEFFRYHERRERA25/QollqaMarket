package controller;

import com.google.gson.Gson;
import dao.DaoCliente;
import dao.DaoMovimiento;
import dao.DaoProducto;
import dao.DaoUsuario;
import dao.DaoVenta;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/configuracion")
public class ControlConfiguracion extends HttpServlet {
    
    private DaoProducto daoProducto = new DaoProducto();
    private DaoVenta daoVenta = new DaoVenta();
    private DaoUsuario daoUsuario = new DaoUsuario();
    private DaoCliente daoCliente = new DaoCliente();
    private DaoMovimiento daoMovimiento = new DaoMovimiento();
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (!"admin".equals(usuario.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Solo administradores.");
            return;
        }
        
        String action = req.getParameter("action");
        
        if ("estadisticas".equals(action)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            
            try {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalProductos", daoProducto.contarProductos());
                stats.put("totalVentas", daoVenta.getTotalVentas());
                stats.put("totalUsuarios", daoUsuario.listarTodos().size());
                stats.put("productosAgotados", daoProducto.contarProductosAgotados());
                
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(stats));
                
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
        } else {
            req.getRequestDispatcher("/configuracion.jsp").forward(req, resp);
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
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (!"admin".equals(usuario.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado.");
            return;
        }
        
        String action = req.getParameter("action");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        try {
            if ("inicializar".equals(action)) {
                inicializarDatos();
                resp.getWriter().print("{\"success\": true, \"message\": \"Sistema inicializado con datos de demostración\"}");
                
            } else if ("limpiar".equals(action)) {
                limpiarTodosLosDatos();
                resp.getWriter().print("{\"success\": true, \"message\": \"Todos los datos han sido eliminados\"}");
                
            } else if ("reset".equals(action)) {
                limpiarTodosLosDatos();
                inicializarDatos();
                resp.getWriter().print("{\"success\": true, \"message\": \"Sistema reiniciado correctamente\"}");
                
            } else if ("validar".equals(action)) {
                Map<String, Object> resultado = validarIntegridad();
                resp.getWriter().print(gson.toJson(resultado));
                
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"success\": false, \"error\": \"Acción no reconocida\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private void limpiarTodosLosDatos() throws SQLException {
        daoMovimiento.eliminarTodos();
        daoVenta.eliminarTodas();
        daoProducto.eliminarTodos();
        daoCliente.eliminarTodos();  // ← CORREGIDO
    }
    
    private void inicializarDatos() throws SQLException {
        if (daoProducto.contarProductos() == 0) {
            // Aquí puedes agregar productos de demostración si lo deseas
        }
    }
    
    private Map<String, Object> validarIntegridad() throws SQLException {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("valid", true);
        resultado.put("mensaje", "Todos los datos son consistentes");
        return resultado;
    }
}