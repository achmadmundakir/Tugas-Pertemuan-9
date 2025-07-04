package com.uas.rondaapp.util;
import com.uas.rondaapp.model.User;
import java.io.Serializable;
public class AppSession implements Serializable {
    private static final long serialVersionUID = 1L;
    private final User loggedInUser;
    public AppSession(User user) { this.loggedInUser = user; }
    public User getLoggedInUser() { return loggedInUser; }
}