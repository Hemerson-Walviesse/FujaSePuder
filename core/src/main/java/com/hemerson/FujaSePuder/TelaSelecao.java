package com.hemerson.FujaSePuder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.hemerson.FujaSePuder.entidades.ClasseHeroi;

/** Tela de escolha de classe: clique/toque no card ou aperte 1, 2 ou 3. */
public class TelaSelecao extends ScreenAdapter {

    private static final float LARGURA = 800f;
    private static final float ALTURA = 480f;
    private static final float CARD_LARGURA = 230f;
    private static final float CARD_ALTURA = 395f;
    private static final float RETRATO = 100f;   // tamanho da imagem do heroi no card

    private final FujaSePuder jogo;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(LARGURA, ALTURA, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fonte = new BitmapFont();
    private final Vector3 toque = new Vector3();

    private final ClasseHeroi[] classes = ClasseHeroi.values();
    private final Array<Rectangle> cards = new Array<>();
    private final Texture[] retratos = new Texture[classes.length];
    private float tempo;

    public TelaSelecao(FujaSePuder jogo) {
        this.jogo = jogo;
        montarCards(classes.length);
        for (int i = 0; i < classes.length; i++) retratos[i] = carregarRetrato(classes[i]);
    }

    private void montarCards(int quantidade) {
        cards.clear();
        float espaco = (LARGURA - quantidade * CARD_LARGURA) / (quantidade + 1);
        for (int i = 0; i < quantidade; i++) {
            cards.add(new Rectangle(espaco + i * (CARD_LARGURA + espaco), 45, CARD_LARGURA, CARD_ALTURA));
        }
    }

    @Override
    public void render(float delta) {
        int escolha = lerEscolha();
        if (escolha >= 0 && escolha < classes.length) {
            jogo.setScreen(new TelaJogo(jogo, classes[escolha]));
            dispose();
            return;
        }
        desenhar();
    }

    private int lerEscolha() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) return 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) return 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) return 2;
        if (Gdx.input.justTouched()) {
            toque.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(toque);
            for (int i = 0; i < cards.size; i++) {
                if (cards.get(i).contains(toque.x, toque.y)) return i;
            }
        }
        return -1;
    }

    // ---------------------------------------------------------------- desenho

    private void desenhar() {
        ScreenUtils.clear(0.08f, 0.08f, 0.10f, 1f);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < cards.size; i++) {
            Rectangle c = cards.get(i);
            Color cor = classes[i].cor;
            shapes.setColor(0.16f, 0.16f, 0.20f, 1f);
            shapes.rect(c.x, c.y, c.width, c.height);
            shapes.setColor(cor);
            shapes.rect(c.x, c.y + c.height - 36, c.width, 36);
            if (retratos[i] == null) shapes.rect(c.x + c.width / 2f - 20, c.y + c.height - 110, 40, 40);
        }
        shapes.end();

        batch.begin();
        desenharRetratos();
        fonte.setColor(Color.WHITE);
        fonte.draw(batch, "ESCOLHA SUA CLASSE", 0, ALTURA - 20, LARGURA, Align.center, false);
        fonte.draw(batch, "Clique no card ou aperte 1, 2 ou 3", 0, 30, LARGURA, Align.center, false);
        desenharTextosClasses();
        batch.end();
    }

    private static Texture carregarRetrato(ClasseHeroi classe) {
        String nome;
        switch (classe) {
            case HUMANO:  nome = "sprites/heroi_humano.png"; break;
            case DEMONIO: nome = "sprites/heroi_demonio.png"; break;
            case ANJO:    nome = "sprites/heroi_anjo.png"; break;
            default:      return null;
        }
        if (!Gdx.files.internal(nome).exists()) return null;
        Texture t = new Texture(Gdx.files.internal(nome), true);
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        return t;
    }

    /** Imagem de cada heroi no card, respirando devagar; o card embaixo do mouse "pula" um pouco. */
    private void desenharRetratos() {
        tempo += Gdx.graphics.getDeltaTime();
        toque.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(toque);
        for (int i = 0; i < classes.length; i++) {
            Texture t = retratos[i];
            if (t == null) continue;
            Rectangle c = cards.get(i);
            boolean emCima = c.contains(toque.x, toque.y);
            float respira = (float) Math.sin(tempo * 2.2f + i) * 0.02f;
            float lado = RETRATO * (emCima ? 1.1f : 1f);
            float h = lado * (1f + respira);
            float x = c.x + c.width / 2f - lado / 2f;
            float y = c.y + c.height - 40f - RETRATO;
            batch.draw(t, x, y, lado, h);
        }
    }

    private void desenharTextosClasses() {
        for (int i = 0; i < classes.length; i++) {
            Rectangle c = cards.get(i);
            ClasseHeroi classe = classes[i];
            float x = c.x + 10;
            float largura = c.width - 20;

            fonte.setColor(Color.BLACK);
            fonte.draw(batch, (i + 1) + ". " + classe.nome.toUpperCase(), c.x, c.y + c.height - 12, c.width, Align.center, false);

            float y = c.y + c.height - 150;
            fonte.setColor(Color.WHITE);
            fonte.draw(batch, "Vida: " + (int) classe.vidaMaxima, x, y);          y -= 18;
            fonte.draw(batch, "Velocidade: " + (int) classe.velocidade, x, y);    y -= 18;
            fonte.draw(batch, "Dano: " + (int) classe.dano + "   Alcance: " + (int) classe.alcanceAtaque, x, y); y -= 18;
            fonte.draw(batch, String.format("Ataques/s: %.1f", 1f / classe.intervaloAtaque), x, y); y -= 26;

            fonte.setColor(Color.LIGHT_GRAY);
            fonte.draw(batch, "Passiva: " + classe.passiva, x, y, largura, Align.left, true); y -= 50;

            fonte.setColor(classe.cor);
            fonte.draw(batch, classe.nomeHabilidade + " (" + (int) classe.recargaHabilidade + "s)", x, y); y -= 18;
            fonte.setColor(Color.LIGHT_GRAY);
            fonte.draw(batch, classe.descricaoHabilidade, x, y, largura, Align.left, true);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        for (Texture t : retratos) if (t != null) t.dispose();
        fonte.dispose();
    }
}
