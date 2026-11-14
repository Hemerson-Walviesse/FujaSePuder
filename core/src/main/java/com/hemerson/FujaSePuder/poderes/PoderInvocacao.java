package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Elemento;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Invocacoes que lutam sozinhas: Torreta, Drone, Sombra Viva, Diabretes. */
public class PoderInvocacao extends Poder {

    public enum Modo { FIXA, ORBITA, SEGUE, PERSEGUE }

    public Modo modo = Modo.FIXA;
    public boolean permanente = false;      // Drone: fica para sempre
    public boolean tiro = false;            // usa o elemento da arma do Humano
    public boolean corpoACorpo = false;
    public int quantidade = 1;
    public int quantidadeNv5 = -1;
    public float intervalo = 10f;           // recarga para invocar de novo
    public float duracao = 8f;
    public float intervaloAtaque = 0.5f;
    public float alcanceAtaque = 300f;
    public float danoBase = 10f;
    public float velocidadeMovimento = 170f;
    public float tamanho = 12f;
    public Color cor = Color.WHITE;

    private static class Invocacao {
        final Vector2 posicao = new Vector2();
        float tempo;
        float recargaAtaque;
        float angulo;
    }

    private final Array<Invocacao> invocacoes = new Array<>();
    private final Array<Efeito> golpes = new Array<>();
    private final Array<Efeito> explosoes = new Array<>();
    private final Vector2 dir = new Vector2();

    public PoderInvocacao(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return permanente ? 1f : intervalo; }

    @Override
    public Elemento elemento(Heroi heroi) {
        return tiro ? ArmaElemental.elemento(heroi) : tipo.elemento;
    }

    private int qtd() { return nivel5() && quantidadeNv5 > 0 ? quantidadeNv5 : quantidade; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        if (permanente) {
            while (invocacoes.size < qtd()) criar(heroi, invocacoes.size);
            return true;
        }
        if (maisProximo(heroi.posicao, monstros, 450f, null) == null) return false;
        for (int k = 0; k < qtd(); k++) criar(heroi, k);
        return true;
    }

    private void criar(Heroi heroi, int indice) {
        Invocacao inv = new Invocacao();
        inv.posicao.set(heroi.posicao).add(MathUtils.random(-30f, 30f), MathUtils.random(-30f, 30f));
        inv.angulo = indice * MathUtils.PI2 / Math.max(1, qtd());
        invocacoes.add(inv);
    }

    @Override
    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        super.atualizar(delta, heroi, monstros, projeteis);
        Efeito.atualizarTodos(golpes, delta);
        Efeito.atualizarTodos(explosoes, delta);
        float d = dano(heroi, danoBase);

        for (int i = invocacoes.size - 1; i >= 0; i--) {
            Invocacao inv = invocacoes.get(i);
            inv.tempo += delta;
            if (!permanente && inv.tempo >= duracao) {
                invocacoes.removeIndex(i);
                continue;
            }
            mover(inv, delta, heroi, monstros, i);

            inv.recargaAtaque -= delta;
            if (inv.recargaAtaque > 0) continue;
            Monstro alvo = maisProximo(inv.posicao, monstros, alcanceAtaque, null);
            if (alvo == null) continue;
            inv.recargaAtaque = intervaloAtaque * fatorRecarga(heroi);
            if (corpoACorpo) {
                ferir(heroi, alvo, d);
                Efeito e = new Efeito(inv.posicao.x, inv.posicao.y, 0, 0.1f);
                e.x2 = alvo.posicao.x;
                e.y2 = alvo.posicao.y;
                golpes.add(e);
            } else {
                dir.set(alvo.posicao).sub(inv.posicao);
                Projetil p = tiro
                    ? ArmaElemental.criarTiro(this, heroi, inv.posicao, dir, d, 560f, 5f, alcanceAtaque + 80f, 0, cor)
                    : new Projetil(this, inv.posicao, dir, 520f, 5f, d, alcanceAtaque + 80f, 0, cor);
                projeteis.add(p);
            }
        }
    }

    private void mover(Invocacao inv, float delta, Heroi heroi, Array<Monstro> monstros, int indice) {
        switch (modo) {
            case ORBITA: {
                inv.angulo += 2.5f * delta;
                inv.posicao.set(heroi.posicao).add(MathUtils.cos(inv.angulo) * 60f, MathUtils.sin(inv.angulo) * 60f);
                break;
            }
            case SEGUE: {
                float a = indice * 2.4f + 2.2f;
                dir.set(heroi.posicao).add(MathUtils.cos(a) * 45f, MathUtils.sin(a) * 45f).sub(inv.posicao);
                if (dir.len() > 4f) inv.posicao.mulAdd(dir.nor(), Math.min(dir.len(), heroi.velocidadeAtual() * 1.2f * delta));
                break;
            }
            case PERSEGUE: {
                Monstro alvo = maisProximo(inv.posicao, monstros, 500f, null);
                if (alvo != null && alvo.posicao.dst(inv.posicao) > alcanceAtaque * 0.8f) {
                    dir.set(alvo.posicao).sub(inv.posicao).nor();
                    inv.posicao.mulAdd(dir, velocidadeMovimento * delta);
                } else if (alvo == null && inv.posicao.dst(heroi.posicao) > 60f) {
                    dir.set(heroi.posicao).sub(inv.posicao).nor();
                    inv.posicao.mulAdd(dir, velocidadeMovimento * delta);
                }
                break;
            }
            default:
                break; // FIXA
        }
    }

    @Override
    public void aoAcertar(Projetil p, Monstro alvo, Heroi heroi, Array<Monstro> monstros) {
        if (tiro && heroi.elemento != null) ArmaElemental.aplicarAcerto(this, p, alvo, heroi, monstros, explosoes);
        else ferir(heroi, alvo, p.dano);
    }

    @Override
    public void aoRemover(Heroi heroi) {
        invocacoes.clear();
    }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(cor);
        for (int i = 0; i < golpes.size; i++) {
            Efeito e = golpes.get(i);
            s.line(e.x, e.y, e.x2, e.y2);
        }
        s.setColor(Color.ORANGE);
        for (int i = 0; i < explosoes.size; i++) {
            Efeito e = explosoes.get(i);
            s.circle(e.x, e.y, e.raio * (0.3f + 0.7f * e.progresso()));
        }
    }

    @Override
    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        for (int i = 0; i < invocacoes.size; i++) {
            Invocacao inv = invocacoes.get(i);
            s.setColor(cor);
            if (modo == Modo.FIXA) s.rect(inv.posicao.x - tamanho, inv.posicao.y - tamanho, tamanho * 2, tamanho * 2);
            else s.circle(inv.posicao.x, inv.posicao.y, tamanho);
            s.setColor(Color.BLACK);
            s.circle(inv.posicao.x, inv.posicao.y, tamanho * 0.35f);
        }
    }
}
