package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Elemento;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/**
 * Regras dos tiros do Humano conforme o elemento da arma:
 *  - Fogo: explode (50% do dano em volta) e queima.
 *  - Gelo: deixa lento e pode congelar.
 *  - Laser: mais rapido, vai mais longe e atravessa todos.
 */
public final class ArmaElemental {

    private static final float RAIO_EXPLOSAO_BASE = 35f;

    private ArmaElemental() { }

    public static Elemento elemento(Heroi heroi) {
        return heroi.elemento == null ? Elemento.FISICO : heroi.elemento.familia();
    }

    public static Projetil criarTiro(Poder dono, Heroi heroi, Vector2 origem, Vector2 direcao, float dano,
                                     float velocidade, float raio, float alcance, int perfuracoes, Color corPadrao) {
        ElementoArma e = heroi.elemento;
        Color cor = e != null ? e.cor : corPadrao;
        if (e == ElementoArma.LASER) {
            velocidade *= 1.5f;
            raio = Math.min(raio, 4f);
            perfuracoes = Projetil.ATRAVESSA_TUDO;
            dano *= 1f + 0.2f * heroi.nivelElemento(0);
            alcance *= 1.3f * (1f + 0.25f * heroi.nivelElemento(1));
        }
        return new Projetil(dono, origem, direcao, velocidade, raio, dano, alcance, perfuracoes, cor);
    }

    /** Aplica o acerto de um tiro elemental (inclui o dano). */
    public static void aplicarAcerto(Poder dono, Projetil p, Monstro alvo, Heroi heroi, Array<Monstro> monstros,
                                     Array<Efeito> explosoes) {
        ElementoArma e = heroi.elemento;
        if (e == ElementoArma.FOGO) {
            float raio = RAIO_EXPLOSAO_BASE * (1f + 0.25f * heroi.nivelElemento(1));
            float queimadura = dono.dano(heroi, 3f) * (1f + 0.5f * heroi.nivelElemento(0));
            dono.ferir(heroi, alvo, p.dano);
            alvo.aplicarQueimadura(queimadura, 2.5f);
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m != alvo && m.estaVivo() && m.posicao.dst(p.posicao) <= raio + m.tamanho / 2f) {
                    dono.ferir(heroi, m, p.dano * 0.5f);
                    m.aplicarQueimadura(queimadura, 2.5f);
                }
            }
            if (explosoes != null) explosoes.add(new Efeito(p.posicao.x, p.posicao.y, raio, 0.2f));
        } else if (e == ElementoArma.GELO) {
            dono.ferir(heroi, alvo, p.dano);
            alvo.aplicarLentidao(Math.max(0.2f, 0.6f - 0.08f * heroi.nivelElemento(0)), 2f);
            float chanceCongelar = Math.min(0.5f, 0.05f + 0.10f * heroi.nivelElemento(1));
            if (MathUtils.randomBoolean(chanceCongelar)) alvo.atordoar(1f);
        } else {
            dono.ferir(heroi, alvo, p.dano);
        }
    }
}
