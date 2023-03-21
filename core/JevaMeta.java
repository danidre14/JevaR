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
}
