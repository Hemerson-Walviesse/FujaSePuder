package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;

/**
 * As 3 classes jogaveis. Todos os numeros de balanceamento ficam aqui,
 * para facilitar o ajuste depois.
 */
public enum ClasseHeroi {

    HUMANO("Humano",
        100f, 200f, 25f, 140f, 0.50f,
        Color.SKY,
        "Regenera 1 de vida a cada 2s.",
        "Golpe Giratorio", "Causa 40 de dano em todos os monstros ao redor.", 20f),

    DEMONIO("Demonio",
        140f, 175f, 35f, 100f, 0.60f,
        Color.SCARLET,
        "Recupera 5% do dano como vida (max. 2/s).",
        "Furia Infernal", "Por 5s: +25% de dano e ataca 2x mais rapido.", 20f),

    ANJO("Anjo",
        80f, 225f, 14f, 160f, 0.40f,
        Color.GOLD,
        "Cada ataque atinge ate 2 monstros.",
        "Luz Sagrada", "Cura 30 de vida e causa 30 de dano em area grande.", 20f);

    public final String nome;
    public final float vidaMaxima;
    public final float velocidade;
    public final float dano;
    public final float alcanceAtaque;
    public final float intervaloAtaque;
    public final Color cor;
    public final String passiva;
    public final String nomeHabilidade;
    public final String descricaoHabilidade;
    public final float recargaHabilidade;

    ClasseHeroi(String nome, float vidaMaxima, float velocidade, float dano, float alcanceAtaque,
                float intervaloAtaque, Color cor, String passiva,
                String nomeHabilidade, String descricaoHabilidade, float recargaHabilidade) {
        this.nome = nome;
        this.vidaMaxima = vidaMaxima;
        this.velocidade = velocidade;
        this.dano = dano;
        this.alcanceAtaque = alcanceAtaque;
        this.intervaloAtaque = intervaloAtaque;
        this.cor = cor;
        this.passiva = passiva;
        this.nomeHabilidade = nomeHabilidade;
        this.descricaoHabilidade = descricaoHabilidade;
        this.recargaHabilidade = recargaHabilidade;
    }
}
