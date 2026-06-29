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
    <title>Qollqa Market - Clientes</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="clientes"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>👤 Gestión de Clientes</h2>
                <p>Registrar y administrar clientes</p>
            </div>
            <div class="content-body">
                <% if (request.getAttribute("mensaje") != null) { %>
                    <div class="message <%= request.getAttribute("tipoMensaje") %>">
                        <%= request.getAttribute("mensaje") %>
                    </div>
                <% } %>
                
                <div class="card">
                    <div class="card-header">Registrar Cliente</div>
                    <div class="card-body">
                        <form id="formCliente">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label>👤 Nombre *</label>
                                    <input type="text" id="nombreCliente" required>
                                </div>
                                <div class="form-group">
                                    <label>📄 DNI</label>
                                    <input type="text" id="dniCliente">
                                </div>
                                <div class="form-group">
                                    <label>📞 Teléfono</label>
                                    <input type="text" id="telefonoCliente">
                                </div>
                                <div class="form-group">
                                    <label>📍 Dirección</label>
                                    <input type="text" id="direccionCliente">
                                </div>
                            </div>
                            <div class="actions">
                                <button type="submit" class="btn btn-success">💾 Registrar Cliente</button>
                                <button type="button" class="btn btn-secondary" id="btnLimpiarCliente">🧹 Limpiar</button>
                            </div>
                        </form>
                        <div id="msgCliente"></div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">Lista de Clientes</div>
                    <div class="card-body">
                        <div class="table-container" id="tablaClientes"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== MODAL EDITAR CLIENTE ===== -->
    <div id="modalEditarCliente" class="modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999;">
        <div class="modal-content" style="background:#fff; margin:10% auto; padding:20px; border-radius:8px; max-width:450px; position:relative;">
            <div class="modal-header" style="border-bottom:2px solid #c0392b; padding-bottom:10px; margin-bottom:15px;">
                <h3 id="modalTituloCliente" style="margin:0; color:#c0392b;">Editar Cliente</h3>
                <span onclick="cerrarModalCliente()" style="position:absolute; top:15px; right:20px; font-size:22px; cursor:pointer; color:#888;">&times;</span>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editIdCliente">
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Nombre:</label>
                    <input type="text" id="editNombreCliente" placeholder="Nombre completo" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                </div>
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">DNI:</label>
                    <input type="text" id="editDniCliente" placeholder="DNI" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                </div>
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Teléfono:</label>
                    <input type="text" id="editTelefonoCliente" placeholder="Teléfono" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                </div>
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Dirección:</label>
                    <input type="text" id="editDireccionCliente" placeholder="Dirección" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                </div>
            </div>
            <div class="modal-footer" style="display:flex; gap:10px; justify-content:flex-end; margin-top:15px;">
                <button type="button" onclick="cerrarModalCliente()" style="padding:8px 16px; background:#6c757d; color:#fff; border:none; border-radius:4px; cursor:pointer;">Cancelar</button>
                <button type="button" onclick="guardarEdicionCliente()" style="padding:8px 16px; background:#27ae60; color:#fff; border:none; border-radius:4px; cursor:pointer;">Guardar Cambios</button>
            </div>
        </div>
    </div>
    <!-- ===== FIN MODAL EDITAR CLIENTE ===== -->

    <script>
        function mostrarMensaje(elementId, mensaje, tipo) {
            const element = document.getElementById(elementId);
            if (element) {
                element.innerHTML = `<div class="message \${tipo}">\${mensaje}</div>`;
                setTimeout(() => { if (element.innerHTML) element.innerHTML = ""; }, 3000);
            }
        }
        
        // ===== FUNCIONES DEL MODAL =====
        function abrirModalEditarCliente(id, nombre, dni, telefono, direccion) {
            document.getElementById('editIdCliente').value = id;
            document.getElementById('editNombreCliente').value = nombre;
            document.getElementById('editDniCliente').value = dni || '';
            document.getElementById('editTelefonoCliente').value = telefono || '';
            document.getElementById('editDireccionCliente').value = direccion || '';
            document.getElementById('modalTituloCliente').textContent = 'Editar Cliente: ' + nombre;
            document.getElementById('modalEditarCliente').style.display = 'block';
        }

        function cerrarModalCliente() {
            document.getElementById('modalEditarCliente').style.display = 'none';
        }

        function guardarEdicionCliente() {
            var id = document.getElementById('editIdCliente').value;
            var nombre = document.getElementById('editNombreCliente').value.trim();
            var dni = document.getElementById('editDniCliente').value.trim();
            var telefono = document.getElementById('editTelefonoCliente').value.trim();
            var direccion = document.getElementById('editDireccionCliente').value.trim();

            if (!nombre) {
                mostrarMensaje('msgCliente', 'El nombre es obligatorio', 'error');
                return;
            }

            var formData = new URLSearchParams();
            formData.append('action', 'editar');
            formData.append('id', id);
            formData.append('nombre', nombre);
            formData.append('dni', dni);
            formData.append('telefono', telefono);
            formData.append('direccion', direccion);

            fetch('${pageContext.request.contextPath}/clientes', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: formData
            })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                cerrarModalCliente();
                if (data.success) {
                    mostrarMensaje('msgCliente', 'Cliente actualizado correctamente', 'success');
                    cargarClientes();
                } else {
                    mostrarMensaje('msgCliente', 'Error: ' + (data.error || data.message), 'error');
                }
            })
            .catch(function() {
                cerrarModalCliente();
                mostrarMensaje('msgCliente', 'Error al conectar con el servidor', 'error');
            });
        }

        window.onclick = function(event) {
            var modal = document.getElementById('modalEditarCliente');
            if (event.target === modal) cerrarModalCliente();
        };
        // ===== FIN FUNCIONES MODAL =====

        // ===== ELIMINAR CLIENTE =====
        window.eliminarCliente = function(id, nombre) {
            if (confirm('¿Eliminar cliente "' + nombre + '"?')) {
                var formData = new URLSearchParams();
                formData.append('action', 'eliminar');
                formData.append('id', id);
                fetch('${pageContext.request.contextPath}/clientes', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: formData
                })
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.success) {
                        mostrarMensaje('msgCliente', 'Cliente eliminado', 'success');
                        cargarClientes();
                    } else {
                        mostrarMensaje('msgCliente', 'Error al eliminar: ' + (data.error || data.message), 'error');
                    }
                })
                .catch(function() {
                    mostrarMensaje('msgCliente', 'Error al conectar con el servidor', 'error');
                });
            }
        };
        
        function cargarClientes() {
            fetch('${pageContext.request.contextPath}/clientes?action=listar')
                .then(response => response.json())
                .then(data => {
                    const container = document.getElementById('tablaClientes');
                    if (data.length === 0) {
                        container.innerHTML = '<div class="message info">👤 No hay clientes registrados</div>';
                        return;
                    }
                    
                    // Escapar datos para evitar problemas con comillas
                    const getEscaped = (str) => (str || '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
                    
                    let filas = '';
                    for (let i = 0; i < data.length; i++) {
                        const c = data[i];
                        const nombreEsc = getEscaped(c.nombre);
                        
                        filas += `<tr>
                            <td>\${c.nombre}</td>
                            <td>\${c.dni || '-'}</td>
                            <td>\${c.telefono || '-'}</td>
                            <td>\${c.direccion || '-'}</td>
                            <td>
                                <button class="btn-edit" onclick="abrirModalEditarCliente('\${c.id}', '\${nombreEsc}', '\${c.dni || ''}', '\${c.telefono || ''}', '\${c.direccion || ''}')">Editar</button>
                                <button class="btn-small btn-danger" onclick="eliminarCliente('\${c.id}', '\${nombreEsc}')">Eliminar</button>
                            </td>
                        </tr>`;
                    }
                    
                    container.innerHTML = `
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Nombre</th>
                                    <th>DNI</th>
                                    <th>Teléfono</th>
                                    <th>Dirección</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                \${filas}
                            </tbody>
                        </table>
                    `;
                })
                .catch(error => {
                    console.error('Error al cargar clientes:', error);
                    document.getElementById('tablaClientes').innerHTML = '<p class="error">Error al cargar clientes</p>';
                });
        }
        
        document.getElementById('formCliente').addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new URLSearchParams();
            formData.append('action', 'registrar');
            formData.append('nombre', document.getElementById('nombreCliente').value);
            formData.append('dni', document.getElementById('dniCliente').value);
            formData.append('telefono', document.getElementById('telefonoCliente').value);
            formData.append('direccion', document.getElementById('direccionCliente').value);
            
            const response = await fetch('${pageContext.request.contextPath}/clientes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            });
            const text = await response.text();
            if (text.includes('success') || text.includes('exitosamente')) {
                mostrarMensaje('msgCliente', '✅ Cliente registrado', 'success');
                document.getElementById('formCliente').reset();
                cargarClientes();
            } else {
                mostrarMensaje('msgCliente', '❌ Error al registrar', 'error');
            }
        });
        
        document.getElementById('btnLimpiarCliente').onclick = () => {
            document.getElementById('formCliente').reset();
            mostrarMensaje('msgCliente', '🧹 Formulario limpiado', 'info');
        };
        
        cargarClientes();
    </script>
</body>
</html>