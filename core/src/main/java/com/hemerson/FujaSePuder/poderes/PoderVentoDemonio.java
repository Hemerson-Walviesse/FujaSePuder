package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** DEMONIO - Vento: rajada em volta do heroi que empurra os monstros para longe. */
public class PoderVentoDemonio extends Poder {
    private final Vector2 empurrao = new Vector2();
    private final Array<Efeito> ondas = new Array<>();

    public PoderVentoDemonio() { super(TipoPoder.VENTO_DEMONIO); }

    @Override protected float intervaloBase() { return 2.5f; }

    @Override
    protected boolean usar(Heroi heroi, Array<Monstro> monstros, Array<Projetil> projeteis) {
        float raio = alcance(heroi, 160f);
        if (maisProximo(heroi.posicao, monstros, raio, null) == null) return false;

        float forca = 420f + 40f * (nivel - 1);
        float d = dano(heroi, 8f);
        for (int i = 0; i < monstros.size; i++) {
            Monstro m = monstros.get(i);
            if (m.estaVivo() && m.posicao.dst(heroi.posicao) <= raio) {
                empurrao.set(m.posicao).sub(heroi.posicao).nor().scl(forca);
                m.empurrar(empurrao.x, empurrao.y);
                ferir(heroi, m, d);
                if (nivel5()) m.atordoar(0.8f); // Nv5: atordoa
            }
        }
        ondas.add(new Efeito(heroi.posicao.x, heroi.posicao.y, raio, 0.3f));
        return true;
    }

    @Override protected void atualizarEfeitos(float delta) { Efeito.atualizarTodos(ondas, delta); }

    @Override
    public void desenharLinhas(ShapeRenderer s, Heroi heroi) {
        s.setColor(tipo.cor);
        for (int i = 0; i < ondas.size; i++) {
            Efeito e = ondas.get(i);
            s.circle(heroi.posicao.x, heroi.posicao.y, e.raio * e.progresso());
            s.circle(heroi.posicao.x, heroi.posicao.y, e.raio * e.progresso() * 0.85f);
        }
    }
}
