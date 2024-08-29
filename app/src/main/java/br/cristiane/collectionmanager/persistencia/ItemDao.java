package br.cristiane.collectionmanager.persistencia;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.cristiane.collectionmanager.modelo.Item;

@Dao
public interface ItemDao {

    @Insert
    long insert(Item item);

    @Delete
    int delete(Item item);

    @Update
    int update(Item item);

    @Query("SELECT * FROM item WHERE id = :id")
    Item queryById(long id);

    @Query("SELECT * FROM item ORDER BY personagemNome ASC")
    List<Item> queryAllAscending();

    @Query("SELECT * FROM item ORDER BY personagemNome DESC")
    List<Item> queryAllDownward();
}
