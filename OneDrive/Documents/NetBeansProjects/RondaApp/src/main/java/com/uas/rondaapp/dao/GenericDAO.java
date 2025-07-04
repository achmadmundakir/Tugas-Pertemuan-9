package com.uas.rondaapp.dao;
import java.util.List;
public interface GenericDAO<T, ID> {
    void save(T entity);
    T findById(ID id);
    List<T> findAll();
}