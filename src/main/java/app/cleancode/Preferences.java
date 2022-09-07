package app.cleancode;

public class Preferences {
  private static final java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(Preferences.class);
  
public static String getPreference(String key) {
  return preferences.get(key, "");
}
public static void setPreference(String key, String value) {
  preferences.put(key, value);
}
}
