package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.poderes.ElementoArma;
import com.hemerson.FujaSePuder.poderes.TipoEpico;

/** Escolha (ou troca) do elemento da arma do Humano. Trocar gera penalidade de 2 buffs. */
public class RecompensaElemento implements Recompensa {
    private final ElementoArma elemento;

    public RecompensaElemento(ElementoArma elemento) {
        this.elemento = elemento;
    }

    @Override public String getNome() { return "Arma de " + elemento.nome; }
    @Override public String getDescricao() { return elemento.descricao; }
    @Override public Color getCor() { return elemento.cor; }

    @Override
    public void aplicar(Heroi heroi) {
        if (heroi.elemento != null && heroi.elemento != elemento) {
            heroi.penalidadesPendentes += 2; // troca: perde 2 buffs (o jogador escolhe)
        }
        // Os niveis das melhorias continuam e viram as melhorias equivalentes do novo elemento
        heroi.elemento = elemento;
        if (heroi.epico != null) heroi.epico.tipo = TipoEpico.para(heroi); // epico acompanha o elemento
    }
}
