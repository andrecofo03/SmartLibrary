package com.smartlibrary.logic;

import com.smartlibrary.model.ElementoBibliotecario;
import com.smartlibrary.model.Ebook;
import com.smartlibrary.model.Libro;
import com.smartlibrary.model.Prestito;
import com.smartlibrary.model.Studente;
import com.smartlibrary.orm.LibraryItemDAO;
import com.smartlibrary.orm.LoanDAO;
import java.util.List;

public class LoanService {
    private final LibraryItemDAO itemDAO;
    private final LoanDAO loanDAO;

    public LoanService(LibraryItemDAO itemDAO, LoanDAO loanDAO) {
        this.itemDAO = itemDAO;
        this.loanDAO = loanDAO;
    }

    public String richiediPrestito(Studente studente, String isbn) {
        ElementoBibliotecario item = itemDAO.findByIsbn(isbn);
        if (item == null)
            return "Errore: Nessun elemento trovato con ISBN '" + isbn + "'.";
        if (loanDAO.hasActiveLoan(studente.getId(), isbn)) {
            return "Errore: Hai già un prestito attivo per '" + item.getTitolo() + "'.";
        }
        if (item instanceof Libro) {
            if (item.isAvailable()) {
                itemDAO.decrementaCopie(isbn);
                loanDAO.createLoan(studente.getId(), isbn);
                WaitingListManager.getInstance().detach(isbn, studente);
                return "PRESTITO CARTACEO CONFERMATO (Scadenza: 30gg)";
            } else {
                WaitingListManager.getInstance().attach(isbn, studente);
                return "COPIE ESAURITE: Inserito in lista d'attesa.";
            }
        } else if (item instanceof Ebook) {
            loanDAO.createLoan(studente.getId(), isbn);
            return "EBOOK SCARICABILE (Scadenza accesso: 30gg). URL: " + ((Ebook) item).getUrl();
        }
        return "Errore: Tipo elemento non gestito '" + item.getClass().getSimpleName() + "'.";
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