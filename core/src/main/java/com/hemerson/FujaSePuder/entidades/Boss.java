package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Base dos bosses: muita vida, resiste a controle (25% do efeito), tem 1 FRAQUEZA (+50% de dano)
 * e 2 RESISTENCIAS (-50%). Cada boss da fase cria seus ataques em atualizarAtaques().
 * Tem ainda a "investida" pronta para quem quiser usar.
 */
public abstract class Boss extends Monstro {

    public final String nome;
    protected Elemento fraqueza;
    protected Elemento[] resistencias;

    // Investida (opcional para cada boss)
    private float tempoPreparando = 0f;
    private float tempoInvestida = 0f;

    protected Boss(float x, float y, String nome, float vida, float velocidade, float dano, float tamanho,
                   Color cor, Elemento fraqueza, Elemento... resistencias) {
        super(x, y, vida, velocidade, dano, tamanho, cor, 0f);
        this.nome = nome;
        this.fraqueza = fraqueza;
        this.resistencias = resistencias;
        this.tipo = TipoMonstro.BOSS;
        this.resistenciaControle = 0.25f;
    }

    /** Cria tiros, meteoros, lacaios... Chamado pela TelaJogo todo frame. */
    public abstract void atualizarAtaques(float delta, ContextoCombate ctx);

    /** Texto extra na barra do boss (ex.: "ESCUDO"). */
    public String estado() { return enraivecido() ? "ENRAIVECIDO" : ""; }

    @Override
    protected float multiplicadorElemento(Elemento elemento) {
        float m = 1f;
        if (elemento == fraqueza) m = 1.5f;
        for (Elemento r : resistencias) if (r == elemento) m = 0.5f;
        return m;
    }

    public Elemento getFraqueza() { return fraqueza; }
    public Elemento[] getResistencias() { return resistencias; }
    public boolean enraivecido() { return vida < vidaMaxima * 0.5f; }

    // ---------------------------------------------------------------- investida

    /** Inicia a investida: para (aviso vermelho) e depois avanca rapido. */
    protected void iniciarInvestida() {
        if (tempoPreparando <= 0 && tempoInvestida <= 0) tempoPreparando = 0.7f;
    }

    /** Chamar todo frame por bosses que usam investida. */
    protected void atualizarInvestida(float delta) {
        if (tempoPreparando > 0) {
            tempoPreparando -= delta;
            multiplicadorVelocidade = 0f;
            if (tempoPreparando <= 0) tempoInvestida = 0.6f;
        } else if (tempoInvestida > 0) {
            tempoInvestida -= delta;
            multiplicadorVelocidade = 4.5f;
        } else {
            multiplicadorVelocidade = 1f;
        }
    }

    public boolean preparandoInvestida() { return tempoPreparando > 0; }
    public boolean emInvestida() { return tempoInvestida > 0; }

    // ---------------------------------------------------------------- ataques prontos

    /** Tiros em todas as direcoes. */
    protected void rajada(ContextoCombate ctx, int quantidade, float velocidadeTiro, float danoTiro) {
        float inicio = MathUtils.random(360f);
        for (int k = 0; k < quantidade; k++) {
            ctx.projeteisInimigos.add(new ProjetilInimigo(posicao, inicio + k * 360f / quantidade,
                velocidadeTiro, 9f, danoTiro, 800f));
        }
    }

    /** Tiros mirados no heroi, abertos em leque. */
    protected void leque(ContextoCombate ctx, int quantidade, float abertura, float velocidadeTiro, float danoTiro) {
        float angulo = MathUtils.atan2(ctx.heroi.posicao.y - posicao.y, ctx.heroi.posicao.x - posicao.x) * MathUtils.radiansToDegrees;
        for (int k = 0; k < quantidade; k++) {
            float a = quantidade == 1 ? angulo : angulo - abertura / 2f + abertura * k / (quantidade - 1);
            ctx.projeteisInimigos.add(new ProjetilInimigo(posicao, a, velocidadeTiro, 8f, danoTiro, 800f));
        }
    }

    /** Meteoros: um em cima do heroi e o resto em volta. */
    protected void chuvaDeMeteoros(ContextoCombate ctx, int quantidade, float raio, float danoMeteoro) {
        Vector2 h = ctx.heroi.posicao;
        ctx.meteoros.add(new Meteoro(h.x, h.y, raio, danoMeteoro));
        for (int k = 1; k < quantidade; k++) {
            float x = MathUtils.clamp(h.x + MathUtils.random(-200f, 200f), 0, ctx.larguraMundo);
            float y = MathUtils.clamp(h.y + MathUtils.random(-200f, 200f), 0, ctx.alturaMundo);
            ctx.meteoros.add(new Meteoro(x, y, raio, danoMeteoro));
        }
    }

    /** Invoca lacaios em volta do boss. Retorna o ultimo criado (ou null). */
    protected Monstro invocarLacaios(ContextoCombate ctx, int quantidade, com.badlogic.gdx.utils.Array<Monstro> guardar) {
        Monstro ultimo = null;
        for (int k = 0; k < quantidade; k++) {
            float a = k * MathUtils.PI2 / quantidade;
            float f = ctx.fatorFase;
            Monstro m = new Monstro(posicao.x + MathUtils.cos(a) * 80f, posicao.y + MathUtils.sin(a) * 80f,
                30f * f, 140f, 8f * f, 20f, Color.OLIVE, 0f).comTipo(TipoMonstro.LACAIO);
            ctx.monstros.add(m);
            if (guardar != null) guardar.add(m);
            ultimo = m;
        }
        return ultimo;
    }

    /** Fabrica o boss de cada fase. */
    public static Boss daFase(int fase, float x, float y) {
        switch (fase) {
            case 2:  return new BossRainha(x, y);
            case 3:  return new BossEspectro(x, y);
            case 4:  return new BossGolem(x, y);
            case 5:  return new BossAbismo(x, y);
            default: return new BossGuardiao(x, y);
        }
    }
}
