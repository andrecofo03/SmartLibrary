package com.smartlibrary.logic;

import com.smartlibrary.model.Utente;
import com.smartlibrary.orm.UserDAO;
import java.util.regex.Pattern;

public class AuthenticationController {
    private UserDAO userDAO = new UserDAO();

    public AuthenticationController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public AuthenticationController() {
        this(new UserDAO());
    }

    public Utente login(String matricola, String password) {
        if (matricola == null || password == null)
            return null;
        return userDAO.login(matricola, password);
    }

    public String registraStudente(String matricola, String password, String nome, String email) {
        if (email == null || !Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", email)) {
            return "ERRORE: Formato email non valido.";
        }
        if (nome == null || nome.trim().isEmpty()) {
            return "ERRORE: Il nome non può essere vuoto.";
        }
        if (!Pattern.matches("^\\d{7}$", matricola)) {
            return "ERRORE: La matricola deve essere composta da 7 cifre numeriche.";
        }
        if (password.length() < 5) {
            return "ERRORE: Password troppo breve (min 5 caratteri).";
        }
        boolean success = userDAO.register(matricola, password, nome, email, "STUDENTE");
        return success ? "Registrazione completata! Ora puoi fare il login." : "ERRORE: Matricola già esistente.";
    }
}
