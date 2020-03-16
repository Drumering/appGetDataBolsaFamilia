package com.example.exerciciosandroidopet;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editMunicipio;
    private List<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textName);
        editMunicipio = findViewById(R.id.editMunicipio);
        names = new ArrayList<>();
    }

    public void btnCarregarEvent(View v){
        carregarDados();
    }

    public void btnCarregarIBGEEvent(View v) {
        carregarCodigoIBGE();
    }

    private void generateRequest(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if (response.has("results")) {
                    try {
                        JSONArray array = response.getJSONArray("results");

                        for(int i = 0; i < array.length(); i++){
                            names.add(array.getJSONObject(i).getString("name"));
                        }

                        if (response.has("next")) {
                            String url = response.getString("next");
                            if(!url.equals("null")){
                                Log.i("VOLLEY",url);
                                generateRequest(url);
                            } else {
                                Log.i("VOLLEY","ENCERRADO");
                                showData();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) { }
        });

        APISingleton.getInstance(this).addToRequestQueue(request);
    }

    private void generateRequestIBGE(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            editMunicipio.setText(response.get("id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) { }
        });

        APISingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void showData() {
        StringBuilder names_str = new StringBuilder();
        for (String name : names) {
            names_str.append(name).append("\n");
        }

        textView.setText(names_str.toString());
    }

    private void carregarDados() {
        String endpoint = "http://www.transparencia.gov.br/api-de-dados/bolsa-familia-por-municipio?";
        generateRequest(endpoint);
    }

    private void carregarCodigoIBGE() {
        String cidade = editMunicipio.getText().toString().replace(' ', '-');

        String endpoint = "https://servicodados.ibge.gov.br/api/v1/localidades/municipios/" +
                cidade;
        generateRequestIBGE(endpoint);
    }
}
