package rondaapp;

import java.io.Serializable;

public abstract class DataItem<T> implements Serializable {
    protected T isi;

    public DataItem(T isi) {
        this.isi = isi;
    }

    public T getIsi() {
        return isi;
    }
}
