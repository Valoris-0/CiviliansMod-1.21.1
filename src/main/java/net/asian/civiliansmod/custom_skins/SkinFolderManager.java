package net.asian.civiliansmod.custom_skins;

import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkinFolderManager {

    private static final String BASE_FOLDER_NAME = "civiliansmod_skins";


    public static void ensureFolderExists(String subFolderName) {
        // Get the `.minecraft` folder
        File baseFolder = new File(MinecraftClient.getInstance().runDirectory, BASE_FOLDER_NAME);

        // Create the base folder if it doesn't exist
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }

        // Create the specific subfolder (e.g., "default" or "slim")
        File subFolder = new File(baseFolder, subFolderName);
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }
    }
    public static List<File> getSkinFiles(String subFolderName) {
        ensureFolderExists(subFolderName);

        File subFolder = new File(MinecraftClient.getInstance().runDirectory, BASE_FOLDER_NAME + "/" + subFolderName);

        // Get all `.png` files from the folder
        File[] skinFiles = subFolder.listFiles((dir, name) -> name.endsWith(".png"));
        List<File> skinFileList = new ArrayList<>();

        if (skinFiles != null) {
            for (File file : skinFiles) {
                skinFileList.add(file);
            }
        }

        return skinFileList;
    }

    public static void openFolder(String subFolderName) {
        // Ensure the folder exists
        ensureFolderExists(subFolderName);

        // Get the full path to the subfolder
        File folderToOpen = new File(MinecraftClient.getInstance().runDirectory, BASE_FOLDER_NAME + "/" + subFolderName);

        try {
            // Check the operating system and execute the appropriate command
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                // Windows: Use the "explorer" command
                new ProcessBuilder("explorer", folderToOpen.getAbsolutePath()).start();
            } else if (osName.contains("mac")) {
                // macOS: Use the "open" command
                new ProcessBuilder("open", folderToOpen.getAbsolutePath()).start();
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                // Linux/Unix: Use the "xdg-open" command
                new ProcessBuilder("xdg-open", folderToOpen.getAbsolutePath()).start();
            } else {
                // If OS is not recognized, print a warning to the log
                System.err.println("Unknown operating system. Cannot open folder.");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log the error if something goes wrong
        }
    }
}