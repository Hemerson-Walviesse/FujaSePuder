package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Tiro, bola de fogo, jato, lanca... qualquer coisa do heroi que voa e acerta monstros. */
public class Projetil {
    public static final int ATRAVESSA_TUDO = 9999;

    public final Poder origem;
    public final Vector2 posicao = new Vector2();
    public final Vector2 velocidade = new Vector2();
    public final float raio;
    public final float dano;
    public final Color cor;

    /** Bumerangue: ao chegar no fim, volta. */
    public boolean volta = false;
    /** Granada: explode no fim do alcance (Poder.aoExpirar). */
    public boolean explodeNoFim = false;
    /** Granada: nao causa dano ao encostar, so na explosao. */
    public boolean soExplode = false;

    private int perfuracoes;
    private final float alcanceInicial;
    private float distanciaRestante;
    private boolean ativo = true;
    private boolean voltando = false;
    private final Array<Monstro> jaAtingidos = new Array<>();

    public Projetil(Poder origem, Vector2 inicio, Vector2 direcao, float velocidadeEscalar,
                    float raio, float dano, float alcance, int perfuracoes, Color cor) {
        this.origem = origem;
        this.posicao.set(inicio);
        this.velocidade.set(direcao).nor().scl(velocidadeEscalar);
        this.raio = raio;
        this.dano = dano;
        this.alcanceInicial = alcance;
        this.distanciaRestante = alcance;
        this.perfuracoes = perfuracoes;
        this.cor = cor;
    }

    public void atualizar(float delta) {
        posicao.mulAdd(velocidade, delta);
        distanciaRestante -= velocidade.len() * delta;
        if (distanciaRestante <= 0) {
            if (volta && !voltando) {
                voltando = true;
                velocidade.scl(-1f);
                distanciaRestante = alcanceInicial;
                jaAtingidos.clear(); // pode acertar de novo na volta
            } else {
                ativo = false;
            }
        }
    }

    public boolean podeAcertar(Monstro m) {
        return ativo && !jaAtingidos.contains(m, true) && m.posicao.dst(posicao) < raio + m.tamanho / 2f;
    }

    public void registrarAcerto(Monstro m) {
        jaAtingidos.add(m);
        if (perfuracoes <= 0) ativo = false;
        else perfuracoes--;
    }

    public boolean isAtivo() { return ativo; }
}
