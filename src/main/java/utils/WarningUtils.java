package utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WarningUtils {
    public static Map<String, Integer> getWarnings(String guildID) throws NullPointerException {
        Map<String, Integer> commands = new HashMap<String, Integer>();

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guildID).toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, "warnings.txt");
        try {
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + outFile.getName() + " file at " + outFile.getAbsolutePath());
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(outFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ", 2);
                commands.put(split[0], Integer.parseInt(split[1]));
            }
            br.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return commands;
    }

    public static void addWarning(String guildID, String userID, int warnNum) throws NullPointerException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guildID).toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, "warnings.txt");
        try {
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + outFile.getName() + " file at " + outFile.getAbsolutePath());
        }

        try (FileWriter fw = new FileWriter(outFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(userID + " " + warnNum);
        } catch (IOException e) {
        }
    }

    public static void removeWarning(String guildID, String userID, int warnNum) throws NullPointerException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guildID).toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File file = new File(guildDir, "warnings.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + file.getName() + " file at " + file.getAbsolutePath());
        }

        File temp = null;
        try {
            temp = File.createTempFile("warn", ".txt", file.getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String charset = "UTF-8";
        String delete = userID + " " + warnNum;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), charset));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.split(" ")[0].trim().equalsIgnoreCase(userID)) {
                    line = line.replace(delete, "").trim();
                }
                if (!line.equals("")) // don't write out blank lines
                {
                    writer.println(line);
                }
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        file.delete();
        temp.renameTo(file);
    }

    public static void setWarning(String guildID, String userID, int oldWarnNum, int newWarnNum) throws NullPointerException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/servers/" + guildID).toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File file = new File(guildDir, "warnings.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + file.getName() + " file at " + file.getAbsolutePath());
        }

        File temp = null;
        try {
            temp = File.createTempFile("cstmrep", ".txt", file.getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), Charset.defaultCharset()));
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.split(" ")[0].equals(userID)) {
                    line = line.split(" ")[0] + " " + line.split(" ")[1].replace(Integer.toString(oldWarnNum), Integer.toString(newWarnNum)).trim();
                }
                if (!line.equals("")) // don't write out blank lines
                {
                    writer.println(line);
                }
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        file.delete();
        temp.renameTo(file);
    }
}
