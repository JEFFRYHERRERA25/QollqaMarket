<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%@ page import="model.Movimiento" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    Movimiento movimientoEditar = (Movimiento) request.getAttribute("movimiento");
    String accion = (String) request.getAttribute("accion");
    boolean esEdicion = "editar".equals(accion) && movimientoEditar != null;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Qollqa Market - Movimientos</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
    <style>
        .message.success { background: #d4edda; color: #155724; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
        .message.error { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    </style>
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="movimientos"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2><%= esEdicion ? "✏️ Editar Movimiento" : "🔄 Registrar Movimiento" %></h2>
                <p><%= esEdicion ? "Modificar los datos del movimiento" : "Ingresos, salidas y ajustes de inventario" %></p>
            </div>
            <div class="content-body">
                <% if (request.getAttribute("mensaje") != null) { %>
                    <div class="message <%= request.getAttribute("tipoMensaje") %>" id="mensajeMovimiento">
                        <%= request.getAttribute("mensaje") %>
                    </div>
                <% } %>
                
                <div class="card">
                    <div class="card-header"><%= esEdicion ? "Editar Movimiento" : "Nuevo Movimiento" %></div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/movimientos" method="POST" id="formMovimiento">
                            <% if (esEdicion) { %>
                                <input type="hidden" name="accion" value="actualizar">
                                <input type="hidden" name="id" value="<%= movimientoEditar.getId() %>">
                            <% } %>
                            
                            <div class="form-grid">

                                <div class="form-group">
                                    <label>🔍 Código del Producto *</label>
                                    <div class="autocomplete-container">
                                        <input type="text" name="codigo" id="codigoMov" 
                                               value="<%= esEdicion ? movimientoEditar.getCodigo() : "" %>" 
                                               placeholder="Ingrese el código del producto" required autocomplete="off"
                                               <%= esEdicion ? "readonly" : "" %>>
                                        <div id="autocompleteDropdown" class="autocomplete-dropdown" style="display: none;"></div>
                                    </div>
                                    <div id="productoInfo" style="margin-top: 10px; font-size: 0.9rem;"></div>
                                </div>

                                <div class="form-group">
                                    <label>📅 Fecha *</label>
                                    <input type="date" name="fecha" id="fechaMov" 
                                           value="<%= esEdicion ? movimientoEditar.getFecha() : "" %>" required>
                                </div>

                                <div class="form-group">
                                    <label>🔄 Tipo de Movimiento *</label>
                                    <select name="tipo" id="tipoMov" required>
                                        <option value="INGRESO" <%= esEdicion && "INGRESO".equals(movimientoEditar.getTipo()) ? "selected" : "" %>>📥 Ingreso</option>
                                        <option value="SALIDA" <%= esEdicion && "SALIDA".equals(movimientoEditar.getTipo()) ? "selected" : "" %>>📤 Salida</option>
                                        <option value="AJUSTE_POSITIVO" <%= esEdicion && "AJUSTE_POSITIVO".equals(movimientoEditar.getTipo()) ? "selected" : "" %>>➕ Ajuste Positivo</option>
                                        <option value="AJUSTE_NEGATIVO" <%= esEdicion && "AJUSTE_NEGATIVO".equals(movimientoEditar.getTipo()) ? "selected" : "" %>>➖ Ajuste Negativo</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label>🔢 Cantidad *</label>
                                    <input type="number" name="cantidad" id="cantMov" 
                                           value="<%= esEdicion ? (int) movimientoEditar.getCantidad() : "" %>" 
                                           step="1" min="1" placeholder="Cantidad" required>
                                </div>

                                <div class="form-group">
                                    <label>💰 Costo del producto *</label>
                                    <input type="number" name="costo" id="costoMov" 
                                           value="<%= esEdicion ? movimientoEditar.getCosto() : "" %>" 
                                           step="0.01" min="0" placeholder="Ingrese el costo del producto">
                                </div>

                                <div class="form-group">
                                    <label>📝 Observaciones</label>
                                    <textarea name="observaciones" id="obsMov" rows="3" placeholder="Observaciones opcionales"><%= esEdicion ? (movimientoEditar.getObservaciones() != null ? movimientoEditar.getObservaciones() : "") : "" %></textarea>
                                </div>

                            </div>

                            <div class="actions">
                                <button type="submit" class="btn btn-success"><%= esEdicion ? "💾 Actualizar Movimiento" : "💾 Guardar Movimiento" %></button>
                                <button type="button" class="btn btn-secondary" id="btnLimpiarMovimiento">🧹 Limpiar</button>
                                <% if (esEdicion) { %>
                                    <a href="${pageContext.request.contextPath}/reportes" class="btn btn-secondary">🔙 Cancelar</a>
                                <% } %>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        //var contextPath = window.location.pathname.split('/')[1] || '';
        var contextPath = '<%= request.getContextPath() %>';
        
        // Establecer fecha actual si es nuevo movimiento
        <% if (!esEdicion) { %>
            document.getElementById('fechaMov').valueAsDate = new Date();
        <% } %>
        
        // Si es edición, mostrar información del producto
        <% if (esEdicion) { %>
            var infoHtml = '<div style="background: #e8f4fd; padding: 10px; border-radius: 8px; border-left: 4px solid #c0392b;">';
            infoHtml += '<strong>✅ Producto:</strong><br>';
            infoHtml += '📝 Nombre: <%= movimientoEditar.getNombre() %><br>';
            infoHtml += '💰 Costo: Bs. <%= movimientoEditar.getCosto() %><br>';
            infoHtml += '</div>';
            document.getElementById('productoInfo').innerHTML = infoHtml;
        <% } %>
        
        var input = document.getElementById('codigoMov');
        var dropdown = document.getElementById('autocompleteDropdown');
        var timeoutId;
        
        <% if (!esEdicion) { %>
        input.addEventListener('input', function() {
            clearTimeout(timeoutId);
            var value = this.value.trim().toUpperCase();
            
            if (value.length < 2) {
                dropdown.style.display = 'none';
                document.getElementById('productoInfo').innerHTML = '';
                return;
            }
            
            timeoutId = setTimeout(function() {
                fetch(contextPath + '/productos?action=listar')
                    .then(function(response) {
                        return response.json();
                    })
                    .then(function(productos) {
                        var filtrados = [];
                        for (var i = 0; i < productos.length; i++) {
                            var p = productos[i];
                            if (p.codigo.toUpperCase().indexOf(value) !== -1 || p.nombre.toLowerCase().indexOf(value.toLowerCase()) !== -1) {
                                filtrados.push(p);
                            }
                            if (filtrados.length >= 10) break;
                        }
                        
                        if (filtrados.length === 0) {
                            dropdown.style.display = 'none';
                            return;
                        }
                        
                        var html = '';
                        for (var i = 0; i < filtrados.length; i++) {
                            var p = filtrados[i];
                            var stockClass = '';
                            var stockText = '';
                            if (p.cantidad === 0) {
                                stockClass = 'color: #e74c3c;';
                                stockText = 'AGOTADO';
                            } else if (p.cantidad <= p.stockMin) {
                                stockClass = 'color: #f39c12;';
                                stockText = 'STOCK BAJO';
                            } else {
                                stockClass = 'color: #27ae60;';
                                stockText = 'DISPONIBLE';
                            }

                            html += '<div class="autocomplete-item" data-codigo="' + p.codigo + '" data-nombre="' + p.nombre + '" data-stock="' + p.cantidad + '" data-unidad="' + p.unidad + '" data-stockmin="' + p.stockMin + '">';
                            html += '<div><strong>📦 ' + p.codigo + '</strong> - ' + p.nombre + '</div>';
                            html += '<div style="font-size: 0.8rem; ' + stockClass + '">Stock: ' + p.cantidad + ' ' + p.unidad + ' | Mín: ' + p.stockMin + ' | ' + stockText + '</div>';
                            html += '</div>';
                        }
                        
                        dropdown.innerHTML = html;
                        dropdown.style.display = 'block';
                        
                        var items = document.querySelectorAll('.autocomplete-item');
                        for (var i = 0; i < items.length; i++) {
                            items[i].addEventListener('click', function() {
                                input.value = this.dataset.codigo;
                                dropdown.style.display = 'none';
                                
                                var stock = parseInt(this.dataset.stock);
                                var unidad = this.dataset.unidad;
                                var stockMin = this.dataset.stockmin;
                                var nombre = this.dataset.nombre;
                                
                                var infoHtml = '<div style="background: #e8f4fd; padding: 10px; border-radius: 8px; border-left: 4px solid #c0392b;">';
                                infoHtml += '<strong>✅ Producto seleccionado:</strong><br>';
                                infoHtml += '📝 Nombre: ' + nombre + '<br>';
                                infoHtml += '📦 Stock actual: <strong>' + stock + ' ' + unidad + '</strong><br>';
                                infoHtml += '⚠️ Stock mínimo: ' + stockMin + ' ' + unidad;
                                if (stock === 0) {
                                    infoHtml += '<br><span style="color: #e74c3c;">❌ ¡PRODUCTO AGOTADO!</span>';
                                } else if (stock <= stockMin) {
                                    infoHtml += '<br><span style="color: #f39c12;">⚠️ ¡STOCK BAJO!</span>';
                                }
                                infoHtml += '</div>';
                                document.getElementById('productoInfo').innerHTML = infoHtml;
                            });
                        }
                    })
                    .catch(function(error) {
                        console.error('Error:', error);
                    });
            }, 300);
        });
        <% } %>
        
        document.addEventListener('click', function(e) {
            if (e.target !== input && dropdown && !dropdown.contains(e.target)) {
                dropdown.style.display = 'none';
            }
        });
        
        document.getElementById('btnLimpiarMovimiento').addEventListener('click', function() {
            if (<%= !esEdicion %>) {
                document.getElementById('formMovimiento').reset();
                document.getElementById('fechaMov').valueAsDate = new Date();
                document.getElementById('codigoMov').focus();
                document.getElementById('productoInfo').innerHTML = '';
                dropdown.style.display = 'none';
            }
        });
        
        setTimeout(function() {
            var msg = document.querySelector('.message');
            if (msg) {
                setTimeout(function() {
                    msg.style.display = 'none';
                }, 5000);
            }
        }, 100);
        
        function actualizarDashboard() {
            if (window.opener && window.opener.document.getElementById('statsGrid')) {
                window.opener.location.reload();
            }
            if (window.parent && window.parent.document.getElementById('statsGrid')) {
                window.parent.location.reload();
            }
            fetch(contextPath + '/reportes?action=dashboard')
                .then(function(response) {
                    return response.json();
                })
                .then(function(data) {
                    console.log('Dashboard actualizado:', data);
                })
                .catch(function(error) {
                    console.error('Error actualizando dashboard:', error);
                });
        }
        
        var formMov = document.getElementById('formMovimiento');
        if (formMov) {
            formMov.addEventListener('submit', function() {
                setTimeout(function() {
                    actualizarDashboard();
                }, 1000);
            });
        }
    </script>
</body>
</html>