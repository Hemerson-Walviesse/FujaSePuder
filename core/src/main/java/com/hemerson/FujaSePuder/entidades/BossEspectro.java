package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * FASE 3 - Espectro: a cada ciclo fica INTANGIVEL por 3s (so Luz e Raio/Laser acertam)
 * e depois se teletransporta perto do heroi. Atira aneis de tiros lentos.
 */
public class BossEspectro extends Boss {
    private float ciclo = 0f;
    private boolean intangivelAgora = false;
    private float tempoAnel = 2f;

    public BossEspectro(float x, float y) {
        super(x, y, "Espectro", 6000f, 85f, 22f, 75f, Color.LIGHT_GRAY,
            Elemento.LUZ, Elemento.SOMBRA, Elemento.FISICO);
    }

    private float duracaoNormal() { return enraivecido() ? 3.5f : 5f; }

    @Override
    public boolean intangivel() { return intangivelAgora; }

    @Override
    protected float multiplicadorElemento(Elemento elemento) {
        if (intangivelAgora && elemento != Elemento.LUZ && elemento != Elemento.ELETRICO) return 0f;
        return super.multiplicadorElemento(elemento);
    }

    @Override
    public void atualizarAtaques(float delta, ContextoCombate ctx) {
        ciclo += delta;
        if (!intangivelAgora && ciclo >= duracaoNormal()) {
            intangivelAgora = true;
            ciclo = 0f;
        } else if (intangivelAgora && ciclo >= 3f) {
            intangivelAgora = false;
            ciclo = 0f;
            // teletransporte para perto do heroi
            float a = MathUtils.random(MathUtils.PI2);
            posicao.set(
                MathUtils.clamp(ctx.heroi.posicao.x + MathUtils.cos(a) * 250f, 0, ctx.larguraMundo),
                MathUtils.clamp(ctx.heroi.posicao.y + MathUtils.sin(a) * 250f, 0, ctx.alturaMundo));
            rajada(ctx, 10, 160f, 14f);
        }

        tempoAnel -= delta;
        if (tempoAnel <= 0 && !intangivelAgora) {
            rajada(ctx, enraivecido() ? 14 : 10, 150f, 14f);
            tempoAnel = enraivecido() ? 2f : 3f;
        }
    }

    @Override
    public String estado() {
        String s = intangivelAgora ? "INTANGIVEL: so Luz e Raio/Laser" : "";
        if (enraivecido()) s = s.isEmpty() ? "ENRAIVECIDO" : s + " | ENRAIVECIDO";
        return s;
    }
}
