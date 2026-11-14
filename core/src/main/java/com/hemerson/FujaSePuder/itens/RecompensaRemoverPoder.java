package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.poderes.Poder;
import com.hemerson.FujaSePuder.poderes.TipoPoder;

/** Carta da troca de poder: este poder sai (e perde os niveis). */
public class RecompensaRemoverPoder implements Recompensa {
    private final TipoPoder tipo;
    private final int nivel;

    public RecompensaRemoverPoder(Poder poder) {
        this.tipo = poder.tipo;
        this.nivel = poder.getNivel();
    }

    @Override public String getNome() { return "Sai: " + tipo.nome + " Nv" + nivel; }
    @Override public String getDescricao() { return "Este poder sai e perde todos os niveis."; }
    @Override public Color getCor() { return tipo.cor; }

    @Override
    public void aplicar(Heroi heroi) {
        heroi.removerPoder(tipo);
        heroi.ultimoRemovido = tipo;
        heroi.pedidoNovoPoder = true;
    }
}
