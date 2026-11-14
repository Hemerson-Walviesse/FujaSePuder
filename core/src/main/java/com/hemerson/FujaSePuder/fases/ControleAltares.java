package com.hemerson.FujaSePuder.fases;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Altar;
import com.hemerson.FujaSePuder.entidades.ContextoCombate;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;
import com.hemerson.FujaSePuder.entidades.TipoMonstro;
import com.hemerson.FujaSePuder.itens.ItemDrop;

/**
 * Regras dos altares de cada fase:
 *  Fase 1: altares visiveis e abertos.
 *  Fase 2: altares SELADOS - cace o Portador da Chave (dourado, foge) e leve a chave ate o altar.
 *  Fase 3: altares ESCONDIDOS - entre num PORTAL (aparece por alguns segundos) para ser levado ate um altar.
 *  Fase 4: ESCONDIDOS e SELADOS - portal + chave.
 *  Fase 5: ESCONDIDOS, portais TRANCADOS (precisa da Chave do Portal, solta pelo Portador Sombrio)
 *          e o altar so abre quando o GUARDIAO DO SELO morre dentro dele.
 */
public class ControleAltares {

    public static final int QUANTIDADE_ALTARES = 2;
    private static final float DISTANCIA_MINIMA_ALTARES = 700f;
    private static final float TEMPO_FUGA_PORTADOR = 20f;   // depois disso o portador some
    private static final float INTERVALO_PORTADOR = 12f;
    private static final float INTERVALO_PORTAL = 10f;
    private static final float INTERVALO_GUARDIAO = 8f;
    private static final float DURACAO_CHAVE_NO_CHAO = 30f;

    /** Lugares possiveis dos altares (cantos e meio das bordas). */
    private static final float[][] POSICOES_ALTAR = {
        { 300, 250 }, { 1300, 250 }, { 300, 710 }, { 1300, 710 },
        { 800, 150 }, { 800, 810 }, { 200, 480 }, { 1400, 480 } };

    public final int fase;
    public final Array<Altar> altares = new Array<>();
    public final Array<ItemDrop> chaves = new Array<>();
    public Portal portal = null;
    public int chavesAltar = 0;
    public int chavesPortal = 0;

    public final boolean selados;
    public final boolean escondidos;
    public final boolean portaisTrancados;
    public final boolean guardiaoDoSelo;

    private final float larguraMundo;
    private final float alturaMundo;
    private float tempoPortador = 8f;
    private float tempoPortadorPortal = 6f;
    private float tempoPortal = 4f;
    private float tempoGuardiao = 3f;

    // Mensagens para a tela
    public String mensagem = "";
    public float tempoMensagem = 0f;

    public ControleAltares(int fase, int sacrificiosPorAltar, float larguraMundo, float alturaMundo) {
        this.fase = fase;
        this.larguraMundo = larguraMundo;
        this.alturaMundo = alturaMundo;
        selados = fase == 2 || fase == 4 || fase == 5;
        escondidos = fase >= 3;
        portaisTrancados = fase == 5;
        guardiaoDoSelo = fase == 5;

        for (int i = 0; i < QUANTIDADE_ALTARES; i++) {
            float[] pos = posicaoLivre();
            Altar a = new Altar(pos[0], pos[1], sacrificiosPorAltar);
            a.selado = selados;
            a.abreComGuardiao = guardiaoDoSelo;
            a.escondido = escondidos;
            altares.add(a);
        }
    }

    /** Texto de abertura da fase. */
    public String descricaoDaFase() {
        switch (fase) {
            case 2: return "Altares SELADOS: cace o PORTADOR DA CHAVE (dourado) e leve a chave ate o altar";
            case 3: return "Altares ESCONDIDOS: entre num PORTAL para ser levado ate eles";
            case 4: return "Altares ESCONDIDOS e SELADOS: ache os PORTAIS e cace o PORTADOR DA CHAVE";
            case 5: return "Portais TRANCADOS (cace o Portador Sombrio) e o GUARDIAO DO SELO deve morrer DENTRO do altar";
            default: return "Leve as hordas ate os 2 ALTARES e mate os monstros dentro deles";
        }
    }

    private void avisar(String texto) {
        mensagem = texto;
        tempoMensagem = 2.5f;
    }

    // ------------------------------------------------------------------ atualizacao

    /** bossLiberado = true depois que o boss apareceu (para de criar portadores, portais e guardioes). */
    public void atualizar(float delta, ContextoCombate ctx, boolean bossLiberado) {
        Heroi heroi = ctx.heroi;
        if (tempoMensagem > 0) tempoMensagem -= delta;

        // Chaves no chao
        for (int i = chaves.size - 1; i >= 0; i--) {
            ItemDrop c = chaves.get(i);
            c.atualizar(delta);
            if (c.colideCom(heroi)) {
                chaves.removeIndex(i);
                if (c.tipo == ItemDrop.Tipo.CHAVE_PORTAL) {
                    chavesPortal++;
                    avisar("Voce pegou a CHAVE DO PORTAL!");
                } else {
                    chavesAltar++;
                    avisar("Voce pegou a CHAVE DO ALTAR! Leve ate um altar selado.");
                }
            } else if (c.expirou()) {
                chaves.removeIndex(i);
            }
        }

        // Portadores que fugiram
        for (int i = ctx.monstros.size - 1; i >= 0; i--) {
            Monstro m = ctx.monstros.get(i);
            if (m.ehPortador()) {
                m.posicao.x = MathUtils.clamp(m.posicao.x, 15f, larguraMundo - 15f);
                m.posicao.y = MathUtils.clamp(m.posicao.y, 15f, alturaMundo - 15f);
                if (m.tempoDeVida >= TEMPO_FUGA_PORTADOR && m.estaVivo()) {
                    ctx.monstros.removeIndex(i);
                    avisar("O portador FUGIU! Outro vai aparecer.");
                }
            }
        }

        // Abrir altar com a chave: basta entrar no altar segurando uma chave
        for (int i = 0; i < altares.size; i++) {
            Altar a = altares.get(i);
            if (a.selado && !a.escondido && !a.abreComGuardiao && chavesAltar > 0 && a.contem(heroi.posicao)) {
                a.selado = false;
                chavesAltar--;
                avisar("ALTAR ABERTO! Agora ele aceita sacrificios.");
            }
        }

        // Portal
        if (portal != null) {
            portal.atualizar(delta);
            if (portal.posicao.dst(heroi.posicao) < portal.raio + heroi.tamanho / 2f) {
                if (portal.trancado && chavesPortal <= 0) {
                    if (tempoMensagem <= 0) avisar("PORTAL TRANCADO: cace o Portador Sombrio para pegar a chave.");
                } else {
                    entrarNoPortal(heroi);
                }
            }
            if (portal != null && portal.acabou()) portal = null;
        }

        if (bossLiberado) return;

        // Criar portador da chave do altar
        if (precisaDePortadorChave(ctx)) {
            tempoPortador -= delta;
            if (tempoPortador <= 0) {
                criarPortador(ctx, TipoMonstro.PORTADOR_CHAVE);
                tempoPortador = INTERVALO_PORTADOR;
            }
        }
        // Criar portador da chave do portal (fase 5)
        if (precisaDePortadorPortal(ctx)) {
            tempoPortadorPortal -= delta;
            if (tempoPortadorPortal <= 0) {
                criarPortador(ctx, TipoMonstro.PORTADOR_PORTAL);
                tempoPortadorPortal = INTERVALO_PORTADOR;
            }
        }
        // Criar portal enquanto houver altar escondido
        if (portal == null && temAltarEscondido()) {
            tempoPortal -= delta;
            if (tempoPortal <= 0) {
                criarPortal(heroi);
                tempoPortal = INTERVALO_PORTAL;
            }
        }
        // Criar Guardiao do Selo (fase 5) quando ha altar encontrado e selado
        if (precisaDeGuardiao(ctx)) {
            tempoGuardiao -= delta;
            if (tempoGuardiao <= 0) {
                criarGuardiao(ctx);
                tempoGuardiao = INTERVALO_GUARDIAO;
            }
        }
    }

    private boolean precisaDePortadorChave(ContextoCombate ctx) {
        if (guardiaoDoSelo) return false; // na fase 5 o altar abre com o Guardiao
        int necessarias = 0;
        for (int i = 0; i < altares.size; i++) if (altares.get(i).selado) necessarias++;
        int disponiveis = chavesAltar + contarChaves(ItemDrop.Tipo.CHAVE_ALTAR) + contarMonstros(ctx, TipoMonstro.PORTADOR_CHAVE);
        return necessarias > disponiveis;
    }

    private boolean precisaDePortadorPortal(ContextoCombate ctx) {
        if (!portaisTrancados || !temAltarEscondido()) return false;
        return chavesPortal + contarChaves(ItemDrop.Tipo.CHAVE_PORTAL) + contarMonstros(ctx, TipoMonstro.PORTADOR_PORTAL) == 0;
    }

    private boolean precisaDeGuardiao(ContextoCombate ctx) {
        if (!guardiaoDoSelo) return false;
        boolean altarEsperando = false;
        for (int i = 0; i < altares.size; i++) {
            Altar a = altares.get(i);
            if (a.selado && !a.escondido) altarEsperando = true;
        }
        return altarEsperando && contarMonstros(ctx, TipoMonstro.GUARDIAO_SELO) == 0;
    }

    private boolean temAltarEscondido() {
        for (int i = 0; i < altares.size; i++) if (altares.get(i).escondido) return true;
        return false;
    }

    private int contarChaves(ItemDrop.Tipo tipo) {
        int n = 0;
        for (int i = 0; i < chaves.size; i++) if (chaves.get(i).tipo == tipo) n++;
        return n;
    }

    private static int contarMonstros(ContextoCombate ctx, TipoMonstro tipo) {
        int n = 0;
        for (int i = 0; i < ctx.monstros.size; i++) if (ctx.monstros.get(i).tipo == tipo && ctx.monstros.get(i).estaVivo()) n++;
        return n;
    }

    private Vector2 pontoLonge(Vector2 heroi, float minimo, float maximo) {
        for (int t = 0; t < 30; t++) {
            float a = MathUtils.random(MathUtils.PI2);
            float d = MathUtils.random(minimo, maximo);
            float x = heroi.x + MathUtils.cos(a) * d;
            float y = heroi.y + MathUtils.sin(a) * d;
            if (x > 60 && x < larguraMundo - 60 && y > 60 && y < alturaMundo - 60) return new Vector2(x, y);
        }
        return new Vector2(MathUtils.random(100f, larguraMundo - 100f), MathUtils.random(100f, alturaMundo - 100f));
    }

    private void criarPortador(ContextoCombate ctx, TipoMonstro tipo) {
        Vector2 p = pontoLonge(ctx.heroi.posicao, 250f, 380f);
        float f = ctx.fatorFase;
        boolean doPortal = tipo == TipoMonstro.PORTADOR_PORTAL;
        Monstro m = new Monstro(p.x, p.y, 70f * f, 165f, 4f, 26f, doPortal ? Color.ROYAL : Color.GOLD, 0f).comTipo(tipo);
        ctx.monstros.add(m);
        avisar(doPortal ? "Um PORTADOR SOMBRIO apareceu! Ele tem a chave do portal." : "Um PORTADOR DA CHAVE apareceu! Pegue antes que ele fuja.");
    }

    private void criarGuardiao(ContextoCombate ctx) {
        Vector2 p = pontoLonge(ctx.heroi.posicao, 420f, 550f);
        float f = ctx.fatorFase;
        Monstro m = new Monstro(p.x, p.y, 300f * f, 55f, 25f, 50f, Color.FIREBRICK, 0f).comTipo(TipoMonstro.GUARDIAO_SELO);
        ctx.monstros.add(m);
        avisar("O GUARDIAO DO SELO apareceu! Atraia ele para DENTRO do altar e mate-o la.");
    }

    private void criarPortal(Heroi heroi) {
        Vector2 p = pontoLonge(heroi.posicao, 300f, 500f);
        portal = new Portal(p.x, p.y, portaisTrancados);
        avisar(portaisTrancados ? "Um PORTAL TRANCADO apareceu! Siga a seta azul." : "Um PORTAL apareceu! Siga a seta azul antes que ele feche.");
    }

    private void entrarNoPortal(Heroi heroi) {
        Altar destino = null;
        for (int i = 0; i < altares.size; i++) {
            if (altares.get(i).escondido) {
                destino = altares.get(i);
                break;
            }
        }
        if (destino == null) return;
        if (portal.trancado) chavesPortal--;
        destino.escondido = false;
        heroi.posicao.set(destino.posicao).add(destino.raio * 0.6f, 0);
        heroi.ficarInvencivel(1f);
        portal = null;
        tempoPortal = INTERVALO_PORTAL;
        avisar(destino.selado
            ? (destino.abreComGuardiao ? "ALTAR ENCONTRADO! Ele esta selado: o Guardiao do Selo deve morrer aqui."
                                       : "ALTAR ENCONTRADO! Ele esta selado: traga a chave.")
            : "ALTAR ENCONTRADO! Leve as hordas ate aqui.");
    }

    // ------------------------------------------------------------------ mortes

    /**
     * Chamado quando um monstro morre. Trata chaves, Guardiao do Selo e sacrificios.
     * Retorna true se o altar mudou de lugar (para a tela avisar).
     */
    public boolean aoMonstroMorrer(Monstro morto) {
        switch (morto.tipo) {
            case PORTADOR_CHAVE:
                chaves.add(new ItemDrop(morto.posicao.x, morto.posicao.y, ItemDrop.Tipo.CHAVE_ALTAR, DURACAO_CHAVE_NO_CHAO));
                avisar("O portador soltou a CHAVE DO ALTAR!");
                return false;
            case PORTADOR_PORTAL:
                chaves.add(new ItemDrop(morto.posicao.x, morto.posicao.y, ItemDrop.Tipo.CHAVE_PORTAL, DURACAO_CHAVE_NO_CHAO));
                avisar("O Portador Sombrio soltou a CHAVE DO PORTAL!");
                return false;
            case GUARDIAO_SELO:
                for (int i = 0; i < altares.size; i++) {
                    Altar a = altares.get(i);
                    if (a.selado && a.abreComGuardiao && !a.escondido && a.contem(morto.posicao)) {
                        a.selado = false;
                        avisar("O SELO SE QUEBROU! O altar aceita sacrificios.");
                        return false;
                    }
                }
                avisar("O Guardiao morreu FORA do altar. Outro vai aparecer.");
                return false;
            case LACAIO:
                return false;
            default:
                break;
        }
        // Sacrificio comum
        for (int i = 0; i < altares.size; i++) {
            Altar a = altares.get(i);
            if (!a.ativo() || a.completo() || !a.contem(morto.posicao)) continue;
            a.registrarSacrificio();
            if (!a.completo() && a.etapa() > a.etapaMovida) {
                a.etapaMovida = a.etapa();
                float[] nova = posicaoLivre();
                a.mover(nova[0], nova[1]);
                return true;
            }
            return false;
        }
        return false;
    }

    // ------------------------------------------------------------------ consultas

    public boolean completos() {
        for (int i = 0; i < altares.size; i++) if (!altares.get(i).completo()) return false;
        return true;
    }

    public int totalSacrificios() {
        int t = 0;
        for (int i = 0; i < altares.size; i++) t += altares.get(i).getSacrificios();
        return t;
    }

    public int totalNecessario() {
        int t = 0;
        for (int i = 0; i < altares.size; i++) t += altares.get(i).getNecessario();
        return t;
    }

    /** Sorteia um lugar longe (>= 700) de todos os altares atuais; se nao houver, o mais distante. */
    private float[] posicaoLivre() {
        Array<float[]> validas = new Array<>();
        float[] maisDistante = POSICOES_ALTAR[0];
        float melhorDistancia = -1f;
        for (float[] p : POSICOES_ALTAR) {
            float menor = Float.MAX_VALUE;
            for (int i = 0; i < altares.size; i++) {
                Altar a = altares.get(i);
                menor = Math.min(menor, Vector2.dst(p[0], p[1], a.posicao.x, a.posicao.y));
            }
            if (menor >= DISTANCIA_MINIMA_ALTARES) validas.add(p);
            if (menor > melhorDistancia) {
                melhorDistancia = menor;
                maisDistante = p;
            }
        }
        return validas.size > 0 ? validas.random() : maisDistante;
    }
}
