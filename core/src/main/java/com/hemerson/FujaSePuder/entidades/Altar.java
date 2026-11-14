package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.math.Vector2;

/**
 * Altar de sacrificio: monstros mortos dentro dele contam para invocar o boss.
 *  - selado: nao aceita sacrificios ate ser aberto (chave do Portador, ou o Guardiao do Selo morrer dentro dele).
 *  - escondido: nao aparece no mapa ate o heroi entrar num portal que leva ate ele.
 */
public class Altar {
    public final Vector2 posicao = new Vector2();
    public final float raio = 110f;
    private final int necessario;
    private int sacrificios = 0;
    /** Ultima etapa (1/3, 2/3) em que o altar ja mudou de lugar. */
    public int etapaMovida = 0;

    public boolean selado = false;
    /** Fase 5: so abre quando o Guardiao do Selo morre dentro dele (e nao com chave). */
    public boolean abreComGuardiao = false;
    public boolean escondido = false;

    public Altar(float x, float y, int necessario) {
        posicao.set(x, y);
        this.necessario = necessario;
    }

    public boolean contem(Vector2 ponto) {
        return ponto.dst(posicao) <= raio;
    }

    public void mover(float x, float y) {
        posicao.set(x, y);
    }

    /** 0 = ate 1/3, 1 = entre 1/3 e 2/3, 2 = depois de 2/3, 3 = completo. */
    public int etapa() {
        return Math.min(3, sacrificios * 3 / necessario);
    }

    public void registrarSacrificio() {
        if (sacrificios < necessario) sacrificios++;
    }

    /** Aceita sacrificios: ja foi encontrado e nao esta selado. */
    public boolean ativo() { return !selado && !escondido; }
    public boolean completo() { return sacrificios >= necessario; }
    public int getSacrificios() { return sacrificios; }
    public int getNecessario() { return necessario; }
    public float progresso() { return Math.min(1f, sacrificios / (float) necessario); }
}
