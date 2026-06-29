<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    String active = request.getParameter("active");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
<nav class="sidebar">
    <div class="sidebar-header">
        <h1>🏪 QOLLQA MARKET</h1>
        <p class="sidebar-subtitle">Sistema de Inventario</p>
        <div class="user-info">
            <span>👤 <%= usuario.getNombre() %></span>
            <span class="user-role <%= usuario.getRol() %>"><%= usuario.getRol().toUpperCase() %></span>
        </div>
    </div>
    <ul class="nav-menu">
        <li class="nav-item"><a href="${pageContext.request.contextPath}/dashboard" class="nav-link <%= "dashboard".equals(active) ? "active" : "" %>">📊 Dashboard</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/productos" class="nav-link <%= "productos".equals(active) ? "active" : "" %>">➕ Productos</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/movimientos" class="nav-link <%= "movimientos".equals(active) ? "active" : "" %>">🔄 Movimientos</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/inventario" class="nav-link <%= "inventario".equals(active) ? "active" : "" %>">📋 Inventario</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/reportes" class="nav-link <%= "reportes".equals(active) ? "active" : "" %>">📈 Reportes</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/buscar" class="nav-link <%= "buscar".equals(active) ? "active" : "" %>">🔍 Buscar</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/clientes" class="nav-link <%= "clientes".equals(active) ? "active" : "" %>">👥 Clientes</a></li>
        <li class="nav-item"><a href="${pageContext.request.contextPath}/ventas" class="nav-link <%= "ventas".equals(active) ? "active" : "" %>">🛒 Ventas</a></li>
        <% if ("admin".equals(usuario.getRol())) { %>
            <li class="nav-item"><a href="${pageContext.request.contextPath}/usuarios" class="nav-link <%= "usuarios".equals(active) ? "active" : "" %>">👑 Usuarios</a></li>
            <li class="nav-item"><a href="${pageContext.request.contextPath}/configuracion" class="nav-link <%= "configuracion".equals(active) ? "active" : "" %>">⚙️ Configuración</a></li>
        <% } %>
    </ul>
    <div class="sidebar-footer">
        <a href="${pageContext.request.contextPath}/logout" class="btn-logout">🚪 Cerrar Sesión</a>
    </div>
</nav>
</body>
</html>