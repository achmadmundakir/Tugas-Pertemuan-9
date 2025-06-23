package rondaapp;

import java.io.*;

class DataManager {
    public static void saveData(Object data, String filename) throws Exception {
        byte[] encrypted = CryptoUtil.encrypt(serialize(data));
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(encrypted);
        }
    }

    public static Object loadData(String filename) throws Exception {
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] encrypted = fis.readAllBytes();
            return deserialize(CryptoUtil.decrypt(encrypted));
        }
    }

    private static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
}
