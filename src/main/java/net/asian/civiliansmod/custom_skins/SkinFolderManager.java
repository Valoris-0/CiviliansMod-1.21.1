package net.asian.civiliansmod.custom_skins;

import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.io.IOException;
public class SkinFolderManager {

    private static final String BASE_FOLDER_NAME_1 = "civiliansmod_skins_default";
    private static final String BASE_FOLDER_NAME_2 = "civiliansmod_skins_slim";
    static {
        // Ensure both folders are created when the manager is initialized
        ensureFolderExists("default");
        ensureFolderExists("slim");
    }
    public static void ensureFolderExists(String subFolderName) {
        if (subFolderName.equalsIgnoreCase("both")) {

            //noinspection ResultOfMethodCallIgnored
            new File(MinecraftClient.getInstance().runDirectory, BASE_FOLDER_NAME_1).mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(MinecraftClient.getInstance().runDirectory, BASE_FOLDER_NAME_2).mkdirs();
            return;
        }

        // Proceed with creating only the specified folder
        String baseFolderName = subFolderName.equalsIgnoreCase("default") ? BASE_FOLDER_NAME_1 : BASE_FOLDER_NAME_2;
        File baseFolder = new File(MinecraftClient.getInstance().runDirectory, baseFolderName);

        if (!baseFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            baseFolder.mkdirs();
        }
    }


    public static void openFolder(String subFolderName) {
        // Determine the base folder name based on the subFolderName
        String baseFolderName = subFolderName.equalsIgnoreCase("default") ? BASE_FOLDER_NAME_1 : BASE_FOLDER_NAME_2;

        // Fetch the actual folder
        File folderToOpen = new File(MinecraftClient.getInstance().runDirectory, baseFolderName);

        try {
            // Open the folder based on the OS
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                new ProcessBuilder("explorer", folderToOpen.getAbsolutePath()).start();
            } else if (osName.contains("mac")) {
                new ProcessBuilder("open", folderToOpen.getAbsolutePath()).start();
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                new ProcessBuilder("xdg-open", folderToOpen.getAbsolutePath()).start();
            } else {
                System.err.println("Unknown operating system. Cannot open folder.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}