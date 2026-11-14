package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Feixes em linha reta que atravessam tudo (Prisma). */
public class PoderFeixe extends Poder {

    public float intervalo = 1.3f;
    public float danoBase = 16f;
    public float comprimento = 380f;
    public float largura = 8f;
    public int quantidade = 3;
    public int quantidadeNv5 = -1;
    public float abertura = 20f;            // graus entre feixes
    public float atordoar = 0f;
    public Color cor = Color.WHITE;

    private final Array<Efeito> feixes = new Array<>();
    private final Vector2 dir = new Vector2();
    private final Vector2 fim = new Vector2();

    public PoderFeixe(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return intervalo; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, alcance(heroi, comprimento), null);
        if (alvo == null) return false;
        int n = nivel5() && quantidadeNv5 > 0 ? quantidadeNv5 : quantidade;
        float d = dano(heroi, danoBase);
        float comp = alcance(heroi, comprimento);
        for (int k = 0; k < n; k++) {
            dir.set(alvo.posicao).sub(heroi.posicao).nor().rotateDeg((k - (n - 1) / 2f) * abertura);
            fim.set(dir).scl(comp).add(heroi.posicao);
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m.estaVivo() && Intersector.distanceSegmentPoint(heroi.posicao, fim, m.posicao) < largura + m.tamanho / 2f) {
                    ferir(heroi, m, d);
                    if (atordoar > 0) m.atordoar(atordoar);
                }
            }
            Efeito e = new Efeito(heroi.posicao.x, heroi.posicao.y, 0, 0.15f);
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
            float l = largura * 2f * (1f - e.progresso());
            s.setColor(cor);
            s.rectLine(e.x, e.y, e.x2, e.y2, l);
            s.setColor(Color.WHITE);
            s.rectLine(e.x, e.y, e.x2, e.y2, l * 0.35f);
        }
    }
}
