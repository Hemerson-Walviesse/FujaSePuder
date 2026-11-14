package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/**
 * Area no chao criada por um poder: poca, nuvem, pilar que cai, mina, redemoinho...
 *  - atraso: tempo de aviso antes de ativar (circulo vermelho).
 *  - unica: aplica uma vez so (golpe que cai do ceu).
 *  - gatilho: mina, espera um monstro encostar e explode.
 *  - senao: aplica a cada intervaloTick durante a duracao.
 */
public class Zona {
    public final Vector2 posicao = new Vector2();
    public final Vector2 velocidade = new Vector2();
    public float raio;
    public Color cor;
    public float duracao = 1f;
    public float atraso = 0f;
    public float intervaloTick = 0.5f;
    public float dano = 0f;

    // Efeitos nos monstros
    public float lentidao = 1f;       // fator (0.5 = 50% mais lento)
    public float queimadura = 0f;     // dano por segundo
    public float atordoar = 0f;       // segundos
    public float puxar = 0f;          // >0 puxa para o centro, <0 empurra (por segundo)
    public float vulneravel = 0f;     // segundos de +20% de dano recebido

    // Comportamento
    public boolean seguirHeroi = false;
    public boolean unica = false;
    public boolean gatilho = false;

    // Efeitos no heroi (Santuario)
    public float reducaoHeroi = 0f;
    public float curaHeroiPorSegundo = 0f;

    private float tempo = 0f;
    private float acumulado = 0f;
    private boolean disparou = false;
    private float tempoDisparo = 0f;
    private final Vector2 tmp = new Vector2();

    public Zona(float x, float y, float raio, Color cor) {
        posicao.set(x, y);
        this.raio = raio;
        this.cor = cor;
    }

    public void atualizar(float delta, Poder dono, Heroi heroi, Array<Monstro> monstros) {
        tempo += delta;
        if (seguirHeroi) posicao.set(heroi.posicao);
        else posicao.mulAdd(velocidade, delta);
        if (tempo < atraso) return;

        if (gatilho) {
            if (!disparou) {
                for (int i = 0; i < monstros.size; i++) {
                    if (monstros.get(i).posicao.dst(posicao) < raio * 0.5f + monstros.get(i).tamanho / 2f) {
                        aplicar(dono, heroi, monstros);
                        disparou = true;
                        tempoDisparo = tempo;
                        break;
                    }
                }
            }
            return;
        }
        if (unica) {
            if (!disparou) {
                aplicar(dono, heroi, monstros);
                disparou = true;
                tempoDisparo = tempo;
            }
            return;
        }

        // Puxao continuo
        if (puxar != 0f) {
            for (int i = 0; i < monstros.size; i++) {
                Monstro m = monstros.get(i);
                if (m.posicao.dst(posicao) <= raio + m.tamanho / 2f) {
                    tmp.set(posicao).sub(m.posicao);
                    if (tmp.len2() > 4f) {
                        tmp.nor().scl(puxar * delta);
                        m.deslocar(tmp.x, tmp.y);
                    }
                }
            }
        }
        // Santuario
        if ((reducaoHeroi > 0 || curaHeroiPorSegundo > 0) && heroi.posicao.dst(posicao) <= raio) {
            heroi.reducaoTemp = Math.max(heroi.reducaoTemp, reducaoHeroi);
            heroi.curar(curaHeroiPorSegundo * delta);
        }

        acumulado += delta;
        while (acumulado >= intervaloTick) {
            acumulado -= intervaloTick;
            aplicar(dono, heroi, monstros);
        }
    }

    private void aplicar(Poder dono, Heroi heroi, Array<Monstro> monstros) {
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (!m.estaVivo() || m.posicao.dst(posicao) > raio + m.tamanho / 2f) continue;
            if (dano > 0) dono.ferir(heroi, m, dano);
            if (lentidao < 1f) m.aplicarLentidao(lentidao, Math.max(1f, intervaloTick + 0.2f));
            if (queimadura > 0) m.aplicarQueimadura(queimadura, 2.5f);
            if (atordoar > 0) m.atordoar(atordoar);
            if (vulneravel > 0) m.vulnerabilizar(vulneravel);
        }
    }

    public boolean acabou() {
        if (unica || gatilho) {
            if (disparou) return tempo - tempoDisparo > 0.25f;
            return gatilho && tempo >= atraso + duracao;
        }
        return tempo >= atraso + duracao;
    }

    public void desenharLinhas(ShapeRenderer s) {
        if (tempo < atraso) {
            // aviso: circulo de fora e um de dentro que cresce
            s.setColor(cor);
            s.circle(posicao.x, posicao.y, raio);
            s.circle(posicao.x, posicao.y, raio * Math.min(1f, tempo / atraso));
            return;
        }
        if (gatilho && !disparou) {
            s.setColor(cor);
            s.circle(posicao.x, posicao.y, 9f);
            s.circle(posicao.x, posicao.y, 5f);
            return;
        }
        if ((unica || gatilho) && disparou) return; // desenhado em desenharFormas
        s.setColor(cor);
        s.circle(posicao.x, posicao.y, raio);
        s.circle(posicao.x, posicao.y, raio * (0.55f + 0.1f * (float) Math.sin(tempo * 6f)));
    }

    public void desenharFormas(ShapeRenderer s) {
        if ((unica || gatilho) && disparou) {
            float p = (tempo - tempoDisparo) / 0.25f;
            s.setColor(cor);
            s.circle(posicao.x, posicao.y, raio * (0.6f + 0.4f * p));
        }
    }
}
