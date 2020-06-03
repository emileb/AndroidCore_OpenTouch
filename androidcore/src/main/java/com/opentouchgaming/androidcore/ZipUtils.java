package com.opentouchgaming.androidcore;

import android.util.Log;

import com.opentouchgaming.saffal.FileSAF;
import com.opentouchgaming.saffal.UtilsSAF;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static boolean extractFile(String zipFilename, String filename, String outputFile) {

        Log.i("ZipUtils", "extractFile zip: " + zipFilename + " Extract: " + filename + " Output: " + outputFile);

        boolean fileFound = false;

        if (UtilsSAF.isInSAFRoot(zipFilename)) {
            try {
                FileSAF file = new FileSAF(zipFilename);
                InputStream inputStream = file.getInputStream();
                ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {

                    if (entry.getName().contentEquals(filename)) {

                        Log.i("ZipUtils", "FOUND");

                        new File(outputFile).getParentFile().mkdirs();

                        OutputStream out = new FileOutputStream(outputFile);
                        Utils.copyFile(zipInputStream, out);
                        inputStream.close();
                        fileFound = true;
                        break;
                    }
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                File file = new File(zipFilename);
                if (file != null) {
                    ZipFile zipFile = new ZipFile(file);
                    File outFile = new File(outputFile);
                    FileHeader header = zipFile.getFileHeader(filename);
                    if (header != null) {
                        zipFile.extractFile(header, outFile.getParent(), outFile.getName());
                        fileFound = true;
                    }
                }
            } catch (ZipException e) {
                e.printStackTrace();
                fileFound = false;
            }
        }
        return fileFound;
    }

    // Different from above because it is just looking at the filename in any directory
    public static boolean searchZip(String zipFilename, String searchName, String outputFile) {
        if (UtilsSAF.isInSAFRoot(zipFilename)) {
            try {
                FileSAF file = new FileSAF(zipFilename);
                InputStream inputStream = file.getInputStream();
                ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    //Log.e("field", entry.getName());
                    if (entry.getName().toLowerCase().contains(searchName)) {
                        OutputStream out = new FileOutputStream(outputFile);
                        Utils.copyFile(zipInputStream, out);
                        inputStream.close();
                        return true;
                    }
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            boolean fileFound = false;
            try {
                File file = new File(zipFilename);

                if (file != null) {
                    ZipFile zipFile = new ZipFile(file);
                    for (FileHeader header : (List<FileHeader>) zipFile.getFileHeaders()) {
                        if (!header.isDirectory()) {
                            // log.log(D,header.getFileName());
                            if (header.getFileName().toLowerCase().contains(searchName)) {
                                File outFile = new File(outputFile);
                                zipFile.extractFile(header, outFile.getParent(), outFile.getName());
                                fileFound = true;
                                break;
                            }
                        }
                    }
                }
            } catch (ZipException e) {
                e.printStackTrace();
            }
            return fileFound;
        }
    }

    public static ZipFile findAllFilesExtension(String zipFilename, String extension, ArrayList<String> output) throws ZipException {
        File file = new File(zipFilename);

        ZipFile zipFile = new ZipFile(file);
        for (FileHeader header : (List<FileHeader>) zipFile.getFileHeaders()) {
            if (!header.isDirectory()) {
                // log.log(D,header.getFileName());
                if (header.getFileName().toLowerCase().endsWith(extension)) {
                    output.add(header.getFileName());

                }
            }
        }
        return zipFile;
    }

    public static InputStream getInputStream(String zipFilename, String filename) {

        ZipFile zip = new ZipFile(zipFilename);
        InputStream is = null;
        try {
            is = zip.getInputStream(zip.getFileHeader(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
/*
       //java.util.zip.ZipFile zf = new java.util.zip.ZipFile()
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(zipFilename);
            // this is where you start, with an InputStream containing the bytes from the zip file
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry entry;
            // while there are entries I process them
            zis.
            while ((entry = zis.getNextEntry()) != null)
            {
                System.out.println("entry: " + entry.getName() + ", " + entry.getSize());
                // consume all the data from this entry
                while (zis.available() > 0)
                    zis.read();
                // I could close the entry, but getNextEntry does it automatically
                // zis.closeEntry()
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }
}
