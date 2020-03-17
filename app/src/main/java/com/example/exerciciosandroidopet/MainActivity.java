package com.example.exerciciosandroidopet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.exerciciosandroidopet.Constants.API_DADOS_SITE;
import static com.example.exerciciosandroidopet.Constants.IBGE_SITE;
import static com.example.exerciciosandroidopet.Constants.MGS_ERRO_ANO_VAZIO;
import static com.example.exerciciosandroidopet.Constants.MGS_ERRO_CONSULTA;

public class MainActivity extends AppCompatActivity {

    private TextView cidade, estado, total, media, maiorValor, menorValor;
    private EditText editMunicipio, editYear;
    private Double valorTotal, valorMedia, valorMaior, valorMenor;
    private String nomeCidade, UF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cidade = findViewById(R.id.cidade);
        estado = findViewById(R.id.estado);
        total = findViewById(R.id.total);
        media = findViewById(R.id.media);
        maiorValor = findViewById(R.id.maiorValor);
        menorValor = findViewById(R.id.menorValor);

        editMunicipio = findViewById(R.id.editMunicipio);
        editYear = findViewById(R.id.editYear);
    }

    public void btnCarregarIBGEEvent(View v) {
        carregarCodigoIBGE();
    }

    private void carregarCodigoIBGE() {
        String cidade = editMunicipio.getText().toString().replace(' ', '-');

        String endpoint = IBGE_SITE + cidade;
        generateRequest(endpoint, 1);
    }

    public void btnCarregarEvent(View v){
        carregarDados(v);
    }

    private void carregarDados(View view) {
        String codigoIbge = editMunicipio.getText().toString();

        if (validarDados(view, codigoIbge)) {
            limparValoresAntigos();
            loopRequest(codigoIbge);
        }
    }

    private void loopRequest(String codigoIbge) {
        for (int i = 1; i <= 12; i++) {
            String mes = validarMes(i);

            String dataConsulta = editYear.getText().toString() + mes;
            String endpoint = String.format(API_DADOS_SITE + "?mesAno=%s&codigoIbge=%s&pagina=1",
                    dataConsulta, codigoIbge
            );

            generateRequest(endpoint, 0);
        }
    }

    private void generateRequest(String url, int operacao) {
        if (operacao == 0) {
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject dataObject = response.getJSONObject(0);
                                buscarValoresObjJson(dataObject);
                                setTextosView();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) { }
            });

            APISingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        } else if (operacao == 1) {
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
    }

    private void buscarValoresObjJson(JSONObject dataObject) throws JSONException {
        valorTotal += Double.parseDouble(dataObject.getString("valor"));
        valorMedia += Double.parseDouble(dataObject.getString("quantidadeBeneficiados"));
        nomeCidade = dataObject.getJSONObject("municipio").getString("nomeIBGE");
        UF = dataObject.getJSONObject("municipio").getJSONObject("uf").getString("nome");

        double valorMensal = Double.parseDouble(dataObject.getString("valor"));
        if(valorMaior <= valorMensal) {
            valorMaior = valorMensal;
        }
        if (valorMenor >= valorMensal) {
            valorMenor = valorMensal;
        }
    }

    private boolean validarDados(View view, String codigoIbge) {
        boolean municipioVazio = editMunicipio.getText().toString().trim().equals("");
        boolean anoVazio = editYear.getText().toString().trim().equals("");

        if (!TextUtils.isDigitsOnly(codigoIbge) || municipioVazio) {
            Snackbar snackBar = Snackbar.make(view, MGS_ERRO_CONSULTA, Snackbar.LENGTH_SHORT);
            snackBar.show();
            return false;

        } else if (anoVazio) {
            Snackbar snackBar = Snackbar.make(view, MGS_ERRO_ANO_VAZIO, Snackbar.LENGTH_SHORT);
            snackBar.show();
            return false;

        } else {
            return true;
        }
    }

    private void setTextosView() {
        cidade.setText(nomeCidade);
        estado.setText(UF);
        total.setText(String.format("%.2f", valorTotal));
        media.setText(String.format("%.2f", valorMedia / 12));
        maiorValor.setText(String.format("%.2f", valorMaior));
        menorValor.setText(String.format("%.2f", valorMenor));
    }

    private void limparValoresAntigos() {
        valorTotal = 0.0;
        valorMedia = 0.0;
        valorMaior = -9999999999.0;
        valorMenor = 99999999999.0;
    }

    private String validarMes(int i) {
        String mes;

        if (i < 10) {
            mes = "0" + i;
        } else {
            mes = Integer.toString(i);
        }

        return mes;
    }
}
