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

import br.cristiane.collectionmanager.modelo.Item;
import br.cristiane.collectionmanager.utils.UtilsGUI;

public class ListarActivity extends AppCompatActivity {

    private ListView listViewItem;
    private ItemAdapter listaAdapter;
    private ArrayList<Item> listaItem;
    private ActionMode actionMode;
    private View viewSelecionada;
    private int posicaoSelecionada = -1;
    public static final String ARQUIVO = "br.cristiane.collectionmanager.sharedpreferences.PREFERENCIAS";
    public static final String ORDENACAO_ASCENDENTE = "ORDENACAO_ASCENDENTE";
    private boolean ordenacaoAscendende = true;

    // Callback para gerenciar o menu de ação contextual
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Infla o menu de ação contextual
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
                editarItem();  // Chama o método para editar o item selecionado
                mode.finish();  // Encerra o ActionMode
                return true;
            } else if (item.getItemId() == R.id.excluir) {
                excluirItem(mode);  // Chama o método para excluir o item selecionado
                return true;
            } else {
                return false;
            }
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
        Item item = listaItem.get(posicaoSelecionada);
        String mensagem = getString(R.string.deseja_excluir) + "\n" + "\"" + item.getPersonagemNome() + "\"";

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    listaItem.remove(posicaoSelecionada);
                    listaAdapter.notifyDataSetChanged();
                    mode.finish();
                }
            }
        };

        UtilsGUI.confirmaAcao(this, mensagem, listener);
    }

    private void editarItem() {
        Item item = listaItem.get(posicaoSelecionada);
        Intent intent = new Intent(ListarActivity.this, CadastrarActivity.class);
        intent.putExtra(CadastrarActivity.MODO, CadastrarActivity.EDITAR);
        intent.putExtra(CadastrarActivity.ID, item.getId());
        launcherNovoItem.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar);
        setTitle(R.string.app_name);

        // Inicializa a lista de itens e o adapter
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

                return false;
            }
        });

        lerPreferenciaOrdenacaoAscendente();

        popularLista();
    }

    private void popularLista() {
        listaItem = new ArrayList<>();
        listaAdapter = new ItemAdapter(this, listaItem); // Usando o ItemAdapter customizado
        listViewItem.setAdapter(listaAdapter);
    }

    ActivityResultLauncher<Intent> launcherNovoItem = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Item itemEditado = (Item) result.getData().getSerializableExtra("novoItem");

                        if (itemEditado != null && posicaoSelecionada != -1) {
                            listaItem.set(posicaoSelecionada, itemEditado);
                            ordenarLista();
                            Toast.makeText(ListarActivity.this, R.string.item_editado_com_sucesso, Toast.LENGTH_SHORT).show();
                        } else if (itemEditado != null) {
                            // Se for um novo item
                            listaItem.add(itemEditado);
                            ordenarLista();
                            Toast.makeText(ListarActivity.this, R.string.item_adicionado_com_sucesso, Toast.LENGTH_SHORT).show();
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
        int id = item.getItemId();

        if (id == R.id.action_sobre) {
            // Inicia a Activity SobreActivity
            Intent intent = new Intent(ListarActivity.this, SobreActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuItemOrdenacao) {
            salvarPreferenciaOrdenacaoAscendente(!ordenacaoAscendende);
            atualizarIconeOrdenacao(item);
            ordenarLista();
            return true;
        } else if (id == R.id.action_adicionar) {
            // Reinicia a posição selecionada para -1, indicando que é um novo item
            posicaoSelecionada = -1;

            // Inicia a Activity de Cadastro para adicionar um novo item
            Intent intent = new Intent(ListarActivity.this, CadastrarActivity.class);
            launcherNovoItem.launch(intent);
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
        if (ordenacaoAscendende) {
            menuItemOrdenacao.setIcon(R.drawable.ic_action_ascendente);
        } else {
            menuItemOrdenacao.setIcon(R.drawable.ic_action_descendente);
        }
    }

    private void ordenarLista() {
        if (ordenacaoAscendende) {
            Collections.sort(listaItem, Item.ordenacaoCrescente);
        } else {
            Collections.sort(listaItem, Item.ordenacaoDecrescente);
        }
        listaAdapter.notifyDataSetChanged();
    }

    private void lerPreferenciaOrdenacaoAscendente() {
        SharedPreferences shared = getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
        ordenacaoAscendende = shared.getBoolean(ORDENACAO_ASCENDENTE, ordenacaoAscendende);
    }

    private void salvarPreferenciaOrdenacaoAscendente(boolean novoValor) {
        SharedPreferences shared = getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(ORDENACAO_ASCENDENTE, novoValor);
        editor.apply();
        ordenacaoAscendende = novoValor;
    }
}
