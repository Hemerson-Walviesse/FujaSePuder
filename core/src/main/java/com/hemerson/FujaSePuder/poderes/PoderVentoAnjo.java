package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** ANJO - Vento: laminas que giram em volta do heroi (2 no Nv1, +1 por nivel, maximo 5). */
public class PoderVentoAnjo extends Poder {
    private static final float RAIO_LAMINA = 12f;
    private float angulo = 0f;

    public PoderVentoAnjo() { super(TipoPoder.VENTO_ANJO); }

    @Override protected float intervaloBase() { return 0.5f; } // tempo ate acertar o mesmo monstro de novo
    @Override protected boolean usar(Heroi h, Array<Monstro> m, Array<Projetil> p) { return false; }

    private int quantidadeLaminas() { return Math.min(5, nivel + 1); }
    private float raioOrbita(Heroi heroi) { return alcance(heroi, 70f); }

    @Override
    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        angulo += (3f / fatorRecarga(heroi)) * (nivel5() ? 2f : 1f) * delta; // gira mais rapido com buffs de velocidade de ataque
        float d = dano(heroi, 8f);
        int n = quantidadeLaminas();
        float raio = raioOrbita(heroi);

        for (int k = 0; k < n; k++) {
            float a = angulo + k * MathUtils.PI2 / n;
            float lx = heroi.posicao.x + MathUtils.cos(a) * raio;
            float ly = heroi.posicao.y + MathUtils.sin(a) * raio;
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m.estaVivo() && m.tempoImuneOrbita <= 0
                    && Vector2.dst(lx, ly, m.posicao.x, m.posicao.y) < RAIO_LAMINA + m.tamanho / 2f) {
                    ferir(heroi, m, d);
                    m.tempoImuneOrbita = intervaloBase();
                }
            }
        }
    }

    @Override
    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        int n = quantidadeLaminas();
        float raio = raioOrbita(heroi);
        for (int k = 0; k < n; k++) {
            float a = angulo + k * MathUtils.PI2 / n;
            float lx = heroi.posicao.x + MathUtils.cos(a) * raio;
            float ly = heroi.posicao.y + MathUtils.sin(a) * raio;
            s.setColor(tipo.cor);
            s.circle(lx, ly, RAIO_LAMINA);
            s.setColor(Color.WHITE);
            s.circle(lx, ly, RAIO_LAMINA * 0.4f);
        }
    }
}
