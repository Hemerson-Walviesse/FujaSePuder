package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.math.Vector2;

/** Perigo no chao: marca um circulo vermelho e "cai" depois do aviso, ferindo o heroi se estiver dentro. */
public class Meteoro {
    public static final float TEMPO_AVISO_PADRAO = 1.2f;
    public static final float TEMPO_EXPLOSAO = 0.25f;

    public final Vector2 posicao = new Vector2();
    public final float raio;
    public final float dano;
    private final float tempoAviso;
    private float tempo = 0f;
    private boolean caiu = false;

    public Meteoro(float x, float y, float raio, float dano) {
        this(x, y, raio, dano, TEMPO_AVISO_PADRAO);
    }

    public Meteoro(float x, float y, float raio, float dano, float tempoAviso) {
        posicao.set(x, y);
        this.raio = raio;
        this.dano = dano;
        this.tempoAviso = tempoAviso;
    }

    /** Retorna true no instante exato em que cai. */
    public boolean atualizar(float delta) {
        tempo += delta;
        if (!caiu && tempo >= tempoAviso) {
            caiu = true;
            return true;
        }
        return false;
    }

    public float progressoAviso() { return Math.min(1f, tempo / tempoAviso); }
    public boolean caiu() { return caiu; }
    public boolean terminou() { return tempo >= tempoAviso + TEMPO_EXPLOSAO; }

    public boolean atinge(Heroi heroi) {
        return posicao.dst(heroi.posicao) <= raio + heroi.tamanho / 2f;
    }
}
