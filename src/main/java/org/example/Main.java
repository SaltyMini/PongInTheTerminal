package org.example;

public class Main {



    public static void main(String[] args) {
        System.err.println("=== MAIN STARTING ===");
        System.out.print("\033[H\033[2J");
        System.out.flush();

        Screen screen = Screen.getInstance();

        Game game = Game.getInstance();

        // separate thread
        Thread gameThread = new Thread(() -> {
            game.start();
        });

        gameThread.start();
        screen.startRendering();
    }
}