package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;
import com.hemerson.FujaSePuder.entidades.ProjetilInimigo;

/** Objetos que giram em volta do heroi e ferem quem encostam (Lamina Giratoria, Escudo de Ossos). */
public class PoderOrbital extends Poder {

    public int quantidade = 1;
    public int quantidadeNv5 = -1;
    public float raioOrbita = 55f;
    public float velocidadeAngular = 4f;    // radianos por segundo
    public float tamanho = 12f;
    public float danoBase = 10f;
    public float imunidade = 0.5f;          // tempo ate acertar o mesmo monstro de novo
    public boolean bloqueiaTiros = false;   // Escudo de Ossos
    public Color cor = Color.WHITE;

    private float angulo = 0f;
    private float relogio = 0f;
    private final ObjectFloatMap<Monstro> ultimoAcerto = new ObjectFloatMap<>();

    public PoderOrbital(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return 1f; }
    @Override protected boolean usar(Heroi h, Array<Monstro> m, Array<Projetil> p) { return false; }

    private int qtd() { return nivel5() && quantidadeNv5 > 0 ? quantidadeNv5 : quantidade; }

    @Override
    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        relogio += delta;
        angulo += velocidadeAngular / fatorRecarga(heroi) * delta;
        float d = dano(heroi, danoBase);
        int n = qtd();
        float r = alcance(heroi, raioOrbita);

        for (int k = 0; k < n; k++) {
            float a = angulo + k * MathUtils.PI2 / n;
            float x = heroi.posicao.x + MathUtils.cos(a) * r;
            float y = heroi.posicao.y + MathUtils.sin(a) * r;
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (!m.estaVivo() || Vector2.dst(x, y, m.posicao.x, m.posicao.y) > tamanho + m.tamanho / 2f) continue;
                float ultimo = ultimoAcerto.get(m, -999f);
                if (relogio - ultimo >= imunidade) {
                    ferir(heroi, m, d);
                    ultimoAcerto.put(m, relogio);
                }
            }
            if (bloqueiaTiros && ctx != null) {
                Array<ProjetilInimigo> tiros = ctx.projeteisInimigos;
                for (int i = tiros.size - 1; i >= 0; i--) {
                    if (Vector2.dst(x, y, tiros.get(i).posicao.x, tiros.get(i).posicao.y) < tamanho + tiros.get(i).raio) {
                        tiros.removeIndex(i);
                    }
                }
            }
        }
        // limpeza dos monstros que ja morreram
        if (ultimoAcerto.size > 200) ultimoAcerto.clear();
    }

    @Override
    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        int n = qtd();
        float r = alcance(heroi, raioOrbita);
        for (int k = 0; k < n; k++) {
            float a = angulo + k * MathUtils.PI2 / n;
            float x = heroi.posicao.x + MathUtils.cos(a) * r;
            float y = heroi.posicao.y + MathUtils.sin(a) * r;
            s.setColor(cor);
            s.circle(x, y, tamanho);
            s.setColor(Color.DARK_GRAY);
            s.circle(x, y, tamanho * 0.35f);
        }
    }
}
