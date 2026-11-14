package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.Heroi;
import com.hemerson.FujaSePuder.entidades.Monstro;

/** Lista de zonas de um poder: atualiza, remove as que acabaram e desenha. */
public class ListaZonas {
    public final Array<Zona> zonas = new Array<>();

    public void add(Zona z) { zonas.add(z); }
    public int size() { return zonas.size; }

    public void atualizar(float delta, Poder dono, Heroi heroi, Array<Monstro> monstros) {
        for (int i = zonas.size - 1; i >= 0; i--) {
            Zona z = zonas.get(i);
            z.atualizar(delta, dono, heroi, monstros);
            if (z.acabou()) zonas.removeIndex(i);
        }
    }

    public void desenharLinhas(ShapeRenderer s) {
        for (int i = 0; i < zonas.size; i++) zonas.get(i).desenharLinhas(s);
    }

    public void desenharFormas(ShapeRenderer s) {
        for (int i = 0; i < zonas.size; i++) zonas.get(i).desenharFormas(s);
    }
}
