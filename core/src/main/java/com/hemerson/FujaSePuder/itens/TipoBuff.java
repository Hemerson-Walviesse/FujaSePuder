package com.hemerson.FujaSePuder.itens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.ClasseHeroi;
import com.hemerson.FujaSePuder.entidades.Heroi;

/**
 * Buffs genericos. Os permanentes ficam guardados em heroi.buffs, assim a penalidade
 * de troca (elemento/classe) consegue remover um e recalcular os atributos.
 * classeExclusiva = null -> disponivel para todas as classes.
 */
public enum TipoBuff implements Recompensa {

    // ---- Vida (comuns a todas as classes, para sempre)
    POCAO_DE_VIDA("Pocao de Vida", "Recupera 40 de vida.", Color.RED, null) {
        @Override public void modificar(Heroi h) { } // instantaneo, nao fica guardado
    },
    CORACAO_EXTRA("Coracao Extra", "+20 de vida maxima e cura 20.", Color.PINK, null) {
        @Override public void modificar(Heroi h) { h.vidaMaxima += 20f; }
    },

    // ---- Genericos
    LAMINA_AFIADA("Lamina Afiada", "+15% de dano (ataque e poderes).", Color.LIGHT_GRAY, null) {
        @Override public void modificar(Heroi h) { h.dano *= 1.15f; }
    },
    BOTAS_LEVES("Botas Leves", "+10% de velocidade.", Color.TAN, null) {
        @Override public void modificar(Heroi h) { h.velocidade *= 1.10f; }
    },
    OLHOS_DE_AGUIA("Olhos de Aguia", "+15% de alcance (ataque e poderes).", Color.CYAN, null) {
        @Override public void modificar(Heroi h) { h.alcanceAtaque *= 1.15f; }
    },
    RITMO_DE_BATALHA("Ritmo de Batalha", "Ataque e poderes 10% mais rapidos.", Color.YELLOW, null) {
        @Override public void modificar(Heroi h) { h.intervaloAtaque *= 0.90f; }
    },
    CONCENTRACAO("Concentracao", "Habilidade especial recarrega 15% mais rapido.", Color.VIOLET, null) {
        @Override public void modificar(Heroi h) { h.multiplicadorRecarga *= 0.85f; }
    },
    PELE_GROSSA("Pele Grossa", "Recebe 10% menos dano (maximo 60%).", Color.BROWN, null) {
        @Override public void modificar(Heroi h) { h.reducaoDano = Math.min(0.6f, h.reducaoDano + 0.10f); }
    };

    public final String nome;
    public final String descricao;
    public final Color cor;
    public final ClasseHeroi classeExclusiva;

    TipoBuff(String nome, String descricao, Color cor, ClasseHeroi classeExclusiva) {
        this.nome = nome;
        this.descricao = descricao;
        this.cor = cor;
        this.classeExclusiva = classeExclusiva;
    }

    /** Altera os atributos do heroi. Chamado por Heroi.recalcularAtributos(). */
    public abstract void modificar(Heroi heroi);

    public boolean permanente() {
        return this != POCAO_DE_VIDA;
    }

    @Override
    public void aplicar(Heroi heroi) {
        if (!permanente()) {
            heroi.curar(40f);
            return;
        }
        heroi.buffs.add(this);
        heroi.recalcularAtributos();
        if (this == CORACAO_EXTRA) heroi.curar(20f);
    }

    @Override public String getNome() { return nome; }
    @Override public String getDescricao() { return descricao; }
    @Override public Color getCor() { return cor; }

    public boolean disponivelPara(ClasseHeroi classe) {
        return classeExclusiva == null || classeExclusiva == classe;
    }
}
