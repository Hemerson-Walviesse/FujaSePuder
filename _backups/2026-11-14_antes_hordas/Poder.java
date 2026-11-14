package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.ContextoCombate;
import com.hemerson.FujaSePuder.entidades.Elemento;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;
import com.hemerson.FujaSePuder.entidades.TipoMonstro;

/**
 * Base de todos os poderes. Cada poder dispara sozinho quando recarrega.
 *  - Niveis 1 a 5: cada nivel +15% de dano e recarga 6% mais rapida; o Nv5 da um efeito extra.
 *  - Ressonancia: 3+ poderes do mesmo elemento = +30% de dano neles.
 *  - O dano passa pelo elemento do poder (fraquezas/resistencias dos bosses).
 * Obs.: use loops com indice (for int i) nas listas de monstros, nunca for-each,
 * porque o Array do libGDX nao permite dois for-each aninhados na mesma lista.
 */
public abstract class Poder {

    public static final int NIVEL_MAXIMO = 5;
    /** Ganho por nivel (balanceamento da evolucao do heroi). */
    public static final float DANO_POR_NIVEL = 0.15f;
    public static final float RECARGA_POR_NIVEL = 0.06f;

    public final TipoPoder tipo;
    protected int nivel = 1;
    protected float recarga = 0f;
    protected ContextoCombate ctx;

    protected Poder(TipoPoder tipo) {
        this.tipo = tipo;
    }

    /** Chamado pela TelaJogo todo frame. */
    public void atualizar(float delta, ContextoCombate ctx) {
        this.ctx = ctx;
        atualizar(delta, ctx.heroi, ctx.monstros, ctx.projeteis);
    }

    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        atualizarEfeitos(delta);
        recarga -= delta;
        if (recarga <= 0) {
            boolean usou = usar(heroi, monstros, projeteis);
            recarga = usou ? intervaloBase() * fatorRecarga(heroi) : 0.1f; // sem alvo: tenta de novo logo
        }
    }

    /** Segundos entre usos, no nivel 1 e sem buffs. */
    protected abstract float intervaloBase();

    /** Dispara o poder. Retorna false se nao tinha alvo. */
    protected abstract boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis);

    /** Quando um projetil deste poder acerta um monstro. */
    public void aoAcertar(Projetil projetil, Monstro alvo, Heroi heroi, Array<Monstro> monstros) {
        ferir(heroi, alvo, projetil.dano);
    }

    /** Quando um projetil deste poder chega ao fim do alcance (granadas). */
    public void aoExpirar(Projetil projetil, Heroi heroi, Array<Monstro> monstros) { }

    /** Ganchos opcionais. */
    public void aoAtaqueBasico(ContextoCombate ctx) { }
    public void aoMonstroMorrer(Monstro morto, ContextoCombate ctx) { }
    public void aoRemover(Heroi heroi) { }

    protected void atualizarEfeitos(float delta) { }
    public void desenharLinhas(ShapeRenderer shapes, Heroi heroi) { }
    public void desenharFormas(ShapeRenderer shapes, Heroi heroi) { }

    /** Elemento do dano deste poder (armas do Humano usam o elemento escolhido). */
    public Elemento elemento(Heroi heroi) {
        return tipo.elemento;
    }

    // ------------------------------------------------------------------ niveis

    public int getNivel() { return nivel; }
    public boolean nivelMaximo() { return nivel >= NIVEL_MAXIMO; }
    public void subirNivel() { if (nivel < NIVEL_MAXIMO) nivel++; }
    protected boolean nivel5() { return nivel >= NIVEL_MAXIMO; }

    /** Base * nivel * buffs do heroi * ressonancia. */
    public float dano(Heroi heroi, float base) {
        return base * (1f + DANO_POR_NIVEL * (nivel - 1)) * heroi.fatorDano() * heroi.fatorRessonancia(elemento(heroi));
    }

    /** Cada nivel: recarrega 8% mais rapido. Tambem aplica os buffs de velocidade de ataque. */
    protected float fatorRecarga(Heroi heroi) {
        return (1f - RECARGA_POR_NIVEL * (nivel - 1)) * heroi.fatorVelocidadeAtaque();
    }

    protected float alcance(Heroi heroi, float base) {
        return base * heroi.fatorAlcance();
    }

    // ---------------------------------------------------------------- utilitarios

    /** Causa dano com o elemento do poder. Retorna o dano que entrou. */
    public float ferir(Heroi heroi, Monstro monstro, float quantidade) {
        float real = monstro.receberDano(quantidade, elemento(heroi));
        heroi.aoCausarDano(real);
        return real;
    }

    protected static Monstro maisProximo(Vector2 de, Array<Monstro> monstros, float alcanceMaximo, Array<Monstro> ignorar) {
        Monstro escolhido = null;
        float menor = alcanceMaximo;
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (!m.estaVivo() || (ignorar != null && ignorar.contains(m, true))) continue;
            float d = m.posicao.dst(de);
            if (d <= menor) {
                menor = d;
                escolhido = m;
            }
        }
        return escolhido;
    }

    /** Monstro com mais vida dentro do alcance. */
    protected static Monstro maisForte(Vector2 de, Array<Monstro> monstros, float alcanceMaximo, Array<Monstro> ignorar) {
        Monstro escolhido = null;
        float maior = -1f;
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (!m.estaVivo() || m.posicao.dst(de) > alcanceMaximo || (ignorar != null && ignorar.contains(m, true))) continue;
            if (m.vida > maior) {
                maior = m.vida;
                escolhido = m;
            }
        }
        return escolhido;
    }

    /** Monstro com mais vizinhos (onde o grupo esta mais cheio). */
    protected static Monstro maisDenso(Vector2 de, Array<Monstro> monstros, float alcanceMaximo, Array<Monstro> ignorar) {
        Monstro escolhido = null;
        int melhor = -1;
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (!m.estaVivo() || m.posicao.dst(de) > alcanceMaximo || (ignorar != null && ignorar.contains(m, true))) continue;
            int vizinhos = 0;
            for (int j = 0; j < monstros.size; j++) {
                if (monstros.get(j).posicao.dst(m.posicao) < 110f) vizinhos++;
            }
            if (vizinhos > melhor) {
                melhor = vizinhos;
                escolhido = m;
            }
        }
        return escolhido;
    }

    protected static boolean ehBoss(Monstro m) {
        return m.tipo == TipoMonstro.BOSS;
    }
}
