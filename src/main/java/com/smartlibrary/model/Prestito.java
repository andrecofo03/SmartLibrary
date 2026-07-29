package com.smartlibrary.model;

import java.sql.Date;

public class Prestito {
    private int id;
    private String titoloLibro;
    private String isbn;
    private Date dataInizio;
    private Date dataScadenza;
    private Date dataRestituzione;
    private int renewalCount;

    public Prestito(int id, String titoloLibro, String isbn, Date dataInizio, Date dataScadenza, Date dataRestituzione,
            int renewalCount) {
        this.id = id;
        this.titoloLibro = titoloLibro;
        this.isbn = isbn;
        this.dataInizio = dataInizio;
        this.dataScadenza = dataScadenza;
        this.dataRestituzione = dataRestituzione;
        this.renewalCount = renewalCount;
    }

    public int getId() {
        return id;
    }

    public String getTitoloLibro() {
        return titoloLibro;
    }

    public String getIsbn() {
        return isbn;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public Date getDataScadenza() {
        return dataScadenza;
    }

    public Date getDataRestituzione() {
        return dataRestituzione;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public boolean isExpired() {
        return dataRestituzione == null && new java.util.Date().after(dataScadenza);
    }

    public String getDataInizioFormatted() {
        if (dataInizio == null)
            return "N/A";
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(dataInizio);
    }

    public String getDataScadenzaFormatted() {
        if (dataScadenza == null)
            return "N/A";
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(dataScadenza);
    }

    public boolean isRenewable() {
        return renewalCount < 2;
    }
}