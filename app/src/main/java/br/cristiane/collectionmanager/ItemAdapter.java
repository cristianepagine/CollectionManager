package br.cristiane.collectionmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.cristiane.collectionmanager.modelo.Item;

public class ItemAdapter extends ArrayAdapter<Item> {
    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obter o item para esta posição
        Item item = getItem(position);

        // Verificar se a View existente está sendo reutilizada, caso contrário, inflar a View(reutilizar)
        if (convertView == null) {
            // Inflar (criar) uma nova View a partir do layout item_list.xml
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }

        // Procurar TextViews para inserir os dados do item
        TextView colecaoNome = convertView.findViewById(R.id.colecao_nome);
        TextView personagemNome = convertView.findViewById(R.id.personagem_nome);
        TextView dataAquisicao = convertView.findViewById(R.id.data_aquisicao);
        TextView precoAquisicao = convertView.findViewById(R.id.preco_aquisicao);
        TextView desejo = convertView.findViewById(R.id.desejo);
        TextView condicao = convertView.findViewById(R.id.condicao);

        // Preencher as TextViews com os dados do item
        colecaoNome.setText("Coleção: " + item.getColecao().toString()); // Atualizado para usar item.getColecao().toString()
        personagemNome.setText("Personagem: " + item.getPersonagemNome());
        dataAquisicao.setText("Data Aquisição: " + item.getDataAquisicao());
        precoAquisicao.setText("Preço: R$ " + item.getPrecoAquisicao());
        desejo.setText("Desejo: " + (item.isDesejo() ? "Sim" : "Não"));
        condicao.setText("Condição: " + item.getCondicao());

        // Retornar a View completa para ser exibida
        return convertView;
    }
}
