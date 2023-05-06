import core.JevaR;
import core.JevaScene;
import core.JevaSharedObjects;
import core.JevaKey;
import core.JevaMouse;
import core.JevaPrefab;
import core.JevaClip;
import core.JevaClipProps;
import core.JevaSound;
import core.JevaMeta;
import core.JevaSpriteSheet;
import core.JevaText;
import core.JevaTileMap;
import core.JevaUtils;
import core.JevaVCam;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.plaf.nimbus.State;

public class GameApp4 {

    public static class WavePoint {
        public double x;
        public double y;
        public double spdy;
        public double mass;

        public WavePoint(double x, double y, double spdy, double mass) {
            this.x = x;
            this.y = y;
            this.spdy = spdy;
            this.mass = mass;
        }
    }

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

            int backgroundDepth = 0;
            int collectibleDepth = 2;
            int worldMapDepth = 3;
            int baddieDepth = 4;
            int playerDepth = 5;
            int floodDepth = 7;
            int overlayDepth = 10;

            jevar.createSound("underwater_ambience", 1).setVolume(0.5);
            jevar.createSound("drop_water_splash", 3).setVolume(0.95);
            jevar.createSound("step_splash_sfx", 3).setVolume(0.65);
            jevar.createSound("step_sfx", 3).setVolume(1);
            jevar.createSound("open_chest_sfx", 3);
            jevar.createSound("chest_open_sfx", 3).setVolume(0.5);
            jevar.createSound("skele_walk_sfx");
            jevar.createSound("level_win_sfx").setVolume(0.7);
            jevar.createSound("skele_dead_sfx").setVolume(0.6);
            jevar.createSound("jump_sfx", 3).setVolume(0.3);
            jevar.createSound("baddie_shoot_sfx", 3).setVolume(0.95);
            jevar.createSound("player_shoot_sfx", 3).setVolume(0.6);
            jevar.createSound("coin_collect_sfx", 5).setVolume(0.9);
            jevar.createSound("skele_hit_sfx").setVolume(0.6);
            jevar.createSound("baddie_dead_sfx", 3).setVolume(0.6);
            jevar.createSound("player_hit_sfx", 3).setVolume(0.9);
            jevar.createSound("game_over_sfx", 1).setVolume(0.7);
            jevar.createSound("baddie_hit_sfx", 5).setVolume(0.6);
            jevar.createSound("chest_near_sfx");
            jevar.createSound("button_hover_sfx", 4);
            jevar.createSound("button_press_sfx", 2);
            jevar.createSound("snake_baddie_hiss_sfx").setVolume(2);
            jevar.createSound("bat_baddie_screech").setVolume(0.5);
            jevar.createSound("health_collected", 5).setVolume(0.7);
            jevar.createSound("background_music_1").setVolume(0.5);

            JevaSharedObjects so = JevaSharedObjects.getLocal("cave_climb");

            final String initPlayerName = (String) so.getItem("playerName");

            final int maxHighScores = 5;
            final String nameLessScore = "<None>";
            int currHighScores = 0;

            class HighScore {
                String name;
                int level;
                int score;
                long duration;
                String id;

                public HighScore(String name, int level, int score, long duration) {
                    this.name = name;
                    this.level = level;
                    this.score = score;
                    this.duration = duration;
                    this.id = JevaUtils.generateUUID();
                }
            }

            HighScore[] highScores = new HighScore[maxHighScores];

            for (int i = 0; i < maxHighScores; i++) {
                String _name = so.getItem("highscore_" + i + "_name");
                if (_name == null) {
                    HighScore highScore = new HighScore(nameLessScore, 0, 0, 0);
                    highScores[i] = highScore;
                } else {
                    int _level = Integer.parseInt(so.getItem("highscore_" + i + "_level"));
                    int _score = Integer.parseInt(so.getItem("highscore_" + i + "_score"));
                    long _duration = Long.parseLong(so.getItem("highscore_" + i + "_duration"));

                    HighScore highScore = new HighScore(_name, _level, _score, _duration);
                    highScores[i] = highScore;
                    currHighScores++;
                }
            }

            jevar.createFunc("calcHighScores", (state, arg) -> {
                ArrayList<HighScore> tempHighScores = new ArrayList<>(Arrays.asList(highScores));

                long timeTaken = jevar.currentClockMillis() - jevar.state.getLong("timeStartedGame");

                HighScore currentScore = new HighScore(jevar.state.getString("playerName"),
                        jevar.state.getInt("currLevel"), jevar.state.getInt("score"), timeTaken);

                jevar.state.setState("currHighschore", currentScore);

                tempHighScores.add(currentScore);

                Collections.sort(tempHighScores, new Comparator<HighScore>() {
                    @Override
                    public int compare(HighScore a, HighScore b) {
                        if (a.level == b.level) {
                            return b.score - a.score;
                        } else {
                            return b.level - a.level;
                        }
                    }
                });

                for (int i = 0; i < maxHighScores; i++) {
                    highScores[i] = tempHighScores.get(i);
                    so.setItem("highscore_" + i + "_name", highScores[i].name);
                    so.setItem("highscore_" + i + "_level", highScores[i].level);
                    so.setItem("highscore_" + i + "_score", highScores[i].score);
                    so.setItem("highscore_" + i + "_duration", highScores[i].duration);
                }

                return null;
            });
            jevar.createFunc("resetHighScores", (state, arg) -> {
                HighScore clearScore = new HighScore(nameLessScore, 0, 0, 0);
                for (int i = 0; i < maxHighScores; i++) {
                    highScores[i] = clearScore;
                    so.setItem("highscore_" + i + "_name", nameLessScore);
                    so.setItem("highscore_" + i + "_level", 0);
                    so.setItem("highscore_" + i + "_score", 0);
                    so.setItem("highscore_" + i + "_duration", 0);
                }

                return null;
            });

            jevar.state.setInt("score", 0);
            jevar.state.setInt("maxHealth", 10);
            jevar.state.setInt("health", jevar.state.getInt("maxHealth"));
            jevar.state.setLong("levelStartTime", 0);
            jevar.state.setInt("levelMaxTime", 10);
            jevar.state.setInt("currLevel", 2);
            jevar.state.setString("playerName", initPlayerName != null ? initPlayerName : "");

            jevar.getSound("background_music_1").playLoop();

            String[][][] worldMap = {
                    { { "300" },
                            {
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                                    "e     c eeeeeeeeeeeeeeeee",
                                    "e      cdeeeeeeeeeeeeeeee",
                                    "e   c ggeeeeeeeeeeeeeeeee",
                                    "e    ceeeeeeeeeeeeeeeeeee",
                                    "e c gge          e      e",
                                    "ec7 eee          e P0   e",
                                    "egg/eee  4       egggg  e",
                                    "e clc eg/gg      eeeee  e",
                                    "ec l6ce l e   g         e",
                                    "egg/gge   e 3 e 2      1e",
                                    "eee|eee   egggeggggggggge",
                                    "e  l      eeeeeeeeeeeeeee",
                                    "e  l  5   eeeeeeeeeeeeeee",
                                    "egggggggggeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeee",
                            },
                            {
                                    "Press 'D' or 'Right Arrow' to go right.",
                                    "Press 'A' or 'Left Arrow' to go left.",
                                    "Press 'W' or 'Up Arrow' to jump.",
                                    "Hold the jump button to jump higher.",
                                    "Hold 'S' or 'Down Arrow' to climb down ladders.",
                                    "Hold up or down to climb up or down ladders.",
                                    "Collect coins for score",
                                    "Proceed to the cave exit to get to a higher floor.",
                            } },
                    { { "40" },
                            {
                                    "eeeeeeeeeee",
                                    "e  c c ceee",
                                    "e  2c c dee",
                                    "e  gggggeee",
                                    "e  eeeeeeee",
                                    "e  ee  c  e",
                                    "e  eec    e",
                                    "e  eegg   e",
                                    "e  eeee   e",
                                    "ec c  c   e",
                                    "e c c    ce",
                                    "e/gggggg be",
                                    "e|eeeeeec e",
                                    "e|eeeeee  e",
                                    "e     eeb e",
                                    "e     ee ce",
                                    "e   c ee  e",
                                    "e    cee be",
                                    "e   ggeec e",
                                    "e   eeee  e",
                                    "e   eeeeb e",
                                    "e   c c c e",
                                    "e  c c c ce",
                                    "e bggggggge",
                                    "e  eeeeeeee",
                                    "e  eeeeeeee",
                                    "eb ee c   e",
                                    "e  eec    e",
                                    "e  eegg   e",
                                    "e beeee   e",
                                    "e         e",
                                    "e       1 e",
                                    "e      ggge",
                                    "e  P0  eeee",
                                    "eggggggeeee",
                                    "eeeeeeeeeee",
                                    "eeeeeeeeeee",
                                    "eeeeeeeeeee",
                                    "eeeeeeeeeee",
                                    "eeeeeeeeeee",
                            },
                            {
                                    "Water is filling the cave. Escape before you run out of breath.",
                                    "Hold up to swim up, and release to sink. Try not to drown.",
                                    "Climb out the cave to win!",
                            } },
                    { { "150" },
                            {
                                    "eeeeeeeeeeeeeeeeeeeeeeeeee",
                                    "e c c c eeeeeeeeeeeeeeeeee",
                                    "ec c c cdeeeeeeeeeeeeeeeee",
                                    "eg/ggggggeeeeeeeeeeeeeeeee",
                                    "ee|eeeeeeeeeeeeeeeeeeeeeee",
                                    "e l                      e",
                                    "e l                      e",
                                    "e l     b      b      c  e",
                                    "e l    SbS     b     c   e",
                                    "eg/gggggggggggggg   c    e",
                                    "e l eeeeeeeeeeeeec c     e",
                                    "e l eeeeeeeeeeeee c  3   e",
                                    "e   eeeeeeeeeeeeegggggg  e",
                                    "e t4eeeeeeeeeeeeeel B c  e",
                                    "eeeeeeeeeeeeeeeeeel  c c e",
                                    "eeeeeeeeeeeeeeeeeel  gggge",
                                    "e                el  c Ble",
                                    "e                el c   le",
                                    "e      G         eeggg  le",
                                    "e      gggg 1           le",
                                    "e      eeeeggg 2        le",
                                    "e    ggeeeeeeeggggggggggge",
                                    "e P0 eeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeeee",
                                    "eeeeeeeeeeeeeeeeeeeeeeeeee",
                            },
                            {
                                    "There are many enemies underground. Avoid them!",
                                    "Press 'Space' to shoot. Destroy the snake!",
                                    "Look out for flying bats!",
                                    "Skeletons are most dangerous! They shoot arrows!",
                                    "You've found treasure! Well done!",
                            } },
                    { { "60" }, {
                            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                            "b    l                        b",
                            "b    l                        d",
                            "bbbbblbbbbb                bbbb",
                            "b    l          t             b",
                            "b    l         ggg      G     b",
                            "b    l       g/eeeg    gggggggb",
                            "b         2  e|eeee           b",
                            "b     ggggggge|eeeegg         b",
                            "b             l     B         b",
                            "b             l         5     b",
                            "bbbb          lS      bbbbb   b",
                            "b            ggggg          c b",
                            "b     b    1geeeeegc       bbbb",
                            "b P 0 b    geeeeeeegg         b",
                            "bggggggg/ggeeeeeeeeeeggg  5 c b",
                            "beeeeeee|eeeeeeeeeeeeeeeggggggb",
                            "be      l              eeeeeeeb",
                            "be      l                    eb",
                            "beeeeee l     t              eb",
                            "beeeeeeeeeeeeeeeeeeeeeeeeeeeeeb",
                            "beeeeeeeeeeeeeeeeeeeeeeeeeeeeeb",
                    }, { "Use wasd or arrows to move.", "Press the spacebar key to shoot.", "Climb out the cave to win!" } },
                    { { "60" }, {
                            "bbbbbbbbbbbbbbbbbbbbbb",
                            "b                    d",
                            "b         t       bbbb",
                            "b      P  b     B    b",
                            "b      b  b   bbb    b",
                            "b 0 1  b  b          b",
                            "bggggggggggggggggggggb",
                            "beeeeeeeeeeeeeeeeeeeeb",
                            "beeeeeeeeeeeeeeeeeeeeb",
                            "beeeeeeeeeeeeeeeeeeeeb",
                            "beeeeeeeeeeeeeeeeeeeeb",
                    }, { "other sign 1" } }
            };

            int charAnimationSize = 41;
            int baddieAnimationSize = 32;
            String playerSpriteSheet = "player_spritesheet";

            jevar.createSpriteSheet("snake_baddie_animation", "snake_baddie_spritesheet", 4, baddieAnimationSize, 0, 8);
            jevar.createSpriteSheet("bat_baddie_fly_animation", "bat_baddie_spritesheet", 4, baddieAnimationSize, 0, 8);
            jevar.createSpriteSheet("bat_baddie_die_animation", "bat_baddie_spritesheet", 4, baddieAnimationSize, 1, 6);

            jevar.createSpriteSheet("heart_animation", "heart_spritesheet", 4, 16, 0, 8);

            jevar.createSpriteSheet("char_idle_animation", playerSpriteSheet, 4, charAnimationSize, 0, 6);
            jevar.createSpriteSheet("char_jump_animation", playerSpriteSheet, 2, charAnimationSize, 1, 10);
            jevar.createSpriteSheet("char_run_animation", playerSpriteSheet, 6, charAnimationSize, 2, 10);
            jevar.createSpriteSheet("char_hurt_animation", playerSpriteSheet, 2, charAnimationSize, 3, 10);
            jevar.createSpriteSheet("char_idleclimb_animation", playerSpriteSheet, 1, charAnimationSize, 4, 1);
            jevar.createSpriteSheet("char_climbup_animation", playerSpriteSheet, 6, charAnimationSize, 4, 10);
            jevar.createSpriteSheet("char_climbdown_animation", playerSpriteSheet, 6, charAnimationSize, 5, 10);
            jevar.createSpriteSheet("char_dead_animation", playerSpriteSheet, 6, charAnimationSize, 6, 10);
            jevar.createSpriteSheet("char_fall_animation", playerSpriteSheet, 1, charAnimationSize, 7, 1);
            jevar.createSpriteSheet("char_shoot_animation", playerSpriteSheet, 3, charAnimationSize, 8, 18);
            jevar.createSpriteSheet("char_swim_animation", playerSpriteSheet, 2, charAnimationSize, 9, 10);

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

            jevar.createSpriteSheet("skele_walk_animation", "skele_spritesheet", 6, baddieAnimationSize, 0, 10);
            jevar.createSpriteSheet("skele_dead_animation", "skele_spritesheet", 7, baddieAnimationSize, 1, 10);

            jevar.createTileMap("worldMap", 150, 3, tileSize, tileSize, (loaded_self) -> {
                JevaTileMap loaded_clip = (JevaTileMap) loaded_self;
                loaded_clip.setInstanceName("worldMap");

                int currLevel = jevar.state.getInt("currLevel", 0);

                if (currLevel >= worldMap.length) {
                    jevar.useScene("highscoresScene", true);
                    return;
                }

                jevar.state.setInt("levelMaxTime", Integer.parseInt(worldMap[currLevel][0][0]) * 1000);
                jevar.state.setLong("levelStartTime", jevar.currentClockMillis());

                loaded_clip.setTileEnum('e', "earth", "floor");
                loaded_clip.setTileEnum('g', "grass", "floor");
                loaded_clip.setTileEnum('b', "cave_stone", "floor");
                loaded_clip.setTileEnum('l', "ladder", "ladder");
                loaded_clip.setTileEnum('/', "ladder_grass", "ladder_floor");
                loaded_clip.setTileEnum('|', "ladder_earth", "ladder_floor");
                loaded_clip.setTileEnum(' ', "air", "air");
                loaded_clip.setTileEnum('!', "decor_1", "air");

                int infoLen = 0;
                String[] infos = null;
                if (worldMap[currLevel].length > 2) {
                    infos = worldMap[currLevel][2];
                    infoLen = infos.length;
                }

                for (int i = infoLen; i < 10; i++) {
                    loaded_clip.setTileEnum((char) ('0' + i), "air", "air");
                }

                loaded_clip.loadMapFrom2DArray(worldMap[currLevel][1]);

                loaded_clip.createClipAtTile('P', "mainChar", playerDepth);
                loaded_clip.createClipAtTile('G', "snake_baddie", baddieDepth);
                loaded_clip.createClipAtTile('B', "bat_baddie", baddieDepth);
                loaded_clip.createClipAtTile('S', "skele_baddie", baddieDepth);
                loaded_clip.createClipAtTile('c', "coin_drop", collectibleDepth);
                loaded_clip.createClipAtTile('h', "heart_drop", collectibleDepth);
                loaded_clip.createClipAtTile('t', "treasure", collectibleDepth);
                loaded_clip.createClipAtTile('d', "door", collectibleDepth);

                if (infos != null) {
                    for (int i = 0; i < infoLen; i++) {
                        final String msg = infos[i];
                        loaded_clip.createClipAtTile((char) ('0' + i), "info_sign", collectibleDepth, (info_loaded) -> {
                            JevaPrefab info_clip = (JevaPrefab) info_loaded;

                            info_clip.state.setString("info_message", msg);
                        });
                    }
                }
            });

            jevar.createPrefab("hurtable", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                loaded_clip.state.setInt("maxHealth", 10);
                loaded_clip.state.setInt("health", loaded_clip.state.getInt("maxHealth"));
                loaded_clip.state.setBoolean("gotHurt", false);
                loaded_clip.state.setBoolean("isHurt", false);
                loaded_clip.state.setBoolean("canGetHurt", true);
                loaded_clip.state.setBoolean("playingHurt", false);
                loaded_clip.state.setBoolean("isAlive", true);
                loaded_clip.state.setBoolean("justDied", false);
                loaded_clip.state.setInt("hurtingWait", 500);
                loaded_clip.state.setLong("hurtingTime", 0);

                loaded_clip.state.setString("hb_diedSound", "baddie_dead_sfx");
                loaded_clip.state.setString("hb_hurtSound", "baddie_hit_sfx");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    int maxHealth = clip.state.getInt("maxHealth");
                    int health = clip.state.getInt("health");
                    boolean gotHurt = clip.state.getBoolean("gotHurt");
                    boolean isHurt = clip.state.getBoolean("isHurt");
                    boolean canGetHurt = clip.state.getBoolean("canGetHurt");
                    boolean isAlive = clip.state.getBoolean("isAlive");
                    boolean justDied = clip.state.getBoolean("justDied");
                    int hurtingWait = clip.state.getInt("hurtingWait");
                    long hurtingTime = clip.state.getLong("hurtingTime");
                    JevaSound deadSound = jevar.getSound(clip.state.getString("hb_diedSound"));
                    JevaSound hurtSound = jevar.getSound(clip.state.getString("hb_hurtSound"));

                    isHurt = false;
                    justDied = false;

                    if (isAlive) {
                        clip.props.setAlpha(canGetHurt ? 1 : 0.5);
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
                                    justDied = true;
                                    clip.props.setAlpha(1);
                                }
                                loaded_clip.state.setBoolean("playingHurt", true);
                                jevar.setTimeout((state, arg) -> {
                                    loaded_clip.state.setBoolean("playingHurt", false);
                                    return null;
                                }, 400);
                            }
                        }

                        if (!canGetHurt && jevar.currentClockMillis() - hurtingTime > hurtingWait) {
                            canGetHurt = true;
                        }
                    } else {
                        clip.props.setAlpha(1);
                    }

                    if (isHurt) {
                        if (hurtSound != null)
                            hurtSound.playOnce();
                    }
                    if (justDied) {
                        if (deadSound != null)
                            deadSound.playOnce();
                    }

                    clip.state.setInt("health", health);
                    clip.state.setBoolean("gotHurt", gotHurt);
                    clip.state.setBoolean("isHurt", isHurt);
                    clip.state.setBoolean("canGetHurt", canGetHurt);
                    clip.state.setBoolean("isAlive", isAlive);
                    clip.state.setBoolean("justDied", justDied);
                    clip.state.setInt("hurtingWait", hurtingWait);
                    clip.state.setLong("hurtingTime", hurtingTime);
                });
            });

            jevar.createPrefab("collectible", 0, 0, collectibleSize, collectibleSize, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                JevaPrefab mainChar = (JevaPrefab) jevar.getJevaClip("mainChar");
                loaded_clip.state.setState("mainChar", mainChar);

                loaded_clip.props.shiftAnchorX(0.5);
                loaded_clip.props.shiftAnchorY(0.5);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    if (mainChar == null)
                        return;
                    Rectangle2D.Double charBounds = (Rectangle2D.Double) mainChar.state.getState("rd_bounds");
                    if (charBounds != null && clip.hitTest(charBounds)) {
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
                loaded_clip.setDepth(collectibleDepth);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    if (clip.state.getBoolean("giveReward")) {
                        // play sound
                        jevar.state.alterInt("score", clip.state.getInt("scoreIncrease"));
                        jevar.getSound("coin_collect_sfx").playOnce();
                        clip.remove();
                    }
                });
            });
            jevar.createPrefab("heart_drop", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("drop");
                loaded_clip.useSpriteSheet("heart_animation");
                loaded_clip.state.setInt("score", 10);
                loaded_clip.setDepth(collectibleDepth);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    if (clip.state.getBoolean("giveReward")) {
                        // play sound
                        if (jevar.state.getInt("health") < jevar.state.getInt("maxHealth")) {
                            JevaPrefab mainChar = (JevaPrefab) clip.state.getState("mainChar");
                            jevar.getSound("health_collected").playOnce();
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
                loaded_clip.extend("dynamic_sound");

                loaded_clip.state.setString("ds_targetClip", "mainChar");
                loaded_clip.state.setString("ds_soundSource", jevar.copySound("chest_near_sfx"));
                loaded_clip.state.setDouble("ds_innerRadius", loaded_clip.props._width * 4);
                loaded_clip.state.setDouble("ds_falloffDistance", loaded_clip.props._width * 8);

                loaded_clip.extend("collectible");
                loaded_clip.useGraphic("chest");
                loaded_clip.state.setBoolean("wasOpened", false);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    boolean wasOpened = clip.state.getBoolean("wasOpened");
                    if (!wasOpened && clip.state.getBoolean("touchingPlayer")) {
                        jevar.execFunc("baddie_drop_item", "coin_drop", clip.props, 50, 2);
                        jevar.execFunc("baddie_drop_item", "heart_drop", clip.props, 3);

                        wasOpened = true;
                        clip.useGraphic("chest_open");
                        // play sound
                        jevar.getSound("open_chest_sfx").playOnce();
                        jevar.getSound("chest_open_sfx").playOnce();
                        loaded_clip.state.setBoolean("ds_isActive", false);
                    }

                    clip.state.setBoolean("wasOpened", wasOpened);
                });
            });

            jevar.createPrefab("dynamic_sound", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                // https://forums.unrealengine.com/t/how-to-get-music-to-lower-in-volume-according-to-distance-from-player-to-an-object/464744/2
                loaded_clip.state.setDouble("ds_innerRadius", loaded_clip.props._width * 3);
                loaded_clip.state.setDouble("ds_falloffDistance", loaded_clip.props._width * 7);
                loaded_clip.state.setDouble("ds_maxVolume", 0.6);
                loaded_clip.state.setBoolean("ds_playingClip", false);
                loaded_clip.state.setBoolean("ds_isActive", true);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    JevaPrefab target = (JevaPrefab) jevar.getJevaClip(clip.state.getString("ds_targetClip"));
                    JevaSound sound = jevar.getSound(clip.state.getString("ds_soundSource"));

                    double maxVolume = clip.state.getDouble("ds_maxVolume");

                    if (sound == null)
                        return;
                    if (!clip.state.getBoolean("ds_isActive")) {
                        if (clip.state.getBoolean("ds_playingClip")) {
                            clip.state.setBoolean("ds_playingClip", false);
                            sound.setVolume(0).stop();
                        }
                        return;
                    }

                    if (!clip.state.getBoolean("ds_playingClip")) {
                        clip.state.setBoolean("ds_playingClip", true);
                        sound.playLoop();
                    }

                    if (target == null) {
                        sound.setVolume(0);
                    } else {
                        double innerRadius = clip.state.getDouble("ds_innerRadius");
                        double falloffDistance = clip.state.getDouble("ds_falloffDistance");

                        double xDistance = clip.props._x - target.props._x;
                        double yDistance = clip.props._y - target.props._y;

                        double dis = Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                        if (dis < innerRadius) {
                            sound.setVolume(maxVolume);
                        } else if (dis > falloffDistance) {
                            sound.setVolume(0);
                        } else {
                            double range = falloffDistance - innerRadius;
                            dis -= innerRadius;
                            sound.setVolume(((range - dis) / range) * maxVolume);
                        }
                    }
                });
                loaded_clip.addUnload((unload_clip) -> {
                    JevaPrefab clip = (JevaPrefab) unload_clip;
                    if (clip.state.getBoolean("ds_playingClip")) {
                        JevaSound sound = jevar.getSound(clip.state.getString("ds_soundSource"));
                        sound.setVolume(0).stop();
                    }
                });
            });

            jevar.createPrefab("drop", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                loaded_clip.props._width = 60;
                loaded_clip.props._height = 60;

                loaded_clip.extend("collectible").extend("ragdoll");

                loaded_clip.state.setLong("timeSpawned", jevar.currentClockMillis());
                loaded_clip.state.setInt("actionDelay", 500);
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

                // https://gamedev.stackexchange.com/questions/44547/how-do-i-create-2d-water-with-dynamic-waves

                // Resolution of simulation
                int NUM_POINTS = (int) (map._tileWidth * 1.5);
                // Width of simulation
                double WIDTH = loaded_clip.props._width + tileSize * 2;
                // Spring constant for forces applied by adjacent points
                double SPRING_CONSTANT = 0.01;
                // Sprint constant for force applied to baseline
                double SPRING_CONSTANT_BASELINE = 0.01;
                // Vertical draw offset of simulation
                final double Y_OFFSET = 100;
                // Damping to apply to speed changes
                double DAMPING = 0.9858;
                // Number of iterations of point-influences-point to do on wave per step
                // (this makes the waves animate faster)
                int ITERATIONS = 4;

                double POINTS_OFFSET = (1.0 / NUM_POINTS) * WIDTH;

                jevar.createFunc("makeWavePoints", (state, arg) -> {
                    int numPoints = (int) arg[0];
                    ArrayList<WavePoint> wavePoints = new ArrayList<>();
                    for (int n = 0; n < numPoints; n++) {
                        // This represents a point on the wave
                        WavePoint newPoint = new WavePoint(-10 + n * POINTS_OFFSET, Y_OFFSET, 0, 1);
                        wavePoints.add(newPoint);
                    }
                    return wavePoints;
                });

                jevar.createFunc("splashWave", (state, arg) -> {
                    WavePoint closestPoint = null;
                    @SuppressWarnings("unchecked")
                    ArrayList<WavePoint> wavePoints = (ArrayList<GameApp4.WavePoint>) loaded_clip.state
                            .getState("wavePoints");
                    double closestDistance = -1;
                    int closestN = -1;
                    double xPos = (double) arg[0];
                    double yForce = (double) arg[1];
                    for (var n = 0; n < wavePoints.size(); n++) {
                        WavePoint p = wavePoints.get(n);
                        double distance = Math.abs((int) (xPos - p.x));
                        if (closestDistance == -1) {
                            closestPoint = p;
                            closestDistance = distance;
                            closestN = n;
                        } else if (distance <= closestDistance) {
                            closestPoint = p;
                            closestDistance = distance;
                            closestN = n;
                        }

                    }
                    closestPoint.y += yForce;

                    wavePoints.set(closestN, closestPoint);
                    loaded_clip.state
                            .setState("wavePoints", wavePoints);

                    return null;
                });

                // A phase difference to apply to each sine
                loaded_clip.state.setInt("offset", 0);

                int NUM_BACKGROUND_WAVES = 3;
                double BACKGROUND_WAVE_MAX_HEIGHT = 6;
                double BACKGROUND_WAVE_COMPRESSION = 1.0 / 12;
                // Amounts by which a particular sine is offset
                ArrayList<Double> sineOffsets = new ArrayList<>();
                // Amounts by which a particular sine is amplified
                ArrayList<Double> sineAmplitudes = new ArrayList<>();
                // Amounts by which a particular sine is stretched
                ArrayList<Double> sineStretches = new ArrayList<>();
                // Amounts by which a particular sine's offset is multiplied
                ArrayList<Double> offsetStretches = new ArrayList<>();
                // Set each sine's values to a reasonable random value
                for (int i = 0; i < NUM_BACKGROUND_WAVES; i++) {
                    double sineOffset = -Math.PI + 2 * Math.PI * Math.random();
                    sineOffsets.add(sineOffset);
                    double sineAmplitude = Math.random() * BACKGROUND_WAVE_MAX_HEIGHT;
                    sineAmplitudes.add(sineAmplitude);
                    double sineStretch = Math.random() * BACKGROUND_WAVE_COMPRESSION;
                    sineStretches.add(sineStretch);
                    double offsetStretch = Math.random() * BACKGROUND_WAVE_COMPRESSION;
                    offsetStretches.add(offsetStretch);
                }

                jevar.createFunc("overlapSines", (state, arg) -> {
                    double x = (double) arg[0];
                    int offset = (int) arg[1];
                    double result = 0;
                    for (int i = 0; i < NUM_BACKGROUND_WAVES; i++) {
                        result = result
                                + sineOffsets.get(i)
                                + sineAmplitudes.get(i)
                                        * Math.sin(x * sineStretches.get(i) + offset * offsetStretches.get(i));

                    }
                    return result;
                });

                @SuppressWarnings("unchecked")
                ArrayList<WavePoint> initWavePoints = (ArrayList<GameApp4.WavePoint>) jevar.execFunc("makeWavePoints",
                        NUM_POINTS);
                loaded_clip.state.setState("wavePoints", initWavePoints);

                jevar.createFunc("updateWavePoints", (state, arg) -> {
                    @SuppressWarnings("unchecked")
                    ArrayList<WavePoint> points = (ArrayList<WavePoint>) arg[0];
                    double dt = (double) arg[1];

                    for (int i = 0; i < ITERATIONS; i++) {

                        for (int n = 0; n < points.size(); n++) {
                            WavePoint p = points.get(n);
                            // force to apply to this point
                            double force = 0;

                            // forces caused by the point immediately to the left or the right
                            double forceFromLeft, forceFromRight;

                            if (n == 0) { // wrap to left-to-right
                                double dy = points.get(points.size() - 1).y - p.y;
                                forceFromLeft = SPRING_CONSTANT * dy;
                            } else { // normally
                                double dy = points.get(n - 1).y - p.y;
                                forceFromLeft = SPRING_CONSTANT * dy;
                            }
                            if (n == points.size() - 1) { // wrap to right-to-left
                                double dy = points.get(0).y - p.y;
                                forceFromRight = SPRING_CONSTANT * dy;
                            } else { // normally
                                double dy = points.get(n + 1).y - p.y;
                                forceFromRight = SPRING_CONSTANT * dy;
                            }

                            // Also apply force toward the baseline
                            double dy = Y_OFFSET - p.y;
                            double forceToBaseline = SPRING_CONSTANT_BASELINE * dy;

                            // Sum up forces
                            force = force + forceFromLeft;
                            force = force + forceFromRight;
                            force = force + forceToBaseline;

                            // Calculate acceleration
                            double acceleration = force * 1.0 / p.mass;

                            // Apply acceleration (with damping)
                            p.spdy = DAMPING * p.spdy + acceleration;

                            // Apply speed
                            p.y = p.y + p.spdy;
                        }
                    }

                    return points;
                });

                loaded_clip.state.setLong("updateWaveTime", 0);
                loaded_clip.state.setInt("updateWaveInt", 50);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    clip.state.alterInt("offset", 1);
                    if (clip.state.getInt("offset") > jevar.meta.getScreenWidth())
                        clip.state.setInt("offset", 0);

                    if (jevar.currentClockMillis() - clip.state.getLong("updateWaveTime") > clip.state
                            .getInt("updateWaveInt")) {
                        clip.state.setLong("updateWaveTime", jevar.currentClockMillis());
                        @SuppressWarnings("unchecked")
                        ArrayList<WavePoint> wavePoints = (ArrayList<GameApp4.WavePoint>) clip.state
                                .getState("wavePoints");
                        @SuppressWarnings("unchecked")
                        ArrayList<WavePoint> newWavePoints = (ArrayList<GameApp4.WavePoint>) jevar
                                .execFunc("updateWavePoints", wavePoints, jevar.getDelta());
                        loaded_clip.state.setState("wavePoints", newWavePoints);
                    }
                });

                loaded_clip.usePainting((ctx, _x, _y, _width, _height, state) -> {

                    JevaVCam vcam = jevar.getVCam("mainCamera");

                    if (vcam == null)
                        return;
                    double camWidth = vcam.projection._width;
                    double camBaseX = vcam.projection._x - camWidth / 2;
                    double clipWidth = loaded_clip.props._width;
                    double clipBaseX = loaded_clip.props._x - clipWidth / 2;

                    int offsetXLeft = (int) (((camBaseX - clipBaseX) * 1.0) / POINTS_OFFSET);
                    int offsetXRight = offsetXLeft + 3 + (int) ((camWidth) * 1.0 / POINTS_OFFSET);

                    Polygon polyGon = new Polygon();
                    polyGon.addPoint((int) (_x + _width + 10), (int) (_y + _height + 10));
                    polyGon.addPoint((int) (_x - 10), (int) (_y + _height + 10));

                    @SuppressWarnings("unchecked")
                    ArrayList<WavePoint> wavePoints = (ArrayList<GameApp4.WavePoint>) state.getState("wavePoints");

                    int activeOffset = state.getInt("offset", 0);
                    int drawXLeft = Math.max(offsetXLeft, 0);
                    int drawXRight = Math.min(offsetXRight, wavePoints.size());

                    // Draw points and line
                    for (var n = drawXLeft; n < drawXRight; n++) {
                        var p = wavePoints.get(n);
                        double overlapSinesPX = (double) jevar.execFunc("overlapSines", p.x, activeOffset);

                        polyGon.addPoint((int) p.x,
                                (int) (p.y + overlapSinesPX));
                    }

                    ctx.setStroke(new BasicStroke(10));
                    ctx.setColor(new Color(123, 204, 255, 95));
                    // ctx.setColor(new Color(73, 154, 205, 95));
                    ctx.fillPolygon(polyGon);
                    ctx.setColor(new Color(167, 223, 255, 155));
                    // ctx.setColor(new Color(138, 194, 226, 155));
                    ctx.drawPolygon(polyGon);
                });

                loaded_clip.state.setLong("splashTimer", 0);
                loaded_clip.state.setInt("splashWait", 300);
                loaded_clip.state.setBoolean("canSplash", false);

                loaded_clip.props.shiftAnchorX(0.5);
                loaded_clip.props.shiftAnchorY(1);

                final JevaSound ambienceSound = jevar.getSound("underwater_ambience");

                ambienceSound.playLoop();

                loaded_clip.addUnload((state) -> {
                    ambienceSound.stop();
                });

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    long levelStartTime = jevar.state.getLong("levelStartTime");
                    int levelMaxTime = jevar.state.getInt("levelMaxTime");
                    long splashTimer = clip.state.getLong("splashTimer");
                    int splashWait = clip.state.getInt("splashWait");

                    double mapHeight = map.props._height;

                    clip.props._height = Math.min(((jevar.currentClockMillis() - levelStartTime)
                            * 1.0 / levelMaxTime), 1) * mapHeight;

                    JevaPrefab mainChar = (JevaPrefab) jevar.getJevaClip("mainChar");

                    if (mainChar != null) {
                        boolean charTouchingWater = clip.state.getBoolean("charTouchingWater");
                        boolean canSplash = clip.state.getBoolean("canSplash");
                        boolean charSubmerged = clip.state.getBoolean("charSubmerged");
                        double surfaceY = clip.props._y - clip.props._height + Y_OFFSET;
                        double boundX = (clip.props._x - clip.props._width / 2);
                        double exitWaterSplashForce = -60;
                        double enterWaterSplashForce = 50;
                        double stepSplashForce = 30;
                        if (surfaceY < mainChar.props._y && !charTouchingWater) {
                            charTouchingWater = true;
                            jevar.execFunc("splashWave", mainChar.props._x - boundX, enterWaterSplashForce);
                            jevar.getSound("drop_water_splash").playOnce();
                        }
                        if (surfaceY > mainChar.props._y && charTouchingWater) {
                            charTouchingWater = false;
                            jevar.execFunc("splashWave", mainChar.props._x - boundX, exitWaterSplashForce);
                            jevar.getSound("drop_water_splash").playOnce();
                        }
                        if (charTouchingWater && !charSubmerged && canSplash && (mainChar.state.getBoolean("goingLeft")
                                || mainChar.state.getBoolean("goingRight"))) {
                            canSplash = false;
                            splashTimer = jevar.currentClockMillis();
                            jevar.execFunc("splashWave", mainChar.props._x - boundX, stepSplashForce);
                            jevar.getSound("step_splash_sfx").playOnce();
                        }
                        if (!canSplash && (jevar.currentClockMillis() - splashTimer > splashWait)) {
                            canSplash = true;
                        }
                        if (surfaceY < (mainChar.props._y - (mainChar.props._height * 0.8))
                                && !charSubmerged) {
                            charSubmerged = true;
                            mainChar.state.setBoolean("isUnderwater", charSubmerged);
                            ambienceSound.setVolume(0.8);
                        }
                        if (surfaceY > (mainChar.props._y - (mainChar.props._height * 0.8))
                                && charSubmerged) {
                            charSubmerged = false;
                            mainChar.state.setBoolean("isUnderwater", charSubmerged);
                            ambienceSound.setVolume(0.2);
                        }
                        clip.state.setBoolean("charTouchingWater", charTouchingWater);
                        clip.state.setBoolean("canSplash", canSplash);
                        clip.state.setBoolean("charSubmerged", charSubmerged);
                        clip.state.setLong("splashTimer", splashTimer);
                    }
                });
            });

            jevar.createFunc("newGame", (state, arg) -> {
                state.setInt("currLevel", 0);
                state.setLong("timeStartedGame", jevar.currentClockMillis());
                state.setInt("score", 0);
                state.setInt("health", state.getInt("maxHealth"));
                jevar.useScene("gameScene", true);

                return null;
            });

            jevar.createFunc("nextLevel", (state, arg) -> {
                jevar.setTimeScale(1);
                int currLevel = state.getInt("currLevel");

                if (currLevel < worldMap.length) {
                    currLevel++;
                    state.setInt("currLevel", currLevel);
                    jevar.useScene("gameScene", true);
                } else {
                    jevar.useScene("highscoresScene", true);
                }

                return null;
            });

            jevar.createPrefab("door", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("collectible");
                loaded_clip.useGraphic("door_tile_set");
                loaded_clip.state.setBoolean("playerObtained", false);
                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    if (clip.state.getBoolean("touchingPlayer") && !clip.state.getBoolean("playerObtained")) {
                        // play sound
                        jevar.getSound("level_win_sfx").playOnce();
                        jevar.getJevaClip("mainChar").state.setState("wonGame", true);
                        jevar.setTimeScale(0.25);
                        clip.state.setBoolean("playerObtained", true);
                        jevar.setTimeout("nextLevel", 500);
                    }
                });
            });

            jevar.createPrefab("info_sign", 0, 0, tileSize, tileSize, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.setDepth(collectibleDepth);
                loaded_clip.useGraphic("info_sign");
            });

            jevar.createJevascript("updateScreenDimensions", (self) -> {
                JevaClip clip = (JevaClip) self;
                clip.props._width = jevar.meta.getScreenWidth();
                clip.props._height = jevar.meta.getScreenHeight();
            });

            jevar.createPrefab("info_manager", 0, 0, jevar.meta.getScreenWidth(), jevar.meta.getScreenHeight(),
                    (loaded_self) -> {
                        JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                        loaded_clip.addJevascript("updateScreenDimensions");

                        loaded_clip.useGraphic("air");
                        loaded_clip.props._visible = false;
                        JevaText infoText = (JevaText) loaded_clip.addText("Information Text here",
                                0, 0,
                                jevar.meta.getScreenWidth(), 60);
                        infoText.props.setAlign("c");
                        infoText.props.setFontSize(40);
                        infoText.props.setAnchorX(0);
                        infoText.props.setAnchorY(0);
                        infoText.props._backgroundColor = new Color(30, 22, 22, 70);

                        loaded_clip.addJevascript((self) -> {
                            JevaPrefab clip = (JevaPrefab) self;

                            JevaPrefab mainChar = (JevaPrefab) jevar.getJevaClip("mainChar");

                            infoText.props._width = jevar.meta.getScreenWidth();

                            if (mainChar == null) {
                                clip.props._visible = false;
                                return;
                            }

                            JevaPrefab infoSign = (JevaPrefab) mainChar.hitTestGet("info_sign");
                            if (infoSign == null) {
                                clip.props._visible = false;
                            } else {
                                String note = infoSign.state.getString("info_message");
                                infoText.props._text = "Note: " + note;
                                clip.props._visible = true;
                            }
                        });
                    });

            jevar.createPrefab("overlay", 0, 0, jevar.meta.getScreenWidth(), jevar.meta.getScreenHeight(),
                    (loaded_self) -> {
                        JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                        loaded_clip.setDepth(overlayDepth);

                        loaded_clip.addJevascript("updateScreenDimensions");

                        JevaVCam vcam = jevar.getVCam("mainCamera");

                        loaded_clip.usePainting((ctx, _x, _y, _width, _height, state) -> {
                            ctx.setColor(new Color(0, 0, 0, 120));
                            ctx.fillRect(0, jevar.meta.getScreenHeight() - 50, 560, 50);
                            ctx.fillRect(jevar.meta.getScreenWidth() - 560, jevar.meta.getScreenHeight() - 50, 560, 50);
                        });

                        loaded_clip.addText("Health: ", 10, jevar.meta.getScreenHeight() - 50, 150, 50, (ht_tx) -> {
                            JevaText ht_cp = (JevaText) ht_tx;

                            ht_cp.addJevascript((score_clip_script) -> {
                                ht_cp.props._y = jevar.meta.getScreenHeight() - 50;
                            });
                        });

                        loaded_clip.addText("Level: 0", jevar.meta.getScreenWidth() - 550,
                                jevar.meta.getScreenHeight() - 50, 200, 50, (loaded_score) -> {
                                    JevaText score_clip = (JevaText) loaded_score;

                                    score_clip.addJevascript((score_clip_script) -> {
                                        score_clip.props._x = jevar.meta.getScreenWidth() - 550;
                                        score_clip.props._y = jevar.meta.getScreenHeight() - 50;
                                        score_clip.props._text = "Level: " + (jevar.state.getInt("currLevel") + 1);
                                    });
                                });
                        loaded_clip.addPrefab(jevar.meta.getScreenWidth() - 350, jevar.meta.getScreenHeight() - 50, 60,
                                60, (cgc) -> {
                                    JevaPrefab coin_graphic_clip = (JevaPrefab) cgc;

                                    coin_graphic_clip.addJevascript((clp) -> {
                                        coin_graphic_clip.props._x = jevar.meta.getScreenWidth() - 350;
                                        coin_graphic_clip.props._y = jevar.meta.getScreenHeight() - 50;
                                    });

                                    coin_graphic_clip.useSpriteSheet("coin_animation");
                                });
                        loaded_clip.addText("0", jevar.meta.getScreenWidth() - 290, jevar.meta.getScreenHeight() - 50,
                                300, 50, (loaded_score) -> {
                                    JevaText score_clip = (JevaText) loaded_score;

                                    score_clip.addJevascript((score_clip_script) -> {
                                        score_clip.props._x = jevar.meta.getScreenWidth() - 290;
                                        score_clip.props._y = jevar.meta.getScreenHeight() - 50;
                                        score_clip.props._text = "" + jevar.state.getInt("score");
                                    });
                                });

                        loaded_clip.addPrefab(150, jevar.meta.getScreenHeight() - 50, 400, 50, (l) -> {
                            JevaPrefab health_clip = (JevaPrefab) l;

                            Image heartFullImg = jevar.getImage("life_full");
                            Image heartEmptyImg = jevar.getImage("life_empty");
                            int gap = 10;
                            int heartSize = (int) (health_clip.props._height * 0.6);
                            int xOffset = 0;

                            health_clip.usePainting((ctx, _x, _y, _width, _height, state) -> {
                                int health = jevar.state.getInt("health");
                                int maxHealth = jevar.state.getInt("maxHealth");
                                for (int i = 0; i < health; i++) {
                                    ctx.drawImage(heartFullImg, xOffset + (int) _x + ((heartSize + gap) * i),
                                            gap + (int) _y,
                                            (int) heartSize, (int) (heartSize), null);
                                }
                                for (int i = health; i < maxHealth; i++) {
                                    ctx.drawImage(heartEmptyImg, xOffset + (int) _x + ((heartSize + gap) * i),
                                            gap + (int) _y,
                                            (int) heartSize, (int) (heartSize), null);
                                }
                            });

                            health_clip.addJevascript((clp) -> {
                                health_clip.props._y = jevar.meta.getScreenHeight() - 50;
                            });
                        });
                        loaded_clip.addPrefab(150, jevar.meta.getScreenHeight() - 100, 410, 50, (l) -> {
                            JevaPrefab breath_clip = (JevaPrefab) l;

                            breath_clip.props.shiftAnchorX(0.5);

                            Image breathFullImg = jevar.getImage("bubble_full");
                            Image breathEmptyImg = jevar.getImage("bubble_empty");
                            int bubbleSize = (int) (breath_clip.props._height * 0.6);
                            int bubbleDistance = 10;

                            breath_clip.addJevascript((clp) -> {
                                breath_clip.props._y = jevar.meta.getScreenHeight() - 100;
                            });

                            breath_clip.usePainting((ctx, _x, _y, _width, _height, state) -> {
                                JevaPrefab mainChar = (JevaPrefab) jevar.getJevaClip("mainChar");

                                if (mainChar == null)
                                    return;

                                int breath = mainChar.state.getInt("breath");
                                int maxBreath = mainChar.state.getInt("maxBreath");

                                if (breath == maxBreath)
                                    return;

                                ctx.setColor(new Color(0, 0, 0, 120));
                                ctx.fillRect((int) _x, (int) _y, (int) _width, (int) _height);

                                int bubblesWidth = (bubbleSize * maxBreath) + (bubbleDistance * (maxBreath - 1));
                                int bubblesOffset = (int) (_width / 2) - (bubblesWidth / 2);
                                for (int i = 0; i < breath; i++) {
                                    ctx.drawImage(breathFullImg,
                                            bubblesOffset + (int) _x + ((bubbleSize + bubbleDistance) * i),
                                            bubbleDistance + (int) _y,
                                            (int) bubbleSize, (int) (bubbleSize), null);
                                }
                                for (int i = breath; i < maxBreath; i++) {
                                    ctx.drawImage(breathEmptyImg,
                                            bubblesOffset + (int) _x + ((bubbleSize + bubbleDistance) * i),
                                            bubbleDistance + (int) _y,
                                            (int) bubbleSize, (int) (bubbleSize), null);
                                }
                            });
                        });

                        loaded_clip.addPrefab("info_manager");

                        loaded_clip.addJevascript((self) -> {
                            JevaPrefab clip = (JevaPrefab) self;

                            if (vcam == null)
                                return;

                            clip.props._x = vcam.projection._x - vcam.projection._width / 2;
                            clip.props._y = vcam.projection._y - vcam.projection._height / 2;
                        });
                    });

            jevar.createPrefab("ragdoll", jevar.meta.getScreenWidth() / 2, 350, 100, 90, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("hurtable");

                loaded_clip.state.setBoolean("canClimbLadder", false);
                loaded_clip.state.setBoolean("touchingLadder", false);
                loaded_clip.state.setBoolean("climbingLadder", false);
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
                loaded_clip.state.setInt("heldLadderAscendYSpeed", -500);
                loaded_clip.state.setInt("heldLadderDescendYSpeed", 500);

                loaded_clip.state.setInt("coyoteWait", 150);
                loaded_clip.state.setLong("coyoteTime", 0);
                loaded_clip.state.setInt("jumpBufferWait", 100);
                loaded_clip.state.setLong("jumpBufferTime", 0);
                loaded_clip.state.setInt("delayJumpWait", 200);
                loaded_clip.state.setLong("delayJumpTime", 0);

                JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");

                loaded_clip.props.shiftAnchorX(0.5);
                loaded_clip.props.shiftAnchorY(1);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    long timeNow = jevar.currentClockMillis();

                    boolean isHurt = clip.state.getBoolean("isHurt");

                    boolean canClimbLadder = clip.state.getBoolean("canClimbLadder");
                    boolean touchingLadder = clip.state.getBoolean("touchingLadder");
                    boolean climbingLadder = clip.state.getBoolean("climbingLadder");
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

                    boolean isAlive = clip.state.getBoolean("isAlive");

                    boolean usesGravity = clip.state.getBoolean("usesGravity");
                    double grav = clip.state.getDouble("grav");
                    double gravity = clip.state.getDouble("gravity");
                    double playerSpeed = clip.state.getDouble("playerSpeed");
                    double fallingSpeed = clip.state.getDouble("fallingSpeed");
                    int maxXSpeed = clip.state.getInt("maxXSpeed");
                    int maxFallSpeed = clip.state.getInt("maxFallSpeed");
                    int jumpYSpeed = clip.state.getInt("jumpYSpeed");
                    int heldJumpYSpeed = clip.state.getInt("heldJumpYSpeed");
                    int heldLadderAscendYSpeed = clip.state.getInt("heldLadderAscendYSpeed");
                    int heldLadderDescendYSpeed = clip.state.getInt("heldLadderDescendYSpeed");

                    long coyoteTime = clip.state.getLong("coyoteTime");
                    int coyoteWait = clip.state.getInt("coyoteWait");
                    long jumpBufferTime = clip.state.getLong("jumpBufferTime");
                    int jumpBufferWait = clip.state.getInt("jumpBufferWait");
                    long delayJumpTime = clip.state.getLong("delayJumpTime");
                    int delayJumpWait = clip.state.getInt("delayJumpWait");

                    String defaultAnim = clip.state.getString("rd_default_anim");
                    String idleAnim = clip.state.getString("rd_idle_anim");
                    String jumpAnim = clip.state.getString("rd_jump_anim");
                    String walkAnim = clip.state.getString("rd_walk_anim");
                    String hurtAnim = clip.state.getString("rd_hurt_anim");
                    String climbUpAnim = clip.state.getString("rd_climbup_anim");
                    String climbDownAnim = clip.state.getString("rd_climbdown_anim");
                    String idleclimbAnim = clip.state.getString("rd_idleclimb_anim");
                    String fallAnim = clip.state.getString("rd_fall_anim");
                    String swimAnim = clip.state.getString("rd_swim_anim");
                    String deadAnim = clip.state.getString("rd_dead_anim");

                    if (!touchingFloor && !climbingLadder) {
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

                    if (!climbingLadder && touchingWater) {
                        canJump = true;
                    }

                    if (goingLeft) {
                        if (isAlive)
                            if (!touchingLeftWall)
                                clip.props._x -= playerSpeed;
                        if (clip.props._scaleX > 0)
                            clip.props._scaleX *= -1;
                    }
                    if (goingRight) {
                        if (isAlive)
                            if (!touchingRightWall)
                                clip.props._x += playerSpeed;
                        if (clip.props._scaleX < 0)
                            clip.props._scaleX *= -1;
                    }
                    if (goingUp) {
                        jumpBufferTime = timeNow;

                        if (canClimbLadder && touchingLadder && !climbingLadder) {
                            climbingLadder = true;
                        } else if (!climbingLadder && touchingWater) {
                            holdingJump = true;
                            grav = heldJumpYSpeed;
                            delayJumpTime = timeNow;
                            climbingLadder = false;
                        }
                        if (climbingLadder) {
                            grav = heldLadderAscendYSpeed;
                        }
                    }
                    if (goingDown) {
                        if (canClimbLadder && touchingLadder && !climbingLadder) {
                            climbingLadder = true;
                        }
                        if (climbingLadder) {
                            grav = heldLadderDescendYSpeed;
                        }
                    }

                    if (!climbingLadder && !touchingWater && canJump && (timeNow - jumpBufferTime) < jumpBufferWait) {
                        if (clip.getInstanceName() == "mainChar")
                            jevar.getSound("jump_sfx").playOnce();
                        grav = jumpYSpeed;
                        canJump = false;

                        if (goingUp) {
                            holdingJump = true;
                            delayJumpTime = timeNow;
                        }
                    }

                    if (!climbingLadder && holdingJump) {
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
                        String[] ladderTypes = new String[] { "ladder", "ladder_floor" };
                        String[] floorTypes = new String[] { "floor", "ladder_floor" };
                        if (map.hitTest(clip.props._x - widthX, clip.props._y - heightY * 0.05, ladderTypes)
                                || map.hitTest(clip.props._x + widthX, clip.props._y - heightY * 0.05, ladderTypes)
                                || map.hitTest(clip.props._x + offsetX, clip.props._y, ladderTypes)) {
                            touchingLadder = true;
                        } else {
                            touchingLadder = false;
                            climbingLadder = false;
                        }
                        if (map.hitTest(clip.props._x - offsetX, clip.props._y, floorTypes)
                                || map.hitTest(clip.props._x + offsetX, clip.props._y, floorTypes)) {
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
                        if (map.hitTest(clip.props._x - widthX, clip.props._y - heightY * 0.05, "floor")
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

                    if (!isAlive) {
                        climbingLadder = false;
                        touchingLadder = false;
                        touchingWater = false;
                        holdingJump = false;
                    }

                    if (touchingFloor && !touchingLadder) {
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

                    if (!isAlive) {
                        clip.useSpriteSheet(deadAnim, 1);
                    } else if (!clip.state.getBoolean("reserveAnim")) {
                        if (clip.state.getBoolean("playingHurt")) {
                            clip.useSpriteSheet(hurtAnim, 1);
                        } else if (climbingLadder) {
                            // char is climbing
                            if (goingUp)
                                clip.useSpriteSheet(climbUpAnim);
                            else if (goingDown)
                                clip.useSpriteSheet(climbDownAnim);
                            else
                                clip.useSpriteSheet(idleclimbAnim);
                        } else if (holdingJump || grav < 0) {
                            if (touchingWater) {
                                // char is swimming up
                                clip.useSpriteSheet(swimAnim);
                            } else {
                                // char is jumping
                                clip.useSpriteSheet(jumpAnim, 1);
                            }
                        } else if (grav > 0 && !touchingFloor) {
                            if (touchingWater) {
                                // char is swimming down
                                clip.useSpriteSheet(swimAnim);
                            } else {
                                // char is falling
                                clip.useSpriteSheet(fallAnim, 1);
                            }
                        } else if (goingLeft || goingRight) {
                            if (touchingWater && (touchingCeiling || grav < 0)) {
                                // char is swimming
                                clip.useSpriteSheet(swimAnim);
                            } else {
                                // char is walking
                                clip.useSpriteSheet(walkAnim);
                            }
                        } else {
                            // char is idle
                            clip.useSpriteSheet(idleAnim);
                        }
                    }

                    clip.state.setBoolean("touchingLadder", touchingLadder);
                    clip.state.setBoolean("climbingLadder", climbingLadder);
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

                loaded_clip.props._scaleX = 1.25;
                loaded_clip.props._scaleY = 1.25;

                loaded_clip.state.setBoolean("canSwim", true);
                loaded_clip.state.setBoolean("canClimbLadder", true);
                loaded_clip.state.setInt("hurtingWait", 600);
                loaded_clip.state.setInt("maxHealth", jevar.state.getInt("maxHealth"));
                loaded_clip.state.setInt("health", jevar.state.getInt("health"));
                loaded_clip.state.setInt("maxBreath", 10);
                loaded_clip.state.setInt("breath", loaded_clip.state.getInt("maxBreath"));
                loaded_clip.state.setBoolean("wonGame", false);
                loaded_clip.state.setBoolean("isUnderwater", false);

                loaded_clip.state.setInt("breathWait", 500);
                loaded_clip.state.setLong("breathTime", 0);

                loaded_clip.state.setString("hb_diedSound", "game_over_sfx");
                loaded_clip.state.setString("hb_hurtSound", "player_hit_sfx");

                loaded_clip.state.setString("rd_default_anim", "char_idle_animation");
                loaded_clip.state.setString("rd_idle_anim", "char_idle_animation");
                loaded_clip.state.setString("rd_jump_anim", "char_jump_animation");
                loaded_clip.state.setString("rd_walk_anim", "char_run_animation");
                loaded_clip.state.setString("rd_hurt_anim", "char_hurt_animation");
                loaded_clip.state.setString("rd_climbup_anim", "char_climbup_animation");
                loaded_clip.state.setString("rd_climbdown_anim", "char_climbdown_animation");
                loaded_clip.state.setString("rd_idleclimb_anim", "char_idleclimb_animation");
                loaded_clip.state.setString("rd_fall_anim", "char_fall_animation");
                loaded_clip.state.setString("rd_shoot_anim", "char_shoot_animation");
                loaded_clip.state.setString("rd_swim_anim", "char_swim_animation");
                loaded_clip.state.setString("rd_dead_anim", "char_dead_animation");

                loaded_clip.state.setLong("stepTimer", 0);
                loaded_clip.state.setInt("stepWait", 300);
                loaded_clip.state.setBoolean("canStep", false);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    jevar.state.setInt("health", clip.state.getInt("health"));

                    boolean wonGame = clip.state.getBoolean("wonGame");

                    boolean isUnderwater = clip.state.getBoolean("isUnderwater");
                    boolean touchingFloor = clip.state.getBoolean("touchingFloor");
                    int breathWait = clip.state.getInt("breathWait");
                    long breathTime = clip.state.getLong("breathTime");
                    int breath = clip.state.getInt("breath");
                    int maxBreath = clip.state.getInt("maxBreath");
                    String shootAnim = clip.state.getString("rd_shoot_anim");
                    String idleAnim = clip.state.getString("rd_idle_anim");

                    boolean charTouchingWater = clip.state.getBoolean("charTouchingWater");
                    boolean canStep = clip.state.getBoolean("canStep");
                    boolean charSubmerged = clip.state.getBoolean("charSubmerged");
                    long stepTimer = clip.state.getLong("stepTimer");
                    int stepWait = clip.state.getInt("stepWait");

                    if (wonGame) {
                        clip.state.setBoolean("reserveAnim", true);
                        clip.useSpriteSheet(idleAnim);
                        clip.state.setBoolean("goingLeft", false);
                        clip.state.setBoolean("goingRight", false);
                        clip.state.setBoolean("goingUp", false);
                        clip.state.setBoolean("goingDown", false);
                        return;
                    }

                    clip.state.setBoolean("goingLeft", (key.isDown(JevaKey.LEFT) || key.isDown(JevaKey.A)));
                    clip.state.setBoolean("goingRight", (key.isDown(JevaKey.RIGHT) || key.isDown(JevaKey.D)));
                    clip.state.setBoolean("goingUp", (key.isDown(JevaKey.UP) || key.isDown(JevaKey.W)));
                    clip.state.setBoolean("goingDown", (key.isDown(JevaKey.DOWN) || key.isDown(JevaKey.S)));

                    boolean movingLeftOrRight = (clip.state.getBoolean("goingLeft")
                            || clip.state.getBoolean("goingRight"));

                    if (!charTouchingWater && !charSubmerged && touchingFloor && canStep && movingLeftOrRight) {
                        canStep = false;
                        stepTimer = jevar.currentClockMillis();
                        jevar.getSound("step_sfx").playOnce();
                    }
                    if (!canStep && (jevar.currentClockMillis() - stepTimer > stepWait)) {
                        canStep = true;
                    }

                    clip.state.setBoolean("canStep", canStep);
                    clip.state.setLong("stepTimer", stepTimer);

                    if (key.isPressed("space") && !clip.state.getBoolean("playerShooting", false)) {
                        JevaScene currScene = jevar.getCurrentScene();

                        clip.state.setBoolean("playerShooting", true);
                        clip.state.setBoolean("reserveAnim", true);
                        jevar.getSound("player_shoot_sfx").playOnce();

                        clip.useSpriteSheet(shootAnim, 1, (state, arg) -> {
                            clip.state.setBoolean("reserveAnim", false);
                            return null;
                        });
                        JevaPrefab projectile = (JevaPrefab) currScene.addPrefab("player_projectile", clip.props._x,
                                clip.props._y - (clip.props._height), 40, 40);
                        if (clip.props._scaleX > 0)
                            projectile.state.setBoolean("goingRight", true);
                        else
                            projectile.state.setBoolean("goingLeft", true);

                        clip.state.setBoolean("playerShooting", false);
                    }

                    if (!clip.state.getBoolean("isAlive") && !clip.state.getBoolean("leftLevel")) {
                        clip.state.setBoolean("leftLevel", true);
                        jevar.setTimeScale(0.25);
                        jevar.setTimeout((state, arg) -> {
                            jevar.execFunc("calcHighScores");
                            jevar.useScene("highscoresScene", true);
                            jevar.setTimeScale(1);

                            return null;
                        }, 500);
                    }

                    if (jevar.currentClockMillis() - breathTime > breathWait) {
                        breathTime = jevar.currentClockMillis();
                        if (isUnderwater) {
                            if (breath > 0) {
                                breath--;
                            } else {
                                clip.state.setBoolean("gotHurt", true);
                            }
                        } else {
                            if (breath < maxBreath)
                                breath++;
                        }
                    }

                    Rectangle2D.Double charBounds = new Rectangle2D.Double(clip.props._x - (clip.props._width / 6),
                            clip.props._y - clip.props._height * 2 / 3,
                            clip.props._width / 3, clip.props._height / 3);
                    clip.state.setState("rd_bounds", charBounds);

                    JevaClip projectile = clip.hitTestGet("baddie_projectile");
                    if (projectile != null && charBounds != null && clip.state.getBoolean("canGetHurt")
                            && projectile.hitTest(charBounds)) {
                        clip.state.setBoolean("gotHurt", true);
                        projectile.remove();
                    }

                    clip.state.setLong("breathTime", breathTime);
                    clip.state.setInt("breath", breath);

                    if (vcam != null) {
                        JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");

                        if (map.props._width < vcam.projection._width) {
                            vcam.projection._x = map.props._x + map.props._width / 2;
                        } else {
                            vcam.projection._x = clip.props._x;
                            if (vcam.projection._x < map.props._x + vcam.projection._width / 2)
                                vcam.projection._x = map.props._x + vcam.projection._width / 2;
                            else if (vcam.projection._x > map.props._x + map.props._width -
                                    vcam.projection._width / 2)
                                vcam.projection._x = map.props._x + map.props._width - vcam.projection._width
                                        / 2;
                        }
                        if (map.props._height < vcam.projection._height) {
                            vcam.projection._y = map.props._y + map.props._height / 2;
                        } else {
                            vcam.projection._y = clip.props._y;
                            if (vcam.projection._y < map.props._y + vcam.projection._height / 2)
                                vcam.projection._y = map.props._y + vcam.projection._height / 2;
                            else if (vcam.projection._y > map.props._y + map.props._height
                                    - vcam.projection._height / 2)
                                vcam.projection._y = map.props._y + map.props._height -
                                        vcam.projection._height / 2;
                        }
                    }

                });
            });

            jevar.createPrefab("baddie_health", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                int startHealth = JevaUtils.randomInt(2, 5);

                loaded_clip.state.setInt("maxHealth", startHealth);
                loaded_clip.state.setInt("health", startHealth);
                int heartSize = 30;
                int gap = 10;
                Image heartFullImg = jevar.getImage("life_full");
                Image heartEmptyImg = jevar.getImage("life_empty");

                loaded_clip.addPrefab(0, -loaded_clip.props._height, heartSize, heartSize,
                        (loaded_hrt) -> {
                            JevaPrefab hrt_clip = (JevaPrefab) loaded_hrt;

                            hrt_clip.props.setAnchorX(0.5);
                            hrt_clip.props.setAnchorY(1);
                            hrt_clip.props._height = heartSize;
                            hrt_clip.usePainting((ctx, x, y, w, h, state) -> {
                                int maxHealth = loaded_clip.state.getInt("maxHealth");
                                int health = loaded_clip.state.getInt("health");
                                if (health == maxHealth || health == 0)
                                    return;
                                hrt_clip.props._width = (heartSize * maxHealth) + (gap * (maxHealth - 1));
                                for (int i = 0; i < health; i++) {
                                    ctx.drawImage(heartFullImg, (int) x + ((heartSize + gap) * i), (int) y,
                                            (int) heartSize,
                                            (int) (heartSize), null);
                                }
                                for (int i = health; i < maxHealth; i++) {
                                    ctx.drawImage(heartEmptyImg, (int) x + ((heartSize + gap) * i), (int) y,
                                            (int) heartSize,
                                            (int) (heartSize), null);
                                }
                            });
                        });
            });

            jevar.createPrefab("ground_baddie", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("collectible").extend("ragdoll").extend("baddie_health");

                loaded_clip.state.setBoolean("goingLeft", true);
                loaded_clip.state.setInt("maxXSpeed", 300);
                loaded_clip.useGraphic("ghost_baddie");

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

                        JevaClip projectile = clip.hitTestGet("player_projectile");
                        if (projectile != null && clip.state.getBoolean("canGetHurt")) {
                            clip.state.setBoolean("gotHurt", true);
                            projectile.remove();
                        }
                    }

                    clip.state.setBoolean("goingLeft", goingLeft);
                    clip.state.setBoolean("goingRight", goingRight);
                    clip.state.setBoolean("touchingLeftEdge", touchingLeftEdge);
                    clip.state.setBoolean("touchingRightEdge", touchingRightEdge);
                    clip.state.setBoolean("touchingLeftWall", touchingLeftWall);
                    clip.state.setBoolean("touchingRightWall", touchingRightWall);
                });
            });

            jevar.createPrefab("snake_baddie", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("ground_baddie").extend("dynamic_sound");

                loaded_clip.state.setString("ds_targetClip", "mainChar");
                loaded_clip.state.setString("ds_soundSource", jevar.copySound("bat_baddie_screech"));

                loaded_clip.state.setBoolean("goingLeft", true);
                loaded_clip.state.setInt("maxXSpeed", 200);
                loaded_clip.useSpriteSheet("snake_baddie_animation");

                JevaPrefab mainChar = (JevaPrefab) loaded_clip.state.getState("mainChar");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    if (!clip.state.getBoolean("isAlive")) {
                        clip.state.setBoolean("goingLeft", false);
                        clip.state.setBoolean("goingRight", false);
                        clip.state.setBoolean("ds_isActive", false);

                        if (!clip.state.getBoolean("gaveReward")) {
                            jevar.execFunc("baddie_drop_item", "coin_drop", clip.props, 25);

                            clip.state.setBoolean("gaveReward", true);
                        }
                        clip.remove();
                    }
                });
            });

            jevar.createPrefab("skele_baddie", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("ground_baddie").extend("dynamic_sound");

                loaded_clip.state.setString("ds_targetClip", "mainChar");
                loaded_clip.state.setString("ds_soundSource", jevar.copySound("skele_walk_sfx"));

                loaded_clip.state.setString("hb_diedSound", "skele_dead_sfx");
                loaded_clip.state.setString("hb_hurtSound", "skele_hit_sfx");

                loaded_clip.state.setBoolean("goingLeft", true);
                loaded_clip.state.setInt("maxXSpeed", 60);
                loaded_clip.useSpriteSheet("skele_walk_animation");

                loaded_clip.state.setBoolean("seeingChar", false);
                loaded_clip.state.setLong("shootTime", 0);
                loaded_clip.state.setInt("shootWait", 1500);

                JevaPrefab mainChar = (JevaPrefab) loaded_clip.state.getState("mainChar");

                final int sightDistance = 5;

                jevar.createFunc("baddie_drop_item", (state, arg) -> {
                    JevaScene currScene = jevar.getCurrentScene();

                    if (currScene == null)
                        return null;

                    String itemType = (String) arg[0];
                    JevaClipProps props = (JevaClipProps) arg[1];

                    double leftBound = props._x - (props._width * props.getAnchorX()) - collectibleSize / 2;
                    double topBound = props._y - (props._height * props.getAnchorY());
                    double rightBound = leftBound + props._width;
                    double bottomBound = topBound + props._height - collectibleSize;

                    if (itemType == "coin_drop") {
                        int minAmt = (arg.length > 3 && arg[3] != null) ? (int) arg[3] : 1;
                        int amt = JevaUtils.randomInt(2) + minAmt;
                        int scoreIncrease = (arg.length > 2 && arg[2] != null) ? (int) arg[2] : 10;
                        for (int i = 0; i < amt; i++) {
                            double xPos = JevaUtils.randomDouble(leftBound, rightBound);
                            double yPos = JevaUtils.randomDouble(topBound, bottomBound);
                            currScene.addPrefab("coin_drop", xPos, yPos).state.setInt("scoreIncrease", scoreIncrease);
                        }
                    } else if (itemType == "heart_drop") {
                        int amt = JevaUtils.randomInt((arg.length > 2 && arg[2] != null) ? (int) arg[3] : 1);
                        for (int i = 0; i < amt; i++) {
                            double xPos = JevaUtils.randomDouble(leftBound, rightBound);
                            double yPos = JevaUtils.randomDouble(topBound, bottomBound);
                            currScene.addPrefab("heart_drop", xPos, yPos);
                        }
                    }

                    return null;
                });

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    boolean seeingChar = clip.state.getBoolean("seeingChar");
                    long shootTime = clip.state.getLong("shootTime");
                    int shootWait = clip.state.getInt("shootWait");

                    if (!clip.state.getBoolean("isAlive")) {
                        clip.useSpriteSheet("skele_dead_animation", 1);
                        clip.state.setBoolean("goingLeft", false);
                        clip.state.setBoolean("goingRight", false);
                        clip.state.setBoolean("ds_isActive", false);

                        if (!clip.state.getBoolean("gaveReward")) {
                            jevar.execFunc("baddie_drop_item", "coin_drop", clip.props, 25);
                            clip.state.setBoolean("gaveReward", true);
                        }
                    } else {
                        JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");

                        if (map != null && mainChar != null) {
                            int myTileY = map.pixelsToTilesY(clip.props._y - clip.props._height / 2);
                            int myTileX = map.pixelsToTilesX(clip.props._x);
                            int charTileY = map.pixelsToTilesY(mainChar.props._y - mainChar.props._height / 2);
                            int charTileX = map.pixelsToTilesX(mainChar.props._x);

                            if (myTileY == charTileY) {

                                int reach = myTileX + (int) (sightDistance * clip.props._scaleX);

                                int rangeLeft = Math.min(reach, myTileX);
                                int rangeRight = Math.max(reach, myTileX);

                                if (charTileX >= rangeLeft && charTileX <= rangeRight) {
                                    seeingChar = true;
                                } else {
                                    seeingChar = false;
                                }
                            } else {
                                seeingChar = false;
                            }

                            if (seeingChar && jevar.currentClockMillis() - shootTime > shootWait
                                    && Math.random() > 0.9) {
                                shootTime = jevar.currentClockMillis();

                                JevaScene currScene = jevar.getCurrentScene();

                                jevar.getSound("baddie_shoot_sfx").playOnce();

                                JevaPrefab projectile = (JevaPrefab) currScene.addPrefab("baddie_projectile",
                                        clip.props._x, clip.props._y - (clip.props._height), 40, 40);
                                if (clip.props._scaleX > 0)
                                    projectile.state.setBoolean("goingRight", true);
                                else
                                    projectile.state.setBoolean("goingLeft", true);
                            }
                        }

                    }

                    clip.state.setBoolean("seeingChar", seeingChar);
                    clip.state.setLong("shootTime", shootTime);
                });
            });

            jevar.createPrefab("bat_baddie", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("collectible").extend("ragdoll").extend("dynamic_sound").extend("baddie_health");

                loaded_clip.state.setString("ds_targetClip", "mainChar");
                loaded_clip.state.setString("ds_soundSource", jevar.copySound("bat_baddie_screech"));

                loaded_clip.state.setBoolean("goingLeft", true);
                loaded_clip.state.setBoolean("usesGravity", false);
                loaded_clip.state.setInt("maxXSpeed", 250);
                loaded_clip.useSpriteSheet("bat_baddie_fly_animation");

                JevaPrefab mainChar = (JevaPrefab) loaded_clip.state.getState("mainChar");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    boolean isAlive = clip.state.getBoolean("isAlive");
                    boolean goingLeft = clip.state.getBoolean("goingLeft");
                    boolean goingRight = clip.state.getBoolean("goingRight");
                    boolean touchingLeftWall = clip.state.getBoolean("touchingLeftWall");
                    boolean touchingRightWall = clip.state.getBoolean("touchingRightWall");

                    if (isAlive) {
                        if (goingLeft && touchingLeftWall) {
                            goingLeft = false;
                            goingRight = true;
                        }
                        if (goingRight && touchingRightWall) {
                            goingRight = false;
                            goingLeft = true;
                        }

                        if (clip.state.getBoolean("touchingPlayer")) {
                            mainChar.state.setBoolean("gotHurt", true);
                        }

                        JevaClip projectile = clip.hitTestGet("player_projectile");
                        if (projectile != null && clip.state.getBoolean("canGetHurt")) {
                            clip.state.setBoolean("gotHurt", true);
                            projectile.remove();
                        }
                    } else {
                        goingLeft = false;
                        goingRight = false;
                        clip.state.setBoolean("usesGravity", true);
                        clip.useSpriteSheet("bat_baddie_die_animation", 1);
                        clip.state.setBoolean("ds_isActive", false);

                        if (!clip.state.getBoolean("gaveReward")) {
                            jevar.execFunc("baddie_drop_item", "coin_drop", clip.props, 25);
                            clip.state.setBoolean("gaveReward", true);
                        }
                    }

                    clip.state.setBoolean("goingLeft", goingLeft);
                    clip.state.setBoolean("goingRight", goingRight);
                    clip.state.setBoolean("touchingLeftWall", touchingLeftWall);
                    clip.state.setBoolean("touchingRightWall", touchingRightWall);
                });
            });

            jevar.createPrefab("projectile", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("ragdoll");

                loaded_clip.state.setBoolean("usesGravity", false);
                loaded_clip.state.setInt("maxXSpeed", 800);
                loaded_clip.useGraphic("player_projectile");
                loaded_clip.props.setAnchorY(0.5);

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;

                    boolean isColliding = clip.state.getBoolean("isColliding");

                    if (isColliding) {
                        clip.remove();
                    }
                });
            });

            jevar.createPrefab("player_projectile", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("projectile");

                loaded_clip.useGraphic("player_projectile");
            });

            jevar.createPrefab("baddie_projectile", (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;
                loaded_clip.extend("projectile");

                loaded_clip.useGraphic("baddie_projectile");
            });

            jevar.createPrefab("background", 0, 0, jevar.meta.getScreenWidth(), jevar.meta.getScreenHeight(),
                    (loaded_self) -> {
                        JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                        loaded_clip.addJevascript("updateScreenDimensions");

                        JevaVCam vcam = jevar.getVCam("mainCamera");
                        JevaTileMap map = (JevaTileMap) jevar.getJevaClip("worldMap");
                        Image image1 = jevar.getImage("r_parallax_cave_1");
                        Image image2 = jevar.getImage("r_parallax_cave_2");
                        Image image3 = jevar.getImage("r_parallax_cave_3");
                        Image image4 = jevar.getImage("r_parallax_cave_4");
                        double imgW = image1.getWidth(null);
                        double imgH = image1.getHeight(null);

                        loaded_clip.usePainting((ctx, x, y, w, h, state) -> {
                            if (vcam == null || map == null)
                                return;

                            double currH = h + 100;
                            int newY = (int) (y - 50);
                            // double currH = map.props._height;
                            // int newY = (int) -((vcam.projection._y - vcam.projection._height / 2) -
                            // map.props._y);

                            int newW = (int) (imgW / imgH * currH);

                            double vcamLeft = vcam.projection._x - vcam.projection._width / 2;
                            int widthLeft = JevaUtils.pixelToBlock(vcamLeft - map.props._x, newW) - 1;

                            double vcamRight = vcam.projection._x + vcam.projection._width / 2;
                            int widthRight = JevaUtils.pixelToBlock(vcamRight + map.props._x, newW) + 1;

                            ctx.drawImage(image4, (int) x, (int) y, (int) w, (int) h, null);
                            for (int i = (int) (widthLeft) - 5; i < (int) (widthRight) + 5; i++)
                                ctx.drawImage(image3, (int) ((i * newW) - (vcamLeft + (vcamLeft * 0.25))), newY,
                                        (int) newW,
                                        (int) currH, null);
                            for (int i = (int) (widthLeft) - 3; i < (int) (widthRight) + 3; i++)
                                ctx.drawImage(image2, (int) ((i * newW) - (vcamLeft + (vcamLeft * 0.1))), newY,
                                        (int) newW,
                                        (int) currH, null);
                            for (int i = widthLeft; i < widthRight; i++)
                                ctx.drawImage(image1, (int) ((i * newW) - vcamLeft), newY, (int) newW, (int) currH,
                                        null);

                        });

                        loaded_clip.addJevascript((self) -> {
                            JevaPrefab clip = (JevaPrefab) self;

                            clip.props._width = jevar.meta.getScreenWidth() + 100;
                            clip.props._height = jevar.meta.getScreenHeight() + 100;

                            if (vcam == null)
                                return;

                            clip.props._x = -50 + vcam.projection._x - vcam.projection._width / 2;
                            clip.props._y = -50 + vcam.projection._y - vcam.projection._height / 2;
                        });
                    });

            jevar.createText("textfield1", "Hello World", 0, 450, 300, 400,
                    (loaded_self) -> {
                        JevaText loaded_clip = (JevaText) loaded_self;
                        loaded_clip.props.setFontSize(24);
                        loaded_clip.props._x = jevar.meta.getScreenWidth() / 2;
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

            jevar.createText("button", "BUTTON", jevar.meta.getScreenWidth() / 2, 350, 250, 80, (t) -> {
                JevaText loaded_clip = (JevaText) t;

                loaded_clip.state.setDouble("initialWidth", loaded_clip.props._height);
                loaded_clip.state.setBoolean("justRolledOver", false);
                loaded_clip.props._width = (int) loaded_clip.props._height * 25 / 6;
                loaded_clip.props.setFontSize((int) loaded_clip.props._height * 5 / 6);

                loaded_clip.props._fontColor = new Color(48, 31, 36);
                loaded_clip.props._backgroundColor = new Color(88, 75, 84);
                loaded_clip.props._borderColor = new Color(61, 48, 53);
                // loaded_clip.props._fontColor = new Color(68, 55, 64);
                // loaded_clip.props._backgroundColor = new Color(48, 31, 36);
                // loaded_clip.props._borderColor = new Color(61, 48, 53);
                loaded_clip.props._borderSize = 10;

                loaded_clip.props.setAnchorX(0.5);
                loaded_clip.props.setAnchorY(0.5);
                loaded_clip.props.setAlign("c");

                loaded_clip.addJevascript((self) -> {
                    JevaText clip = (JevaText) self;
                    clip.state.setBoolean("isClicked", false);

                    boolean justRolledOver = clip.state.getBoolean("justRolledOver");

                    if (clip.props.isHovered()) {
                        double height = clip.state.getDouble("initialWidth") * 1.2;
                        if (!justRolledOver) {
                            jevar.getSound("button_hover_sfx").playOnce();
                        }
                        justRolledOver = true;
                        loaded_clip.props._height = height;
                        loaded_clip.props._width = (int) height * 25 / 6;
                        loaded_clip.props.setFontSize((int) height * 5 / 6);
                        jevar.meta.setCursor("p");
                    } else {
                        double height = clip.state.getDouble("initialWidth");
                        justRolledOver = false;
                        loaded_clip.props._height = height;
                        loaded_clip.props._width = (int) height * 25 / 6;
                        loaded_clip.props.setFontSize((int) height * 5 / 6);
                    }
                    loaded_clip.state.setBoolean("justRolledOver", justRolledOver);

                    if (jevar.mouse.isPressed(JevaMouse.LEFT) && clip.props.isHovered()) {
                        clip.state.setBoolean("pressed", true);
                    }
                    if (jevar.mouse.isReleased(JevaMouse.LEFT) && clip.state.getBoolean("pressed")) {
                        if (clip.props.isHovered()) {
                            clip.state.setBoolean("isClicked", true);
                            jevar.getSound("button_press_sfx").playOnce();
                        }
                        clip.state.setBoolean("pressed", false);
                    }
                });
            });

            jevar.createScene("menuScene", (s) -> {
                JevaScene scene = (JevaScene) s;

                scene.addPrefab(0, 0, jevar.meta.getScreenWidth(), jevar.meta.getScreenWidth(), (loaded_self) -> {
                    JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                    loaded_clip.addJevascript("updateScreenDimensions");
                    loaded_clip.useGraphic("background_cave");

                });
                scene.addText("CAVE CLIMB", jevar.meta.getScreenWidth() / 2, 80, jevar.meta.getScreenWidth() * 0.8, 150,
                        (t) -> {
                            JevaText loaded_clip = (JevaText) t;

                            loaded_clip.props.setAnchorX(0.5);
                            loaded_clip.props.setAlign("c");

                            loaded_clip.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._x = jevar.meta.getScreenWidth() / 2;
                            });
                        });

                scene.addText("Credits:\nMade by Micah Brereton and Carlonn Rivers\nCOMP3609 Game Programming Project",
                        40, jevar.meta.getScreenHeight() - 40, jevar.meta.getScreenWidth() * 0.8, 65, (t) -> {
                            JevaText loaded_clip = (JevaText) t;

                            loaded_clip.props.setFontSize(20);

                            loaded_clip.props.setAnchorX(0);
                            loaded_clip.props.setAnchorY(1);

                            loaded_clip.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._y = jevar.meta.getScreenHeight() - 40;
                            });
                        });

                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "PLAY";
                    loaded_text.props._x = jevar.meta.getScreenWidth() / 2;
                    loaded_text.props._y = 320;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked")) {
                            jevar.useScene("setNameScene", true);
                        }

                        clip.props._x = jevar.meta.getScreenWidth() / 2;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });
                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "SCORES";
                    loaded_text.props._x = jevar.meta.getScreenWidth() / 2;
                    loaded_text.props._y = 510;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked")) {
                            jevar.useScene("highscoresScene", true);
                        }

                        clip.props._x = jevar.meta.getScreenWidth() / 2;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });

                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "EXIT";
                    loaded_text.props._x = jevar.meta.getScreenWidth() / 2;
                    loaded_text.props._y = 700;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked")) {
                            jevar.meta.closeApplication();
                        }

                        clip.props._x = jevar.meta.getScreenWidth() / 2;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });

            });
            jevar.createScene("highscoresScene", (s) -> {
                JevaScene scene = (JevaScene) s;

                scene.addPrefab(0, 0, jevar.meta.getScreenWidth(), jevar.meta.getScreenWidth(), (loaded_self) -> {
                    JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                    loaded_clip.addJevascript("updateScreenDimensions");
                    loaded_clip.useGraphic("background_cave");
                });

                final int scoreW = 750;
                final int lineH = 30;

                HighScore curScore = (HighScore) jevar.state.getState("currHighschore");

                if (curScore != null)
                    for (int i = 0; i < maxHighScores; i++) {
                        if (highScores[i].id == curScore.id) {
                            final int index = i;
                            scene.addText("", jevar.meta.getScreenWidth() / 2,
                                    250 + (lineH * 2) * (index + 1) - 10, scoreW + 20, lineH + 30, (t) -> {
                                        JevaText loaded_text = (JevaText) t;
                                        loaded_text.props.setAnchorX(0.5);
                                        loaded_text.setDepth(2);
                                        loaded_text.state.setDouble("defY", 250);

                                        loaded_text.props._backgroundColor = new Color(234, 154, 9, 130);

                                        loaded_text.addJevascript((self) -> {
                                            JevaText clip = (JevaText) self;

                                            clip.props._x = jevar.meta.getScreenWidth() / 2;
                                            clip.props._y = (loaded_text.state.getDouble("defY") / initHeight
                                                    * jevar.meta.getScreenHeight()) + (lineH * 2) * (index + 1) - 10;
                                        });
                                    });
                            break;
                        }
                    }

                scene.addText("HIGH SCORES", jevar.meta.getScreenWidth() / 2, 100, jevar.meta.getScreenWidth() * 0.8,
                        110,
                        (t) -> {
                            JevaText loaded_clip = (JevaText) t;

                            loaded_clip.props.setAnchorX(0.5);
                            loaded_clip.props.setAlign("c");

                            loaded_clip.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._x = jevar.meta.getScreenWidth() / 2;
                            });
                        });

                scene.addText("Name",
                        jevar.meta.getScreenWidth() / 2, 250, 380, 350, (t) -> {
                            JevaText loaded_text = (JevaText) t;

                            loaded_text.props.setFontSize(lineH);
                            loaded_text.state.setDouble("defY", loaded_text.props._y);

                            String scoreText = "Name\n\n";

                            int offset = 0;

                            for (int i = 0; i < maxHighScores; i++) {
                                scoreText += ((highScores[i].name.equals(nameLessScore)) ? "- - -"
                                        : highScores[i].name) + "\n\n";
                            }
                            loaded_text.props._text = scoreText;

                            loaded_text.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._x = (jevar.meta.getScreenWidth() / 2) - (scoreW / 2) + offset;
                                clip.props._y = loaded_text.state.getDouble("defY") / initHeight
                                        * jevar.meta.getScreenHeight();
                            });
                        });
                scene.addText("Level",
                        jevar.meta.getScreenWidth() / 2, 250, 90, 350, (t) -> {
                            JevaText loaded_text = (JevaText) t;

                            loaded_text.props.setFontSize(lineH);
                            loaded_text.props.setAlign("r");
                            loaded_text.state.setDouble("defY", loaded_text.props._y);

                            String scoreText = "Level\n\n";

                            int offset = 380;

                            for (int i = 0; i < maxHighScores; i++) {
                                scoreText += ((highScores[i].name.equals(nameLessScore)) ? "- - -"
                                        : (highScores[i].level + 1)) + "\n\n";
                            }
                            loaded_text.props._text = scoreText;

                            loaded_text.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._x = jevar.meta.getScreenWidth() / 2 - scoreW / 2 + offset;
                                clip.props._y = loaded_text.state.getDouble("defY") / initHeight
                                        * jevar.meta.getScreenHeight();
                            });
                        });
                scene.addText("Score",
                        jevar.meta.getScreenWidth() / 2, 250, 150, 350, (t) -> {
                            JevaText loaded_text = (JevaText) t;

                            loaded_text.props.setFontSize(lineH);
                            loaded_text.props.setAlign("r");
                            loaded_text.state.setDouble("defY", loaded_text.props._y);

                            String scoreText = "Score\n\n";

                            int offset = 470;

                            for (int i = 0; i < maxHighScores; i++) {
                                scoreText += ((highScores[i].name.equals(nameLessScore)) ? "- - -"
                                        : highScores[i].score) + "\n\n";
                            }
                            loaded_text.props._text = scoreText;

                            loaded_text.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._x = jevar.meta.getScreenWidth() / 2 - scoreW / 2 + offset;
                                clip.props._y = loaded_text.state.getDouble("defY") / initHeight
                                        * jevar.meta.getScreenHeight();
                            });
                        });
                scene.addText("Time",
                        jevar.meta.getScreenWidth() / 2, 250, 130, 350, (t) -> {
                            JevaText loaded_text = (JevaText) t;

                            loaded_text.props.setFontSize(lineH);
                            loaded_text.props.setAlign("r");
                            loaded_text.state.setDouble("defY", loaded_text.props._y);

                            String scoreText = "Time\n\n";

                            int offset = 620;

                            for (int i = 0; i < maxHighScores; i++) {
                                scoreText += ((highScores[i].name.equals(nameLessScore)) ? "- -:- -:- -"
                                        : highScores[i].duration) + "\n\n";
                            }
                            loaded_text.props._text = scoreText;

                            loaded_text.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._x = jevar.meta.getScreenWidth() / 2 - scoreW / 2 + offset;
                                clip.props._y = loaded_text.state.getDouble("defY") / initHeight
                                        * jevar.meta.getScreenHeight();
                            });
                        });

                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "MENU";
                    loaded_text.props._x = jevar.meta.getScreenWidth() / 3;
                    loaded_text.props._y = 700;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked") || key.isReleased("escape")) {
                            jevar.useScene("menuScene", true);
                        }

                        clip.props._x = jevar.meta.getScreenWidth() / 3;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });
                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "RESET";
                    loaded_text.props._x = jevar.meta.getScreenWidth() * 2 / 3;
                    loaded_text.props._y = 700;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked")) {
                            jevar.execFunc("resetHighScores");
                            jevar.resetScene();
                        }

                        clip.props._x = jevar.meta.getScreenWidth() * 2 / 3;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });

            });
            jevar.createScene("setNameScene", (s) -> {
                JevaScene scene = (JevaScene) s;

                scene.addPrefab(0, 0, jevar.meta.getScreenWidth(), jevar.meta.getScreenWidth(), (loaded_self) -> {
                    JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                    loaded_clip.addJevascript("updateScreenDimensions");
                    loaded_clip.useGraphic("background_cave");

                });
                jevar.state.setBoolean("typing", true);
                scene.addUnload((clp) -> {
                    jevar.state.setBoolean("typing", false);
                });
                String playerName = jevar.state.getString("playerName");
                scene.addText("Enter Name:\n", jevar.meta.getScreenWidth() / 2, 150, jevar.meta.getScreenWidth() * 0.8,
                        100,
                        (t) -> {
                            JevaText loaded_clip = (JevaText) t;

                            loaded_clip.props.setAnchorX(0.5);
                            loaded_clip.props.setAlign("c");
                            loaded_clip.props.setFontSize(40);

                            loaded_clip.setInstanceName("playerNameText");

                            loaded_clip.state.setString("enteredName", playerName);

                            loaded_clip.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;
                                String enteredName = clip.state.getString("enteredName");
                                int len = enteredName.length();
                                int maxLen = 10;

                                clip.props._x = jevar.meta.getScreenWidth() / 2;

                                char letter = key.getTyped();

                                if (letter != 0 && len < maxLen) {
                                    enteredName += letter;
                                } else if (key.isPressed("backspace") && len > 0) {
                                    enteredName = enteredName.substring(0, len - 1);
                                }

                                String fill = "";
                                for (int i = len; i < maxLen; i++)
                                    fill += " _";

                                clip.props._text = "Enter Name:\n" + enteredName + fill;

                                loaded_clip.state.setString("enteredName", enteredName);
                            });
                        });

                scene.addText("Credits:\nMade by Micah Brereton and Carlonn Rivers\nCOMP3609 Game Programming Project",
                        40, jevar.meta.getScreenHeight() - 40, jevar.meta.getScreenWidth() * 0.8, 65, (t) -> {
                            JevaText loaded_clip = (JevaText) t;

                            loaded_clip.props.setFontSize(20);

                            loaded_clip.props.setAnchorX(0);
                            loaded_clip.props.setAnchorY(1);

                            loaded_clip.addJevascript((self) -> {
                                JevaText clip = (JevaText) self;

                                clip.props._y = jevar.meta.getScreenHeight() - 40;
                            });
                        });

                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "PLAY";
                    loaded_text.props._x = jevar.meta.getScreenWidth() / 2;
                    loaded_text.props._y = 350;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked") || key.isReleased("enter")) {
                            String newName = ((JevaText) jevar.getJevaClip("playerNameText")).state
                                    .getString("enteredName").strip();

                            if (newName == "")
                                newName = "PLAYER";

                            jevar.state.setString("playerName", newName);
                            so.setItem("playerName", newName);
                            jevar.execFunc("newGame");
                        }

                        clip.props._x = jevar.meta.getScreenWidth() / 2;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });

                scene.addText("button", (t) -> {
                    JevaText loaded_text = (JevaText) t;

                    loaded_text.props._text = "MENU";
                    loaded_text.props._x = jevar.meta.getScreenWidth() / 2;
                    loaded_text.props._y = 550;
                    loaded_text.state.setDouble("defY", loaded_text.props._y);

                    loaded_text.addJevascript((self) -> {
                        JevaText clip = (JevaText) self;

                        if (clip.state.getBoolean("isClicked") || key.isReleased("escape")) {
                            jevar.useScene("menuScene", true);
                        }

                        clip.props._x = jevar.meta.getScreenWidth() / 2;
                        clip.props._y = loaded_text.state.getDouble("defY") / initHeight * jevar.meta.getScreenHeight();
                    });
                });

            });
            jevar.createScene("gameScene", (s) -> {
                JevaScene scene = (JevaScene) s;

                JevaVCam cam1 = scene.addVCam("mainCamera", 0, 0, jevar.meta.getScreenWidth(),
                        jevar.meta.getScreenHeight());
                cam1.centerPAnchors();

                scene.addTileMap("worldMap").setDepth(worldMapDepth);
                scene.addPrefab("background").setDepth(backgroundDepth);
                // scene.addText("textfield1");
                scene.addPrefab("flood").setDepth(floodDepth);

                scene.addPrefab("overlay").setDepth(overlayDepth);

                scene.addJevascript((scn) -> {
                    cam1.projection._width = jevar.meta.getScreenWidth();
                    cam1.projection._height = jevar.meta.getScreenHeight();
                    cam1.viewport._width = jevar.meta.getScreenWidth();
                    cam1.viewport._height = jevar.meta.getScreenHeight();
                });
            });

            jevar.useScene("menuScene");

            jevar.attachJevascript((s) -> {
                JevaR core = (JevaR) s;

                // if (key.isReleased("1")) {
                // core.useScene("menuScene");
                // } else if (key.isReleased("2")) {
                // core.useScene("gameScene");
                // }
                if (!jevar.state.getBoolean("typing")) {
                    if (key.isReleased("Q")) {
                        meta.closeApplication();
                    }
                    // if (key.isReleased("R")) {
                    // core.resetScene();
                    // }
                    if (key.isPressed(JevaKey.Z)) {
                        jevar.debugMode = !jevar.debugMode;
                    }
                    if (key.isPressed(JevaKey.C)) {
                        core.alterTimeScale(-0.25);
                    }
                    if (key.isPressed(JevaKey.V)) {
                        core.alterTimeScale(0.25);
                    }
                    if (key.isPressed(JevaKey.F)) {
                        core.meta.toggleFullscreen();
                    }
                    if (key.isPressed(JevaKey.M)) {
                        // core.sound
                        if (JevaSound.getMasterVolume() == 1)
                            JevaSound.setMasterVolume(0);
                        else
                            JevaSound.setMasterVolume(1);
                    }
                }
            });
        });
    }
}