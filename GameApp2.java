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
import core.JevaVCam;

import java.awt.*;
import java.io.IOException;

public class GameApp2 {

    public static void main(String[] args) {
        int initWidth = 1420;
        int initHeight = 800;
        int initFps = 120;
        int initTps = 120;
        new JevaR(initWidth, initHeight, initFps, initTps, (jevar) -> {
            JevaMeta meta = jevar.meta;
            JevaMouse mouse = jevar.mouse;
            JevaKey key = jevar.key;
            jevar.createGraphic("player1");
            jevar.createGraphic("player2");
            jevar.createGraphic("background", "background.jpg");

            jevar.createPrefab("player1", initWidth - 100, 350, 100, 100, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                JevaVCam vcam = jevar.getVCam("myCam2");

                // loaded_clip.centerAnchors();
                loaded_clip.props.setAnchorX(0.5);
                loaded_clip.props.setAnchorY(0.5);
                loaded_clip.useGraphic("player1");

                loaded_clip.addJevascript((self) -> {
                    JevaPrefab clip = (JevaPrefab) self;
                    double playerSpeed = 480 * jevar.getDelta();
                    if (key.isDown("left")) {
                        clip.props._x -= playerSpeed;
                        clip.props._scaleX = -1;
                    }
                    if (key.isDown(JevaKey.RIGHT)) {
                        clip.props._x += playerSpeed;
                        clip.props._scaleX = 1;
                    }
                    if (key.isDown(JevaKey.UP)) {
                        clip.props._y -= playerSpeed;
                    }
                    if (key.isDown(JevaKey.DOWN)) {
                        clip.props._y += playerSpeed;
                    }
                    if (vcam != null) {
                        vcam.projection._x = clip.props._x;
                        vcam.projection._y = clip.props._y;
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
            jevar.createPrefab("background", -700, -1200, initWidth * 2, 2000, (loaded_self) -> {
                JevaPrefab loaded_clip = (JevaPrefab) loaded_self;

                loaded_clip.props.centerAnchors();
                loaded_clip.useGraphic("background");
            });

            jevar.createText("textfield1", "Hello World", 0, 0, 300, 100,
                    (loaded_self) -> {
                        ((JevaText) loaded_self).props.setFontSize(24);
                        ((JevaText) loaded_self).props._x = initWidth / 2;
                        ((JevaText) loaded_self).props._y = 40;
                        ((JevaText) loaded_self).props.setAnchorX(0.5);
                        // ((JevaText) loaded_self).setAnchorY(1);

                        ((JevaText) loaded_self).addJevascript((self) -> {

                            JevaText clip = (JevaText) self;

                            clip.props._text = "Mouse: [" + mouse._xmouse + ":" + mouse._ymouse + "]" + "\n\nFPS: ["
                            + jevar.getFPS() + "]" + "\nTPS: ["
                            + jevar.getTPS() + "]";
                        });
                    });

            jevar.createScene("scene1", (s) -> {
                JevaScene scene = (JevaScene) s;

                JevaVCam cam1 = scene.addVCam("myCam1", 0, 0, initWidth / 2, initHeight - 50);
                cam1.centerPAnchors();
                JevaVCam cam2 = scene.addVCam("myCam2", initWidth / 2, 50, initWidth / 2, initHeight - 50);
                cam2.centerPAnchors();
                // System.out.println("Cam: " + cam.toString());
                // scene.addVCam("myCam2", 0, 0, initWidth / 2, initHeight - 50);

                scene.addPrefab("background");
                scene.addPrefab("player1");
                scene.addPrefab("player2");
                scene.addText("textfield1");
            });
            jevar.createScene("scene2", (s) -> {
                JevaScene scene = (JevaScene) s;

                scene.addPrefab("background");
                scene.addPrefab("player1");
                scene.addPrefab("player2");
                scene.addText("textfield1");
            });

            jevar.useScene("scene2");
            jevar.attachJevascript((s) -> {
                JevaR core = (JevaR) s;

                if (key.isReleased("1")) {
                    core.useScene("scene1");
                } else if (key.isReleased("2")) {
                    core.useScene("scene2");
                }
                if (key.isReleased("Q")) {
                    meta.closeApplication();
                }
                if(key.isReleased("R")) {
                    core.resetScene();
                }
            });

            // jevar.attachPrefab("player1");
            // jevar.attachPrefab("enemy_guy");
            // jevar.attachPrefab("enemy_guy", 250, 200);
            // jevar.attachText("textfield1");

            // jevar.createJevascript("move_jevaclip", (self) -> {
            // int n = 5;
            // JevaClip clip = (JevaClip) self;
            // if (key.isDown("left")) {
            // clip.props._x -= 8;
            // }
            // if (key.isDown(JevaKey.RIGHT)) {
            // clip.props._x += 8;
            // }
            // if (key.isDown(JevaKey.UP)) {
            // clip.props._y -= 8;
            // }
            // if (key.isDown(JevaKey.DOWN)) {
            // clip.props._y += 8;
            // }
            // double nnn = 0.01;
            // if (key.isDown(JevaKey.W)) {
            // clip.props._scaleY -= nnn;
            // }
            // if (key.isDown(JevaKey.S)) {
            // clip.props._scaleY += nnn;
            // }
            // if (key.isDown(JevaKey.A)) {
            // clip.props._scaleX -= nnn;
            // }
            // if (key.isDown(JevaKey.D)) {
            // clip.props._scaleX += nnn;
            // }
            // double nn = 0.05;
            // if (key.isDown(JevaKey.T)) {
            // clip.setAnchorY(clip.getAnchorY() - nn);
            // }
            // if (key.isDown(JevaKey.G)) {
            // clip.setAnchorY(clip.getAnchorY() + nn);
            // }
            // if (key.isDown(JevaKey.F)) {
            // clip.setAnchorX(clip.getAnchorX() - nn);
            // }
            // if (key.isDown(JevaKey.H)) {
            // clip.setAnchorX(clip.getAnchorX() + nn);
            // }
            // // System.out.println(clip.props._scaleX + ":" + clip.props._scaleY);
            // // System.out.println(clip.getAnchorX() + ":" + clip.getAnchorY());
            // if (key.isDown(JevaKey.I)) {
            // clip.props._height -= n;
            // }
            // if (key.isDown(JevaKey.K)) {
            // clip.props._height += n;
            // }
            // if (key.isDown(JevaKey.J)) {
            // clip.props._width -= n;
            // }
            // if (key.isDown(JevaKey.L)) {
            // clip.props._width += n;
            // }
            // if (key.isPressed("space")) {
            // clip.props._x = 200;
            // clip.props._y = 200;
            // }

            // if(key.isReleased("Q"))
            // meta.closeApplication();
            // });

            // jevar.createGraphic("butterfly", "Butterfly.png");
            // jevar.createGraphic("animImage1", "animImage1.png");
            // jevar.createGraphic("animImage2", "animImage2.png");
            // jevar.createGraphic("animImage3", "animImage3.png");

            // jevar.createSpriteSheet("face_animation", (self) -> {
            // JevaSpriteSheet spritesheet = (JevaSpriteSheet) self;

            // spritesheet.addFrame("animImage1", 150);
            // spritesheet.addFrame("animImage2", 150);
            // spritesheet.addFrame("animImage1", 150);
            // spritesheet.addFrame("animImage2", 150);
            // spritesheet.addFrame("animImage1", 150);
            // spritesheet.addFrame("animImage2", 150);
            // spritesheet.addFrame("animImage1", 150);
            // spritesheet.addFrame("animImage3", 150);
            // });

            // jevar.createJevascript("load_butterfly_clip", (self) -> {
            // JevaPrefab clip = (JevaPrefab) self;
            // clip.addJevascript("move_jevaclip");
            // clip.useGraphic("butterfly");

            // // jevar.createSound("adn", "path/to/sound.mp4");
            // // JevaSound.play("adn");
            // });

            // jevar.createJevascript("load_jevaclip2", (self) -> {
            // JevaClip clip = (JevaClip) self;
            // clip.addJevascript("move_jevaclip");
            // });
            // jevar.createJevascript("load_textfield", (self) -> {
            // JevaClip clip = (JevaClip) self;
            // clip.addJevascript("move_jevaclip");
            // });
            // jevar.createJevascript("load_face", (self) -> {
            // JevaPrefab clip = (JevaPrefab) self;
            // clip.addJevascript("move_jevaclip");

            // clip.addJevascript((_clip) -> {
            // clip.useSpriteSheet("face_animation");

            // });

            // // jevar.createSound("adn", "path/to/sound.mp4");
            // // JevaSound.play("adn");

            // /*
            // * if(clip.hitTest("jevaclip2")) {
            // * score++;
            // * }
            // */
            // });

            // // jeva.createTextField("my_textfield", _x, _y, _width, _heigth, _text,
            // _color,
            // // _font);

            // int begSize = 100;

            // jevar.createPrefab("butterfly_clip", 200, 80, begSize, begSize,
            // "load_butterfly_clip");
            // jevar.createPrefab("jevaclip2", 200, 280, begSize, begSize,
            // "load_jevaclip2");

            // jevar.createPrefab("face", 400, 80, begSize, begSize, "load_face");

            // jevar.createText("text", "Hello world", 400, 280, begSize, begSize,
            // "load_textfield");

            // jevar.attachPrefab("face");
            // jevar.attachPrefab("jevaclip2");
            // jevar.attachPrefab("butterfly_clip");
            // jevar.attachText("text");

            // jevar.createSound("background", "Background.wav");
            // jevar.createSound("hitsound", "HitSound2.wav");

            // jevar.attachJevascript((self) -> {
            // if (key.isPressed("z")) {
            // JevaSound background = jevar.getSound("background");
            // background.play(true);
            // }
            // if (key.isPressed("x")) {
            // JevaSound background = jevar.getSound("background");
            // background.stop();
            // }
            // if (key.isPressed("c")) {
            // JevaSound hitsound = jevar.getSound("hitsound");
            // hitsound.play(false);
            // }
            // });

            // jevar.attachJevaclip("butterfly_clip", 420, 320);
            // jevar.attachJevaclip("butterfly_clip", 250, 130, 150, 80);

            // jevar.createJevascript("my_cool_script", (self) -> {
            // if (key.isDown(65) || key.isDown(JevaKey.LEFT)) {
            // jevaclip.props._x -= 30;
            // }
            // if (key.isDown(JevaKey.D) || key.isDown(JevaKey.RIGHT)) {
            // jevaclip.props._x += 30;
            // }
            // if (key.isDown(JevaKey.W) || key.isDown(JevaKey.UP)) {
            // jevaclip.props._y -= 30;
            // }
            // if (key.isDown(JevaKey.S) || key.isDown(JevaKey.DOWN)) {
            // jevaclip.props._y += 30;
            // }
            // if (key.isPressed(JevaKey.SPACE)) {
            // jevaclip.props._x = 300;
            // jevaclip.props._y = 250;
            // }
            // });

            // jevar.attachJevascript("my_cool_script");
            // jevar.attachJevascript((self) -> {
            // if (key.isPressed(JevaKey.R)) {
            // jevaclip.props._x = 0;
            // jevaclip.props._y = 0;
            // }
            // });
        });
    }
}