package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** DEMONIO - Fogo: bola que explode em area e deixa queimadura. */
public class PoderFogo extends Poder {
    private static final float RAIO_EXPLOSAO = 60f;
    private final Vector2 dir = new Vector2();
    private final Array<Efeito> explosoes = new Array<>();

    public PoderFogo() { super(TipoPoder.FOGO); }

    @Override protected float intervaloBase() { return 1.0f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, alcance(heroi, 320f), null);
        if (alvo == null) return false;
        dir.set(alvo.posicao).sub(heroi.posicao);
        int n = nivel5() ? 3 : 1; // Nv5: 3 bolas
        for (int k = 0; k < n; k++) {
            Vector2 d = new Vector2(dir).rotateDeg((k - (n - 1) / 2f) * 15f);
            projeteis.add(new Projetil(this, heroi.posicao, d, 360f, 9f, dano(heroi, 22f), alcance(heroi, 360f), 0, tipo.cor));
        }
        return true;
    }

    @Override
    public void aoAcertar(Projetil p, Monstro alvo, Heroi heroi, Array<Monstro> monstros) {
        float raio = RAIO_EXPLOSAO * (1f + 0.1f * (nivel - 1));
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (m.estaVivo() && m.posicao.dst(p.posicao) <= raio + m.tamanho / 2f) {
                ferir(heroi, m, p.dano);
                m.aplicarQueimadura(dano(heroi, 5f), 3f);
            }
        }
        explosoes.add(new Efeito(p.posicao.x, p.posicao.y, raio, 0.25f));
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(explosoes, delta); }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        for (int i = 0; i < explosoes.size; i++) {
            Efeito e = explosoes.get(i);
            s.setColor(Color.ORANGE);
            s.circle(e.x, e.y, e.raio * (0.4f + 0.6f * e.progresso()));
            s.setColor(Color.RED);
            s.circle(e.x, e.y, e.raio * (0.2f + 0.6f * e.progresso()));
        }
    }
}
