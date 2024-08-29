package br.cristiane.collectionmanager.modelo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import br.cristiane.collectionmanager.persistencia.ColecaoConverter;

@Entity(tableName = "item")
public class Item implements Serializable {

    public static final Comparator<Item> ordenacaoCrescente = new Comparator<Item>() {
        @Override
        public int compare(Item item1, Item item2) {
            return item1.getPersonagemNome().compareToIgnoreCase(item2.getPersonagemNome());
        }
    };

    public static final Comparator<Item> ordenacaoDecrescente = new Comparator<Item>() {
        @Override
        public int compare(Item item1, Item item2) {
            return -1 * item1.getPersonagemNome().compareToIgnoreCase(item2.getPersonagemNome());
        }
    };

    @PrimaryKey(autoGenerate = true)
    private long id;

    @TypeConverters(ColecaoConverter.class)
    private Colecao colecao;
    private String personagemNome;
    private String dataAquisicao;
    private double precoAquisicao;
    private boolean desejo;
    private String condicao;

    // Construtor padrão para o Room
    public Item() {
    }

    public Item(Colecao colecao, String personagemNome, String dataAquisicao, double precoAquisicao, boolean desejo, String condicao) {
        this.colecao = colecao;
        this.personagemNome = personagemNome;
        this.dataAquisicao = dataAquisicao;
        this.precoAquisicao = precoAquisicao;
        this.desejo = desejo;
        this.condicao = condicao;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Colecao getColecao() {
        return colecao;
    }

    public void setColecao(Colecao colecao) {
        this.colecao = colecao;
    }

    public String getPersonagemNome() {
        return personagemNome;
    }

    public void setPersonagemNome(String personagemNome) {
        this.personagemNome = personagemNome;
    }

    public String getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(String dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public double getPrecoAquisicao() {
        return precoAquisicao;
    }

    public void setPrecoAquisicao(double precoAquisicao) {
        this.precoAquisicao = precoAquisicao;
    }

    public boolean isDesejo() {
        return desejo;
    }

    public void setDesejo(boolean desejo) {
        this.desejo = desejo;
    }

    public String getCondicao() {
        return condicao;
    }

    public void setCondicao(String condicao) {
        this.condicao = condicao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Double.compare(item.precoAquisicao, precoAquisicao) == 0 &&
                colecao == item.colecao &&
                desejo == item.desejo &&
                Objects.equals(personagemNome, item.personagemNome) &&
                Objects.equals(dataAquisicao, item.dataAquisicao) &&
                Objects.equals(condicao, item.condicao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colecao, personagemNome, dataAquisicao, precoAquisicao, desejo, condicao);
    }

    @Override
    public String toString() {
        return "Item{" +
                "Nome da coleção='" + colecao + '\'' +
                ", Nome do Personagem='" + personagemNome + '\'' +
                '}';
    }
}
