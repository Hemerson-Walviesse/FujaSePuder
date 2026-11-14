package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Golpe em cone na frente do heroi, mirando no monstro mais proximo (Garras Sombrias). */
public class PoderCone extends Poder {

    public float intervalo = 0.9f;
    public float danoBase = 30f;
    public float alcanceCone = 110f;
    public float angulo = 45f;              // metade da abertura
    public float anguloNv5 = 90f;
    public Color cor = Color.VIOLET;

    private final Array<Efeito> golpes = new Array<>();
    private final Vector2 dir = new Vector2();
    private final Vector2 tmp = new Vector2();

    public PoderCone(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return intervalo; }

    private float anguloAtual() { return nivel5() ? anguloNv5 : angulo; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        float alc = alcance(heroi, alcanceCone);
        Monstro alvo = maisProximo(heroi.posicao, monstros, alc + 20f, null);
        if (alvo == null) return false;
        dir.set(alvo.posicao).sub(heroi.posicao).nor();
        float cosMeio = MathUtils.cosDeg(anguloAtual());
        float d = dano(heroi, danoBase);
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            tmp.set(m.posicao).sub(heroi.posicao);
            float dist = tmp.len();
            if (m.estaVivo() && dist <= alc + m.tamanho / 2f && (dist < 1f || tmp.scl(1f / dist).dot(dir) >= cosMeio)) {
                ferir(heroi, m, d);
            }
        }
        Efeito e = new Efeito(heroi.posicao.x, heroi.posicao.y, alc, 0.15f);
        e.x2 = dir.angleDeg();
        golpes.add(e);
        return true;
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(golpes, delta); }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(cor);
        for (int i = 0; i < golpes.size; i++) {
            Efeito e = golpes.get(i);
            float a = e.x2;
            float ang = anguloAtual();
            s.arc(heroi.posicao.x, heroi.posicao.y, e.raio, a - ang, ang * 2);
            s.arc(heroi.posicao.x, heroi.posicao.y, e.raio * 0.7f, a - ang, ang * 2);
        }
    }
}
