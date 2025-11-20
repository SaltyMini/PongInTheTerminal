package org.example;

public class Main {



    public static void main(String[] args) {
        System.err.println("=== MAIN STARTING ===");
        System.out.print("\033[H\033[2J");
        System.out.flush();

        Screen screen = Screen.getInstance();
        System.err.println("Screen instance created");

        Game game = Game.getInstance();
        System.err.println("Game instance created");

        // Start the game loop in a separate thread
        Thread gameThread = new Thread(() -> {
            System.err.println("Game thread started!");
            game.start();
        });
        gameThread.start();
        System.err.println("Game thread launched");

        // Start rendering in the main thread
        System.err.println("Starting renderer...");
        screen.startRendering();
    }
}