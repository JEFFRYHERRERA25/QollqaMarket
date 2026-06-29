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

    <title>Qollqa Market - Buscar Productos</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/CSS/style.css">

</head>

<body>

    <div class="app-container">

        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="buscar"/>
        </jsp:include>

        <div class="main-content">

            <div class="content-header">

                <h2>🔍 Búsqueda de Productos</h2>

                <p>Encuentra productos rápidamente</p>

            </div>

            <div class="content-body">

                <div class="card">

                    <div class="card-header">

                        Buscar Productos

                    </div>

                    <div class="card-body">

                        <div class="form-grid">

                            <div class="form-group">

                                <label>
                                    🔎 Buscar por código, nombre o grupo
                                </label>

                                <input type="text"
                                       id="buscarTexto"
                                       placeholder="Escriba para buscar...">

                            </div>

                        </div>

                        <div class="actions">

                            <button class="btn btn-primary"
                                    onclick="buscarProductos()">

                                🔍 Buscar

                            </button>

                            <button class="btn btn-secondary"
                                    onclick="limpiarBusqueda()">

                                🧹 Limpiar

                            </button>

                        </div>

                        <div class="table-container">

                            <div id="resultadosBusqueda"></div>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    </div>

    <!-- MODAL EDITAR -->
    <div id="modalEditar" class="modal">

        <div class="modal-content">

            <div class="modal-header">

                <h3>✏️ Modificar Producto</h3>

                <span class="close"
                      onclick="cerrarModal()">

                    &times;

                </span>

            </div>

            <div class="modal-body">

                <div class="form-group">

                    <label>Código</label>

                    <input type="text"
                           id="editCodigo"
                           readonly>

                </div>

                <div class="form-group">

                    <label>Nombre</label>

                    <input type="text"
                           id="editNombre">

                </div>

                <div class="form-group">

                    <label>Grupo</label>

                    <select id="editGrupo">

                        <option value="ABARROTES">ABARROTES</option>
                        <option value="BEBIDAS">BEBIDAS</option>
                        <option value="LIMPIEZA">LIMPIEZA</option>
                        <option value="LACTEOS">LACTEOS</option>
                        <option value="SNACKS">SNACKS</option>
                        <option value="OTROS">OTROS</option>

                    </select>

                </div>

                <div class="form-group">

                    <label>Unidad</label>

                    <select id="editUnidad">

                        <option value="UNIDAD">UNIDAD</option>
                        <option value="KILOGRAMO">KILOGRAMO</option>
                        <option value="LITRO">LITRO</option>
                        <option value="PAQUETE">PAQUETE</option>
                        <option value="CAJA">CAJA</option>

                    </select>

                </div>

                <div class="form-group">

                    <label>Stock Mínimo</label>

                    <input type="number"
                           id="editStockMin">

                </div>

            </div>

            <div class="modal-footer">

                <button class="btn btn-secondary"
                        onclick="cerrarModal()">

                    Cancelar

                </button>

                <button class="btn btn-success"
                        onclick="guardarCambios()">

                    💾 Guardar

                </button>

            </div>

        </div>

    </div>

    <script>

        const contextPath = "<%= request.getContextPath() %>";

        function buscarProductos() {

            const texto =
                document.getElementById('buscarTexto')
                .value
                .trim();

            fetch(
                contextPath +
                '/buscar?action=productos&texto=' +
                encodeURIComponent(texto)
            )

            .then(response => response.json())

            .then(data => {

                let html =
                    '<table class="data-table"><thead><tr>' +
                    '<th>Código</th><th>Nombre</th><th>Grupo</th><th>Stock</th><th>Unidad</th><th>Stock Mínimo</th><th>Acciones</th>' +
                    '</tr></thead><tbody>';

                for (let p of data) {

                    html +=
                        '<tr>' +
                        '<td>' + p.codigo + '</td>' +
                        '<td>' + p.nombre + '</td>' +
                        '<td>' + p.grupo + '</td>' +
                        '<td><strong>' + p.cantidad + '</strong></td>' +
                        '<td>' + p.unidad + '</td>' +
                        '<td>' + p.stockMin + '</td>' +
                        '<td>' +

                        '<button class="btn-edit" onclick="abrirModal(\'' +
                        p.codigo + '\',\'' +
                        p.nombre + '\',\'' +
                        p.grupo + '\',\'' +
                        p.unidad + '\',' +
                        p.stockMin +
                        ')">✏️ Modificar</button>' +

                        '<button class="btn-danger" onclick="eliminarProducto(\'' +
                        p.codigo +
                        '\')">🗑️ Eliminar</button>' +

                        '</td></tr>';
                }

                html += '</tbody></table>';

                document.getElementById("resultadosBusqueda").innerHTML = html;
            });
        }

        // ========================= FIX ELIMINAR (SOLO ESTO SE AJUSTÓ) =========================
        function eliminarProducto(codigo) {

            if (!confirm('¿Estás seguro de eliminar el producto ' + codigo + '?')) {
                return;
            }

            const datos = new URLSearchParams();
            datos.append("accion", "eliminar");
            datos.append("codigo", codigo);

            fetch(contextPath + "/buscar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: datos.toString()
            })
            .then(response => response.text())
            .then(data => {

                console.log("RESPUESTA DELETE:", data);

                if (data.trim() === "OK") {
                    alert("✅ Producto eliminado correctamente");
                    buscarProductos();
                } else {
                    alert("❌ Error al eliminar: " + data);
                }
            })
            .catch(error => {
                console.error(error);
                alert("❌ Error de conexión");
            });
        }

        function abrirModal(codigo, nombre, grupo, unidad, stockMin) {

            document.getElementById("modalEditar").style.display = "block";

            document.getElementById("editCodigo").value = codigo;
            document.getElementById("editNombre").value = nombre;
            document.getElementById("editGrupo").value = grupo;
            document.getElementById("editUnidad").value = unidad;
            document.getElementById("editStockMin").value = stockMin;
        }

        function cerrarModal() {
            document.getElementById("modalEditar").style.display = "none";
        }

        function guardarCambios() {

            const datos = new URLSearchParams();

            datos.append("codigo", document.getElementById("editCodigo").value);
            datos.append("nombre", document.getElementById("editNombre").value);
            datos.append("grupo", document.getElementById("editGrupo").value);
            datos.append("unidad", document.getElementById("editUnidad").value);
            datos.append("stockMin", document.getElementById("editStockMin").value);

            fetch(contextPath + "/buscar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: datos.toString()
            })

            .then(response => response.text())
            .then(data => {

                if (data === "OK") {
                    alert("✅ Producto modificado correctamente");
                    cerrarModal();
                    buscarProductos();
                } else {
                    alert("❌ Error al modificar el producto");
                }
            })
            .catch(error => {
                console.error(error);
                alert("❌ Error al modificar");
            });
        }

        window.onclick = function(event) {

            const modal =
                document.getElementById("modalEditar");

            if (event.target == modal) {
                cerrarModal();
            }
        }

        document.addEventListener('DOMContentLoaded', function() {

            const inputBuscar =
                document.getElementById('buscarTexto');

            if (inputBuscar) {

                inputBuscar.addEventListener('keypress', function(e) {

                    if (e.key === 'Enter') {
                        buscarProductos();
                    }
                });
            }
        });

    </script>

</body>
</html>