package com.hemerson.FujaSePuder.poderes;

/** Efeito visual curto (explosao, onda, feixe...). So para desenho, nao causa dano. */
public class Efeito {
    public float x, y, x2, y2, raio;
    public float tempo;
    public final float duracao;
    public float[] pontos; // usado pelo raio em cadeia

    public Efeito(float x, float y, float raio, float duracao) {
        this.x = x;
        this.y = y;
        this.raio = raio;
        this.duracao = duracao;
        this.tempo = duracao;
    }

    /** 0 no inicio, 1 no fim. */
    public float progresso() {
        return 1f - tempo / duracao;
    }

    public boolean acabou() {
        return tempo <= 0;
    }

    /** Atualiza e remove os efeitos que acabaram. */
    public static void atualizarTodos(com.badlogic.gdx.utils.Array<Efeito> efeitos, float delta) {
        for (int i = efeitos.size - 1; i >= 0; i--) {
            efeitos.get(i).tempo -= delta;
            if (efeitos.get(i).acabou()) efeitos.removeIndex(i);
        }
    }
}
