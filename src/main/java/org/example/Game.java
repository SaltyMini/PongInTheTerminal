package org.example;

import java.awt.event.KeyEvent;

public class Game {

    private boolean isRunning = false;
    private static Game instance;

    private KeyboardListener kbListener;

    private record ScreenCord(double x, double y) {}

    private final int MAX_PADDLE_SPEED = 10;

    private record Ball(ScreenCord cord, double velocityX, double velocityY) { }
    private record Paddle(ScreenCord origin, double width, double height, double velocityY) {}

    private Ball ball = new Ball(new ScreenCord(60.0, 60.0), 50.0, 50.0);
    private final int PADDLE_WALL_OFFSET = 12;
    private Paddle paddle1 = new Paddle(new ScreenCord(PADDLE_WALL_OFFSET, 3.0), 2.0, 7, 0);
    private Paddle paddle2 = new Paddle(new ScreenCord(Screen.getInstance().getXBound() - PADDLE_WALL_OFFSET, 3.0), 2.0, 7, 0);

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public Game() {
        kbListener = new KeyboardListener();

    }

    public void start() {
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

        moveBall(deltaTime);

        //update render
        Screen screen = Screen.getInstance();
        screen.clearScreen();
        screen.setBall((int) ball.cord.x, (int) ball.cord.y);  // Cast to int only when rendering

        //uodate paddles
        movePaddle(deltaTime, paddle1);
        movePaddle(deltaTime, paddle2);

        screen.setPaddle1((int) paddle1.origin.x, (int) paddle1.origin.y);
        screen.setPaddle2((int) paddle2.origin.x, (int) paddle2.origin.y);



    }

    private void moveBall(double deltaTime) {
        double newX = ball.cord.x + (ball.velocityX * deltaTime);
        double newY = ball.cord.y + (ball.velocityY * deltaTime);

        Screen screen = Screen.getInstance();
        double newVelocityX = ball.velocityX;
        double newVelocityY = ball.velocityY;

        int ballSizeX = Assets.getBallSizeX();
        int ballSizeY = Assets.getBallSizeY();

        // Check horizontal bounds with proper validation
        if (newX <= 1) {
            newX = 1;
            newVelocityX = Math.abs(ball.velocityX); 
        } else if (newX + ballSizeX >= screen.getXBound() - 1) {
            newX = Math.max(1, screen.getXBound() - 1 - ballSizeX);
            newVelocityX = -Math.abs(ball.velocityX); 
        }

        // Check vertical bounds with proper validation
        if (newY <= 1) {
            newY = 1;
            newVelocityY = Math.abs(ball.velocityY);
        } else if (newY + ballSizeY >= screen.getYBound() - 1) {
            newY = Math.max(1, screen.getYBound() - 1 - ballSizeY);
            newVelocityY = -Math.abs(ball.velocityY);
        }

        // Ensure coordinates are never negative or too large
        newX = Math.max(0, Math.min(newX, screen.getXBound() - ballSizeX));
        newY = Math.max(0, Math.min(newY, screen.getYBound() - ballSizeY));

        ScreenCord cordToo = new ScreenCord(newX, newY);
        ball = new Ball(cordToo, newVelocityX, newVelocityY);
    }


    private void movePaddle(double deltaTime, Paddle paddle) {
        double newVelocity = paddle.velocityY;
        int SCALER = 5;

        if(paddle == paddle2) {
            if(kbListener.getCurrentlyPressedKeys().contains(KeyEvent.VK_W)) {
                newVelocity = paddle.velocityY + SCALER;
            } else if(kbListener.getCurrentlyPressedKeys().contains(KeyEvent.VK_S)) {
                newVelocity = paddle.velocityY - SCALER;
            }
        } else {
            if(kbListener.getCurrentlyPressedKeys().contains(KeyEvent.VK_UP)) {
                newVelocity = paddle.velocityY + SCALER;
            } else if(kbListener.getCurrentlyPressedKeys().contains(KeyEvent.VK_DOWN)) {
                newVelocity = paddle.velocityY - SCALER;
            }
        }

        if(newVelocity > MAX_PADDLE_SPEED) {
            newVelocity = MAX_PADDLE_SPEED;
        } else if(newVelocity < -MAX_PADDLE_SPEED) {
            newVelocity = -MAX_PADDLE_SPEED;
        }

        double newY = paddle.origin.y + (newVelocity * deltaTime);
        
        // Add bounds checking for paddle movement
        Screen screen = Screen.getInstance();
        int paddleHeight = Assets.getPaddleY();
        
        // Ensure paddle stays within bounds
        if (newY < 1) {
            newY = 1;
            newVelocity = 0; // Stop movement when hitting boundary
        } else if (newY + paddleHeight >= screen.getYBound() - 1) {
            newY = screen.getYBound() - 1 - paddleHeight;
            newVelocity = 0; // Stop movement when hitting boundary
        }
        
        ScreenCord newOrigin = new ScreenCord(paddle.origin.x, newY);

        if (paddle == paddle1) {
            paddle1 = new Paddle(newOrigin, paddle.width, paddle.height, newVelocity);
        } else {
            paddle2 = new Paddle(newOrigin, paddle.width, paddle.height, newVelocity);
        }
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