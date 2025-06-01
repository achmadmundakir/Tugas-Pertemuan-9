package multibahasa;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("en"));

    public static void setLanguage(String langCode) {
        bundle = ResourceBundle.getBundle("messages", new Locale(langCode));
    }

    public static String get(String key) {
        return bundle.getString(key);
    }
}