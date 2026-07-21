import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A minimal JSON helper. We avoid pulling in an external library (like Gson)
// so the project has zero dependencies and compiles with just the JDK.
public class JsonUtil {

    // Escapes special characters so strings are safe inside JSON
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Extracts an integer value for a given key from a simple JSON body
    // e.g. body = {"productId":3,"quantity":2}  ->  getInt(body, "quantity") = 2
    public static int getInt(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalArgumentException("Key not found: " + key);
    }

    // Wraps a list of already-built JSON objects into a JSON array string
    public static String array(Iterable<String> jsonObjects) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String obj : jsonObjects) {
            if (!first) sb.append(",");
            sb.append(obj);
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
