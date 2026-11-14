package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.poderes.ElementoArma;
import com.hemerson.FujaSePuder.poderes.Poder;
import com.hemerson.FujaSePuder.poderes.RecompensaEpico;
import com.hemerson.FujaSePuder.poderes.RecompensaPoder;
import com.hemerson.FujaSePuder.poderes.TipoPoder;

/**
 * Monta as cartas da escolha de fim de horda.
 * Raridade de cada carta: 1% EPICO (poder epico), 2% RARO (efeito em dobro), resto comum.
 */
public final class SorteioRecompensas {

    public static final float CHANCE_EPICO = 0.01f;
    public static final float CHANCE_RARO = 0.02f;

    private SorteioRecompensas() { }

    /** Cartas normais de fim de horda. */
    public static Array<Recompensa> sortear(Heroi heroi, int quantidade) {
        Array<Recompensa> escolhidas = sortearBase(heroi, quantidade, false);
        // Raridade por carta
        boolean temEpico = false;
        for (int i = 0; i < escolhidas.size; i++) {
            float r = MathUtils.random();
            if (r < CHANCE_EPICO && !temEpico) {
                escolhidas.set(i, new RecompensaEpico(heroi));
                temEpico = true;
            } else if (r < CHANCE_EPICO + CHANCE_RARO && RecompensaRara.podeSerRara(escolhidas.get(i))) {
                escolhidas.set(i, new RecompensaRara(escolhidas.get(i)));
            }
        }
        return escolhidas;
    }

    /** Cartas RARAS garantidas (recompensa de boss). */
    public static Array<Recompensa> sortearRaras(Heroi heroi, int quantidade) {
        Array<Recompensa> base = sortearBase(heroi, quantidade, true);
        Array<Recompensa> raras = new Array<>();
        for (int i = 0; i < base.size; i++) raras.add(new RecompensaRara(base.get(i)));
        return raras;
    }

    private static Array<Recompensa> sortearBase(Heroi heroi, int quantidade, boolean soQuePodemSerRaras) {
        Array<Recompensa> sacola = new Array<>();

        for (TipoBuff b : TipoBuff.values()) {
            if (b.disponivelPara(heroi.classe)) sacola.add(b);
        }

        // Niveis dos poderes que ja tem
        for (int i = 0; i < heroi.poderes.size; i++) {
            Poder p = heroi.poderes.get(i);
            if (!p.nivelMaximo()) sacola.add(new RecompensaPoder(p.tipo, false, p.getNivel() + 1));
        }

        // Poderes novos (2 sorteados, cada um 2x na sacola) enquanto houver espaco
        if (heroi.poderes.size < Heroi.MAX_PODERES) {
            Array<TipoPoder> livres = poderesLivres(heroi);
            livres.shuffle();
            for (int i = 0; i < Math.min(2, livres.size); i++) {
                Recompensa carta = new RecompensaPoder(livres.get(i), true, 1);
                sacola.add(carta);
                sacola.add(carta);
            }
        } else if (!soQuePodemSerRaras && poderesLivres(heroi).size > 0) {
            sacola.add(new RecompensaTrocarPoder());
        }

        // Humano: melhorias do elemento escolhido (2x) e, as vezes, a troca de elemento
        if (heroi.elemento != null) {
            for (int espaco = 0; espaco < 2; espaco++) {
                int nivel = heroi.niveisElemento[espaco];
                if (nivel < ElementoArma.NIVEL_MAXIMO_MELHORIA) {
                    Recompensa carta = new RecompensaMelhoriaElemento(heroi.elemento, espaco, nivel + 1);
                    sacola.add(carta);
                    sacola.add(carta);
                }
            }
            if (!soQuePodemSerRaras) sacola.add(new RecompensaMudarElemento());
        }

        sacola.shuffle();
        Array<Recompensa> escolhidas = new Array<>();
        for (int i = 0; i < sacola.size && escolhidas.size < quantidade; i++) {
            Recompensa r = sacola.get(i);
            if (soQuePodemSerRaras && !RecompensaRara.podeSerRara(r)) continue;
            if (!escolhidas.contains(r, true)) escolhidas.add(r);
        }
        return escolhidas;
    }

    /** Poderes da classe que o heroi ainda nao tem. */
    public static Array<TipoPoder> poderesLivres(Heroi heroi) {
        Array<TipoPoder> livres = new Array<>();
        for (TipoPoder t : TipoPoder.daClasse(heroi.classe)) {
            if (!heroi.temPoder(t)) livres.add(t);
        }
        return livres;
    }
}
