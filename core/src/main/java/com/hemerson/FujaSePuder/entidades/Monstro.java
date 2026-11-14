package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Um monstro. A posicao e o CENTRO do quadrado.
 * O comportamento muda com o tipo (atirador fica longe, fantasma fica intangivel, etc.).
 * Pedidos de ataque (tiro, cura) sao lidos pela TelaJogo com consumirTiro()/consumirCura().
 */
public class Monstro {
    public final Vector2 posicao = new Vector2();
    public final float tamanho;
    public final float velocidade;
    public final float vidaMaxima;
    public float vida;
    public final float dano;
    public final Color cor;
    /** Chance de soltar um item ao morrer (0.2 = 20%). */
    public final float chanceDrop;
    public TipoMonstro tipo = TipoMonstro.COMUM;
    /** Segundos desde que nasceu (portadores fogem depois de um tempo). */
    public float tempoDeVida = 0f;

    /** Usado por laminas/orbitais para nao acertar o mesmo monstro todo frame. */
    public float tempoImuneOrbita = 0f;

    /** Usado pelo boss na investida (1 = normal). */
    protected float multiplicadorVelocidade = 1f;
    /** 1 = sofre controle normal; 0.25 = lentidao/atordoar/empurrao com 25% do efeito (boss). */
    protected float resistenciaControle = 1f;

    private final Vector2 direcao = new Vector2();
    private final Vector2 empurrao = new Vector2();
    private float tempoPiscando = 0f;

    // Efeitos de status
    private float fatorLentidao = 1f;
    private float tempoLentidao = 0f;
    private float danoQueimaduraPorSegundo = 0f;
    private float tempoQueimadura = 0f;
    private float tempoAtordoado = 0f;
    private float tempoMarcado = 0f;      // Marca da Morte: +50% de dano recebido
    private float tempoVulneravel = 0f;   // Chuva Acida: +20% de dano recebido
    private float tempoMedo = 0f;         // Medo: foge do heroi

    // Comportamentos por tipo
    private float tempoAcao = 1.5f;
    private boolean pedidoTiro = false;
    private boolean pedidoCura = false;
    private float cicloFantasma = 0f;
    public int escudoPontos = 0;          // Blindado
    private float tempoRegeneraEscudo = 0f;

    public Monstro(float x, float y, float vida, float velocidade, float dano, float tamanho, Color cor, float chanceDrop) {
        this.posicao.set(x, y);
        this.vidaMaxima = vida;
        this.vida = vida;
        this.velocidade = velocidade;
        this.dano = dano;
        this.tamanho = tamanho;
        this.cor = cor;
        this.chanceDrop = chanceDrop;
    }

    public Monstro comTipo(TipoMonstro tipo) {
        this.tipo = tipo;
        if (tipo == TipoMonstro.BLINDADO) escudoPontos = 3;
        return this;
    }

    public void atualizar(float delta, Vector2 alvo) {
        tempoDeVida += delta;
        if (tempoPiscando > 0) tempoPiscando -= delta;
        if (tempoImuneOrbita > 0) tempoImuneOrbita -= delta;
        if (tempoMarcado > 0) tempoMarcado -= delta;
        if (tempoVulneravel > 0) tempoVulneravel -= delta;

        if (tempoLentidao > 0) {
            tempoLentidao -= delta;
            if (tempoLentidao <= 0) fatorLentidao = 1f;
        }
        if (tempoQueimadura > 0) {
            tempoQueimadura -= delta;
            vida -= danoQueimaduraPorSegundo * delta;
        }

        atualizarComportamento(delta, alvo);

        float vel = velocidade * fatorLentidao * multiplicadorVelocidade;
        if (tempoAtordoado > 0) {
            tempoAtordoado -= delta; // parado
        } else if (tempoMedo > 0 || ehPortador()) {
            if (tempoMedo > 0) tempoMedo -= delta;
            direcao.set(posicao).sub(alvo);
            if (direcao.len2() > 1f) posicao.add(direcao.nor().scl(vel * delta));
        } else {
            float distancia = posicao.dst(alvo);
            float ideal = distanciaIdeal();
            direcao.set(alvo).sub(posicao);
            if (direcao.len2() > 1f) {
                direcao.nor();
                if (ideal <= 0 || distancia > ideal + 20f) {
                    posicao.mulAdd(direcao, vel * delta);          // aproxima
                } else if (distancia < ideal - 40f) {
                    posicao.mulAdd(direcao, -vel * 0.6f * delta);  // se afasta
                }
            }
        }

        // Empurrao vai perdendo forca
        if (!empurrao.isZero(1f)) {
            posicao.mulAdd(empurrao, delta);
            empurrao.scl(Math.max(0f, 1f - 6f * delta));
        }
    }

    /** Distancia que o monstro tenta manter do heroi (0 = encosta). */
    protected float distanciaIdeal() {
        switch (tipo) {
            case ATIRADOR:   return 260f;
            case CURANDEIRO: return 200f;
            default:         return 0f;
        }
    }

    private void atualizarComportamento(float delta, Vector2 alvo) {
        switch (tipo) {
            case ATIRADOR:
                tempoAcao -= delta;
                if (tempoAcao <= 0 && posicao.dst(alvo) < 450f && tempoAtordoado <= 0) {
                    pedidoTiro = true;
                    tempoAcao = 2.5f;
                }
                break;
            case CURANDEIRO:
                tempoAcao -= delta;
                if (tempoAcao <= 0 && tempoAtordoado <= 0) {
                    pedidoCura = true;
                    tempoAcao = 3f;
                }
                break;
            case FANTASMA:
                cicloFantasma += delta;
                break;
            case BLINDADO:
                if (escudoPontos < 3) {
                    tempoRegeneraEscudo += delta;
                    if (tempoRegeneraEscudo >= 3f) {
                        tempoRegeneraEscudo = 0f;
                        escudoPontos++;
                    }
                }
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------------ dano

    /** Dano sem elemento. */
    public void receberDano(float quantidade) {
        receberDano(quantidade, Elemento.FISICO);
    }

    /** Aplica fraquezas, resistencias, marcas e escudos. Retorna o dano que realmente entrou. */
    public float receberDano(float quantidade, Elemento elemento) {
        if (quantidade <= 0) return 0f;
        if (tipo == TipoMonstro.BLINDADO && escudoPontos > 0) {
            escudoPontos--;          // escudo absorve o golpe inteiro
            tempoRegeneraEscudo = 0f;
            tempoPiscando = 0.1f;
            return 0f;
        }
        float mult = multiplicadorElemento(elemento);
        if (tempoMarcado > 0) mult *= 1.5f;
        if (tempoVulneravel > 0) mult *= 1.2f;
        float real = quantidade * mult;
        if (real <= 0) return 0f;
        vida -= real;
        tempoPiscando = 0.1f;
        return real;
    }

    /** 1 = normal. Bosses sobrescrevem com fraqueza/resistencias. */
    protected float multiplicadorElemento(Elemento elemento) {
        if (intangivel()) return (elemento == Elemento.LUZ || elemento == Elemento.ELETRICO) ? 1f : 0f;
        return 1f;
    }

    public boolean intangivel() {
        return tipo == TipoMonstro.FANTASMA && (cicloFantasma % 4f) > 3f; // 1s a cada 4s
    }

    public void curar(float quantidade) {
        vida = Math.min(vidaMaxima, vida + quantidade);
    }

    // ------------------------------------------------------------------ efeitos

    public void aplicarLentidao(float fator, float duracao) {
        fator = 1f - (1f - fator) * resistenciaControle;
        fatorLentidao = Math.min(fatorLentidao, fator);
        tempoLentidao = Math.max(tempoLentidao, duracao);
    }

    public void aplicarQueimadura(float danoPorSegundo, float duracao) {
        danoQueimaduraPorSegundo = Math.max(danoQueimaduraPorSegundo, danoPorSegundo);
        tempoQueimadura = Math.max(tempoQueimadura, duracao);
    }

    public void atordoar(float duracao) {
        tempoAtordoado = Math.max(tempoAtordoado, duracao * resistenciaControle);
    }

    public void empurrar(float forcaX, float forcaY) {
        empurrao.add(forcaX * resistenciaControle, forcaY * resistenciaControle);
    }

    /** Move direto (puxoes continuos de zonas). */
    public void deslocar(float dx, float dy) {
        posicao.add(dx * resistenciaControle, dy * resistenciaControle);
    }

    public void marcar(float duracao) { tempoMarcado = Math.max(tempoMarcado, duracao); }
    public void vulnerabilizar(float duracao) { tempoVulneravel = Math.max(tempoVulneravel, duracao); }
    public void amedrontar(float duracao) { tempoMedo = Math.max(tempoMedo, duracao * resistenciaControle); }

    public boolean consumirTiro() {
        boolean r = pedidoTiro;
        pedidoTiro = false;
        return r;
    }

    public boolean consumirCura() {
        boolean r = pedidoCura;
        pedidoCura = false;
        return r;
    }

    public boolean ehPortador() { return tipo == TipoMonstro.PORTADOR_CHAVE || tipo == TipoMonstro.PORTADOR_PORTAL; }
    public boolean estaVivo() { return vida > 0; }
    public boolean estaPiscando() { return tempoPiscando > 0; }
    public boolean estaLento() { return tempoLentidao > 0; }
    public boolean estaQueimando() { return tempoQueimadura > 0; }
    public boolean estaAtordoado() { return tempoAtordoado > 0; }
    public boolean estaMarcado() { return tempoMarcado > 0; }
    public boolean temEfeito() { return estaLento() || estaQueimando() || estaAtordoado(); }

    public boolean colideCom(Heroi heroi) {
        return posicao.dst(heroi.posicao) < (tamanho + heroi.tamanho) / 2f;
    }
}
