package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Onda em volta do heroi: Halo (empurra), Medo (faz fugir), Purificacao (bonus em monstro com efeito), Aura (lentidao). */
public class PoderPulso extends Poder {

    public float intervalo = 1f;
    public float raio = 100f;
    public float raioNv5 = -1f;
    public float danoBase = 0f;
    public float empurrar = 0f;
    public float medo = 0f;
    public float medoNv5 = -1f;
    public float lentidao = 1f;
    public float lentidaoNv5 = -1f;
    public float lentidaoDuracao = 0.5f;
    public float bonusComEfeito = 0f;       // Purificacao: +50% (Nv5: +100%)
    public float bonusComEfeitoNv5 = -1f;
    public boolean mostrarOnda = true;
    public boolean aura = false;            // desenha um anel fixo em vez da onda
    public Color cor = Color.WHITE;

    private final Array<Efeito> ondas = new Array<>();
    private final Vector2 tmp = new Vector2();

    public PoderPulso(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return intervalo; }

    private float raioAtual(Heroi heroi) {
        return alcance(heroi, nivel5() && raioNv5 > 0 ? raioNv5 : raio);
    }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        float r = raioAtual(heroi);
        if (maisProximo(heroi.posicao, monstros, r, null) == null) return false;
        float d = danoBase > 0 ? dano(heroi, danoBase) : 0f;
        float bonus = nivel5() && bonusComEfeitoNv5 >= 0 ? bonusComEfeitoNv5 : bonusComEfeito;
        float med = nivel5() && medoNv5 > 0 ? medoNv5 : medo;
        float lent = nivel5() && lentidaoNv5 > 0 ? lentidaoNv5 : lentidao;

        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (!m.estaVivo() || m.posicao.dst(heroi.posicao) > r + m.tamanho / 2f) continue;
            if (d > 0) ferir(heroi, m, m.temEfeito() ? d * (1f + bonus) : d);
            if (empurrar > 0) {
                tmp.set(m.posicao).sub(heroi.posicao).nor().scl(empurrar);
                m.empurrar(tmp.x, tmp.y);
            }
            if (med > 0) m.amedrontar(med);
            if (lent < 1f) m.aplicarLentidao(lent, lentidaoDuracao);
        }
        if (mostrarOnda) ondas.add(new Efeito(heroi.posicao.x, heroi.posicao.y, r, 0.3f));
        return true;
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(ondas, delta); }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(cor);
        if (aura) s.circle(heroi.posicao.x, heroi.posicao.y, raioAtual(heroi));
        for (int i = 0; i < ondas.size; i++) {
            Efeito e = ondas.get(i);
            s.circle(heroi.posicao.x, heroi.posicao.y, e.raio * e.progresso());
        }
    }
}
