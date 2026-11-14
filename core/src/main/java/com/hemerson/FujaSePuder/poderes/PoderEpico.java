package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/**
 * Poder epico: ativado por botao (tecla E), fica ativo por 5s (+0.5s por nivel) e depois recarrega 30s.
 * Enquanto ativo, causa efeito em "pulsos" a cada 0.2s. Evolui ate o nivel 5 nos bosses.
 */
public class PoderEpico {

    public static final float DURACAO_BASE = 5f;
    public static final float RECARGA = 30f;
    public static final int NIVEL_MAXIMO = 5;
    private static final float INTERVALO_PULSO = 0.2f;

    // Tamanhos dos efeitos
    private static final float ALCANCE_CHAMAS = 210f;
    private static final float ANGULO_CHAMAS = 30f;   // metade da abertura do cone
    private static final float RAIO_NEVASCA = 190f;
    private static final float COMPRIMENTO_LASER = 650f;
    private static final float LARGURA_LASER = 16f;
    private static final float RAIO_INFERNAL = 140f;
    private static final float ALCANCE_JULGAMENTO = 320f;

    public TipoEpico tipo;
    private int nivel = 1;
    private float tempoAtivo = 0f;
    private float recarga = 0f;
    private float acumulador = 0f;

    private final Vector2 direcao = new Vector2(1, 0);
    private final Vector2 tmp = new Vector2();
    private final Vector2 fimLaser = new Vector2();
    private final Array<Efeito> efeitos = new Array<>();
    private final Array<Monstro> candidatos = new Array<>();

    public PoderEpico(TipoEpico tipo) {
        this.tipo = tipo;
    }

    public int getNivel() { return nivel; }
    public void subirNivel() { if (nivel < NIVEL_MAXIMO) nivel++; }
    public boolean ativo() { return tempoAtivo > 0; }
    public boolean pronto() { return recarga <= 0 && tempoAtivo <= 0; }
    public float getTempoAtivo() { return Math.max(0, tempoAtivo); }
    public float getRecarga() { return Math.max(0, recarga); }
    public float duracao() { return DURACAO_BASE + (nivel - 1) * 0.5f; }

    public void ativar() {
        if (!pronto()) return;
        tempoAtivo = duracao();
        acumulador = INTERVALO_PULSO; // primeiro pulso ja no proximo frame
    }

    public void atualizar(float delta, Heroi heroi, Array<Monstro> monstros) {
        Efeito.atualizarTodos(efeitos, delta);
        if (tempoAtivo > 0) {
            tempoAtivo -= delta;
            mirar(heroi, monstros);
            acumulador += delta;
            while (acumulador >= INTERVALO_PULSO) {
                acumulador -= INTERVALO_PULSO;
                pulso(heroi, monstros);
            }
            if (tempoAtivo <= 0) recarga = RECARGA * heroi.multiplicadorRecarga;
        } else if (recarga > 0) {
            recarga -= delta;
        }
    }

    /** Lanca-chamas e laser giram suavemente na direcao do monstro mais proximo. */
    private void mirar(Heroi heroi, Array<Monstro> monstros) {
        Monstro alvo = maisProximo(heroi.posicao, monstros, 700f);
        if (alvo == null) return;
        tmp.set(alvo.posicao).sub(heroi.posicao).nor();
        direcao.lerp(tmp, 0.25f);
        if (direcao.isZero(0.01f)) direcao.set(tmp);
        direcao.nor();
    }

    private void pulso(Heroi heroi, Array<Monstro> monstros) {
        float f = (1f + 0.3f * (nivel - 1)) * heroi.fatorDano();

        switch (tipo) {
            case LANCA_CHAMAS: {
                float cosMeioAngulo = MathUtils.cosDeg(ANGULO_CHAMAS);
                for (int i = 0; i < monstros.size; i++) {
                    Monstro m = monstros.get(i);
                    tmp.set(m.posicao).sub(heroi.posicao);
                    float d = tmp.len();
                    if (m.estaVivo() && d > 0 && d <= ALCANCE_CHAMAS + m.tamanho / 2f
                        && tmp.scl(1f / d).dot(direcao) >= cosMeioAngulo) {
                        ferir(heroi, m, 12f * f);
                        m.aplicarQueimadura(8f * f, 2f);
                    }
                }
                break;
            }
            case NEVASCA: {
                for (int i = 0; i < monstros.size; i++) {
                    Monstro m = monstros.get(i);
                    if (m.estaVivo() && m.posicao.dst(heroi.posicao) <= RAIO_NEVASCA + m.tamanho / 2f) {
                        ferir(heroi, m, 6f * f);
                        m.aplicarLentidao(0.3f, 1f);
                        if (MathUtils.randomBoolean(0.12f)) m.atordoar(1f);
                    }
                }
                break;
            }
            case CANHAO_LASER: {
                fimLaser.set(direcao).scl(COMPRIMENTO_LASER).add(heroi.posicao);
                for (int i = 0; i < monstros.size; i++) {
                    Monstro m = monstros.get(i);
                    if (m.estaVivo() && Intersector.distanceSegmentPoint(heroi.posicao, fimLaser, m.posicao)
                        < LARGURA_LASER + m.tamanho / 2f) {
                        ferir(heroi, m, 14f * f);
                    }
                }
                break;
            }
            case FORMA_INFERNAL: {
                for (int i = 0; i < monstros.size; i++) {
                    Monstro m = monstros.get(i);
                    if (m.estaVivo() && m.posicao.dst(heroi.posicao) <= RAIO_INFERNAL + m.tamanho / 2f) {
                        ferir(heroi, m, 10f * f);
                        m.aplicarQueimadura(6f * f, 2f);
                    }
                }
                break;
            }
            case JULGAMENTO_DIVINO: {
                candidatos.clear();
                for (int i = 0; i < monstros.size; i++) {
                    Monstro m = monstros.get(i);
                    if (m.estaVivo() && Poder.naVisao(m) && m.posicao.dst(heroi.posicao) <= ALCANCE_JULGAMENTO) candidatos.add(m);
                }
                int quantidade = 2 + (nivel - 1) / 2;
                for (int k = 0; k < quantidade && candidatos.size > 0; k++) {
                    Monstro m = candidatos.removeIndex(MathUtils.random(candidatos.size - 1));
                    ferir(heroi, m, 24f * f);
                    m.atordoar(0.4f);
                    efeitos.add(new Efeito(m.posicao.x, m.posicao.y, 26f, 0.3f));
                }
                break;
            }
        }
    }

    // ---------------------------------------------------------------- desenho

    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        // Pilares do Julgamento Divino
        s.setColor(Color.GOLD);
        for (int i = 0; i < efeitos.size; i++) {
            Efeito e = efeitos.get(i);
            float r = e.raio * (1f - 0.5f * e.progresso());
            s.circle(e.x, e.y, r);
            s.line(e.x - r * 0.5f, e.y, e.x - r * 0.5f, e.y + 300f * (1f - e.progresso()));
            s.line(e.x + r * 0.5f, e.y, e.x + r * 0.5f, e.y + 300f * (1f - e.progresso()));
        }
        if (!ativo()) return;

        float x = heroi.posicao.x;
        float y = heroi.posicao.y;
        switch (tipo) {
            case LANCA_CHAMAS: {
                float a = direcao.angleDeg();
                s.setColor(Color.ORANGE);
                for (float k = -ANGULO_CHAMAS; k <= ANGULO_CHAMAS; k += 7.5f) {
                    tmp.set(ALCANCE_CHAMAS * MathUtils.random(0.7f, 1f), 0).rotateDeg(a + k);
                    s.line(x, y, x + tmp.x, y + tmp.y);
                }
                s.setColor(Color.RED);
                s.arc(x, y, ALCANCE_CHAMAS, a - ANGULO_CHAMAS, ANGULO_CHAMAS * 2);
                break;
            }
            case NEVASCA:
                s.setColor(Color.CYAN);
                s.circle(x, y, RAIO_NEVASCA);
                s.circle(x, y, RAIO_NEVASCA * MathUtils.random(0.3f, 1f));
                break;
            case FORMA_INFERNAL:
                s.setColor(Color.RED);
                s.circle(x, y, RAIO_INFERNAL);
                s.setColor(Color.ORANGE);
                s.circle(x, y, RAIO_INFERNAL * MathUtils.random(0.5f, 1f));
                break;
            default:
                break;
        }
    }

    public void desenharFormas(ShapeRenderer s, Heroi heroi) {
        if (!ativo()) return;
        if (tipo == TipoEpico.CANHAO_LASER) {
            fimLaser.set(direcao).scl(COMPRIMENTO_LASER).add(heroi.posicao);
            s.setColor(Color.MAGENTA);
            s.rectLine(heroi.posicao, fimLaser, LARGURA_LASER * 1.8f);
            s.setColor(Color.WHITE);
            s.rectLine(heroi.posicao, fimLaser, LARGURA_LASER * 0.6f);
        } else if (tipo == TipoEpico.NEVASCA) {
            s.setColor(Color.WHITE);
            for (int k = 0; k < 14; k++) {
                tmp.set(MathUtils.random(RAIO_NEVASCA), 0).rotateDeg(MathUtils.random(360f));
                s.circle(heroi.posicao.x + tmp.x, heroi.posicao.y + tmp.y, 3f);
            }
        }
    }

    // ---------------------------------------------------------------- utilitarios

    private void ferir(Heroi heroi, Monstro m, float quantidade) {
        heroi.aoCausarDano(m.receberDano(quantidade, tipo.elemento()));
    }

    private static Monstro maisProximo(Vector2 de, Array<Monstro> monstros, float alcance) {
        Monstro escolhido = null;
        float menor = alcance;
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (!m.estaVivo() || !Poder.naVisao(m)) continue;
            float d = m.posicao.dst(de);
            if (d <= menor) {
                menor = d;
                escolhido = m;
            }
        }
        return escolhido;
    }
}
