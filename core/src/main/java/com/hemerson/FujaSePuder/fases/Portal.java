package com.hemerson.FujaSePuder.fases;

import com.badlogic.gdx.math.Vector2;

/** Portal temporario que leva o heroi ate um altar escondido. */
public class Portal {
    public static final float DURACAO = 9f;
    public final Vector2 posicao = new Vector2();
    public final float raio = 28f;
    public final boolean trancado;   // fase 5: precisa da chave do portal
    private float tempo = 0f;

    public Portal(float x, float y, boolean trancado) {
        posicao.set(x, y);
        this.trancado = trancado;
    }

    public void atualizar(float delta) { tempo += delta; }
    public boolean acabou() { return tempo >= DURACAO; }
    public float tempo() { return tempo; }
    /** Pisca nos ultimos 2 segundos. */
    public boolean visivel() { return DURACAO - tempo > 2f || ((int) (tempo * 8)) % 2 == 0; }
}
