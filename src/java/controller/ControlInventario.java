package controller;

import com.google.gson.Gson;
import dao.DaoProducto;
import model.Producto;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/inventario")
public class ControlInventario extends HttpServlet {
    
    private DaoProducto daoProducto = new DaoProducto();
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
            resp.setCharacterEncoding("UTF-8");
            
            try {
                List<Producto> productos = daoProducto.listarTodos();
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(productos));
                
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
            
        } else if ("alertas".equals(action)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            
            try {
                List<Producto> alertas = daoProducto.listarStockBajo();
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(alertas));
                
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
            
        } else {
            // Mostrar página JSP
            req.getRequestDispatcher("/inventario.jsp").forward(req, resp);
        }
    }
}