package br.cristiane.collectionmanager.persistencia;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import br.cristiane.collectionmanager.modelo.Colecao;
import br.cristiane.collectionmanager.modelo.Item;

@Database(entities = { Item.class}, version = 1, exportSchema = false)
public abstract class ItemsDatabase extends RoomDatabase {

    public abstract ItemDao getItemDao();

    private static ItemsDatabase instance;

    public static ItemsDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (ItemsDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context,
                                                    ItemsDatabase.class,
                                                    "items_database")
                                                    .build();
                }
            }
        }
        return instance;
    }
}
