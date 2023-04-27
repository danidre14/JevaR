import core.JevaR;
import core.JevaScene;
import core.JevaSharedObjects;
import core.JevaKey;
import core.JevaMouse;
import core.JevaPrefab;
import core.JevaClip;
import core.JevaSound;
import core.JevaMeta;
import core.JevaSpriteSheet;
import core.JevaText;
import core.JevaTileMap;
import core.JevaUtils;
import core.JevaVCam;

import java.awt.*;
import java.io.IOException;

import javax.swing.plaf.nimbus.State;

public class GameApp4 {

    public static void main(String[] args) {
        int initWidth = 1420;
        int initHeight = 800;
        int tileSize = 100;
        int collectibleSize = 80;
        int initFps = 120;
        int initTps = 120;
        new JevaR(initWidth, initHeight, initFps, initTps, (jevar) -> {

            // jevar.debugMode = true;
            JevaMeta meta = jevar.meta;
            JevaMouse mouse = jevar.mouse;
            JevaKey key = jevar.key;

            jevar.state.setInt("score", 10);
            jevar.state.setInt("maxHealth", 5);
            jevar.state.setInt("health", jevar.state.getInt("maxHealth"));
            jevar.state.setLong("levelStartTime", 0);
            jevar.state.setInt("levelMaxTime", 10);

            String[][][] worldMap = {
                    { { "30" }, {
                            "2222222222222222222222",
                            "2                   d2",
                            "2        t        2222",
                            "2     3  2    B      2",
                            "2     2  2   2222    2",
                            "2     2  2           2",
                            "2   2222222          2",
                            "2                    2",
                            "2t             B     2",
                            "222    P     22222   2",
                            "2      11          c 2",
                            "2     1001c       2222",
                            "2   11000011         2",
                            "211100000000111    c 2",
                            "2000000000000001111112",
                    }
                    },
                    { { "10" }, {
                            "2222222222222222222222",
                            "2                   d2",
                            "2         t       2222",
                            "2      P  2          2",
                            "2      2  2   222    2",
                            "2      2  2          2",
                            "2000000000000001111112",
                    },
                    }
            };

            jevar.createSpriteSheet("face_animation", (self) -> {
                JevaSpriteSheet spritesheet = (JevaSpriteSheet) self;

                spritesheet.addFrame("animImage1", 150);
                spritesheet.addFrame("animImage2", 150);
                spritesheet.addFrame("animImage1", 150);
                spritesheet.addFrame("animImage2", 150);
                spritesheet.addFrame("animImage1", 150);
                spritesheet.addFrame("animImage2", 150);
                spritesheet.addFrame("animImage1", 150);
                spritesheet.addFrame("animImage3", 150);
            });

            jevar.createSpriteSheet("coin_animation", (self) -> {
                JevaSpriteSheet spritesheet = (JevaSpriteSheet) self;

                int duration = 100;

                spritesheet.addFrame("coin_anim_1", duration);
                spritesheet.addFrame("coin_anim_2", duration);
                spritesheet.addFrame("coin_anim_3", duration);
                spritesheet.addFrame("coin_anim_4", duration);
                spritesheet.addFrame("coin_anim_5", duration);
                spritesheet.addFrame("coin_anim_6", duration);
            });

            jevar.createTileMap("worldMap", 150, 3, tileSize, tileSize, (loaded_self) -> {
                JevaTileMap loaded_clip = (JevaTileMap) loaded_self;
                loaded_clip.setInstanceName("worldMap");

                int currLevel = jevar.state.getInt("currLevel", 0);

                if (currLevel >= worldMap.length) {
                    jevar.useScene("menuScene");
                    return;
                }

                jevar.state.setInt("levelMaxTime", Integer.parseInt(worldMap[currLevel][0][0]) * 1000);
                jevar.state.setLong("levelStartTime", jevar.currentClockMillis());

                loaded_clip.setTileMapEnum('0', "ground_tile_2");
                loaded_clip.setTileMapEnum('1', "ground_tile_1");
                loaded_clip.setTileMapEnum('2', "brick");
                loaded_clip.setTileMapEnum('3', "air");

                loaded_clip.setTileTypeEnum('0', "floor");
                loaded_clip.setTileTypeEnum('1', "floor");
                loaded_clip.setTileTypeEnum('2', "floor");
                loaded_clip.setTileTypeEnum(' ', "air");

                loaded_clip.loadMapFrom2DArray(worldMap[currLevel][1]);

                loaded_clip.createClipAtTile('P', "mainChar");
                loaded_clip.createClipAtTile('B', "baddie1");
                loaded_clip.createClipAtTile('c', "coin_drop");
                loaded_clip.createClipAtTile('t', "treasure");
                loaded_clip.createClipAtTile('d', "door");

            });

            jevar.createPrefab("hurtable", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                loaded_clip.state.setInt("maxHealth", 5);
                loaded_clip.state.setInt("health", loaded_clip.state.getInt("maxHealth"));
                loaded_clip.state.setBoolean("gotHurt", false);
                loaded_clip.state.setBoolean("isHurt", false);
                loaded_clip.state.setBoolean("canGetHurt", true);
                loaded_clip.state.setBoolean("isAlive", true);
                loaded_clip.state.setInt("hurtingWait", 500);
                loaded_clip.state.setLong("hurtingTime", 0);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    int maxHealth = clip.state.getInt("maxHealth");
                    int health = clip.state.getInt("health");
                    boolean gotHurt = clip.state.getBoolean("gotHurt");
                    boolean isHurt = clip.state.getBoolean("isHurt");
                    boolean canGetHurt = clip.state.getBoolean("canGetHurt");
                    boolean isAlive = clip.state.getBoolean("isAlive");
                    int hurtingWait = clip.state.getInt("hurtingWait");
                    long hurtingTime = clip.state.getLong("hurtingTime");

                    isHurt = false;

                    if (isAlive) {
                        if (gotHurt) {
                            gotHurt = false;
                            if (canGetHurt) {
                                canGetHurt = false;
                                hurtingTime = jevar.currentClockMillis();
                                // take damage
                                isHurt = true;
                                health--;
                                if (health <= 0) {
                                    isAlive = false;
                                }
                            }
                        }

                        if (!canGetHurt && jevar.currentClockMillis() - hurtingTime > hurtingWait) {
                            canGetHurt = true;
                        }
                    }

                    clip.props.setAlpha(canGetHurt ? 1 : 0.5);

                    clip.state.setInt("health", health);
                    clip.state.setBoolean("gotHurt", gotHurt);
                    clip.state.setBoolean("isHurt", isHurt);
                    clip.state.setBoolean("canGetHurt", canGetHurt);
                    clip.state.setBoolean("isAlive", isAlive);
                    clip.state.setInt("hurtingWait", hurtingWait);
                    clip.state.setLong("hurtingTime", hurtingTime);
                });
            });

            jevar.createPrefab("collectible", 0, 0, collectibleSize, collectibleSize, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                JevaPrefab mainChar = (JevaPrefab) jevar.getJevaClip("mainChar");
                loaded_clip.state.setState("mainChar", mainChar);
                // loaded_clip.useGraphic("coins");

                loaded_clip.props.shiftAnchorX(0.5);
                loaded_clip.props.shiftAnchorY(0.5);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    if (mainChar == null)
                        return;
                    if (clip.hitTest(mainChar)) {
                        clip.state.setBoolean("touchingPlayer", true);
                    } else
                        clip.state.setBoolean("touchingPlayer", false);
                });
            });
            jevar.createPrefab("coin_drop", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("drop");
                loaded_clip.useSpriteSheet("coin_animation");
                loaded_clip.state.setInt("scoreIncrease", 10);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    if (clip.state.getBoolean("giveReward")) {
                        // play sound
                        jevar.state.alterInt("score", clip.state.getInt("scoreIncrease"));
                        clip.remove();
                    }
                });
            });
            jevar.createPrefab("heart_drop", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("drop");
                loaded_clip.useGraphic("life_full");
                loaded_clip.state.setInt("score", 10);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    if (clip.state.getBoolean("giveReward")) {
                        // play sound
                        if (jevar.state.getInt("health") < jevar.state.getInt("maxHealth")) {
                            JevaPrefab mainChar = (JevaPrefab) clip.state.getState("mainChar");
                            mainChar.state.alterInt("health", 1);
                            clip.remove();
                        } else {
                            clip.state.setBoolean("giveReward", false);
                        }
                    }
                });
            });

            jevar.createPrefab("treasure", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("collectible");
                loaded_clip.useGraphic("chest");
                loaded_clip.state.setBoolean("wasOpened", false);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    boolean wasOpened = clip.state.getBoolean("wasOpened");
                    if (!wasOpened && clip.state.getBoolean("touchingPlayer")) {
                        JevaScene currScene = jevar.getCurrentScene();
                        currScene.addPrefab("coin_drop", clip.props._x - clip.props._width * 1,
                                clip.props._y - (clip.props._height) * 1.4).state.setInt("scoreIncrease", 50);

                        currScene.addPrefab("coin_drop", clip.props._x - clip.props._width * 0.1,
                                clip.props._y - (clip.props._height) * 0.8).state.setInt("scoreIncrease", 50);

                        currScene.addPrefab("heart_drop", clip.props._x - clip.props._width * 0.55,
                                clip.props._y - (clip.props._height) * 1.1);

                        wasOpened = true;
                        clip.useGraphic("chest_open");
                        // play sound
                        // JevaPrefab mainChar = (JevaPrefab) clip.state.getState("mainChar");
                        // mainChar.state.setInt("health",
                        // Math.min(mainChar.state.getInt("health") + 1,
                        // jevar.state.getInt("maxHealth")));
                    }

                    clip.state.setBoolean("wasOpened", wasOpened);
                });
            });

            jevar.createPrefab("drop", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                loaded_clip.props._width = 60;
                loaded_clip.props._height = 60;

                loaded_clip.extend("collectible").extend("ragdoll");

                loaded_clip.state.setLong("timeSpawned", jevar.currentClockMillis());
                loaded_clip.state.setInt("actionDelay", 1000);
                loaded_clip.state.setBoolean("giveReward", false);

                loaded_clip.state.setInt("jumpYSpeed", -500);
                loaded_clip.state.setInt("heldJumpYSpeed", -250);
                loaded_clip.state.setDouble("grav", -500);
                loaded_clip.state.setBoolean("canJump", true);
                loaded_clip.state.setBoolean("goingUp", true);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    if (jevar.currentClockMillis() - clip.state.getLong("timeSpawned") > clip.state
                            .getInt("actionDelay") && clip.state.getBoolean("touchingPlayer")) {
                        clip.state.setBoolean("giveReward", true);
                    }
                });
            });

            jevar.createPrefab("flood", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");

                loaded_clip.setInstanceName("flood");
                loaded_clip.props._width = map.props._width;
                loaded_clip.props._x = map.props._x;
                loaded_clip.props._y = map.props._y + map.props._height - tileSize;

                loaded_clip.useGraphic("flood");

                loaded_clip.props.shiftAnchorX(0.5);
                loaded_clip.props.shiftAnchorY(1);

                loaded_clip.props.setAlpha(0.6);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    long levelStartTime = jevar.state.getLong("levelStartTime");
                    int levelMaxTime = jevar.state.getInt("levelMaxTime");

                    double mapHeight = map.props._height;

                    clip.props._height = Math.min(((jevar.currentClockMillis() - levelStartTime) * 1.0 / levelMaxTime),
                            1)
                            * mapHeight;

                    JevaPrefab mainChar = (JevaPrefab) jevar.getJevaClip("mainChar");

                    if (mainChar != null) {
                        if (clip.hitTest(mainChar.props._x, mainChar.props._y - mainChar.props._height * 0.8)) {
                            mainChar.state.setBoolean("gotHurt", true);
                        }
                    }
                });
            });

            jevar.createFunc("newGame", (state) -> {
                state.setInt("currLevel", 0);

                state.setInt("health", state.getInt("maxHealth"));
                jevar.useScene("gameScene");
            });

            jevar.createFunc("nextLevel", (state) -> {
                int currLevel = state.getInt("currLevel", 0);

                if (currLevel < worldMap.length) {
                    currLevel++;
                    state.setInt("currLevel", currLevel);
                    jevar.useScene("gameScene", true);
                } else {
                    jevar.useScene("menuScene");
                }
            });

            jevar.createPrefab("door", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("collectible");
                loaded_clip.useGraphic("Ruins_Tile Set");
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    if (clip.state.getBoolean("touchingPlayer")) {
                        // play sound
                        // jevar.useScene("menuScene");
                        jevar.execFunc("nextLevel");
                        // jevar.state.alterInt("score", 100);
                        clip.remove();
                    }
                });
            });

            jevar.createPrefab("overlay", 0, 0, initWidth, initHeight, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                JevaVCam vcam = jevar.getVCam("mainCamera");

                loaded_clip.usePainting((ctx, _x, _y, _width, _height, state) -> {
                    ctx.setColor(JevaUtils.color("A5EDFF"));
                    ctx.fillRect(0, 0, 400, 100);
                    ctx.fillRect((int) _width - 450, 0, 450, 100);

                    ctx.setColor(JevaUtils.color("9666FF"));
                    ctx.setStroke(new BasicStroke(5));
                    ctx.drawRect(0, 0, 400, 100);
                    ctx.drawRect((int) _width - 450, 0, 450, 100);

                });

                loaded_clip.addText("Floor: 1", initWidth - 420, 20, 300, 60, (loaded_score) -> {
                    JevaText score_clip = (JevaText) loaded_score;

                    score_clip.props._backgroundColor = new Color(0, 0, 0, 0);

                    score_clip.addJevascript((score_clip_script) -> {
                        score_clip.props._text = "Floor: " + (jevar.state.getInt("currLevel") + 1);
                        score_clip.props._fontColor = new Color(20, 20, 20);
                    });
                });
                loaded_clip.addPrefab(initWidth - 210, 20, 60, 60, (cgc) -> {
                    JevaPrefab coin_graphic_clip = (JevaPrefab) cgc;
                    
                    coin_graphic_clip.useSpriteSheet("coin_animation");
                });
                loaded_clip.addText("0", initWidth - 150, 20, 300, 60, (loaded_score) -> {
                    JevaText score_clip = (JevaText) loaded_score;
                    
                    score_clip.props._backgroundColor = new Color(0, 0, 0, 0);
                    
                    score_clip.addJevascript((score_clip_script) -> {
                        score_clip.props._text = "" + jevar.state.getInt("score");
                        score_clip.props._fontColor = new Color(142,111,48);
                    });
                });

                loaded_clip.addPrefab(5, 26, 400, 60, (l) -> {
                    JevaPrefab health_clip = (JevaPrefab) l;

                    Image heartImg = jevar.getImage("life_full");
                    int heartDistance = 20;

                    health_clip.usePainting((ctx, _x, _y, _width, _height, state) -> {
                        int health = jevar.state.getInt("health");
                        int heartSize = (int) _height;
                        for (int i = 0; i < health; i++) {
                            ctx.drawImage(heartImg, (int) _x + ((heartSize + heartDistance) * i), (int) _y,
                                    (int) heartSize, (int) (heartSize * 0.8), null);
                        }
                    });
                });

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    if (vcam == null)
                        return;

                    clip.props._x = vcam.projection._x - vcam.projection._width / 2;
                    clip.props._y = vcam.projection._y - vcam.projection._height / 2;
                });
            });

            jevar.createPrefab("ragdoll", initWidth / 2, 350, 100, 90, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("hurtable");

                loaded_clip.state.setBoolean("touchingWater", false);
                loaded_clip.state.setBoolean("touchingFloor", false);
                loaded_clip.state.setBoolean("touchingLeftWall", false);
                loaded_clip.state.setBoolean("touchingRightWall", false);
                loaded_clip.state.setBoolean("touchingLeftEdge", false);
                loaded_clip.state.setBoolean("touchingRightEdge", false);
                loaded_clip.state.setBoolean("touchingCeiling", false);
                loaded_clip.state.setBoolean("isColliding", false);

                loaded_clip.state.setBoolean("canJump", false);
                loaded_clip.state.setBoolean("canSwim", false);
                loaded_clip.state.setBoolean("holdingJump", false);
                loaded_clip.state.setBoolean("goingUp", false);
                loaded_clip.state.setBoolean("goingDown", false);
                loaded_clip.state.setBoolean("goingLeft", false);
                loaded_clip.state.setBoolean("goingRight", false);

                loaded_clip.state.setBoolean("usesGravity", true);
                loaded_clip.state.setDouble("grav", 0);
                loaded_clip.state.setDouble("gravity", 40);
                loaded_clip.state.setDouble("playerSpeed", 0);
                loaded_clip.state.setDouble("fallingSpeed", 0);
                loaded_clip.state.setInt("maxXSpeed", 600);
                loaded_clip.state.setInt("maxFallSpeed", 2500);
                loaded_clip.state.setInt("jumpYSpeed", -1500);
                loaded_clip.state.setInt("heldJumpYSpeed", -1000);

                loaded_clip.state.setInt("coyoteWait", 150);
                loaded_clip.state.setLong("coyoteTime", 0);
                loaded_clip.state.setInt("jumpBufferWait", 100);
                loaded_clip.state.setLong("jumpBufferTime", 0);
                loaded_clip.state.setInt("delayJumpWait", 200);
                loaded_clip.state.setLong("delayJumpTime", 0);

                JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");

                loaded_clip.props.shiftAnchorX(0.5);
                loaded_clip.props.shiftAnchorY(1);
                // loaded_clip.useGraphic("player1");
                loaded_clip.useSpriteSheet("face_animation");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    long timeNow = jevar.currentClockMillis();

                    boolean isHurt = clip.state.getBoolean("isHurt");

                    boolean touchingWater = clip.state.getBoolean("touchingWater");
                    boolean touchingFloor = clip.state.getBoolean("touchingFloor");
                    boolean touchingLeftWall = clip.state.getBoolean("touchingLeftWall");
                    boolean touchingRightWall = clip.state.getBoolean("touchingRightWall");
                    boolean touchingLeftEdge = clip.state.getBoolean("touchingLeftEdge");
                    boolean touchingRightEdge = clip.state.getBoolean("touchingRightEdge");
                    boolean touchingCeiling = clip.state.getBoolean("touchingCeiling");
                    boolean isColliding = clip.state.getBoolean("isColliding");

                    boolean canJump = clip.state.getBoolean("canJump");
                    boolean canSwim = clip.state.getBoolean("canSwim");
                    boolean holdingJump = clip.state.getBoolean("holdingJump");
                    boolean goingUp = clip.state.getBoolean("goingUp");
                    boolean goingDown = clip.state.getBoolean("goingDown");
                    boolean goingLeft = clip.state.getBoolean("goingLeft");
                    boolean goingRight = clip.state.getBoolean("goingRight");

                    boolean usesGravity = clip.state.getBoolean("usesGravity");
                    double grav = clip.state.getDouble("grav");
                    double gravity = clip.state.getDouble("gravity");
                    double playerSpeed = clip.state.getDouble("playerSpeed");
                    double fallingSpeed = clip.state.getDouble("fallingSpeed");
                    int maxXSpeed = clip.state.getInt("maxXSpeed");
                    int maxFallSpeed = clip.state.getInt("maxFallSpeed");
                    int jumpYSpeed = clip.state.getInt("jumpYSpeed");
                    int heldJumpYSpeed = clip.state.getInt("heldJumpYSpeed");

                    long coyoteTime = clip.state.getLong("coyoteTime");
                    int coyoteWait = clip.state.getInt("coyoteWait");
                    long jumpBufferTime = clip.state.getLong("jumpBufferTime");
                    int jumpBufferWait = clip.state.getInt("jumpBufferWait");
                    long delayJumpTime = clip.state.getLong("delayJumpTime");
                    int delayJumpWait = clip.state.getInt("delayJumpWait");

                    if (!touchingFloor) {
                        grav += gravity;
                    } else {
                        grav = 0;
                    }
                    playerSpeed = maxXSpeed * jevar.getDelta() * (touchingWater ? 0.5 : 1);

                    if (touchingFloor) {
                        canJump = true;
                        coyoteTime = timeNow;
                    } else {
                        if (!touchingWater && canJump && timeNow - coyoteTime > coyoteWait) {
                            canJump = false;
                        }
                    }

                    if (touchingWater) {
                        canJump = true;
                    }

                    if (goingLeft) {
                        if (!touchingLeftWall)
                            clip.props._x -= playerSpeed;
                        clip.props._scaleX = -1;
                    }
                    if (goingRight) {
                        if (!touchingRightWall)
                            clip.props._x += playerSpeed;
                        clip.props._scaleX = 1;
                    }
                    if (goingUp) {
                        jumpBufferTime = timeNow;

                        if (touchingWater) {
                            holdingJump = true;
                            grav = heldJumpYSpeed;
                            delayJumpTime = timeNow;
                        }
                    }

                    if (!touchingWater && canJump && (timeNow - jumpBufferTime) < jumpBufferWait) {
                        grav = jumpYSpeed;
                        canJump = false;

                        if (goingUp) {
                            holdingJump = true;
                            delayJumpTime = timeNow;
                        }
                    }

                    if (holdingJump) {
                        if (!goingUp || timeNow - delayJumpTime > delayJumpWait || touchingCeiling) {
                            holdingJump = false;
                        } else
                            grav = Math.min(grav, heldJumpYSpeed);
                    }

                    if (isHurt) {
                        grav = -500;
                        touchingFloor = false;
                    }

                    if (usesGravity) {
                        grav = Math.min(maxFallSpeed, grav);

                        fallingSpeed = grav * jevar.getDelta() * (touchingWater ? 0.5 : 1);
                        clip.props._y += fallingSpeed;
                    } else {
                        grav = 0;
                        fallingSpeed = 0;
                    }

                    JevaPrefab flood = (JevaPrefab) jevar.getJevaClip("flood");

                    if (flood != null && canSwim) {
                        if (flood.hitTest(clip.props._x, clip.props._y - clip.props._height * 0.8)) {
                            touchingWater = true;
                        } else {
                            touchingWater = false;
                        }
                    } else {
                        touchingWater = false;
                    }

                    if (map != null) {
                        double offsetX = clip.props._width / 4;
                        double widthX = clip.props._width / 3;
                        double heightY = clip.props._height * 0.9;
                        if (map.hitTest(clip.props._x - offsetX, clip.props._y, "floor")
                                || map.hitTest(clip.props._x + offsetX, clip.props._y, "floor")) {
                            touchingFloor = true;
                        } else {
                            touchingFloor = false;
                        }
                        if (map.hitTest(clip.props._x - offsetX, clip.props._y - heightY, "floor")
                                || map.hitTest(clip.props._x + offsetX, clip.props._y - heightY, "floor")) {
                            if (fallingSpeed < 0) {
                                touchingCeiling = true;
                            }
                        } else {
                            touchingCeiling = false;
                        }
                        if (map.hitTest(clip.props._x - widthX - 3, clip.props._y - heightY * 0.05, "floor")
                                || map.hitTest(clip.props._x - widthX, clip.props._y - heightY * 0.5,
                                        "floor")
                                || map.hitTest(clip.props._x - widthX, clip.props._y - heightY * 0.95,
                                        "floor")) {
                            touchingLeftWall = true;
                        } else {
                            touchingLeftWall = false;
                        }
                        if (map.hitTest(clip.props._x + widthX, clip.props._y - heightY * 0.05, "floor")
                                || map.hitTest(clip.props._x + widthX, clip.props._y - heightY * 0.5,
                                        "floor")
                                || map.hitTest(clip.props._x + widthX, clip.props._y - heightY * 0.95,
                                        "floor")) {
                            touchingRightWall = true;
                        } else {
                            touchingRightWall = false;
                        }
                        if (touchingFloor) {
                            if (map.hitTest(clip.props._x - widthX - 3, clip.props._y + heightY * 0.05, "air")
                                    || map.hitTest(clip.props._x - widthX, clip.props._y + heightY * 0.5,
                                            "air")
                                    || map.hitTest(clip.props._x - widthX, clip.props._y + heightY * 0.95,
                                            "air")) {
                                touchingLeftEdge = true;
                            } else {
                                touchingLeftEdge = false;
                            }
                            if (map.hitTest(clip.props._x + widthX, clip.props._y + heightY * 0.05, "air")
                                    || map.hitTest(clip.props._x + widthX, clip.props._y + heightY * 0.5,
                                            "air")
                                    || map.hitTest(clip.props._x + widthX, clip.props._y + heightY * 0.95,
                                            "air")) {
                                touchingRightEdge = true;
                            } else {
                                touchingRightEdge = false;
                            }
                        } else {
                            touchingLeftEdge = false;
                            touchingRightEdge = false;
                        }

                        isColliding = (touchingFloor || touchingCeiling || touchingLeftWall || touchingRightWall);
                    } else {
                        touchingFloor = touchingCeiling = touchingLeftWall = touchingRightWall = false;
                    }

                    if (touchingFloor) {
                        clip.props._y -= fallingSpeed;
                        grav = 0;
                    }
                    if (touchingCeiling) {
                        clip.props._y -= fallingSpeed;
                        grav = 0;
                    }
                    if (touchingLeftWall) {
                        clip.props._x += playerSpeed;
                    }
                    if (touchingRightWall) {
                        clip.props._x -= playerSpeed;
                    }

                    clip.state.setBoolean("touchingWater", touchingWater);
                    clip.state.setBoolean("touchingFloor", touchingFloor);
                    clip.state.setBoolean("touchingLeftWall", touchingLeftWall);
                    clip.state.setBoolean("touchingRightWall", touchingRightWall);
                    clip.state.setBoolean("touchingLeftEdge", touchingLeftEdge);
                    clip.state.setBoolean("touchingRightEdge", touchingRightEdge);
                    clip.state.setBoolean("touchingCeiling", touchingCeiling);
                    clip.state.setBoolean("isColliding", isColliding);

                    clip.state.setBoolean("canSwim", canSwim);
                    clip.state.setBoolean("canJump", canJump);
                    clip.state.setBoolean("holdingJump", holdingJump);
                    clip.state.setBoolean("goingUp", goingUp);
                    clip.state.setBoolean("goingDown", goingDown);
                    clip.state.setBoolean("goingLeft", goingLeft);
                    clip.state.setBoolean("goingRight", goingRight);

                    clip.state.setDouble("grav", grav);
                    clip.state.setDouble("gravity", gravity);
                    clip.state.setDouble("playerSpeed", playerSpeed);
                    clip.state.setDouble("fallingSpeed", fallingSpeed);

                    clip.state.setLong("coyoteTime", coyoteTime);
                    clip.state.setInt("coyoteWait", coyoteWait);
                    clip.state.setLong("jumpBufferTime", jumpBufferTime);
                    clip.state.setInt("jumpBufferWait", jumpBufferWait);
                    clip.state.setLong("delayJumpTime", delayJumpTime);
                    clip.state.setInt("delayJumpWait", delayJumpWait);
                });
            });

            jevar.createPrefab("mainChar", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("ragdoll");

                loaded_clip.setInstanceName("mainChar");
                JevaVCam vcam = jevar.getVCam("mainCamera");

                loaded_clip.state.setBoolean("canSwim", true);
                loaded_clip.state.setInt("hurtingWait", 2000);
                loaded_clip.state.setInt("maxHealth", jevar.state.getInt("maxHealth"));
                loaded_clip.state.setInt("health", jevar.state.getInt("health"));

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    jevar.state.setInt("health", clip.state.getInt("health"));

                    clip.state.setBoolean("goingLeft", key.isDown("left"));
                    clip.state.setBoolean("goingRight", key.isDown(JevaKey.RIGHT));
                    clip.state.setBoolean("goingUp", key.isDown(JevaKey.UP));

                    if (key.isPressed("space")) {
                        JevaScene currScene = jevar.getCurrentScene();

                        JevaPrefab projectile = (JevaPrefab) currScene.addPrefab("projectile", clip.props._x,
                                clip.props._y - (clip.props._height), 40, 40);
                        if (clip.props._scaleX > 0)
                            projectile.state.setBoolean("goingRight", true);
                        else
                            projectile.state.setBoolean("goingLeft", true);
                    }

                    if (!clip.state.getBoolean("isAlive")) {
                        jevar.useScene("menuScene");
                    }

                    if (vcam != null) {
                        JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");

                        if (map.props._width < vcam.projection._width) {
                            vcam.projection._x = map.props._x + map.props._width / 2;
                        } else {
                            vcam.projection._x = clip.props._x;
                            if (vcam.projection._x < map.props._x + vcam.projection._width / 2)
                                vcam.projection._x = map.props._x + vcam.projection._width / 2;
                            else if (vcam.projection._x > map.props._x + map.props._width - vcam.projection._width / 2)
                                vcam.projection._x = map.props._x + map.props._width - vcam.projection._width / 2;
                        }
                        if (map.props._height < vcam.projection._height) {
                            vcam.projection._y = map.props._y + map.props._height / 2;
                        } else {
                            vcam.projection._y = clip.props._y;
                            if (vcam.projection._y < map.props._y + vcam.projection._height / 2)
                                vcam.projection._y = map.props._y + vcam.projection._height / 2;
                            else if (vcam.projection._y > map.props._y + map.props._height
                                    - vcam.projection._height / 2)
                                vcam.projection._y = map.props._y + map.props._height - vcam.projection._height / 2;
                        }
                    }

                });
            });

            jevar.createPrefab("baddie1", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("collectible").extend("ragdoll");

                loaded_clip.state.setInt("health", 3);
                loaded_clip.state.setBoolean("goingLeft", true);
                loaded_clip.state.setInt("maxXSpeed", 300);
                loaded_clip.useGraphic("baddie_1");

                JevaPrefab mainChar = (JevaPrefab) loaded_clip.state.getState("mainChar");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    boolean isAlive = clip.state.getBoolean("isAlive");
                    boolean goingLeft = clip.state.getBoolean("goingLeft");
                    boolean goingRight = clip.state.getBoolean("goingRight");
                    boolean touchingLeftEdge = clip.state.getBoolean("touchingLeftEdge");
                    boolean touchingRightEdge = clip.state.getBoolean("touchingRightEdge");
                    boolean touchingLeftWall = clip.state.getBoolean("touchingLeftWall");
                    boolean touchingRightWall = clip.state.getBoolean("touchingRightWall");

                    if (isAlive) {
                        if (goingLeft && (touchingLeftEdge || touchingLeftWall)) {
                            goingLeft = false;
                            goingRight = true;
                        }
                        if (goingRight && (touchingRightEdge || touchingRightWall)) {
                            goingRight = false;
                            goingLeft = true;
                        }

                        if (clip.state.getBoolean("touchingPlayer")) {
                            mainChar.state.setBoolean("gotHurt", true);
                        }

                        JevaClip projectile = clip.hitTestGet("projectile");
                        if (projectile != null) {
                            clip.state.setBoolean("gotHurt", true);
                            projectile.remove();
                        }
                    } else {
                        clip.remove();
                    }

                    clip.state.setBoolean("goingLeft", goingLeft);
                    clip.state.setBoolean("goingRight", goingRight);
                    clip.state.setBoolean("touchingLeftEdge", touchingLeftEdge);
                    clip.state.setBoolean("touchingRightEdge", touchingRightEdge);
                    clip.state.setBoolean("touchingLeftWall", touchingLeftWall);
                    clip.state.setBoolean("touchingRightWall", touchingRightWall);
                });
            });

            jevar.createPrefab("projectile", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("ragdoll");

                loaded_clip.state.setBoolean("usesGravity", false);

                loaded_clip.props.setAnchorY(0.5);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    boolean isColliding = clip.state.getBoolean("isColliding");

                    if (isColliding) {
                        clip.remove();
                    }
                });
            });

            jevar.createPrefab("player2", 100, 350, 100, 100, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                JevaVCam vcam = jevar.getVCam("myCam1");

                loaded_clip.props.setAnchorX(0.5);
                loaded_clip.props.setAnchorY(0.5);
                loaded_clip.useGraphic("player2");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    double playerSpeed = 480 * jevar.getDelta();
                    if (key.isDown("a")) {
                        clip.props._x -= playerSpeed;
                        clip.props._scaleX = -1;
                    }
                    if (key.isDown(JevaKey.D)) {
                        clip.props._x += playerSpeed;
                        clip.props._scaleX = 1;
                    }
                    if (key.isDown(JevaKey.W)) {
                        clip.props._y -= playerSpeed;
                    }
                    if (key.isDown(JevaKey.S)) {
                        clip.props._y += playerSpeed;
                    }
                    if (vcam != null) {
                        vcam.projection._x = clip.props._x;
                        vcam.projection._y = clip.props._y;
                    }
                });
            });
            jevar.createPrefab("background", 0, 0, initWidth, initHeight, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                JevaVCam vcam = jevar.getVCam("mainCamera");
                loaded_clip.useGraphic("background_cave");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    if (vcam == null)
                        return;

                    clip.props._x = vcam.projection._x - vcam.projection._width / 2;
                    clip.props._y = vcam.projection._y - vcam.projection._height / 2;
                });
            });

            jevar.createText("textfield1", "Hello World", 0, 450, 300, 400,
                    (loaded_self) -> {
                        JevaText loaded_clip = (JevaText) loaded_self;
                        loaded_clip.props._fontSize = 24;
                        loaded_clip.props._x = initWidth / 2;
                        // loaded_clip.props._y = 40;
                        loaded_clip.props.setAnchorX(0.5);
                        loaded_clip.props.setAnchorY(1);
                        loaded_clip.props._backgroundColor = new Color(35, 0, 0);
                        loaded_clip.props.setAlign("l");

                        JevaVCam vcam = jevar.getVCam("mainCamera");

                        loaded_clip.addJevascript((self) -> {
                            JevaText clip = (JevaText) self;
                            // System.out.println("Textfield props _y: " + clip.props._y);
                            // System.out.println("Textfield props _anchorX: " + clip.props.getAnchorX());

                            String vcamMouseCords = "";
                            if (vcam != null)
                                vcamMouseCords = "\nVcam Mouse: [" + vcam._xmouse + ":" + vcam._ymouse + "]";

                            clip.props._text = "Mouse: [" + mouse._xmouse + ":" + mouse._ymouse + "]" + vcamMouseCords
                                    + "\n\nFPS: ["
                                    + jevar.getFPS() + "]" + "\nTPS: ["
                                    + jevar.getTPS() + "]" + "\nTimeScale: [" + jevar.getTimeScale() + "]"
                                    + "\nProcess Time: [" + jevar.currentProcessMillis() + "]"
                                    + "\nClock Time: [" + jevar.currentClockMillis() + "]"
                                    + "\nThis is a very long string of text that should go of the page sucesssfully";

                            if (jevar.key.isDown(JevaKey.N))
                                clip.props._width -= 30;
                            if (jevar.key.isDown(JevaKey.M))
                                clip.props._width += 30;
                            if (jevar.key.isPressed(JevaKey.J))
                                clip.props.setAlign("l");
                            if (jevar.key.isPressed(JevaKey.K))
                                clip.props.setAlign("c");
                            if (jevar.key.isPressed(JevaKey.L))
                                clip.props.setAlign("r");
                        });
                    });

            jevar.createText("button", "BUTTON", initWidth / 2, 350, 200, 80, (t) -> {
                JevaText loaded_clip = (JevaText) t;

                loaded_clip.state.setDouble("initHeight", loaded_clip.props._height);
                loaded_clip.props._width = (int) loaded_clip.props._height * 20 / 6;
                loaded_clip.props._fontSize = (int) loaded_clip.props._height * 5 / 6;

                loaded_clip.props._backgroundColor = Color.GREEN;

                loaded_clip.props.setAnchorX(0.5);
                loaded_clip.props.setAnchorY(0.5);
                loaded_clip.props.setAlign("c");

                loaded_clip.addJevascript((self) -> {
                    JevaText clip = (JevaText) self;
                    clip.state.setBoolean("isClicked", false);

                    if (clip.props.isHovered()) {
                        double height = clip.state.getDouble("initHeight") * 1.2;
                        loaded_clip.props._height = height;
                        loaded_clip.props._width = (int) height * 20 / 6;
                        loaded_clip.props._fontSize = (int) height * 5 / 6;
                    } else {
                        double height = clip.state.getDouble("initHeight");
                        loaded_clip.props._height = height;
                        loaded_clip.props._width = (int) height * 20 / 6;
                        loaded_clip.props._fontSize = (int) height * 5 / 6;
                    }

                    if (jevar.mouse.isPressed(JevaMouse.LEFT) && clip.props.isHovered()) {
                        clip.state.setBoolean("pressed", true);
                    }
                    if (jevar.mouse.isReleased(JevaMouse.LEFT) && clip.state.getBoolean("pressed")) {
                        if (clip.props.isHovered()) {
                            clip.state.setBoolean("isClicked", true);
                        }
                        clip.state.setBoolean("pressed", false);
                    }
                });
            });

            jevar.createScene("menuScene", (s) -> {
                JevaScene scene = (JevaScene) s;

                scene.addText("CAVE CLIMB", initWidth / 2, 80, initWidth * 0.8, 150, (t) -> {
                    JevaText loaded_clip = (JevaText) t;

                    loaded_clip.props.setAnchorX(0.5);
                    loaded_clip.props.setAlign("c");
                });

                scene.addText("Credits:\nMade by Micah Brereton and Carlonn Rivers\nCOMP3609 Game Programming Project",
                        40, initHeight - 40, initWidth * 0.8, 60, (t) -> {
                            JevaText loaded_clip = (JevaText) t;

                            loaded_clip.props._fontSize = 20;

                            loaded_clip.props.setAnchorX(0);
                            loaded_clip.props.setAnchorY(1);
                        });

                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "PLAY";
                    loaded_text.props._x = initWidth / 2;
                    loaded_text.props._y = 350;

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked")) {
                            jevar.execFunc("newGame");
                        }
                    });
                });
                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "EXIT";
                    loaded_text.props._x = initWidth / 2;
                    loaded_text.props._y = 500;

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked")) {
                            jevar.meta.closeApplication();
                        }
                    });
                });

            });
            jevar.createScene("gameScene", (s) -> {
                JevaScene scene = (JevaScene) s;

                JevaVCam cam1 = scene.addVCam("mainCamera", 0, 0, initWidth, initHeight);
                cam1.centerPAnchors();

                scene.addPrefab("background");

                scene.addTileMap("worldMap");
                // scene.addText("textfield1");
                scene.addPrefab("flood");

                scene.addPrefab("overlay");
            });

            jevar.useScene("menuScene");

            jevar.attachJevascript((s) -> {
                JevaR core = (JevaR) s;

                if (key.isReleased("1")) {
                    core.useScene("menuScene");
                } else if (key.isReleased("2")) {
                    core.useScene("gameScene");
                }
                if (key.isReleased("Q")) {
                    meta.closeApplication();
                }
                if (key.isReleased("R")) {
                    core.resetScene();
                }
                if (key.isPressed(JevaKey.Z)) {
                    jevar.debugMode = !jevar.debugMode;
                }
                if (key.isPressed(JevaKey.C)) {
                    core.alterTimeScale(-0.25);
                }
                if (key.isPressed(JevaKey.V)) {
                    core.alterTimeScale(0.25);
                }
            });
        });
    }
}