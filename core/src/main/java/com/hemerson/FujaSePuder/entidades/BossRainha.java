package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

/** FASE 2 - Rainha da Colmeia: invoca lacaios; enquanto houver lacaio vivo, ela tem ESCUDO (30% do dano). */
public class BossRainha extends Boss {
    private final Array<Monstro> lacaios = new Array<>();
    private float tempoLacaios = 1.5f;
    private float tempoFerroes = 3f;

    public BossRainha(float x, float y) {
        super(x, y, "Rainha da Colmeia", 5000f, 55f, 25f, 85f, Color.GOLDENROD,
            Elemento.FOGO, Elemento.GELO, Elemento.ELETRICO);
    }

    private boolean comEscudo() {
        for (int i = lacaios.size - 1; i >= 0; i--) {
            if (!lacaios.get(i).estaVivo()) lacaios.removeIndex(i);
        }
        return lacaios.size > 0;
    }

    @Override
    protected float multiplicadorElemento(Elemento elemento) {
        float m = super.multiplicadorElemento(elemento);
        return comEscudo() ? m * 0.3f : m;
    }

    @Override
    public void atualizarAtaques(float delta, ContextoCombate ctx) {
        tempoLacaios -= delta;
        if (tempoLacaios <= 0) {
            invocarLacaios(ctx, enraivecido() ? 5 : 4, lacaios);
            tempoLacaios = 6f;
        }
        tempoFerroes -= delta;
        if (tempoFerroes <= 0) {
            leque(ctx, enraivecido() ? 5 : 3, 40f, 280f, 14f);
            tempoFerroes = enraivecido() ? 2.5f : 4f;
        }
    }

    @Override
    public String estado() {
        String s = comEscudo() ? "ESCUDO: mate os lacaios" : "";
        if (enraivecido()) s = s.isEmpty() ? "ENRAIVECIDA" : s + " | ENRAIVECIDA";
        return s;
    }

    public boolean escudoAtivo() { return comEscudo(); }
}
