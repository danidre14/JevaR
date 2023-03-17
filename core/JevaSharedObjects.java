package core;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.StringTokenizer;

public class JevaSharedObjects {
    private String name;
    private String path;

    private static String dir;
    private static HashMap<String, JevaSharedObjects> sharedObjects;

    static {
        dir = "./storage/";
        sharedObjects = new HashMap<String, JevaSharedObjects>();
    }

    public static JevaSharedObjects getLocal(String sharedObjectName) {
        JevaSharedObjects so = sharedObjects.get(sharedObjectName);
        if (so != null)
            return so;

        so = new JevaSharedObjects(sharedObjectName);
        sharedObjects.put(sharedObjectName, so);
        return so;
    }

    private JevaSharedObjects(String sharedObjectName) {
        name = sharedObjectName;
        path = dir.concat("f_").concat(sharedObjectName).concat(".txt");
    }

    public String getItem(String key) {
        try {
            String ukey = _obfuscateString(key);
            int index = _indexOf(ukey);
            if (index == -1)
                return null;

            String line = _getLine(index);

            StringTokenizer st = new StringTokenizer(line, ":");
            st.nextToken(); // key
            String _uvalue = st.nextToken();
            String _value = _deobfuscateString(_uvalue);
            _cleanup();
            return _value;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setItem(String key, Object value) {
        try {
            String ukey = _obfuscateString(key);
            String uvalue = value == null ? null : _obfuscateString(value.toString());
            int index = _indexOf(ukey);

            String line = uvalue == null ? null : ukey + ":" + uvalue;
            if (index == -1) {
                if (uvalue != null) {
                    _addLine(line);
                }
            } else {
                _replaceLine(index, line);
            }
            _cleanup();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean removeItem(String key) {
        try {
            String ukey = _obfuscateString(key);
            int index = _indexOf(ukey);
            if (index != -1)
                _replaceLine(index, null);
            _cleanup();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String _getLine(int index) throws IOException {
        if (index == -1)
            return null;

        File file = new File(path);
        file.getParentFile().mkdirs();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        try {
            for (int i = 0; i <= index; i++) {
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
        return line;
    }

    private void _addLine(String line) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

        try {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bw.close();
        }
    }

    private void _replaceLine(int index, String replacement) throws IOException {
        File oldFile = new File(path);
        oldFile.getParentFile().mkdirs();
        File newFile = File.createTempFile(name.concat("_tmp"), ".txt", new File(dir));

        BufferedReader br = new BufferedReader(new FileReader(oldFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));

        try {
            for (int i = 0; i < index; i++) {
                bw.write(br.readLine());
                bw.newLine();
            }

            br.readLine();
            if (replacement != null) {
                bw.write(replacement);
                bw.newLine();
            }

            String _line;
            while ((_line = br.readLine()) != null) {
                bw.write(_line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
            bw.close();
        }

        if (oldFile.delete())
            newFile.renameTo(oldFile);
    }

    private void _cleanup() {
        File file = new File(path);
        if (!(file.exists() && !file.isDirectory()))
            return;
        try {
            if (_length() == 0) {
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int _length() throws IOException {
        File file = new File(path);
        if (!(file.exists() && !file.isDirectory()))
            return 0;

        BufferedReader br = new BufferedReader(new FileReader(file));
        int i = 0;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ":");

                if (st.nextToken().equals(""))
                    break;
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
        return i;
    }

    private int _indexOf(String key) throws IOException {
        File file = new File(path);
        if (!(file.exists() && !file.isDirectory()))
            return -1;

        BufferedReader br = new BufferedReader(new FileReader(file));
        int i = 0;
        boolean found = false;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ":");

                if (st.nextToken().equals(key)) {
                    found = true;
                    break;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }

        return found ? i : -1;
    }

    private static String _escapeString(String str) {
        return str.replaceAll("&", "&amp;")
                .replaceAll(">", "&gt;")
                .replaceAll("<", "&lt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;")
                .replaceAll(":", "&col;");
    }

    private static String _unescapeString(String str) {
        return str.replaceAll("&col;", ":")
                .replaceAll("&apos;", "'")
                .replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&");
    }

    private static String _encodeToBase64(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes());
    }

    private static String _decodeFromBase64(String encodedMessage) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedMessage);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    private static String _obfuscateString(String string) {
        return _encodeToBase64(_escapeString(string));
    }

    private static String _deobfuscateString(String string) {
        return _unescapeString(_decodeFromBase64(string));
    }
}
