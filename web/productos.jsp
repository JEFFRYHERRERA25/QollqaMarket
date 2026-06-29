<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Qollqa Market - Productos</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <nav class="sidebar">
            <div class="sidebar-header">
                <h1>🏪 QOLLQA MARKET</h1>
                <div class="user-info">
                    <span><%= usuario.getNombre() %></span>
                    <span class="user-role <%= usuario.getRol() %>"><%= usuario.getRol().toUpperCase() %></span>
                </div>
            </div>
            <ul class="nav-menu">
                <li class="nav-item"><a href="${pageContext.request.contextPath}/dashboard" class="nav-link">📊 Dashboard</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/productos" class="nav-link active">➕ Productos</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/movimientos" class="nav-link">🔄 Movimientos</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/inventario" class="nav-link">📋 Inventario</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/reportes" class="nav-link">📈 Reportes</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/buscar" class="nav-link">🔍 Buscar</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/clientes" class="nav-link">👤 Clientes</a></li>
                <li class="nav-item"><a href="${pageContext.request.contextPath}/ventas" class="nav-link">🛒 Ventas</a></li>
                <% if ("admin".equals(usuario.getRol())) { %>
                    <li class="nav-item"><a href="${pageContext.request.contextPath}/usuarios" class="nav-link">👥 Usuarios</a></li>
                    <li class="nav-item"><a href="${pageContext.request.contextPath}/configuracion" class="nav-link">⚙️ Configuración</a></li>
                <% } %>
            </ul>
            <div class="sidebar-footer">
                <a href="${pageContext.request.contextPath}/logout" class="btn-logout">🚪 Cerrar Sesión</a>
            </div>
        </nav>

        <div class="main-content">
            <div class="content-header">
                <h2>➕ Registrar Nuevo Producto</h2>
                <p>Complete los datos del producto</p>
            </div>
            <div class="content-body">
                <% if (request.getAttribute("mensaje") != null) { %>
                    <div class="message <%= request.getAttribute("tipoMensaje") %>">
                        <%= request.getAttribute("mensaje") %>
                    </div>
                <% } %>
                
                <div class="card">
                    <div class="card-header">Información del Producto</div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/productos" method="POST">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label>📌 Código *</label>
                                    <input type="text" name="codigo" placeholder="Ej: PROD-001" required>
                                </div>
                                <div class="form-group">
                                    <label>📝 Nombre *</label>
                                    <input type="text" name="nombre" placeholder="Nombre del producto" required>
                                </div>
                                <div class="form-group">
                                    <label>📏 Unidad de Medida</label>
                                    <select name="unidad" required>
                                        <option value="UNIDAD">UNIDAD</option>
                                        <option value="KILOGRAMO">KILOGRAMO</option>
                                        <option value="LITRO">LITRO</option>
                                        <option value="PAQUETE">PAQUETE</option>
                                        <option value="CAJA">CAJA</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>🏷️ Grupo</label>
                                    <select name="grupo" required>
                                        <option value="ABARROTES">ABARROTES</option>
                                        <option value="BEBIDAS">BEBIDAS</option>
                                        <option value="LIMPIEZA">LIMPIEZA</option>
                                        <option value="LACTEOS">LACTEOS</option>
                                        <option value="SNACKS">SNACKS</option>
                                        <option value="OTROS">OTROS</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>⚠️ Stock Mínimo</label>
                                    <input type="number" name="stockMin" value="5" min="0">
                                </div>
                            </div>
                            <div class="actions">
                                <button type="submit" class="btn btn-success">💾 Registrar Producto</button>
                                <button type="reset" class="btn btn-secondary">🧹 Limpiar</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>