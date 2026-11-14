package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.itens.Recompensa;

/** Carta do boss: ganha o poder epico da classe, ou evolui o que ja tem. */
public class RecompensaEpico implements Recompensa {
    private final TipoEpico tipo;
    private final int proximoNivel;

    public RecompensaEpico(Heroi heroi) {
        this.tipo = TipoEpico.para(heroi);
        this.proximoNivel = heroi.epico == null ? 1 : Math.min(PoderEpico.NIVEL_MAXIMO, heroi.epico.getNivel() + 1);
    }

    @Override public String getNome() {
        return proximoNivel == 1 ? "EPICO: " + tipo.nome : "EPICO: " + tipo.nome + " Nv " + proximoNivel;
    }

    @Override public String getDescricao() {
        return proximoNivel == 1
            ? tipo.descricao + " Botao [E], dura 5s."
            : "Evolui: +30% de dano e +0.5s de duracao.";
    }

    @Override public Color getCor() { return tipo.cor; }

    @Override
    public void aplicar(Heroi heroi) {
        if (heroi.epico == null) heroi.epico = new PoderEpico(tipo);
        else heroi.epico.subirNivel();
    }
}
