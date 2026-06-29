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
    <title>Qollqa Market - Ventas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="ventas"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>🛒 Registro de Ventas</h2>
                <p>Gestión de ventas del sistema</p>
            </div>
            <div class="content-body">
                <div class="card">
                    <div class="card-header">Nueva Venta</div>
                    <div class="card-body">
                        <form id="formVenta">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label>👤 Cliente</label>
                                    <select id="clienteVenta"></select>
                                </div>
                                <div class="form-group">
                                    <label>📅 Fecha</label>
                                    <input type="date" id="fechaVenta" required>
                                </div>
                                <div class="form-group">
                                    <label>💳 Método de Pago</label>
                                    <select id="metodoPago">
                                        <option value="Efectivo">Efectivo</option>
                                        <option value="Yape">Yape</option>
                                        <option value="Tarjeta">Tarjeta</option>
                                    </select>
                                </div>
                            </div>
                            <hr>
                            <div class="form-grid">
                                <div class="form-group">
                                    <label>📦 Producto</label>
                                    <select id="productoVenta"></select>
                                </div>
                                <div class="form-group">
                                    <label>🔢 Cantidad</label>
                                    <input type="number" id="cantidadVenta" min="1">
                                </div>
                                <div class="form-group">
                                    <label>💲 Precio</label>
                                    <input type="number" id="precioVenta" step="0.01" min="0">
                                </div>
                            </div>
                            <div class="actions">
                                <button type="button" class="btn btn-info" id="btnAgregarDetalle">➕ Agregar Producto</button>
                            </div>
                            <div class="table-container" id="detalleVenta"></div>
                            <h3 id="totalVenta">Total: S/ 0.00</h3>
                            <div class="actions">
                                <button type="submit" class="btn btn-success">💾 Registrar Venta</button>
                                <button type="button" class="btn btn-secondary" id="btnLimpiarVenta">🧹 Limpiar</button>
                            </div>
                        </form>
                        <div id="msgVenta"></div>
                    </div>
                </div>
                
                <div class="card">
                    <div class="card-header">Historial de Ventas</div>
                    <div class="card-body">
                        <div class="table-container" id="tablaVentas"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        let detalleVentaTemp = [];
        
        function mostrarMensaje(elementId, mensaje, tipo) {
            const element = document.getElementById(elementId);
            if (element) {
                element.innerHTML = `<div class="message \${tipo}">\${mensaje}</div>`;
                setTimeout(() => { if (element.innerHTML) element.innerHTML = ""; }, 3000);
            }
        }
        
        function cargarSelectores() {
            fetch('${pageContext.request.contextPath}/ventas?action=clientes')
                .then(r => r.json())
                .then(data => {
                    const select = document.getElementById('clienteVenta');
                    select.innerHTML = data.map(c => `<option value="\${c.id}">\${c.nombre}</option>`).join('');
                });
            
            fetch('${pageContext.request.contextPath}/ventas?action=productos')
                .then(r => r.json())
                .then(data => {
                    const select = document.getElementById('productoVenta');
                    select.innerHTML = data.filter(p => p.cantidad > 0).map(p => `<option value="\${p.codigo}" data-stock="\${p.cantidad}">\${p.nombre} (Stock: \${p.cantidad})</option>`).join('');
                    if (select.innerHTML === '') select.innerHTML = '<option value="">⚠️ No hay productos con stock</option>';
                });
            
            document.getElementById('fechaVenta').valueAsDate = new Date();
            actualizarDetalle();
        }
        
        function actualizarDetalle() {
            const container = document.getElementById('detalleVenta');
            let total = 0;
            if (detalleVentaTemp.length === 0) {
                container.innerHTML = '<div class="message info">🛒 No hay productos agregados</div>';
                document.getElementById('totalVenta').innerHTML = '<strong>Total: S/ 0.00</strong>';
                return;
            }
            container.innerHTML = `
                <table class="data-table">
                    <thead><tr><th>Producto</th><th>Cantidad</th><th>Precio Unit.</th><th>Subtotal</th><th></th></tr></thead>
                    <tbody>
                        \${detalleVentaTemp.map((d, idx) => { total += parseFloat(d.subtotal); return `<tr><td>\${d.nombre}</td><td>\${d.cantidad}</td><td>S/ \${d.precio}</td><td>S/ \${d.subtotal}</td><td><button class="btn-small btn-danger" onclick="eliminarDetalle(\${idx})">❌</button></td>`; }).join('')}
                    </tbody>
                </table>
            `;
            document.getElementById('totalVenta').innerHTML = `<strong>Total: S/ \${total.toFixed(2)}</strong>`;
        }
        
        window.eliminarDetalle = (idx) => {
            detalleVentaTemp.splice(idx, 1);
            actualizarDetalle();
        };
        
        function agregarDetalle() {
            const select = document.getElementById('productoVenta');
            const codigo = select.value;
            const nombre = select.options[select.selectedIndex]?.text.split(' (')[0];
            const cantidad = parseFloat(document.getElementById('cantidadVenta').value);
            const precio = parseFloat(document.getElementById('precioVenta').value);
            
            if (!codigo || isNaN(cantidad) || cantidad <= 0) { mostrarMensaje('msgVenta', '❌ Cantidad inválida', 'error'); return; }
            if (isNaN(precio) || precio <= 0) { mostrarMensaje('msgVenta', '❌ Precio inválido', 'error'); return; }
            
            detalleVentaTemp.push({ codigo, nombre, cantidad, precio: precio.toFixed(2), subtotal: (cantidad * precio).toFixed(2) });
            actualizarDetalle();
            document.getElementById('cantidadVenta').value = '';
            document.getElementById('precioVenta').value = '';
            mostrarMensaje('msgVenta', '✅ Producto agregado', 'success');
        }
        
        async function finalizarVenta(e) {
            e.preventDefault();
            if (detalleVentaTemp.length === 0) { mostrarMensaje('msgVenta', '❌ Agregue productos', 'error'); return; }
            
            const clienteId = document.getElementById('clienteVenta').value;
            const cliente = document.getElementById('clienteVenta').options[document.getElementById('clienteVenta').selectedIndex]?.text;
            const metodo = document.getElementById('metodoPago').value;
            const fecha = document.getElementById('fechaVenta').value;
            const total = detalleVentaTemp.reduce((s, d) => s + parseFloat(d.subtotal), 0);
            
            const venta = { cliente, clienteId, metodo, fecha, total: total.toFixed(2), items: detalleVentaTemp };
            
            const response = await fetch('${pageContext.request.contextPath}/ventas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(venta)
            });
            const data = await response.json();
            if (data.success) {
                mostrarMensaje('msgVenta', `✅ Venta registrada. Total: S/ \${total.toFixed(2)}`, 'success');
                detalleVentaTemp = [];
                actualizarDetalle();
                cargarSelectores();
                cargarHistorialVentas();
                document.getElementById('formVenta').reset();
                document.getElementById('fechaVenta').valueAsDate = new Date();
            } else {
                mostrarMensaje('msgVenta', `❌ \${data.error}`, 'error');
            }
        }
        
        function limpiarVenta() {
            if (detalleVentaTemp.length > 0 && !confirm('¿Limpiar venta actual?')) return;
            detalleVentaTemp = [];
            actualizarDetalle();
            document.getElementById('formVenta').reset();
            document.getElementById('fechaVenta').valueAsDate = new Date();
            cargarSelectores();
            mostrarMensaje('msgVenta', '🧹 Venta limpiada', 'info');
        }
        
        function cargarHistorialVentas() {
            fetch('${pageContext.request.contextPath}/ventas?action=listar')
                .then(r => r.json())
                .then(data => {
                    const container = document.getElementById('tablaVentas');
                    if (data.length === 0) { container.innerHTML = '<div class="message info">🛒 No hay ventas registradas</div>'; return; }
                    container.innerHTML = `
                        <table class="data-table">
                            <thead><tr><th>Fecha</th><th>Cliente</th><th>Método</th><th>Total</th><th>Items</th></tr></thead>
                            <tbody>
                                \${data.slice().reverse().slice(0, 20).map(v => `<tr><td>\${v.fecha}</td><td>\${v.cliente}</td><td>\${v.metodo}</td><td><strong>S/ \${v.total}</strong></td><td>\${v.items?.length || 0} productos</td>`).join('')}
                            </tbody>
                        </table>
                    `;
                });
        }
        
        document.getElementById('btnAgregarDetalle').onclick = agregarDetalle;
        document.getElementById('btnLimpiarVenta').onclick = limpiarVenta;
        document.getElementById('formVenta').addEventListener('submit', finalizarVenta);
        
        cargarSelectores();
        cargarHistorialVentas();
    </script>
</body>
</html>