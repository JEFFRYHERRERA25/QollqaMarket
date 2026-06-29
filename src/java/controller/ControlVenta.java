package controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.DaoCliente;
import dao.DaoProducto;
import dao.DaoVenta;
import model.Cliente;
import model.DetalleVenta;
import model.Producto;
import model.Usuario;
import model.Venta;
import service.ServicioVenta;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/ventas")
public class ControlVenta extends HttpServlet {

private DaoVenta daoVenta = new DaoVenta();
private DaoProducto daoProducto = new DaoProducto();
private DaoCliente daoCliente = new DaoCliente();

private ServicioVenta servicioVenta = new ServicioVenta();

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

            List<Venta> ventas = daoVenta.listarTodas();

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(ventas));

        } catch (Exception e) {

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
        }

    } else if ("clientes".equals(action)) {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {

            List<Cliente> clientes = daoCliente.listarTodos();

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(clientes));

        } catch (Exception e) {

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
        }

    } else if ("productos".equals(action)) {

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

    } else {

        req.getRequestDispatcher("/ventas.jsp").forward(req, resp);
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

    StringBuilder sb = new StringBuilder();

    try (BufferedReader reader = req.getReader()) {

        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
    }

    String json = sb.toString();

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    try {

        Map<String, Object> ventaData =
                gson.fromJson(json,
                        new TypeToken<Map<String, Object>>() {}.getType());

        Venta venta = new Venta();

        venta.setFecha(LocalDate.parse((String) ventaData.get("fecha")));
        venta.setCliente((String) ventaData.get("cliente"));
        venta.setClienteId((String) ventaData.get("clienteId"));
        venta.setMetodo((String) ventaData.get("metodo"));
        venta.setVendedor(usuario.getUsername());
        venta.setTotal(new BigDecimal((String) ventaData.get("total")));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsJson =
                (List<Map<String, Object>>) ventaData.get("items");

        for (Map<String, Object> itemJson : itemsJson) {

            DetalleVenta item = new DetalleVenta();

            item.setCodigo((String) itemJson.get("codigo"));
            item.setNombre((String) itemJson.get("nombre"));
            item.setCantidad(((Double) itemJson.get("cantidad")).intValue());
            item.setPrecio(new BigDecimal((String) itemJson.get("precio")));
            item.setSubtotal(new BigDecimal((String) itemJson.get("subtotal")));

            venta.getItems().add(item);
        }

        // REGISTRAR VENTA + DESCONTAR STOCK + CREAR MOVIMIENTO SALIDA
        servicioVenta.registrarVenta(venta);

        resp.getWriter().print(
                "{\"success\": true, \"message\": \"Venta registrada exitosamente\"}"
        );

    } catch (Exception e) {

        e.printStackTrace();

        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        resp.getWriter().print(
                "{\"success\": false, \"error\": \"" +
                        e.getMessage().replace("\"", "'") +
                        "\"}"
        );
    }
}
}

