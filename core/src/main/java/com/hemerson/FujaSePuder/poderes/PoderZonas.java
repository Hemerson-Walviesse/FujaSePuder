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
 * Poder generico que cria ZONAS: minas, pocas, nuvens, pilares, meteoros, ondas, redemoinhos...
 * O "modo" diz onde as zonas aparecem.
 */
public class PoderZonas extends Poder {

    public enum Modo {
        NO_HEROI,      // embaixo do heroi (fumaca, santuario, minas)
        PERSISTENTE,   // uma zona que segue o heroi para sempre (chuva acida)
        MAIS_PROXIMO,  // no monstro mais proximo (coquetel)
        ALEATORIOS,    // em monstros aleatorios (pilar de fogo, tempestade)
        DENSO,         // onde ha mais monstros (meteoro, rede, ceu aberto, redemoinho)
        LINHA,         // varios golpes em linha (ataque aereo)
        ONDA,          // zona que anda reto (tsunami)
        VAGANTE        // zona que anda numa direcao aleatoria (ciclone)
    }

    // ---- Configuracao
    public Modo modo = Modo.DENSO;
    public float intervalo = 2f;
    public float alcanceAlvo = 350f;
    public int quantidade = 1;
    public int quantidadeNv5 = -1;
    public int maxAtivas = 99;
    public int maxAtivasNv5 = -1;
    public Color cor = Color.WHITE;

    // Modelo da zona
    public float raio = 50f;
    public float raioNv5 = -1f;
    public float duracao = 1f;
    public float atraso = 0f;
    public float intervaloTick = 0.5f;
    public float danoBase = 0f;
    public float danoNv5Extra = 0f;         // dano a mais no Nv5 (Rede, Redemoinho)
    public float lentidao = 1f;
    public float queimaduraBase = 0f;
    public float atordoar = 0f;
    public float puxar = 0f;
    public float vulneravelNv5 = 0f;
    public boolean unica = false;
    public boolean gatilho = false;
    public float velocidadeZona = 0f;       // ONDA / VAGANTE
    public float espacamento = 70f;         // LINHA
    public float atrasoPasso = 0.12f;       // LINHA
    public boolean cruzNv5 = false;         // LINHA/ONDA: segunda linha cruzada no Nv5
    public float reducaoHeroi = 0f;         // Santuario
    public float curaHeroiNv5 = 0f;         // Santuario Nv5 (fracao da vida por segundo)
    public boolean invencivelNv5 = false;   // Bomba de Fumaca Nv5
    public boolean usaElementoArma = false; // Coquetel

    private final ListaZonas zonas = new ListaZonas();
    private final Array<Monstro> usados = new Array<>();
    private final Vector2 dir = new Vector2();

    public PoderZonas(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return intervalo; }

    @Override
    public Elemento elemento(Heroi heroi) {
        return usaElementoArma ? ArmaElemental.elemento(heroi) : tipo.elemento;
    }

    private int qtd() { return nivel5() && quantidadeNv5 > 0 ? quantidadeNv5 : quantidade; }
    private float raioAtual() { return nivel5() && raioNv5 > 0 ? raioNv5 : raio; }

    @Override
    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        zonas.atualizar(delta, this, heroi, monstros);
        super.atualizar(delta, heroi, monstros, projeteis);
    }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        int limite = nivel5() && maxAtivasNv5 > 0 ? maxAtivasNv5 : maxAtivas;
        float alc = alcance(heroi, alcanceAlvo);
        int n = qtd();

        switch (modo) {
            case PERSISTENTE: {
                if (zonas.size() > 0) return true;
                Zona z = nova(heroi.posicao.x, heroi.posicao.y, heroi, 0f);
                z.seguirHeroi = true;
                z.duracao = 1e9f;
                zonas.add(z);
                return true;
            }
            case NO_HEROI: {
                if (zonas.size() >= limite) return false;
                if (!gatilho && maisProximo(heroi.posicao, monstros, alc, null) == null) return false;
                zonas.add(nova(heroi.posicao.x, heroi.posicao.y, heroi, 0f));
                if (invencivelNv5 && nivel5()) heroi.ficarInvencivel(1f);
                return true;
            }
            case MAIS_PROXIMO: {
                Monstro m = maisProximo(heroi.posicao, monstros, alc, null);
                if (m == null) return false;
                zonas.add(nova(m.posicao.x, m.posicao.y, heroi, 0f));
                return true;
            }
            case ALEATORIOS: {
                usados.clear();
                for (int i = 0; i < monstros.size; i++) {
                    Monstro m = monstros.get(i);
                    if (m.estaVivo() && naVisao(m) && m.posicao.dst(heroi.posicao) <= alc) usados.add(m);
                }
                if (usados.size == 0) return false;
                for (int k = 0; k < n; k++) {
                    Monstro m = usados.random();
                    zonas.add(nova(m.posicao.x + MathUtils.random(-15f, 15f), m.posicao.y + MathUtils.random(-15f, 15f), heroi, 0f));
                }
                return true;
            }
            case DENSO: {
                usados.clear();
                for (int k = 0; k < n; k++) {
                    Monstro m = maisDenso(heroi.posicao, monstros, alc, usados);
                    if (m == null) break;
                    usados.add(m);
                    zonas.add(nova(m.posicao.x, m.posicao.y, heroi, 0f));
                }
                return usados.size > 0;
            }
            case LINHA: {
                Monstro m = maisProximo(heroi.posicao, monstros, alc, null);
                if (m == null) return false;
                dir.set(m.posicao).sub(heroi.posicao).nor();
                linha(heroi, dir, n);
                if (cruzNv5 && nivel5()) {
                    // segunda linha cruzando no meio do caminho
                    float meio = espacamento * (n - 1) / 2f;
                    Vector2 centro = new Vector2(heroi.posicao).mulAdd(dir, espacamento + meio);
                    Vector2 perp = new Vector2(-dir.y, dir.x);
                    for (int k = 0; k < n; k++) {
                        float off = (k - (n - 1) / 2f) * espacamento;
                        zonas.add(nova(centro.x + perp.x * off, centro.y + perp.y * off, heroi, atraso + atrasoPasso * k));
                    }
                }
                return true;
            }
            case ONDA: {
                Monstro m = maisProximo(heroi.posicao, monstros, alc, null);
                if (m == null) return false;
                dir.set(m.posicao).sub(heroi.posicao).nor();
                onda(heroi, dir);
                if (cruzNv5 && nivel5()) onda(heroi, new Vector2(-dir.y, dir.x));
                return true;
            }
            case VAGANTE: {
                if (maisProximo(heroi.posicao, monstros, alc, null) == null) return false;
                for (int k = 0; k < n; k++) {
                    Zona z = nova(heroi.posicao.x, heroi.posicao.y, heroi, 0f);
                    z.velocidade.set(velocidadeZona, 0).rotateDeg(MathUtils.random(360f));
                    zonas.add(z);
                }
                return true;
            }
        }
        return false;
    }

    private void linha(Heroi heroi, Vector2 direcao, int n) {
        for (int k = 0; k < n; k++) {
            float dist = espacamento * (k + 1);
            zonas.add(nova(heroi.posicao.x + direcao.x * dist, heroi.posicao.y + direcao.y * dist, heroi, atraso + atrasoPasso * k));
        }
    }

    private void onda(Heroi heroi, Vector2 direcao) {
        Zona z = nova(heroi.posicao.x, heroi.posicao.y, heroi, 0f);
        z.velocidade.set(direcao).nor().scl(velocidadeZona);
        zonas.add(z);
    }

    /** Cria uma zona a partir do modelo configurado. */
    private Zona nova(float x, float y, Heroi heroi, float atrasoZona) {
        Zona z = new Zona(x, y, raioAtual(), cor);
        z.duracao = duracao;
        z.atraso = Math.max(atraso, atrasoZona);
        z.intervaloTick = intervaloTick;
        z.unica = unica;
        z.gatilho = gatilho;
        float d = danoBase + (nivel5() ? danoNv5Extra : 0f);
        z.dano = d > 0 ? dano(heroi, d) : 0f;
        z.lentidao = lentidao;
        z.queimadura = queimaduraBase > 0 ? dano(heroi, queimaduraBase) : 0f;
        z.atordoar = atordoar;
        z.puxar = puxar;
        if (nivel5() && vulneravelNv5 > 0) z.vulneravel = vulneravelNv5;
        z.reducaoHeroi = reducaoHeroi;
        if (nivel5() && curaHeroiNv5 > 0) z.curaHeroiPorSegundo = heroi.vidaMaxima * curaHeroiNv5;

        // Coquetel: o efeito muda com o elemento da arma
        if (usaElementoArma && heroi.elemento != null) {
            z.cor = heroi.elemento.cor;
            switch (heroi.elemento) {
                case FOGO:  z.queimadura = dano(heroi, 5f); break;
                case GELO:  z.lentidao = 0.5f; break;
                case LASER: z.dano *= 1.3f; break;
            }
        }
        return z;
    }

    @Override
    public void aoRemover(Heroi heroi) {
        zonas.zonas.clear();
    }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        zonas.desenharLinhas(s);
    }

    @Override
    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        zonas.desenharFormas(s);
    }
}
