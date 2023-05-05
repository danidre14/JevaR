package core;

public class JevaMeta {
    JevaR core;

    protected JevaMeta(JevaR core) {
        this.core = core;
    }

    public void closeApplication() {
        if (this.core != null)
            System.exit(0);
    }

    public void setFullscreen(boolean value) {
        boolean isFullscreen = core.screen.isFullscreen();
        if (value && !isFullscreen) {
            core.screen.requestFullscreen();
        } else if (!value && isFullscreen) {
            core.screen.exitFullscreen();
        }
    }

    public void toggleFullscreen() {
        boolean isFullscreen = core.screen.isFullscreen();
        if (!isFullscreen) {
            core.screen.requestFullscreen();
        } else if (isFullscreen) {
            core.screen.exitFullscreen();
        }
    }

    public boolean isFullscreen() {
        return core.screen.isFullscreen();
    }

    public int getScreenWidth() {
        return core.screen.getScreenWidth();
    }

    public int getScreenHeight() {
        return core.screen.getScreenHeight();
    }

    public String getCursorType() {
        return core.screen._getCursorType();
    }

    public void setCursor(String type) {
        core.screen._setCursorType(type);
    }
}
