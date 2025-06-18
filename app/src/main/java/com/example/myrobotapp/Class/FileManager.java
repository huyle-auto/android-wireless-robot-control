package com.example.myrobotapp.Class;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.sax.Element;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okio.BufferedSource;
import okio.Okio;
import okio.BufferedSink;
import okio.Source;

public class FileManager {
    public static final String TAG = "FileManager";

    public static void copyFolderFromAssets(Context context, String assetFolderName, String destFolderName) {
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();
            File destFolder = new File(internalStorage, destFolderName);

            // Create the destination folder if it doesn't exist
            if (!destFolder.exists()) {
                boolean isFolderCreated = destFolder.mkdirs();
                if (!isFolderCreated) {
                    Log.e("FileManager", "Failed to create directory: " + destFolder.getAbsolutePath());
                    return;
                }
            }

            // List all files and subdirectories in the asset folder
            String[] assets = context.getAssets().list(assetFolderName);

            if (assets != null) {
                for (String asset : assets) {
                    // Construct the full paths for the asset and destination
                    String assetPath = assetFolderName + File.separator + asset;
                    File destPath = new File(destFolder, asset);

                    // Check if the asset is a directory or file
                    if (context.getAssets().list(assetPath).length > 0) {
                        // It's a folder; recursively copy it
                        copyFolderFromAssets(context, assetPath, destFolderName + File.separator + asset);
                    } else {
                        // It's a file; copy it
                        copyFileFromAssets(context, assetPath, destFolderName, asset);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFileFromAssets(Context context, String assetFileName, String destFolderName, String destFileName) {
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();
            File destFolder = new File(internalStorage, destFolderName);    // Just parsing destination folder name, will create later (if not created)

            // Create the destination folder if needed
            if (!destFolder.exists()) {
                boolean isFolderCreated = destFolder.mkdirs(); // or mkdirs() for multiple levels of folders
                if (!isFolderCreated) {
                    // Handle the error, e.g., log it or throw an exception
                    Log.e("FileManager", "Failed to create directory: " + destFolder.getAbsolutePath());
                    return; // Exit the method if the folder couldn't be created
                }
            }

            // Define the destination file
            File destFile = new File(destFolder, destFileName);

            // Open the asset file as InputStream
            InputStream inputStream = context.getAssets().open(assetFileName);

            // Use Okio for more efficient file copying
            Source source = Okio.source(inputStream);
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));

            sink.writeAll(source);
            sink.close();
            source.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // WRITE (single file only / overwrites)
    public static void writeToDir(Context context, String destFolderName, String destFileName, File orgFile){
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();
            File destFolder = new File(internalStorage, destFolderName);    // Just parsing destination folder name, will create later (if not created)

            // Create the destination folder if needed
            if (!destFolder.exists()) {
                boolean isFolderCreated = destFolder.mkdirs(); // or mkdirs() for multiple levels of folders
                if (!isFolderCreated) {
                    // Handle the error, e.g., log it or throw an exception
                    Log.e("FileManager", "Failed to create directory: " + destFolder.getAbsolutePath());
                    return; // Exit the method if the folder couldn't be created
                }
            }

            // Define the destination file
            File destFile = new File(destFolder, destFileName);

            // Use Okio for more efficient file copying
            Source source = Okio.source(orgFile);
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));

            sink.writeAll(source);
            sink.close();
            source.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // WRITE (single file only / append)
    public static void writeToDir(Context context, String destFolderName, String destFileName, File orgFile, boolean append){
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();
            File destFolder = new File(internalStorage, destFolderName);    // Just parsing destination folder name, will create later (if not created)

            // Create the destination folder if needed
            if (!destFolder.exists()) {
                boolean isFolderCreated = destFolder.mkdirs(); // or mkdirs() for multiple levels of folders
                if (!isFolderCreated) {
                    // Handle the error, e.g., log it or throw an exception
                    Log.e("FileManager", "Failed to create directory: " + destFolder.getAbsolutePath());
                    return; // Exit the method if the folder couldn't be created
                }
            }

            // Define the destination file
            File destFile = new File(destFolder, destFileName);

            // Use Okio for more efficient file copying
            Source source = Okio.source(orgFile);
            BufferedSink sink = Okio.buffer(Okio.appendingSink(destFile));

            sink.writeAll(source);
            sink.close();
            source.close();
            Log.i(TAG, "Written file to local storage");

        } catch (Exception e) {
            Log.i(TAG, "Cannot write file to local storage");
            e.printStackTrace();
        }
    }

    // DUPLICATE
    // 1. Acquire source folder name
    // 2. Generate duplicate folder name
    // 3. Call writeToDir to create new folder with duplicate name and same children file content
    // 4. Rename children files name
    public static void duplicateFolder(Context context, String sourceFolderName) {
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();

            // Locate the source folder within internal storage
            File sourceFolder = new File(internalStorage, sourceFolderName);
            if (!sourceFolder.exists()) {
                Log.e("FileManager", "Source folder does not exist: " + sourceFolder.getAbsolutePath());
                return;
            }

            // Generate the destination folder name
            String destFolderName = generateUniqueFolderName(internalStorage, sourceFolderName);

            // Perform the copy operation
            copyToDir(context, destFolderName, sourceFolderName);

            Toast.makeText(context, "Duplicated folder successfully as: " + destFolderName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to generate a unique folder name
    private static String generateUniqueFolderName(File internalStorage, String baseName) {
        String suffix = "_copy";
        String newFolderName = baseName + suffix;
        int counter = 0;

        // Increment the counter until a unique folder name is found
        while (new File(internalStorage, newFolderName).exists()) {
            counter++;
            newFolderName = baseName + suffix + (counter == 1 ? "" : "_" + counter);
        }

        return newFolderName;
    }

    // *********************************** IN MAINTENANCE **********************************************
    public static void copyToDir(Context context, String destFolderName, String sourceFolderName) {
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();

            // Locate the source folder within internal storage
            File sourceFolder = new File(internalStorage, sourceFolderName);
            if (!sourceFolder.exists()) {
                Log.e("FileManager", "Source folder does not exist: " + sourceFolder.getAbsolutePath());
                return;
            }

            // Generate the unique destination folder name
            File destFolder = new File(internalStorage, destFolderName);
            if (!destFolder.exists() && !destFolder.mkdirs()) {
                Log.e("FileManager", "Failed to create directory: " + destFolder.getAbsolutePath());
                return;
            }

            // Copy contents from source to destination
            if (sourceFolder.isDirectory()) {
                File[] files = sourceFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        copyFileOrDirectory(file, new File(destFolder, file.getName()));
                    }
                }
            } else {
                copyFileOrDirectory(sourceFolder, new File(destFolder, sourceFolder.getName()));
            }

            // Rename files in the destination folder by using the folder name suffix
            appendSuffixToFiles(destFolder);

            Toast.makeText(context, "Duplicated program successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void appendSuffixToFiles(File destFolder) {
        try {
            // Get the parent folder name
            String parentFolderName = destFolder.getName();

            // List all files in the destination folder
            File[] files = destFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Skip if it's not a file
                    if (!file.isFile()) continue;

                    // Get the file name and check for the "_Ctrl" part
                    String fileName = file.getName();
                    int dotIndex = fileName.lastIndexOf(".");

                    // Generate the new file name
                    String newFileName;
                    if (dotIndex > 0) {
                        // File has an extension
                        String extension = fileName.substring(dotIndex); // e.g., ".txt"
                        String baseName = fileName.substring(0, dotIndex); // e.g., "Test_Duplicate_Ctrl"

                        // Check if the file name contains "_Ctrl"
                        if (baseName.contains("_Ctrl")) {
                            // Preserve the "_Ctrl" part
                            newFileName = parentFolderName + "_Ctrl" + extension;
                        } else {
                            // No "_Ctrl", just append the parent folder name
                            newFileName = parentFolderName + extension;
                        }
                    } else {
                        // File has no extension
                        newFileName = parentFolderName;
                    }

                    // Rename the file
                    File renamedFile = new File(destFolder, newFileName);
                    if (file.renameTo(renamedFile)) {
                        System.out.println("Renamed: " + fileName + " -> " + newFileName);
                    } else {
                        System.err.println("Failed to rename: " + fileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to copy files or directories
    private static void copyFileOrDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists() && !destination.mkdirs()) {
                Log.e("FileManager", "Failed to create directory: " + destination.getAbsolutePath());
                return;
            }
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyFileOrDirectory(file, new File(destination, file.getName()));
                }
            }
        } else {
            // Use Okio for efficient file copying
            try (Source sourceStream = Okio.source(source); BufferedSink sink = Okio.buffer(Okio.sink(destination))) {
                sink.writeAll(sourceStream);
            }
        }
    }

    // **************************************************************************************************

    // DELETE
    public static void deleteFromDir(Context context, String destFolderName, String destFileName) {
        try {
            // Get the internal storage directory
            File internalStorage = context.getFilesDir();
            File destFolder = new File(internalStorage, destFolderName);

            // Check if destFileName is null or empty
            if (destFileName == null || destFileName.isEmpty()) {
                // Delete the whole folder if no specific file is provided
                deleteRecursively(destFolder);
            } else {
                // Delete only the specified file inside the folder
                File destFile = new File(destFolder, destFileName);
                if (destFile.exists()) {
                    boolean isFileDeleted = destFile.delete();
                    if (!isFileDeleted) {
                        Log.e("FileManager", "Failed to delete file: " + destFile.getAbsolutePath());
                    }
                } else {
                    Log.e("FileManager", "File not found: " + destFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to delete a folder and all its contents recursively.
     */
    private static void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursively(child);
            }
        }
        boolean isDeleted = fileOrDirectory.delete();
        if (!isDeleted) {
            Log.e("FileManager", "Failed to delete: " + fileOrDirectory.getAbsolutePath());
        }
    }

    public static String[] getFilesName(Context context, String folderName){
        File subfolder = new File(context.getFilesDir(), folderName);
        File[] files = subfolder.listFiles();

        String[] fileNames;
        if (files != null) {
            fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileNames[i] = files[i].getName();
            }
        } else {
            fileNames = new String[0]; // No files found
        }
        return fileNames;
    }

    public static File createTempTextFile(Context context, String text, String tempFileName) {
        // Create a temporary file in the cache directory
        File tempFile = new File(context.getCacheDir(), tempFileName + ".txt");

        try (BufferedSink sink = Okio.buffer(Okio.sink(tempFile))) {
            // Write the text to the file
            sink.writeUtf8(text);
            return tempFile; // Return the created file

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null if there's an error during file creation
        }
    }

    public static void convertProgramFile(Context context, String programName) {
        try {
            // Step 1: Locate the files in internal storage
            File dir = context.getFilesDir();
            File txtFile = new File(dir, "Program/" + programName + "/" + programName + ".txt");
            File pntFile = new File(dir, "Program/" + programName + "/" + programName + ".pnt");

            // Step 2: Read contents of the files with Okio
            String txtContent = readFile(txtFile);
            String pntContent = readFile(pntFile);

            // Step 3: Parse the .pnt file content into a map
            Map<String, String> pntMap = new HashMap<>();
            String[] pntLines = pntContent.split("\n");
            for (String line : pntLines) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length == 7) {
                        String point = "P[" + parts[0].trim() + "]";
                        StringBuilder coordinates = new StringBuilder("[");
                        for (int i = 1; i < parts.length; i++) {
                            coordinates.append(parts[i].trim());
                            if (i < parts.length - 1) {
                                coordinates.append(", ");
                            }
                        }
                        coordinates.append("]");
                        pntMap.put(point, coordinates.toString());
                    }
                }
            }

            // Step 4: Replace P references in .txt content with coordinates
            StringBuilder resultContent = new StringBuilder();
            for (Map.Entry<String, String> entry : pntMap.entrySet()) {
                String key = entry.getKey();
                String value = "POINT " + key + " = " + entry.getValue();
                resultContent.append(value);
                resultContent.append("\n");
                //resultContent = resultContent.replace(key, value);
            }

            // Step 5: Write result to a new file with Okio
            writeToDir(context, "Program/" + programName, programName+".ctrl", createTempTextFile(context, resultContent.toString(), "Anyname"));
            System.out.println("Written controller file to directory");

        } catch (Exception e) {
            System.out.println("Unable to create controller code file" + e.getMessage());
        }
    }

    public static String readFile(File file) throws IOException {
        // Reading the entire file content at once using Okio
        try (Source source = Okio.source(file); BufferedSource bufferedSource = Okio.buffer(source)) {
            return bufferedSource.readUtf8();
        }
    }

    // ******************************** JSON FILE PROCESSING *************************** //

    public static String loadJsonFromAsset(Context context, String fileName) {
        String json = null;
        try (InputStream is = context.getAssets().open(fileName)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }
}

