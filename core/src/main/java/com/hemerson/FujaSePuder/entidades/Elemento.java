package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;

/**
 * Familia de elemento de cada dano. Usado nas fraquezas/resistencias dos bosses
 * e na ressonancia (3+ poderes do mesmo elemento = +30% de dano).
 */
public enum Elemento {
    FISICO("Fisico", Color.LIGHT_GRAY),
    FOGO("Fogo", Color.ORANGE),
    GELO("Gelo/Agua", Color.CYAN),
    ELETRICO("Raio/Laser", Color.YELLOW),
    VENTO("Vento", Color.TEAL),
    LUZ("Luz", Color.GOLD),
    SOMBRA("Sombra/Sangue", Color.VIOLET);

    public final String nome;
    public final Color cor;

    Elemento(String nome, Color cor) {
        this.nome = nome;
        this.cor = cor;
    }
}
