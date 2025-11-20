package org.example;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class KeyboardListener implements KeyListener {

    private final Set<Integer> keysToListenActivly = Set.of(
            KeyEvent.VK_W,
            KeyEvent.VK_S,
            KeyEvent.VK_UP,
            KeyEvent.VK_DOWN
    );

    private Set<Integer> currentlyPressedKeys = new HashSet<>();

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }


}
