package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.math.Vector2;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Item no chao: vida (cura), chave do altar ou chave do portal. */
public class ItemDrop {
    public enum Tipo { VIDA, CHAVE_ALTAR, CHAVE_PORTAL }

    public static final float DURACAO = 20f; // some depois de 20s se ninguem pegar
    public final Tipo tipo;
    public final Vector2 posicao = new Vector2();
    public final float raio = 10f;
    private float tempoRestante;

    public ItemDrop(float x, float y) {
        this(x, y, Tipo.VIDA, DURACAO);
    }

    public ItemDrop(float x, float y, Tipo tipo, float duracao) {
        posicao.set(x, y);
        this.tipo = tipo;
        this.tempoRestante = duracao;
    }

    public void atualizar(float delta) {
        tempoRestante -= delta;
    }

    public boolean expirou() {
        return tempoRestante <= 0;
    }

    /** Pisca nos ultimos 5 segundos para avisar que vai sumir. */
    public boolean visivel() {
        return tempoRestante > 5f || ((int) (tempoRestante * 6)) % 2 == 0;
    }

    public boolean colideCom(Heroi heroi) {
        return posicao.dst(heroi.posicao) < raio + heroi.tamanho / 2f;
    }
}
