package controller;

import com.google.gson.Gson;
import dao.DaoProducto;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/dashboard")
public class ControlDashboard extends HttpServlet {
    
    private DaoProducto daoProducto = new DaoProducto();
    private DaoVenta daoVenta = new DaoVenta();
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
        
        if ("estadisticas".equals(action)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            
            try {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalProductos", daoProducto.contarProductos());
                stats.put("productosAgotados", daoProducto.contarProductosAgotados());
                stats.put("productosStockBajo", daoProducto.listarStockBajo().size());
                stats.put("totalVentas", daoVenta.getTotalVentas());
                stats.put("cantidadVentas", daoVenta.contarVentas());
                
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(stats));
                
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
        } else {
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        }
    }
}