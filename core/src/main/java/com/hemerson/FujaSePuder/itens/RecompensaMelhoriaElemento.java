package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.poderes.ElementoArma;

/** Melhoria exclusiva do elemento atual (ex.: Chamas Intensas para Fogo). */
public class RecompensaMelhoriaElemento implements Recompensa {
    private final ElementoArma elemento;
    private final int espaco;
    private final int proximoNivel;

    public RecompensaMelhoriaElemento(ElementoArma elemento, int espaco, int proximoNivel) {
        this.elemento = elemento;
        this.espaco = espaco;
        this.proximoNivel = proximoNivel;
    }

    @Override public String getNome() { return elemento.nomesMelhoria[espaco] + " Nv " + proximoNivel; }
    @Override public String getDescricao() { return elemento.descricoesMelhoria[espaco]; }
    @Override public Color getCor() { return elemento.cor; }

    @Override
    public void aplicar(Heroi heroi) {
        heroi.niveisElemento[espaco] = Math.min(ElementoArma.NIVEL_MAXIMO_MELHORIA, heroi.niveisElemento[espaco] + 1);
    }
}
