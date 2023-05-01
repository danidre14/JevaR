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
            return (boolean) _getOrThrow(name);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getBoolean(String name, boolean def) {
        try {
            return (boolean) _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, (boolean) def);
                return (boolean) _getOrThrow(name);
            } catch (Exception ee) {
                return false;
            }
        }
    }

    public int getInt(String name) {
        try {
            return (int) _getOrThrow(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getInt(String name, int def) {
        try {
            return (int) _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, (int) def);
                return (int) _getOrThrow(name);
            } catch (Exception ee) {
                return 0;
            }
        }
    }

    public float getFloat(String name) {
        try {
            return (float) _getOrThrow(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public float getFloat(String name, float def) {
        try {
            return (float) _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, (float) def);
                return (float) _getOrThrow(name);
            } catch (Exception ee) {
                return 0;
            }
        }
    }

    public double getDouble(String name) {
        try {
            return (double) _getOrThrow(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getDouble(String name, double def) {
        try {
            return (double) _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, (double) def);
                return (double) _getOrThrow(name);
            } catch (Exception ee) {
                return 0;
            }
        }
    }

    public long getLong(String name) {
        try {
            return (long) _getOrThrow(name);
        } catch (Exception e) {
            return 0;
        }
    }

    public long getLong(String name, long def) {
        try {
            return (long) _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, (long) def);
                return (long) _getOrThrow(name);
            } catch (Exception ee) {
                return 0;
            }
        }
    }

    public String getString(String name) {
        try {
            return (String) _getOrThrow(name);
        } catch (Exception e) {
            return "";
        }
    }

    public String getString(String name, String def) {
        try {
            return (String) _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, (String) def);
                return (String) _getOrThrow(name);
            } catch (Exception ee) {
                return "";
            }
        }
    }

    public Object getState(String name) {
        try {
            return _getOrThrow(name);
        } catch (Exception e) {
            return null;
        }
    }

    public Object getState(String name, Object def) {
        try {
            return _getOrThrow(name);
        } catch (Exception e) {
            try {
                states.put(name, def);
                return _getOrThrow(name);
            } catch (Exception ee) {
                return null;
            }
        }
    }
    

    private Object _getOrThrow(String key) throws Exception {
        Object val = states.get(key);
        if (val == null)
            throw new Exception("Can't find");
        return val;
    }

    public void setBoolean(String name, boolean value) {
        states.put(name, value);
    }

    public void setInt(String name, int value) {
        states.put(name, value);
    }

    public void setFloat(String name, float value) {
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
            states.put(name, getBoolean(name) ^ value);
        } catch (Exception e) {
        }
    }

    public void alterInt(String name, int value) {
        try {
            states.put(name, getInt(name) + value);
        } catch (Exception e) {
        }
    }

    public void alterFloat(String name, float value) {
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
