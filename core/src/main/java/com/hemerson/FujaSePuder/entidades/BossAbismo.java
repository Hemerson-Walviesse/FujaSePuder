package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * FASE 5 - Senhor do Abismo: luta em 3 etapas (pela vida). A fraqueza muda em cada etapa.
 *  Etapa 1 (100-66%): lacaios + rajadas.            Fraco a Luz,  resiste Sombra e Fogo.
 *  Etapa 2 (66-33%):  fica intangivel + meteoros.     Fraco a Fogo, resiste Gelo e Luz.
 *  Etapa 3 (33-0%):   tudo junto + investida.         Fraco a Gelo, resiste Fogo e Raio/Laser.
 */
public class BossAbismo extends Boss {
    private float tempoLacaios = 2f;
    private float tempoRajada = 3f;
    private float tempoMeteoros = 4f;
    private float tempoInvestida = 4f;
    private float ciclo = 0f;
    private boolean intangivelAgora = false;

    public BossAbismo(float x, float y) {
        super(x, y, "Senhor do Abismo", 12000f, 60f, 30f, 100f, Color.MAROON,
            Elemento.LUZ, Elemento.SOMBRA, Elemento.FOGO);
    }

    public int etapa() {
        float p = vida / vidaMaxima;
        return p > 0.66f ? 1 : p > 0.33f ? 2 : 3;
    }

    private void ajustarElementos() {
        switch (etapa()) {
            case 1: fraqueza = Elemento.LUZ;  resistencias = new Elemento[] { Elemento.SOMBRA, Elemento.FOGO }; break;
            case 2: fraqueza = Elemento.FOGO; resistencias = new Elemento[] { Elemento.GELO, Elemento.LUZ }; break;
            default: fraqueza = Elemento.GELO; resistencias = new Elemento[] { Elemento.FOGO, Elemento.ELETRICO }; break;
        }
    }

    @Override
    public boolean intangivel() { return intangivelAgora; }

    @Override
    protected float multiplicadorElemento(Elemento elemento) {
        if (intangivelAgora && elemento != Elemento.LUZ && elemento != Elemento.ELETRICO) return 0f;
        return super.multiplicadorElemento(elemento);
    }

    @Override
    public void atualizar(float delta, Vector2 alvo) {
        if (etapa() == 3) atualizarInvestida(delta);
        else multiplicadorVelocidade = 1f;
        super.atualizar(delta, alvo);
    }

    @Override
    public void atualizarAtaques(float delta, ContextoCombate ctx) {
        ajustarElementos();
        int etapa = etapa();

        // Lacaios: etapas 1 e 3
        if (etapa != 2) {
            tempoLacaios -= delta;
            if (tempoLacaios <= 0) {
                invocarLacaios(ctx, etapa == 3 ? 5 : 4, null);
                tempoLacaios = etapa == 3 ? 6f : 7f;
            }
        }
        // Rajada: etapas 1 e 3
        if (etapa != 2) {
            tempoRajada -= delta;
            if (tempoRajada <= 0) {
                rajada(ctx, etapa == 3 ? 18 : 12, 240f, 16f);
                tempoRajada = etapa == 3 ? 3f : 4f;
            }
        }
        // Intangivel: etapa 2 (3s a cada 7s)
        if (etapa == 2) {
            ciclo += delta;
            if (!intangivelAgora && ciclo >= 4f) { intangivelAgora = true; ciclo = 0f; }
            else if (intangivelAgora && ciclo >= 3f) { intangivelAgora = false; ciclo = 0f; }
        } else {
            intangivelAgora = false;
        }
        // Meteoros: etapas 2 e 3
        if (etapa != 1) {
            tempoMeteoros -= delta;
            if (tempoMeteoros <= 0) {
                chuvaDeMeteoros(ctx, etapa == 3 ? 8 : 6, 50f, 22f);
                tempoMeteoros = etapa == 3 ? 4f : 5f;
            }
        }
        // Investida: etapa 3
        if (etapa == 3) {
            tempoInvestida -= delta;
            if (tempoInvestida <= 0) { iniciarInvestida(); tempoInvestida = MathUtils.random(3f, 4.5f); }
        }
    }

    @Override
    public String estado() {
        String s = "ETAPA " + etapa() + "/3";
        if (intangivelAgora) s += " | INTANGIVEL: so Luz e Raio/Laser";
        return s;
    }
}
