package core;

public interface JevaFunction {
    Object call(JevaState state, Object ... args);
}
