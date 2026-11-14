package com.hemerson.FujaSePuder.poderes;

import com.badlogic.gdx.graphics.Color;

/**
 * Cria cada poder com os seus numeros. Para balancear, mude os valores aqui.
 * (danoBase sobe +15% por nivel; intervalos caem 6% por nivel; Nv5 da o efeito extra.)
 */
public final class FabricaPoderes {

    private FabricaPoderes() { }

    public static Poder criar(TipoPoder t) {
        switch (t) {
            // ================================================== HUMANO
            case PISTOLA: {
                PoderProjetil p = proj(t, true);
                p.intervalo = 0.35f; p.danoBase = 15f; p.velocidade = 650f;
                p.rajadaNv5 = 2;
                return p;
            }
            case DUPLA: {
                PoderProjetil p = proj(t, true);
                p.intervalo = 0.6f; p.danoBase = 13f; p.velocidade = 600f;
                p.quantidade = 2; p.quantidadeNv5 = 4; p.espalhamento = 5f;
                return p;
            }
            case ESCOPETA: {
                PoderProjetil p = proj(t, true);
                p.intervalo = 1.1f; p.danoBase = 9f; p.velocidade = 600f; p.alcance = 230f; p.alcanceMira = 220f;
                p.quantidade = 6; p.quantidadeNv5 = 9; p.espalhamento = 8f; p.empurrar = 150f;
                return p;
            }
            case FUZIL: {
                PoderProjetil p = proj(t, true);
                p.intervalo = 1.6f; p.danoBase = 45f; p.velocidade = 1100f; p.alcance = 700f; p.alcanceMira = 600f;
                p.mira = PoderProjetil.Mira.MAIS_FORTE; p.perfuracoes = 2; p.multiplicadorNv5 = 2f; p.raio = 4f;
                return p;
            }
            case METRALHADORA: {
                PoderProjetil p = proj(t, true);
                p.intervalo = 2.2f; p.danoBase = 10f; p.velocidade = 700f;
                p.rajada = 8; p.rajadaNv5 = 14; p.intervaloRajada = 0.08f; p.tremor = 6f; p.raio = 4f;
                return p;
            }
            case GRANADA: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 2.5f; p.danoBase = 30f; p.velocidade = 320f; p.raio = 8f; p.alcanceMira = 380f;
                p.explodeNoFim = true; p.soExplode = true; p.explosao = 70f;
                p.quantidadeNv5 = 3; p.mira = PoderProjetil.Mira.MAIS_PROXIMO; p.espalhamento = 20f;
                return p;
            }
            case MINA: {
                PoderZonas z = zonas(t, PoderZonas.Modo.NO_HEROI);
                z.intervalo = 1.5f; z.gatilho = true; z.duracao = 25f; z.raio = 60f; z.danoBase = 35f;
                z.maxAtivas = 3; z.maxAtivasNv5 = 6;
                return z;
            }
            case TORRETA: {
                PoderInvocacao i = invoc(t, PoderInvocacao.Modo.FIXA);
                i.tiro = true; i.intervalo = 12f; i.duracao = 8f; i.intervaloAtaque = 0.4f;
                i.alcanceAtaque = 320f; i.danoBase = 10f; i.quantidadeNv5 = 2; i.tamanho = 11f;
                return i;
            }
            case DRONE: {
                PoderInvocacao i = invoc(t, PoderInvocacao.Modo.ORBITA);
                i.tiro = true; i.permanente = true; i.intervaloAtaque = 0.7f;
                i.alcanceAtaque = 300f; i.danoBase = 10f; i.quantidadeNv5 = 2; i.tamanho = 8f;
                return i;
            }
            case BUMERANGUE: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 1.4f; p.danoBase = 18f; p.velocidade = 420f; p.alcance = 300f; p.raio = 10f;
                p.perfuracoes = Projetil.ATRAVESSA_TUDO; p.volta = true;
                p.quantidadeNv5 = 2; p.espalhamento = 30f;
                return p;
            }
            case ARPAO: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 2.2f; p.danoBase = 14f; p.velocidade = 750f; p.alcance = 400f; p.alcanceMira = 380f;
                p.empurrar = -450f; p.atordoar = 1f; p.quantidadeNv5 = 3; p.espalhamento = 20f;
                return p;
            }
            case REDE: {
                PoderZonas z = zonas(t, PoderZonas.Modo.DENSO);
                z.intervalo = 4f; z.unica = true; z.raio = 80f; z.raioNv5 = 120f;
                z.atordoar = 2f; z.danoNv5Extra = 20f; z.atraso = 0.2f;
                return z;
            }
            case ESCUDO_ENERGIA:
            case KIT_MEDICO:
            case ADRENALINA:
            case SEDE_DE_SANGUE:
            case MARCA_DA_MORTE:
            case BENCAO:
            case JUIZO:
            case GUARDIAO:
            case ASAS_INFERNAIS:
            case ASAS_VELOZES:
            case ECO_TROVEJANTE:
            case EXPLOSAO_DE_CADAVER:
                return new PoderPassivo(t);
            case BOMBA_FUMACA: {
                PoderZonas z = zonas(t, PoderZonas.Modo.NO_HEROI);
                z.intervalo = 6f; z.duracao = 5f; z.raio = 120f; z.lentidao = 0.5f; z.intervaloTick = 0.3f;
                z.maxAtivas = 2; z.invencivelNv5 = true; z.alcanceAlvo = 200f;
                return z;
            }
            case LAMINA_GIRATORIA: {
                PoderOrbital o = new PoderOrbital(t);
                o.cor = t.cor; o.quantidade = 1; o.quantidadeNv5 = 2; o.raioOrbita = 55f;
                o.velocidadeAngular = 4.5f; o.tamanho = 14f; o.danoBase = 14f;
                return o;
            }
            case ATAQUE_AEREO: {
                PoderZonas z = zonas(t, PoderZonas.Modo.LINHA);
                z.intervalo = 7f; z.unica = true; z.quantidade = 6; z.raio = 45f; z.danoBase = 30f;
                z.atraso = 0.5f; z.atrasoPasso = 0.12f; z.espacamento = 70f; z.cruzNv5 = true; z.alcanceAlvo = 400f;
                return z;
            }
            case COQUETEL: {
                PoderZonas z = zonas(t, PoderZonas.Modo.MAIS_PROXIMO);
                z.intervalo = 3f; z.duracao = 4f; z.raio = 60f; z.raioNv5 = 120f; z.danoBase = 6f;
                z.intervaloTick = 0.5f; z.lentidao = 0.8f; z.usaElementoArma = true;
                return z;
            }
            case TASER: {
                PoderCorrente c = new PoderCorrente(t);
                c.cor = t.cor; c.intervalo = 1.5f; c.danoBase = 10f; c.saltos = 1; c.saltosNv5 = 3; c.atordoar = 1f;
                return c;
            }

            // ================================================== DEMONIO
            case FOGO:          return new PoderFogo();
            case AGUA:          return new PoderAgua();
            case VENTO_DEMONIO: return new PoderVentoDemonio();
            case CHUVA_ACIDA: {
                PoderZonas z = zonas(t, PoderZonas.Modo.PERSISTENTE);
                z.raio = 130f; z.intervaloTick = 0.5f; z.danoBase = 5f; z.vulneravelNv5 = 1f;
                return z;
            }
            case CORRENTE_INFERNAL: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 2.2f; p.danoBase = 16f; p.velocidade = 650f; p.alcance = 380f;
                p.empurrar = -400f; p.atordoar = 1.5f; p.quantidadeNv5 = 3; p.espalhamento = 20f; p.raio = 7f;
                return p;
            }
            case PILAR_DE_FOGO: {
                PoderZonas z = zonas(t, PoderZonas.Modo.ALEATORIOS);
                z.intervalo = 2.5f; z.unica = true; z.quantidade = 3; z.quantidadeNv5 = 6;
                z.atraso = 0.6f; z.raio = 40f; z.danoBase = 25f; z.queimaduraBase = 5f;
                return z;
            }
            case TSUNAMI: {
                PoderZonas z = zonas(t, PoderZonas.Modo.ONDA);
                z.intervalo = 6f; z.duracao = 2f; z.raio = 80f; z.velocidadeZona = 320f;
                z.intervaloTick = 0.25f; z.danoBase = 12f; z.puxar = -150f; z.cruzNv5 = true;
                return z;
            }
            case SOMBRA_VIVA: {
                PoderInvocacao i = invoc(t, PoderInvocacao.Modo.SEGUE);
                i.corpoACorpo = true; i.intervalo = 10f; i.duracao = 6f; i.intervaloAtaque = 0.5f;
                i.alcanceAtaque = 140f; i.danoBase = 12f; i.quantidadeNv5 = 2; i.tamanho = 13f;
                return i;
            }
            case GARRAS: {
                PoderCone c = new PoderCone(t);
                c.cor = t.cor; c.intervalo = 0.9f; c.danoBase = 30f; c.alcanceCone = 110f; c.angulo = 45f; c.anguloNv5 = 90f;
                return c;
            }
            case METEORO_INFERNAL: {
                PoderZonas z = zonas(t, PoderZonas.Modo.DENSO);
                z.intervalo = 4f; z.unica = true; z.atraso = 0.9f; z.raio = 90f; z.danoBase = 50f;
                z.queimaduraBase = 8f; z.quantidadeNv5 = 3; z.alcanceAlvo = 400f;
                return z;
            }
            case REDEMOINHO: {
                PoderZonas z = zonas(t, PoderZonas.Modo.DENSO);
                z.intervalo = 5f; z.duracao = 3f; z.raio = 140f; z.puxar = 180f;
                z.intervaloTick = 0.5f; z.danoNv5Extra = 6f;
                return z;
            }
            case LANCA_GELO_NEGRO: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 1.3f; p.danoBase = 28f; p.velocidade = 700f; p.alcance = 450f; p.raio = 7f;
                p.perfuracoes = Projetil.ATRAVESSA_TUDO; p.atordoar = 1f; p.atordoarNv5 = 2f;
                return p;
            }
            case CICLONE_BRASAS: {
                PoderZonas z = zonas(t, PoderZonas.Modo.VAGANTE);
                z.intervalo = 8f; z.duracao = 6f; z.raio = 60f; z.velocidadeZona = 120f;
                z.intervaloTick = 0.3f; z.danoBase = 7f; z.queimaduraBase = 4f; z.quantidadeNv5 = 2;
                return z;
            }
            case MEDO: {
                PoderPulso p = pulso(t);
                p.intervalo = 5f; p.raio = 150f; p.medo = 1.5f; p.medoNv5 = 3f;
                return p;
            }
            case ESCUDO_DE_OSSOS: {
                PoderOrbital o = new PoderOrbital(t);
                o.cor = t.cor; o.quantidade = 4; o.quantidadeNv5 = 6; o.raioOrbita = 50f;
                o.velocidadeAngular = 3f; o.tamanho = 9f; o.danoBase = 8f; o.bloqueiaTiros = true;
                return o;
            }
            case DIABRETES: {
                PoderInvocacao i = invoc(t, PoderInvocacao.Modo.PERSEGUE);
                i.corpoACorpo = true; i.intervalo = 14f; i.duracao = 10f; i.quantidade = 2; i.quantidadeNv5 = 4;
                i.intervaloAtaque = 0.6f; i.alcanceAtaque = 35f; i.danoBase = 10f; i.velocidadeMovimento = 180f; i.tamanho = 9f;
                return i;
            }

            // ================================================== ANJO
            case RAIO:       return new PoderRaio();
            case VENTO_ANJO: return new PoderVentoAnjo();
            case LUZ:        return new PoderLuz();
            case TEMPESTADE: {
                PoderZonas z = zonas(t, PoderZonas.Modo.ALEATORIOS);
                z.intervalo = 1.2f; z.unica = true; z.quantidade = 2; z.quantidadeNv5 = 4;
                z.atraso = 0.3f; z.raio = 35f; z.danoBase = 18f; z.atordoar = 0.3f; z.alcanceAlvo = 300f;
                return z;
            }
            case LANCA_SAGRADA: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 1.5f; p.danoBase = 35f; p.velocidade = 800f; p.alcance = 600f; p.alcanceMira = 450f;
                p.perfuracoes = Projetil.ATRAVESSA_TUDO; p.raio = 7f; p.voltaNv5 = true;
                return p;
            }
            case HALO: {
                PoderPulso p = pulso(t);
                p.intervalo = 0.5f; p.raio = 70f; p.raioNv5 = 100f; p.danoBase = 6f; p.empurrar = 250f;
                p.mostrarOnda = false; p.aura = true;
                return p;
            }
            case PRISMA: {
                PoderFeixe f = new PoderFeixe(t);
                f.cor = t.cor; f.intervalo = 1.3f; f.danoBase = 16f; f.comprimento = 380f; f.largura = 8f;
                f.quantidade = 3; f.quantidadeNv5 = 5; f.abertura = 20f;
                return f;
            }
            case FURACAO: {
                PoderZonas z = zonas(t, PoderZonas.Modo.DENSO);
                z.intervalo = 6f; z.duracao = 3f; z.raio = 90f; z.puxar = 220f; z.intervaloTick = 0.5f;
                z.danoBase = 5f; z.atordoar = 0.4f; z.quantidadeNv5 = 2;
                return z;
            }
            case ESPADAS_DE_LUZ: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 1.4f; p.danoBase = 18f; p.velocidade = 600f; p.raio = 6f;
                p.mira = PoderProjetil.Mira.MULTIPLOS; p.quantidade = 3; p.quantidadeNv5 = 6;
                return p;
            }
            case SANTUARIO: {
                PoderZonas z = zonas(t, PoderZonas.Modo.NO_HEROI);
                z.intervalo = 8f; z.duracao = 5f; z.raio = 110f; z.reducaoHeroi = 0.4f; z.curaHeroiNv5 = 0.01f;
                z.maxAtivas = 1; z.alcanceAlvo = 300f;
                return z;
            }
            case PENAS: {
                PoderProjetil p = proj(t, false);
                p.intervalo = 1.2f; p.danoBase = 9f; p.velocidade = 520f; p.alcance = 300f; p.alcanceMira = 280f;
                p.quantidade = 7; p.quantidadeNv5 = 16; p.espalhamento = 12f; p.todasDirecoesNv5 = true; p.raio = 5f;
                return p;
            }
            case CORRENTE_ELETRICA: {
                PoderCorrente c = new PoderCorrente(t);
                c.cor = t.cor; c.intervalo = 2f; c.danoBase = 12f; c.saltos = 3; c.saltosNv5 = 6;
                c.atordoar = 0.6f; c.distanciaSalto = 150f;
                return c;
            }
            case PURIFICACAO: {
                PoderPulso p = pulso(t);
                p.intervalo = 3f; p.raio = 200f; p.danoBase = 15f; p.bonusComEfeito = 0.5f; p.bonusComEfeitoNv5 = 1f;
                return p;
            }
            case AURA_DE_LUZ: {
                PoderPulso p = pulso(t);
                p.intervalo = 0.3f; p.raio = 120f; p.lentidao = 0.8f; p.lentidaoNv5 = 0.6f; p.lentidaoDuracao = 0.5f;
                p.mostrarOnda = false; p.aura = true;
                return p;
            }
            case CEU_ABERTO: {
                PoderZonas z = zonas(t, PoderZonas.Modo.DENSO);
                z.intervalo = 3f; z.unica = true; z.atraso = 0.7f; z.raio = 70f; z.danoBase = 40f;
                z.quantidadeNv5 = 3; z.alcanceAlvo = 400f;
                return z;
            }
            default:
                throw new IllegalStateException("Poder sem implementacao: " + t);
        }
    }

    private static PoderProjetil proj(TipoPoder t, boolean tiro) {
        PoderProjetil p = new PoderProjetil(t);
        p.tiro = tiro;
        p.cor = t.cor;
        return p;
    }

    private static PoderZonas zonas(TipoPoder t, PoderZonas.Modo modo) {
        PoderZonas z = new PoderZonas(t);
        z.modo = modo;
        z.cor = t.cor;
        return z;
    }

    private static PoderInvocacao invoc(TipoPoder t, PoderInvocacao.Modo modo) {
        PoderInvocacao i = new PoderInvocacao(t);
        i.modo = modo;
        i.cor = t.cor;
        return i;
    }

    private static PoderPulso pulso(TipoPoder t) {
        PoderPulso p = new PoderPulso(t);
        p.cor = t.cor;
        return p;
    }

    /** Usado so para evitar aviso de import nao usado em algumas IDEs. */
    static Color cor(TipoPoder t) { return t.cor; }
}
