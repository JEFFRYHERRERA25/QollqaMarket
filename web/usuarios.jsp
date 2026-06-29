<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || !"admin".equals(usuario.getRol())) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Qollqa Market - Usuarios</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
</head>
<body>
    <div class="app-container">
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="usuarios"/>
        </jsp:include>

        <div class="main-content">
            <div class="content-header">
                <h2>&#x1F465; Gestion de Usuarios</h2>
                <p>Administrar usuarios del sistema</p>
            </div>
            <div class="content-body">
                <% if (request.getAttribute("mensaje") != null) { %>
                    <div class="message <%= request.getAttribute("tipoMensaje") %>">
                        <%= request.getAttribute("mensaje") %>
                    </div>
                <% } %>

                <div class="card">
                    <div class="card-header">Registrar Nuevo Usuario</div>
                    <div class="card-body">
                        <form id="formUsuario">
                            <div class="form-grid">
                                <div class="form-group">
                                    <label>&#x1F464; Usuario *</label>
                                    <input type="text" id="nuevoUsuario" required>
                                </div>
                                <div class="form-group">
                                    <label>&#x1F512; Contrasena *</label>
                                    <input type="password" id="nuevaPassword" required>
                                </div>
                                <div class="form-group">
                                    <label>&#x1F454; Rol *</label>
                                    <select id="nuevoRol" required>
                                        <option value="vendedor">Vendedor</option>
                                        <option value="almacenero">Almacenero</option>
                                        <option value="admin">Administrador</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label>&#x1F4DD; Nombre</label>
                                    <input type="text" id="nombreUsuario">
                                </div>
                            </div>
                            <div class="actions">
                                <button type="submit" class="btn btn-success">&#x2795; Registrar Usuario</button>
                            </div>
                        </form>
                        <div id="msgUsuario"></div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">Lista de Usuarios</div>
                    <div class="card-body">
                        <div class="table-container" id="tablaUsuarios"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== MODAL EDITAR ===== -->
    <div id="modalEditar" class="modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999;">
        <div class="modal-content" style="background:#fff; margin:10% auto; padding:20px; border-radius:8px; max-width:450px; position:relative;">
            <div class="modal-header" style="border-bottom:2px solid #c0392b; padding-bottom:10px; margin-bottom:15px;">
                <h3 id="modalTitulo" style="margin:0; color:#c0392b;">Editar Usuario</h3>
                <span onclick="cerrarModal()" style="position:absolute; top:15px; right:20px; font-size:22px; cursor:pointer; color:#888;">&times;</span>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editUsername">
                <!-- NUEVO CAMPO: Usuario editable -->
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Usuario:</label>
                    <input type="text" id="editUsernameField" placeholder="Nombre de usuario" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                </div>
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Nombre:</label>
                    <input type="text" id="editNombre" placeholder="Nombre completo" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                </div>
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Nueva Contrasena:</label>
                    <input type="password" id="editPassword" placeholder="Dejar en blanco para no cambiar" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                    <small style="color:#888;">Minimo 4 caracteres si desea cambiarla</small>
                </div>
                <div class="form-group" style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:4px; font-weight:bold;">Rol:</label>
                    <select id="editRol" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; box-sizing:border-box;">
                        <option value="vendedor">Vendedor</option>
                        <option value="almacenero">Almacenero</option>
                        <option value="admin">Administrador</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer" style="display:flex; gap:10px; justify-content:flex-end; margin-top:15px;">
                <button type="button" onclick="cerrarModal()" style="padding:8px 16px; background:#6c757d; color:#fff; border:none; border-radius:4px; cursor:pointer;">Cancelar</button>
                <button type="button" onclick="guardarEdicion()" style="padding:8px 16px; background:#27ae60; color:#fff; border:none; border-radius:4px; cursor:pointer;">Guardar Cambios</button>
            </div>
        </div>
    </div>
    <!-- ===== FIN MODAL ===== -->

    <script>
        function mostrarMensaje(elementId, mensaje, tipo) {
            var element = document.getElementById(elementId);
            if (element) {
                element.innerHTML = '<div class="message ' + tipo + '">' + mensaje + '</div>';
                setTimeout(function() { if (element.innerHTML) element.innerHTML = ""; }, 3000);
            }
        }

        // ===== FUNCIONES DEL MODAL =====
        function abrirModalEditar(username, nombre, rol) {
            document.getElementById('editUsername').value = username;
            document.getElementById('editUsernameField').value = username;
            document.getElementById('editNombre').value   = nombre;
            document.getElementById('editRol').value      = rol;
            document.getElementById('editPassword').value = '';

            var titulos = {
                admin:      'Editar Administrador',
                vendedor:   'Editar Vendedor',
                almacenero: 'Editar Almacenero'
            };
            document.getElementById('modalTitulo').textContent = titulos[rol] || 'Editar Usuario';

            document.getElementById('modalEditar').style.display = 'block';
        }

        function cerrarModal() {
            document.getElementById('modalEditar').style.display = 'none';
        }

        function guardarEdicion() {
            var oldUsername = document.getElementById('editUsername').value;
            var newUsername = document.getElementById('editUsernameField').value.trim();
            var nombre   = document.getElementById('editNombre').value.trim();
            var password = document.getElementById('editPassword').value;
            var rol      = document.getElementById('editRol').value;

            if (!newUsername) {
                mostrarMensaje('msgUsuario', 'El nombre de usuario es obligatorio', 'error');
                return;
            }
            if (!nombre) {
                mostrarMensaje('msgUsuario', 'El nombre es obligatorio', 'error');
                return;
            }

            var formData = new URLSearchParams();
            formData.append('action',      'editar');
            formData.append('oldUsername', oldUsername);
            formData.append('newUsername', newUsername);
            formData.append('nombre',      nombre);
            formData.append('password',    password);
            formData.append('rol',         rol);

            fetch('${pageContext.request.contextPath}/usuarios', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: formData
            })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                cerrarModal();
                if (data.success) {
                    mostrarMensaje('msgUsuario', 'Usuario actualizado correctamente', 'success');
                    cargarUsuarios();
                } else {
                    mostrarMensaje('msgUsuario', 'Error: ' + (data.error || data.message), 'error');
                }
            })
            .catch(function() {
                cerrarModal();
                mostrarMensaje('msgUsuario', 'Error al conectar con el servidor', 'error');
            });
        }

        window.onclick = function(event) {
            var modal = document.getElementById('modalEditar');
            if (event.target === modal) cerrarModal();
        };
        // ===== FIN FUNCIONES MODAL =====

        function cargarUsuarios() {
            fetch('${pageContext.request.contextPath}/usuarios?action=listar')
                .then(function(response) { return response.json(); })
                .then(function(data) {
                    var container = document.getElementById('tablaUsuarios');
                    var getRolLabel = function(r) {
                        if (r === 'admin')      return 'Administrador';
                        if (r === 'vendedor')   return 'Vendedor';
                        if (r === 'almacenero') return 'Almacenero';
                        return r;
                    };

                    var filas = '';
                    for (var i = 0; i < data.length; i++) {
                        var u = data[i];
                        var nombreEsc = (u.nombre || '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
                        var btnEliminar = u.username !== 'admin'
                            ? '<button class="btn-small btn-danger" onclick="eliminarUsuario(\'' + u.username + '\')">Eliminar</button>'
                            : '<span class="text-muted">Principal</span>';

                        filas += '<tr>'
                            + '<td>' + u.username + '</td>'
                            + '<td>' + (u.nombre || '-') + '</td>'
                            + '<td>' + getRolLabel(u.rol) + '</td>'
                            + '<td>'
                            +   '<button class="btn-edit" onclick="abrirModalEditar(\'' + u.username + '\', \'' + nombreEsc + '\', \'' + u.rol + '\')">'
                            +     'Editar'
                            +   '</button> '
                            +   btnEliminar
                            + '</td>'
                            + '</tr>';
                    }

                    container.innerHTML = '<table class="data-table">'
                        + '<thead><tr>'
                        + '<th>USUARIO</th><th>NOMBRE</th><th>ROL</th><th>ACCIONES</th>'
                        + '</tr></thead>'
                        + '<tbody>' + filas + '</tbody>'
                        + '<table>';
                })
                .catch(function(error) {
                    console.error('Error al cargar usuarios:', error);
                    document.getElementById('tablaUsuarios').innerHTML = '<p class="error">Error al cargar usuarios</p>';
                });
        }

        window.eliminarUsuario = function(username) {
            if (confirm('Eliminar usuario "' + username + '"?')) {
                var formData = new URLSearchParams();
                formData.append('action', 'eliminar');
                formData.append('username', username);
                fetch('${pageContext.request.contextPath}/usuarios', {
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
                        mostrarMensaje('msgUsuario', 'Usuario eliminado', 'success');
                        cargarUsuarios();
                    } else {
                        mostrarMensaje('msgUsuario', 'Error al eliminar', 'error');
                    }
                });
            }
        };

        document.getElementById('formUsuario').addEventListener('submit', function(e) {
            e.preventDefault();
            var formData = new URLSearchParams();
            formData.append('action',   'registrar');
            formData.append('username', document.getElementById('nuevoUsuario').value);
            formData.append('password', document.getElementById('nuevaPassword').value);
            formData.append('rol',      document.getElementById('nuevoRol').value);
            formData.append('nombre',   document.getElementById('nombreUsuario').value || document.getElementById('nuevoUsuario').value);

            fetch('${pageContext.request.contextPath}/usuarios', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            })
            .then(function(r) { return r.text(); })
            .then(function(text) {
                if (text.includes('success') || text.includes('exitosamente')) {
                    mostrarMensaje('msgUsuario', 'Usuario registrado', 'success');
                    document.getElementById('formUsuario').reset();
                    cargarUsuarios();
                } else {
                    mostrarMensaje('msgUsuario', 'Error al registrar', 'error');
                }
            });
        });

        cargarUsuarios();
    </script>
</body>
</html>