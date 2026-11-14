package com.hemerson.FujaSePuder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.hemerson.FujaSePuder.entidades.Altar;
import com.hemerson.FujaSePuder.entidades.Boss;
import com.hemerson.FujaSePuder.entidades.BossGolem;
import com.hemerson.FujaSePuder.entidades.BossGuardiao;
import com.hemerson.FujaSePuder.entidades.BossRainha;
import com.hemerson.FujaSePuder.entidades.ClasseHeroi;
import com.hemerson.FujaSePuder.entidades.ContextoCombate;
import com.hemerson.FujaSePuder.entidades.Elemento;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Meteoro;
import com.hemerson.FujaSePuder.entidades.Monstro;
import com.hemerson.FujaSePuder.entidades.ProjetilInimigo;
import com.hemerson.FujaSePuder.entidades.TipoMonstro;
import com.hemerson.FujaSePuder.fases.ControleAltares;
import com.hemerson.FujaSePuder.fases.Portal;
import com.hemerson.FujaSePuder.hordas.GerenciadorDeHordas;
import com.hemerson.FujaSePuder.itens.ItemDrop;
import com.hemerson.FujaSePuder.itens.Recompensa;
import com.hemerson.FujaSePuder.itens.RecompensaElemento;
import com.hemerson.FujaSePuder.itens.RecompensaPerderBuff;
import com.hemerson.FujaSePuder.itens.RecompensaRara;
import com.hemerson.FujaSePuder.itens.RecompensaRemoverPoder;
import com.hemerson.FujaSePuder.itens.RecompensaTrocaClasse;
import com.hemerson.FujaSePuder.itens.SorteioRecompensas;
import com.hemerson.FujaSePuder.itens.TipoBuff;
import com.hemerson.FujaSePuder.poderes.Efeito;
import com.hemerson.FujaSePuder.poderes.ElementoArma;
import com.hemerson.FujaSePuder.poderes.Poder;
import com.hemerson.FujaSePuder.poderes.PoderProjetil;
import com.hemerson.FujaSePuder.poderes.Projetil;
import com.hemerson.FujaSePuder.poderes.RecompensaEpico;
import com.hemerson.FujaSePuder.poderes.RecompensaPoder;
import com.hemerson.FujaSePuder.poderes.TipoPoder;

/**
 * Tela do jogo: 5 fases, cada uma com hordas, 2 altares de sacrificio e um boss.
 * Cada fase muda a regra dos altares (selados, escondidos, portais...): ver fases/ControleAltares.
 * Desktop: WASD/setas andam, ESPACO = habilidade, E = epico.
 * Android: toque e segure para andar; botoes redondos para habilidade e epico.
 * Teclas de teste: P = pula para a proxima fase, B = invoca o boss agora.
 */
public class TelaJogo extends ScreenAdapter {

    private static final float LARGURA = 800f;
    private static final float ALTURA = 480f;
    public static final int TOTAL_FASES = 5;

    // O mundo e maior que a tela: a camera segue o heroi
    private static final float MUNDO_LARGURA = 1600f;
    private static final float MUNDO_ALTURA = 960f;
    private static final float TAMANHO_GRADE = 80f;
    private static final float TAMANHO_TILE_CHAO = 320f; // quanto do mundo cada repeticao do chao ocupa

    // Botoes (usados no celular, mas tambem funcionam com clique)
    private static final float BOTAO_X = LARGURA - 70f;
    private static final float BOTAO_Y = 70f;
    private static final float BOTAO_RAIO = 45f;
    private static final float BOTAO_EPICO_X = BOTAO_X - 105f;
    private static final float BOTAO_EPICO_RAIO = 38f;

    // Cards da escolha de recompensa
    private static final float CARD_LARGURA = 210f;
    private static final float CARD_ALTURA = 200f;
    private static final float CARD_PEQUENO_LARGURA = 180f;
    private static final float CARD_PEQUENO_ALTURA = 80f;

    /** Horda em que o Humano escolhe o elemento da arma (Fogo, Gelo ou Laser), na fase 1. */
    private static final int HORDA_ESCOLHA_ELEMENTO = 3;

    private enum ModoEscolha { RECOMPENSA, PRIMEIRO_PODER, ELEMENTO, TROCA_ELEMENTO, PERDER_BUFF, BOSS, REMOVER_PODER, NOVO_PODER }

    // Drops de vida dos monstros
    private static final int MAX_DROPS_POR_HORDA = 1;
    private static final float CURA_DO_DROP = 25f;

    // Altares: CADA altar pede 30 sacrificios na fase 1, +10 por fase (maximo 80)
    private static final int SACRIFICIOS_FASE_1 = 30;
    private static final int SACRIFICIOS_POR_FASE = 10;
    private static final int SACRIFICIOS_MAXIMO = 80;
    private static final float INTERVALO_MONSTRO_EXTRA = 0.8f; // depois das hordas, enquanto os altares nao enchem
    private static final int MAX_MONSTROS_EXTRA = 25;

    private final FujaSePuder jogo;
    private final ClasseHeroi classeInicial;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(LARGURA, ALTURA, camera);
    private final OrthographicCamera cameraHud = new OrthographicCamera();
    private final FitViewport viewportHud = new FitViewport(LARGURA, ALTURA, cameraHud);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fonte = new BitmapFont();

    private final Vector3 toque = new Vector3();
    private final Vector3 toqueHud = new Vector3();
    private final Vector2 movimento = new Vector2();
    private final Color corTemp = new Color();

    // ---- Artes (assets/sprites). Se o arquivo nao existir, continua desenhando a forma geometrica.
    private final Texture texChao = carregarTextura("sprites/chao_fase1.png", true);
    private final Texture texAltar = carregarTextura("sprites/altar.png", false);
    private final Texture texHeroiHumano = carregarTextura("sprites/heroi_humano.png", false);
    private final Texture texHeroiAnjo = carregarTextura("sprites/heroi_anjo.png", false);
    private final Texture texHeroiDemonio = carregarTextura("sprites/heroi_demonio.png", false);
    private final Texture texMonstroComum = carregarTextura("sprites/monstro_comum.png", false);
    private final Texture texBossGuardiao = carregarTextura("sprites/boss_guardiao.png", false);
    private float heroiXAnterior, heroiYAnterior;
    private float heroiAndando;  // 0 = parado, 1 = andando (suavizado)
    private float tempoAnim;     // relogio das animacoes
    private boolean heroiOlhandoEsquerda = true; // as artes originais olham para a esquerda

    // Andar: pernas animadas (arte cortada em corpo + 2 pernas), spritesheets opcionais e poeira
    private final Texture texHeroiHumanoSemArma = carregarTextura("sprites/heroi_humano_sem_arma.png", false);
    private boolean artesExtrasCarregadas;
    private final Array<Texture> texturasExtras = new Array<>();
    private final ObjectMap<Texture, float[]> pernas = new ObjectMap<>();          // {altura das pernas, divisao}
    private final ObjectMap<Texture, TextureRegion[]> framesAndando = new ObjectMap<>();
    private final Array<float[]> poeira = new Array<>();                           // x, y, tempo, lado
    private float passoHeroiAnterior;

    // Arma na mao do Humano
    private final ObjectMap<TipoPoder, Texture> texArmas = new ObjectMap<>();
    private float anguloArma = 200f;
    private float tempoDesdeAtaqueBasico = 99f;
    private final Vector2 direcaoAtaqueBasico = new Vector2(-1, 0);
    private TipoPoder armaNaMao = TipoPoder.PISTOLA;
    private float armaDesdeTiro = 99f;
    private final Array<Vector2> linhasAtaque = new Array<>();
    private final Array<Monstro> atingidos = new Array<>();
    private final Array<Efeito> efeitosCura = new Array<>();
    private float tempoEfeitoAtaque;

    private Heroi heroi;
    private final ContextoCombate ctx = new ContextoCombate();
    private final Array<Monstro> monstros = new Array<>();
    private final Array<Projetil> projeteis = new Array<>();
    private final Array<ProjetilInimigo> projeteisInimigos = new Array<>();
    private final Array<Meteoro> meteoros = new Array<>();
    private final Array<ItemDrop> drops = new Array<>();
    private GerenciadorDeHordas hordas;
    private int abates;

    // Recompensas
    private final Array<Recompensa> opcoes = new Array<>();
    private final Array<Rectangle> cards = new Array<>();
    private ModoEscolha modo = ModoEscolha.RECOMPENSA;
    private int escolhasPendentes;
    private int dropsNestaHorda;
    private int hordaDosDrops;

    // Fase / altares / boss
    private int faseAtual = 1;
    private ControleAltares controle;
    private Boss boss;
    private boolean bossDerrotado;
    private boolean recompensaBossPendente;
    private boolean faseConcluida;
    private boolean jogoVencido;
    private float tempoMonstroExtra;
    private float tempoAvisoAltar;
    private float tempoAvisoFase;

    public TelaJogo(FujaSePuder jogo, ClasseHeroi classe) {
        this.jogo = jogo;
        this.classeInicial = classe;
        reiniciar();
    }

    /** Nova partida do zero. */
    private void reiniciar() {
        heroi = new Heroi(MUNDO_LARGURA / 2f, MUNDO_ALTURA / 2f, classeInicial);
        abates = 0;
        jogoVencido = false;
        opcoes.clear();
        escolhasPendentes = 0;
        iniciarFase(1);
    }

    /** Comeca uma fase: hordas novas, altares novos, boss da fase. O heroi continua com tudo. */
    private void iniciarFase(int fase) {
        faseAtual = fase;
        hordas = new GerenciadorDeHordas(fase, MUNDO_LARGURA, MUNDO_ALTURA);
        monstros.clear();
        projeteis.clear();
        projeteisInimigos.clear();
        meteoros.clear();
        drops.clear();
        efeitosCura.clear();
        linhasAtaque.clear();
        tempoEfeitoAtaque = 0f;
        dropsNestaHorda = 0;
        hordaDosDrops = 0;
        modo = ModoEscolha.RECOMPENSA;
        cards.clear();

        if (fase > 1) {
            heroi.posicao.set(MUNDO_LARGURA / 2f, MUNDO_ALTURA / 2f);
            heroi.curar(heroi.vidaMaxima * 0.5f); // descanso entre fases: recupera 50%
        }

        controle = new ControleAltares(fase, sacrificiosDaFase(fase), MUNDO_LARGURA, MUNDO_ALTURA);
        tempoAvisoAltar = 0f;
        tempoAvisoFase = 5f;
        boss = null;
        bossDerrotado = false;
        recompensaBossPendente = false;
        faseConcluida = false;
        tempoMonstroExtra = 0f;

        ctx.heroi = heroi;
        ctx.monstros = monstros;
        ctx.projeteis = projeteis;
        ctx.projeteisInimigos = projeteisInimigos;
        ctx.meteoros = meteoros;
        ctx.larguraMundo = MUNDO_LARGURA;
        ctx.alturaMundo = MUNDO_ALTURA;
        ctx.fatorFase = hordas.getFatorFase();
        atualizarCamera();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f); // evita saltos se o jogo travar por um instante

        if (escolhendoRecompensa()) {
            processarEscolha(); // jogo pausado enquanto escolhe
        } else if (heroi.estaVivo() && !faseConcluida) {
            atualizar(delta);
        } else if (faseConcluida && !jogoVencido && heroi.estaVivo()
                   && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched())) {
            iniciarFase(faseAtual + 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reiniciar();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                   || ((!heroi.estaVivo() || jogoVencido) && Gdx.input.justTouched())) {
            jogo.setScreen(new TelaSelecao(jogo));
            dispose();
            return;
        }

        desenhar();
    }

    // ------------------------------------------------------------------ logica

    private void atualizar(float delta) {
        // Teclas de teste
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && faseAtual < TOTAL_FASES) {
            iniciarFase(faseAtual + 1);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && boss == null && !bossDerrotado) invocarBoss();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || (Gdx.input.justTouched() && tocandoNoBotao())) {
            heroi.usarHabilidade(monstros);
        }
        if (heroi.epico != null && (Gdx.input.isKeyJustPressed(Input.Keys.E)
            || (Gdx.input.justTouched() && tocandoNoBotaoEpico()))) {
            heroi.epico.ativar();
        }

        moverHeroi(delta);
        atualizarCamera();
        heroi.atualizar(delta);
        hordas.atualizar(delta, monstros, heroi.posicao);

        // Recompensa garantida ao terminar cada horda (menos a ultima, que leva ao boss)
        if (hordas.consumirFimDeHorda() && !hordas.concluida()) escolhasPendentes++;
        atualizarAltaresEBoss(delta);
        controle.atualizar(delta, ctx, boss != null || bossDerrotado);

        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            m.atualizar(delta, heroi.posicao);
            if (m.colideCom(heroi)) heroi.receberDano(m.dano);
            if (m.consumirTiro()) {
                float ang = MathUtils.atan2(heroi.posicao.y - m.posicao.y, heroi.posicao.x - m.posicao.x) * MathUtils.radiansToDegrees;
                projeteisInimigos.add(new ProjetilInimigo(m.posicao, ang, 260f, 7f, m.dano, 600f));
            }
            if (m.consumirCura()) curarMonstrosPerto(m);
        }
        if (boss != null && boss.estaVivo()) boss.atualizarAtaques(delta, ctx);
        atualizarPerigos(delta);

        atacarAutomaticamente();
        atualizarPoderes(delta);
        removerMortos();

        if (tempoEfeitoAtaque > 0) tempoEfeitoAtaque -= delta;
        if (tempoAvisoAltar > 0) tempoAvisoAltar -= delta;
        if (tempoAvisoFase > 0) tempoAvisoFase -= delta;
        Efeito.atualizarTodos(efeitosCura, delta);
        atualizarDrops(delta);

        if (recompensaBossPendente && !escolhendoRecompensa()) abrirEscolhaBoss();
        if (bossDerrotado && !recompensaBossPendente && !escolhendoRecompensa()
            && escolhasPendentes == 0 && heroi.penalidadesPendentes == 0) {
            faseConcluida = true;
            jogoVencido = faseAtual >= TOTAL_FASES;
        }
    }

    private void curarMonstrosPerto(Monstro curandeiro) {
        for (int j = 0; j < monstros.size; j++) {
            Monstro outro = monstros.get(j);
            if (outro.posicao.dst(curandeiro.posicao) <= 160f) outro.curar(outro.vidaMaxima * 0.15f);
        }
        efeitosCura.add(new Efeito(curandeiro.posicao.x, curandeiro.posicao.y, 160f, 0.4f));
    }

    /** Tiros e meteoros (do boss e dos monstros). */
    private void atualizarPerigos(float delta) {
        for (int i = projeteisInimigos.size - 1; i >= 0; i--) {
            ProjetilInimigo p = projeteisInimigos.get(i);
            p.atualizar(delta);
            if (p.acertou(heroi)) {
                heroi.receberDano(p.dano);
                projeteisInimigos.removeIndex(i);
            } else if (!p.ativo()) {
                projeteisInimigos.removeIndex(i);
            }
        }
        for (int i = meteoros.size - 1; i >= 0; i--) {
            Meteoro m = meteoros.get(i);
            if (m.atualizar(delta) && m.atinge(heroi)) heroi.receberDano(m.dano);
            if (m.terminou()) meteoros.removeIndex(i);
        }
    }

    // ------------------------------------------------------------------ altares e boss

    /** 30 na fase 1, 40 na 2, 50 na 3... ate no maximo 80 (por altar). */
    private static int sacrificiosDaFase(int fase) {
        return Math.min(SACRIFICIOS_MAXIMO, SACRIFICIOS_FASE_1 + SACRIFICIOS_POR_FASE * (fase - 1));
    }

    /** Depois das hordas: com os altares cheios, invoca o boss; senao continuam vindo monstros. */
    private void atualizarAltaresEBoss(float delta) {
        if (!hordas.concluida() || boss != null || bossDerrotado) return;
        if (controle.completos()) {
            invocarBoss();
            return;
        }
        tempoMonstroExtra -= delta;
        if (tempoMonstroExtra <= 0 && monstros.size < MAX_MONSTROS_EXTRA) {
            monstros.add(hordas.criarMonstroExtra(heroi.posicao));
            tempoMonstroExtra = INTERVALO_MONSTRO_EXTRA;
        }
    }

    private void invocarBoss() {
        Altar a = controle.altares.first();
        boss = Boss.daFase(faseAtual, a.posicao.x, a.posicao.y);
        monstros.add(boss);
    }

    private void abrirEscolhaBoss() {
        recompensaBossPendente = false;
        opcoes.clear();
        modo = ModoEscolha.BOSS;
        opcoes.add(new RecompensaEpico(heroi));
        boolean bossDeTroca = (faseAtual == 1 || faseAtual == 3) && !heroi.jaTrocouDeClasse;
        if (bossDeTroca) opcoes.addAll(RecompensaTrocaClasse.opcoesPara(heroi));   // 1 epico + 2 trocas
        else opcoes.addAll(SorteioRecompensas.sortearRaras(heroi, 2));             // 1 epico + 2 raros
        montarCards(opcoes.size);
    }

    // ------------------------------------------------------------------ heroi e combate

    private void atualizarPoderes(float delta) {
        heroi.iniciarModificadores();
        if (heroi.epico != null) heroi.epico.atualizar(delta, heroi, monstros);
        for (int i = 0; i < heroi.poderes.size; i++) {
            heroi.poderes.get(i).atualizar(delta, ctx);
        }

        for (int i = projeteis.size - 1; i >= 0; i--) {
            Projetil p = projeteis.get(i);
            p.atualizar(delta);
            for (int j = 0; j < monstros.size && p.isAtivo(); j++) {
                Monstro m = monstros.get(j);
                if (m.estaVivo() && p.podeAcertar(m)) {
                    p.registrarAcerto(m);
                    p.origem.aoAcertar(p, m, heroi, monstros);
                }
            }
            if (!p.isAtivo()) {
                if (p.explodeNoFim) p.origem.aoExpirar(p, heroi, monstros);
                projeteis.removeIndex(i);
            }
        }
    }

    private void atacarAutomaticamente() {
        if (!heroi.podeAtacar()) return;

        // Escolhe os N monstros mais proximos dentro do alcance (N = 2 para o Anjo)
        atingidos.clear();
        for (int i = 0; i < heroi.alvosPorAtaque(); i++) {
            Monstro maisProximo = null;
            float menorDistancia = heroi.alcanceAtaque;
            for (int j = 0; j < monstros.size; j++) {
                Monstro m = monstros.get(j);
                if (atingidos.contains(m, true)) continue;
                float d = m.posicao.dst(heroi.posicao);
                if (d <= menorDistancia) {
                    menorDistancia = d;
                    maisProximo = m;
                }
            }
            if (maisProximo == null) break;
            atingidos.add(maisProximo);
        }
        if (atingidos.isEmpty()) return;

        float dano = heroi.getDanoAtual();
        Elemento e = heroi.elementoAtaqueBasico();
        linhasAtaque.clear();
        for (int i = 0; i < atingidos.size; i++) {
            Monstro m = atingidos.get(i);
            heroi.aoCausarDano(m.receberDano(dano, e));
            linhasAtaque.add(new Vector2(m.posicao));
        }
        heroi.registrarAtaque();
        tempoEfeitoAtaque = 0.1f;
        tempoDesdeAtaqueBasico = 0f;
        direcaoAtaqueBasico.set(linhasAtaque.first()).sub(heroi.posicao);
        for (int i = 0; i < heroi.poderes.size; i++) heroi.poderes.get(i).aoAtaqueBasico(ctx);
    }

    /** Centraliza a camera no heroi, sem mostrar nada fora do mundo. */
    private void atualizarCamera() {
        float meioL = LARGURA / 2f;
        float meioA = ALTURA / 2f;
        camera.position.set(
            MathUtils.clamp(heroi.posicao.x, meioL, MUNDO_LARGURA - meioL),
            MathUtils.clamp(heroi.posicao.y, meioA, MUNDO_ALTURA - meioA), 0);
        camera.update();
    }

    private boolean tocandoNoBotao() {
        toqueHud.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewportHud.unproject(toqueHud);
        return Vector2.dst(toqueHud.x, toqueHud.y, BOTAO_X, BOTAO_Y) <= BOTAO_RAIO;
    }

    private boolean tocandoNoBotaoEpico() {
        if (heroi.epico == null) return false;
        toqueHud.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewportHud.unproject(toqueHud);
        return Vector2.dst(toqueHud.x, toqueHud.y, BOTAO_EPICO_X, BOTAO_Y) <= BOTAO_EPICO_RAIO;
    }

    private void moverHeroi(float delta) {
        movimento.setZero();

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    movimento.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  movimento.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  movimento.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movimento.x += 1;

        if (Gdx.input.isTouched() && !tocandoNoBotao() && !tocandoNoBotaoEpico()) {
            toque.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(toque);
            movimento.set(toque.x - heroi.posicao.x, toque.y - heroi.posicao.y);
            if (movimento.len() < 5f) movimento.setZero();
        }

        if (!movimento.isZero()) {
            movimento.nor().scl(heroi.velocidadeAtual() * delta);
            heroi.posicao.add(movimento);
        }

        float meio = heroi.tamanho / 2f;
        heroi.posicao.x = MathUtils.clamp(heroi.posicao.x, meio, MUNDO_LARGURA - meio);
        heroi.posicao.y = MathUtils.clamp(heroi.posicao.y, meio, MUNDO_ALTURA - meio);
    }

    private void removerMortos() {
        for (int i = monstros.size - 1; i >= 0; i--) {
            if (monstros.get(i).estaVivo()) continue;
            Monstro morto = monstros.removeIndex(i);

            if (morto instanceof Boss) {
                bossDerrotado = true;
                recompensaBossPendente = true;
                projeteisInimigos.clear();
                meteoros.clear();
                // lacaios somem junto com o boss
                for (int j = monstros.size - 1; j >= 0; j--) {
                    if (monstros.get(j).tipo == TipoMonstro.LACAIO) monstros.removeIndex(j);
                }
                i = Math.min(i, monstros.size);
                continue;
            }
            abates++;

            for (int k = 0; k < heroi.poderes.size; k++) heroi.poderes.get(k).aoMonstroMorrer(morto, ctx);

            // Explosivo: explode ao morrer (aviso curto)
            if (morto.tipo == TipoMonstro.EXPLOSIVO) {
                meteoros.add(new Meteoro(morto.posicao.x, morto.posicao.y, 70f, morto.dano * 1.5f, 0.5f));
            }

            // Altares: sacrificios, chaves dos portadores e Guardiao do Selo (antes do boss aparecer)
            if (boss == null && !bossDerrotado && controle.aoMonstroMorrer(morto)) tempoAvisoAltar = 2.5f;

            if (hordas.getHordaAtual() != hordaDosDrops) {
                hordaDosDrops = hordas.getHordaAtual();
                dropsNestaHorda = 0;
            }
            if (dropsNestaHorda < MAX_DROPS_POR_HORDA && MathUtils.randomBoolean(morto.chanceDrop)) {
                drops.add(new ItemDrop(morto.posicao.x, morto.posicao.y));
                dropsNestaHorda++;
            }
        }
    }

    // ------------------------------------------------------- drops / recompensas

    private void atualizarDrops(float delta) {
        for (int i = drops.size - 1; i >= 0; i--) {
            ItemDrop d = drops.get(i);
            d.atualizar(delta);
            if (d.colideCom(heroi)) {
                drops.removeIndex(i);
                heroi.curar(CURA_DO_DROP);
            } else if (d.expirou()) {
                drops.removeIndex(i);
            }
        }
        if (escolhasPendentes > 0 && !escolhendoRecompensa()) abrirEscolhaDeHorda();
    }

    private boolean escolhendoRecompensa() {
        return opcoes.size > 0;
    }

    /** Escolha de fim de horda: primeiro poder, elemento (Humano) ou recompensa normal. */
    private void abrirEscolhaDeHorda() {
        escolhasPendentes--;
        opcoes.clear();
        if (heroi.poderes.size == 0) {
            modo = ModoEscolha.PRIMEIRO_PODER;
            Array<TipoPoder> lista = TipoPoder.daClasse(heroi.classe);
            lista.shuffle();
            for (int i = 0; i < Math.min(3, lista.size); i++) opcoes.add(new RecompensaPoder(lista.get(i), true, 1));
        } else if (heroi.classe == ClasseHeroi.HUMANO && heroi.elemento == null
                   && (faseAtual > 1 || hordas.getHordaAtual() >= HORDA_ESCOLHA_ELEMENTO)) {
            abrirEscolhaElemento(false);
            return;
        } else {
            modo = ModoEscolha.RECOMPENSA;
            opcoes.addAll(SorteioRecompensas.sortear(heroi, 3));
        }
        montarCards(opcoes.size);
    }

    private void abrirEscolhaElemento(boolean troca) {
        opcoes.clear();
        modo = troca ? ModoEscolha.TROCA_ELEMENTO : ModoEscolha.ELEMENTO;
        for (ElementoArma e : ElementoArma.values()) {
            if (e != heroi.elemento) opcoes.add(new RecompensaElemento(e));
        }
        montarCards(opcoes.size);
    }

    /** Monta as cartas da penalidade. Retorna false se o heroi nao tem nada para perder. */
    private boolean abrirPerderBuff() {
        opcoes.clear();
        modo = ModoEscolha.PERDER_BUFF;
        Array<TipoBuff> vistos = new Array<>();
        for (int i = 0; i < heroi.buffs.size; i++) {
            TipoBuff b = heroi.buffs.get(i);
            if (vistos.contains(b, true)) continue;
            vistos.add(b);
            int quantidade = 0;
            for (int k = 0; k < heroi.buffs.size; k++) if (heroi.buffs.get(k) == b) quantidade++;
            opcoes.add(RecompensaPerderBuff.doBuff(b, quantidade));
        }
        if (heroi.elemento != null) {
            for (int espaco = 0; espaco < 2; espaco++) {
                if (heroi.niveisElemento[espaco] > 0) opcoes.add(RecompensaPerderBuff.daMelhoria(heroi, espaco));
            }
        }
        if (opcoes.size > 12) opcoes.truncate(12);
        if (opcoes.isEmpty()) return false;
        montarCards(opcoes.size);
        return true;
    }

    /** Troca de poder, passo 1: qual sai. */
    private void abrirRemoverPoder() {
        opcoes.clear();
        modo = ModoEscolha.REMOVER_PODER;
        for (int i = 0; i < heroi.poderes.size; i++) opcoes.add(new RecompensaRemoverPoder(heroi.poderes.get(i)));
        montarCards(opcoes.size);
    }

    /** Troca de poder, passo 2: qual entra (3 sorteados, Nv1). */
    private void abrirNovoPoder() {
        opcoes.clear();
        modo = ModoEscolha.NOVO_PODER;
        Array<TipoPoder> livres = SorteioRecompensas.poderesLivres(heroi);
        livres.removeValue(heroi.ultimoRemovido, true);
        livres.shuffle();
        for (int i = 0; i < Math.min(3, livres.size); i++) opcoes.add(new RecompensaPoder(livres.get(i), true, 1));
        if (opcoes.isEmpty()) return;
        montarCards(opcoes.size);
    }

    /** Depois de cada escolha: resolve trocas, penalidades e escolhas de horda pendentes. */
    private void proximaEscolha() {
        opcoes.clear();
        if (heroi.pedidoTrocaPoder) {
            heroi.pedidoTrocaPoder = false;
            abrirRemoverPoder();
            return;
        }
        if (heroi.pedidoNovoPoder) {
            heroi.pedidoNovoPoder = false;
            abrirNovoPoder();
            if (escolhendoRecompensa()) return;
        }
        if (heroi.pedidoTrocaElemento) {
            heroi.pedidoTrocaElemento = false;
            abrirEscolhaElemento(true);
            return;
        }
        if (heroi.penalidadesPendentes > 0) {
            if (abrirPerderBuff()) return;
            heroi.penalidadesPendentes = 0;
        }
        if (escolhasPendentes > 0) abrirEscolhaDeHorda();
    }

    private void montarCards(int quantidade) {
        cards.clear();
        if (quantidade <= 3) {
            float espaco = (LARGURA - quantidade * CARD_LARGURA) / (quantidade + 1);
            for (int i = 0; i < quantidade; i++) {
                cards.add(new Rectangle(espaco + i * (CARD_LARGURA + espaco), 120, CARD_LARGURA, CARD_ALTURA));
            }
        } else {
            int colunas = 4;
            float espaco = (LARGURA - colunas * CARD_PEQUENO_LARGURA) / (colunas + 1);
            for (int i = 0; i < quantidade; i++) {
                int col = i % colunas;
                int linha = i / colunas;
                cards.add(new Rectangle(espaco + col * (CARD_PEQUENO_LARGURA + espaco),
                    300 - linha * (CARD_PEQUENO_ALTURA + 15), CARD_PEQUENO_LARGURA, CARD_PEQUENO_ALTURA));
            }
        }
    }

    private void processarEscolha() {
        int escolha = -1;
        for (int k = 0; k < 9; k++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + k)) escolha = k;
        }
        if (Gdx.input.justTouched()) {
            toqueHud.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewportHud.unproject(toqueHud);
            for (int i = 0; i < cards.size; i++) {
                if (cards.get(i).contains(toqueHud.x, toqueHud.y)) escolha = i;
            }
        }
        if (escolha >= 0 && escolha < opcoes.size) {
            opcoes.get(escolha).aplicar(heroi);
            proximaEscolha();
        }
    }

    // ---------------------------------------------------------------- desenho

    private void desenhar() {
        ScreenUtils.clear(0.10f, 0.10f, 0.12f, 1f);

        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        desenharFundoEAltares();
        desenharLinhasMundo();
        desenharFormasMundo();

        viewportHud.apply();
        shapes.setProjectionMatrix(cameraHud.combined);
        batch.setProjectionMatrix(cameraHud.combined);
        desenharHud();
        desenharTextos();
        if (escolhendoRecompensa()) desenharEscolha();
    }

    private void desenharLinhasMundo() {
        shapes.begin(ShapeRenderer.ShapeType.Line);

        if (texChao == null) {
            shapes.setColor(0.16f, 0.16f, 0.19f, 1f);
            for (float x = 0; x <= MUNDO_LARGURA; x += TAMANHO_GRADE) shapes.line(x, 0, x, MUNDO_ALTURA);
            for (float y = 0; y <= MUNDO_ALTURA; y += TAMANHO_GRADE) shapes.line(0, y, MUNDO_LARGURA, y);
        }
        shapes.setColor(Color.GRAY);
        shapes.rect(0, 0, MUNDO_LARGURA, MUNDO_ALTURA);

        // Altares (escondidos nao aparecem; selados tem correntes cinzas)
        for (int i = 0; i < controle.altares.size; i++) {
            Altar altar = controle.altares.get(i);
            if (altar.escondido) continue;
            if (texAltar == null) {
                shapes.setColor(altar.selado ? Color.GRAY : Color.MAGENTA);
                shapes.circle(altar.posicao.x, altar.posicao.y, altar.raio);
            }
            if (altar.selado) {
                shapes.setColor(Color.GRAY);
                float r = altar.raio * 0.7f;
                shapes.line(altar.posicao.x - r, altar.posicao.y - r, altar.posicao.x + r, altar.posicao.y + r);
                shapes.line(altar.posicao.x - r, altar.posicao.y + r, altar.posicao.x + r, altar.posicao.y - r);
            } else if (altar.progresso() > 0) {
                shapes.setColor(Color.GOLD);
                shapes.arc(altar.posicao.x, altar.posicao.y, altar.raio + 8, 90, 360 * altar.progresso());
            }
        }

        // Portal
        Portal portal = controle.portal;
        if (portal != null && portal.visivel()) {
            shapes.setColor(portal.trancado ? Color.ROYAL : Color.CYAN);
            float giro = portal.tempo() * 4f;
            for (int k = 0; k < 3; k++) {
                float r = portal.raio * (0.45f + 0.27f * k) + MathUtils.sin(giro + k) * 3f;
                shapes.circle(portal.posicao.x, portal.posicao.y, r);
            }
        }

        // Avisos de meteoros / explosoes
        for (int i = 0; i < meteoros.size; i++) {
            Meteoro m = meteoros.get(i);
            if (m.caiu()) continue;
            shapes.setColor(Color.RED);
            shapes.circle(m.posicao.x, m.posicao.y, m.raio);
            shapes.circle(m.posicao.x, m.posicao.y, m.raio * m.progressoAviso());
        }

        // Cura dos curandeiros
        shapes.setColor(Color.GREEN);
        for (int i = 0; i < efeitosCura.size; i++) {
            Efeito e = efeitosCura.get(i);
            shapes.circle(e.x, e.y, e.raio * e.progresso());
        }

        // Boss: aviso da investida, escudo, ponto fraco
        if (boss != null && boss.estaVivo()) {
            if (boss.preparandoInvestida()) {
                shapes.setColor(Color.RED);
                shapes.circle(boss.posicao.x, boss.posicao.y, boss.tamanho * 0.75f);
                shapes.circle(boss.posicao.x, boss.posicao.y, boss.tamanho * 0.85f);
            }
            if (boss instanceof BossRainha && ((BossRainha) boss).escudoAtivo()) {
                shapes.setColor(Color.CYAN);
                shapes.circle(boss.posicao.x, boss.posicao.y, boss.tamanho * 0.8f);
            }
            if (boss instanceof BossGolem && ((BossGolem) boss).pontoAberto()) {
                shapes.setColor(Color.YELLOW);
                shapes.circle(boss.posicao.x, boss.posicao.y, boss.tamanho * 0.75f);
                shapes.circle(boss.posicao.x, boss.posicao.y, boss.tamanho * 0.8f);
            }
        }

        // Escudos dos blindados
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (m.tipo == TipoMonstro.BLINDADO && m.escudoPontos > 0) {
                shapes.setColor(Color.SKY);
                for (int k = 0; k < m.escudoPontos; k++) shapes.circle(m.posicao.x, m.posicao.y, m.tamanho * 0.7f + k * 4f);
            }
        }

        // Ataque basico
        shapes.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapes.circle(heroi.posicao.x, heroi.posicao.y, heroi.alcanceAtaque);
        if (tempoEfeitoAtaque > 0) {
            shapes.setColor(Color.YELLOW);
            for (int i = 0; i < linhasAtaque.size; i++) shapes.line(heroi.posicao, linhasAtaque.get(i));
        }

        for (int i = 0; i < heroi.poderes.size; i++) heroi.poderes.get(i).desenharLinhas(shapes, heroi);
        if (heroi.epico != null) heroi.epico.desenharLinhas(shapes, heroi);

        float raio = heroi.getRaioHabilidade();
        if (heroi.getTempoEfeitoHabilidade() > 0 && raio > 0) {
            float progresso = 1f - heroi.getTempoEfeitoHabilidade() / heroi.getDuracaoEfeitoHabilidade();
            shapes.setColor(heroi.classe.cor);
            shapes.circle(heroi.posicao.x, heroi.posicao.y, raio * progresso);
        }
        if (heroi.emFuria()) {
            shapes.setColor(Color.ORANGE);
            shapes.circle(heroi.posicao.x, heroi.posicao.y, heroi.tamanho * 0.9f);
            shapes.circle(heroi.posicao.x, heroi.posicao.y, heroi.tamanho * 1.0f);
        }
        // Escudo de Energia e Guardiao
        if (heroi.escudo > 0) {
            shapes.setColor(Color.CYAN);
            shapes.circle(heroi.posicao.x, heroi.posicao.y, heroi.tamanho * 0.85f);
        }
        if (heroi.bloqueioPronto) {
            shapes.setColor(Color.GOLD);
            shapes.circle(heroi.posicao.x, heroi.posicao.y + heroi.tamanho, 6f);
        }

        shapes.end();
    }

    private void desenharFormasMundo() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < controle.altares.size && texAltar == null; i++) {
            Altar altar = controle.altares.get(i);
            if (altar.escondido) continue;
            if (altar.selado) shapes.setColor(0.18f, 0.18f, 0.20f, 1f);
            else if (altar.completo()) shapes.setColor(0.35f, 0.28f, 0.05f, 1f);
            else shapes.setColor(0.22f, 0.08f, 0.28f, 1f);
            shapes.circle(altar.posicao.x, altar.posicao.y, altar.raio);
        }

        // Chaves no chao (dourada = altar, azul = portal)
        for (int i = 0; i < controle.chaves.size; i++) {
            ItemDrop c = controle.chaves.get(i);
            if (!c.visivel()) continue;
            shapes.setColor(c.tipo == ItemDrop.Tipo.CHAVE_PORTAL ? Color.ROYAL : Color.GOLD);
            shapes.circle(c.posicao.x - 6, c.posicao.y, 7f);
            shapes.rect(c.posicao.x, c.posicao.y - 2, 14, 4);
            shapes.rect(c.posicao.x + 9, c.posicao.y - 7, 3, 5);
        }

        for (int i = 0; i < drops.size; i++) {
            ItemDrop d = drops.get(i);
            if (!d.visivel()) continue;
            shapes.setColor(Color.WHITE);
            shapes.circle(d.posicao.x, d.posicao.y, d.raio);
            shapes.setColor(Color.RED);
            shapes.circle(d.posicao.x, d.posicao.y, d.raio * 0.6f);
        }

        // Monstros (cor muda com o efeito; intangiveis ficam apagados)
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (texturaMonstro(m) != null) continue; // desenhado como sprite em desenharSprites()
            float meio = m.tamanho / 2f;
            if (m.estaPiscando())       corTemp.set(Color.WHITE);
            else if (m.estaAtordoado()) corTemp.set(Color.YELLOW);
            else if (m.estaLento())     corTemp.set(m.cor).lerp(Color.CYAN, 0.5f);
            else if (m.estaQueimando()) corTemp.set(m.cor).lerp(Color.ORANGE, 0.5f);
            else                        corTemp.set(m.cor);
            if (m instanceof Boss && ((Boss) m).emInvestida()) corTemp.lerp(Color.RED, 0.5f);
            if (m.intangivel()) corTemp.lerp(0.10f, 0.10f, 0.12f, 1f, 0.7f);
            shapes.setColor(corTemp);
            shapes.rect(m.posicao.x - meio, m.posicao.y - meio, m.tamanho, m.tamanho);

            if (!(m instanceof Boss)) {
                float porcentagem = Math.max(0, m.vida / m.vidaMaxima);
                shapes.setColor(Color.DARK_GRAY);
                shapes.rect(m.posicao.x - meio, m.posicao.y + meio + 3, m.tamanho, 4);
                shapes.setColor(Color.RED);
                shapes.rect(m.posicao.x - meio, m.posicao.y + meio + 3, m.tamanho * porcentagem, 4);
            }
        }

        // Meteoros caindo
        for (int i = 0; i < meteoros.size; i++) {
            Meteoro m = meteoros.get(i);
            if (!m.caiu()) continue;
            shapes.setColor(Color.ORANGE);
            shapes.circle(m.posicao.x, m.posicao.y, m.raio);
            shapes.setColor(Color.YELLOW);
            shapes.circle(m.posicao.x, m.posicao.y, m.raio * 0.5f);
        }

        // Tiros inimigos
        for (int i = 0; i < projeteisInimigos.size; i++) {
            ProjetilInimigo p = projeteisInimigos.get(i);
            shapes.setColor(Color.VIOLET);
            shapes.circle(p.posicao.x, p.posicao.y, p.raio);
            shapes.setColor(Color.WHITE);
            shapes.circle(p.posicao.x, p.posicao.y, p.raio * 0.4f);
        }

        // Projeteis do heroi
        for (int i = 0; i < projeteis.size; i++) {
            Projetil p = projeteis.get(i);
            shapes.setColor(p.cor);
            shapes.circle(p.posicao.x, p.posicao.y, p.raio);
        }

        if (texturaHeroi() == null) {
            float meioHeroi = heroi.tamanho / 2f;
            shapes.setColor(heroi.estaInvencivel() ? Color.WHITE : heroi.classe.cor);
            shapes.rect(heroi.posicao.x - meioHeroi, heroi.posicao.y - meioHeroi, heroi.tamanho, heroi.tamanho);
        }

        for (int i = 0; i < heroi.poderes.size; i++) heroi.poderes.get(i).desenharFormas(shapes, heroi);
        if (heroi.epico != null) heroi.epico.desenharFormas(shapes, heroi);

        shapes.end();

        desenharSprites();

        // Texto dos altares (no mundo)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonte.setColor(Color.WHITE);
        for (int i = 0; i < controle.altares.size; i++) {
            Altar altar = controle.altares.get(i);
            if (altar.escondido) continue;
            String textoAltar = altar.selado ? (altar.abreComGuardiao ? "SELADO: o Guardiao deve morrer aqui" : "SELADO: traga a chave")
                : altar.completo() ? "ALTAR COMPLETO" : "ALTAR " + altar.getSacrificios() + "/" + altar.getNecessario();
            fonte.draw(batch, textoAltar, altar.posicao.x - 110, altar.posicao.y + altar.raio + 24, 220, Align.center, false);
        }
        if (controle.portal != null) {
            fonte.draw(batch, controle.portal.trancado ? "PORTAL (trancado)" : "PORTAL",
                controle.portal.posicao.x - 70, controle.portal.posicao.y + controle.portal.raio + 18, 140, Align.center, false);
        }
        batch.end();
    }

    // ---------------------------------------------------------------- artes

    private static Texture carregarTextura(String caminho, boolean repetir) {
        if (!Gdx.files.internal(caminho).exists()) return null;
        Texture t = new Texture(Gdx.files.internal(caminho), true); // true = mipmaps (fica nitido pequeno)
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        if (repetir) t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return t;
    }

    /** Arte do heroi pela classe. Null = ainda sem arte (desenha o quadrado). */
    private Texture texturaHeroi() {
        switch (heroi.classe) {
            case HUMANO: return texHeroiHumanoSemArma != null ? texHeroiHumanoSemArma : texHeroiHumano;
            case ANJO:    return texHeroiAnjo;
            case DEMONIO: return texHeroiDemonio;
            default:      return null;
        }
    }

    /** Arte do monstro. Null = ainda sem arte (desenha o quadrado). */
    private Texture texturaMonstro(Monstro m) {
        if (m instanceof Boss) return (m instanceof BossGuardiao) ? texBossGuardiao : null;
        switch (m.tipo) {
            case COMUM: case CORREDOR: case TANQUE: case LACAIO: return texMonstroComum;
            default: return null;
        }
    }

    /** Chao repetido e altares (ficam embaixo de tudo). */
    private void desenharFundoEAltares() {
        if (texChao == null && texAltar == null) return;
        batch.begin();
        batch.setColor(Color.WHITE);
        if (texChao != null) {
            batch.draw(texChao, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA,
                0, 0, MUNDO_LARGURA / TAMANHO_TILE_CHAO, MUNDO_ALTURA / TAMANHO_TILE_CHAO);
        }
        if (texAltar != null) {
            for (int i = 0; i < controle.altares.size; i++) {
                Altar altar = controle.altares.get(i);
                if (altar.escondido) continue; // so aparece depois do portal
                float lado = altar.raio * 2.15f;
                if (altar.selado) batch.setColor(0.45f, 0.45f, 0.5f, 1f); // selado: apagado
                else if (altar.completo()) batch.setColor(1f, 0.85f, 0.45f, 1f); // dourado
                else batch.setColor(Color.WHITE);
                batch.draw(texAltar, altar.posicao.x - lado / 2f, altar.posicao.y - lado / 2f, lado, lado);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    /** Monstros, boss e heroi com arte, por cima das formas. Barras de vida por ultimo. */
    private void desenharSprites() {
        float delta = escolhendoRecompensa() ? 0f : Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        tempoAnim += delta;
        if (delta > 0f) {
            float movido = Vector2.dst(heroi.posicao.x, heroi.posicao.y, heroiXAnterior, heroiYAnterior);
            float alvo = (movido / delta > 20f) ? 1f : 0f;
            heroiAndando += (alvo - heroiAndando) * Math.min(1f, delta * 10f);
        }
        if (heroi.posicao.x < heroiXAnterior - 0.01f) heroiOlhandoEsquerda = true;
        else if (heroi.posicao.x > heroiXAnterior + 0.01f) heroiOlhandoEsquerda = false;
        heroiXAnterior = heroi.posicao.x;
        heroiYAnterior = heroi.posicao.y;
        if (!artesExtrasCarregadas) carregarArtesExtras();
        atualizarMiraArma(delta);  // atirando: o heroi vira para o alvo
        atualizarPoeira(delta);

        // Sombras (ajudam a ver os personagens no chao escuro)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.4f);
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (texturaMonstro(m) == null || m.intangivel()) continue;
            float w = m.tamanho * 1.1f, h = m.tamanho * 0.38f;
            shapes.ellipse(m.posicao.x - w / 2f, m.posicao.y - m.tamanho / 2f - h / 2f, w, h);
        }
        if (texturaHeroi() != null) {
            float w = heroi.tamanho * 1.1f, h = heroi.tamanho * 0.38f;
            shapes.ellipse(heroi.posicao.x - w / 2f, heroi.posicao.y - heroi.tamanho / 2f - h / 2f, w, h);
        }
        for (int i = 0; i < poeira.size; i++) {
            float[] p = poeira.get(i);
            float t = p[2] / DURACAO_POEIRA;
            shapes.setColor(0.62f, 0.56f, 0.48f, 0.45f * (1f - t));
            shapes.circle(p[0] + p[3] * t * 10f, p[1] + t * 6f, 2.5f + t * 7f);
        }
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            Texture t = texturaMonstro(m);
            if (t == null) continue;
            corTemp.set(Color.WHITE);
            if (m.tipo == TipoMonstro.CORREDOR || m.tipo == TipoMonstro.TANQUE) corTemp.lerp(m.cor, 0.35f);
            if (m.estaAtordoado())      corTemp.lerp(Color.YELLOW, 0.5f);
            else if (m.estaLento())     corTemp.lerp(Color.CYAN, 0.5f);
            else if (m.estaQueimando()) corTemp.lerp(Color.ORANGE, 0.5f);
            if (m instanceof Boss && ((Boss) m).emInvestida()) corTemp.lerp(Color.RED, 0.5f);
            if (m.estaPiscando()) corTemp.a = 0.45f;
            if (m.intangivel())   corTemp.a = 0.3f;
            batch.setColor(corTemp);
            float lado = m.tamanho * ((m instanceof Boss) ? 1.7f : 1.8f);
            boolean olhaEsquerda = m.posicao.x > heroi.posicao.x; // sempre de frente para o heroi
            float freq = 9f, forca = 1f;                       // ritmo do passo de cada tipo
            if (m instanceof Boss)                       { freq = 5f;  forca = 1.3f; } // passos pesados
            else if (m.tipo == TipoMonstro.CORREDOR)     { freq = 14f; forca = 0.9f; }
            else if (m.tipo == TipoMonstro.TANQUE)       { freq = 6f;  forca = 1.2f; }
            float andando = m.estaAtordoado() ? 0f : (m.estaLento() ? 0.5f : 1f);
            float fase = (System.identityHashCode(m) % 628) / 100f; // cada monstro num tempo diferente
            desenharSpriteAnimado(t, m.posicao.x, m.posicao.y - m.tamanho / 2f, lado, !olhaEsquerda, fase, andando, freq, forca);
        }
        Texture th = texturaHeroi();
        if (th != null) {
            batch.setColor(1f, 1f, 1f, heroi.estaInvencivel() ? 0.55f : 1f);
            desenharSpriteAnimado(th, heroi.posicao.x, heroi.posicao.y - heroi.tamanho / 2f, heroi.tamanho * 1.9f,
                !heroiOlhandoEsquerda, 0f, heroiAndando, 10f, 1f);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        desenharArmaHumano();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (m instanceof Boss || texturaMonstro(m) == null) continue;
            float lado = m.tamanho * 1.8f;
            float x = m.posicao.x - m.tamanho / 2f;
            float y = m.posicao.y - m.tamanho / 2f + lado;
            float porcentagem = Math.max(0, m.vida / m.vidaMaxima);
            shapes.setColor(Color.DARK_GRAY);
            shapes.rect(x, y, m.tamanho, 4);
            shapes.setColor(Color.RED);
            shapes.rect(x, y, m.tamanho * porcentagem, 4);
        }
        shapes.end();
    }

    // ------------------------------------------------------------------ andar (pernas, spritesheet, poeira)

    private static final float DURACAO_POEIRA = 0.45f;

    /**
     * Carrega o que e opcional: spritesheets de andar ("<arte>_andando.png", quadros quadrados lado a lado)
     * e as armas do Humano ("arma_pistola.png" etc., cano para a direita, de preferencia em cinza/branco).
     * Tambem diz onde cortar cada arte para mexer as pernas.
     */
    private void carregarArtesExtras() {
        artesExtrasCarregadas = true;
        // {altura das pernas (fracao de baixo), onde separar perna da frente e de tras (fracao da largura)}
        if (texHeroiHumano != null)        pernas.put(texHeroiHumano, new float[] { 0.36f, 0.53f });
        if (texHeroiHumanoSemArma != null) pernas.put(texHeroiHumanoSemArma, new float[] { 0.36f, 0.53f });
        if (texHeroiAnjo != null)          pernas.put(texHeroiAnjo, new float[] { 0.36f, 0.50f });
        if (texHeroiDemonio != null)       pernas.put(texHeroiDemonio, new float[] { 0.36f, 0.52f });
        if (texMonstroComum != null)       pernas.put(texMonstroComum, new float[] { 0.30f, 0.55f });

        registrarAndando(texHeroiHumano, "sprites/heroi_humano_andando.png");
        registrarAndando(texHeroiHumanoSemArma, "sprites/heroi_humano_andando.png");
        registrarAndando(texHeroiAnjo, "sprites/heroi_anjo_andando.png");
        registrarAndando(texHeroiDemonio, "sprites/heroi_demonio_andando.png");
        registrarAndando(texMonstroComum, "sprites/monstro_comum_andando.png");

        registrarArma(TipoPoder.PISTOLA, "sprites/arma_pistola.png");
        registrarArma(TipoPoder.DUPLA, "sprites/arma_pistola.png");
        registrarArma(TipoPoder.ESCOPETA, "sprites/arma_escopeta.png");
        registrarArma(TipoPoder.FUZIL, "sprites/arma_fuzil.png");
        registrarArma(TipoPoder.METRALHADORA, "sprites/arma_metralhadora.png");
    }

    private final ObjectMap<String, Texture> cacheExtras = new ObjectMap<>();

    private Texture carregarExtra(String caminho) {
        if (cacheExtras.containsKey(caminho)) return cacheExtras.get(caminho); // mesma imagem usada 2x (pistola/dupla)
        Texture t = carregarTextura(caminho, false);
        cacheExtras.put(caminho, t);
        if (t != null) texturasExtras.add(t);
        return t;
    }

    private void registrarAndando(Texture base, String caminho) {
        if (base == null) return;
        Texture folha = carregarExtra(caminho);
        if (folha == null) return;
        int n = Math.max(1, Math.round(folha.getWidth() / (float) folha.getHeight()));
        framesAndando.put(base, TextureRegion.split(folha, folha.getWidth() / n, folha.getHeight())[0]);
    }

    private void registrarArma(TipoPoder tipo, String caminho) {
        Texture t = carregarExtra(caminho);
        if (t != null) texArmas.put(tipo, t);
    }

    private void atualizarPoeira(float delta) {
        for (int i = poeira.size - 1; i >= 0; i--) {
            float[] p = poeira.get(i);
            p[2] += delta;
            if (p[2] >= DURACAO_POEIRA) poeira.removeIndex(i);
        }
        // Um "puff" de poeira cada vez que um pe do heroi toca o chao
        float passo = MathUtils.sin(tempoAnim * 10f);
        if (heroiAndando > 0.5f && delta > 0f && Math.signum(passo) != Math.signum(passoHeroiAnterior) && poeira.size < 20) {
            float tras = heroiOlhandoEsquerda ? 1f : -1f;
            poeira.add(new float[] { heroi.posicao.x + tras * heroi.tamanho * 0.15f + MathUtils.random(-3f, 3f),
                heroi.posicao.y - heroi.tamanho / 2f, 0f, tras });
        }
        passoHeroiAnterior = passo;
    }

    /**
     * Desenha a arte com os pes em (x, pesY), animada sem precisar de varios frames:
     *  - se existir spritesheet de andar, usa os quadros;
     *  - se a arte tiver pernas configuradas, corta em corpo + 2 pernas e as pernas dao passos de verdade
     *    (uma sobe e vai para frente enquanto a outra fica no chao), o corpo balanca e inclina para frente;
     *  - senao, so sobe/desce e inclina (bosses).
     * Parado: respira devagar. "andando" vai de 0 (parado) a 1 (andando). A arte original olha para a esquerda.
     */
    private void desenharSpriteAnimado(Texture t, float x, float pesY, float lado, boolean espelhar,
                                       float fase, float andando, float freq, float forca) {
        float ciclo = tempoAnim * freq + fase;
        float s = MathUtils.sin(ciclo);
        float frente = espelhar ? 1f : -1f;
        float respira = MathUtils.sin(tempoAnim * 2.2f + fase) * 0.025f * (1f - andando);
        float inclinaFrente = -frente * 5f * forca * andando;     // corpo inclinado na direcao do movimento

        TextureRegion[] frames = framesAndando.get(t);
        if (frames != null && andando > 0.3f) {
            int q = ((int) (ciclo / MathUtils.PI2 * frames.length) % frames.length + frames.length) % frames.length;
            TextureRegion r = frames[q];
            float sobe = Math.abs(s) * lado * 0.015f * andando;
            batch.draw(r.getTexture(), x - lado / 2f, pesY + sobe, lado / 2f, 0f, lado, lado, 1f, 1f, inclinaFrente * 0.5f,
                r.getRegionX(), r.getRegionY(), r.getRegionWidth(), r.getRegionHeight(), espelhar, false);
            return;
        }

        float[] cfg = pernas.get(t);
        if (cfg == null) {
            float passo = Math.abs(s);                                      // 0 no chao, 1 no alto do passo
            float pulo = passo * lado * 0.06f * forca * andando;
            float inclinacao = s * 6f * forca * andando + inclinaFrente * 0.5f;
            float achata = (1f - passo) * 0.07f * forca * andando;          // amassa ao pisar
            float escalaX = 1f + achata * 0.6f - respira * 0.4f;
            float escalaY = 1f - achata + respira;
            batch.draw(t, x - lado / 2f, pesY + pulo, lado / 2f, 0f, lado, lado, escalaX, escalaY, inclinacao,
                0, 0, t.getWidth(), t.getHeight(), espelhar, false);
            return;
        }

        int tw = t.getWidth(), th = t.getHeight();
        float kx = lado / tw, ky = lado / th;
        int corte = Math.round(th * (1f - cfg[0]));        // linha (de cima) onde comecam as pernas
        int sobra = Math.round(th * 0.05f);                // perna entra um pouco embaixo do corpo (sem fresta)
        int divisao = Math.round(tw * cfg[1]);
        int topoPerna = corte - sobra;
        int alturaPerna = th - topoPerna;

        float rot = s * 2.5f * forca * andando + inclinaFrente;
        float corpoSobe = Math.abs(s) * lado * 0.018f * forca * andando + respira * lado * 0.5f;
        float levanta = lado * 0.045f * forca * andando;
        float avanca = lado * 0.022f * andando;
        // perna "A" (lado esquerdo da arte = perna da frente) sobe quando s > 0; a "B" quando s < 0
        float aY = Math.max(0f, s) * levanta, bY = Math.max(0f, -s) * levanta;
        float aX = frente * s * avanca, bX = -aX;

        float base = x - lado / 2f;
        parte(t, base, x, pesY, lado, espelhar, rot, 0, topoPerna, divisao, alturaPerna,
            0f, 0f, divisao * kx, alturaPerna * ky, aX, aY);
        parte(t, base, x, pesY, lado, espelhar, rot, divisao, topoPerna, tw - divisao, alturaPerna,
            divisao * kx, 0f, (tw - divisao) * kx, alturaPerna * ky, bX, bY);
        parte(t, base, x, pesY, lado, espelhar, rot, 0, 0, tw, corte,
            0f, (th - corte) * ky, lado, corte * ky, 0f, corpoSobe);
    }

    /** Um pedaco da arte, girando em volta dos pes (x, pesY) junto com o resto. */
    private void parte(Texture t, float base, float x, float pesY, float lado, boolean espelhar, float rot,
                       int srcX, int srcY, int srcW, int srcH,
                       float lx, float ly, float w, float h, float dx, float dy) {
        float lx2 = espelhar ? lado - lx - w : lx;
        float px = base + lx2 + dx, py = pesY + ly + dy;
        batch.draw(t, px, py, x - px, pesY - py, w, h, 1f, 1f, rot, srcX, srcY, srcW, srcH, espelhar, false);
    }

    // ------------------------------------------------------------------ arma na mao do Humano

    /** Armas de mao: aparecem na mao do Humano. As outras (granada, torreta...) nao. */
    private static boolean armaDeMao(TipoPoder t) {
        return t == TipoPoder.PISTOLA || t == TipoPoder.DUPLA || t == TipoPoder.ESCOPETA
            || t == TipoPoder.FUZIL || t == TipoPoder.METRALHADORA;
    }

    /**
     * Escolhe a arma da mao (a de maior nivel; empate = a ultima pega; nenhuma = pistola do ataque basico),
     * mira no ultimo tiro e vira o heroi para o alvo enquanto atira.
     */
    private void atualizarMiraArma(float delta) {
        tempoDesdeAtaqueBasico += delta;
        if (heroi.classe != ClasseHeroi.HUMANO) return;

        PoderProjetil arma = null;
        for (int i = 0; i < heroi.poderes.size; i++) {
            Poder p = heroi.poderes.get(i);
            if (!(p instanceof PoderProjetil) || !armaDeMao(p.tipo)) continue;
            if (arma == null || p.getNivel() >= arma.getNivel()) arma = (PoderProjetil) p;
        }
        armaNaMao = arma != null ? arma.tipo : TipoPoder.PISTOLA;

        float alvo;
        float desde = 99f;
        if (arma != null && arma.tempoDesdeTiro < tempoDesdeAtaqueBasico) {
            desde = arma.tempoDesdeTiro;
            alvo = arma.direcaoTiro.angleDeg();
        } else {
            desde = tempoDesdeAtaqueBasico;
            alvo = direcaoAtaqueBasico.angleDeg();
        }
        armaDesdeTiro = desde;
        if (desde < 0.6f) {
            heroiOlhandoEsquerda = MathUtils.cosDeg(alvo) < 0f;
        } else {
            alvo = heroiOlhandoEsquerda ? 205f : -25f;  // descansando: apontada para frente e para baixo
        }
        if (delta > 0f) anguloArma = MathUtils.lerpAngleDeg(anguloArma, alvo, Math.min(1f, delta * 20f));
    }

    /** Medidas de cada arma (em "pixels" de um heroi de 60): comprimento total e onde fica o cano. */
    private static float comprimentoArma(TipoPoder t) {
        switch (t) {
            case ESCOPETA:     return 29f;
            case FUZIL:        return 35f;
            case METRALHADORA: return 27f;
            default:           return 12f;
        }
    }

    private void desenharArmaHumano() {
        if (heroi.classe != ClasseHeroi.HUMANO || texturaHeroi() == null) return;
        float lado = heroi.tamanho * 1.9f;
        float k = lado / 60f;
        float pesY = heroi.posicao.y - heroi.tamanho / 2f;
        float lado1 = heroiOlhandoEsquerda ? -1f : 1f;
        // Mao da frente (na arte: x 32%, y 54% de baixo). Dupla: a outra mao fica no peito.
        // Acompanha o balanco do corpo (mesma conta do desenharSpriteAnimado para o heroi)
        float s = MathUtils.sin(tempoAnim * 10f);
        float rot = s * 2.5f * heroiAndando - lado1 * 5f * heroiAndando;
        float sobe = Math.abs(s) * lado * 0.018f * heroiAndando;
        float cos = MathUtils.cosDeg(rot), sen = MathUtils.sinDeg(rot);
        float dx = lado1 * 0.18f * lado, dy = 0.54f * lado + sobe;
        float maoX = heroi.posicao.x + dx * cos - dy * sen;
        float maoY = pesY + dx * sen + dy * cos;
        float recuo = Math.max(0f, 1f - armaDesdeTiro / 0.1f) * 3f * k;

        ElementoArma el = heroi.elemento;
        Color cor = el != null ? el.cor : Color.LIGHT_GRAY;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Texture tex = texArmas.get(armaNaMao);
        if (armaNaMao == TipoPoder.DUPLA) {
            float ex = -lado1 * 0.086f * lado, ey = 0.63f * lado + sobe;
            float m2x = heroi.posicao.x + ex * cos - ey * sen;
            float m2y = pesY + ex * sen + ey * cos;
            desenharUmaArma(TipoPoder.PISTOLA, tex, m2x, m2y, anguloArma + 8f, k, recuo, cor, 0.7f);
        }
        desenharUmaArma(armaNaMao == TipoPoder.DUPLA ? TipoPoder.PISTOLA : armaNaMao, tex, maoX, maoY, anguloArma, k, recuo, cor, 1f);
    }

    /** Uma arma girando em volta da mao (hx, hy). "luz" escurece a arma de tras da Dupla. */
    private void desenharUmaArma(TipoPoder tipo, Texture tex, float hx, float hy, float ang, float k, float recuo,
                                 Color cor, float luz) {
        float flip = MathUtils.cosDeg(ang) < 0f ? -1f : 1f;   // nunca de cabeca para baixo
        float comp = comprimentoArma(tipo) * k;

        if (tex != null) {
            // Arte da arma: cano para a direita, empunhadura a 25% da esquerda e 40% de baixo
            float h = comp * tex.getHeight() / (float) tex.getWidth();
            float ox = comp * 0.25f + recuo, oy = h * 0.4f;
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            corTemp.set(Color.WHITE).lerp(cor, 0.6f).mul(luz, luz, luz, 1f);
            batch.setColor(corTemp);
            batch.draw(tex, hx - ox, hy - oy, ox, oy, comp, h, 1f, flip, ang,
                0, 0, tex.getWidth(), tex.getHeight(), false, false);
            batch.setColor(Color.WHITE);
            batch.end();
        } else {
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            Color metal = corTemp.set(0.2f, 0.2f, 0.23f, 1f).lerp(cor, 0.35f).mul(luz, luz, luz, 1f);
            float mr = metal.r, mg = metal.g, mb = metal.b;
            float ar = cor.r * luz, ag = cor.g * luz, ab = cor.b * luz;
            float wr = 0.42f * luz, wg = 0.26f * luz, wb = 0.12f * luz;   // madeira
            float r = -recuo / k;
            switch (tipo) {
                case ESCOPETA:
                    peca(hx, hy, ang, flip, k, -9 + r, -2.2f, 7, 3.4f, wr, wg, wb);
                    peca(hx, hy, ang, flip, k, -2 + r, -1.6f, 11, 4, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 8 + r, 0.2f, 12, 2.2f, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 9 + r, -2.2f, 6, 2.2f, wr, wg, wb);
                    peca(hx, hy, ang, flip, k, 14 + r, 1.2f, 6, 0.9f, ar, ag, ab);
                    break;
                case FUZIL:
                    peca(hx, hy, ang, flip, k, -9 + r, -2.2f, 7, 3.4f, wr, wg, wb);
                    peca(hx, hy, ang, flip, k, -2 + r, -1.6f, 13, 3.6f, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 11 + r, 0.2f, 15, 1.4f, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 1 + r, 2.2f, 8, 2f, ar, ag, ab);       // luneta
                    break;
                case METRALHADORA:
                    peca(hx, hy, ang, flip, k, -7 + r, -2.2f, 5, 3.4f, mr * 0.8f, mg * 0.8f, mb * 0.8f);
                    peca(hx, hy, ang, flip, k, -2 + r, -2.2f, 14, 5, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 12 + r, 0.2f, 8, 2, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 3 + r, -7.5f, 3.5f, 5.5f, ar, ag, ab); // pente
                    peca(hx, hy, ang, flip, k, 0 + r, 1.6f, 10, 1f, ar, ag, ab);
                    break;
                default: // pistola
                    peca(hx, hy, ang, flip, k, -1.5f + r, -5f, 3.5f, 4.5f, mr * 0.8f, mg * 0.8f, mb * 0.8f);
                    peca(hx, hy, ang, flip, k, -2 + r, -1.4f, 12, 4, mr, mg, mb);
                    peca(hx, hy, ang, flip, k, 3 + r, 0.8f, 8, 1.2f, ar, ag, ab);
                    break;
            }
            shapes.end();
        }

        // Clarao no cano
        if (armaDesdeTiro < 0.07f && luz >= 1f) {
            float ponta = comp * 0.75f - recuo;   // cano a 75% do comprimento a partir da mao
            float cx = hx + MathUtils.cosDeg(ang) * ponta, cy = hy + MathUtils.sinDeg(ang) * ponta;
            float tam = (tipo == TipoPoder.PISTOLA ? 3.5f : 5f) * k;
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(cor.r, cor.g, cor.b, 0.85f);
            shapes.triangle(cx, cy,
                cx + MathUtils.cosDeg(ang + 25f) * tam * 2.2f, cy + MathUtils.sinDeg(ang + 25f) * tam * 2.2f,
                cx + MathUtils.cosDeg(ang - 25f) * tam * 2.2f, cy + MathUtils.sinDeg(ang - 25f) * tam * 2.2f);
            shapes.setColor(1f, 1f, 0.85f, 0.95f);
            shapes.circle(cx, cy, tam);
            shapes.end();
        }
    }

    /** Retangulo da arma em coordenadas da arma (x = cano, y = cima), girado em volta da mao. */
    private void peca(float hx, float hy, float ang, float flip, float k,
                      float lx, float ly, float w, float h, float r, float g, float b) {
        shapes.setColor(r, g, b, 1f);
        float x = lx * k, y = ly * k;
        shapes.rect(hx + x, hy + y, -x, -y, w * k, h * k, 1f, flip, ang);
    }

    /** Seta na borda da tela apontando para algo fora da visao (altar, boss). */
    private void desenharSeta(Vector2 alvo, Color cor) {
        float dx = alvo.x - camera.position.x;
        float dy = alvo.y - camera.position.y;
        float meioL = LARGURA / 2f - 30f;
        float meioA = ALTURA / 2f - 30f;
        if (Math.abs(dx) <= meioL + 30f && Math.abs(dy) <= meioA + 30f) return;
        float escala = Math.min(meioL / Math.max(Math.abs(dx), 0.001f), meioA / Math.max(Math.abs(dy), 0.001f));
        float x = LARGURA / 2f + dx * escala;
        float y = ALTURA / 2f + dy * escala;
        float ang = MathUtils.atan2(dy, dx);
        shapes.setColor(cor);
        shapes.circle(x, y, 9f);
        shapes.triangle(
            x + MathUtils.cos(ang) * 20f, y + MathUtils.sin(ang) * 20f,
            x + MathUtils.cos(ang + 2.4f) * 10f, y + MathUtils.sin(ang + 2.4f) * 10f,
            x + MathUtils.cos(ang - 2.4f) * 10f, y + MathUtils.sin(ang - 2.4f) * 10f);
    }

    private void desenharHud() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(10, ALTURA - 24, 200, 14);
        shapes.setColor(Color.GREEN);
        shapes.rect(10, ALTURA - 24, 200 * (heroi.vida / heroi.vidaMaxima), 14);
        if (heroi.escudo > 0) {
            shapes.setColor(Color.CYAN);
            shapes.rect(10, ALTURA - 28, Math.min(200f, 200 * (heroi.escudo / heroi.vidaMaxima)), 3);
        }

        shapes.setColor(heroi.habilidadePronta() ? heroi.classe.cor : Color.DARK_GRAY);
        shapes.circle(BOTAO_X, BOTAO_Y, BOTAO_RAIO);

        if (heroi.epico != null) {
            if (heroi.epico.ativo())       shapes.setColor(Color.WHITE);
            else if (heroi.epico.pronto()) shapes.setColor(heroi.epico.tipo.cor);
            else                           shapes.setColor(Color.DARK_GRAY);
            shapes.circle(BOTAO_EPICO_X, BOTAO_Y, BOTAO_EPICO_RAIO);
        }

        if (boss != null && boss.estaVivo()) {
            shapes.setColor(Color.DARK_GRAY);
            shapes.rect(200, ALTURA - 52, 400, 12);
            shapes.setColor(boss.cor);
            shapes.rect(200, ALTURA - 52, 400 * Math.max(0, boss.vida / boss.vidaMaxima), 12);
            desenharSeta(boss.posicao, Color.PURPLE);
        } else if (boss == null && !bossDerrotado) {
            for (int i = 0; i < controle.altares.size; i++) {
                Altar a = controle.altares.get(i);
                if (!a.escondido && !a.completo()) desenharSeta(a.posicao, a.selado ? Color.GRAY : Color.MAGENTA);
            }
            if (controle.portal != null) desenharSeta(controle.portal.posicao, controle.portal.trancado ? Color.ROYAL : Color.CYAN);
            for (int i = 0; i < controle.chaves.size; i++) desenharSeta(controle.chaves.get(i).posicao, Color.GOLD);
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m.ehPortador()) desenharSeta(m.posicao, m.cor);
                if (m.tipo == TipoMonstro.GUARDIAO_SELO) desenharSeta(m.posicao, Color.FIREBRICK);
            }
        }

        shapes.end();
    }

    private void desenharTextos() {
        batch.begin();
        fonte.setColor(Color.WHITE);

        fonte.draw(batch, "Vida: " + (int) heroi.vida + "/" + (int) heroi.vidaMaxima, 220, ALTURA - 12);
        fonte.draw(batch, "Fase " + faseAtual + "/" + TOTAL_FASES, 345, ALTURA - 12);
        fonte.draw(batch, "Horda " + hordas.getHordaAtual() + "/" + hordas.getTotalHordas(), 425, ALTURA - 12);
        fonte.draw(batch, "Altares " + controle.totalSacrificios() + "/" + controle.totalNecessario(), 525, ALTURA - 12);
        if (controle.selados || controle.portaisTrancados) {
            String chaves = (controle.selados && !controle.guardiaoDoSelo ? "Chaves do altar: " + controle.chavesAltar + "   " : "")
                + (controle.portaisTrancados ? "Chaves do portal: " + controle.chavesPortal : "");
            fonte.draw(batch, chaves, 10, ALTURA - 34);
        }
        fonte.draw(batch, heroi.nomeClasse, 0, ALTURA - 12, LARGURA - 10, Align.right, false);

        if (boss != null && boss.estaVivo()) {
            StringBuilder r = new StringBuilder();
            for (int i = 0; i < boss.getResistencias().length; i++) {
                if (i > 0) r.append(", ");
                r.append(boss.getResistencias()[i].nome);
            }
            fonte.draw(batch, "BOSS: " + boss.nome + "   Fraco: " + boss.getFraqueza().nome + "   Resiste: " + r,
                0, ALTURA - 58, LARGURA, Align.center, false);
            String estado = boss.estado();
            if (!estado.isEmpty()) fonte.draw(batch, estado, 0, ALTURA - 76, LARGURA, Align.center, false);
        }

        // Poderes ativos (2 linhas) + ressonancia
        StringBuilder sb = new StringBuilder();
        if (heroi.poderes.size == 0) sb.append("Poderes: escolha o primeiro ao fim da horda 1");
        for (int i = 0; i < heroi.poderes.size; i++) {
            Poder p = heroi.poderes.get(i);
            if (i > 0) sb.append(" | ");
            sb.append(p.tipo.nome).append(" ").append(p.getNivel());
        }
        fonte.draw(batch, sb.toString(), 10, 64, LARGURA - 230, Align.left, true);
        StringBuilder linha2 = new StringBuilder(heroi.classe.nomeHabilidade + " [ESPACO]");
        if (heroi.elemento != null) linha2.append("   Arma: ").append(heroi.elemento.nome);
        Elemento res = heroi.elementoEmRessonancia();
        if (res != null) linha2.append("   RESSONANCIA: ").append(res.nome).append(" +30%");
        if (heroi.epico != null) linha2.append("   Epico: ").append(heroi.epico.tipo.nome).append(" ").append(heroi.epico.getNivel()).append(" [E]");
        fonte.draw(batch, linha2.toString(), 10, 20);

        String textoBotao = heroi.habilidadePronta() ? "HAB" : String.valueOf((int) Math.ceil(heroi.getRecargaRestante()));
        fonte.setColor(heroi.habilidadePronta() ? Color.BLACK : Color.WHITE);
        fonte.draw(batch, textoBotao, BOTAO_X - BOTAO_RAIO, BOTAO_Y + 6, BOTAO_RAIO * 2, Align.center, false);
        if (heroi.epico != null) {
            String textoEpico = heroi.epico.ativo() ? (int) Math.ceil(heroi.epico.getTempoAtivo()) + "s"
                : heroi.epico.pronto() ? "EPICO" : String.valueOf((int) Math.ceil(heroi.epico.getRecarga()));
            fonte.setColor(heroi.epico.ativo() || heroi.epico.pronto() ? Color.BLACK : Color.WHITE);
            fonte.draw(batch, textoEpico, BOTAO_EPICO_X - BOTAO_EPICO_RAIO, BOTAO_Y + 6, BOTAO_EPICO_RAIO * 2, Align.center, false);
        }

        fonte.setColor(Color.WHITE);
        float centroY = ALTURA / 2f + 60;
        if (!heroi.estaVivo()) {
            fonte.draw(batch, "VOCE MORREU NA FASE " + faseAtual + "!", 0, ALTURA / 2f + 20, LARGURA, Align.center, false);
            fonte.draw(batch, "R = recomecar   |   ESC ou toque = trocar de classe", 0, ALTURA / 2f - 5, LARGURA, Align.center, false);
        } else if (jogoVencido) {
            fonte.draw(batch, "VOCE VENCEU O SENHOR DO ABISMO! FIM DE JOGO!", 0, ALTURA / 2f + 20, LARGURA, Align.center, false);
            fonte.draw(batch, "R = jogar de novo   |   ESC ou toque = trocar de classe", 0, ALTURA / 2f - 5, LARGURA, Align.center, false);
        } else if (faseConcluida) {
            fonte.draw(batch, "FASE " + faseAtual + " CONCLUIDA!", 0, ALTURA / 2f + 20, LARGURA, Align.center, false);
            fonte.draw(batch, "ENTER ou toque = ir para a fase " + (faseAtual + 1) + "  |  R = recomecar  |  ESC = trocar de classe",
                0, ALTURA / 2f - 5, LARGURA, Align.center, false);
        } else if (!escolhendoRecompensa()) {
            if (tempoAvisoFase > 0) {
                fonte.draw(batch, "FASE " + faseAtual + " - " + hordas.getTotalHordas() + " hordas", 0, centroY + 20, LARGURA, Align.center, false);
                fonte.draw(batch, controle.descricaoDaFase(), 0, centroY, LARGURA, Align.center, false);
            } else if (controle.tempoMensagem > 0) {
                fonte.draw(batch, controle.mensagem, 0, centroY, LARGURA, Align.center, false);
            } else if (hordas.concluida() && boss == null && !bossDerrotado) {
                fonte.draw(batch, "HORDAS VENCIDAS! Complete os 2 ALTARES para invocar o BOSS ("
                    + controle.totalSacrificios() + "/" + controle.totalNecessario() + ")", 0, centroY, LARGURA, Align.center, false);
            } else if (tempoAvisoAltar > 0) {
                fonte.draw(batch, "UM ALTAR MUDOU DE LUGAR! Siga as setas roxas.", 0, centroY, LARGURA, Align.center, false);
            } else if (hordas.emIntervalo()) {
                int proxima = hordas.getHordaAtual() + 1;
                fonte.draw(batch, "Prepare-se! Horda " + proxima + " em " + (int) Math.ceil(hordas.getTempoIntervalo()) + "...",
                    0, centroY, LARGURA, Align.center, false);
            }
        }
        batch.end();
    }

    private void desenharEscolha() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.7f);
        shapes.rect(0, 0, LARGURA, ALTURA);

        for (int i = 0; i < opcoes.size; i++) {
            Rectangle c = cards.get(i);
            Recompensa r = opcoes.get(i);
            boolean pequeno = c.height < 100;
            // moldura de raridade
            if (r instanceof RecompensaEpico) {
                shapes.setColor(Color.MAGENTA);
                shapes.rect(c.x - 4, c.y - 4, c.width + 8, c.height + 8);
            } else if (r instanceof RecompensaRara) {
                shapes.setColor(Color.GOLD);
                shapes.rect(c.x - 4, c.y - 4, c.width + 8, c.height + 8);
            }
            shapes.setColor(0.16f, 0.16f, 0.20f, 1f);
            shapes.rect(c.x, c.y, c.width, c.height);
            shapes.setColor(r.getCor());
            shapes.rect(c.x, c.y + c.height - (pequeno ? 22 : 30), c.width, pequeno ? 22 : 30);
            if (!pequeno) shapes.circle(c.x + c.width / 2f, c.y + c.height - 75, 24);
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, tituloEscolha(), 0, ALTURA - 70, LARGURA, Align.center, false);
        fonte.draw(batch, "Clique/toque no card ou aperte o numero", 0, 90, LARGURA, Align.center, false);
        for (int i = 0; i < opcoes.size; i++) {
            Rectangle c = cards.get(i);
            Recompensa r = opcoes.get(i);
            boolean pequeno = c.height < 100;
            fonte.setColor(Color.BLACK);
            fonte.draw(batch, (i + 1) + ". " + r.getNome(), c.x, c.y + c.height - (pequeno ? 5 : 9), c.width, Align.center, false);
            fonte.setColor(Color.WHITE);
            float yDescricao = pequeno ? c.y + c.height - 30 : c.y + 92;
            fonte.draw(batch, r.getDescricao(), c.x + 8, yDescricao, c.width - 16, Align.center, true);
        }
        batch.end();
    }

    private String tituloEscolha() {
        switch (modo) {
            case PRIMEIRO_PODER: return "HORDA CONCLUIDA! ESCOLHA SEU PRIMEIRO PODER";
            case ELEMENTO:       return "HORDA CONCLUIDA! ESCOLHA O ELEMENTO DA SUA ARMA";
            case TROCA_ELEMENTO: return "MUDAR ELEMENTO: ESCOLHA O NOVO ELEMENTO";
            case BOSS:           return "BOSS DERROTADO! ESCOLHA SUA RECOMPENSA";
            case REMOVER_PODER:  return "TROCAR PODER: QUAL PODER SAI? (perde os niveis)";
            case NOVO_PODER:     return "TROCAR PODER: QUAL PODER ENTRA?";
            case PERDER_BUFF:    return "PENALIDADE: ESCOLHA UM BUFF PARA PERDER (faltam " + heroi.penalidadesPendentes + ")";
            default:             return "HORDA CONCLUIDA! ESCOLHA UMA RECOMPENSA";
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, false);
        viewportHud.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        Texture[] texturas = { texChao, texAltar, texHeroiHumano, texHeroiHumanoSemArma, texHeroiAnjo, texHeroiDemonio,
            texMonstroComum, texBossGuardiao };
        for (Texture t : texturas) if (t != null) t.dispose();
        for (int i = 0; i < texturasExtras.size; i++) texturasExtras.get(i).dispose();
        fonte.dispose();
    }
}
