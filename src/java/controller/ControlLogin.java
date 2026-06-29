package controller;

import dao.DaoUsuario;
import model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")
public class ControlLogin extends HttpServlet {

    private DaoUsuario daoUsuario = new DaoUsuario();

    // ── Rate limiting ─────────────────────────────────────────────
    private static final int  MAX_INTENTOS = 3;
    private static final long BLOQUEO_MS   = 60 * 1000L; 

    private static final ConcurrentHashMap<String, int[]> intentosFallidos = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long>  tiempoBloqueo    = new ConcurrentHashMap<>();
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ip = obtenerIP(req);

        // 1. ¿Está bloqueada esta IP?
        if (estaIpBloqueada(ip)) {
            long segundos = segundosRestantes(ip);
            long minutos  = Math.max(1, (segundos / 60) + 1);

            req.setAttribute("error",            "⛔ Demasiados intentos fallidos. Intente en " + minutos + " minuto(s).");
            req.setAttribute("bloqueado",         true);
            req.setAttribute("segundosRestantes", segundos);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            String  passwordMD5 = encriptarMD5(password);
            Usuario usuario     = daoUsuario.validarLogin(username, passwordMD5);

            if (usuario != null) {
                // ✅ Login correcto → limpiar contadores
                intentosFallidos.remove(ip);
                tiempoBloqueo.remove(ip);

                HttpSession session = req.getSession();
                session.setAttribute("usuario", usuario);
                session.setMaxInactiveInterval(30 * 60);
                resp.sendRedirect(req.getContextPath() + "/dashboard");

            } else {
                // ❌ Credenciales incorrectas → sumar intento
                registrarIntentoFallido(ip);

                int[] contador = intentosFallidos.getOrDefault(ip, new int[]{0});
                int   intentos  = contador[0];
                int   restantes = MAX_INTENTOS - intentos;

                // Atributo para los puntos del login.jsp
                req.setAttribute("intentosUsados", intentos);

                if (estaIpBloqueada(ip)) {
                    long segundos = segundosRestantes(ip);
                    req.setAttribute("error",            "⛔ Cuenta bloqueada por demaciado intentos fallidos.");
                    req.setAttribute("bloqueado",         true);
                    req.setAttribute("segundosRestantes", segundos);
                } else {
                    req.setAttribute("error", "❌ Credenciales incorrectas. Intentos restantes: " + restantes);
                }

                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }

        } catch (Exception e) {
            req.setAttribute("error", "❌ Error en el servidor: " + e.getMessage());
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }


    private boolean estaIpBloqueada(String ip) {
        Long tBloqueo = tiempoBloqueo.get(ip);
        if (tBloqueo == null) return false;
        if (System.currentTimeMillis() - tBloqueo < BLOQUEO_MS) return true;
        // Bloqueo expirado → limpiar
        tiempoBloqueo.remove(ip);
        intentosFallidos.remove(ip);
        return false;
    }

    private long segundosRestantes(String ip) {
        Long tBloqueo = tiempoBloqueo.get(ip);
        if (tBloqueo == null) return 0;
        long ms = BLOQUEO_MS - (System.currentTimeMillis() - tBloqueo);
        return Math.max(1, ms / 1000);
    }

    private void registrarIntentoFallido(String ip) {
        int[] contador = intentosFallidos.computeIfAbsent(ip, k -> new int[]{0});
        synchronized (contador) {
            contador[0]++;
            if (contador[0] >= MAX_INTENTOS) {
                tiempoBloqueo.put(ip, System.currentTimeMillis());
            }
        }
    }

    private String obtenerIP(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }


    private String encriptarMD5(String password) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}