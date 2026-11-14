package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.hemerson.FujaSePuder.entidades.Elemento;
import com.hemerson.FujaSePuder.entidades.Heroi;

/** Poderes epicos (botao, duram alguns segundos). O do Humano acompanha o elemento da arma. */
public enum TipoEpico {
    LANCA_CHAMAS("Lanca-chamas", "Cone de fogo continuo na sua frente.", Color.ORANGE),
    NEVASCA("Nevasca", "Tempestade de gelo em volta: deixa lento e congela.", Color.CYAN),
    CANHAO_LASER("Canhao Laser", "Feixe continuo que atravessa tudo.", Color.MAGENTA),
    FORMA_INFERNAL("Forma Infernal", "Aura de fogo queima em volta e voce causa +30% de dano.", Color.SCARLET),
    JULGAMENTO_DIVINO("Julgamento Divino", "Pilares de luz caem nos monstros proximos e atordoam.", Color.GOLD);

    public final String nome;
    public final String descricao;
    public final Color cor;

    TipoEpico(String nome, String descricao, Color cor) {
        this.nome = nome;
        this.descricao = descricao;
        this.cor = cor;
    }

    /** Elemento do dano do epico (fraquezas dos bosses). */
    public Elemento elemento() {
        switch (this) {
            case LANCA_CHAMAS:
            case FORMA_INFERNAL: return Elemento.FOGO;
            case NEVASCA:        return Elemento.GELO;
            case CANHAO_LASER:   return Elemento.ELETRICO;
            default:             return Elemento.LUZ;
        }
    }

    /** Qual epico o heroi recebe, conforme a classe (e o elemento, no caso do Humano). */
    public static TipoEpico para(Heroi heroi) {
        switch (heroi.classe) {
            case DEMONIO: return FORMA_INFERNAL;
            case ANJO:    return JULGAMENTO_DIVINO;
            default:
                if (heroi.elemento == ElementoArma.FOGO) return LANCA_CHAMAS;
                if (heroi.elemento == ElementoArma.GELO) return NEVASCA;
                return CANHAO_LASER;
        }
    }
}
