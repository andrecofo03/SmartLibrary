package com.smartlibrary.view.command;
import com.smartlibrary.model.*;
import com.smartlibrary.orm.CourseDAO;
import com.smartlibrary.logic.StudentLoanController;
import com.smartlibrary.utils.InputManager.ValidatedReader;
import java.util.List;
import java.util.Map;
public class StudentCommandPattern {
    public interface Command {
        void execute();
        String getDescription();
    }
    public static class SearchCommand implements Command {
        private ValidatedReader input;
        private StudentLoanController controller;
        private CourseDAO courseDAO;
        private Map<Integer, String> corsiMap;
        public SearchCommand(ValidatedReader input, StudentLoanController controller, CourseDAO courseDAO,
                Map<Integer, String> corsiMap) {
            this.input = input;
            this.controller = controller;
            this.courseDAO = courseDAO;
            this.corsiMap = corsiMap;
        }
        @Override
        public String getDescription() {
            return "Visualizza Libri Disponibili";
        }
        @Override
        public void execute() {
            System.out.println("\n SELEZIONA IL CORSO ");
            corsiMap.forEach((k, v) -> System.out.println(k + ". " + v));
            int corsoId = input.readInt("Seleziona > ", 1, corsiMap.size());
            String nomeCorso = corsiMap.get(corsoId);
            List<Integer> anniDisponibili = courseDAO.getAnniByCorso(nomeCorso);
            if (anniDisponibili.isEmpty()) {
                System.out.println(">> Nessun anno trovato per questo corso");
                return;
            }
            System.out.println("Anni disponibili: " + anniDisponibili);
            int anno = input.readInt("Scegli anno: ",
                    anniDisponibili.get(0),
                    anniDisponibili.get(anniDisponibili.size() - 1));
            List<ElementoBibliotecario> risultati = controller.cercaPerCorso(nomeCorso, anno);
            if (risultati.isEmpty()) {
                System.out.println(">> Nessun libro trovato per questo corso/anno");
            } else {
                System.out.printf("\n%-15s %-35s %-20s\n", "ISBN", "TITOLO", "DISPONIBILITÀ");
                System.out.println("--------------------------------------------------------------------------");
                for (ElementoBibliotecario e : risultati) {
                    String disp = (e instanceof Libro)
                            ? "Copie: " + ((Libro) e).getCopieDisponibili()
                            : "EBOOK";
                    System.out.printf("%-15s %-35s %-20s\n",
                            truncate(e.getIsbn(), 15),
                            truncate(e.getTitolo(), 33),
                            disp);
                }
            }
        }
        private String truncate(String s, int len) {
            return (s.length() > len) ? s.substring(0, len - 3) + "..." : s;
        }
    }
    public static class LoanCommand implements Command {
        private ValidatedReader input;
        private StudentLoanController controller;
        private Studente studente;
        public LoanCommand(ValidatedReader input, StudentLoanController controller, Studente studente) {
            this.input = input;
            this.controller = controller;
            this.studente = studente;
        }
        @Override
        public String getDescription() {
            return "Richiedi prestito / Scarica ebook";
        }
        @Override
        public void execute() {
            String isbn = input.readString("Inserisci ISBN del libro: ");
            String risultato = controller.richiediPrestito(studente, isbn);
            System.out.println(">> ESITO: " + risultato);
        }
    }
    public static class ReturnCommand implements Command {
        private ValidatedReader input;
        private StudentLoanController controller;
        private Studente studente;
        public ReturnCommand(ValidatedReader input, StudentLoanController controller, Studente studente) {
            this.input = input;
            this.controller = controller;
            this.studente = studente;
        }
        @Override
        public String getDescription() {
            return "Restituisci libro";
        }
        @Override
        public void execute() {
            String isbn = input.readString("Inserisci ISBN da restituire: ");
            controller.restituisciLibro(isbn, studente.getId());
            System.out.println(">> Restituzione registrata (se il prestito esisteva)");
        }
    }
    public static class HistoryCommand implements Command {
        private StudentLoanController controller;
        private Studente studente;
        public HistoryCommand(StudentLoanController controller, Studente studente) {
            this.controller = controller;
            this.studente = studente;
        }
        @Override
        public String getDescription() {
            return "Il tuo storico prestiti";
        }
        @Override
        public void execute() {
            List<Prestito> storico = controller.getStorico(studente.getId());
            System.out.println("\n STORICO PRESTITI ");
            System.out.printf("%-15s %-30s %-12s %-12s %-10s\n", "ISBN", "TITOLO", "INIZIO", "SCADENZA", "STATO");
            System.out.println("-------------------------------------------------------------------------------------");
            for (Prestito p : storico) {
                String stato = (p.getDataRestituzione() != null) ? "CHIUSO" : (p.isExpired() ? "SCADUTO" : "ATTIVO");
                System.out.printf("%-15s %-30s %-12s %-12s %-10s\n",
                        p.getIsbn(),
                        (p.getTitoloLibro().length() > 28 ? p.getTitoloLibro().substring(0, 25) + "..."
                                : p.getTitoloLibro()),
                        p.getDataInizio(),
                        p.getDataScadenza(),
                        stato);
            }
            System.out.println("-------------------------------------------------------------------------------------");
        }
    }
}