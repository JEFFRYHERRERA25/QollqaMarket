package controller;

import com.google.gson.Gson;
import dao.DaoProducto;
import model.Producto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/buscar")
public class ControlBuscar extends HttpServlet {

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

        // ================= BUSCAR PRODUCTOS =================
        if ("productos".equals(action)) {

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            try {

                String texto = req.getParameter("texto");

                List<Producto> productos = daoProducto.buscarPorTexto(texto);

                String json = gson.toJson(productos);

                PrintWriter out = resp.getWriter();
                out.print(json);
                out.flush();

            } catch (Exception e) {

                e.printStackTrace();

                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                resp.getWriter().print(
                    "{\"error\":\"" + e.getMessage() + "\"}"
                );
            }

        } else {

            req.getRequestDispatcher("/buscar.jsp").forward(req, resp);
        }
    }

    // ================= MODIFICAR Y ELIMINAR PRODUCTO =================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String accion = req.getParameter("accion");
            
            // ================= ELIMINAR PRODUCTO =================
            if ("eliminar".equals(accion)) {
                String codigo = req.getParameter("codigo");
                daoProducto.eliminar(codigo);
                resp.setContentType("text/plain");
                resp.getWriter().print("OK");
                return;
            }
            
            // ================= MODIFICAR PRODUCTO =================
            Producto producto = new Producto();

            producto.setCodigo(req.getParameter("codigo"));
            producto.setNombre(req.getParameter("nombre"));
            producto.setGrupo(req.getParameter("grupo"));
            producto.setUnidad(req.getParameter("unidad"));
            producto.setStockMin(
                Integer.parseInt(req.getParameter("stockMin"))
            );

            producto.setPrecio(BigDecimal.ZERO);

            daoProducto.actualizar(producto);

            resp.setContentType("text/plain");
            resp.getWriter().print("OK");

        } catch (Exception e) {

            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            resp.getWriter().print("ERROR");
        }
    }
}