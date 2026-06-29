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
    <title>Qollqa Market - Reportes</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="reportes"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>📈 Reportes y Análisis</h2>
                <p>Historial de movimientos</p>
            </div>

            <div class="content-body">
                <div class="card">
                    <div class="card-header">Historial de Movimientos</div>

                    <div class="card-body">
                        <div class="form-grid">
                            <div class="form-group">
                                <label>📅 Fecha Desde</label>
                                <input type="date" id="fechaDesde">
                            </div>

                            <div class="form-group">
                                <label>📅 Fecha Hasta</label>
                                <input type="date" id="fechaHasta">
                            </div>

                            <div class="form-group">
                                <label>🔍 Filtrar por Tipo</label>
                                <select id="filtroTipo">
                                    <option value="">Todos los movimientos</option>
                                    <option value="INGRESO">📥 Solo Ingresos</option>
                                    <option value="SALIDA">📤 Solo Salidas</option>
                                    <option value="AJUSTE_POSITIVO">➕ Solo Ajustes Positivos</option>
                                    <option value="AJUSTE_NEGATIVO">➖ Solo Ajustes Negativos</option>
                                </select>
                            </div>
                        </div>

                        <div class="actions">
                            <button class="btn btn-primary" onclick="generarReporte()">🔍 Generar Reporte</button>
                            <button class="btn btn-success" onclick="exportarReporte()">📥 Exportar CSV</button>
                        </div>

                        <div class="table-container">
                            <div id="historialTable"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- MODAL PARA EDITAR -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>✏️ Editar Movimiento</h3>
                <span class="close">&times;</span>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editId">
                
                <div class="form-group">
                    <label>📅 Fecha</label>
                    <input type="date" id="editFecha" class="form-control">
                </div>
                
                <div class="form-group">
                    <label>🔢 Cantidad</label>
                    <input type="number" id="editCantidad" step="1" class="form-control">
                </div>
                
                <div class="form-group">
                    <label>💰 Costo (Bs)</label>
                    <input type="number" id="editCosto" step="0.01" class="form-control">
                </div>
                
                <div class="form-group">
                    <label>📝 Observaciones</label>
                    <textarea id="editObservaciones" rows="3" class="form-control"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="cerrarModal()">Cancelar</button>
                <button class="btn btn-primary" onclick="guardarEdicion()">💾 Guardar Cambios</button>
            </div>
        </div>
    </div>

    <!-- MODAL PARA ELIMINAR -->
    <div id="deleteModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>⚠️ Confirmar Eliminación</h3>
                <span class="close-delete">&times;</span>
            </div>
            <div class="modal-body">
                <p>¿Estás seguro de que deseas eliminar este movimiento?</p>
                <p class="text-danger">⚠️ Esta acción no se puede deshacer.</p>
                <input type="hidden" id="deleteId">
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="cerrarModalDelete()">Cancelar</button>
                <button class="btn btn-danger" onclick="confirmarEliminar()">🗑️ Sí, Eliminar</button>
            </div>
        </div>
    </div>

<script>
    var contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    var movimientoActual = null;

    var hoy = new Date();
    var primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    document.getElementById('fechaDesde').valueAsDate = primerDia;
    document.getElementById('fechaHasta').valueAsDate = hoy;

    // ==================== MODAL EDITAR ====================
    function abrirModalEditar(id, fecha, cantidad, costo, observaciones) {
        movimientoActual = id;
        document.getElementById('editId').value = id;
        document.getElementById('editFecha').value = fecha;
        document.getElementById('editCantidad').value = cantidad;
        document.getElementById('editCosto').value = costo;
        document.getElementById('editObservaciones').value = observaciones || '';
        document.getElementById('editModal').style.display = 'block';
    }

    function cerrarModal() {
        document.getElementById('editModal').style.display = 'none';
        movimientoActual = null;
    }

    function guardarEdicion() {
        var id = document.getElementById('editId').value;
        var fecha = document.getElementById('editFecha').value;
        var cantidad = document.getElementById('editCantidad').value;
        var costo = document.getElementById('editCosto').value;
        var observaciones = document.getElementById('editObservaciones').value;

        fetch(contextPath + '/movimientos?action=actualizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'id=' + id + '&fecha=' + fecha + '&cantidad=' + cantidad + '&costo=' + costo + '&observaciones=' + encodeURIComponent(observaciones)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                cerrarModal();
                generarReporte();
                alert('✅ Movimiento actualizado correctamente');
            } else {
                alert('❌ Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ Error al actualizar');
        });
    }

    // ==================== MODAL ELIMINAR ====================
    function abrirModalEliminar(id) {
        document.getElementById('deleteId').value = id;
        document.getElementById('deleteModal').style.display = 'block';
    }

    function cerrarModalDelete() {
        document.getElementById('deleteModal').style.display = 'none';
    }

    function confirmarEliminar() {
        var id = document.getElementById('deleteId').value;
        
        fetch(contextPath + '/movimientos?action=eliminar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'id=' + id
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                cerrarModalDelete();
                generarReporte();
                alert('✅ Movimiento eliminado correctamente');
            } else {
                alert('❌ Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ Error al eliminar');
        });
    }

    window.onclick = function(event) {
        var modal = document.getElementById('editModal');
        var modalDelete = document.getElementById('deleteModal');
        if (event.target == modal) cerrarModal();
        if (event.target == modalDelete) cerrarModalDelete();
    }

    function generarReporte() {
        var desde = document.getElementById('fechaDesde').value;
        var hasta = document.getElementById('fechaHasta').value;
        var tipo = document.getElementById('filtroTipo').value;

        var url = contextPath + '/reportes?action=movimientos';
        if (desde) url += '&desde=' + desde;
        if (hasta) url += '&hasta=' + hasta;
        if (tipo && tipo !== '') url += '&tipo=' + tipo;

        document.getElementById('historialTable').innerHTML = '<div class="message info">🔄 Cargando datos...</div>';

        fetch(url)
            .then(response => response.json())
            .then(data => {
                if (!data || data.length === 0) {
                    document.getElementById('historialTable').innerHTML = '<div class="message info">📋 No hay movimientos en el período seleccionado</div>';
                    return;
                }

                var html = '<table class="data-table">';
                html += '<thead><tr>';
                html += '<th>FECHA</th>';
                html += '<th>CÓDIGO</th>';
                html += '<th>PRODUCTO</th>';
                html += '<th>TIPO</th>';
                html += '<th>CANTIDAD</th>';
                html += '<th>COSTO (Bs)</th>';
                html += '<th>USUARIO</th>';
                html += '<th>OBSERVACIONES</th>';
                html += '<th>ACCIONES</th>';
                html += '</tr></thead><tbody>';

                for (var i = 0; i < data.length; i++) {
                    var m = data[i];
                    var tipoClase = '';
                    var tipoTexto = m.tipo || '-';
                    if (tipoTexto === 'INGRESO') tipoClase = 'tipo-ingreso';
                    else if (tipoTexto === 'SALIDA') tipoClase = 'tipo-salida';
                    else if (tipoTexto === 'AJUSTE_POSITIVO') tipoClase = 'tipo-ajuste-positivo';
                    else if (tipoTexto === 'AJUSTE_NEGATIVO') tipoClase = 'tipo-ajuste-negativo';

                    html += '<tr>';
                    html += '<td>' + (m.fecha || '-') + '</td>';
                    html += '<td>' + (m.codigo || '-') + '</td>';
                    html += '<td>' + (m.nombre || '-') + '</td>';
                    html += '<td class="' + tipoClase + '">' + tipoTexto + '</td>';
                    html += '<td class="text-center">' + (m.cantidad || 0) + '</td>';
                    html += '<td class="text-center">' + (m.costo ? parseFloat(m.costo).toFixed(2) : '0.00') + '</td>';
                    html += '<td>' + (m.usuario || '-') + '</td>';
                    html += '<td>' + (m.observaciones || '-') + '</td>';
                    html += '<td class="text-center">';
                    html += '<button class="btn-editar" onclick="abrirModalEditar(' + m.id + ', \'' + m.fecha + '\', ' + m.cantidad + ', ' + m.costo + ', \'' + (m.observaciones || '').replace(/'/g, "\\'") + '\')">✏️ Editar</button>';
                    html += '<button class="btn-eliminar" onclick="abrirModalEliminar(' + m.id + ')">🗑️ Eliminar</button>';
                    html += '</td>';
                    html += '</tr>';
                }

                html += '</tbody></table>';
                html += '<div class="total-movimientos">📊 Total de movimientos: <strong>' + data.length + '</strong></div>';
                document.getElementById('historialTable').innerHTML = html;
            })
            .catch(error => {
                console.error('Error:', error);
                document.getElementById('historialTable').innerHTML = '<div class="message error">❌ Error al cargar el reporte</div>';
            });
    }

    function exportarReporte() {
        var desde = document.getElementById('fechaDesde').value;
        var hasta = document.getElementById('fechaHasta').value;
        var tipo = document.getElementById('filtroTipo').value;
        var url = contextPath + '/reportes?action=exportar-csv';
        if (desde) url += '&desde=' + desde;
        if (hasta) url += '&hasta=' + hasta;
        if (tipo && tipo !== '') url += '&tipo=' + tipo;
        window.location.href = url;
    }

    generarReporte();
</script>

</body>
</html>