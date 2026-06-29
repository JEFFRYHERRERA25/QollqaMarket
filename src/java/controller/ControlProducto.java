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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/productos")
public class ControlProducto extends HttpServlet {
    
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
            
        } else if ("buscar".equals(action)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            
            try {
                String texto = req.getParameter("texto");
                List<Producto> productos = daoProducto.buscarPorTexto(texto);
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(productos));
                
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
            }
            
        } else {
            req.getRequestDispatcher("/productos.jsp").forward(req, resp);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        String codigo = req.getParameter("codigo");
        String nombre = req.getParameter("nombre");
        String unidad = req.getParameter("unidad");
        String grupo = req.getParameter("grupo");
        int stockMin = Integer.parseInt(req.getParameter("stockMin"));
        
        // Validaciones
        if (codigo == null || codigo.trim().isEmpty()) {
            req.setAttribute("mensaje", "❌ El código es obligatorio");
            req.setAttribute("tipoMensaje", "error");
            req.getRequestDispatcher("/productos.jsp").forward(req, resp);
            return;
        }
        
        try {
            // Verificar si ya existe
            Producto existente = daoProducto.buscarPorCodigo(codigo);
            if (existente != null) {
                req.setAttribute("mensaje", "❌ El código de producto ya existe");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/productos.jsp").forward(req, resp);
                return;
            }
            
            Producto producto = new Producto();
            producto.setCodigo(codigo);
            producto.setNombre(nombre);
            producto.setUnidad(unidad);
            producto.setGrupo(grupo);
            producto.setStockMin(stockMin);
            producto.setCantidad(0);
            
            daoProducto.insertar(producto);
            req.setAttribute("mensaje", "✅ Producto registrado exitosamente");
            req.setAttribute("tipoMensaje", "success");
            
        } catch (SQLException e) {
            req.setAttribute("mensaje", "❌ Error al registrar producto: " + e.getMessage());
            req.setAttribute("tipoMensaje", "error");
        }
        
        req.getRequestDispatcher("/productos.jsp").forward(req, resp);
    }
}