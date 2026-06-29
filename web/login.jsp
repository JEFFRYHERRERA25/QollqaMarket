<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Qollqa Market - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Segoe UI', sans-serif;
        }

        /* ── Fondo ── */
        .login-container {
            position: fixed;
            inset: 0;
            background: linear-gradient(135deg, #7b241c 0%, #c0392b 50%, #e74c3c 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 2000;
            overflow: hidden;
        }

        /* Círculos decorativos de fondo */
        .login-container::before,
        .login-container::after {
            content: '';
            position: absolute;
            border-radius: 50%;
            opacity: 0.08;
            background: white;
            pointer-events: none;
        }
        .login-container::before {
            width: 500px;
            height: 500px;
            top: -150px;
            right: -100px;
        }
        .login-container::after {
            width: 350px;
            height: 350px;
            bottom: -100px;
            left: -80px;
        }

        /* ── Card ── */
        .login-card {
            background: #fff;
            border-radius: 24px;
            padding: 44px 40px 36px;
            width: 90%;
            max-width: 420px;
            box-shadow: 0 24px 64px rgba(0, 0, 0, 0.28);
            animation: slideUp 0.45s cubic-bezier(.22,.68,0,1.2);
            position: relative;
            z-index: 1;
        }

        @keyframes slideUp {
            from { opacity: 0; transform: translateY(36px) scale(0.97); }
            to   { opacity: 1; transform: translateY(0)    scale(1);    }
        }

        /* ── Header ── */
        .login-header {
            text-align: center;
            margin-bottom: 28px;
        }

        .login-header .logo {
            font-size: 3.6rem;
            line-height: 1;
            margin-bottom: 12px;
            display: block;
            animation: pop 0.5s cubic-bezier(.34,1.56,.64,1) 0.2s both;
        }

        @keyframes pop {
            from { transform: scale(0.6); opacity: 0; }
            to   { transform: scale(1);   opacity: 1; }
        }

        .login-header h1 {
            color: #c0392b;
            font-size: 1.75rem;
            font-weight: 800;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }

        .login-header .desc {
            color: #888;
            font-size: 0.85rem;
        }

        .login-header .subtitle {
            color: #c0392b;
            font-size: 0.75rem;
            margin-top: 6px;
            font-style: italic;
            opacity: 0.8;
        }

        /* ── Mensajes ── */
        .msg {
            font-size: 0.875rem;
            margin-bottom: 18px;
            text-align: center;
            padding: 11px 14px;
            border-radius: 10px;
            display: flex;
            align-items: flex-start;
            gap: 8px;
            line-height: 1.45;
        }

        .msg-error {
            color: #922b21;
            background: #fdecea;
            border: 1px solid #f5c6c2;
        }

        .msg-blocked {
            color: #6e2116;
            background: #fadbd8;
            border: 1px solid #f1948a;
        }

        .msg-icon {
            flex-shrink: 0;
            margin-top: 1px;
        }

        /* Barra de progreso de bloqueo */
        .block-bar-wrap {
            margin: -8px 0 18px;
            height: 4px;
            background: #f5c6c2;
            border-radius: 4px;
            overflow: hidden;
        }
        .block-bar {
            height: 100%;
            background: linear-gradient(90deg, #c0392b, #e74c3c);
            border-radius: 4px;
            animation: shrink linear forwards;
        }

        @keyframes shrink {
            from { width: 100%; }
            to   { width: 0%;   }
        }

        /* ── Formulario ── */
        .form-group {
            margin-bottom: 18px;
        }

        .form-group label {
            display: block;
            margin-bottom: 7px;
            color: #2c3e50;
            font-size: 0.875rem;
            font-weight: 600;
        }

        .input-wrap {
            position: relative;
            display: flex;
            align-items: center;
        }

        .input-wrap .input-icon {
            position: absolute;
            left: 13px;
            color: #bbb;
            font-size: 1rem;
            pointer-events: none;
            transition: color 0.2s;
        }

        .input-wrap input {
            width: 100%;
            padding: 11px 40px 11px 38px;
            border: 2px solid #ecf0f1;
            border-radius: 10px;
            font-size: 0.975rem;
            transition: border-color 0.2s, box-shadow 0.2s;
            color: #2c3e50;
            background: #fafafa;
        }

        .input-wrap input:focus {
            border-color: #c0392b;
            outline: none;
            box-shadow: 0 0 0 3px rgba(192, 57, 43, 0.1);
            background: #fff;
        }

        .input-wrap input:focus + .placeholder-spacer ~ .input-icon,
        .input-wrap input:focus ~ .input-icon {
            color: #c0392b;
        }

        /* Ojo contraseña */
        .toggle-password {
            position: absolute;
            right: 13px;
            cursor: pointer;
            font-size: 1rem;
            color: #bbb;
            background: transparent;
            border: none;
            padding: 4px;
            transition: color 0.2s;
            display: flex;
            align-items: center;
        }
        .toggle-password:hover { color: #c0392b; }

        /* ── Intentos restantes ── */
        .attempts-indicator {
            display: flex;
            justify-content: center;
            gap: 6px;
            margin-bottom: 16px;
        }

        .attempt-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #e74c3c;
            transition: background 0.3s, transform 0.3s;
        }

        .attempt-dot.used {
            background: #f5c6c2;
            transform: scale(0.8);
        }

        /* ── Botón ── */
        .btn-login {
            width: 100%;
            padding: 13px;
            background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 1rem;
            font-weight: 700;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
            letter-spacing: 0.3px;
        }

        .btn-login:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 6px 18px rgba(192, 57, 43, 0.38);
        }

        .btn-login:active:not(:disabled) {
            transform: translateY(0);
            box-shadow: none;
        }

        .btn-login:disabled {
            opacity: 0.55;
            cursor: not-allowed;
            background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);
        }

        /* ── Footer ── */
        .login-footer {
            text-align: center;
            margin-top: 20px;
            font-size: 0.75rem;
            color: #bbb;
        }
    </style>
</head>
<body>

<%
    boolean bloqueado = Boolean.TRUE.equals(request.getAttribute("bloqueado"));
    String  error     = (String) request.getAttribute("error");

    /* Calcular intentos usados para los puntos indicadores.
       El controlador pone en "intentosUsados" cuántos van (1-4).
       Si no existe el atributo, se usa 0. */
    Integer intentosUsadosAttr = (Integer) request.getAttribute("intentosUsados");
    int intentosUsados = (intentosUsadosAttr != null) ? intentosUsadosAttr : 0;

    /* Segundos restantes de bloqueo para la barra animada */
    Long segundosRestantesAttr = (Long) request.getAttribute("segundosRestantes");
    long segundosRestantes = (segundosRestantesAttr != null) ? segundosRestantesAttr : 0;
%>

<div class="login-container">
    <div class="login-card">

        <!-- Header -->
        <div class="login-header">
            <span class="logo">🏪</span>
            <h1>QOLLQA MARKET</h1>
            <p class="desc">Sistema de Inventario y Control</p>
            <p class="subtitle">"Controla tu stock, maximiza tus ganancias"</p>
        </div>

        <!-- Mensaje de error o bloqueo -->
        <% if (error != null) { %>
            <div class="msg <%= bloqueado ? "msg-blocked" : "msg-error" %>">
                <span class="msg-icon">
                    <i class="fa-solid <%= bloqueado ? "fa-lock" : "fa-circle-exclamation" %>"></i>
                </span>
                <span><%= error %></span>
            </div>

            <% if (bloqueado && segundosRestantes > 0) { %>
                <!-- Barra que se vacía en el tiempo restante -->
                <div class="block-bar-wrap">
                    <div class="block-bar"
                         style="animation-duration: <%= segundosRestantes %>s;"></div>
                </div>
            <% } %>
        <% } %>

        <!-- Indicador de intentos (solo si hay intentos fallidos y no está bloqueado) -->
        <% if (intentosUsados > 0 && !bloqueado) { %>
            <div class="attempts-indicator" title="Intentos fallidos">
                <% for (int i = 0; i < 5; i++) { %>
                    <div class="attempt-dot <%= (i < intentosUsados) ? "used" : "" %>"></div>
                <% } %>
            </div>
        <% } %>

        <!-- Formulario -->
        <form action="${pageContext.request.contextPath}/login" method="POST">

            <div class="form-group">
                <label for="username">Usuario</label>
                <div class="input-wrap">
                    <i class="input-icon fa-regular fa-user"></i>
                    <input type="text"
                           id="username"
                           name="username"
                           placeholder="Ingrese su usuario"
                           required
                           autocomplete="off"
                           <%= bloqueado ? "disabled" : "" %>>
                </div>
            </div>

            <div class="form-group">
                <label for="password">Contraseña</label>
                <div class="input-wrap">
                    <i class="input-icon fa-regular fa-lock"></i>
                    <input type="password"
                           id="password"
                           name="password"
                           placeholder="Ingrese su contraseña"
                           required
                           <%= bloqueado ? "disabled" : "" %>>
                    <% if (!bloqueado) { %>
                        <button type="button"
                                class="toggle-password"
                                onclick="togglePassword()"
                                tabindex="-1"
                                aria-label="Mostrar/ocultar contraseña">
                            <i id="eyeIcon" class="fa-regular fa-eye"></i>
                        </button>
                    <% } %>
                </div>
            </div>

            <% if (bloqueado) { %>
                <button type="button" class="btn-login" disabled>
                    <i class="fa-solid fa-lock"></i>&nbsp; Acceso bloqueado temporalmente
                </button>
            <% } else { %>
                <button type="submit" class="btn-login">
                    Ingresar al Sistema
                </button>
            <% } %>

        </form>

        <div class="login-footer">
            &copy; <%= java.time.Year.now().getValue() %> Qollqa Market
        </div>

    </div>
</div>

<script>
    function togglePassword() {
        var input = document.getElementById('password');
        var icon  = document.getElementById('eyeIcon');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.replace('fa-eye', 'fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.replace('fa-eye-slash', 'fa-eye');
        }
    }

    /* Auto-recarga cuando termina el bloqueo */
    <% if (bloqueado && segundosRestantes > 0) { %>
        setTimeout(function() {
            window.location.reload();
        }, <%= segundosRestantes * 1000 %>);
    <% } %>
</script>

</body>
</html>
