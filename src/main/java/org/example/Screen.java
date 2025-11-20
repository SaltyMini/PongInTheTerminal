package org.example;

public class Screen {

    private boolean shoundRender = false;

    private final int X_BOUND = 512;
    private final int Y_BOUND = 128;

    private final String[] debugMessages = new String[3];

    private Character[][] renderScreen = new Character[Y_BOUND][X_BOUND];
    private final Object renderLock = new Object();

    private static Screen instance;

    public static Screen getInstance() {
        if (instance == null) {
            instance = new Screen();
        }
        return instance;
    }

    public Screen() {
        // Enable ANSI escape sequences on Windows
        try {
            new ProcessBuilder("cmd", "/c", "echo off").inheritIO().start().waitFor();
            System.out.print("\033[?25l"); // Hide cursor
        } catch (Exception e) {
            // Ignore if not Windows or command fails
        }

        debugMessages[0] = "";
        debugMessages[1] = "";
        debugMessages[2] = "";


        for (int y = 0; y < Y_BOUND; y++) {
            for (int x = 0; x < X_BOUND; x++) {
                renderScreen[y][x] = ' ';
            }
        }

        //fill bounds
        for(int y = 0; y < Y_BOUND; y++) {
            renderScreen[y][0] = '#';
            renderScreen[y][X_BOUND - 1] = '#';
        }
        for(int x = 0; x < X_BOUND; x++) {
            renderScreen[0][x] = '#';
            renderScreen[Y_BOUND - 1][x] = '#';
        }

        shoundRender = true;
    }

    public void startRendering() {
        Renderer renderer = new Renderer();
        renderer.render();
    }

    public void setBall(int x, int y) {
        synchronized (renderLock) {
            int ballSizeX = Assets.getBallSizeX();
            int ballSizeY = Assets.getBallSizeY();
            for (int row = 0; row < ballSizeX; row++) {
                for (int col = 0; col < ballSizeY; col++) {
                    renderScreen[y + row][x + col] = '#';
                }
            }
        }
    }

    public void clearScreen() {
        synchronized (renderLock) {
            // Clear the inner area (not the borders)
            for (int y = 1; y < Y_BOUND - Assets.getBallSizeY(); y++) {
                for (int x = 1; x < X_BOUND - Assets.getBallSizeX(); x++) {
                    renderScreen[y][x] = ' ';
                }
            }
        }
    }

    public int getXBound() {
        return X_BOUND;
    }

    public int getYBound() {
        return Y_BOUND;
    }

    public void addDebugMessage(String message) {
        synchronized (renderLock) {
            if (debugMessages.length == 3) {
                debugMessages[0] = debugMessages[1];
                debugMessages[1] = debugMessages[2];
                debugMessages[2] = message;
            } else {
                debugMessages[debugMessages.length - 1] = message;
            }
        }
    }



    /// renderer
    ///
    ///
    private class Renderer {

        private final long TARGET_FPS = 30;
        private final long TARGET_TIME = 1000000000L / TARGET_FPS; // nanoseconds per frame

        public void render() {
            renderLoop();
        }

        private void renderLoop() {
            long lastTime = System.nanoTime();
            int count = 0;

            while (shoundRender) {
                count++;
                addDebugMessage( "Render loop count: " + count);
                long currentTime = System.nanoTime();
                long deltaTime = currentTime - lastTime;

                if (deltaTime >= TARGET_TIME) {
                    // Perform rendering here
                    renderFrame();
                    lastTime = currentTime;
                } else {
                    // Sleep for a short time to avoid busy waiting
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        private void renderFrame() {
            // Move cursor to top-left without clearing
            System.out.print("\033[H");
            
            // Build entire frame in memory
            StringBuilder frameBuffer = new StringBuilder();
            
            synchronized (renderLock) {
                for (int y = 0; y < Y_BOUND; y++) {
                    for (int x = 0; x < X_BOUND; x++) {
                        frameBuffer.append(renderScreen[y][x]);
                    }
                    if (y < Y_BOUND - 1) {
                        frameBuffer.append('\n');
                    }
                }
            }
            frameBuffer.append('\n');
            frameBuffer.append(debugMessages[0]).append('\n');
            frameBuffer.append(debugMessages[1]).append('\n');
            frameBuffer.append(debugMessages[2]).append('\n');

            frameBuffer.append('\n');

            System.out.print(frameBuffer);
            System.out.flush();
        }
    }
}
