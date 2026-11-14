package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.ContextoCombate;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/**
 * Poderes passivos e de gatilho, cada um com sua regra (switch no tipo):
 * Escudo de Energia, Kit Medico, Adrenalina, Sede de Sangue, Marca da Morte, Bencao, Juizo,
 * Guardiao, Asas Infernais, Asas Velozes, Eco Trovejante, Explosao de Cadaver.
 */
public class PoderPassivo extends Poder {

    private float timer = 0f;
    private float tempoAtivo = 0f;
    private int contador = 0;
    private float janelaExplosoes = 0f;
    private int explosoesNaJanela = 0;
    /** Explosao de Cadaver: no maximo isso por segundo (evita reacao em cadeia infinita). */
    private static final int MAX_EXPLOSOES_POR_SEGUNDO = 6;
    private final ListaZonas zonas = new ListaZonas();
    private final Array<Efeito> efeitos = new Array<>();
    private final Vector2 ultimaPosicao = new Vector2();
    private final Vector2 tmp = new Vector2();
    private final Array<Monstro> marcados = new Array<>();

    public PoderPassivo(TipoPoder tipo) {
        super(tipo);
    }

    @Override protected float intervaloBase() { return 1f; }
    @Override protected boolean usar(Heroi h, Array<Monstro> m, Array<Projetil> p) { return false; }

    @Override
    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        zonas.atualizar(delta, this, heroi, monstros);
        Efeito.atualizarTodos(efeitos, delta);
        float fr = fatorRecarga(heroi);
        janelaExplosoes += delta;
        if (janelaExplosoes >= 1f) {
            janelaExplosoes = 0f;
            explosoesNaJanela = 0;
        }

        switch (tipo) {
            case ESCUDO_ENERGIA: {
                float maximo = nivel5() ? 60f : 20f + 5f * (nivel - 1);
                if (heroi.escudo < maximo) {
                    timer += delta;
                    if (timer >= 10f * fr) {
                        heroi.escudo = maximo;
                        timer = 0f;
                    }
                }
                // Nv5: reflete o dano absorvido em quem estiver perto
                if (heroi.absorvidoPeloEscudo > 0) {
                    if (nivel5()) {
                        for (int i = 0; i < monstros.size; i++) {
                            Monstro m = monstros.get(i);
                            if (m.posicao.dst(heroi.posicao) <= 120f) ferir(heroi, m, dano(heroi, 30f));
                        }
                        efeitos.add(new Efeito(heroi.posicao.x, heroi.posicao.y, 120f, 0.25f));
                    }
                    heroi.absorvidoPeloEscudo = 0f;
                }
                break;
            }
            case KIT_MEDICO: {
                timer += delta;
                if (timer >= 5f * fr) {
                    heroi.curar(heroi.vidaMaxima * (0.03f + 0.0075f * (nivel - 1)));
                    timer = 0f;
                    efeitos.add(new Efeito(heroi.posicao.x, heroi.posicao.y, 30f, 0.3f));
                }
                break;
            }
            case ADRENALINA: {
                if (heroi.danoRecebidoAcumulado >= heroi.vidaMaxima * 0.25f) {
                    heroi.danoRecebidoAcumulado = 0f;
                    tempoAtivo = 4f;
                }
                if (tempoAtivo > 0) {
                    tempoAtivo -= delta;
                    float bonus = nivel5() ? 1.6f : 1.3f + 0.05f * (nivel - 1);
                    heroi.bonusVelocidadeTemp *= bonus;
                    heroi.bonusDanoTemp *= bonus;
                }
                break;
            }
            case SEDE_DE_SANGUE: {
                float perdida = 1f - heroi.vida / heroi.vidaMaxima;
                heroi.bonusDanoTemp *= 1f + perdida * (nivel5() ? 1.5f : 1f);
                break;
            }
            case MARCA_DA_MORTE: {
                timer += delta;
                if (timer >= 3f * fr) {
                    timer = 0f;
                    marcados.clear();
                    int n = nivel5() ? 3 : 1;
                    for (int k = 0; k < n; k++) {
                        Monstro m = maisForte(heroi.posicao, monstros, 400f, marcados);
                        if (m == null) break;
                        m.marcar(3f);
                        marcados.add(m);
                    }
                }
                break;
            }
            case BENCAO: {
                heroi.bonusVelocidadeTemp *= 1.1f;
                timer += delta;
                if (timer >= 3f * fr) {
                    timer = 0f;
                    heroi.curar(heroi.vidaMaxima * (nivel5() ? 0.04f : 0.02f + 0.004f * (nivel - 1)));
                    efeitos.add(new Efeito(heroi.posicao.x, heroi.posicao.y, 30f, 0.3f));
                }
                break;
            }
            case JUIZO: {
                timer += delta;
                if (timer >= 0.5f) {
                    timer = 0f;
                    float limite = nivel5() ? 0.25f : 0.15f;
                    for (int i = 0; i < monstros.size; i++) {
                        Monstro m = monstros.get(i);
                        if (!m.estaVivo() || ehBoss(m) || !naVisao(m) || m.posicao.dst(heroi.posicao) > alcance(heroi, 300f)) continue;
                        if (m.vida / m.vidaMaxima < limite) {
                            heroi.aoCausarDano(m.vida);
                            m.vida = 0f; // executa
                            efeitos.add(new Efeito(m.posicao.x, m.posicao.y, 22f, 0.3f));
                        }
                    }
                }
                break;
            }
            case GUARDIAO: {
                if (!heroi.bloqueioPronto) {
                    timer += delta;
                    if (timer >= (nivel5() ? 4f : 8f) * fr) {
                        heroi.bloqueioPronto = true;
                        timer = 0f;
                    }
                }
                break;
            }
            case ASAS_INFERNAIS: {
                timer += delta;
                if (timer >= (nivel5() ? 2f : 4f) * fr) {
                    Monstro m = maisProximo(heroi.posicao, monstros, 60f, null);
                    if (m != null) {
                        timer = 0f;
                        tmp.set(heroi.posicao).sub(m.posicao).nor();
                        ultimaPosicao.set(heroi.posicao);
                        float lw = ctx != null ? ctx.larguraMundo : 1600f;
                        float la = ctx != null ? ctx.alturaMundo : 960f;
                        heroi.posicao.mulAdd(tmp, 150f);
                        heroi.posicao.x = MathUtils.clamp(heroi.posicao.x, 16f, lw - 16f);
                        heroi.posicao.y = MathUtils.clamp(heroi.posicao.y, 16f, la - 16f);
                        heroi.ficarInvencivel(0.5f);
                        for (int k = 0; k < 3; k++) {
                            float t = k / 2f;
                            Zona z = new Zona(MathUtils.lerp(ultimaPosicao.x, heroi.posicao.x, t),
                                MathUtils.lerp(ultimaPosicao.y, heroi.posicao.y, t), 35f, Color.ORANGE);
                            z.duracao = 2f;
                            z.intervaloTick = 0.3f;
                            z.dano = dano(heroi, 8f);
                            z.queimadura = dano(heroi, 4f);
                            zonas.add(z);
                        }
                    }
                }
                break;
            }
            case ASAS_VELOZES: {
                heroi.bonusVelocidadeTemp *= 1.25f;
                timer += delta;
                if (timer >= 0.3f && heroi.posicao.dst(ultimaPosicao) > 10f) {
                    timer = 0f;
                    ultimaPosicao.set(heroi.posicao);
                    Zona z = new Zona(heroi.posicao.x, heroi.posicao.y, 25f, Color.TEAL);
                    z.duracao = 1.2f;
                    z.intervaloTick = 0.3f;
                    z.dano = dano(heroi, 5f);
                    if (nivel5()) z.lentidao = 0.6f;
                    zonas.add(z);
                }
                break;
            }
            default:
                break; // ECO_TROVEJANTE e EXPLOSAO_DE_CADAVER usam ganchos
        }
    }

    @Override
    public void aoAtaqueBasico(ContextoCombate ctx) {
        if (tipo != TipoPoder.ECO_TROVEJANTE) return;
        contador++;
        if (contador < (nivel5() ? 3 : 5)) return;
        contador = 0;
        Monstro alvo = maisProximo(ctx.heroi.posicao, ctx.monstros, 350f, null);
        if (alvo == null) return;
        Zona z = new Zona(alvo.posicao.x, alvo.posicao.y, 60f, Color.YELLOW);
        z.unica = true;
        z.atraso = 0.1f;
        z.dano = dano(ctx.heroi, 35f);
        z.atordoar = 0.3f;
        zonas.add(z);
    }

    @Override
    public void aoMonstroMorrer(Monstro morto, ContextoCombate ctx) {
        if (tipo != TipoPoder.EXPLOSAO_DE_CADAVER) return;
        if (explosoesNaJanela >= MAX_EXPLOSOES_POR_SEGUNDO) return;
        explosoesNaJanela++;
        Zona z = new Zona(morto.posicao.x, morto.posicao.y, nivel5() ? 120f : 60f, Color.FIREBRICK);
        z.unica = true;
        z.dano = dano(ctx.heroi, 15f);
        zonas.add(z);
    }

    @Override
    public void aoRemover(Heroi heroi) {
        if (tipo == TipoPoder.ESCUDO_ENERGIA) heroi.escudo = 0f;
        if (tipo == TipoPoder.GUARDIAO) heroi.bloqueioPronto = false;
        zonas.zonas.clear();
    }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        zonas.desenharLinhas(s);
        s.setColor(tipo.cor);
        for (int i = 0; i < efeitos.size; i++) {
            Efeito e = efeitos.get(i);
            s.circle(e.x, e.y, e.raio * (0.4f + 0.6f * e.progresso()));
        }
        // Marca da Morte: X em cima dos marcados
        if (tipo == TipoPoder.MARCA_DA_MORTE) {
            for (int i = 0; i < marcados.size; i++) {
                Monstro m = marcados.get(i);
                if (!m.estaVivo() || !m.estaMarcado()) continue;
                float r = m.tamanho * 0.7f;
                s.line(m.posicao.x - r, m.posicao.y - r, m.posicao.x + r, m.posicao.y + r);
                s.line(m.posicao.x - r, m.posicao.y + r, m.posicao.x + r, m.posicao.y - r);
            }
        }
        // Adrenalina ativa
        if (tipo == TipoPoder.ADRENALINA && tempoAtivo > 0) {
            s.setColor(Color.RED);
            s.circle(heroi.posicao.x, heroi.posicao.y, heroi.tamanho * 0.95f);
        }
    }

    @Override
    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        zonas.desenharFormas(s);
    }
}
