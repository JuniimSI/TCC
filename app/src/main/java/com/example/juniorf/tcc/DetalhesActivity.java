package com.example.juniorf.tcc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.juniorf.tcc.ADAPTER.MessageAdapter;
import com.example.juniorf.tcc.CONFIG.AppController;
import com.example.juniorf.tcc.DAO.MessageDAO;
import com.example.juniorf.tcc.DAO.MyLocationDAO;
import com.example.juniorf.tcc.MAPS.MapsActivity;
import com.example.juniorf.tcc.MODEL.Mensagem;
import com.example.juniorf.tcc.MODEL.MyLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalhesActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private TextView nomeText;
    private TextView telefoneText;
    private TextView ratingText;
    private EditText message;
    private ExpandableListView listView;
    private Button btnSend;
    private ProgressDialog progressDetails;
    private ProgressDialog progressAnswer;
    private ProgressDialog progressMessages;
    private ProgressDialog progressInsert;
    private ProgressDialog progressDelete;
    private ProgressDialog progressUpdate;
    private ProgressDialog progressDeleteMarker;

    private String emailDestino;
    private String emailOrigem;
    private String typeToken;
    private String lat;
    private String localToken;
    private String lng;
    private String placeId;

    private MessageAdapter adapter;
    private List<Mensagem> lista;
    private HashMap<Mensagem, List<Mensagem>> respostas;

    private MyLocation myLocation;
    private Mensagem mensagemSelecionada;
    private Mensagem answerSelecionada;
    private long idMensagemSelecionada;

    private GoogleApiClient apiClient;
    private String urlJsonDetailsLocation = "http://grainmapey.pe.hu/GranMapey/details_location.php";

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public List<Mensagem> show(final Mensagem mensagem, final Context c){
        showProgressMessages();
        lista = new ArrayList<Mensagem>();
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, "http://grainmapey.pe.hu/GranMapey/show_message.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject person = (JSONObject) jsonArray.get(i);

                        Mensagem mensagemz = new Mensagem();
                        String id = person.getString("id");
                        String emailOrigem = person.getString("emailOrigem");
                        String emailDestino = person.getString("emailDestino");
                        String message = person.getString("message");
                        String local = person.getString("local");
                        String answer = person.getString("answer");
                        mensagemz.setId(Integer.parseInt(id));
                        mensagemz.setEmailOrigem(emailOrigem);
                        mensagemz.setEmailDestino(emailDestino);
                        mensagemz.setTexto(message);
                        mensagemz.setLocal(local);
                        mensagemz.setAnswer(Boolean.parseBoolean(answer));
                        lista.add(mensagemz);
                        showAnswers(mensagemz);
                    }
		            showList();
                    hideProgressMessages();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(c, "Erro= "+error, Toast.LENGTH_SHORT).show();
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("emailDestino", mensagem.getEmailDestino());
                parameters.put("local", mensagem.getLocal());
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
        hideProgressMessages();
        return this.lista;
    }

    private void showList() {
       adapter = new MessageAdapter(this, this.lista, respostas);
        listView.setAdapter(adapter);
    }

    private void showAnswers(final Mensagem m){
        showProgressAnswer();
	final List<Mensagem> answers = new ArrayList<Mensagem>();
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, "http://grainmapey.pe.hu/GranMapey/show_answer.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject person = (JSONObject) jsonArray.get(i);

                        Mensagem mensagemz = new Mensagem();
                        String id = person.getString("id");
                        String emailOrigem = person.getString("emailOrigem");
                        String emailDestino = person.getString("emailDestino");
                        String message = person.getString("message");
                        mensagemz.setId(Integer.parseInt(id));
                        mensagemz.setAnswer(true);
                        mensagemz.setEmailOrigem(emailOrigem);
                        mensagemz.setEmailDestino(emailDestino);
                        mensagemz.setTexto(message);
                        mensagemz.setLocal(message);
                        answers.add(mensagemz);

                    }
		            respostas.put(m, answers);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DetalhesActivity.this, "Erro= "+error, Toast.LENGTH_SHORT).show();
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("idMessage", String.valueOf(m.getId()));
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
        hideProgressAnswer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);
        respostas = new HashMap<Mensagem, List<Mensagem>>();



        /////////////////Toolbar///////////////////////////
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        

        ///////////////////Progress////////////////////////
        progressDetails = new ProgressDialog(this);
        progressDetails.setMessage("Recebendo os detalhes...");
        progressDetails.setCancelable(true);

        progressInsert = new ProgressDialog(this);
        progressInsert.setMessage("Inserindo mensagem...");
        progressInsert.setCancelable(true);

        progressDeleteMarker = new ProgressDialog(this);
        progressDeleteMarker.setMessage("Deletando marker");
        progressDeleteMarker.setCancelable(true);

        progressUpdate = new ProgressDialog(this);
        progressUpdate.setMessage("Atualizando mensagem...");
        progressUpdate.setCancelable(true);

        progressDelete = new ProgressDialog(this);
        progressDelete.setMessage("Apagando mensagem...");
        progressDelete.setCancelable(true);

        progressAnswer = new ProgressDialog(this);
        progressAnswer.setMessage("Carregando respostas...");
        progressAnswer.setCancelable(true);

        progressMessages = new ProgressDialog(this);
        progressMessages.setMessage("Carregando mensagens...");
        progressMessages.setCancelable(true);

        ///Do intent bundle da main
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        emailDestino = bundle.getString("emailDestino");
        Toast.makeText(DetalhesActivity.this, "emailDestino!"+emailDestino, Toast.LENGTH_SHORT).show();
        emailOrigem = bundle.getString("emailOrigem");
        Toast.makeText(DetalhesActivity.this, "emailOrigem!"+emailOrigem, Toast.LENGTH_SHORT).show();
        typeToken = bundle.getString("typeToken");
        placeId = bundle.getString("place_id");
        lat = bundle.getString("lat");
        lng = bundle.getString("lng");
        localToken = bundle.getString("local");

        //Dados da activity
        message = (EditText) findViewById(R.id.etMessage);
        listView = (ExpandableListView) findViewById(R.id.listView);
        nomeText = (TextView) findViewById(R.id.nomeText);
        telefoneText = (TextView) findViewById(R.id.telefoneText);
        ratingText = (TextView) findViewById(R.id.ratingText);

        registerForContextMenu(listView);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    int pai = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    if(childPosition==-1){
                        mensagemSelecionada = adapter.getGroup(pai);
                    }else{
                        idMensagemSelecionada = ( adapter.getGroup(pai)).getId();
                        mensagemSelecionada = adapter.getChild(pai, childPosition);
                    }

                return false;
            }

        });
        btnSend = (Button) findViewById(R.id.btnSend);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailOrigem!=null && !emailOrigem.equals("null")) {
                    Mensagem n = new Mensagem();
                    String text = message.getText().toString();
                    n.setTexto(text);
                    n.setEmailOrigem(emailOrigem);
                    n.setEmailDestino(emailDestino);
                    n.setLocal(localToken);
                    MessageDAO messageDAO = new MessageDAO(getApplicationContext());
                    messageDAO.insert(n, getApplicationContext());
                    Toast.makeText(DetalhesActivity.this, "Menssagem inserida com sucesso!", Toast.LENGTH_SHORT).show();
                    updateActivity();
                }else{
                    Toast.makeText(DetalhesActivity.this, "Faça o login.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //////////////////GOOGLE API//////////////////
        apiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        apiClient.connect();

        ////////////////////////Detalhes//////////////////////

        if(!existArroba(placeId)) {
            myLocation = detailsSearch(placeId);
        }else{
            MyLocationDAO mDAO = new MyLocationDAO(getApplicationContext());
            myLocation = findLocationById(Integer.parseInt(placeId));

        }
        //////////////////ATUALIZANDO LISTVIEW//////////////////
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detalhes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            if(emailOrigem!=null && emailDestino!=null) {
                
                if (emailDestino.equals(emailOrigem)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Deseja realmente excluir este ponto?").setTitle("Confirmação do delete");
                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                    MyLocationDAO locationDAO = new MyLocationDAO(getApplicationContext());
                                        if (locationDAO.delete(lat, lng, getApplicationContext())) {
                                            Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("emailOrigem", (emailOrigem));
                                            i.putExtras(bundle);
                                            Toast.makeText(DetalhesActivity.this, "Deletado", Toast.LENGTH_SHORT).show();
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(DetalhesActivity.this, "Erro no delete, Tente outra vez..", Toast.LENGTH_SHORT).show();
                                            hideProgressDeleteMarker();
                                        }

                               }
                           });
                    builder.setNegativeButton("Não", null);

                // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    
                } else {
                    Toast.makeText(DetalhesActivity.this, "Você não tem permissão para deletar esse ponto", Toast.LENGTH_SHORT).show();
                    hideProgressDeleteMarker();

                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageDAO mDAO = new MessageDAO(this);
        Mensagem n = new Mensagem();
        n.setEmailDestino(emailDestino);
        n.setLocal(localToken);
        show(n, this);
    }

    public void insertAnswer(){


        if(emailOrigem!=null && !emailOrigem.equals("null")){
        showProgressInsert();
        final String[] n = {"", ""};

        LayoutInflater layoutInflater = LayoutInflater.from(DetalhesActivity.this);
        View pront = layoutInflater.inflate(R.layout.insert_answer, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(DetalhesActivity.this);
        alertDialogBuilder.setView(pront);

        final EditText nome = (EditText) pront.findViewById(R.id.textAnswer);

        alertDialogBuilder.setCancelable(false).setPositiveButton("Responder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                n[0] = nome.getText().toString();
                MessageDAO mDAO = new MessageDAO(getApplicationContext());
                mDAO.insertAnswer(mensagemSelecionada.getId(), emailDestino, emailOrigem, n[0], getApplicationContext());

                Toast.makeText(DetalhesActivity.this, "Resposta inserida com sucesso!", Toast.LENGTH_SHORT).show();
                updateActivity();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        hideProgressInsert();
        }else{
            Toast.makeText(DetalhesActivity.this, "Faça login para obter esta funcionalidade!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateActivity() {
        Intent i = new Intent(DetalhesActivity.this,DetalhesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("emailOrigem", emailOrigem);
        bundle.putString("emailDestino", emailDestino);
        bundle.putString("lat", lat);
        bundle.putString("lng", lng);
        bundle.putString("typeToken", typeToken);
        bundle.putString("place_id", placeId);
        bundle.putString("local", localToken);
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }

    public void updateMessage(String msg){
        final String[] n = {"", ""};

        LayoutInflater layoutInflater = LayoutInflater.from(DetalhesActivity.this);
        View pront = layoutInflater.inflate(R.layout.update_message, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(DetalhesActivity.this);
        alertDialogBuilder.setView(pront);

        final EditText nome = (EditText) pront.findViewById(R.id.textUpdateMessage);
        nome.setText(msg);

        alertDialogBuilder.setCancelable(false).setPositiveButton("Atualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                n[0] = nome.getText().toString();
                MessageDAO mDAO = new MessageDAO(getApplicationContext());
                mDAO.update(mensagemSelecionada.getId(), n[0],getApplicationContext());
                updateActivity();
                Toast.makeText(DetalhesActivity.this, "Mensagem atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void deleteMessage(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Confirma a exclusão desta mensagem?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MessageDAO messageDAO = new MessageDAO(getApplicationContext());
                messageDAO.findOrigemByDestinoAndMessageAndLocal(mensagemSelecionada.getEmailDestino(),mensagemSelecionada.getTexto(),
                        mensagemSelecionada.getLocal(), getApplicationContext());
                mensagemSelecionada = null;
                updateActivity();
            }
        });
        builder.setNegativeButton("Não", null);
        AlertDialog alert = builder.create();
        alert.setTitle("Confirmação de exclusão");
        alert.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_message, menu);
    }

    public MyLocation findLocationById(final int id){
        progressDetails.show();
        //Toast.makeText(this, "id+"+id, Toast.LENGTH_SHORT).show();
        final MyLocation[] myLocation = new MyLocation[1];
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, urlJsonDetailsLocation, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject person = (JSONObject) jsonArray.get(i);
                        myLocation[0] = new MyLocation();
                        String id = person.getString("id");
                        String nome = person.getString("Nome");
                        String telefone = person.getString("Telefone");
                        String lat = person.getString("Lat");
                        String lng = person.getString("Lng");
                        String tipo = person.getString("Tipo");
                        String email = person.getString("Email");
                        myLocation[0].setId(Integer.parseInt(id));
                        myLocation[0].setTipo(tipo);
                        myLocation[0].setNome(nome);
                        myLocation[0].setEmail(email);
                        myLocation[0].setTelefone(telefone);
                        myLocation[0].setLat(Double.parseDouble(lat));
                        myLocation[0].setLng(Double.parseDouble(lng));
                    }
                    nomeText.setText("Nome: "+myLocation[0].getNome());
                    telefoneText.setText("Telefone: "+myLocation[0].getTelefone());
                    ratingText.setText("Tipo: "+myLocation[0].getTipo());
                    progressDetails.dismiss();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("id", String.valueOf(id));
                return parameters;
            }
        };
        progressDetails.dismiss();
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
        return myLocation[0];
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                if (!emailOrigem.equals("null") && (emailOrigem.equals(mensagemSelecionada.getEmailDestino()) || emailOrigem.equals(mensagemSelecionada.getEmailOrigem()))){
                    if (mensagemSelecionada.isAnswer()) {
                        answerSelecionada = mensagemSelecionada;
                        updateAnswer(answerSelecionada.getTexto());
                    } else {
                        updateMessage(mensagemSelecionada.getTexto());
                    }
                }else{
                    Toast.makeText(this, "Você não tem permissão", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_delete:
                if (((emailOrigem != null && !emailOrigem.equals("null")) && (emailOrigem.equals(mensagemSelecionada.getEmailDestino())) || emailOrigem.equals(mensagemSelecionada.getEmailOrigem()))) {
                    if (mensagemSelecionada.isAnswer()) {
                        answerSelecionada = mensagemSelecionada;
                        deleteAnswer();
                    } else {
                        deleteMessage();
                    }
                }else{
                    Toast.makeText(this, "Você não tem permissão", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_answer:
                if((emailOrigem != null && !emailOrigem.equals("null"))) {
                    if (mensagemSelecionada.isAnswer()) {
                        answerSelecionada = mensagemSelecionada;
                        insertAnswerAnswer(idMensagemSelecionada);
                    } else {
                        insertAnswer();
                    }
                }else{
                    Toast.makeText(this, "Você não tem permissão", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void insertAnswerAnswer(final long idMensagemSelecionada) {
        showProgressInsert();
        final String[] n = {"", ""};

        LayoutInflater layoutInflater = LayoutInflater.from(DetalhesActivity.this);
        View pront = layoutInflater.inflate(R.layout.insert_answer, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(DetalhesActivity.this);
        alertDialogBuilder.setView(pront);

        final EditText nome = (EditText) pront.findViewById(R.id.textAnswer);

        alertDialogBuilder.setCancelable(false).setPositiveButton("Responder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                n[0] = nome.getText().toString();
                MessageDAO mDAO = new MessageDAO(getApplicationContext());
                mDAO.insertAnswer(idMensagemSelecionada, emailDestino, emailOrigem, n[0], getApplicationContext());
                updateActivity();
                Toast.makeText(DetalhesActivity.this, "Resposta inserida com sucesso!", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        hideProgressInsert();
    }

    private void updateAnswer(String texto) {
        showProgressUpdate();
        final String[] n = {"", ""};

        LayoutInflater layoutInflater = LayoutInflater.from(DetalhesActivity.this);
        View pront = layoutInflater.inflate(R.layout.update_message, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(DetalhesActivity.this);
        alertDialogBuilder.setView(pront);

        final EditText nome = (EditText) pront.findViewById(R.id.textUpdateMessage);
        nome.setText(texto);

        alertDialogBuilder.setCancelable(false).setPositiveButton("Atualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                n[0] = nome.getText().toString();
                MessageDAO mDAO = new MessageDAO(getApplicationContext());
                mDAO.updateAnswer(mensagemSelecionada.getId(), n[0],getApplicationContext());
                updateActivity();

                Toast.makeText(DetalhesActivity.this, "Resposta atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                return;
            }
        });
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        hideProgressUpdate();
    }

    public void deleteAnswer(){
        showProgressDelete();
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Confirma a exclusão desta resposta?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MessageDAO messageDAO = new MessageDAO(getApplicationContext());
                messageDAO.deleteAnswer(answerSelecionada.getId(), getApplicationContext());
                answerSelecionada = null;
                updateActivity();
            }
        });
        builder.setNegativeButton("Não", null);
        AlertDialog alert = builder.create();
        alert.setTitle("Confirmação de exclusão");
        alert.show();
        hideProgressDelete();
    }

    private MyLocation detailsSearch(String placeId){
        progressDetails.show();
        final MyLocation ml = new MyLocation();
        Places.GeoDataApi.getPlaceById(apiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            ml.setNome(String.valueOf(myPlace.getName()));
                            ml.setTelefone(String.valueOf(myPlace.getPhoneNumber()));
                            ml.setEmail(String.valueOf(myPlace.getAttributions()));
                            ml.setTipo(String.valueOf(myPlace.getRating()));
                            nomeText.setText("Nome: "+ml.getNome());
                            telefoneText.setText("Telefone: "+ml.getTelefone());
                            ratingText.setText("Nota: "+ml.getTipo());
                        } else {
                            Toast.makeText(DetalhesActivity.this, "Falha na requisição dos detalhes!", Toast.LENGTH_SHORT).show();
                        }
                        places.release();
                    }
                });
        progressDetails.dismiss();
        return ml;
    }
    private void showProgressAnswer() {
        if (!progressAnswer.isShowing())
            progressAnswer.show();
    }
    private void hideProgressAnswer() {
        if (progressAnswer.isShowing())
            progressAnswer.dismiss();
    }
    private void showProgressMessages() {
        if (!progressMessages.isShowing())
            progressMessages.show();
    }
    private  void showProgressDeleteMarker(){
        if(!progressDeleteMarker.isShowing())
            progressDeleteMarker.show();
    }
    private void hideProgressDeleteMarker(){
        if(progressDeleteMarker.isShowing())
            progressDeleteMarker.dismiss();
    }
    private void hideProgressMessages() {
        if (progressMessages.isShowing())
            progressMessages.dismiss();
    }
    private void showProgressInsert() {
        if (!progressInsert.isShowing())
            progressInsert.show();
    }
    private void hideProgressInsert() {
        if (progressInsert.isShowing())
            progressInsert.dismiss();
    }
    private void showProgressUpdate() {
        if (!progressUpdate.isShowing())
            progressUpdate.show();
    }
    private void hideProgressUpdate() {
        if (progressUpdate.isShowing())
            progressUpdate.dismiss();
    }
    private void showProgressDelete() {
        if (!progressDelete.isShowing())
            progressDelete.show();
    }
    private void hideProgressDelete() {
        if (progressDelete.isShowing())
            progressDelete.dismiss();
    }

    public boolean existArroba(String frase){
        if (frase == null || ! frase.matches("^[0-9]{1,8}$"))
            return false;
        return true;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        apiClient.connect();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}