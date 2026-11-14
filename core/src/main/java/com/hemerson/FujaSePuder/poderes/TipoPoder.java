package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.hemerson.FujaSePuder.entidades.ClasseHeroi;
import com.hemerson.FujaSePuder.entidades.Elemento;

/**
 * Os 60 poderes do jogo (20 por classe).
 * Papel: 0 = Dano, 1 = Controle, 2 = Area, 3 = Defesa, 4 = Suporte (usado na troca de classe).
 * Elemento: usado nas fraquezas dos bosses e na ressonancia. "tiro" = usa o elemento da arma do Humano.
 * Os numeros de cada poder ficam na FabricaPoderes.
 */
public enum TipoPoder {

    // ======================================================== HUMANO (armas e equipamentos)
    PISTOLA(ClasseHeroi.HUMANO, 0, Elemento.FISICO, true, "Pistola", "Tiro rapido no monstro mais proximo. Nv5: 2 tiros seguidos.", Color.LIGHT_GRAY),
    DUPLA(ClasseHeroi.HUMANO, 2, Elemento.FISICO, true, "Dupla", "2 tiros abertos. Nv5: 4 tiros.", Color.WHITE),
    ESCOPETA(ClasseHeroi.HUMANO, 2, Elemento.FISICO, true, "Escopeta", "Leque de 6 balas curtas que empurram. Nv5: 9 balas.", Color.TAN),
    FUZIL(ClasseHeroi.HUMANO, 0, Elemento.FISICO, true, "Fuzil de Precisao", "Tiro forte no monstro com mais vida, atravessa 2. Nv5: critico 2x.", Color.LIGHT_GRAY),
    METRALHADORA(ClasseHeroi.HUMANO, 0, Elemento.FISICO, true, "Metralhadora", "Rajada de 8 tiros a cada 3s. Nv5: 14 tiros.", Color.GRAY),
    GRANADA(ClasseHeroi.HUMANO, 2, Elemento.FISICO, false, "Granada", "Explode onde cair. Nv5: 3 granadas.", Color.OLIVE),
    MINA(ClasseHeroi.HUMANO, 1, Elemento.FISICO, false, "Mina Terrestre", "Deixa minas por onde passa. Nv5: ate 6 minas.", Color.FIREBRICK),
    TORRETA(ClasseHeroi.HUMANO, 4, Elemento.FISICO, true, "Torreta", "Torreta fixa atira sozinha por 8s. Nv5: 2 torretas.", Color.GRAY),
    DRONE(ClasseHeroi.HUMANO, 0, Elemento.FISICO, true, "Drone de Combate", "Drone gira em volta e atira. Nv5: 2 drones.", Color.SKY),
    BUMERANGUE(ClasseHeroi.HUMANO, 2, Elemento.FISICO, false, "Bumerangue", "Vai e volta atravessando. Nv5: 2 bumerangues.", Color.BROWN),
    ARPAO(ClasseHeroi.HUMANO, 1, Elemento.FISICO, false, "Arpao", "Puxa um monstro e prende 1s. Nv5: puxa 3.", Color.LIGHT_GRAY),
    REDE(ClasseHeroi.HUMANO, 1, Elemento.FISICO, false, "Rede", "Prende os monstros de uma area por 2s. Nv5: maior e com dano.", Color.TAN),
    ESCUDO_ENERGIA(ClasseHeroi.HUMANO, 3, Elemento.FISICO, false, "Escudo de Energia", "Absorve 20 de dano, recarrega em 10s. Nv5: 60 e reflete.", Color.CYAN),
    KIT_MEDICO(ClasseHeroi.HUMANO, 4, Elemento.FISICO, false, "Kit Medico", "Cura 3% da vida a cada 5s. Nv5: 6%.", Color.GREEN),
    BOMBA_FUMACA(ClasseHeroi.HUMANO, 3, Elemento.FISICO, false, "Bomba de Fumaca", "Nuvem que deixa os monstros 50% mais lentos. Nv5: invisivel 1s.", Color.GRAY),
    LAMINA_GIRATORIA(ClasseHeroi.HUMANO, 2, Elemento.FISICO, false, "Lamina Giratoria", "Espada gira em volta de voce. Nv5: 2 laminas.", Color.WHITE),
    ATAQUE_AEREO(ClasseHeroi.HUMANO, 2, Elemento.FOGO, false, "Ataque Aereo", "Bombardeio em linha. Nv5: 2 linhas em cruz.", Color.ORANGE),
    COQUETEL(ClasseHeroi.HUMANO, 2, Elemento.FISICO, false, "Coquetel", "Poca do elemento da arma por 4s. Nv5: poca 2x maior.", Color.CORAL),
    TASER(ClasseHeroi.HUMANO, 1, Elemento.ELETRICO, false, "Taser", "Atordoa 1s e salta para 1 vizinho. Nv5: salta para 3.", Color.YELLOW),
    ADRENALINA(ClasseHeroi.HUMANO, 4, Elemento.FISICO, false, "Adrenalina", "Ao perder 25% da vida: +30% velocidade e dano por 4s. Nv5: +60%.", Color.RED),

    // ======================================================== DEMONIO (fogo, agua, vento, sombra, sangue)
    FOGO(ClasseHeroi.DEMONIO, 0, Elemento.FOGO, false, "Fogo", "Bola de fogo que explode e queima. Nv5: 3 bolas.", Color.ORANGE),
    AGUA(ClasseHeroi.DEMONIO, 1, Elemento.GELO, false, "Agua", "Jato que atravessa e deixa lento. Nv5: jato duplo.", Color.ROYAL),
    VENTO_DEMONIO(ClasseHeroi.DEMONIO, 3, Elemento.VENTO, false, "Vento", "Rajada que empurra os monstros. Nv5: atordoa.", Color.SLATE),
    CHUVA_ACIDA(ClasseHeroi.DEMONIO, 2, Elemento.GELO, false, "Chuva Acida", "Nuvem segue voce causando dano. Nv5: +20% de dano neles.", Color.CHARTREUSE),
    CORRENTE_INFERNAL(ClasseHeroi.DEMONIO, 1, Elemento.SOMBRA, false, "Corrente Infernal", "Prende um monstro e arrasta. Nv5: prende 3.", Color.VIOLET),
    PILAR_DE_FOGO(ClasseHeroi.DEMONIO, 2, Elemento.FOGO, false, "Pilar de Fogo", "Colunas de fogo sob monstros aleatorios. Nv5: 2x mais.", Color.ORANGE),
    TSUNAMI(ClasseHeroi.DEMONIO, 2, Elemento.GELO, false, "Tsunami", "Onda atravessa em linha e empurra. Nv5: 2 ondas em X.", Color.ROYAL),
    SOMBRA_VIVA(ClasseHeroi.DEMONIO, 4, Elemento.SOMBRA, false, "Sombra Viva", "Clone de sombra luta por 6s. Nv5: 2 clones.", Color.VIOLET),
    SEDE_DE_SANGUE(ClasseHeroi.DEMONIO, 0, Elemento.SOMBRA, false, "Sede de Sangue", "+1% de dano por 1% de vida perdida. Nv5: +1,5%.", Color.SCARLET),
    GARRAS(ClasseHeroi.DEMONIO, 0, Elemento.SOMBRA, false, "Garras Sombrias", "Golpe em cone na frente, dano alto. Nv5: cone 2x.", Color.VIOLET),
    METEORO_INFERNAL(ClasseHeroi.DEMONIO, 2, Elemento.FOGO, false, "Meteoro Infernal", "Meteoro cai no maior grupo. Nv5: 3 meteoros.", Color.FIREBRICK),
    REDEMOINHO(ClasseHeroi.DEMONIO, 1, Elemento.GELO, false, "Redemoinho", "Vortice puxa os monstros. Nv5: tambem causa dano.", Color.ROYAL),
    LANCA_GELO_NEGRO(ClasseHeroi.DEMONIO, 0, Elemento.GELO, false, "Lanca de Gelo Negro", "Atravessa e congela 1s. Nv5: 2s.", Color.CYAN),
    CICLONE_BRASAS(ClasseHeroi.DEMONIO, 2, Elemento.FOGO, false, "Ciclone de Brasas", "Tornado de fogo anda pelo mapa. Nv5: 2 ciclones.", Color.ORANGE),
    MARCA_DA_MORTE(ClasseHeroi.DEMONIO, 0, Elemento.SOMBRA, false, "Marca da Morte", "Marca o mais forte: +50% de dano nele. Nv5: marca 3.", Color.VIOLET),
    EXPLOSAO_DE_CADAVER(ClasseHeroi.DEMONIO, 2, Elemento.SOMBRA, false, "Explosao de Cadaver", "Monstros mortos explodem. Nv5: explosao 2x.", Color.FIREBRICK),
    ASAS_INFERNAIS(ClasseHeroi.DEMONIO, 3, Elemento.FOGO, false, "Asas Infernais", "Escapa de quem encosta, invulneravel, rastro de fogo. Nv5: 2x mais.", Color.SCARLET),
    MEDO(ClasseHeroi.DEMONIO, 1, Elemento.SOMBRA, false, "Medo", "Monstros perto fogem por 1,5s. Nv5: 3s.", Color.PURPLE),
    ESCUDO_DE_OSSOS(ClasseHeroi.DEMONIO, 3, Elemento.SOMBRA, false, "Escudo de Ossos", "Ossos giram e bloqueiam tiros e golpes. Nv5: 6 ossos.", Color.WHITE),
    DIABRETES(ClasseHeroi.DEMONIO, 4, Elemento.FOGO, false, "Invocar Diabretes", "2 diabretes lutam por 10s. Nv5: 4.", Color.SCARLET),

    // ======================================================== ANJO (raio, vento, luz, sagrado)
    RAIO(ClasseHeroi.ANJO, 0, Elemento.ELETRICO, false, "Raio", "Acerta e ricocheteia em outros. Nv5: 6 alvos.", Color.YELLOW),
    VENTO_ANJO(ClasseHeroi.ANJO, 2, Elemento.VENTO, false, "Vento", "Laminas de vento giram em volta (2 a 5). Nv5: 2x mais rapidas.", Color.TEAL),
    LUZ(ClasseHeroi.ANJO, 1, Elemento.LUZ, false, "Luz", "Feixe que atravessa e atordoa. Nv5: feixe duplo.", Color.WHITE),
    TEMPESTADE(ClasseHeroi.ANJO, 2, Elemento.ELETRICO, false, "Tempestade", "Raios caem perto de voce. Nv5: 2x mais raios.", Color.YELLOW),
    LANCA_SAGRADA(ClasseHeroi.ANJO, 0, Elemento.LUZ, false, "Lanca Sagrada", "Lanca atravessa em linha longa. Nv5: volta ate voce.", Color.GOLD),
    HALO(ClasseHeroi.ANJO, 3, Elemento.LUZ, false, "Halo", "Anel empurra e fere quem chega perto. Nv5: maior.", Color.GOLD),
    BENCAO(ClasseHeroi.ANJO, 4, Elemento.LUZ, false, "Bencao", "Cura 2% a cada 3s e +10% velocidade. Nv5: 4%.", Color.GREEN),
    PRISMA(ClasseHeroi.ANJO, 0, Elemento.LUZ, false, "Prisma", "Feixe que se divide em 3. Nv5: 5.", Color.SKY),
    FURACAO(ClasseHeroi.ANJO, 1, Elemento.VENTO, false, "Furacao Celestial", "Tornado puxa e atordoa. Nv5: 2 furacoes.", Color.TEAL),
    ESPADAS_DE_LUZ(ClasseHeroi.ANJO, 0, Elemento.LUZ, false, "Espadas de Luz", "3 espadas voam ate alvos diferentes. Nv5: 6.", Color.GOLD),
    SANTUARIO(ClasseHeroi.ANJO, 3, Elemento.LUZ, false, "Santuario", "Area onde voce recebe -40% de dano. Nv5: tambem cura.", Color.GOLD),
    JUIZO(ClasseHeroi.ANJO, 0, Elemento.LUZ, false, "Juizo", "Mata monstros com menos de 15% de vida. Nv5: 25%.", Color.WHITE),
    PENAS(ClasseHeroi.ANJO, 2, Elemento.VENTO, false, "Penas Afiadas", "Rajada de penas em leque. Nv5: em todas as direcoes.", Color.WHITE),
    CORRENTE_ELETRICA(ClasseHeroi.ANJO, 1, Elemento.ELETRICO, false, "Corrente Eletrica", "Raio salta e atordoa varios. Nv5: salta mais.", Color.YELLOW),
    GUARDIAO(ClasseHeroi.ANJO, 4, Elemento.LUZ, false, "Guardiao", "Bloqueia 1 golpe a cada 8s. Nv5: a cada 4s.", Color.GOLD),
    PURIFICACAO(ClasseHeroi.ANJO, 2, Elemento.LUZ, false, "Purificacao", "Onda com +50% de dano em monstros com efeito. Nv5: +100%.", Color.WHITE),
    AURA_DE_LUZ(ClasseHeroi.ANJO, 3, Elemento.LUZ, false, "Aura de Luz", "Monstros perto ficam 20% mais lentos. Nv5: 40%.", Color.GOLD),
    CEU_ABERTO(ClasseHeroi.ANJO, 2, Elemento.LUZ, false, "Ceu Aberto", "Coluna de luz onde ha mais monstros. Nv5: 3 colunas.", Color.GOLD),
    ECO_TROVEJANTE(ClasseHeroi.ANJO, 0, Elemento.ELETRICO, false, "Eco Trovejante", "A cada 5 ataques basicos, um trovao. Nv5: a cada 3.", Color.YELLOW),
    ASAS_VELOZES(ClasseHeroi.ANJO, 4, Elemento.VENTO, false, "Asas Velozes", "+25% velocidade e rastro que fere. Nv5: rastro deixa lento.", Color.TEAL);

    public final ClasseHeroi classe;
    /** 0 = Dano, 1 = Controle, 2 = Area, 3 = Defesa, 4 = Suporte. */
    public final int papel;
    public final Elemento elemento;
    /** Usa o elemento da arma do Humano (Fogo, Gelo ou Laser). */
    public final boolean tiro;
    public final String nome;
    public final String descricao;
    public final Color cor;

    TipoPoder(ClasseHeroi classe, int papel, Elemento elemento, boolean tiro, String nome, String descricao, Color cor) {
        this.classe = classe;
        this.papel = papel;
        this.elemento = elemento;
        this.tiro = tiro;
        this.nome = nome;
        this.descricao = descricao;
        this.cor = cor;
    }

    public Poder criar() {
        return FabricaPoderes.criar(this);
    }

    public static final String[] NOMES_PAPEL = { "Dano", "Controle", "Area", "Defesa", "Suporte" };

    /** Poder da classe destino com o mesmo papel, que ainda nao foi usado. Se nao houver, qualquer um livre. */
    public static TipoPoder equivalente(TipoPoder origem, ClasseHeroi destino, Array<TipoPoder> usados) {
        for (TipoPoder t : values()) {
            if (t.classe == destino && t.papel == origem.papel && !usados.contains(t, true)) return t;
        }
        for (TipoPoder t : values()) {
            if (t.classe == destino && !usados.contains(t, true)) return t;
        }
        return null;
    }

    public static Array<TipoPoder> daClasse(ClasseHeroi classe) {
        Array<TipoPoder> lista = new Array<>();
        for (TipoPoder t : values()) {
            if (t.classe == classe) lista.add(t);
        }
        return lista;
    }
}
