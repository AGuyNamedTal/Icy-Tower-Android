package com.talv.icytower.game.player;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.SoundPool;
import android.util.Log;

import com.talv.icytower.R;
import com.talv.icytower.game.Debug;
import com.talv.icytower.game.Engine;
import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.PlayerPlatformsIntersection;
import com.talv.icytower.RectHelper;

import java.util.HashMap;
import java.util.LinkedList;

import static com.talv.icytower.game.Engine.PLAYER_SIZE_MULTIPLE;
import static com.talv.icytower.gui.GUI.CONTROLS.*;

public abstract class Player {

    public enum Direction {
        LEFT,
        RIGHT
    }

    public enum PlayerState {
        STANDING,
        SIDE_STAND,
        MOVING,
        JUMPING,
        JUMP_MOVE,
        STARRING
    }

    private int score;

    public void updateScore(int newScore, GameCanvas canvas) {
        this.score = newScore;
        canvas.updateText(SCORE_TXT, "Score: " + newScore);
    }

    public int getScore() {
        return score;
    }

    public float playerSizeMultiple;

    protected int stateUpdateTime;

    private static final int TIME_FROM_SIDE_STAND_TO_STANDING = 400;


    public static final float ACCELERATION_SPEED = PLAYER_SIZE_MULTIPLE * 0.066f / 1.5f;

    public static final float DECELERATION_SPEED = ACCELERATION_SPEED * 2f;

    public static final float MAX_SPEED = PLAYER_SIZE_MULTIPLE * 33.333f / 1.5f;
    public static final float MIN_SPEED = MAX_SPEED / 10f;
    public static final float REACTION_FORCE_MULTIPLE = 0.8f;

    //public static float JUMP_HEIGHT = 125f;
    public static final float VERTICAL_DECELERATION = DECELERATION_SPEED * 0.8f;
    public static final float MAX_FALL_SPEED = MIN_SPEED * 0.3f;
    public static final float MIN_JUMP_SPEED = -MAX_FALL_SPEED * 0.93f;
    public static final float HORIZONTAL_TO_VERTICAL_JUMP_MULTIPLE = 0.053f;

    public static float currentVerticalSpeed;


    private float currentSpeed;

    private float externalSpeed;


    public Direction currentDirection;
    public PlayerState currentState;
    public Rect rect;


    protected HashMap<PlayerState, BitmapAnimation> animations;

    private PlayerControls playerControls;

    private int jumpSoundID;

    public int totalJumps;
    public long totalTime;

    public Player(SoundPool soundPool, Context context, Resources resources, float playerSizeMultiple) {
        this.playerSizeMultiple = playerSizeMultiple;
        initializeAnimations(resources);
        initializeSounds(soundPool, context);
        updateStateAndAnimation(PlayerState.STANDING, 0);
        playerControls = new PlayerControls();
        resetPlayer();

    }

    private void initializeSounds(SoundPool soundPool, Context context) {
        jumpSoundID = soundPool.load(context, R.raw.jump_sound, 1);
    }


    abstract void initializeAnimations(Resources resources);

    abstract void updateStateAndAnimation(PlayerState newState, int msPassed);


    abstract Bitmap getCurrentImage();


    public void render(Canvas canvas, Engine engine) {
        canvas.drawBitmap(getCurrentImage(), rect.left, rect.top - engine.cameraY, Engine.gamePaint);

    }

    public void updatePlayer(int msPassed, Engine engine) {
        totalTime += msPassed;
        int activeControls = engine.gameCanvas.getActiveControls() & PLAYER_MOVEMENT_CONTROLS;
        if (checkActive(activeControls, ARROW_LEFT) &&
                checkActive(activeControls, ARROW_RIGHT)) {
            activeControls &= ~ARROW_LEFT;
            activeControls &= ~ARROW_RIGHT;
        }
        playerControls.passParameters(msPassed, engine);
        playerControls.controlFunctions[activeControls].run();

        int newX = (int) (rect.left + currentSpeed + externalSpeed);
        // decelerate external speed
        if (Math.abs(externalSpeed) > 0) {
            if (externalSpeed > 0) {
                externalSpeed = Math.max(externalSpeed - DECELERATION_SPEED * msPassed, 0);
            } else {
                externalSpeed = Math.min(externalSpeed + DECELERATION_SPEED * msPassed, 0);
            }
        }

        // handle wall collision
        if (newX < 0) {
            newX = 0;
            currentDirection = Direction.RIGHT;
            externalSpeed = currentSpeed * -REACTION_FORCE_MULTIPLE;
            currentSpeed = 0;
        } else if (newX > engine.cameraWidth - rect.width()) {
            newX = engine.cameraWidth - rect.width();
            currentDirection = Direction.LEFT;
            externalSpeed = currentSpeed * -REACTION_FORCE_MULTIPLE;
            currentSpeed = 0;
        }

        // handle direction change
        if (rect.left > newX) {
            currentDirection = Direction.LEFT;
        } else {
            currentDirection = Direction.RIGHT;
        }

        if (currentVerticalSpeed >= 0 /*not jumping*/ && !isOnPlatform(engine.platforms)) {
            currentVerticalSpeed = Math.min(MAX_FALL_SPEED, currentVerticalSpeed + VERTICAL_DECELERATION);
        }

        // handle vertical speed (jump/fall)
        if (currentVerticalSpeed != 0) {
            jumpTick(engine, msPassed);
        }
        // handle gravity

        // handle animation
        PlayerState newState = null;
        if (newX != rect.left) {
            if (currentVerticalSpeed == 0) {
                if (Math.abs(currentSpeed) < MAX_SPEED / 10f) {
                    newState = PlayerState.SIDE_STAND;
                } else {
                    newState = PlayerState.MOVING;
                }
            } else {
                newState = PlayerState.JUMP_MOVE;
            }
        }


        if (newState != null)
            updateStateAndAnimation(newState, msPassed);
        RectHelper.setRectX(rect, newX);


        //update camera:


        // handle gravity
        //check if should fall

        /*
        boolean shouldFall = !isPlatformBelow(engine.platforms) && !(isJumping());
        if (!shouldFall) {
            System.out.println("h");
        }
        if (Debug.LOG_JUMP)
            Log.d("jump", "Should Fall: " + shouldFall);
        if (shouldFall) {
            int newY = (int) (rect.top + MAX_FALL_SPEED * msPassed);
            fall(newY, engine.platforms, msPassed);
        } else {
            if (isFalling()) {
                stopFalling(msPassed, rect.top);
            }
            if (isJumping()) {
                jumpTick(engine.platforms, msPassed);
            }
        }
        */
    }

    public void resetPlayer() {
        updateStateAndAnimation(PlayerState.STANDING, 0);
        currentSpeed = 0;
        externalSpeed = 0;
        currentVerticalSpeed = 0;
        stateUpdateTime = 0;
        score = 0;
        totalJumps = 0;
        totalTime = 0;
        currentDirection = Direction.RIGHT;
        currentState = PlayerState.STANDING;
    }

    private boolean isJumping() {
        return (currentState == PlayerState.JUMPING || currentState == PlayerState.JUMP_MOVE) && currentVerticalSpeed < 0;
    }

    private boolean isFalling() {
        return (currentState == PlayerState.JUMPING || currentState == PlayerState.JUMP_MOVE) && currentVerticalSpeed > 0;
    }

    private boolean isPlatformBelow(LinkedList<Platform> platforms) {
        for (Platform platform :
                platforms) {
            if (RectHelper.isRectBelowRect(platform.rect, rect)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnPlatform(LinkedList<Platform> platforms) {
        for (Platform platform :
                platforms) {
            if (RectHelper.isRectOnRect(rect, platform.rect)) {
                return true;
            }
        }
        return false;
    }


    private void jumpTick(Engine engine, int msPassed) {

        float toMove = currentVerticalSpeed * msPassed;
        // decelerate
        if (currentVerticalSpeed <= 0) {
            // jumping up
            currentVerticalSpeed += VERTICAL_DECELERATION;
        }
        int newY = (int) (rect.top + toMove);
        if (toMove > 0) {
            // falling
            int yAfterIntersection = newY;
            Platform platformWhichFellOn = null;
            for (Platform plat : engine.platforms) {
                PlayerPlatformsIntersection intersection =
                        RectHelper.doesPlatformIntersectWithMovementY(rect, yAfterIntersection, plat.rect);
                if (intersection.didIntersect) {
                    yAfterIntersection = Math.min(yAfterIntersection, intersection.newY);
                    platformWhichFellOn = plat;
                }
            }
            if (platformWhichFellOn != null /*intersected with platform*/) {
                updateStateAndAnimation(PlayerState.STANDING, msPassed);
                currentVerticalSpeed = 0;
                newY = yAfterIntersection;
                platformWhichFellOn.onPlayerFall();
                //onFallOnPlatform(platformWhichFellOn, engine);
                updateScore(platformWhichFellOn.platformNumber * 10, engine.gameCanvas);
            }
        } else {
            // jumping up
            // update camera
        }
        RectHelper.setRectY(rect, newY);
        if (currentSpeed == 0 && System.currentTimeMillis() - stateUpdateTime >= TIME_FROM_SIDE_STAND_TO_STANDING) {
            updateStateAndAnimation(PlayerState.JUMPING, msPassed);
        } else {
            updateStateAndAnimation(PlayerState.JUMP_MOVE, msPassed);
        }

    }


    private float calculateJumpingSpeed() {
        return Math.min(-Math.abs(currentSpeed + externalSpeed) * HORIZONTAL_TO_VERTICAL_JUMP_MULTIPLE, MIN_JUMP_SPEED);
    }


    private class PlayerControls {
        public Runnable[] controlFunctions = new Runnable[PLAYER_MOVEMENT_CONTROLS + 1];
        private int msPassed;
        private Engine engine;

        public void passParameters(int msPassed, Engine engine) {
            this.msPassed = msPassed;
            this.engine = engine;
        }

        public PlayerControls() {
            controlFunctions[0] = new Runnable() {
                @Override
                public void run() {
                    stand();
                }
            };
            controlFunctions[ARROW_LEFT] = new Runnable() {
                @Override
                public void run() {
                    moveLeft();
                }
            };
            controlFunctions[ARROW_RIGHT] = new Runnable() {
                @Override
                public void run() {
                    moveRight();
                }
            };
            controlFunctions[ARROW_UP] = new Runnable() {
                @Override
                public void run() {
                    jump(true);
                }
            };
            controlFunctions[ARROW_LEFT | ARROW_UP] = new Runnable() {
                @Override
                public void run() {
                    moveJumpLeft();
                }
            };
            controlFunctions[ARROW_RIGHT | ARROW_UP] = new Runnable() {
                @Override
                public void run() {
                    moveJumpRight();
                }
            };
        }

        private void moveLeft() {
            if (Debug.LOG_PLAYER)
                Log.d("player", "MOVING LEFT");
            if (currentSpeed > 0) {
                currentSpeed = -MIN_SPEED;
            }
            currentSpeed -= ACCELERATION_SPEED * msPassed;
            currentSpeed = Math.max(currentSpeed, -MAX_SPEED);
            if (Debug.LOG_SPEED)
                Log.d("speed", String.valueOf(currentSpeed));
        }

        private void moveRight() {
            if (Debug.LOG_PLAYER)
                Log.d("player", "MOVING RIGHT");
            if (currentSpeed < 0) {
                currentSpeed = MIN_SPEED;
            }
            currentSpeed += ACCELERATION_SPEED * msPassed;
            currentSpeed = Math.min(currentSpeed, MAX_SPEED);
            if (Debug.LOG_SPEED)
                Log.d("speed", String.valueOf(currentSpeed));
        }


        private void jump(boolean decelerate) {
            if (Debug.LOG_PLAYER)
                Log.d("player", "JUMPING");
            if (decelerate)
                decelerateSpeed();
            boolean cantJump = isJumping() || isFalling();
            if (Debug.LOG_JUMP)
                Log.d("jump", "Can't Jump: " + cantJump);
            if (cantJump) return;
            engine.playSound(jumpSoundID, 0.8f);
            totalJumps++;
            updateStateAndAnimation(PlayerState.JUMPING, msPassed);
            currentVerticalSpeed = calculateJumpingSpeed();
        }

        private void moveJumpLeft() {
            if (Debug.LOG_PLAYER) Log.d("player", "JUMP MOVE LEFT");
            //JUMP:
            jump(false);

            //MOVE RIGHT:
            moveLeft();
        }

        private void moveJumpRight() {
            if (Debug.LOG_PLAYER) Log.d("player", "JUMP MOVE RIGHT");

            //JUMP:
            jump(false);

            //MOVE RIGHT:
            moveRight();
        }

        private void stand() {
            if (Debug.LOG_PLAYER)
                Log.d("player", "STANDING");

            decelerateSpeed();

            if (currentVerticalSpeed == 0) {
                if (System.currentTimeMillis() - stateUpdateTime >= TIME_FROM_SIDE_STAND_TO_STANDING) {
                    updateStateAndAnimation(PlayerState.STANDING, msPassed);
                }
            }
        }

        private void decelerateSpeed() {
            if (currentSpeed > 0) {
                currentSpeed = Math.max(currentSpeed - DECELERATION_SPEED * msPassed, 0);
            } else {
                currentSpeed = Math.min(currentSpeed + DECELERATION_SPEED * msPassed, 0);
            }
        }


    }


}
