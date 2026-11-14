package com.hemerson.FujaSePuder;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class FujaSePuder extends Game {
    @Override
    public void create() {
        setScreen(new TelaSelecao(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        super.dispose();
    }
}
