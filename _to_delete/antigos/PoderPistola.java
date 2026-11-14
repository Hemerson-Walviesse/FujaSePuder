package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** HUMANO - Pistola: tiro unico e rapido. O efeito depende do elemento. */
public class PoderPistola extends PoderArma {
    private final Vector2 dir = new Vector2();

    public PoderPistola() { super(TipoPoder.PISTOLA); }

    @Override protected float intervaloBase() { return 0.35f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, alcanceMira(heroi), null);
        if (alvo == null) return false;
        dir.set(alvo.posicao).sub(heroi.posicao);
        disparar(heroi, projeteis, dir, dano(heroi, 15f));
        return true;
    }
}
