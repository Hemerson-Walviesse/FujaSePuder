package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * FASE 4 - Golem de Pedra: ARMADURA (recebe 30% do dano) ate voce quebrar 3 pontos fracos.
 * A cada 6s um ponto fraco abre por 2.5s (contorno amarelo): cause 300 de dano nele para quebrar.
 * Terremoto: aneis de pedras caindo em volta dele.
 */
public class BossGolem extends Boss {
    private static final float DANO_PARA_QUEBRAR = 300f;

    private int pontosFracos = 3;
    private float tempoProximoPonto = 4f;
    private float tempoPontoAberto = 0f;
    private float danoNoPonto = 0f;
    private float tempoTerremoto = 5f;

    public BossGolem(float x, float y) {
        super(x, y, "Golem de Pedra", 7500f, 45f, 30f, 110f, Color.BROWN,
            Elemento.ELETRICO, Elemento.FISICO, Elemento.FOGO);
    }

    @Override
    public float receberDano(float quantidade, Elemento elemento) {
        float real = super.receberDano(quantidade, elemento);
        if (pontoAberto()) {
            danoNoPonto += real;
            if (danoNoPonto >= DANO_PARA_QUEBRAR) {
                pontosFracos--;
                tempoPontoAberto = 0f;
                danoNoPonto = 0f;
            }
        }
        return real;
    }

    @Override
    protected float multiplicadorElemento(Elemento elemento) {
        float m = super.multiplicadorElemento(elemento);
        return (pontosFracos > 0 && !pontoAberto()) ? m * 0.3f : m;
    }

    @Override
    public void atualizarAtaques(float delta, ContextoCombate ctx) {
        if (pontosFracos > 0) {
            if (tempoPontoAberto > 0) {
                tempoPontoAberto -= delta;
            } else {
                tempoProximoPonto -= delta;
                if (tempoProximoPonto <= 0) {
                    tempoPontoAberto = 2.5f;
                    danoNoPonto = 0f;
                    tempoProximoPonto = 6f;
                }
            }
        }

        tempoTerremoto -= delta;
        if (tempoTerremoto <= 0) {
            int[] quantidades = { 8, 12, 16 };
            for (int anel = 0; anel < 3; anel++) {
                float raioAnel = 130f * (anel + 1);
                for (int k = 0; k < quantidades[anel]; k++) {
                    float a = k * MathUtils.PI2 / quantidades[anel] + anel * 0.3f;
                    float x = MathUtils.clamp(posicao.x + MathUtils.cos(a) * raioAnel, 0, ctx.larguraMundo);
                    float y = MathUtils.clamp(posicao.y + MathUtils.sin(a) * raioAnel, 0, ctx.alturaMundo);
                    ctx.meteoros.add(new Meteoro(x, y, 40f, 22f, 0.8f + 0.4f * anel));
                }
            }
            tempoTerremoto = enraivecido() ? 5f : 7f;
        }
    }

    public boolean pontoAberto() { return tempoPontoAberto > 0; }
    public int getPontosFracos() { return pontosFracos; }

    @Override
    public String estado() {
        if (pontosFracos <= 0) return enraivecido() ? "ARMADURA QUEBRADA | ENRAIVECIDO" : "ARMADURA QUEBRADA";
        String s = "ARMADURA (" + pontosFracos + " pontos fracos)";
        if (pontoAberto()) s += " - PONTO FRACO ABERTO!";
        return s;
    }
}
