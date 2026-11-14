package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** HUMANO - Dupla: 2 tiros abertos (3 a partir do nivel 4). O efeito depende do elemento. */
public class PoderDupla extends PoderArma {
    private final Vector2 dir = new Vector2();
    private final Vector2 girado = new Vector2();

    public PoderDupla() { super(TipoPoder.DUPLA); }

    @Override protected float intervaloBase() { return 0.6f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, alcanceMira(heroi), null);
        if (alvo == null) return false;
        dir.set(alvo.posicao).sub(heroi.posicao).nor();
        float d = dano(heroi, 13f);
        float[] angulos = nivel >= 4 ? new float[] { -12f, 0f, 12f } : new float[] { -7f, 7f };
        for (float a : angulos) {
            girado.set(dir).rotateDeg(a);
            disparar(heroi, projeteis, girado, d);
        }
        return true;
    }
}
