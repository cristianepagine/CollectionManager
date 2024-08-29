package br.cristiane.collectionmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import br.cristiane.collectionmanager.modelo.Colecao;
import br.cristiane.collectionmanager.modelo.Item;
import br.cristiane.collectionmanager.persistencia.ItemsDatabase;

public class CadastrarActivity extends AppCompatActivity {

    public static final String MODO = "MODO";
    public static final String ID = "ID";
    public static final int NOVO = 1;
    public static final int EDITAR = 2;
    private int modo;
    private Item itemOriginal;
    private Spinner spinnerColecao;
    private EditText editTextPersonagem;
    private EditText editTextDataAquisicao;
    private EditText editTextPrecoAquisicao;
    private CheckBox checkBoxDesejo;
    private RadioGroup radioGroupCondicao;
    private RadioButton radioButtonNovo;
    private RadioButton radioButtonUsado;

    public static void novoItem(AppCompatActivity activity, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(activity, CadastrarActivity.class);
        intent.putExtra(MODO, NOVO);
        launcher.launch(intent);
    }

    public static void editarItem(AppCompatActivity activity, ActivityResultLauncher<Intent> launcher, Item item) {
        Intent intent = new Intent(activity, CadastrarActivity.class);
        intent.putExtra(MODO, EDITAR);
        intent.putExtra(ID, item.getId());
        launcher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        spinnerColecao = findViewById(R.id.colecao_nome);
        editTextPersonagem = findViewById(R.id.personagem_nome);
        editTextDataAquisicao = findViewById(R.id.data_aquisicao);
        editTextPrecoAquisicao = findViewById(R.id.preco_aquisicao);
        checkBoxDesejo = findViewById(R.id.desejo);
        radioGroupCondicao = findViewById(R.id.condicao_group);
        radioButtonNovo = findViewById(R.id.novo);
        radioButtonUsado = findViewById(R.id.usado);

        configurarSpinner();
        configurarDatePicker();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            modo = bundle.getInt(MODO, NOVO);

            Log.d("CadastrarActivity", "Modo recebido: " + modo);  // Adicione este log para depuração

            if (modo == NOVO) {
                setTitle(getString(R.string.novo_item));
            } else if (modo == EDITAR) {
                setTitle(getString(R.string.editar_item));
                long id = bundle.getLong(ID);

                ItemsDatabase database = ItemsDatabase.getDatabase(this);
                itemOriginal = database.getItemDao().queryById(id);

                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerColecao.getAdapter();
                spinnerColecao.setSelection(adapter.getPosition(itemOriginal.getColecao().toString()));
                editTextPersonagem.setText(itemOriginal.getPersonagemNome());
                editTextDataAquisicao.setText(itemOriginal.getDataAquisicao());
                editTextPrecoAquisicao.setText(String.valueOf(itemOriginal.getPrecoAquisicao()));
                checkBoxDesejo.setChecked(itemOriginal.isDesejo());
            } else {
                Log.e("CadastrarActivity", "Modo desconhecido: " + modo);  // Adicione este log para depuração
            }
        }
    }

    private void salvar() {
        // Obtém os dados dos campos de entrada
        String personagemNome = editTextPersonagem.getText().toString().trim();
        String dataAquisicao = editTextDataAquisicao.getText().toString().trim();
        String precoAquisicaoStr = editTextPrecoAquisicao.getText().toString().trim();
        boolean desejo = checkBoxDesejo.isChecked();
        int idCondicao = radioGroupCondicao.getCheckedRadioButtonId();
        String condicao = (idCondicao == R.id.novo) ? "Novo" : "Usado";

        // Valida os dados
        if (personagemNome == null) {
            Toast.makeText(this, getString(R.string.nome_do_personagem_obrigatorio), Toast.LENGTH_SHORT).show();
            editTextPersonagem.requestFocus();
            return;
        }
        if (dataAquisicao.isEmpty()) {
            Toast.makeText(this, getString(R.string.data_de_aquisicao_obrigatoria), Toast.LENGTH_SHORT).show();
            editTextDataAquisicao.requestFocus();
            return;
        }
        if (precoAquisicaoStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.preco_de_aquisicao_obrigatorio), Toast.LENGTH_SHORT).show();
            editTextPrecoAquisicao.requestFocus();
            return;
        }
        if (radioGroupCondicao.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.selecione_a_condicao, Toast.LENGTH_SHORT).show();
            radioGroupCondicao.requestFocus();
            return;
        }


        double precoAquisicao;
        try {
            precoAquisicao = Double.parseDouble(precoAquisicaoStr);
        } catch (NumberFormatException e) {
            // Se o preço não puder ser convertido, exiba uma mensagem de erro e saia
            Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        Colecao colecao = Colecao.valueOf(spinnerColecao.getSelectedItem().toString());

        // Comparações para modo EDITAR
        if (modo == EDITAR &&
                personagemNome.equals(itemOriginal.getPersonagemNome()) &&
                dataAquisicao.equals(itemOriginal.getDataAquisicao()) &&
                Math.abs(precoAquisicao - itemOriginal.getPrecoAquisicao()) < 0.0001 && // Usar uma tolerância para comparar doubles
                desejo == itemOriginal.isDesejo() &&
                condicao.equals(itemOriginal.getCondicao()) &&
                Colecao.valueOf(spinnerColecao.getSelectedItem().toString()).equals(itemOriginal.getColecao())) { // Comparar colecao
            cancelar();
            return;
        }
        Intent intent = new Intent();

        ItemsDatabase database = ItemsDatabase.getDatabase(this);

        if (modo == NOVO) {


            // Cria um novo Item com os dados fornecidos
            Item item = new Item(colecao, personagemNome, dataAquisicao, precoAquisicao, desejo, condicao);

            // Insere o item no banco de dados
            long novoId = database.getItemDao().insert(item);

            // Verifica se a inserção foi bem-sucedida
            if (novoId <= 0) {
                Toast.makeText(this, "Erro ao salvar item", Toast.LENGTH_SHORT).show();
                return; // Sai do método se houver um erro
            }

            // Define o ID do item inserido
            item.setId(novoId);

            // Adiciona o ID ao Intent e define o resultado da Activity
            intent.putExtra(ID, item.getId());
            setResult(Activity.RESULT_OK, intent);

        }else{

                Item itemAlterado = new Item(colecao, personagemNome, dataAquisicao, precoAquisicao, desejo, condicao);

                itemAlterado.setId(itemOriginal.getId());

                int quantidadeAlterada = database.getItemDao().update(itemAlterado);



                intent.putExtra(ID, itemAlterado.getId());
            }

            setResult(Activity.RESULT_OK, intent);
            finish();
        }



    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_cadastrar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if (item.getItemId() == R.id.action_salvar) {
            if (validarCampos()) {
                salvar();  // Adiciona um novo item
            }
            return true;
        } else if (item.getItemId() == R.id.action_limpar) {
            limparCampos();
            Toast.makeText(this, R.string.campos_limpos, Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            cancelar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] obterNomesColecao () {
        Colecao[] colecoes = Colecao.values();
        String[] nomes = new String[colecoes.length];
        for (int i = 0; i < colecoes.length; i++) {
            nomes[i] = colecoes[i].name(); // Obtém o nome do enum como uma string
        }
        return nomes;
    }

    private void configurarSpinner () {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, obterNomesColecao());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColecao.setAdapter(adapter);
    }

    private void configurarDatePicker () {
        editTextDataAquisicao.setOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker () {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String data = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    editTextDataAquisicao.setText(data);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
    private void limparCampos () {
        spinnerColecao.setSelection(0);
        editTextPersonagem.setText("");
        editTextDataAquisicao.setText("");
        editTextPrecoAquisicao.setText("");
        checkBoxDesejo.setChecked(false);
        radioGroupCondicao.clearCheck();
    }

    private boolean validarCampos () {
        if (isCampoVazio(editTextPersonagem, getString(R.string.nome_do_personagem_obrigatorio)))
            return false;
        if (isCampoVazio(editTextDataAquisicao, getString(R.string.data_de_aquisicao_obrigatoria)))
            return false;
        if (isCampoVazio(editTextPrecoAquisicao, getString(R.string.preco_de_aquisicao_obrigatorio)))
            return false;
        if (radioGroupCondicao.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.selecione_a_condicao, Toast.LENGTH_SHORT).show();
            radioGroupCondicao.requestFocus();
            return false;
        }
        return true;
    }
    private boolean isCampoVazio (EditText campo, String mensagemErro){
        if (campo.getText().toString().trim().isEmpty()) {
            campo.setError(mensagemErro);
            campo.requestFocus();
            return true;
        }
        return false;
    }



    public void cancelar(){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}


