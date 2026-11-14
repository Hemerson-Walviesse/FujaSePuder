package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** ANJO - Raio: acerta o mais proximo e ricocheteia (3 saltos, +1 a cada 2 niveis). */
public class PoderRaio extends Poder {
    private static final float DISTANCIA_SALTO = 130f;
    private final Array<Monstro> atingidos = new Array<>();
    private final Array<Efeito> raios = new Array<>();

    public PoderRaio() { super(TipoPoder.RAIO); }

    @Override protected float intervaloBase() { return 0.8f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro atual = maisProximo(heroi.posicao, monstros, alcance(heroi, 300f), null);
        if (atual == null) return false;

        int alvos = 1 + 3 + (nivel - 1) / 2;
        float d = dano(heroi, 14f);
        atingidos.clear();
        FloatArrayBuilder pontos = new FloatArrayBuilder();
        pontos.add(heroi.posicao.x, heroi.posicao.y);

        while (atual != null && atingidos.size < alvos) {
            ferir(heroi, atual, d);
            atingidos.add(atual);
            pontos.add(atual.posicao.x, atual.posicao.y);
            d *= 0.8f; // cada salto causa 20% menos
            atual = maisProximo(atual.posicao, monstros, DISTANCIA_SALTO, atingidos);
        }

        Efeito e = new Efeito(0, 0, 0, 0.12f);
        e.pontos = pontos.toArray();
        raios.add(e);
        return true;
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(raios, delta); }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(tipo.cor);
        for (int i = 0; i < raios.size; i++) {
            float[] p = raios.get(i).pontos;
            for (int k = 0; k + 3 < p.length; k += 2) {
                // pequeno zigue-zague para parecer um raio
                float mx = (p[k] + p[k + 2]) / 2f + MathUtils.random(-6f, 6f);
                float my = (p[k + 1] + p[k + 3]) / 2f + MathUtils.random(-6f, 6f);
                s.line(p[k], p[k + 1], mx, my);
                s.line(mx, my, p[k + 2], p[k + 3]);
            }
        }
    }

    /** Lista simples de floats. */
    private static class FloatArrayBuilder {
        private final com.badlogic.gdx.utils.FloatArray valores = new com.badlogic.gdx.utils.FloatArray();
        void add(float x, float y) { valores.add(x); valores.add(y); }
        float[] toArray() { return valores.toArray(); }
    }
}
