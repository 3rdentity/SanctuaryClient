package com.m3m.utils;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.List;
import java.util.jar.*;
import java.util.zip.*;

import java.awt.*;

public class FileManager {

    public static void main(String[] args) {
        File f = new File("C:/test/");
        for (Object elem : listByRegex(f, "A(\\d){8}\\.(\\d){4}-(\\d){4}(.*)SS7Statistics")) {
            System.out.println(elem);
        }
    }

    private FileManager() {
    }
    private static byte[] buffer = new byte[1024 * 200];
    // Generic Files Filter:
    private static String regex = "*", extension = ".*";
    private static FilenameFilter filesFilterRegex = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.matches(regex);
        }
    };
    private static FilenameFilter filesFilterExtension = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            String ext = (name.lastIndexOf(".") == -1) ? "" : name.substring(name.lastIndexOf(".") + 1, name.length());
            return ext.equalsIgnoreCase(extension);
        }
    };

    public static String[] listByRegex(File f, String regex) {
        FileManager.regex = regex;
        return f.list(filesFilterRegex);
    }

    public static String[] listByExtension(File f, String extension) {
        FileManager.extension = (extension.charAt(0) == '.') ? extension.substring(1) : extension;
        return f.list(filesFilterExtension);
    }

    public static void write(String data, String fileName) throws IOException {
        File file = new File(fileName);
        write(data, file);
    }

    public static void write(String data, File file) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
        out.write(data);
        out.close();
    }

    public static void write(byte[] data, String fileName) throws IOException {
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.close();
    }

    public static void append(String data, String fileName) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        append(data, file);
    }

    public static void append(String data, File file) throws FileNotFoundException, IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
        out.write(data);
        out.close();
    }

    public static boolean compressBest(File file, String outputFile) {
        return compress(file, outputFile, Deflater.BEST_COMPRESSION);
    }

    public static boolean compress(File file, String outputFile, int level) {
        try {
            int length = (int) file.length();
            byte[] buf = new byte[length];

            FileInputStream fis = new FileInputStream(file);
            fis.read(buf, 0, buf.length);
            fis.close();

            CRC32 crc = new CRC32();
            ZipOutputStream zipStream = new ZipOutputStream((OutputStream) new FileOutputStream(outputFile));

            zipStream.setLevel(level);

            ZipEntry entry = new ZipEntry(file.getName());
            entry.setSize((long) buf.length);
            crc.reset();
            crc.update(buf);
            entry.setCrc(crc.getValue());
            zipStream.putNextEntry(entry);
            zipStream.write(buf, 0, buf.length);
            zipStream.finish();
            zipStream.close();
            return true;
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return false;
    }

    public static InputStream getAsInputStream(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("utf-8");
        return new ByteArrayInputStream(bytes);
    }

    public static String readAsString(String fileName) throws IOException {
        return readAsString(new File(fileName));
    }

    public static String readAsString(File file) throws IOException {
        int length = (int) file.length();
        StringBuilder fileData = new StringBuilder(length);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[length];
        int numRead = 0;
        if ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    public static byte[] readAsBytes(File file) throws IOException {
        return readAsBytes(file.getAbsolutePath());
    }

    public static byte[] readAsBytes(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public static List<String> readAsLines(String fileName) throws IOException {
        return readAsLines(new File(fileName));
    }

    public static List<String> readAsLines(File file) throws IOException {
        LinkedList<String> lines = new LinkedList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        return lines;
    }

    // Converts the contents of a file into a CharSequence, suitable for use by the regex package.
    public static CharSequence fromFile(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        FileChannel fc = fis.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }

    public static byte[] getJarResource(String fileName, Class clazz) throws IOException {
        InputStream in = clazz.getResourceAsStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] bytes = null;
        synchronized (buffer) {
            int totalBytes = bis.read(buffer);
            bytes = new byte[totalBytes];
            System.arraycopy(buffer, 0, bytes, 0, totalBytes);
        }

        in.close();
        bis.close();

        return bytes;
    }

    public static Image getImageFromJar(String fileName, Class clazz) throws IOException {
        byte[] bytes = getJarResource(fileName, clazz);
        return Toolkit.getDefaultToolkit().createImage(bytes);
    }

    public static List<String> getJarFileListing(String jarLocation, String filter) {
        List<String> files = new ArrayList<>();
        if (jarLocation == null) {
            return files; // Empty.
        }

        // Lets stream the jar file:
        JarInputStream jarInputStream = null;
        try {
            jarInputStream = new JarInputStream(new FileInputStream(jarLocation));
            JarEntry jarEntry;

            // Iterate the jar entries within that jar. Then make sure it follows the
            // filter given from the user.
            do {
                jarEntry = jarInputStream.getNextJarEntry();
                if (jarEntry != null) {
                    String fileName = jarEntry.getName();

                    // The filter could be null or has a matching regular expression.
                    if (filter == null || fileName.matches(filter)) {
                        files.add(fileName);
                    }
                }
            } while (jarEntry != null);
            jarInputStream.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to get Jar input stream from '" + jarLocation + "'", ioe);
        }
        return files;
    }
}