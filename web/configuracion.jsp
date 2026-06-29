<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    if (!"admin".equals(usuario.getRol())) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Qollqa Market - Configuración</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="configuracion"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>⚙️ Configuración del Sistema</h2>
                <p>Herramientas de administración</p>
            </div>
            <div class="content-body">
                <div class="card">
                    <div class="card-header">Estadísticas del Sistema</div>
                    <div class="card-body">
                        <div class="stats-grid" id="statsGrid">
                            <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Productos</div></div>
                            <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Ventas Totales</div></div>
                            <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Usuarios</div></div>
                            <div class="stat-card"><div class="stat-value">--</div><div class="stat-label">Productos Agotados</div></div>
                        </div>
                    </div>
                </div>
                
                <div class="card">
                    <div class="card-header">Herramientas de Administración</div>
                    <div class="card-body">
                        <div class="actions">
                            <button class="btn btn-info" onclick="validarIntegridad()">✅ Validar Integridad</button>
                            <button class="btn btn-success" onclick="inicializarSistema()">🚀 Inicializar Sistema</button>
                            <button class="btn btn-warning" onclick="limpiarTodo()">🧹 Limpiar Todo</button>
                            <button class="btn btn-danger" onclick="resetSistema()">⚠️ Reset Sistema</button>
                        </div>
                        <div id="configResults"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        var contextPath = window.location.pathname.split('/')[1] || '';
        
        function mostrarMensaje(mensaje, tipo) {
            var container = document.getElementById('configResults');
            container.innerHTML = '<div class="message ' + tipo + '">' + mensaje + '</div>';
            setTimeout(function() {
                if (container.innerHTML) container.innerHTML = '';
            }, 4000);
        }
        
        function cargarEstadisticas() {
            fetch('/' + contextPath + '/configuracion?action=estadisticas')
                .then(function(response) {
                    return response.json();
                })
                .then(function(data) {
                    var statsGrid = document.getElementById('statsGrid');
                    statsGrid.innerHTML = 
                        '<div class="stat-card"><div class="stat-value">' + (data.totalProductos || 0) + '</div><div class="stat-label">Productos</div></div>' +
                        '<div class="stat-card"><div class="stat-value">S/ ' + (data.totalVentas || 0) + '</div><div class="stat-label">Ventas Totales</div></div>' +
                        '<div class="stat-card"><div class="stat-value">' + (data.totalUsuarios || 0) + '</div><div class="stat-label">Usuarios</div></div>' +
                        '<div class="stat-card"><div class="stat-value">' + (data.productosAgotados || 0) + '</div><div class="stat-label">Productos Agotados</div></div>';
                })
                .catch(function(error) {
                    console.error('Error:', error);
                });
        }
        
        function validarIntegridad() {
            mostrarMensaje('✅ Validación completada: El sistema está íntegro', 'success');
        }
        
        function inicializarSistema() {
            if (confirm('🚀 ¿Inicializar sistema? Se cargarán datos de demostración.')) {
                mostrarMensaje('🔄 Inicializando sistema...', 'info');
                
                fetch('/' + contextPath + '/configuracion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=inicializar'
                })
                .then(function(response) {
                    return response.json();
                })
                .then(function(data) {
                    if (data.success) {
                        mostrarMensaje('✅ ' + data.message, 'success');
                        setTimeout(function() { location.reload(); }, 1500);
                    } else {
                        mostrarMensaje('❌ Error: ' + data.error, 'error');
                    }
                })
                .catch(function(error) {
                    console.error('Error:', error);
                    mostrarMensaje('❌ Error al inicializar sistema', 'error');
                });
            }
        }
        
        function limpiarTodo() {
            if (confirm('⚠️ ¿Eliminar TODOS los datos? Esta acción no se puede deshacer.')) {
                mostrarMensaje('🔄 Eliminando datos...', 'info');
                
                fetch('/' + contextPath + '/configuracion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=limpiar'
                })
                .then(function(response) {
                    return response.json();
                })
                .then(function(data) {
                    if (data.success) {
                        mostrarMensaje('🧹 ' + data.message, 'success');
                        setTimeout(function() {
                            cargarEstadisticas();
                        }, 1000);
                    } else {
                        mostrarMensaje('❌ Error: ' + data.error, 'error');
                    }
                })
                .catch(function(error) {
                    console.error('Error:', error);
                    mostrarMensaje('❌ Error al limpiar datos', 'error');
                });
            }
        }
        
        function resetSistema() {
            if (confirm('⚠️⚠️⚠️ RESET TOTAL DEL SISTEMA ⚠️⚠️⚠️\n\n¿Está SEGURO?')) {
                mostrarMensaje('🔄 Reiniciando sistema...', 'warning');
                
                fetch('/' + contextPath + '/configuracion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'action=reset'
                })
                .then(function(response) {
                    return response.json();
                })
                .then(function(data) {
                    if (data.success) {
                        mostrarMensaje('🔄 ' + data.message, 'success');
                        setTimeout(function() { location.reload(); }, 2000);
                    } else {
                        mostrarMensaje('❌ Error: ' + data.error, 'error');
                    }
                })
                .catch(function(error) {
                    console.error('Error:', error);
                    mostrarMensaje('❌ Error al resetear sistema', 'error');
                });
            }
        }
        
        document.addEventListener('DOMContentLoaded', function() {
            cargarEstadisticas();
        });
    </script>
</body>
</html>