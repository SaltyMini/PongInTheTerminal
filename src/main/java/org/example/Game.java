package org.example;

public class Game {

    private boolean isRunning = false;
    private static Game instance;

    private record ScreenCord(double x, double y) {
    }  // Changed to double

    private final int MAX_PADDLE_SPEED = 10;

    private record Ball(ScreenCord cord, double velocityX, double velocityY) { }
    private record Paddle(ScreenCord origin, double width, double height, double velocityY) {}

    private Ball ball = new Ball(new ScreenCord(10.0, 5.0), 15.0, 10.0);  // Start away from corner with moderate speed
    private final int PADDLE_WALL_OFFSET = 5;
    private Paddle paddle1 = new Paddle(new ScreenCord(PADDLE_WALL_OFFSET, 3.0), 2.0, 7, 0);
    private Paddle paddle2 = new Paddle(new ScreenCord(Screen.getInstance().getXBound() - PADDLE_WALL_OFFSET, 3.0), 2.0, 7, 0);

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public Game() {

    }

    public void start() {
        Screen.getInstance().addDebugMessage("start() called, isRunning = " + isRunning);
        if (!isRunning) {
            isRunning = true;
            Screen.getInstance().addDebugMessage("Starting game loop...");
            GameLoop gameLoop = new GameLoop();
            gameLoop.run(); // Run directly since Main already created a thread
        }
    }

    public void stop() {
        isRunning = false;
    }

    private void update(double deltaTime) {
        Screen.getInstance().addDebugMessage("Update called! deltaTime: " + deltaTime + " Ball at: " + ball.cord.x + ", " + ball.cord.y);

        moveBall(deltaTime);

        //update render
        Screen screen = Screen.getInstance();
        screen.clearScreen();
        screen.setBall((int) ball.cord.x, (int) ball.cord.y);  // Cast to int only when rendering

    }

    private void moveBall(double deltaTime) {

        //just guna make it bounce around to test
        double newX = ball.cord.x + (ball.velocityX * deltaTime);  // Keep as double
        double newY = ball.cord.y + (ball.velocityY * deltaTime);  // Keep as double

        //check bounds collision
        Screen screen = Screen.getInstance();
        double newVelocityX = ball.velocityX;
        double newVelocityY = ball.velocityY;

        // Check horizontal bounds (ball is 2 units wide, account for border walls)
        if (newX <= 1) {
            newX = 1;
            newVelocityX = Math.abs(ball.velocityX); // Make sure it bounces away from left wall
        } else if (newX + 2 >= screen.getXBound() - 1) {  // Changed from +1 to +2 for ball width
            newX = screen.getXBound() - 3;
            newVelocityX = -Math.abs(ball.velocityX); // Make sure it bounces away from right wall
        }

        // Check vertical bounds (ball is 2 units tall, account for border walls)
        if (newY <= 1) {
            newY = 1;
            newVelocityY = Math.abs(ball.velocityY); // Make sure it bounces away from top wall
        } else if (newY + 2 >= screen.getYBound() - 1) {  // Changed from +1 to +2 for ball height
            newY = screen.getYBound() - 3;
            newVelocityY = -Math.abs(ball.velocityY); // Make sure it bounces away from bottom wall
        }

        ScreenCord cordToo = new ScreenCord(newX, newY);
        ball = new Ball(cordToo, newVelocityX, newVelocityY);
    }

    private void movePaddle(double deltaTime, Paddle paddle) {

        double newY = paddle.origin.y + (paddle.velocityY * deltaTime);

        //check key presses


    }

    private class GameLoop {

        private final long TARGET_TPS = 120; // 120 ticks per second
        private final long TARGET_TIME = 1000000000L / TARGET_TPS; // nanoseconds per tick

        public void run() {
            Screen.getInstance().addDebugMessage("Game loop started!");
            long lastTime = System.nanoTime();
            long accumulator = 0;
            int tickCount = 0;

            while (isRunning) {
                long currentTime = System.nanoTime();
                long frameTime = currentTime - lastTime;
                lastTime = currentTime;

                accumulator += frameTime;

                while (accumulator >= TARGET_TIME) {
                    double deltaTime = TARGET_TIME / 1000000000.0; // Convert to seconds
                    update(deltaTime);
                    tickCount++;
                    if (tickCount % 120 == 0) { // Log once per second
                        Screen.getInstance().addDebugMessage("Ticks: " + tickCount);
                    }
                    accumulator -= TARGET_TIME;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            Screen.getInstance().addDebugMessage("Game loop ended!");
        }
    }
}
