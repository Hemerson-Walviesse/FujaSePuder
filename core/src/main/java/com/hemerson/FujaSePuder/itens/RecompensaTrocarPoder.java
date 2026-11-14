package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Com 5 poderes: escolha qual sai e qual entra. O que sai perde todos os niveis. */
public class RecompensaTrocarPoder implements Recompensa {
    @Override public String getNome() { return "Trocar Poder"; }
    @Override public String getDescricao() { return "Escolha um poder para sair e um novo para entrar (Nv1). O que sai perde todos os niveis."; }
    @Override public Color getCor() { return Color.GRAY; }

    @Override
    public void aplicar(Heroi heroi) {
        heroi.pedidoTrocaPoder = true;
    }
}
