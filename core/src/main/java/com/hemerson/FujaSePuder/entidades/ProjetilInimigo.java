package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/** Tiro do boss: voa em linha reta e machuca o heroi ao encostar. */
public class ProjetilInimigo {
    public final Vector2 posicao = new Vector2();
    public final Vector2 velocidade = new Vector2();
    public final float raio;
    public final float dano;
    private float distanciaRestante;

    public ProjetilInimigo(Vector2 origem, float anguloGraus, float velocidadeEscalar, float raio, float dano, float alcance) {
        posicao.set(origem);
        velocidade.set(MathUtils.cosDeg(anguloGraus), MathUtils.sinDeg(anguloGraus)).scl(velocidadeEscalar);
        this.raio = raio;
        this.dano = dano;
        this.distanciaRestante = alcance;
    }

    public void atualizar(float delta) {
        posicao.mulAdd(velocidade, delta);
        distanciaRestante -= velocidade.len() * delta;
    }

    public boolean ativo() { return distanciaRestante > 0; }

    public boolean acertou(Heroi heroi) {
        return posicao.dst(heroi.posicao) < raio + heroi.tamanho / 2f;
    }
}
