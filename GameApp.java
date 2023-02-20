import core.JevaR;
import core.JevaKey;
import core.JevaClip;

public class GameApp {
    public static void main(String[] args) {
        JevaR jevar = new JevaR(600, 500, 3);

        jevar.createJevascript("move_jevaclip", (self) -> {
            JevaClip clip = (JevaClip) self;
            if (JevaKey.isDown(65) || JevaKey.isDown("left")) {
                clip._x -= 30;
            }
            if (JevaKey.isDown("d") || JevaKey.isDown(JevaKey.RIGHT)) {
                clip._x += 30;
            }
            if (JevaKey.isDown(JevaKey.W) || JevaKey.isDown(JevaKey.UP)) {
                clip._y -= 30;
            }
            if (JevaKey.isDown(JevaKey.S) || JevaKey.isDown(JevaKey.DOWN)) {
                clip._y += 30;
            }
            if (JevaKey.isPressed("space")) {
                clip._x = 200;
                clip._y = 200;
            }
        });

        // jevar.createGraphic("name", "i")

        jevar.createJevascript("load_jevaclip", (self) -> {
            JevaClip clip = (JevaClip) self;
            clip.addJevascript("move_jevaclip");
            // clip.useGraphic("gname");

            // JevaSound.create("adn", "path/to/sound.mp4");
            // JevaSound.play("adn");
        });

        jevar.createJevaclip("jevaclip1", 200, 80, 100, 100, "load_jevaclip");

        jevar.attachJevaclip("jevaclip1", 50, 200);
        jevar.attachJevaclip("jevaclip1", 350, 200);



























        // jevar.attachJevaclip("jevaclip1", 420, 320);
        // jevar.attachJevaclip("jevaclip1", 250, 130, 150, 80);

        // jevar.createJevascript("my_cool_script", (self) -> {
        //     if (JevaKey.isDown(65) || JevaKey.isDown(JevaKey.LEFT)) {
        //         jevaclip._x -= 30;
        //     }
        //     if (JevaKey.isDown(JevaKey.D) || JevaKey.isDown(JevaKey.RIGHT)) {
        //         jevaclip._x += 30;
        //     }
        //     if (JevaKey.isDown(JevaKey.W) || JevaKey.isDown(JevaKey.UP)) {
        //         jevaclip._y -= 30;
        //     }
        //     if (JevaKey.isDown(JevaKey.S) || JevaKey.isDown(JevaKey.DOWN)) {
        //         jevaclip._y += 30;
        //     }
        //     if (JevaKey.isPressed(JevaKey.SPACE)) {
        //         jevaclip._x = 300;
        //         jevaclip._y = 250;
        //     }
        // });

        // jevar.attachJevascript("my_cool_script");
        // jevar.attachJevascript((self) -> {
        //     if (JevaKey.isPressed(JevaKey.R)) {
        //         jevaclip._x = 0;
        //         jevaclip._y = 0;
        //     }
        // });
    }
}