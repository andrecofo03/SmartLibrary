package com.smartlibrary.model;

import com.smartlibrary.orm.LibraryItemDAO;
import com.smartlibrary.orm.CourseDAO;

public class Admin extends Utente {
    public Admin(int id, String matricola, String nome, String email) {
        super(id, matricola, nome, email, "ADMIN");
    }

    public void aggiungiRisorsa(LibraryItemDAO itemDAO, String isbn, String tipo, String titolo, String autore,
            String corsoStudi, int annoAccademico, Object param) {
        itemDAO.addItem(isbn, tipo, titolo, autore, corsoStudi, annoAccademico, param);
    }

    public String aggiornaQuantita(LibraryItemDAO itemDAO, String isbn, int nuoveCopie) {
        return itemDAO.updateQuantita(isbn, nuoveCopie);
    }

    public boolean eliminaRisorsa(LibraryItemDAO itemDAO, String isbn) {
        return itemDAO.deleteItem(isbn);
    }

    public boolean associaCorso(CourseDAO courseDAO, String isbn, String corso, int anno) {
        return courseDAO.addAssociazione(isbn, corso, anno);
    }

    public boolean rimuoviAssociazione(CourseDAO courseDAO, String isbn, String corso, int anno) {
        return courseDAO.removeAssociazione(isbn, corso, anno);
    }
}