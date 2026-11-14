package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Qualquer coisa que pode aparecer como carta na escolha de fim de horda. */
public interface Recompensa {
    String getNome();
    String getDescricao();
    Color getCor();
    void aplicar(Heroi heroi);
}
