package repo;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RememberMe {
    private final File file;
    public RememberMe(String path){ this.file = new File(path); }

    public void save(String username){
        ensureParent();
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            pw.println(username == null ? "" : username.trim());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public String load(){
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String s = br.readLine();
            return (s == null || s.isBlank()) ? null : s.trim();
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    public void clear(){
        if (file.exists()) file.delete();
    }

    private void ensureParent(){
        File p = file.getParentFile();
        if (p != null) p.mkdirs();
    }
}
