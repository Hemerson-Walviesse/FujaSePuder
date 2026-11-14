package com.hemerson.FujaSePuder.entidades;

import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.poderes.Projetil;

/** Tudo que poderes e bosses precisam enxergar durante o combate. */
public class ContextoCombate {
    public Heroi heroi;
    public Array<Monstro> monstros;
    public Array<Projetil> projeteis;                 // do heroi
    public Array<ProjetilInimigo> projeteisInimigos;  // dos monstros/boss
    public Array<Meteoro> meteoros;                   // perigos no chao (boss, explosivos)
    public float larguraMundo;
    public float alturaMundo;
    public float fatorFase = 1f;                      // forca dos monstros da fase atual
}
