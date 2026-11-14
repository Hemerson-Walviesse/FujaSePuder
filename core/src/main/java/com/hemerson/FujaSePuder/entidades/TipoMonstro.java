package com.hemerson.FujaSePuder.entidades;

/** Tipos de monstro. Cada fase acrescenta tipos novos (ver GerenciadorDeHordas). */
public enum TipoMonstro {
    COMUM, CORREDOR, TANQUE,
    ATIRADOR,     // fase 2+: fica longe e atira
    FANTASMA,     // fase 3+: fica intangivel por instantes (so Luz e Raio/Laser acertam)
    BLINDADO,     // fase 4+: escudo que bloqueia 3 golpes e se regenera
    EXPLOSIVO,    // fase 5: explode ao morrer
    CURANDEIRO,   // fase 5: cura os monstros em volta
    LACAIO,       // invocado por bosses
    PORTADOR_CHAVE,   // fases 2, 4, 5: foge do heroi; ao morrer solta a chave do altar
    PORTADOR_PORTAL,  // fase 5: foge do heroi; ao morrer solta a chave do portal
    GUARDIAO_SELO,    // fase 5: precisa morrer DENTRO do altar para abri-lo
    BOSS
}
