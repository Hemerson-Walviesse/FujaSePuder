package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Versao RARA de uma carta: o efeito vale em dobro (buff 2x, poder +2 niveis). */
public class RecompensaRara implements Recompensa {
    public final Recompensa base;

    public RecompensaRara(Recompensa base) {
        this.base = base;
    }

    @Override public String getNome() { return "RARO: " + base.getNome(); }
    @Override public String getDescricao() { return base.getDescricao() + " (EFEITO EM DOBRO)"; }
    @Override public Color getCor() { return base.getCor(); }

    @Override
    public void aplicar(Heroi heroi) {
        base.aplicar(heroi);
        base.aplicar(heroi);
    }

    /** So buffs, poderes e melhorias de elemento podem ser raros. */
    public static boolean podeSerRara(Recompensa r) {
        return r instanceof TipoBuff || r instanceof RecompensaMelhoriaElemento
            || r instanceof com.hemerson.FujaSePuder.poderes.RecompensaPoder;
    }
}
