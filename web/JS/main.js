// ==================== main.js - Qollqa Market ====================
// Este archivo contiene las funciones JavaScript para el frontend

let detalleVentaTemp = [];
let contextPath = window.location.pathname.split('/')[1] || '';

// ==================== FUNCIONES DE NOTIFICACIÓN ====================
function mostrarNotificacion(mensaje, tipo = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${tipo}`;
    
    const iconos = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };
    
    toast.innerHTML = `
        <div class="toast-content">
            <span class="toast-icon">${iconos[tipo]}</span>
            <span class="toast-message">${mensaje}</span>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ==================== FUNCIONES DE INVENTARIO ====================
function cargarInventario() {
    fetch('/QollqaMarket/productos?action=listar')
        .then(response => response.json())
        .then(productos => {
            const container = document.getElementById('stockTable');
            if (!container) return;
            
            if (productos.length === 0) {
                container.innerHTML = '<div class="message info">📦 No hay productos registrados</div>';
                return;
            }
            
            let html = `
                <table class="data-table">
                    <thead>
                        <tr><th>Código</th><th>Nombre</th><th>Grupo</th><th>Stock</th><th>Unidad</th><th>Stock Mínimo</th><th>Estado</th></tr>
                    </thead>
                    <tbody>
            `;
            
            productos.forEach(p => {
                let estado = p.cantidad <= p.stockMin ? (p.cantidad === 0 ? 'AGOTADO' : 'BAJO') : 'NORMAL';
                let clase = p.cantidad === 0 ? 'status-zero' : (p.cantidad <= p.stockMin ? 'status-low' : 'status-normal');
                html += `
                    <tr>
                        <td>${p.codigo}</td>
                        <td>${p.nombre}</td>
                        <td>${p.grupo}</td>
                        <td><strong>${p.cantidad}</strong></td>
                        <td>${p.unidad}</td>
                        <td>${p.stockMin}</td>
                        <td><span class="${clase}">${estado}</span></td>
                    </tr>
                `;
            });
            
            html += `</tbody></table>`;
            container.innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error al cargar inventario', 'error');
        });
}

function exportarStockCSV() {
    fetch('/QollqaMarket/productos?action=listar')
        .then(response => response.json())
        .then(productos => {
            if (productos.length === 0) {
                mostrarNotificacion('No hay productos para exportar', 'warning');
                return;
            }
            
            const headers = ['Código,Nombre,Grupo,Stock,Unidad,Stock Mínimo,Estado'];
            const rows = productos.map(p => `${p.codigo},${p.nombre},${p.grupo},${p.cantidad},${p.unidad},${p.stockMin},${p.cantidad <= p.stockMin ? (p.cantidad === 0 ? 'AGOTADO' : 'BAJO') : 'NORMAL'}`);
            const csv = headers.concat(rows).join('\n');
            
            const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            const url = URL.createObjectURL(blob);
            link.href = url;
            link.setAttribute('download', `inventario_${new Date().toISOString().split('T')[0]}.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
            
            mostrarNotificacion('📥 Inventario exportado exitosamente', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error al exportar', 'error');
        });
}

function filtrarSoloAlertas() {
    fetch('/QollqaMarket/productos?action=alertas')
        .then(response => response.json())
        .then(alertas => {
            const container = document.getElementById('stockTable');
            
            if (alertas.length === 0) {
                container.innerHTML = '<div class="message success">✅ No hay productos con alertas de stock</div>';
                return;
            }
            
            let html = `
                <table class="data-table">
                    <thead>
                        <tr><th>Código</th><th>Nombre</th><th>Stock</th><th>Stock Mínimo</th><th>Unidad</th><th>Estado</th></tr>
                    </thead>
                    <tbody>
            `;
            
            alertas.forEach(p => {
                html += `
                    <tr>
                        <td>${p.codigo}</td>
                        <td>${p.nombre}</td>
                        <td><strong>${p.cantidad}</strong></td>
                        <td>${p.stockMin}</td>
                        <td>${p.unidad}</td>
                        <td><span class="status-low">⚠️ ALERTA</span></td>
                    </tr>
                `;
            });
            
            html += `</tbody></table>
            <div class="actions">
                <button class="btn btn-primary" onclick="cargarInventario()">📋 Ver Todos</button>
            </div>`;
            
            container.innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error al cargar alertas', 'error');
        });
}

// ==================== FUNCIONES DE VENTAS ====================
function cargarSelectoresVenta() {
    // Cargar clientes
    fetch('/QollqaMarket/ventas?action=clientes')
        .then(response => response.json())
        .then(clientes => {
            const select = document.getElementById('clienteVenta');
            if (select) {
                select.innerHTML = '<option value="">Seleccione un cliente</option>';
                clientes.forEach(c => {
                    select.innerHTML += `<option value="${c.id}">${c.nombre}</option>`;
                });
            }
        })
        .catch(error => console.error('Error:', error));
    
    // Cargar productos
    fetch('/QollqaMarket/ventas?action=productos')
        .then(response => response.json())
        .then(productos => {
            const select = document.getElementById('productoVenta');
            if (select) {
                select.innerHTML = '<option value="">Seleccione un producto</option>';
                productos.filter(p => p.cantidad > 0).forEach(p => {
                    select.innerHTML += `<option value="${p.codigo}" data-precio="${p.precio || 0}">${p.nombre} (Stock: ${p.cantidad})</option>`;
                });
            }
        })
        .catch(error => console.error('Error:', error));
    
    // Fecha actual
    const fechaInput = document.getElementById('fechaVenta');
    if (fechaInput) {
        fechaInput.valueAsDate = new Date();
    }
    
    actualizarVistaDetalle();
}

function agregarDetalle() {
    const selectProducto = document.getElementById('productoVenta');
    const codigo = selectProducto.value;
    const nombre = selectProducto.options[selectProducto.selectedIndex]?.text.split(' (')[0];
    const cantidad = parseFloat(document.getElementById('cantidadVenta')?.value);
    const precio = parseFloat(document.getElementById('precioVenta')?.value);
    
    if (!codigo) {
        mostrarNotificacion('❌ Seleccione un producto', 'warning');
        return;
    }
    
    if (isNaN(cantidad) || cantidad <= 0) {
        mostrarNotificacion('❌ Cantidad inválida', 'warning');
        return;
    }
    
    if (isNaN(precio) || precio <= 0) {
        mostrarNotificacion('❌ Precio inválido', 'warning');
        return;
    }
    
    // Verificar stock
    fetch(`/QollqaMarket/productos?action=buscar&codigo=${codigo}`)
        .then(response => response.json())
        .then(producto => {
            if (cantidad > producto.cantidad) {
                mostrarNotificacion(`Stock insuficiente. Disponible: ${producto.cantidad}`, 'error');
                return;
            }
            
            detalleVentaTemp.push({
                codigo: codigo,
                nombre: nombre,
                cantidad: cantidad,
                precio: precio.toFixed(2),
                subtotal: (cantidad * precio).toFixed(2)
            });
            
            actualizarVistaDetalle();
            document.getElementById('cantidadVenta').value = '';
            document.getElementById('precioVenta').value = '';
            mostrarNotificacion('✅ Producto agregado', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error al verificar stock', 'error');
        });
}

function actualizarVistaDetalle() {
    const container = document.getElementById('detalleVenta');
    let total = 0;
    
    if (!container) return;
    
    if (detalleVentaTemp.length === 0) {
        container.innerHTML = '<div class="message info">🛒 No hay productos agregados</div>';
        const totalSpan = document.getElementById('totalVenta');
        if (totalSpan) totalSpan.innerHTML = '<strong>Total: S/ 0.00</strong>';
        return;
    }
    
    let html = `
        <table class="data-table">
            <thead>
                <tr><th>Producto</th><th>Cantidad</th><th>Precio Unit.</th><th>Subtotal</th><th></th></tr>
            </thead>
            <tbody>
    `;
    
    detalleVentaTemp.forEach((d, idx) => {
        total += parseFloat(d.subtotal);
        html += `
            <tr>
                <td>${d.nombre}</td>
                <td>${d.cantidad}</td>
                <td>S/ ${d.precio}</td>
                <td>S/ ${d.subtotal}</td>
                <td><button class="btn-small btn-danger" onclick="eliminarDetalle(${idx})">❌</button></td>
            </tr>
        `;
    });
    
    html += `</tbody></table>`;
    container.innerHTML = html;
    
    const totalSpan = document.getElementById('totalVenta');
    if (totalSpan) totalSpan.innerHTML = `<strong>Total: S/ ${total.toFixed(2)}</strong>`;
}

function eliminarDetalle(index) {
    detalleVentaTemp.splice(index, 1);
    actualizarVistaDetalle();
    mostrarNotificacion('Producto eliminado', 'info');
}

function finalizarVenta(e) {
    e.preventDefault();
    
    if (detalleVentaTemp.length === 0) {
        mostrarNotificacion('❌ Agregue al menos un producto', 'warning');
        return;
    }
    
    const clienteId = document.getElementById('clienteVenta')?.value;
    const clienteSelect = document.getElementById('clienteVenta');
    const cliente = clienteSelect?.options[clienteSelect.selectedIndex]?.text;
    const metodo = document.getElementById('metodoPago')?.value;
    const fecha = document.getElementById('fechaVenta')?.value;
    const total = detalleVentaTemp.reduce((sum, d) => sum + parseFloat(d.subtotal), 0);
    
    const venta = {
        cliente: cliente || 'Público General',
        clienteId: clienteId || 'C001',
        metodo: metodo,
        fecha: fecha,
        total: total.toFixed(2),
        items: detalleVentaTemp
    };
    
    fetch('/QollqaMarket/ventas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(venta)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            mostrarNotificacion(`✅ Venta registrada. Total: S/ ${total.toFixed(2)}`, 'success');
            detalleVentaTemp = [];
            actualizarVistaDetalle();
            cargarSelectoresVenta();
            cargarHistorialVentas();
            document.getElementById('formVenta')?.reset();
            if (document.getElementById('fechaVenta')) {
                document.getElementById('fechaVenta').valueAsDate = new Date();
            }
        } else {
            mostrarNotificacion(`❌ ${data.error}`, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarNotificacion('Error al registrar venta', 'error');
    });
}

function limpiarFormVenta() {
    if (detalleVentaTemp.length > 0) {
        if (confirm('¿Está seguro de limpiar la venta actual?')) {
            detalleVentaTemp = [];
            actualizarVistaDetalle();
            document.getElementById('formVenta')?.reset();
            if (document.getElementById('fechaVenta')) {
                document.getElementById('fechaVenta').valueAsDate = new Date();
            }
            cargarSelectoresVenta();
            mostrarNotificacion('🧹 Venta limpiada', 'info');
        }
    } else {
        document.getElementById('formVenta')?.reset();
        if (document.getElementById('fechaVenta')) {
            document.getElementById('fechaVenta').valueAsDate = new Date();
        }
        mostrarNotificacion('🧹 Formulario limpiado', 'info');
    }
}

function cargarHistorialVentas() {
    fetch('/QollqaMarket/ventas?action=listar')
        .then(response => response.json())
        .then(ventas => {
            const container = document.getElementById('tablaVentas');
            if (!container) return;
            
            if (ventas.length === 0) {
                container.innerHTML = '<div class="message info">🛒 No hay ventas registradas</div>';
                return;
            }
            
            let html = `
                <table class="data-table">
                    <thead>
                        <tr><th>Fecha</th><th>Cliente</th><th>Método</th><th>Total</th><th>Items</th></tr>
                    </thead>
                    <tbody>
            `;
            
            ventas.slice().reverse().slice(0, 20).forEach(v => {
                html += `
                    <tr>
                        <td>${v.fecha}</td>
                        <td>${v.cliente}</td>
                        <td>${v.metodo}</td>
                        <td><strong>S/ ${v.total}</strong></td>
                        <td>${v.items?.length || 0} productos</td>
                    </tr>
                `;
            });
            
            html += `</tbody></table>`;
            if (ventas.length > 20) {
                html += '<div class="message info">📋 Mostrando últimas 20 ventas</div>';
            }
            
            container.innerHTML = html;
        })
        .catch(error => console.error('Error:', error));
}

// ==================== FUNCIONES DE CLIENTES ====================
function cargarClientes() {
    fetch('/QollqaMarket/clientes?action=listar')
        .then(response => response.json())
        .then(clientes => {
            const container = document.getElementById('tablaClientes');
            if (!container) return;
            
            if (clientes.length === 0) {
                container.innerHTML = '<div class="message info">👤 No hay clientes registrados</div>';
                return;
            }
            
            let html = `
                <table class="data-table">
                    <thead>
                        <tr><th>Nombre</th><th>DNI</th><th>Teléfono</th><th>Dirección</th></tr>
                    </thead>
                    <tbody>
            `;
            
            clientes.forEach(c => {
                html += `
                    <tr>
                        <td>${c.nombre}</td>
                        <td>${c.dni || '-'}</td>
                        <td>${c.telefono || '-'}</td>
                        <td>${c.direccion || '-'}</td>
                    </tr>
                `;
            });
            
            html += `</tbody></table>`;
            container.innerHTML = html;
        })
        .catch(error => console.error('Error:', error));
}

// ==================== FUNCIONES DE BÚSQUEDA ====================
function buscarProductos() {
    const texto = document.getElementById('buscarTexto')?.value.trim();
    
    if (!texto) {
        mostrarNotificacion('🔍 Ingrese un término de búsqueda', 'info');
        return;
    }
    
    fetch(`/QollqaMarket/productos?action=buscar&texto=${encodeURIComponent(texto)}`)
        .then(response => response.json())
        .then(resultados => {
            const container = document.getElementById('resultadosBusqueda');
            if (!container) return;
            
            if (resultados.length === 0) {
                container.innerHTML = '<div class="message info">🔍 No se encontraron productos</div>';
                return;
            }
            
            let html = `
                <table class="data-table">
                    <thead>
                        <tr><th>Código</th><th>Nombre</th><th>Grupo</th><th>Stock</th><th>Unidad</th><th>Stock Mínimo</th></tr>
                    </thead>
                    <tbody>
            `;
            
            resultados.forEach(p => {
                html += `
                    <tr>
                        <td>${p.codigo}</td>
                        <td>${p.nombre}</td>
                        <td>${p.grupo}</td>
                        <td><strong>${p.cantidad}</strong></td>
                        <td>${p.unidad}</td>
                        <td>${p.stockMin}</td>
                    </tr>
                `;
            });
            
            html += `</tbody></table>`;
            container.innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarNotificacion('Error en la búsqueda', 'error');
        });
}

function limpiarBusqueda() {
    const input = document.getElementById('buscarTexto');
    const container = document.getElementById('resultadosBusqueda');
    
    if (input) input.value = '';
    if (container) container.innerHTML = '<div class="message info">🔍 Los resultados aparecerán aquí</div>';
    mostrarNotificacion('🧹 Búsqueda limpiada', 'info');
}

// ==================== AUTOCOMPLETADO ====================
function setupAutocomplete() {
    const input = document.getElementById('codigoMov');
    const dropdown = document.getElementById('autocompleteDropdown');
    
    if (!input || !dropdown) return;
    
    let timeoutId;
    
    input.addEventListener('input', function() {
        clearTimeout(timeoutId);
        const value = this.value.trim().toUpperCase();
        
        if (value.length < 2) {
            dropdown.style.display = 'none';
            return;
        }
        
        timeoutId = setTimeout(() => {
            fetch(`/QollqaMarket/productos?action=buscar&texto=${encodeURIComponent(value)}`)
                .then(response => response.json())
                .then(productos => {
                    if (productos.length === 0) {
                        dropdown.innerHTML = '<div class="autocomplete-item">❌ No se encontraron productos</div>';
                        dropdown.style.display = 'block';
                        return;
                    }
                    
                    dropdown.innerHTML = productos.slice(0, 10).map(p => `
                        <div class="autocomplete-item" data-codigo="${p.codigo}">
                            <div><strong>📦 ${p.codigo}</strong> - ${p.nombre}</div>
                            <small>Stock: ${p.cantidad} ${p.unidad} | Mín: ${p.stockMin}</small>
                        </div>
                    `).join('');
                    
                    dropdown.style.display = 'block';
                    
                    document.querySelectorAll('.autocomplete-item').forEach(item => {
                        item.onclick = () => {
                            input.value = item.dataset.codigo;
                            dropdown.style.display = 'none';
                        };
                    });
                })
                .catch(error => console.error('Error:', error));
        }, 300);
    });
    
    document.addEventListener('click', (e) => {
        if (e.target !== input && !dropdown.contains(e.target)) {
            dropdown.style.display = 'none';
        }
    });
}

// ==================== INICIALIZACIÓN ====================
document.addEventListener('DOMContentLoaded', () => {
    // Agregar estilos de notificaciones
    const style = document.createElement('style');
    style.textContent = `
        .toast {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            animation: slideIn 0.3s ease;
            max-width: 350px;
        }
        .toast-content {
            background: white;
            border-radius: 10px;
            padding: 15px 20px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.2);
            display: flex;
            align-items: center;
            gap: 12px;
            border-left: 4px solid;
        }
        .toast-success .toast-content { border-left-color: #27ae60; }
        .toast-error .toast-content { border-left-color: #e74c3c; }
        .toast-warning .toast-content { border-left-color: #f39c12; }
        .toast-info .toast-content { border-left-color: #3498db; }
        .toast-icon { font-size: 1.5rem; }
        .toast-message { flex: 1; font-size: 0.9rem; color: #2c3e50; }
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOut {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
    `;
    document.head.appendChild(style);
    
    // Inicializar según la página actual
    const path = window.location.pathname;
    
    if (path.includes('inventario')) {
        const btnActualizar = document.getElementById('btnActualizarStock');
        const btnExportar = document.getElementById('btnExportarStock');
        const btnAlertas = document.getElementById('btnSoloAlertas');
        
        if (btnActualizar) btnActualizar.onclick = cargarInventario;
        if (btnExportar) btnExportar.onclick = exportarStockCSV;
        if (btnAlertas) btnAlertas.onclick = filtrarSoloAlertas;
        
        cargarInventario();
    }
    
    if (path.includes('ventas')) {
        const btnAgregar = document.getElementById('btnAgregarDetalle');
        const btnLimpiar = document.getElementById('btnLimpiarVenta');
        const formVenta = document.getElementById('formVenta');
        
        if (btnAgregar) btnAgregar.onclick = agregarDetalle;
        if (btnLimpiar) btnLimpiar.onclick = limpiarFormVenta;
        if (formVenta) formVenta.onsubmit = finalizarVenta;
        
        cargarSelectoresVenta();
        cargarHistorialVentas();
    }
    
    if (path.includes('clientes')) {
        const formCliente = document.getElementById('formCliente');
        const btnLimpiar = document.getElementById('btnLimpiarCliente');
        
        if (formCliente) {
            formCliente.onsubmit = async (e) => {
                e.preventDefault();
                const formData = new URLSearchParams();
                formData.append('nombre', document.getElementById('nombreCliente')?.value);
                formData.append('dni', document.getElementById('dniCliente')?.value);
                formData.append('telefono', document.getElementById('telefonoCliente')?.value);
                formData.append('direccion', document.getElementById('direccionCliente')?.value);
                
                const response = await fetch('/QollqaMarket/clientes', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });
                const text = await response.text();
                if (text.includes('success')) {
                    mostrarNotificacion('✅ Cliente registrado', 'success');
                    formCliente.reset();
                    cargarClientes();
                } else {
                    mostrarNotificacion('❌ Error al registrar', 'error');
                }
            };
        }
        
        if (btnLimpiar) {
            btnLimpiar.onclick = () => {
                formCliente?.reset();
                mostrarNotificacion('🧹 Formulario limpiado', 'info');
            };
        }
        
        cargarClientes();
    }
    
    if (path.includes('movimientos')) {
        const formMovimiento = document.getElementById('formMovimiento');
        const btnLimpiar = document.getElementById('btnLimpiarMovimiento');
        
        if (formMovimiento) {
            formMovimiento.onsubmit = async (e) => {
                e.preventDefault();
                const formData = new URLSearchParams();
                formData.append('codigo', document.getElementById('codigoMov')?.value);
                formData.append('tipo', document.getElementById('tipoMov')?.value);
                formData.append('cantidad', document.getElementById('cantMov')?.value);
                formData.append('fecha', document.getElementById('fechaMov')?.value);
                formData.append('observaciones', document.getElementById('obsMov')?.value);
                
                const response = await fetch('/QollqaMarket/movimientos', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData
                });
                const text = await response.text();
                if (text.includes('success')) {
                    mostrarNotificacion('✅ Movimiento registrado', 'success');
                    formMovimiento.reset();
                    if (document.getElementById('fechaMov')) {
                        document.getElementById('fechaMov').valueAsDate = new Date();
                    }
                } else {
                    mostrarNotificacion('❌ Error al registrar', 'error');
                }
            };
        }
        
        if (btnLimpiar) {
            btnLimpiar.onclick = () => {
                formMovimiento?.reset();
                if (document.getElementById('fechaMov')) {
                    document.getElementById('fechaMov').valueAsDate = new Date();
                }
                const dropdown = document.getElementById('autocompleteDropdown');
                if (dropdown) dropdown.innerHTML = '';
                mostrarNotificacion('🧹 Formulario limpiado', 'info');
            };
        }
        
        if (document.getElementById('fechaMov')) {
            document.getElementById('fechaMov').valueAsDate = new Date();
        }
        
        setupAutocomplete();
    }
    
    if (path.includes('buscar')) {
        const btnBuscar = document.getElementById('btnBuscar');
        const btnLimpiar = document.getElementById('btnLimpiarBusqueda');
        
        if (btnBuscar) btnBuscar.onclick = buscarProductos;
        if (btnLimpiar) btnLimpiar.onclick = limpiarBusqueda;
        
        // Buscar al presionar Enter
        const inputBuscar = document.getElementById('buscarTexto');
        if (inputBuscar) {
            inputBuscar.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') buscarProductos();
            });
        }
    }
    
    if (path.includes('reportes')) {
        const btnGenerar = document.getElementById('btnGenerarReporte');
        const btnExportar = document.getElementById('btnExportarReporte');
        
        if (btnGenerar) {
            btnGenerar.onclick = () => {
                const desde = document.getElementById('fechaDesde')?.value;
                const hasta = document.getElementById('fechaHasta')?.value;
                const tipo = document.getElementById('filtroTipo')?.value;
                
                let url = `/QollqaMarket/reportes?action=movimientos`;
                if (desde) url += `&desde=${desde}`;
                if (hasta) url += `&hasta=${hasta}`;
                if (tipo) url += `&tipo=${tipo}`;
                
                fetch(url)
                    .then(response => response.json())
                    .then(data => {
                        const container = document.getElementById('historialTable');
                        if (data.length === 0) {
                            container.innerHTML = '<div class="message info">📋 No hay movimientos</div>';
                            return;
                        }
                        
                        const getIcono = (t) => {
                            const icons = { 'INGRESO': '📥', 'SALIDA': '📤', 'AJUSTE_POSITIVO': '➕', 'AJUSTE_NEGATIVO': '➖' };
                            return icons[t] || '🔄';
                        };
                        
                        let html = `
                            <table class="data-table">
                                <thead>
                                    <tr><th>Fecha</th><th>Código</th><th>Producto</th><th>Tipo</th><th>Cantidad</th><th>Stock Antes</th><th>Stock Después</th><th>Usuario</th></tr>
                                </thead>
                                <tbody>
                        `;
                        
                        data.forEach(m => {
                            html += `
                                <tr>
                                    <td>${m.fecha}</td>
                                    <td>${m.codigo}</td>
                                    <td>${m.nombre}</td>
                                    <td>${getIcono(m.tipo)} ${m.tipo}</td>
                                    <td>${m.cantidad}</td>
                                    <td>${m.cantidadAnterior}</td>
                                    <td>${m.cantidadNueva}</td>
                                    <td>${m.usuario || '-'}</td>
                                </tr>
                            `;
                        });
                        
                        html += `</tbody></table>`;
                        container.innerHTML = html;
                    })
                    .catch(error => console.error('Error:', error));
            };
        }
        
        if (btnExportar) {
            btnExportar.onclick = () => {
                const desde = document.getElementById('fechaDesde')?.value;
                const hasta = document.getElementById('fechaHasta')?.value;
                const tipo = document.getElementById('filtroTipo')?.value;
                
                let url = `/QollqaMarket/reportes?action=exportar-csv&tipo=movimientos`;
                if (desde) url += `&desde=${desde}`;
                if (hasta) url += `&hasta=${hasta}`;
                if (tipo) url += `&tipoMov=${tipo}`;
                
                window.location.href = url;
                mostrarNotificacion('📥 Descargando reporte...', 'info');
            };
        }
        
        // Inicializar fechas
        const hoy = new Date();
        const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
        if (document.getElementById('fechaDesde')) {
            document.getElementById('fechaDesde').valueAsDate = primerDia;
        }
        if (document.getElementById('fechaHasta')) {
            document.getElementById('fechaHasta').valueAsDate = hoy;
        }
        
        // Generar reporte inicial
        setTimeout(() => {
            if (document.getElementById('btnGenerarReporte')) {
                document.getElementById('btnGenerarReporte').click();
            }
        }, 100);
    }
});

// Funciones globales para onclick en HTML
window.eliminarDetalle = eliminarDetalle;
window.cargarInventario = cargarInventario;
window.exportarStockCSV = exportarStockCSV;
window.filtrarSoloAlertas = filtrarSoloAlertas;
window.cargarClientes = cargarClientes;
window.cargarHistorialVentas = cargarHistorialVentas;
window.buscarProductos = buscarProductos;
window.limpiarBusqueda = limpiarBusqueda;