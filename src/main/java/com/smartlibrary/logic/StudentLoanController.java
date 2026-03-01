package com.smartlibrary.logic;

import com.smartlibrary.model.ElementoBibliotecario;

import java.util.List;

import com.smartlibrary.model.Ebook;
import com.smartlibrary.model.Libro;
import com.smartlibrary.model.Prestito;
import com.smartlibrary.model.Studente;
import com.smartlibrary.orm.LibraryItemDAO;
import com.smartlibrary.orm.LoanDAO; 

public class StudentLoanController {
    private final LibraryItemDAO itemDAO;
    private final LoanDAO loanDAO;

    public StudentLoanController(LibraryItemDAO itemDAO, LoanDAO loanDAO) {
        this.itemDAO = itemDAO;
        this.loanDAO = loanDAO;
    }

    public StudentLoanController() {
        this(new LibraryItemDAO(), new LoanDAO());
    }


    public List<ElementoBibliotecario> cercaPerCorso(String corso, int anno) {
        return itemDAO.findByCorsoEAnno(corso, anno);
    }

    public String richiediPrestito(Studente studente, String isbn) {
        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item == null) return "Libro non trovato.";
    
        if (item instanceof Libro) {
            if (item.isAvailable()) {
                itemDAO.decrementaCopie(isbn);
                loanDAO.createLoan(studente.getId(), isbn);
                return "PRESTITO CARTACEO CONFERMATO (Scadenza: 30gg)";
            } else {
                WaitingListManager.getInstance().attach(isbn, studente);
                return "COPIE ESAURITE: Inserito in lista d'attesa.";
            }
        } else if (item instanceof Ebook) {
            loanDAO.createLoan(studente.getId(), isbn);
            return "EBOOK SCARICABILE (Scadenza accesso: 30gg). URL: " + ((Ebook) item).getUrl();
        }
        return "Errore generico.";
    }

    public void restituisciLibro(String isbn, int userId) {
        loanDAO.closeLoanByIsbn(isbn, userId);
        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item instanceof Libro) {
            itemDAO.incrementaCopie(isbn);
            WaitingListManager.getInstance().itemReturned(isbn, item.getTitolo());
        }
    }

    public List<Prestito> getScaduti(int userId) {
        return loanDAO.findExpiredLoans(userId);
    }

    public void estendiPrestito(int loanId) {
        loanDAO.extendLoan(loanId);
    }

    public void terminaPrestitoScaduto(int loanId, String isbn) {
        loanDAO.closeLoan(loanId);
        
        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item instanceof Libro) {
            itemDAO.incrementaCopie(isbn);
            WaitingListManager.getInstance().itemReturned(isbn, item.getTitolo());
        }
    }

    public List<Prestito> getStorico(int userId) {
        return loanDAO.getHistory(userId);
    }
}