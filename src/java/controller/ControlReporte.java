package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import dao.DaoMovimiento;
import dao.DaoProducto;
import dao.DaoVenta;
import model.Movimiento;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/reportes")
public class ControlReporte extends HttpServlet {
    
    private DaoProducto daoProducto = new DaoProducto();
    private DaoVenta daoVenta = new DaoVenta();
    private DaoMovimiento daoMovimiento = new DaoMovimiento();
    
    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public JsonPrimitive serialize(LocalDate date, Type type, com.google.gson.JsonSerializationContext context) {
                return new JsonPrimitive(date.toString());
            }
        })
        .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        try {

            // ==================== DASHBOARD ====================
            if ("dashboard".equals(action)) {

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalProductos", daoProducto.contarProductos());
                stats.put("productosAgotados", daoProducto.contarProductosAgotados());
                stats.put("productosStockBajo", daoProducto.listarStockBajo().size());
                stats.put("totalVentas", daoVenta.getTotalVentas());
                stats.put("cantidadVentas", daoVenta.contarVentas());

                enviarJSON(resp, stats);
            }

            // ==================== MOVIMIENTOS ====================
            else if ("movimientos".equals(action)) {

                String desdeStr = req.getParameter("desde");
                String hastaStr = req.getParameter("hasta");
                String tipo = req.getParameter("tipo");

                System.out.println("=== REPORTE MOVIMIENTOS ===");
                System.out.println("Desde: " + desdeStr);
                System.out.println("Hasta: " + hastaStr);
                System.out.println("Tipo: " + tipo);

                List<Movimiento> movimientos = new ArrayList<>();

                try {
                    boolean tieneFechas = desdeStr != null && hastaStr != null && 
                                          !desdeStr.isEmpty() && !hastaStr.isEmpty();
                    boolean tieneTipo = tipo != null && !tipo.isEmpty() && 
                                        !"todos".equals(tipo) && !"Todos".equals(tipo) &&
                                        !"TODOS".equals(tipo) && !"".equals(tipo);

                    if (tieneFechas && tieneTipo) {
                        LocalDate desde = LocalDate.parse(desdeStr);
                        LocalDate hasta = LocalDate.parse(hastaStr);
                        movimientos = daoMovimiento.filtrarPorFechaYTipo(desde, hasta, tipo);
                        System.out.println("✅ Filtro: FECHA + TIPO");
                        
                    } else if (tieneFechas) {
                        LocalDate desde = LocalDate.parse(desdeStr);
                        LocalDate hasta = LocalDate.parse(hastaStr);
                        movimientos = daoMovimiento.filtrarPorFecha(desde, hasta);
                        System.out.println("✅ Filtro: SOLO FECHAS");
                        
                    } else if (tieneTipo) {
                        movimientos = daoMovimiento.filtrarPorTipo(tipo);
                        System.out.println("✅ Filtro: SOLO TIPO");
                        
                    } else {
                        movimientos = daoMovimiento.listarTodos();
                        System.out.println("✅ Filtro: TODOS");
                    }
                    
                    System.out.println("📊 Total movimientos encontrados: " + movimientos.size());
                    
                    if (!movimientos.isEmpty()) {
                        Movimiento primero = movimientos.get(0);
                        System.out.println("📝 Ejemplo - ID: " + primero.getId() + 
                                           ", Fecha: " + primero.getFecha() + 
                                           ", Producto: " + primero.getNombre() +
                                           ", Tipo: " + primero.getTipo());
                    } else {
                        System.out.println("⚠️ No se encontraron movimientos con los filtros aplicados");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Error al filtrar movimientos: " + e.getMessage());
                    e.printStackTrace();
                    movimientos = new ArrayList<>();
                }

                enviarJSON(resp, movimientos);
            }

            // ==================== EXPORTAR CSV ====================
            else if ("exportar-csv".equals(action)) {
                exportarCSV(req, resp);
            }

            // ==================== VISTA ====================
            else {
                req.getRequestDispatcher("/reportes.jsp").forward(req, resp);
            }

        } catch (Exception e) {
            manejarError(resp, e);
        }
    }

    // ==================== MÉTODO PARA RESPONDER JSON ====================
    private void enviarJSON(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    // ==================== MANEJO DE ERRORES ====================
    private void manejarError(HttpServletResponse resp, Exception e) throws IOException {
        System.err.println("❌ ERROR EN SERVLET: " + e.getMessage());
        e.printStackTrace();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("application/json");
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        resp.getWriter().print(gson.toJson(error));
    }

    // ==================== EXPORTAR CSV (MODIFICADO - SIN STOCK ANTERIOR Y NUEVO) ====================
    private void exportarCSV(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition",
                "attachment; filename=reporte_movimientos_" + LocalDate.now() + ".csv");

        PrintWriter out = resp.getWriter();

        try {
            // Obtener parámetros de filtro
            String desdeStr = req.getParameter("desde");
            String hastaStr = req.getParameter("hasta");
            String tipo = req.getParameter("tipo");
            
            List<Movimiento> movimientos = new ArrayList<>();
            
            // Aplicar los mismos filtros que en movimientos
            boolean tieneFechas = desdeStr != null && hastaStr != null && 
                                  !desdeStr.isEmpty() && !hastaStr.isEmpty();
            boolean tieneTipo = tipo != null && !tipo.isEmpty() && 
                                !"todos".equals(tipo) && !"Todos".equals(tipo) &&
                                !"TODOS".equals(tipo) && !"".equals(tipo);
            
            if (tieneFechas && tieneTipo) {
                LocalDate desde = LocalDate.parse(desdeStr);
                LocalDate hasta = LocalDate.parse(hastaStr);
                movimientos = daoMovimiento.filtrarPorFechaYTipo(desde, hasta, tipo);
                System.out.println("📥 CSV - Filtro: FECHA + TIPO");
            } else if (tieneFechas) {
                LocalDate desde = LocalDate.parse(desdeStr);
                LocalDate hasta = LocalDate.parse(hastaStr);
                movimientos = daoMovimiento.filtrarPorFecha(desde, hasta);
                System.out.println("📥 CSV - Filtro: SOLO FECHAS");
            } else if (tieneTipo) {
                movimientos = daoMovimiento.filtrarPorTipo(tipo);
                System.out.println("📥 CSV - Filtro: SOLO TIPO");
            } else {
                movimientos = daoMovimiento.listarTodos();
                System.out.println("📥 CSV - Filtro: TODOS");
            }
            
            System.out.println("📥 Exportando CSV - Movimientos encontrados: " + movimientos.size());

            // Escribir cabeceras CSV (NUEVAS - sin Stock Anterior ni Stock Nuevo)
            out.println("Fecha,Código,Producto,Tipo,Cantidad,Costo,Usuario,Observaciones");

            // Escribir datos (NUEVOS - sin cantidadAnterior ni cantidadNueva)
            for (Movimiento m : movimientos) {
                out.println(
                    (m.getFecha() != null ? m.getFecha() : "") + "," +
                    (m.getCodigo() != null ? m.getCodigo() : "") + "," +
                    (m.getNombre() != null ? "\"" + m.getNombre() + "\"" : "") + "," +
                    (m.getTipo() != null ? m.getTipo() : "") + "," +
                    m.getCantidad() + "," +
                    m.getCosto() + "," +
                    (m.getUsuario() != null ? m.getUsuario() : "") + "," +
                    (m.getObservaciones() != null ? "\"" + m.getObservaciones().replace(",", ";") + "\"" : "")
                );
            }
            
            out.flush();
            System.out.println("✅ CSV exportado correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error exportando CSV: " + e.getMessage());
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }
}