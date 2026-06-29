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
    <title>Qollqa Market - Inventario</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="inventario"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>📋 Control de Inventario</h2>
                <p>Stock actual de todos los productos</p>
            </div>
            <div class="content-body">
                <div class="card">
                    <div class="card-header">Stock Actual</div>
                    <div class="card-body">
                        <div class="actions">
                            <button class="btn btn-primary" id="btnActualizarStock">🔄 Actualizar Stock</button>
                            <button class="btn btn-success" id="btnExportarStock">📥 Exportar CSV</button>
                            <button class="btn btn-warning" id="btnSoloAlertas">⚠️ Solo Alertas</button>
                        </div>
                        <div class="table-container" id="stockTable"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        let productos = [];
        
        function mostrarStock(filtrarAlertas = false) {
            fetch('${pageContext.request.contextPath}/productos?action=listar')
                .then(response => response.json())
                .then(data => {
                    productos = data;
                    let datos = filtrarAlertas ? data.filter(p => p.cantidad <= p.stockMin) : data;
                    
                    if (datos.length === 0) {
                        document.getElementById('stockTable').innerHTML = '<div class="message info">📦 No hay productos registrados</div>';
                        return;
                    }
                    
                    document.getElementById('stockTable').innerHTML = `
                        <table class="data-table">
                            <thead>
                                <tr><th>Código</th><th>Nombre</th><th>Grupo</th><th>Stock</th><th>Unidad</th><th>Stock Mínimo</th><th>Estado</th></tr>
                            </thead>
                            <tbody>
                                \${datos.map(p => {
                                    let estado = p.cantidad <= p.stockMin ? (p.cantidad === 0 ? 'AGOTADO' : 'BAJO') : 'NORMAL';
                                    let clase = p.cantidad === 0 ? 'status-zero' : (p.cantidad <= p.stockMin ? 'status-low' : 'status-normal');
                                    return `<tr><td>\${p.codigo}</td><td>\${p.nombre}</td><td>\${p.grupo}</td><td><strong>\${p.cantidad}</strong></td><td>\${p.unidad}</td><td>\${p.stockMin}</td><td><span class="\${clase}">\${estado}</span></td></tr>`;
                                }).join('')}
                            </tbody>
                        </table>
                    `;
                });
        }
        
        function exportarCSV() {
            if (productos.length === 0) {
                alert('No hay productos para exportar');
                return;
            }
            const headers = ['Código,Nombre,Grupo,Stock,Unidad,Stock Mínimo,Estado'];
            const rows = productos.map(p => `\${p.codigo},\${p.nombre},\${p.grupo},\${p.cantidad},\${p.unidad},\${p.stockMin},\${p.cantidad <= p.stockMin ? (p.cantidad === 0 ? 'AGOTADO' : 'BAJO') : 'NORMAL'}`);
            const csv = headers.concat(rows).join('\n');
            const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = `inventario_\${new Date().toISOString().split('T')[0]}.csv`;
            link.click();
            alert('📥 Inventario exportado');
        }
        
        document.getElementById('btnActualizarStock').onclick = () => mostrarStock(false);
        document.getElementById('btnExportarStock').onclick = exportarCSV;
        document.getElementById('btnSoloAlertas').onclick = () => mostrarStock(true);
        
        mostrarStock(false);
    </script>
</body>
</html>