package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.itens.Recompensa;

/** Carta de "novo poder" ou "subir nivel de um poder". */
public class RecompensaPoder implements Recompensa {
    private final TipoPoder tipo;
    private final boolean novo;
    private final int proximoNivel;

    public RecompensaPoder(TipoPoder tipo, boolean novo, int proximoNivel) {
        this.tipo = tipo;
        this.novo = novo;
        this.proximoNivel = proximoNivel;
    }

    @Override public String getNome() {
        return novo ? "NOVO: " + tipo.nome : tipo.nome + " Nv " + proximoNivel;
    }

    @Override public String getDescricao() {
        String extra = " [" + TipoPoder.NOMES_PAPEL[tipo.papel] + (tipo.tiro ? ", elemento da arma" : ", " + tipo.elemento.nome) + "]";
        return (novo ? tipo.descricao : "Sobe de nivel: +15% de dano e recarga mais rapida.") + extra;
    }

    @Override public Color getCor() { return tipo.cor; }

    @Override
    public void aplicar(Heroi heroi) {
        // Se ainda nao tem, ganha no Nv1; se ja tem, sobe 1 nivel (a carta rara aplica 2x)
        Poder p = heroi.getPoder(tipo);
        if (p == null) heroi.adicionarPoder(tipo.criar());
        else p.subirNivel();
    }
}
