package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.itens.TipoBuff;
import com.hemerson.FujaSePuder.poderes.ElementoArma;
import com.hemerson.FujaSePuder.poderes.Poder;
import com.hemerson.FujaSePuder.poderes.PoderEpico;
import com.hemerson.FujaSePuder.poderes.TipoEpico;
import com.hemerson.FujaSePuder.poderes.TipoPoder;

/** O personagem controlado pelo jogador. A posicao e o CENTRO do quadrado. */
public class Heroi {

    private static final float DURACAO_EFEITO_HABILIDADE = 0.3f;
    private static final float DURACAO_FURIA = 5f;
    /** Roubo de vida do Demonio: no maximo isso de cura por segundo. */
    private static final float LIMITE_ROUBO_POR_SEGUNDO = 2f;
    /** Ate 5 poderes ativos ao mesmo tempo. */
    public static final int MAX_PODERES = 5;
    /** Ressonancia: com 3+ poderes do mesmo elemento, eles causam +30% de dano. */
    public static final int PODERES_PARA_RESSONANCIA = 3;
    public static final float BONUS_RESSONANCIA = 1.3f;

    public ClasseHeroi classe;                 // pode mudar na troca de classe do boss
    public String nomeClasse;                  // ex.: "Anjo Caido" depois da troca
    public boolean jaTrocouDeClasse = false;
    public PoderEpico epico = null;            // ganho no boss
    public final Vector2 posicao = new Vector2();
    public final float tamanho = 32f;
    public final Array<Poder> poderes = new Array<>();
    /** Buffs permanentes pegos (podem repetir). */
    public final Array<TipoBuff> buffs = new Array<>();

    // Elemento da arma (so Humano) e niveis das 2 melhorias do elemento
    public ElementoArma elemento = null;
    public final int[] niveisElemento = new int[2];

    // Pedidos que a TelaJogo resolve abrindo telas de escolha
    public boolean pedidoTrocaElemento = false;
    public boolean pedidoTrocaPoder = false;   // carta "Trocar Poder": escolher qual sai
    public boolean pedidoNovoPoder = false;    // depois de remover: escolher qual entra
    public TipoPoder ultimoRemovido = null;
    public int penalidadesPendentes = 0;       // quantos buffs ainda precisa escolher para perder

    // Atributos (os buffs alteram esses valores)
    public float velocidade;
    public float vidaMaxima;
    public float vida;
    public float dano;            // comparado com o da classe para dar o multiplicador de dano dos poderes
    public float alcanceAtaque;   // idem para o alcance
    public float intervaloAtaque; // idem para a velocidade de ataque
    public float multiplicadorRecarga = 1f; // 0.85 = habilidade recarrega 15% mais rapido
    public float reducaoDano = 0f;          // 0.10 = recebe 10% menos dano

    // Modificadores temporarios, recalculados todo frame pelos poderes passivos
    public float bonusVelocidadeTemp = 1f;
    public float bonusDanoTemp = 1f;
    public float reducaoTemp = 0f;

    // Defesas vindas de poderes
    public float escudo = 0f;                  // Escudo de Energia: absorve dano
    public boolean bloqueioPronto = false;     // Guardiao: bloqueia 1 golpe inteiro
    public float danoRecebidoAcumulado = 0f;   // Adrenalina le e zera
    public float absorvidoPeloEscudo = 0f;     // Escudo de Energia (Nv5) le e zera para refletir

    private float tempoDesdeAtaque = 0f;
    private float tempoInvencivel = 0f;
    private float recargaRestante = 0f;
    private float furiaRestante = 0f;
    private float tempoEfeitoHabilidade = 0f;
    private float roubadoNoSegundo = 0f;
    private float tempoJanelaRoubo = 0f;

    public Heroi(float x, float y, ClasseHeroi classe) {
        this.classe = classe;
        this.nomeClasse = classe.nome;
        posicao.set(x, y);
        velocidade = classe.velocidade;
        vidaMaxima = classe.vidaMaxima;
        vida = vidaMaxima;
        dano = classe.dano;
        alcanceAtaque = classe.alcanceAtaque;
        intervaloAtaque = classe.intervaloAtaque;
    }

    /** Volta aos atributos da classe e reaplica todos os buffs guardados. */
    public void recalcularAtributos() {
        velocidade = classe.velocidade;
        vidaMaxima = classe.vidaMaxima;
        dano = classe.dano;
        alcanceAtaque = classe.alcanceAtaque;
        intervaloAtaque = classe.intervaloAtaque;
        multiplicadorRecarga = 1f;
        reducaoDano = 0f;
        for (int i = 0; i < buffs.size; i++) buffs.get(i).modificar(this);
        vida = Math.min(vida, vidaMaxima);
    }

    public int nivelElemento(int espaco) {
        return niveisElemento[espaco];
    }

    public void atualizar(float delta) {
        tempoDesdeAtaque += delta;
        if (tempoInvencivel > 0) tempoInvencivel -= delta;
        if (recargaRestante > 0) recargaRestante -= delta;
        if (furiaRestante > 0) furiaRestante -= delta;
        if (tempoEfeitoHabilidade > 0) tempoEfeitoHabilidade -= delta;

        tempoJanelaRoubo += delta;
        if (tempoJanelaRoubo >= 1f) {
            tempoJanelaRoubo = 0f;
            roubadoNoSegundo = 0f;
        }

        // Passiva do Humano: 1 de vida a cada 2s
        if (classe == ClasseHeroi.HUMANO && estaVivo()) curar(0.5f * delta);
    }

    /** Zera os modificadores temporarios; os poderes passivos aplicam de novo no mesmo frame. */
    public void iniciarModificadores() {
        bonusVelocidadeTemp = 1f;
        bonusDanoTemp = 1f;
        reducaoTemp = 0f;
    }

    // ------------------------------------------------------ multiplicadores dos poderes

    /** 1.0 = normal. Sobe com buffs de dano, Furia, Forma Infernal e passivos. */
    public float fatorDano() {
        float f = dano / classe.dano * bonusDanoTemp;
        if (formaInfernalAtiva()) f *= 1.3f;
        return emFuria() ? f * 1.25f : f;
    }

    /** 1.0 = normal. Menor = poderes recarregam mais rapido. */
    public float fatorVelocidadeAtaque() {
        float f = intervaloAtaque / classe.intervaloAtaque;
        return emFuria() ? f * 0.5f : f;
    }

    /** 1.0 = normal. Sobe com buffs de alcance. */
    public float fatorAlcance() {
        return alcanceAtaque / classe.alcanceAtaque;
    }

    public float velocidadeAtual() {
        return velocidade * bonusVelocidadeTemp;
    }

    /** Ressonancia: +30% de dano para poderes de um elemento que aparece em 3+ poderes ativos. */
    public float fatorRessonancia(Elemento e) {
        if (e == null || e == Elemento.FISICO) return 1f;
        int n = 0;
        for (int i = 0; i < poderes.size; i++) if (poderes.get(i).elemento(this) == e) n++;
        return n >= PODERES_PARA_RESSONANCIA ? BONUS_RESSONANCIA : 1f;
    }

    /** Elemento que esta em ressonancia agora (ou null). */
    public Elemento elementoEmRessonancia() {
        for (Elemento e : Elemento.values()) {
            if (e != Elemento.FISICO && fatorRessonancia(e) > 1f) return e;
        }
        return null;
    }

    // ------------------------------------------------------------- ataque basico

    public float getDanoAtual() {
        float d = dano * bonusDanoTemp;
        if (formaInfernalAtiva()) d *= 1.3f;
        return emFuria() ? d * 1.25f : d;
    }

    /** Elemento do ataque basico (o do Anjo e de Luz). */
    public Elemento elementoAtaqueBasico() {
        return classe == ClasseHeroi.ANJO ? Elemento.LUZ : classe == ClasseHeroi.DEMONIO ? Elemento.SOMBRA : Elemento.FISICO;
    }

    /** Epico do Demonio ativo: +30% de dano. */
    public boolean formaInfernalAtiva() {
        return epico != null && epico.tipo == TipoEpico.FORMA_INFERNAL && epico.ativo();
    }

    public float getIntervaloAtual() {
        return emFuria() ? intervaloAtaque * 0.5f : intervaloAtaque;
    }

    public boolean podeAtacar() {
        return tempoDesdeAtaque >= getIntervaloAtual();
    }

    public void registrarAtaque() {
        tempoDesdeAtaque = 0f;
    }

    /** Passiva do Anjo: o ataque basico acerta 2 monstros. */
    public int alvosPorAtaque() {
        return classe == ClasseHeroi.ANJO ? 2 : 1;
    }

    /** Chamado sempre que o heroi causa dano. Passiva do Demonio: roubo de vida limitado. */
    public void aoCausarDano(float danoCausado) {
        if (classe == ClasseHeroi.DEMONIO && danoCausado > 0) {
            float cura = Math.min(danoCausado * 0.05f, LIMITE_ROUBO_POR_SEGUNDO - roubadoNoSegundo);
            if (cura > 0) {
                curar(cura);
                roubadoNoSegundo += cura;
            }
        }
    }

    // ------------------------------------------------------------- troca de classe

    /**
     * Troca de classe (boss). Cada poder vira o poder da nova classe com o MESMO PAPEL, no nivel 1.
     * Virando Humano, o elemento da arma vem do elemento do 1o poder (Fogo -> Fogo, Gelo/Agua -> Gelo, resto -> Laser).
     * A penalidade (perder 2 buffs) e resolvida pela TelaJogo.
     */
    public void trocarClasse(ClasseHeroi nova, String nomeTransformacao) {
        Array<TipoPoder> antigos = new Array<>();
        for (int i = 0; i < poderes.size; i++) antigos.add(poderes.get(i).tipo);
        poderes.clear();

        ElementoArma novoElemento = null;
        if (nova == ClasseHeroi.HUMANO && antigos.size > 0) {
            Elemento e = antigos.first().elemento;
            novoElemento = e == Elemento.FOGO ? ElementoArma.FOGO : e == Elemento.GELO ? ElementoArma.GELO : ElementoArma.LASER;
        }

        Array<TipoPoder> usados = new Array<>();
        for (int i = 0; i < antigos.size; i++) {
            TipoPoder eq = TipoPoder.equivalente(antigos.get(i), nova, usados);
            if (eq != null) {
                usados.add(eq);
                adicionarPoder(eq.criar());
            }
        }

        float porcentagemVida = vida / vidaMaxima;
        classe = nova;
        nomeClasse = nomeTransformacao;
        elemento = novoElemento;
        niveisElemento[0] = 0;
        niveisElemento[1] = 0;
        recalcularAtributos();
        vida = vidaMaxima * porcentagemVida;

        recargaRestante = 0f;
        furiaRestante = 0f;
        jaTrocouDeClasse = true;
        penalidadesPendentes += 2;
        if (epico != null) epico.tipo = TipoEpico.para(this);
    }

    // ------------------------------------------------------------------ poderes

    public Poder getPoder(TipoPoder tipo) {
        for (int i = 0; i < poderes.size; i++) {
            if (poderes.get(i).tipo == tipo) return poderes.get(i);
        }
        return null;
    }

    public boolean temPoder(TipoPoder tipo) {
        return getPoder(tipo) != null;
    }

    public void adicionarPoder(Poder poder) {
        if (poderes.size < MAX_PODERES && !temPoder(poder.tipo)) poderes.add(poder);
    }

    /** Remove o poder (perde todos os niveis dele). Usado pela carta Trocar Poder. */
    public void removerPoder(TipoPoder tipo) {
        Poder p = getPoder(tipo);
        if (p != null) {
            p.aoRemover(this);
            poderes.removeValue(p, true);
        }
    }

    // ---------------------------------------------------------------- habilidade

    public boolean habilidadePronta() {
        return recargaRestante <= 0;
    }

    /** Usa a habilidade especial da classe. Retorna false se ainda estiver recarregando. */
    public boolean usarHabilidade(Array<Monstro> monstros) {
        if (!habilidadePronta() || !estaVivo()) return false;

        switch (classe) {
            case HUMANO:
                causarDanoEmArea(monstros, getRaioHabilidade(), 40f, Elemento.FISICO);
                break;
            case DEMONIO:
                furiaRestante = DURACAO_FURIA;
                break;
            case ANJO:
                curar(30f);
                causarDanoEmArea(monstros, getRaioHabilidade(), 30f, Elemento.LUZ);
                break;
        }

        recargaRestante = classe.recargaHabilidade * multiplicadorRecarga;
        tempoEfeitoHabilidade = DURACAO_EFEITO_HABILIDADE;
        return true;
    }

    private void causarDanoEmArea(Array<Monstro> monstros, float raio, float quantidade, Elemento e) {
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (m.posicao.dst(posicao) <= raio) aoCausarDano(m.receberDano(quantidade, e));
        }
    }

    /** Raio do efeito em area (0 para habilidades sem area). */
    public float getRaioHabilidade() {
        switch (classe) {
            case HUMANO: return 130f;
            case ANJO:   return 220f;
            default:     return 0f;
        }
    }

    // --------------------------------------------------------------- vida / estado

    public void receberDano(float quantidade) {
        if (tempoInvencivel > 0) return; // meio segundo de invencibilidade apos levar dano
        tempoInvencivel = 0.5f;
        if (bloqueioPronto) {            // Guardiao
            bloqueioPronto = false;
            return;
        }
        float d = quantidade * (1f - Math.min(0.8f, reducaoDano + reducaoTemp));
        if (escudo > 0) {                // Escudo de Energia
            float absorvido = Math.min(escudo, d);
            escudo -= absorvido;
            absorvidoPeloEscudo += absorvido;
            d -= absorvido;
        }
        vida = Math.max(0, vida - d);
        danoRecebidoAcumulado += d;
    }

    public void ficarInvencivel(float segundos) {
        tempoInvencivel = Math.max(tempoInvencivel, segundos);
    }

    public void curar(float quantidade) {
        vida = Math.min(vidaMaxima, vida + quantidade);
    }

    public boolean estaVivo() { return vida > 0; }
    public boolean estaInvencivel() { return tempoInvencivel > 0; }
    public boolean emFuria() { return furiaRestante > 0; }
    public float getRecargaRestante() { return Math.max(0, recargaRestante); }
    public float getTempoEfeitoHabilidade() { return tempoEfeitoHabilidade; }
    public float getDuracaoEfeitoHabilidade() { return DURACAO_EFEITO_HABILIDADE; }
}
