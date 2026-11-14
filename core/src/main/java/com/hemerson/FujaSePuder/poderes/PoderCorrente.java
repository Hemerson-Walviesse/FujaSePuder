package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Acerta o mais proximo e salta para os vizinhos (Taser). */
public class PoderCorrente extends Poder {

    public float intervalo = 1.5f;
    public float danoBase = 10f;
    public int saltos = 1;
    public int saltosNv5 = 3;
    public float alcanceInicial = 300f;
    public float distanciaSalto = 120f;
    public float atordoar = 1f;
    public Color cor = Color.YELLOW;

    private final Array<Monstro> atingidos = new Array<>();
    private final Array<Efeito> raios = new Array<>();

    public PoderCorrente(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return intervalo; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro atual = maisProximo(heroi.posicao, monstros, alcance(heroi, alcanceInicial), null);
        if (atual == null) return false;
        int total = 1 + (nivel5() ? saltosNv5 : saltos);
        float d = dano(heroi, danoBase);
        atingidos.clear();
        FloatArray pontos = new FloatArray();
        pontos.add(heroi.posicao.x);
        pontos.add(heroi.posicao.y);
        while (atual != null && atingidos.size < total) {
            ferir(heroi, atual, d);
            if (atordoar > 0) atual.atordoar(atordoar);
            atingidos.add(atual);
            pontos.add(atual.posicao.x);
            pontos.add(atual.posicao.y);
            atual = maisProximo(atual.posicao, monstros, distanciaSalto, atingidos);
        }
        Efeito e = new Efeito(0, 0, 0, 0.15f);
        e.pontos = pontos.toArray();
        raios.add(e);
        return true;
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(raios, delta); }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(cor);
        for (int i = 0; i < raios.size; i++) {
            float[] p = raios.get(i).pontos;
            for (int k = 0; k + 3 < p.length; k += 2) {
                float mx = (p[k] + p[k + 2]) / 2f + MathUtils.random(-5f, 5f);
                float my = (p[k + 1] + p[k + 3]) / 2f + MathUtils.random(-5f, 5f);
                s.line(p[k], p[k + 1], mx, my);
                s.line(mx, my, p[k + 2], p[k + 3]);
            }
        }
    }
}
