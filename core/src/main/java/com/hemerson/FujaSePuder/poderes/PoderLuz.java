package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;
import com.badlogic.gdx.math.Intersector;

/** ANJO - Luz: feixe em linha reta que atravessa tudo, dano alto e atordoa. */
public class PoderLuz extends Poder {
    private static final float LARGURA_FEIXE = 12f;
    private final Vector2 inicio = new Vector2();
    private final Vector2 fim = new Vector2();
    private final Array<Efeito> feixes = new Array<>();

    public PoderLuz() { super(TipoPoder.LUZ); }

    @Override protected float intervaloBase() { return 1.6f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, alcance(heroi, 380f), null);
        if (alvo == null) return false;

        float d = dano(heroi, 28f);
        int n = nivel5() ? 2 : 1; // Nv5: feixe duplo
        for (int k = 0; k < n; k++) {
            inicio.set(heroi.posicao);
            fim.set(alvo.posicao).sub(heroi.posicao).nor().rotateDeg((k - (n - 1) / 2f) * 16f)
                .scl(alcance(heroi, 420f)).add(heroi.posicao);
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m.estaVivo() && Intersector.distanceSegmentPoint(inicio, fim, m.posicao) < LARGURA_FEIXE + m.tamanho / 2f) {
                    ferir(heroi, m, d);
                    m.atordoar(0.6f);
                }
            }
            Efeito e = new Efeito(inicio.x, inicio.y, 0, 0.15f);
            e.x2 = fim.x;
            e.y2 = fim.y;
            feixes.add(e);
        }
        return true;
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(feixes, delta); }

    @Override
    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        for (int i = 0; i < feixes.size; i++) {
            Efeito e = feixes.get(i);
            float largura = LARGURA_FEIXE * 2f * (1f - e.progresso());
            s.setColor(Color.GOLD);
            s.rectLine(e.x, e.y, e.x2, e.y2, largura);
            s.setColor(Color.WHITE);
            s.rectLine(e.x, e.y, e.x2, e.y2, largura * 0.4f);
        }
    }
}
