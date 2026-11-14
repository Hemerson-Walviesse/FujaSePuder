package com.hemerson.FujaSePuder.hordas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Monstro;
import com.hemerson.FujaSePuder.entidades.TipoMonstro;

/**
 * Controla as hordas de uma fase: intervalo entre hordas, quantos monstros nascem,
 * quais tipos aparecem e como a dificuldade cresce.
 *  - Cada horda: +15% de vida e +8% de dano.
 *  - Cada fase: +70% de vida e +30% de dano (somando) e traz um tipo novo de monstro.
 *    Fase 1 = x1 | Fase 2 = vida x1.7, dano x1.3 | ... | Fase 5 = vida x3.8, dano x2.2
 *  - A horda acaba quando os monstros dela morrem OU quando o tempo dela acaba (a proxima vem mesmo assim).
 *  - Portadores, Guardiao do Selo, boss e lacaios nao seguram a horda.
 */
public class GerenciadorDeHordas {

    public enum Estado { INTERVALO, EM_ANDAMENTO, CONCLUIDA }

    /** Hordas por fase (fase 1 a 5). */
    private static final int[] HORDAS_POR_FASE = { 10, 12, 14, 17, 20 };

    private final int fase;
    private final int totalHordas;
    private final float larguraMundo;
    private final float alturaMundo;
    private final float fatorFase;      // vida
    private final float fatorDanoFase;  // dano

    /** Aumento por fase (some a cada fase). */
    public static final float VIDA_POR_FASE = 0.70f;
    public static final float DANO_POR_FASE = 0.30f;
    /** Aumento por horda dentro da fase. */
    public static final float VIDA_POR_HORDA = 0.15f;
    public static final float DANO_POR_HORDA = 0.08f;

    private int hordaAtual = 0;
    private Estado estado = Estado.INTERVALO;
    private float tempoIntervalo = 2f;
    private int monstrosParaNascer = 0;
    private float tempoProximoSpawn = 0f;
    private boolean fimDeHordaPendente = false;
    private float tempoHorda = 0f;

    /** Tempo maximo de cada horda: 20s + 1,5s por horda (horda 1 = 21,5s ... horda 20 = 50s). */
    public static final float TEMPO_HORDA_BASE = 20f;
    public static final float TEMPO_HORDA_POR_HORDA = 1.5f;

    public GerenciadorDeHordas(int fase, float larguraMundo, float alturaMundo) {
        this.fase = fase;
        this.totalHordas = HORDAS_POR_FASE[MathUtils.clamp(fase, 1, 5) - 1];
        this.larguraMundo = larguraMundo;
        this.alturaMundo = alturaMundo;
        this.fatorFase = fatorDaFase(fase);
        this.fatorDanoFase = 1f + DANO_POR_FASE * (fase - 1);
    }

    public static float fatorDaFase(int fase) {
        return 1f + VIDA_POR_FASE * (fase - 1);
    }

    public void atualizar(float delta, Array<Monstro> monstros, Vector2 posicaoHeroi) {
        switch (estado) {
            case INTERVALO:
                tempoIntervalo -= delta;
                if (tempoIntervalo <= 0) iniciarProximaHorda();
                break;

            case EM_ANDAMENTO:
                tempoHorda -= delta;
                if (monstrosParaNascer > 0) {
                    tempoProximoSpawn -= delta;
                    if (tempoProximoSpawn <= 0) {
                        monstros.add(criarMonstro(posicaoHeroi));
                        monstrosParaNascer--;
                        tempoProximoSpawn = intervaloEntreSpawns();
                    }
                }
                boolean limpou = monstrosParaNascer == 0 && monstrosDaHorda(monstros) == 0;
                if (limpou || tempoHorda <= 0) {
                    monstrosParaNascer = 0;   // tempo acabou: quem ficou vivo continua, a proxima vem
                    fimDeHordaPendente = true;
                    if (hordaAtual >= totalHordas) {
                        estado = Estado.CONCLUIDA;
                    } else {
                        estado = Estado.INTERVALO;
                        tempoIntervalo = 3f;
                    }
                }
                break;

            case CONCLUIDA:
                break;
        }
    }

    /** Monstros que seguram a horda (os especiais das fases nao contam). */
    public static int monstrosDaHorda(Array<Monstro> monstros) {
        int n = 0;
        for (int i = 0; i < monstros.size; i++) {
            TipoMonstro t = monstros.get(i).tipo;
            if (t == TipoMonstro.PORTADOR_CHAVE || t == TipoMonstro.PORTADOR_PORTAL || t == TipoMonstro.GUARDIAO_SELO
                || t == TipoMonstro.BOSS || t == TipoMonstro.LACAIO) continue;
            n++;
        }
        return n;
    }

    private void iniciarProximaHorda() {
        hordaAtual++;
        tempoHorda = TEMPO_HORDA_BASE + TEMPO_HORDA_POR_HORDA * hordaAtual;
        monstrosParaNascer = 4 + hordaAtual * 2;   // horda 1 = 6 monstros, horda 10 = 24, horda 20 = 44
        tempoProximoSpawn = 0f;
        estado = Estado.EM_ANDAMENTO;
    }

    private float intervaloEntreSpawns() {
        return Math.max(0.15f, 1.0f - hordaAtual * 0.06f);
    }

    /** Multiplicador de vida: horda x fase. */
    private float dificuldade() {
        return (1f + (hordaAtual - 1) * VIDA_POR_HORDA) * fatorFase;
    }

    /** Multiplicador de dano: horda x fase. */
    private float dificuldadeDano() {
        return (1f + (hordaAtual - 1) * DANO_POR_HORDA) * fatorDanoFase;
    }

    private Monstro criarMonstro(Vector2 posicaoHeroi) {
        // Nasce logo fora da area visivel, em uma direcao aleatoria a partir do heroi
        float angulo = MathUtils.random(MathUtils.PI2);
        float distancia = 500f + MathUtils.random(100f);
        float x = MathUtils.clamp(posicaoHeroi.x + MathUtils.cos(angulo) * distancia, 0, larguraMundo);
        float y = MathUtils.clamp(posicaoHeroi.y + MathUtils.sin(angulo) * distancia, 0, alturaMundo);

        float d = dificuldade();
        float dd = dificuldadeDano();
        int h = hordaAtual;

        // Tipos novos das fases (sorteados primeiro)
        if (fase >= 5 && MathUtils.randomBoolean(0.08f))
            return new Monstro(x, y, 45 * d, 70 + h * 2, 5 * dd, 26, Color.GOLDENROD, 0.30f).comTipo(TipoMonstro.CURANDEIRO);
        if (fase >= 5 && MathUtils.randomBoolean(0.10f))
            return new Monstro(x, y, 30 * d, 120 + h * 3, 10 * dd, 24, Color.CORAL, 0.20f).comTipo(TipoMonstro.EXPLOSIVO);
        if (fase >= 4 && MathUtils.randomBoolean(0.12f))
            return new Monstro(x, y, 90 * d, 60 + h * 2, 15 * dd, 38, Color.SLATE, 0.35f).comTipo(TipoMonstro.BLINDADO);
        if (fase >= 3 && MathUtils.randomBoolean(0.12f))
            return new Monstro(x, y, 40 * d, 100 + h * 3, 12 * dd, 28, Color.LIGHT_GRAY, 0.25f).comTipo(TipoMonstro.FANTASMA);
        if (fase >= 2 && MathUtils.randomBoolean(0.15f))
            return new Monstro(x, y, 35 * d, 80 + h * 2, 8 * dd, 26, Color.SALMON, 0.25f).comTipo(TipoMonstro.ATIRADOR);

        // Tanque (horda 5+ ou fase 2+)
        if ((h >= 5 || fase >= 2) && MathUtils.randomBoolean(0.15f))
            return new Monstro(x, y, 120 * d, 45 + h * 2, 20 * dd, 44, Color.BROWN, 0.40f).comTipo(TipoMonstro.TANQUE);
        // Corredor (horda 3+ ou fase 2+)
        if ((h >= 3 || fase >= 2) && MathUtils.randomBoolean(0.25f))
            return new Monstro(x, y, 25 * d, 130 + h * 4, 8 * dd, 22, Color.LIME, 0.30f).comTipo(TipoMonstro.CORREDOR);
        // Comum
        return new Monstro(x, y, 50 * d, 70 + h * 3, 10 * dd, 30, Color.FOREST, 0.20f).comTipo(TipoMonstro.COMUM);
    }

    /** Monstro extra (dificuldade da horda atual), usado depois das hordas enquanto os altares nao enchem. */
    public Monstro criarMonstroExtra(Vector2 posicaoHeroi) {
        return criarMonstro(posicaoHeroi);
    }

    /** Retorna true uma unica vez logo depois que uma horda termina. */
    public boolean consumirFimDeHorda() {
        boolean terminou = fimDeHordaPendente;
        fimDeHordaPendente = false;
        return terminou;
    }

    public int getFase() { return fase; }
    public float getFatorFase() { return fatorFase; }
    public int getHordaAtual() { return hordaAtual; }
    public int getTotalHordas() { return totalHordas; }
    public float getTempoIntervalo() { return tempoIntervalo; }
    public boolean emAndamento() { return estado == Estado.EM_ANDAMENTO; }
    public float getTempoRestanteHorda() { return Math.max(0f, tempoHorda); }
    public boolean emIntervalo() { return estado == Estado.INTERVALO; }
    public boolean concluida() { return estado == Estado.CONCLUIDA; }
}
