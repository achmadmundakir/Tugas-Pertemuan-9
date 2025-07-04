package com.uas.rondaapp.model;

import java.io.Serializable;

// Model ini sekarang memiliki properti 'role'
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String username;
    private String namaLengkap;
    private String role; // <-- Perubahan di sini

    // Constructor diubah untuk menerima 'role'
    public User(int id, String username, String namaLengkap, String role) {
        this.id = id;
        this.username = username;
        this.namaLengkap = namaLengkap;
        this.role = role;
    }

    // Getters
    public int getId() { 
        return id; 
    }
    public String getUsername() { 
        return username; 
    }
    public String getNamaLengkap() { 
        return namaLengkap; 
    }
    public String getRole() { 
        return role; // <-- Getter baru
    }
}