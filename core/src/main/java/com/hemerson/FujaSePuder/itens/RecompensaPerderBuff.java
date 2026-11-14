package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Carta da tela de penalidade: o buff escolhido e perdido. */
public class RecompensaPerderBuff implements Recompensa {
    private final TipoBuff buff;   // buff generico, ou null se for melhoria de elemento
    private final int espaco;      // espaco da melhoria de elemento (quando buff == null)
    private final String nome;
    private final String descricao;
    private final Color cor;

    private RecompensaPerderBuff(TipoBuff buff, int espaco, String nome, String descricao, Color cor) {
        this.buff = buff;
        this.espaco = espaco;
        this.nome = nome;
        this.descricao = descricao;
        this.cor = cor;
    }

    public static RecompensaPerderBuff doBuff(TipoBuff buff, int quantidade) {
        String n = quantidade > 1 ? buff.nome + " (x" + quantidade + ")" : buff.nome;
        return new RecompensaPerderBuff(buff, -1, n, "Perde 1: " + buff.descricao, buff.cor);
    }

    public static RecompensaPerderBuff daMelhoria(Heroi heroi, int espaco) {
        String n = heroi.elemento.nomesMelhoria[espaco] + " Nv " + heroi.niveisElemento[espaco];
        return new RecompensaPerderBuff(null, espaco, n, "Perde 1 nivel.", heroi.elemento.cor);
    }

    @Override public String getNome() { return nome; }
    @Override public String getDescricao() { return descricao; }
    @Override public Color getCor() { return cor; }

    @Override
    public void aplicar(Heroi heroi) {
        if (buff != null) {
            heroi.buffs.removeValue(buff, true);
            heroi.recalcularAtributos();
        } else {
            heroi.niveisElemento[espaco] = Math.max(0, heroi.niveisElemento[espaco] - 1);
        }
        heroi.penalidadesPendentes = Math.max(0, heroi.penalidadesPendentes - 1);
    }
}
