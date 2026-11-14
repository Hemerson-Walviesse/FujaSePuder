package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/** FASE 1 - Guardiao do Altar: investida, rajada em todas as direcoes e chuva de meteoros. */
public class BossGuardiao extends Boss {
    private float tempoInvestida = 4f;
    private float tempoRajada = 2.5f;
    private float tempoMeteoros = 5f;

    public BossGuardiao(float x, float y) {
        super(x, y, "Guardiao do Altar", 3500f, 65f, 25f, 80f, Color.PURPLE,
            Elemento.GELO, Elemento.FOGO, Elemento.VENTO);
    }

    @Override
    public void atualizar(float delta, Vector2 alvo) {
        atualizarInvestida(delta);
        super.atualizar(delta, alvo);
    }

    @Override
    public void atualizarAtaques(float delta, ContextoCombate ctx) {
        tempoInvestida -= delta;
        if (tempoInvestida <= 0) { iniciarInvestida(); tempoInvestida = 4f; }

        tempoRajada -= delta;
        if (tempoRajada <= 0) {
            rajada(ctx, enraivecido() ? 16 : 12, 230f, 15f);
            tempoRajada = enraivecido() ? 3.5f : 5f;
        }
        tempoMeteoros -= delta;
        if (tempoMeteoros <= 0) {
            chuvaDeMeteoros(ctx, enraivecido() ? 7 : 5, 45f, 20f);
            tempoMeteoros = enraivecido() ? 5f : 7f;
        }
    }
}
