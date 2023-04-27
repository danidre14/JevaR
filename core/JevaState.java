package core;

import java.util.HashMap;
import java.util.Map.Entry;

public class JevaState {
    protected HashMap<String, Object> states;

    protected JevaState() {
        states = new HashMap<>();
    }

    private JevaState(HashMap<String, Object> states) {
        this.states = new HashMap<>(states);
    }

    public boolean getBoolean(String name) {
        try {
            return (boolean) states.get(name);
        } catch (Exception e) {
            return false;
        }
    }

    public int getInt(String name) {
        try {
            return (int) states.get(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getInt(String name, int def) {
        try {
            return (int) states.get(name);
        } catch (Exception e) {
            try {
                states.put(name, (int) def);
                return (int) states.get(name);
            } catch (Exception ee) {
                return 0;
            }
        }
    }

    public double getDouble(String name) {
        try {
            return (double) states.get(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public long getLong(String name) {
        try {
            return (long) states.get(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getString(String name) {
        try {
            return (String) states.get(name);
        } catch (Exception e) {
            return "";
        }
    }

    public Object getState(String name) {
        try {
            return states.get(name);
        } catch (Exception e) {
            return null;
        }
    }

    public void setBoolean(String name, boolean value) {
        states.put(name, value);
    }

    public void setInt(String name, int value) {
        states.put(name, value);
    }

    public void setDouble(String name, double value) {
        states.put(name, value);
    }

    public void setLong(String name, long value) {
        states.put(name, value);
    }

    public void setString(String name, String value) {
        states.put(name, value);
    }

    public void setState(String name, Object value) {
        states.put(name, value);
    }

    public void alterBoolean(String name, boolean value) {
        try {
            states.put(name, !getBoolean(name));
        } catch (Exception e) {
        }
    }

    public void alterInt(String name, int value) {
        try {
            states.put(name, getInt(name) + value);
        } catch (Exception e) {
        }
    }

    public void alterDouble(String name, double value) {
        try {
            states.put(name, getDouble(name) + value);
        } catch (Exception e) {
        }
    }

    public void alterLong(String name, long value) {
        try {
            states.put(name, getLong(name) + value);
        } catch (Exception e) {
        }
    }

    public void alterString(String name, String value) {
        try {
            states.put(name, getString(name) + value);
        } catch (Exception e) {
        }
    }

    protected JevaState clone() {
        return new JevaState(states);
    }

    protected void merge(JevaState otherState) {
        for (Entry<String, Object> set : otherState.states.entrySet()) {
            states.put(set.getKey(), set.getValue());
        }
    }
}
