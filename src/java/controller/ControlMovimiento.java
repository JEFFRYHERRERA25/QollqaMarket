package controller;

import dao.DaoMovimiento;
import dao.DaoProducto;
import model.Movimiento;
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
import java.time.LocalDate;

@WebServlet("/movimientos")
public class ControlMovimiento extends HttpServlet {

    private DaoProducto daoProducto = new DaoProducto();
    private DaoMovimiento daoMovimiento = new DaoMovimiento();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        String idStr = req.getParameter("id");

        // ==================== EDITAR MOVIMIENTO ====================
        if ("editar".equals(action) && idStr != null && !idStr.isEmpty()) {
            try {
                Long id = Long.parseLong(idStr);
                Movimiento movimiento = daoMovimiento.buscarPorId(id);
                if (movimiento != null) {
                    req.setAttribute("movimiento", movimiento);
                    req.setAttribute("accion", "editar");
                } else {
                    req.setAttribute("mensaje", "❌ Movimiento no encontrado");
                    req.setAttribute("tipoMensaje", "error");
                }
            } catch (Exception e) {
                req.setAttribute("mensaje", "❌ Error: " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
            }
            req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
            return;
        }

        req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
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

        String action = req.getParameter("action");

        // ==================== ACTUALIZAR MOVIMIENTO (AJAX) ====================
        if ("actualizar".equals(action)) {
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                LocalDate fecha = LocalDate.parse(req.getParameter("fecha"));
                double cantidad = Double.parseDouble(req.getParameter("cantidad"));
                double costo = Double.parseDouble(req.getParameter("costo"));
                String observaciones = req.getParameter("observaciones");

                Movimiento mov = daoMovimiento.buscarPorId(id);
                if (mov != null) {
                    mov.setFecha(fecha);
                    mov.setCantidad(cantidad);
                    mov.setCosto(costo);
                    mov.setObservaciones(observaciones);
                    daoMovimiento.actualizar(mov);
                    out.print("{\"success\": true}");
                } else {
                    out.print("{\"success\": false, \"message\": \"Movimiento no encontrado\"}");
                }
            } catch (Exception e) {
                out.print("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
            }
            out.flush();
            return;
        }

        // ==================== ELIMINAR MOVIMIENTO (AJAX) ====================
        if ("eliminar".equals(action)) {
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                daoMovimiento.eliminarPorId(id);
                out.print("{\"success\": true}");
            } catch (Exception e) {
                out.print("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
            }
            out.flush();
            return;
        }

        // ==================== ACTUALIZAR MOVIMIENTO (FORMULARIO - VIEJO) ====================
        String accion = req.getParameter("accion");
        if ("actualizar".equals(accion)) {
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                String codigo = req.getParameter("codigo");
                String tipo = req.getParameter("tipo");
                String observaciones = req.getParameter("observaciones");
                double cantidad = Double.parseDouble(req.getParameter("cantidad"));
                LocalDate fecha = LocalDate.parse(req.getParameter("fecha"));
                double costo = Double.parseDouble(req.getParameter("costo"));

                Producto producto = daoProducto.buscarPorCodigo(codigo);
                if (producto == null) {
                    throw new Exception("Producto no encontrado");
                }

                Movimiento movimiento = new Movimiento();
                movimiento.setId(id);
                movimiento.setCodigo(codigo);
                movimiento.setNombre(producto.getNombre());
                movimiento.setTipo(tipo);
                movimiento.setCantidad(cantidad);
                movimiento.setCosto(costo);
                movimiento.setFecha(fecha);
                movimiento.setObservaciones(observaciones);
                movimiento.setUsuario(usuario.getUsername());

                daoMovimiento.actualizar(movimiento);

                req.setAttribute("mensaje", "✅ Movimiento actualizado correctamente");
                req.setAttribute("tipoMensaje", "success");
                resp.sendRedirect(req.getContextPath() + "/reportes");
                return;

            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("mensaje", "❌ Error: " + e.getMessage());
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
                return;
            }
        }

        // ==================== REGISTRAR MOVIMIENTO (NUEVO) ====================
        try {
            String codigo = req.getParameter("codigo");
            String tipo = req.getParameter("tipo");
            String observaciones = req.getParameter("observaciones");

            String cantidadStr = req.getParameter("cantidad");
            double cantidad = (cantidadStr == null || cantidadStr.isEmpty())
                    ? 0
                    : Double.parseDouble(cantidadStr);

            String fechaStr = req.getParameter("fecha");
            LocalDate fecha = (fechaStr == null || fechaStr.isEmpty())
                    ? LocalDate.now()
                    : LocalDate.parse(fechaStr);

            String costoStr = req.getParameter("costo");
            double costo = (costoStr == null || costoStr.isEmpty())
                    ? 0
                    : Double.parseDouble(costoStr);

            if (codigo == null || tipo == null || cantidad <= 0) {
                throw new Exception("Datos incompletos o inválidos");
            }

            Producto producto = daoProducto.buscarPorCodigo(codigo);

            if (producto == null) {
                req.setAttribute("mensaje", "❌ Producto no encontrado");
                req.setAttribute("tipoMensaje", "error");
                req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
                return;
            }

            int stockActual = producto.getCantidad();
            int nuevoStock = stockActual;

            switch (tipo) {
                case "INGRESO":
                    nuevoStock = stockActual + (int) cantidad;
                    break;
                case "SALIDA":
                    if (cantidad > stockActual) {
                        req.setAttribute("mensaje", "❌ Stock insuficiente. Disponible: " + stockActual);
                        req.setAttribute("tipoMensaje", "error");
                        req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
                        return;
                    }
                    nuevoStock = stockActual - (int) cantidad;
                    break;
                case "AJUSTE_POSITIVO":
                    nuevoStock = stockActual + (int) cantidad;
                    break;
                case "AJUSTE_NEGATIVO":
                    if (cantidad > stockActual) {
                        req.setAttribute("mensaje", "❌ No se puede ajustar. Stock actual: " + stockActual);
                        req.setAttribute("tipoMensaje", "error");
                        req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
                        return;
                    }
                    nuevoStock = stockActual - (int) cantidad;
                    break;
                default:
                    throw new Exception("Tipo de movimiento inválido");
            }

            daoProducto.actualizarStock(codigo, nuevoStock);

            Movimiento movimiento = new Movimiento();
            movimiento.setCodigo(codigo);
            movimiento.setNombre(producto.getNombre());
            movimiento.setTipo(tipo);
            movimiento.setCantidad(cantidad);
            movimiento.setFecha(fecha);
            movimiento.setObservaciones(observaciones);
            movimiento.setUsuario(usuario.getUsername());
            movimiento.setCosto(costo);

            daoMovimiento.insertar(movimiento);

            req.setAttribute("mensaje",
                    "✅ Movimiento registrado. Nuevo stock: " + nuevoStock + " " + producto.getUnidad());
            req.setAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("mensaje", "❌ Error: " + e.getMessage());
            req.setAttribute("tipoMensaje", "error");
        }

        req.getRequestDispatcher("/movimientos.jsp").forward(req, resp);
    }
}