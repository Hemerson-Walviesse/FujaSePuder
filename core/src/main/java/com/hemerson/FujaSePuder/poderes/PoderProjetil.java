package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Elemento;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/**
 * Poder generico de projetil (configurado na FabricaPoderes):
 * pistola, escopeta, fuzil, metralhadora, granada, bumerangue, arpao, lancas, penas, espadas...
 */
public class PoderProjetil extends Poder {

    public enum Mira { MAIS_PROXIMO, MAIS_FORTE, MULTIPLOS, TODAS_DIRECOES }

    // ---- Configuracao (preenchida pela FabricaPoderes)
    public boolean tiro = false;            // usa o elemento da arma do Humano
    public float intervalo = 1f;
    public float danoBase = 10f;
    public float velocidade = 550f;
    public float raio = 5f;
    public float alcance = 400f;
    public float alcanceMira = 350f;
    public int quantidade = 1;
    public int quantidadeNv5 = -1;
    public float espalhamento = 0f;         // graus entre projeteis
    public float tremor = 0f;               // desvio aleatorio (graus)
    public int perfuracoes = 0;
    public int rajada = 1;                  // tiros seguidos (metralhadora)
    public int rajadaNv5 = -1;
    public float intervaloRajada = 0.08f;
    public Mira mira = Mira.MAIS_PROXIMO;
    public boolean todasDirecoesNv5 = false;
    public float multiplicadorNv5 = 1f;     // ex.: critico 2x do Fuzil
    public Color cor = Color.WHITE;

    // Efeitos ao acertar
    public float explosao = 0f;             // raio
    public float queimaduraBase = 0f;
    public float lentidao = 1f;
    public float lentidaoDuracao = 1.5f;
    public float atordoar = 0f;
    public float atordoarNv5 = -1f;
    public float empurrar = 0f;             // >0 empurra, <0 puxa para o heroi
    public boolean explodeNoFim = false;    // granada
    public boolean soExplode = false;       // granada: so causa dano na explosao
    public boolean volta = false;           // bumerangue
    public boolean voltaNv5 = false;

    /** Para a arte: tempo desde o ultimo tiro e para onde foi (arma na mao do Humano). */
    public float tempoDesdeTiro = 99f;
    public final Vector2 direcaoTiro = new Vector2(-1, 0);

    private int rajadaRestante = 0;
    private float tempoRajada = 0f;
    private final Array<Efeito> explosoes = new Array<>();
    private final Vector2 dir = new Vector2();
    private final Vector2 tmp = new Vector2();
    private final Array<Monstro> alvos = new Array<>();

    public PoderProjetil(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return intervalo; }

    @Override
    public Elemento elemento(Heroi heroi) {
        return tiro ? ArmaElemental.elemento(heroi) : tipo.elemento;
    }

    private int qtd() { return nivel5() && quantidadeNv5 > 0 ? quantidadeNv5 : quantidade; }

    @Override
    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        tempoDesdeTiro += delta;
        super.atualizar(delta, heroi, monstros, projeteis);
        if (rajadaRestante > 0) {
            tempoRajada -= delta;
            if (tempoRajada <= 0) {
                if (salva(heroi, monstros, projeteis)) { rajadaRestante--; marcarTiro(); }
                else rajadaRestante = 0;
                tempoRajada = intervaloRajada;
            }
        }
    }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        if (!salva(heroi, monstros, projeteis)) return false;
        marcarTiro();
        int r = nivel5() && rajadaNv5 > 0 ? rajadaNv5 : rajada;
        rajadaRestante = r - 1;
        tempoRajada = intervaloRajada;
        return true;
    }

    private void marcarTiro() {
        tempoDesdeTiro = 0f;
        if (!dir.isZero()) direcaoTiro.set(dir).nor();
    }

    /** Dispara um conjunto de projeteis. Retorna false se nao havia alvo. */
    private boolean salva(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        float mira = alcance(heroi, alcanceMira);
        int n = qtd();
        float d = dano(heroi, danoBase) * (nivel5() ? multiplicadorNv5 : 1f);
        Mira modo = (todasDirecoesNv5 && nivel5()) ? Mira.TODAS_DIRECOES : this.mira;

        if (modo == Mira.TODAS_DIRECOES) {
            if (maisProximo(heroi.posicao, monstros, mira, null) == null) return false;
            float inicio = MathUtils.random(360f);
            for (int k = 0; k < n; k++) {
                dir.set(1, 0).rotateDeg(inicio + k * 360f / n);
                lancar(heroi, projeteis, dir, d, alcance(heroi, alcance));
            }
            return true;
        }

        if (modo == Mira.MULTIPLOS) {
            alvos.clear();
            for (int k = 0; k < n; k++) {
                Monstro m = maisProximo(heroi.posicao, monstros, mira, alvos);
                if (m == null) break;
                alvos.add(m);
            }
            if (alvos.size == 0) return false;
            for (int k = 0; k < n; k++) {
                Monstro m = alvos.get(k % alvos.size);
                dir.set(m.posicao).sub(heroi.posicao).rotateDeg(MathUtils.random(-tremor, tremor));
                lancar(heroi, projeteis, dir, d, Math.max(alcance(heroi, alcance), m.posicao.dst(heroi.posicao) + 40f));
            }
            return true;
        }

        Monstro alvo = modo == Mira.MAIS_FORTE
            ? maisForte(heroi.posicao, monstros, mira, null)
            : maisProximo(heroi.posicao, monstros, mira, null);
        if (alvo == null) return false;

        float distanciaAlvo = alvo.posicao.dst(heroi.posicao);
        for (int k = 0; k < n; k++) {
            float offset = (k - (n - 1) / 2f) * espalhamento + MathUtils.random(-tremor, tremor);
            dir.set(alvo.posicao).sub(heroi.posicao).rotateDeg(offset);
            float alc = explodeNoFim ? Math.max(40f, distanciaAlvo) : alcance(heroi, alcance);
            lancar(heroi, projeteis, dir, d, alc);
        }
        return true;
    }

    private void lancar(Heroi heroi, Array<Projetil> projeteis, Vector2 direcao, float d, float alc) {
        int perf = soExplode ? Projetil.ATRAVESSA_TUDO : perfuracoes;
        Projetil p = tiro
            ? ArmaElemental.criarTiro(this, heroi, heroi.posicao, direcao, d, velocidade, raio, alc, perf, cor)
            : new Projetil(this, heroi.posicao, direcao, velocidade, raio, d, alc, perf, cor);
        p.volta = volta || (voltaNv5 && nivel5());
        p.explodeNoFim = explodeNoFim;
        p.soExplode = soExplode;
        projeteis.add(p);
    }

    @Override
    public void aoAcertar(Projetil p, Monstro alvo, Heroi heroi, Array<Monstro> monstros) {
        if (p.soExplode) return;
        if (tiro && heroi.elemento != null) ArmaElemental.aplicarAcerto(this, p, alvo, heroi, monstros, explosoes);
        else ferir(heroi, alvo, p.dano);
        aplicarEfeitos(alvo, heroi, p);
        if (explosao > 0) explodir(p.posicao, p.dano * 0.5f, heroi, monstros, alvo, p);
    }

    @Override
    public void aoExpirar(Projetil p, Heroi heroi, Array<Monstro> monstros) {
        if (p.explodeNoFim) explodir(p.posicao, p.dano, heroi, monstros, null, p);
    }

    private void explodir(Vector2 onde, float d, Heroi heroi, Array<Monstro> monstros, Monstro ignorar, Projetil p) {
        float r = explosao * (nivel5() ? 1.3f : 1f);
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (m == ignorar || !m.estaVivo() || m.posicao.dst(onde) > r + m.tamanho / 2f) continue;
            ferir(heroi, m, d);
            aplicarEfeitos(m, heroi, p);
        }
        explosoes.add(new Efeito(onde.x, onde.y, r, 0.25f));
    }

    private void aplicarEfeitos(Monstro m, Heroi heroi, Projetil p) {
        if (queimaduraBase > 0) m.aplicarQueimadura(dano(heroi, queimaduraBase), 2.5f);
        if (lentidao < 1f) m.aplicarLentidao(lentidao, lentidaoDuracao);
        float at = nivel5() && atordoarNv5 > 0 ? atordoarNv5 : atordoar;
        if (at > 0) m.atordoar(at);
        if (empurrar > 0) {
            tmp.set(p.velocidade).nor().scl(empurrar);
            m.empurrar(tmp.x, tmp.y);
        } else if (empurrar < 0) {
            tmp.set(heroi.posicao).sub(m.posicao).nor().scl(-empurrar);
            m.empurrar(tmp.x, tmp.y);
        }
    }

    @Override
    protected void atualizarEfeitos(float delta) {
        Efeito.atualizarTodos(explosoes, delta);
    }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        for (int i = 0; i < explosoes.size; i++) {
            Efeito e = explosoes.get(i);
            s.setColor(tiro && heroi.elemento != null ? heroi.elemento.cor : cor);
            s.circle(e.x, e.y, e.raio * (0.3f + 0.7f * e.progresso()));
        }
    }
}
