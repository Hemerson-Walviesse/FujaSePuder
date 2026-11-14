package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Carta rara que deixa o Humano trocar de elemento, com penalidade. */
public class RecompensaMudarElemento implements Recompensa {
    @Override public String getNome() { return "Mudar Elemento"; }
    @Override public String getDescricao() { return "Troca o elemento da arma. Penalidade: perde 2 buffs (voce escolhe)."; }
    @Override public Color getCor() { return Color.GRAY; }

    @Override
    public void aplicar(Heroi heroi) {
        heroi.pedidoTrocaElemento = true;
    }
}
