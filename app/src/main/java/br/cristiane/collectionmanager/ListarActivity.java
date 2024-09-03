package br.cristiane.collectionmanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.cristiane.collectionmanager.modelo.Item;
import br.cristiane.collectionmanager.persistencia.ItemsDatabase;
import br.cristiane.collectionmanager.utils.UtilsGUI;

public class ListarActivity extends AppCompatActivity {

    private ListView listViewItem;
    private ItemAdapter listaAdapter;
    private List<Item> listaItem;
    private ActionMode actionMode;
    private View viewSelecionada;
    private int posicaoSelecionada = -1;
    public static final String ARQUIVO = "br.cristiane.collectionmanager.sharedpreferences.PREFERENCIAS";
    public static final String ORDENACAO_ASCENDENTE = "ORDENACAO_ASCENDENTE";
    private boolean ordenacaoAscendente = true;

    // Callback para gerenciar o menu de ação contextual
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_contextual, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.editar) {
                editarItem();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.excluir) {
                excluirItem(mode);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (viewSelecionada != null) {
                viewSelecionada.setBackgroundColor(Color.TRANSPARENT);
            }
            actionMode = null;
            viewSelecionada = null;
            listViewItem.setEnabled(true);
        }
    };

    private void excluirItem(final ActionMode mode) {
        final Item item = listaItem.get(posicaoSelecionada);

        String mensagem = getString(R.string.deseja_excluir) + "\n" + "\"" + item.getPersonagemNome() + "\"";

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which){

                    case DialogInterface.BUTTON_POSITIVE:

                        ItemsDatabase database = ItemsDatabase.getDatabase(ListarActivity.this);

                        int quantidadeAlterada = database.getItemDao().delete(item);

                        if (quantidadeAlterada > 0){
                            listaItem.remove(posicaoSelecionada);
                            listaAdapter.notifyDataSetChanged();
                            mode.finish();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }

            }

        };
        UtilsGUI.confirmaAcao(this, mensagem, listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar);
        setTitle(R.string.app_name);

        listViewItem = findViewById(R.id.list_view);

        listViewItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                posicaoSelecionada = position;
                editarItem();
            }
        });

        listViewItem.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode != null) {
                    return false;
                }

                posicaoSelecionada = position;
                view.setBackgroundColor(Color.LTGRAY);
                viewSelecionada = view;
                listViewItem.setEnabled(false);
                actionMode = startSupportActionMode(mActionModeCallback);
                return true;
            }
        });

        lerPreferenciaOrdenacaoAscendente();

        popularLista();
    }

    private void popularLista() {
        ItemsDatabase database = ItemsDatabase.getDatabase(this);
        if (ordenacaoAscendente){
            listaItem = database.getItemDao().queryAllAscending();
        }else{
            listaItem = database.getItemDao().queryAllDownward();
        }
        listaAdapter = new ItemAdapter(this, listaItem);
        listViewItem.setAdapter(listaAdapter);

    }


    ActivityResultLauncher<Intent> launcherEditarItem = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent intent = result.getData();

                        Bundle bundle = intent.getExtras();

                        if (bundle != null){

                            long id = bundle.getLong(CadastrarActivity.ID);

                            ItemsDatabase database = ItemsDatabase.getDatabase(ListarActivity.this);

                            Item itemEditado = database.getItemDao().queryById(id);

                            listaItem.set(posicaoSelecionada, itemEditado);

                            posicaoSelecionada = -1;

                            ordenarLista();
                        }
                    }
                }
            });

    private void editarItem(){

        Item item = listaItem.get(posicaoSelecionada);

        CadastrarActivity.editarItem(this, launcherEditarItem, item);
    }
    ActivityResultLauncher<Intent> launcherNovoItem = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){

                        Intent intent = result.getData();

                        Bundle bundle = intent.getExtras();

                        if (bundle != null){

                            long id = bundle.getLong(CadastrarActivity.ID);

                            ItemsDatabase database = ItemsDatabase.getDatabase(ListarActivity.this);

                            Item itemInserido = database.getItemDao().queryById(id);

                            listaItem.add(itemInserido);

                            ordenarLista();
                        }
                    }
                }
            });

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItemOrdenacao = menu.findItem(R.id.menuItemOrdenacao);
        atualizarIconeOrdenacao(menuItemOrdenacao);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sobre) {
            Intent intent = new Intent(ListarActivity.this, SobreActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menuItemOrdenacao) {
            salvarPreferenciaOrdenacaoAscendente(!ordenacaoAscendente);
            atualizarIconeOrdenacao(item);
            ordenarLista();
            return true;
        } else if (item.getItemId() == R.id.action_adicionar) {
            posicaoSelecionada = -1;
            Intent adicionarIntent = new Intent(ListarActivity.this, CadastrarActivity.class);
            adicionarIntent.putExtra(CadastrarActivity.MODO, CadastrarActivity.NOVO);
            launcherNovoItem.launch(adicionarIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_listar, menu);
        return true;
    }

    private void atualizarIconeOrdenacao(MenuItem menuItemOrdenacao) {
        if (ordenacaoAscendente) {
            menuItemOrdenacao.setIcon(R.drawable.ic_action_ascendente);
        } else {
            menuItemOrdenacao.setIcon(R.drawable.ic_action_descendente);
        }
    }

    private void ordenarLista(){

        if (ordenacaoAscendente){
            Collections.sort(listaItem, Item.ordenacaoCrescente);
        }else{
            Collections.sort(listaItem, Item.ordenacaoDecrescente);
        }

        listaAdapter.notifyDataSetChanged();
    }

    private void lerPreferenciaOrdenacaoAscendente() {
        SharedPreferences shared = getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
        ordenacaoAscendente = shared.getBoolean(ORDENACAO_ASCENDENTE, ordenacaoAscendente);
    }

    private void salvarPreferenciaOrdenacaoAscendente(boolean novoValor) {
        SharedPreferences shared = getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(ORDENACAO_ASCENDENTE, novoValor);
        editor.apply();
        ordenacaoAscendente = novoValor;
    }
}
