package cubicCastles.user;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProfileReadWrite {
    public static void updateUser(Qbee qbee) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/qbees").toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, qbee.getUserID()+".json");
        try {
            if(outFile.exists()){
                Files.move(Paths.get(outFile.getAbsolutePath()), Paths.get(new File(guildDir, qbee.getUserID()+"-backup.json").getAbsolutePath()), StandardCopyOption.ATOMIC_MOVE);
            }
            outFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Couldn't create " + outFile.getName() + " file at " + outFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(outFile, qbee);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Qbee getUser(String id) {
        ObjectMapper mapper = new ObjectMapper();
        Qbee u = null;

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File guildDir = new File(workingDir.resolve("db/qbees").toUri());
        if (!guildDir.exists()) {
            guildDir.mkdirs();
        }
        File outFile = new File(guildDir, id+".json");
        try {
            u = mapper.readValue(outFile, Qbee.class);
        } catch (IOException e) {
            System.out.println("Couldn't create " + outFile.getName() + " file at " + outFile.getAbsolutePath());
            e.printStackTrace();
        }
        return u;
    }

    public static void loadAllUsers(){
        String[] files = new File(System.getProperty("user.dir") + "/db/qbees").list();
        if (files == null) {
            return;
        }
        for (String userID : files) {
            ProfileCommand.qbees.add(getUser(userID.replaceAll("[^0-9]", "")));
        }
    }
}
