package service;

import dao.DaoUsuario;
import model.Usuario;
import java.sql.SQLException;

public class ServicioLogin {
    
    private DaoUsuario daoUsuario = new DaoUsuario();
    
    public Usuario validarLogin(String username, String password) throws SQLException {
        return daoUsuario.validarLogin(username, password);
    }
}