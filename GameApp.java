import core.JevaR;
import core.JevaKey;
import core.JevaClip;
import core.JevaSound;
import core.JevaSpriteSheet;

public class GameApp {
    public static void main(String[] args) {
        JevaR jevar = new JevaR(600, 500, 60);

        jevar.createJevascript("move_jevaclip", (self) -> {
            int n = 5;
            JevaClip clip = (JevaClip) self;
            if (JevaKey.isDown(65) || JevaKey.isDown("left")) {
                clip._x -= 8;
            }
            if (JevaKey.isDown("d") || JevaKey.isDown(JevaKey.RIGHT)) {
                clip._x += 8;
            }
            if (JevaKey.isDown(JevaKey.W) || JevaKey.isDown(JevaKey.UP)) {
                clip._y -= 8;
            }
            if (JevaKey.isDown(JevaKey.S) || JevaKey.isDown(JevaKey.DOWN)) {
                clip._y += 8;
            }
            if (JevaKey.isDown(JevaKey.I)) {
                clip._height -= n;
            }
            if (JevaKey.isDown(JevaKey.K)) {
                clip._height += n;
            }
            if (JevaKey.isDown(JevaKey.J)) {
                clip._width -= n;
            }
            if (JevaKey.isDown(JevaKey.L)) {
                clip._width += n;
            }
            if (JevaKey.isPressed("space")) {
                clip._x = 200;
                clip._y = 200;
            }
        });

        jevar.createGraphic("butterfly", "Butterfly.png");
        jevar.createGraphic("animImage1", "animImage1.png");
        jevar.createGraphic("animImage2", "animImage2.png");
        jevar.createGraphic("animImage3", "animImage3.png");

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

        jevar.createJevascript("load_jevaclip1", (self) -> {
            JevaClip clip = (JevaClip) self;
            clip.addJevascript("move_jevaclip");
            clip.useGraphic("butterfly");

            // jevar.createSound("adn", "path/to/sound.mp4");
            // JevaSound.play("adn");
        });
        
        jevar.createJevascript("load_jevaclip2", (self) -> {
            JevaClip clip = (JevaClip) self;
            clip.addJevascript("move_jevaclip");
        });
        jevar.createJevascript("load_face", (self) -> {
            JevaClip clip = (JevaClip) self;
            clip.addJevascript("move_jevaclip");
            clip.useSpriteSheet("face_animation");

            // jevar.createSound("adn", "path/to/sound.mp4");
            // JevaSound.play("adn");

            /*
             * if(clip.hitTest("jevaclip2")) {
             *      score++;
             * }
             */
        });

        // jeva.createTextField("my_textfield", _x, _y, _width, _heigth, _text, _color, _font);

        jevar.createJevaclip("jevaclip1", 200, 180, 100, 100, "load_jevaclip1");
        jevar.createJevaclip("jevaclip2", 200, 180, 100, 100, "load_jevaclip2");

        jevar.createJevaclip("face", 200, 20, 100, 100, "load_face");

        jevar.attachJevaclip("jevaclip1", 50, 200);
        jevar.attachJevaclip("jevaclip2", 350, 200);
        jevar.attachJevaclip("face", 200, 50);

        jevar.createSound("background", "Background.wav");
        jevar.createSound("hitsound", "HitSound2.wav");

        jevar.attachJevascript((self) -> {
            if (JevaKey.isPressed("z")) {
                JevaSound background = jevar.getSound("background");
                background.play(true);
            }
            if (JevaKey.isPressed("x")) {
                JevaSound background = jevar.getSound("background");
                background.stop();
            }
            if (JevaKey.isPressed("c")) {
                JevaSound hitsound = jevar.getSound("hitsound");
                hitsound.play(false);
            }
        });

        // jevar.attachJevaclip("jevaclip1", 420, 320);
        // jevar.attachJevaclip("jevaclip1", 250, 130, 150, 80);

        // jevar.createJevascript("my_cool_script", (self) -> {
        // if (JevaKey.isDown(65) || JevaKey.isDown(JevaKey.LEFT)) {
        // jevaclip._x -= 30;
        // }
        // if (JevaKey.isDown(JevaKey.D) || JevaKey.isDown(JevaKey.RIGHT)) {
        // jevaclip._x += 30;
        // }
        // if (JevaKey.isDown(JevaKey.W) || JevaKey.isDown(JevaKey.UP)) {
        // jevaclip._y -= 30;
        // }
        // if (JevaKey.isDown(JevaKey.S) || JevaKey.isDown(JevaKey.DOWN)) {
        // jevaclip._y += 30;
        // }
        // if (JevaKey.isPressed(JevaKey.SPACE)) {
        // jevaclip._x = 300;
        // jevaclip._y = 250;
        // }
        // });

        // jevar.attachJevascript("my_cool_script");
        // jevar.attachJevascript((self) -> {
        // if (JevaKey.isPressed(JevaKey.R)) {
        // jevaclip._x = 0;
        // jevaclip._y = 0;
        // }
        // });
    }
}