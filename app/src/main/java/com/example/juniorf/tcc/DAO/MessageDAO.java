package com.example.juniorf.tcc.DAO;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.juniorf.tcc.CONFIG.AppController;
import com.example.juniorf.tcc.MODEL.Mensagem;
import com.example.juniorf.tcc.CONSTANTS.UtilMethods;
import com.example.juniorf.tcc.CONSTANTS.Codes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by juniorf on 09/06/17.
 */

public class MessageDAO extends  AbstractDAO<Mensagem> {

    private String urlJsonInsertLocation = "http://grainmapey.pe.hu/GranMapey/insert_message.php";
    private ProgressDialog pDialog;
    private String Tag = "Akii";
    private List<String> lista = new ArrayList<String>();
    private String jsonResponse;
    private String urlJsonUpdateMessage = "http://grainmapey.pe.hu/GranMapey/update_message.php";
    private String urlJsonUpdateAnswer = "http://grainmapey.pe.hu/GranMapey/update_answer.php";
    private String urlJsonInsertAnswer = "http://grainmapey.pe.hu/GranMapey/insert_answer.php";

    public MessageDAO(Context context) {
        super(context);

    }

    public void findOrigemByDestinoAndMessageAndLocal(final String destino, final String message, final String local, final Context t){
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        final String[] origem = {""};
        StringRequest request = new StringRequest(Request.Method.POST, "http://grainmapey.pe.hu/GranMapey/find_emailOrigem_message.php", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray ja = new JSONArray(response);
                    JSONObject jo = (JSONObject) ja.get(0);
                    origem[0] = jo.getString(("emailOrigem"));
                    delete(message, origem[0],destino,local,t);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERRO", error+"r");
                UtilMethods.error(t);
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("message", message);
                parameters.put("local", local);
                parameters.put("emailDestino", destino);
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
    }

    

    public void insert(final Mensagem location, final Context t) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        StringRequest request = new StringRequest(Request.Method.POST, urlJsonInsertLocation, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(Tag, response+"a");
                Toast.makeText(t, "Menssagem inserida com sucesso!", Toast.LENGTH_SHORT).show();
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(Tag, error+"r");
                UtilMethods.error(t);
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("emailOrigem", String.valueOf(location.getEmailOrigem()));
                parameters.put("emailDestino", String.valueOf(location.getEmailDestino()));
                parameters.put("message", location.getTexto());
                parameters.put("local", location.getLocal());
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
    }

    public void update(final int id , final String msg, final Context t){
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        StringRequest request = new StringRequest(Request.Method.POST, urlJsonUpdateMessage, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPONSE_update", response + "kk");
                Toast.makeText(t, "Mensagem atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(Tag, error+"r");
                UtilMethods.error(t);
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("id", String.valueOf(id));
                parameters.put("texto", msg);
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);

    }

    public List<String> show(final Mensagem mensagem, final Context c){
        Map<String, String> params = new HashMap();
        params.put("emailDestino", mensagem.getEmailDestino());
        JSONObject parameters = new JSONObject(params);
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST, "http://grainmapey.pe.hu/GranMapey/show_message.php", parameters, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    jsonResponse = "";
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject person = (JSONObject) response.get(i);

                        Mensagem mensagemz = new Mensagem();
                        String id = person.getString("id");
                        String emailOrigem = person.getString("emailOrigem");
                        String emailDestino = person.getString("emailDestino");
                        String message = person.getString("message");
                        mensagemz.setId(Integer.parseInt(id));
                        mensagemz.setEmailOrigem(emailOrigem);
                        mensagemz.setEmailDestino(emailDestino);
                        mensagemz.setTexto(message);
                        mensagemz.setLocal(message);
                        lista.add(message);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                UtilMethods.error(c);
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
        return this.lista;
    }

    public void delete(final String message, final String emailOrigem, final String emailDestino, final String local, final Context t){
        Log.i("delete", message+ " " + emailOrigem+ " "+emailDestino);
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        StringRequest request = new StringRequest(Request.Method.POST, "http://grainmapey.pe.hu/GranMapey/delete_message.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(t, "Mensagem deletada com sucesso!", Toast.LENGTH_SHORT).show();
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERRO", error+"r");
                UtilMethods.error(t);
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("message", message );
                parameters.put("emailOrigem", emailOrigem );
                parameters.put("emailDestino", emailDestino );
                parameters.put("local", local);

                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
    }

    public void insertAnswer(final long idMessage, final String emailDestino, final String emailOrigem, final String answer, final Context t){
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        StringRequest request = new StringRequest(Request.Method.POST, urlJsonInsertAnswer, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPONSE_ANSWER", response+"a");
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(Tag, error+"r");
                UtilMethods.error(t);
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("emailOrigem",emailOrigem);
                parameters.put("idMessage", String.valueOf(idMessage));
                parameters.put("emailDestino", emailDestino);
                parameters.put("message", answer);
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
    }

    public void deleteAnswer(final Integer id, final Context t) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        StringRequest request = new StringRequest(Request.Method.POST, "http://grainmapey.pe.hu/GranMapey/delete_answer.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPONSE_DELETE", "+"+response);
                Toast.makeText(t, "Resposta deletada com sucesso!", Toast.LENGTH_SHORT).show();
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERRO", error+"r");
                UtilMethods.error(t);
            }
        })

        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("id", String.valueOf(id));

                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
    }

    public void updateAnswer(final Integer id, final String message, final Context t ){
        RequestQueue mRequestQueue = Volley.newRequestQueue(t);
        StringRequest request = new StringRequest(Request.Method.POST, urlJsonUpdateAnswer, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPONSE_update", response + "kk");
                Toast.makeText(t, "Resposta atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            }
        },  new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(Tag, error+"r");
                UtilMethods.error(t);
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String , String>();
                parameters.put("id", String.valueOf(id));
                parameters.put("texto", message);
                return parameters;
            }
        };
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(request);
    }


}

