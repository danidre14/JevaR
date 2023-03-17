package core;

public class JevaMeta {
    JevaR parent;

    protected JevaMeta(JevaR parent) {
        this.parent = parent;
    }

    public void closeApplication() {
        if (this.parent != null)
            System.exit(0);
    }
}
