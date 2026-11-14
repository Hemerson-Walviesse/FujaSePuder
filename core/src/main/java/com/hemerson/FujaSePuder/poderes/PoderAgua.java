package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** DEMONIO - Agua: jato que atravessa todos e deixa lento. */
public class PoderAgua extends Poder {
    private final Vector2 dir = new Vector2();

    public PoderAgua() { super(TipoPoder.AGUA); }

    @Override protected float intervaloBase() { return 1.2f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, alcance(heroi, 330f), null);
        if (alvo == null) return false;
        dir.set(alvo.posicao).sub(heroi.posicao);
        int n = nivel5() ? 2 : 1; // Nv5: jato duplo
        for (int k = 0; k < n; k++) {
            Vector2 d = new Vector2(dir).rotateDeg((k - (n - 1) / 2f) * 10f);
            projeteis.add(new Projetil(this, heroi.posicao, d, 480f, 10f, dano(heroi, 12f), alcance(heroi, 380f),
                Projetil.ATRAVESSA_TUDO, tipo.cor));
        }
        return true;
    }

    @Override
    public void aoAcertar(Projetil p, Monstro alvo, Heroi heroi, Array<Monstro> monstros) {
        ferir(heroi, alvo, p.dano);
        alvo.aplicarLentidao(0.5f, 2f); // 50% mais lento por 2s
    }
}
