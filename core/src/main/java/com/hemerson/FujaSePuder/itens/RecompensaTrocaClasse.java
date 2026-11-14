package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.ClasseHeroi;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Carta de troca de classe (so nos bosses 1 e 3, e so uma vez por partida). */
public class RecompensaTrocaClasse implements Recompensa {
    private final ClasseHeroi destino;
    private final String nomeTransformacao;
    private final String caminho;

    public RecompensaTrocaClasse(ClasseHeroi destino, String nomeTransformacao, String caminho) {
        this.destino = destino;
        this.nomeTransformacao = nomeTransformacao;
        this.caminho = caminho;
    }

    @Override public String getNome() { return nomeTransformacao; }

    @Override public String getDescricao() {
        return caminho + ": vira " + destino.nome + ". Poderes equivalentes no Nv 1. Penalidade: perde 2 buffs.";
    }

    @Override public Color getCor() { return destino.cor; }

    @Override
    public void aplicar(Heroi heroi) {
        heroi.trocarClasse(destino, nomeTransformacao);
    }

    /** As 2 opcoes de transformacao para a classe atual. */
    public static Array<Recompensa> opcoesPara(Heroi heroi) {
        Array<Recompensa> lista = new Array<>();
        switch (heroi.classe) {
            case ANJO:
                lista.add(new RecompensaTrocaClasse(ClasseHeroi.DEMONIO, "Anjo Caido", "Caminho do mal"));
                lista.add(new RecompensaTrocaClasse(ClasseHeroi.HUMANO, "Nephilim", "Caminho humano"));
                break;
            case DEMONIO:
                lista.add(new RecompensaTrocaClasse(ClasseHeroi.ANJO, "Demonio Redimido", "Caminho do bem"));
                lista.add(new RecompensaTrocaClasse(ClasseHeroi.HUMANO, "Cambion", "Caminho humano"));
                break;
            default:
                lista.add(new RecompensaTrocaClasse(ClasseHeroi.ANJO, "Paladino", "Caminho do bem"));
                lista.add(new RecompensaTrocaClasse(ClasseHeroi.DEMONIO, "Corrompido", "Caminho do mal"));
                break;
        }
        return lista;
    }
}
