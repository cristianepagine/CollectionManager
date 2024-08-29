package br.cristiane.collectionmanager.persistencia;

import androidx.room.TypeConverter;
import br.cristiane.collectionmanager.modelo.Colecao;

public class ColecaoConverter {

    @TypeConverter
    public static Colecao fromString(String value) {
        return Colecao.valueOf(value);
    }

    @TypeConverter
    public static String fromColecao(Colecao colecao) {
        return colecao.name();
    }
}
