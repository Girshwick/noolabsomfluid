package org.NooLab.utilities.zip;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
 
/**
 * Demonstrates the ability to display the contents of a zip file Also
 * demonstrates how to get the zip info like compressed size, CRC etc.
 */
public class ZipContentDisplay {
 
    /**
     * This method takes the zipfile name input parameter Then lists the content
     * of the zip file.
     */
    public static void listContentsOfZipFile(String zipFileName) {
 
        try {
            ZipFile myZipFile = new ZipFile(zipFileName);
            Enumeration zipEntries = myZipFile.entries();
 
            ZipEntry zipEntry = null;
 
            while (zipEntries.hasMoreElements()) {
                zipEntry = (ZipEntry) zipEntries.nextElement();
                System.out.println(zipEntry.getName());
            }
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
 
    }
 
    public static void listContentsOfZipFileWithInfo(String zipFileName) {
 
        try {
            ZipFile myZipFile = new ZipFile(zipFileName);
            Enumeration zipEntries = myZipFile.entries();
 
            ZipEntry zipEntry = null;
 
            while (zipEntries.hasMoreElements()) {
                zipEntry = (ZipEntry) zipEntries.nextElement();
                /**
                 * Get the file name
                 */
                String nameOfEntry = zipEntry.getName();
                /**
                 * Get Compressed File Size
                 */
                long compressedSizeOfEntry = zipEntry.getCompressedSize();
                /**
                 * Get Uncompressed file size
                 */
                long uncompressedSizeOfEntry = zipEntry.getSize();
                /**
                 * Get the CRC Code. Note that we have converted to hex.
                 */
                String crc = Long.toHexString(zipEntry.getCrc());
                /**
                 * Get the comment for the zip entry
                 */
                String comment = zipEntry.getComment();
 
                /**
                 * Display the result
                 */
                System.out.println("FileName: " + nameOfEntry
                        + "\nCompressed Size(B): " + compressedSizeOfEntry
                        + "\nOriginal Size(B): " + uncompressedSizeOfEntry
                        + "\nCRC :" + crc + "\nComments: " + comment);
                System.out.println("***");
            }
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
 
    }
 
    public static void main(String[] args) {
 
        String zipFileName = "C:/Temp/abc.zip";
        System.out.println("***Zip Content Display (File Names Only)***\n");
        listContentsOfZipFile(zipFileName);
 
        System.out
                .println("\n\n***Zip Content Display (Detailed Information)***\n");
        listContentsOfZipFileWithInfo(zipFileName);
 
    }
}
