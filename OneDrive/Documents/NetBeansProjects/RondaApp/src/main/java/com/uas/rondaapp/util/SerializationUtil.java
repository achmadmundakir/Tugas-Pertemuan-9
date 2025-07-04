package com.uas.rondaapp.util;
import java.io.*;
public class SerializationUtil {
    private static final String SESSION_FILE = "session.ser";
    public static void saveSession(Object obj) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
            oos.writeObject(obj);
        }
    }
    public static Object loadSession() throws IOException, ClassNotFoundException {
        File file = new File(SESSION_FILE);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        }
    }
    public static void deleteSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) file.delete();
    }
}