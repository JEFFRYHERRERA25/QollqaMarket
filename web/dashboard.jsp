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
    <title>Qollqa Market - Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="dashboard"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>📊 Dashboard General</h2>
                <p>Bienvenido, <%= usuario.getNombre() %></p>
            </div>
            <div class="content-body">
                <div class="stats-grid" id="statsGrid">
                    <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Productos</div></div>
                    <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Ventas Totales</div></div>
                    <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Ventas Realizadas</div></div>
                    <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Stock Bajo</div></div>
                    <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Agotados</div></div>
                </div>
                
                <div class="card">
                    <div class="card-header">⚠️ Alertas de Stock</div>
                    <div class="card-body">
                        <div id="alertsContainer"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        var contextPath = window.location.pathname.split('/')[1] || '';
        
        function cargarDashboard() {
            // Cargar estadísticas desde reportes
            fetch('/' + contextPath + '/reportes?action=dashboard')
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error('HTTP error ' + response.status);
                    }
                    return response.json();
                })
                .then(function(data) {
                    var statsGrid = document.getElementById('statsGrid');
                    if (statsGrid) {
                        statsGrid.innerHTML = 
                            '<div class="stat-card"><div class="stat-value">' + (data.totalProductos || 0) + '</div><div class="stat-label">Productos</div></div>' +
                            '<div class="stat-card"><div class="stat-value">S/ ' + (data.totalVentas || 0) + '</div><div class="stat-label">Ventas Totales</div></div>' +
                            '<div class="stat-card"><div class="stat-value">' + (data.cantidadVentas || 0) + '</div><div class="stat-label">Ventas Realizadas</div></div>' +
                            '<div class="stat-card"><div class="stat-value">' + (data.productosStockBajo || 0) + '</div><div class="stat-label">Stock Bajo</div></div>' +
                            '<div class="stat-card"><div class="stat-value">' + (data.productosAgotados || 0) + '</div><div class="stat-label">Agotados</div></div>';
                    }
                })
                .catch(function(error) {
                    console.error('Error cargando estadísticas:', error);
                    document.getElementById('statsGrid').innerHTML = '<div class="stat-card"><div class="stat-value">Error</div><div class="stat-label">Cargando</div></div>';
                });
            
            // Cargar alertas de stock
            fetch('/' + contextPath + '/productos?action=alertas')
                .then(function(response) {
                    return response.json();
                })
                .then(function(data) {
                    var container = document.getElementById('alertsContainer');
                    if (container) {
                        if (data.length === 0) {
                            container.innerHTML = '<div class="message success">✅ Todos los productos tienen stock suficiente</div>';
                        } else {
                            var html = '';
                            for (var i = 0; i < data.length; i++) {
                                var p = data[i];
                                var clase = (p.cantidad === 0) ? 'error' : 'warning';
                                html += '<div class="message ' + clase + '">';
                                html += '<strong>⚠️ ' + p.nombre + '</strong> - Stock: ' + p.cantidad + ' ' + (p.unidad || 'UNIDAD') + ' (Mín: ' + p.stockMin + ')';
                                html += '</div>';
                            }
                            container.innerHTML = html;
                        }
                    }
                })
                .catch(function(error) {
                    console.error('Error cargando alertas:', error);
                });
        }
        
        // Hacer la función global para que pueda ser llamada desde otros JSP
        window.cargarDashboard = cargarDashboard;
        
        // Cargar al iniciar
        cargarDashboard();
    </script>
</body>
</html>