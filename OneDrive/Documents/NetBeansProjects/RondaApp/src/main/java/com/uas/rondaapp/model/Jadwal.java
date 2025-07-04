package com.uas.rondaapp.model;
import java.io.Serializable;
import java.util.Date;
public class Jadwal implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private Date tanggal;
    private String namaPetugas;
    private String shift;
    private String status;
    public Jadwal(int id, Date tanggal, String namaPetugas, String shift, String status) { this.id = id; this.tanggal = tanggal; this.namaPetugas = namaPetugas; this.shift = shift; this.status = status; }
    public int getId() { return id; }
    public Date getTanggal() { return tanggal; }
    public String getNamaPetugas() { return namaPetugas; }
    public String getShift() { return shift; }
    public String getStatus() { return status; }
}