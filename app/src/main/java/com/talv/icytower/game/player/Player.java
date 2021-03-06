package com.talv.icytower.game.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.SoundPool;

import com.talv.icytower.R;
import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.PlayerPlatformsIntersection;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.platform.DisappearingPlatform;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.utils.RectUtils;

import java.util.HashMap;
import java.util.LinkedList;

import static com.talv.icytower.game.engine.Engine.PLAYER_SIZE_MULTIPLE;
import static com.talv.icytower.game.gui.GUI.CONTROLS.ARROW_LEFT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.ARROW_RIGHT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.ARROW_UP;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_MOVEMENT_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.SCORE_TXT;

public class Player {


    public Rect getRect() {
        return rect;
    }


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


    public int getScore() {
        return score;
    }


    protected int stateUpdateTime;

    private static final int TIME_FROM_SIDE_STAND_TO_STANDING = 400;


    public static final float ACCELERATION_SPEED = PLAYER_SIZE_MULTIPLE * 0.066f / 1.5f;

    public static final float DECELERATION_SPEED = ACCELERATION_SPEED * 2f;

    public static final float MAX_SPEED = PLAYER_SIZE_MULTIPLE * 33.333f / 1.5f;
    public static final float MIN_SPEED = MAX_SPEED / 10f;
    public static final float REACTION_FORCE_MULTIPLE = 0.8f;

    public static final float VERTICAL_DECELERATION = DECELERATION_SPEED * 0.8f;
    public static final float MAX_FALL_SPEED = MIN_SPEED * 0.3f;
    public static final float MIN_JUMP_SPEED = -MAX_FALL_SPEED * 0.93f;
    public static final float HORIZONTAL_TO_VERTICAL_JUMP_MULTIPLE = 0.053f;


    private float currentVerticalSpeed;


    private float currentSpeed;

    private float externalSpeed;


    private Direction currentDirection;
    private PlayerState currentState;
    private Rect rect;


    protected HashMap<PlayerState, PlayerAnimation> animations;

    private final PlayerControls playerControls;

    private int jumpSoundID;

    private int totalJumps;
    private long totalTime;

    public int getTotalJumps() {
        return totalJumps;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public Player(SoundPool soundPool, Context context) {
        rect = new Rect();
        playerControls = new PlayerControls();
        resetPlayer();
        initializeSounds(soundPool, context);
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

    public void initializeSounds(SoundPool soundPool, Context context) {
        jumpSoundID = soundPool.load(context, R.raw.jump_sound, 1);
    }

    public void updateScore(int newScore, GameCanvas canvas) {
        this.score = newScore;
        canvas.updateText(SCORE_TXT, "Score: " + newScore);
    }

    public void setCharacter(Character character) {
        if (character == null) {
            this.animations = null;
            RectUtils.setRectSize(rect, 0, 0);
        } else {
            this.animations = character.getAnimations();
            RectUtils.setRectSize(rect, character.getWidth(), character.getHeight());
        }
    }


    private void updateStateAndAnimation(PlayerState newState, int msPassed) {
        if (currentState == newState) {
            if (animations != null)
                animations.get(currentState).updateTime(msPassed);
        } else {
            currentState = newState;
            if (animations != null)
                animations.get(currentState).resetTime();
            stateUpdateTime = (int) System.currentTimeMillis();
        }

    }


    private Bitmap getCurrentImage() {
        return animations.get(currentState).getCurrentBitmap(currentDirection);
    }


    public void render(Canvas canvas, Engine engine) {
        if (animations == null) return;
        canvas.drawBitmap(getCurrentImage(), rect.left, rect.top - engine.getCameraY(), Engine.getGamePaint());

    }

    public void updatePlayer(int msPassed, Engine engine, int activeControls) {
        totalTime += msPassed;
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
        } else if (newX > Engine.CAMERA_WIDTH - rect.width()) {
            newX = Engine.CAMERA_WIDTH - rect.width();
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

        if (currentVerticalSpeed >= 0 /*not jumping*/ && !isOnPlatform(engine.getPlatforms())) {
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
        RectUtils.setRectX(rect, newX);
    }


    private boolean isJumping() {
        return (currentState == PlayerState.JUMPING || currentState == PlayerState.JUMP_MOVE) && currentVerticalSpeed < 0;
    }

    private boolean isFalling() {
        return (currentState == PlayerState.JUMPING || currentState == PlayerState.JUMP_MOVE) && currentVerticalSpeed > 0;
    }

    /*
        private boolean isPlatformBelow(LinkedList<Platform> platforms) {
            for (Platform platform :
                    platforms) {
                if (RectUtils.isRectBelowRect(platform.getRect(), rect)) {
                    return true;
                }
            }
            return false;
        }
    */
    private boolean isOnPlatform(LinkedList<Platform> platforms) {
        for (Platform platform :
                platforms) {
            if (RectUtils.isRectOnRect(rect, platform.getRect())) {
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
            for (Platform plat : engine.getPlatforms()) {
                PlayerPlatformsIntersection intersection =
                        RectUtils.doesPlatformIntersectWithMovementY(rect, yAfterIntersection, plat.getRect());
                if (intersection.didIntersect()) {
                    yAfterIntersection = Math.min(yAfterIntersection, intersection.getNewY());
                    platformWhichFellOn = plat;
                }
            }
            if (platformWhichFellOn != null /*intersected with platform*/) {
                updateStateAndAnimation(PlayerState.STANDING, msPassed);
                currentVerticalSpeed = 0;
                newY = yAfterIntersection;
                if (platformWhichFellOn instanceof DisappearingPlatform) {
                    ((DisappearingPlatform) platformWhichFellOn).onPlayerFall();
                }
                //onFallOnPlatform(platformWhichFellOn, engine);
                updateScore(platformWhichFellOn.getPlatformNumber() * 10, engine.getGameCanvas());
            }
        } /*else {
            // jumping up
            // update camera
        }*/
        RectUtils.setRectY(rect, newY);
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
            if (currentSpeed > 0) {
                currentSpeed = -MIN_SPEED;
            }
            currentSpeed -= ACCELERATION_SPEED * msPassed;
            currentSpeed = Math.max(currentSpeed, -MAX_SPEED);
        }

        private void moveRight() {
            if (currentSpeed < 0) {
                currentSpeed = MIN_SPEED;
            }
            currentSpeed += ACCELERATION_SPEED * msPassed;
            currentSpeed = Math.min(currentSpeed, MAX_SPEED);
        }


        private void jump(boolean decelerate) {
            if (decelerate)
                decelerateSpeed();
            boolean cantJump = isJumping() || isFalling();
            if (cantJump) return;
            engine.playSound(jumpSoundID, 0.8f);
            totalJumps++;
            updateStateAndAnimation(PlayerState.JUMPING, msPassed);
            currentVerticalSpeed = calculateJumpingSpeed();
        }

        private void moveJumpLeft() {
            jump(false);
            moveLeft();
        }

        private void moveJumpRight() {
            jump(false);
            moveRight();
        }

        private void stand() {

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

    @Override
    public String toString() {
        return "Player{" +
                "score=" + score +
                ", stateUpdateTime=" + stateUpdateTime +
                ", currentVerticalSpeed=" + currentVerticalSpeed +
                ", currentSpeed=" + currentSpeed +
                ", externalSpeed=" + externalSpeed +
                ", currentDirection=" + currentDirection +
                ", currentState=" + currentState +
                ", rect=" + rect +
                ", animations=" + animations +
                ", playerControls=" + playerControls +
                ", jumpSoundID=" + jumpSoundID +
                ", totalJumps=" + totalJumps +
                ", totalTime=" + totalTime +
                '}';
    }
}
