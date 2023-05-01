package core;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaTileMap extends JevaClip {
    private class Block {
        private String tileName;
        private char tileCode;
        private String tileType;
        private String appearanceName;
        private appearances appearanceType;
        private Object appearanceSource;

        private Block(char tileCode) {
            this.tileCode = tileCode;
            this.tileName = "";
            this.tileType = "";
            this.appearanceName = "";
            this.appearanceType = appearances.painting;
            this.appearanceSource = null;
        }
    }

    private Block[][] _tileMap;
    private HashMap<Character, String> _tileMapEnum;
    private HashMap<Character, String> _tileTypeEnum;
    public int _tileWidth, _tileHeight;
    public int _mapWidth, _mapHeight;

    private enum appearances {
        painting, graphic, spritesheet
    }

    protected JevaTileMap(JevaR core, String _label, double _x, double _y, int _tileWidth, int _tileHeight) {
        this(core, _label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    protected JevaTileMap(JevaR core, String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            JevaScript onLoad) {
        this(core, _label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaTileMap(JevaR core, String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            ArrayList<JevaScript> onLoads) {
        this(core, _label, _x, _y, _tileWidth, _tileHeight, 0, 0, onLoads);
    }

    protected JevaTileMap(JevaR core, String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            int _mapWidth, int _mapHeight,
            JevaScript onLoad) {
        this(core, _label, _x, _y, _tileWidth, _tileHeight, _mapWidth, _mapHeight,
                new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaTileMap(JevaR core, String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            int _mapWidth, int _mapHeight,
            ArrayList<JevaScript> onLoads) {
        super(core, _label, _x, _y, _tileWidth, _tileHeight, onLoads);

        this._tileWidth = _tileWidth;
        this._tileHeight = _tileHeight;
        this._tileMapEnum = new HashMap<Character, String>();
        this._tileTypeEnum = new HashMap<Character, String>();

        this._mapWidth = _mapWidth;
        this._mapHeight = _mapHeight;
        _tileMap = new Block[_mapWidth][_mapHeight];

        props._width = _mapWidth * this._tileWidth;
        props._height = _mapHeight * this._tileHeight;
    }

    public void loadMapFromFile(String filename)
            throws IOException {
        ArrayList<String> lines = new ArrayList<String>();

        // read every line in the text file into the list

        BufferedReader reader = new BufferedReader(
                new FileReader(filename));
        while (true) {
            String line = reader.readLine();
            // no more lines to read
            if (line == null) {
                reader.close();
                break;
            }

            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
            }
        }

        createMapFromArrayList(lines);
    }

    public void loadMapFrom2DArray(String[] arr) {
        ArrayList<String> lines = new ArrayList<String>();

        for (int y = 0; y < arr.length; y++) {
            String line = arr[y];
            // add every line except for comments
            if (!line.startsWith("#")) {
                lines.add(line);
            }
        }

        createMapFromArrayList(lines);
    }

    public void loadMapFrom2DArray(char[][] arr) {
        ArrayList<String> lines = new ArrayList<String>();

        for (int y = 0; y < arr.length; y++) {
            String line = new String(arr[y]);
            char firstChar = arr[y][0];
            // add every line except for comments
            if (firstChar != '#') {
                lines.add(line);
            }
        }

        createMapFromArrayList(lines);
    }

    private void createMapFromArrayList(ArrayList<String> lines) {
        int _mapWidth = 0;
        int _mapHeight = lines.size();
        for (String line : lines)
            _mapWidth = Math.max(_mapWidth, line.length());

        this._mapWidth = _mapWidth;
        this._mapHeight = _mapHeight;

        // parse the lines to create a TileMap
        _tileMap = new Block[_mapWidth][_mapHeight];

        // for(int i = 0; i < )

        for (int y = 0; y < _mapHeight; y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char ch = line.charAt(x);

                // check if the char represents tile A, B, C etc.
                // String tile = String.valueOf(ch); // - 'A';
                setTile(x, y, ch, true);
                /*
                 * // check if the char represents a sprite
                 * else if (ch == 'o') {
                 * addSprite(newMap, coinSprite, x, y);
                 * }
                 * else if (ch == '!') {
                 * addSprite(newMap, musicSprite, x, y);
                 * }
                 * else if (ch == '*') {
                 * addSprite(newMap, goalSprite, x, y);
                 * }
                 * else if (ch == '1') {
                 * addSprite(newMap, grubSprite, x, y);
                 * }
                 * else if (ch == '2') {
                 * addSprite(newMap, flySprite, x, y);
                 * }
                 */
            }
        }
        props._width = _mapWidth * this._tileWidth;
        props._height = _mapHeight * this._tileHeight;
    }

    public ArrayList<JevaClip> createClipAtTile(char k, String _label) {
        return createClipAtTile(k, _label, 1);
    }

    public ArrayList<JevaClip> createClipAtTile(char k, String _label, int clipDepth) {
        return createClipAtTile(k, _label, clipDepth, JevaUtils.emptyScript);
    }

    public ArrayList<JevaClip> createClipAtTile(char k, String _label, int clipDepth, JevaScript onLoad) {
        ArrayList<JevaClip> createdClips = new ArrayList<>();

        JevaScene scene = core.getCurrentScene();

        if (scene == null)
            return createdClips;

        JevaClip desiredClip = core.jevaclipLibrary.get(_label);

        if (desiredClip == null)
            return createdClips;

        for (int y = 0; y < _mapHeight; y++) {
            for (int x = 0; x < _mapWidth; x++) {
                if (getTileCode(x, y) != k)
                    continue;
                _tileMap[x][y].tileType = _label;
                int _x = JevaUtils.roundInt(props._x + tilesToPixelsX(x));
                int _y = JevaUtils.roundInt(props._y + tilesToPixelsY(y));
                JevaClip addedClip = scene.addPrefab(_label, _x, _y, onLoad).setDepth(clipDepth);
                if (isLoaded || isLoading) {
                    addedClip.load();
                }
                createdClips.add(addedClip);
            }
        }

        return createdClips;
    }

    public void setTileEnum(char k, String _name, String _type) {
        setTileMapEnum(k, _name);
        setTileTypeEnum(k, _type);
    }

    public void setTileMapEnum(char k, String _label) {
        if (_tileMapEnum.get(k) == null)
            _tileMapEnum.put(k, _label);
    }

    public void setTileTypeEnum(char k, String _label) {
        if (_tileTypeEnum.get(k) == null)
            _tileTypeEnum.put(k, _label);
    }

    public void setTile(int x, int y, char tileCode, boolean isGraphic) {
        if (x < 0 || x >= _mapWidth ||
                y < 0 || y >= _mapHeight) {
        } else {
            _tileMap[x][y] = new Block(tileCode);

            String tileName = _tileMapEnum.get(tileCode);
            if (tileName != null) {
                setTileName(x, y, tileName);
                if (isGraphic)
                    useGraphic(x, y, tileName);
                else
                    useSpriteSheet(x, y, tileName);
            }
            String tileType = _tileTypeEnum.get(tileCode);
            if (tileType != null) {
                setTileType(x, y, tileType);
            }
        }
    }

    public void setTileName(int x, int y, String tileName) {
        if (x < 0 || x >= _mapWidth ||
                y < 0 || y >= _mapHeight) {
        } else {
            _tileMap[x][y].tileName = tileName;
        }
    }

    public void setTileType(int x, int y, String tileType) {
        if (x < 0 || x >= _mapWidth ||
                y < 0 || y >= _mapHeight) {
        } else {
            _tileMap[x][y].tileType = tileType;
        }
    }

    public String getTile(int x, int y) {
        try {
            if (x < 0 || x >= _mapWidth ||
                    y < 0 || y >= _mapHeight) {
                return null;
            } else {
                return _tileMap[x][y].tileName;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String getTileType(int x, int y) {
        try {
            if (x < 0 || x >= _mapWidth ||
                    y < 0 || y >= _mapHeight) {
                return null;
            } else {
                return _tileMap[x][y].tileType;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public char getTileCode(int x, int y) {
        try {
            if (x < 0 || x >= _mapWidth ||
                    y < 0 || y >= _mapHeight) {
                return 0;
            } else {
                return _tileMap[x][y].tileCode;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    // adding jobtives
    protected void useGraphic(int x, int y, String _label) {

        if (_tileMap[x][y].appearanceName.equals(_label) && _tileMap[x][y].appearanceType == appearances.graphic)
            return;
        JevaGraphic graphic = core.getJevaGraphic(_label);

        if (graphic == null)
            return;

        Image source = graphic.getSource();

        _tileMap[x][y].appearanceName = _label;
        _tileMap[x][y].appearanceType = appearances.graphic;
        _tileMap[x][y].appearanceSource = source;
    }

    public void useSpriteSheet(int x, int y, String _label) {

        if (_tileMap[x][y].appearanceName.equals(_label) && _tileMap[x][y].appearanceType == appearances.spritesheet)
            return;
        JevaSpriteSheet source = core.jevaspritesheetLibrary.get(_label);

        if (source == null)
            return;

        source.reset(0, null);

        _tileMap[x][y].appearanceName = _label;
        _tileMap[x][y].appearanceType = appearances.spritesheet;
        _tileMap[x][y].appearanceSource = source;
    }

    public int pixelsToTilesX(double pixels) {
        return (int) Math.floor(pixels / _tileWidth);
    }

    public int tilesToPixelsX(int numTiles) {
        return numTiles * _tileWidth;
    }

    public int pixelsToTilesY(double pixels) {
        return (int) Math.floor(pixels / _tileHeight);
    }

    public int tilesToPixelsY(int numTiles) {
        return numTiles * _tileHeight;
    }

    public int pixelTileX(double coords) {
        return tilesToPixelsX(pixelsToTilesX(coords));
    }

    public int pixelTileY(double coords) {
        return tilesToPixelsY(pixelsToTilesY(coords));
    }

    private int tileLeft;
    private int tileRight;
    private int tileTop;
    private int tileBottom;

    protected void tick() {
        if (!isLoaded || shouldRemove())
            return;

        // int screenLeft = 0;
        // int screenWidth = core.screen.getWidth();
        // int screenTop = 0;
        // int screenHeight = core.screen.getHeight();
        // tileLeft = JevaUtils.clampInt(pixelsToTilesX(screenLeft - this._x), 0,
        // _mapWidth);
        // tileRight = JevaUtils.clampInt(pixelsToTilesX(screenLeft - this._x +
        // screenWidth), 0, _mapWidth);
        // tileTop = JevaUtils.clampInt(pixelsToTilesX(screenTop - this._y), 0,
        // _mapHeight);
        // tileBottom = JevaUtils.clampInt(pixelsToTilesX(screenTop - this._y +
        // screenHeight), 0, _mapHeight);

        // run all attached scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
        }

        // for (int y = tileTop; y < tileBottom; y++) {
        // for (int x = tileLeft; x < tileRight; x++) {
        // try {
        // if (_tileMap[x][y].appearanceType == appearances.spritesheet) {
        // JevaSpriteSheet spritesheet = (JevaSpriteSheet)
        // _tileMap[x][y].appearanceSource;
        // spritesheet.tick();
        // }
        // } catch (Exception e) {
        // }
        // }
        // }
    }

    @Override
    protected void render(Graphics2D ctx, JevaClipProps parentProps) {
        render(ctx, new Rectangle2D.Double(0, 0, core.screen.getWidth(), core.screen.getHeight()));
    }

    protected void render(Graphics2D ctx, Rectangle2D.Double viewportBounds) {
        if (!isLoaded || shouldRemove() || !props._visible)
            return;

        int _width = Math.max(Math.abs(JevaUtils.roundInt(this._tileWidth)), 1);
        int _height = Math.max(Math.abs(JevaUtils.roundInt(this._tileHeight)), 1);
        BufferedImage painting = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pCtx = (Graphics2D) painting.getGraphics();
        pCtx.setColor(Color.GRAY);
        pCtx.fillRect(0, 0, _width, _height);

        pCtx.setColor(Color.DARK_GRAY);
        pCtx.fillRect((_width * 3 / 5), 0, (_width / 5), _height);
        pCtx.fillRect(0, (_height * 2 / 5), _width, (_height / 5));

        double screenLeft = viewportBounds.x;
        double screenWidth = viewportBounds.width;
        double screenTop = viewportBounds.y;
        double screenHeight = viewportBounds.height;
        tileLeft = JevaUtils.clampInt(pixelsToTilesX(screenLeft - props._x), 0, _mapWidth);
        tileRight = JevaUtils.clampInt(pixelsToTilesX(screenLeft - props._x + screenWidth) + 1, 0, _mapWidth);
        tileTop = JevaUtils.clampInt(pixelsToTilesX(screenTop - props._y), 0, _mapHeight);
        tileBottom = JevaUtils.clampInt(pixelsToTilesX(screenTop - props._y + screenHeight) + 1, 0, _mapHeight);

        Composite old = ctx.getComposite();
        ctx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, props._alpha));

        for (int y = tileTop; y < tileBottom; y++) {
            for (int x = tileLeft; x < tileRight; x++) {
                int _x = JevaUtils.roundInt(props._x + tilesToPixelsX(x));
                int _y = JevaUtils.roundInt(props._y + tilesToPixelsY(y));
                try {
                    if (_tileMap[x][y].appearanceType == appearances.graphic) {
                        Image source = (Image) _tileMap[x][y].appearanceSource;
                        ctx.drawImage(source, _x, _y, _width, _height, null);
                    } else if (_tileMap[x][y].appearanceType == appearances.spritesheet) {
                        Image source = ((JevaSpriteSheet) _tileMap[x][y].appearanceSource).getSource();
                        ctx.drawImage(source, _x, _y, _width, _height, null);
                    } else if (_tileMap[x][y].tileCode != ' ' && _tileMap[x][y].tileType == "") {
                        ctx.drawImage(painting, _x, _y, _width, _height, null);
                    }
                } catch (Exception e) {
                }
            }
        }
        pCtx.dispose();

        ctx.setComposite(old);

        if (core.isDebugMode()) {
            ctx.setColor(Color.ORANGE);
            for (int y = tileTop; y < tileBottom; y++) {
                for (int x = tileLeft; x < tileRight; x++) {
                    int _x = JevaUtils.roundInt(props._x + tilesToPixelsX(x));
                    int _y = JevaUtils.roundInt(props._y + tilesToPixelsY(y));
                    try {
                        ctx.setColor(Color.ORANGE);
                        ctx.drawRect(_x, _y, _width, _height);
                        Rectangle2D.Double anchor = new Rectangle2D.Double(_x - 5, _y - 5, 10, 10);
                        ctx.setColor(Color.BLUE);
                        ctx.fill(anchor);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    protected Rectangle2D.Double getTileBoundingRectangle(int xTile, int yTile) {
        int tileXCord = tilesToPixelsX(xTile);
        int tileYCord = tilesToPixelsY(yTile);
        return new Rectangle2D.Double(tileXCord, tileYCord, _tileWidth, _tileHeight);
    }

    @Override
    protected Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(props._x, props._y, props._width, props._height);
    }

    @Override
    public boolean hitTest(double x, double y) {
        if (shouldRemove() || !props._visible)
            return false;

        Rectangle2D.Double thisRect = getBoundingRectangle();

        return thisRect.contains(x, y);
    }

    public boolean hitTest(double x, double y, String type) {
        return hitTest(x, y, new String[] { type });
    }

    public boolean hitTest(double x, double y, String[] type) {
        if (shouldRemove() || !props._visible)
            return false;

        double applicableX = x - props._x;
        double applicableY = y - props._y;

        int xTile = pixelsToTilesX(applicableX);
        int yTile = pixelsToTilesY(applicableY);
        if (xTile < 0 || xTile >= _mapWidth ||
                yTile < 0 || yTile >= _mapHeight)
            return false;
        String tileType = getTileType(xTile, yTile);
        if (tileType == null)
            return false;
        for (int i = 0; i < type.length; i++)
            if (tileType.equals(type[i]))
                return true;
        return false;
    }
}
