package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Elemento;

/**
 * Elemento das armas do Humano. O jogador escolhe UM e so recebe melhorias dele.
 * As melhorias tem 2 "espacos" equivalentes entre os elementos (0 = forca, 1 = alcance/area/chance):
 * ao trocar de elemento, os niveis que sobraram viram as melhorias equivalentes do novo elemento.
 */
public enum ElementoArma {

    FOGO("Fogo", "Tiros explodem e queimam os monstros.", Color.ORANGE,
        new String[] { "Chamas Intensas", "Explosao Maior" },
        new String[] { "+50% de dano da queimadura.", "+25% no raio da explosao." }),

    GELO("Gelo", "Tiros deixam lentos e podem congelar.", Color.CYAN,
        new String[] { "Frio Intenso", "Congelamento" },
        new String[] { "Deixa os monstros ainda mais lentos.", "+10% de chance de congelar." }),

    LASER("Laser", "Tiros atravessam todos os monstros e vao mais longe.", Color.MAGENTA,
        new String[] { "Foco do Laser", "Feixe Longo" },
        new String[] { "+20% de dano do laser.", "+25% de alcance do laser." });

    public static final int NIVEL_MAXIMO_MELHORIA = 5;

    public final String nome;
    public final String descricao;
    public final Color cor;
    public final String[] nomesMelhoria;
    public final String[] descricoesMelhoria;

    /** Familia de elemento (para fraquezas e ressonancia). */
    public Elemento familia() {
        switch (this) {
            case FOGO: return Elemento.FOGO;
            case GELO: return Elemento.GELO;
            default:   return Elemento.ELETRICO;
        }
    }

    ElementoArma(String nome, String descricao, Color cor, String[] nomesMelhoria, String[] descricoesMelhoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.cor = cor;
        this.nomesMelhoria = nomesMelhoria;
        this.descricoesMelhoria = descricoesMelhoria;
    }
}
