package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Base das armas do Humano: o tiro muda conforme o elemento escolhido (Fogo, Gelo ou Laser). */
public abstract class PoderArma extends Poder {

    private static final float RAIO_EXPLOSAO_BASE = 35f;
    private final Array<Efeito> explosoes = new Array<>();

    protected PoderArma(TipoPoder tipo) {
        super(tipo);
    }

    /** Distancia para procurar alvo. */
    protected float alcanceMira(Heroi heroi) {
        return heroi.elemento == ElementoArma.LASER ? alcance(heroi, 450f) : alcance(heroi, 350f);
    }

    private float alcanceTiro(Heroi heroi) {
        float a = alcance(heroi, 420f);
        if (heroi.elemento == ElementoArma.LASER) a *= 1.3f * (1f + 0.25f * heroi.nivelElemento(1));
        return a;
    }

    protected void disparar(Heroi heroi, Array<Projetil> projeteis, Vector2 direcao, float dano) {
        ElementoArma e = heroi.elemento;
        float velocidade = 620f;
        float raio = 5f;
        int perfuracoes = 0;
        Color cor = e != null ? e.cor : tipo.cor;

        if (e == ElementoArma.LASER) {
            velocidade = 950f;
            raio = 4f;
            perfuracoes = Projetil.ATRAVESSA_TUDO;
            dano *= 1f + 0.2f * heroi.nivelElemento(0);
        }
        projeteis.add(new Projetil(this, heroi.posicao, direcao, velocidade, raio, dano, alcanceTiro(heroi), perfuracoes, cor));
    }

    @Override
    public void aoAcertar(Projetil p, Monstro alvo, Heroi heroi, Array<Monstro> monstros) {
        ElementoArma e = heroi.elemento;

        if (e == ElementoArma.FOGO) {
            // Alvo leva o dano todo; quem estiver perto leva 50%. Todos queimam.
            float raio = RAIO_EXPLOSAO_BASE * (1f + 0.25f * heroi.nivelElemento(1));
            float queimadura = dano(heroi, 3f) * (1f + 0.5f * heroi.nivelElemento(0));
            ferir(heroi, alvo, p.dano);
            alvo.aplicarQueimadura(queimadura, 2.5f);
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m != alvo && m.estaVivo() && m.posicao.dst(p.posicao) <= raio + m.tamanho / 2f) {
                    ferir(heroi, m, p.dano * 0.5f);
                    m.aplicarQueimadura(queimadura, 2.5f);
                }
            }
            explosoes.add(new Efeito(p.posicao.x, p.posicao.y, raio, 0.2f));

        } else if (e == ElementoArma.GELO) {
            ferir(heroi, alvo, p.dano);
            alvo.aplicarLentidao(Math.max(0.2f, 0.6f - 0.08f * heroi.nivelElemento(0)), 2f);
            float chanceCongelar = Math.min(0.5f, 0.05f + 0.10f * heroi.nivelElemento(1));
            if (MathUtils.randomBoolean(chanceCongelar)) alvo.atordoar(1f);

        } else {
            ferir(heroi, alvo, p.dano); // sem elemento ou Laser
        }
    }

    @Override
    protected void atualizarEfeitos(float delta) {
        Efeito.atualizarTodos(explosoes, delta);
    }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(Color.ORANGE);
        for (int i = 0; i < explosoes.size; i++) {
            Efeito ex = explosoes.get(i);
            s.circle(ex.x, ex.y, ex.raio * (0.3f + 0.7f * ex.progresso()));
        }
    }
}
