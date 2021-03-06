package com.example.juniorf.tcc.TUTORIAL;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.example.juniorf.tcc.MainActivity;
import com.example.juniorf.tcc.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class TutorialActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Crie seus pontos", "Para criar seu ponto de interesse, toque e segure o local onde seu ponto deve ser cadastrado e preencha o formulário", R.drawable.insira_seu_ponto, Color.parseColor("#5472AE")));
        addSlide(AppIntroFragment.newInstance("Edite seus pontos", "Para editar a localização de um ponto de interesse seu, basta clicar, segurar e arrastar", R.drawable.edite_seu_ponto, Color.parseColor("#5472AE")));
        addSlide(AppIntroFragment.newInstance("Apague seus pontos", "Para apagar seu ponto de interesse, basta dar um clique no icone de lixeira na tela de detalhes de tal ponto", R.drawable.clique_para_apagar, Color.parseColor("#5472AE")));


        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

    }

    @Override
    public void onSkipPressed() {

        Intent n = new Intent(this, TutorialActivity2.class);
        n.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(n);

    }

    @Override
    public void onSlideChanged() {
        super.onSlideChanged();
    }

    @Override
    public void onDonePressed() {
        Intent intent =new Intent(this,TutorialActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        //android.widget.Toast.makeText(this, "Antes do finish", Toast.LENGTH_SHORT).show();
        finish();
    }
}
