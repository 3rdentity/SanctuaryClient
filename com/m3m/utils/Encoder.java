/**
 * @author Thunder
 */
package com.m3m.utils;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

public class Encoder {

    public static void main(String[] args) {
        char c = (char) 13;
        try {
            String line1 = "Jon";
            String line2 = "Silva";

            String txt = line1 + c + line2;
            System.out.println(txt);

            String str = Encoder.convertTextToUTF16me(txt);
            System.out.println(str);

            str = Encoder.convertTextToUTF16me(" ");

            str = Encoder.convertTextToUTF16me(txt);
            System.out.println(str);

            str = Encoder.convertTextToUTF16be(txt);
            System.out.println(str);

            str = Encoder.convertTextToUTF16me(txt);
            System.out.println(str);

        } catch (UnsupportedEncodingException ex) {
        }

        Encoder encoder = new Encoder();
        String s1 = "\u0627\u0628\u062A";
        System.out.println("String: " + s1 + " Length: " + s1.length());
        s1 = encoder.getStringUTF(s1);
        System.out.println("String: " + s1 + " Length: " + s1.length());
    }

    private Encoder() {
    }
    static Charset charsetUTF16, charsetASCII;
    static CharsetDecoder decoderUTF16, decoderASCII;
    static CharsetEncoder encoderUTF16, encoderASCII;

    static {
        // Create the encoder and decoder for US-ASCII
        charsetASCII = Charset.forName("US-ASCII");
        decoderASCII = charsetASCII.newDecoder();
        encoderASCII = charsetASCII.newEncoder();

        // Create the encoder and decoder for UTF-16
        charsetUTF16 = Charset.forName("UTF-16");
        decoderUTF16 = charsetUTF16.newDecoder();
        encoderUTF16 = charsetUTF16.newEncoder();
    }

    public static String getStringUTF(String txt) {
        for (int i = 0; i < txt.length(); i++) {
            System.out.println("" + ((int) txt.charAt(i)));
        }

        // Create a direct ByteBuffer.
        // This buffer will be used to send and recieve data from channels.
        ByteBuffer bbuf = ByteBuffer.allocateDirect(txt.length() * 2);

        // Create a non-direct character ByteBuffer
        CharBuffer cbuf = CharBuffer.allocate(txt.length());

        for (int i = 0; i < txt.length(); i++) {
            cbuf = cbuf.append(txt.charAt(i));
        }

        // Convert characters in cbuf to bbuf
        encoderASCII.encode(cbuf, bbuf, false);

        // flip bbuf before reading from it
        bbuf.flip();

        String s = new String(cbuf.array());

        for (int i = 0; i < cbuf.capacity(); i++) {
            System.out.println("" + ((int) cbuf.array()[i]));
        }

        return s;
    }

    private static String convertTextToUTF16(String text, boolean _16be) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        byte[] b = text.getBytes("UTF-16");

        // Start from index [2], to skip the first two bytes (-2, -1):
        for (int i = 2; i < b.length; i++) {
            if (b[i] <= 9) {
                builder.append("0");
            }

            String t = Integer.toHexString(b[i]).toUpperCase();
            // New Line case:
            if (t.equals("D") || t.equals("A")) {
                builder.append("0");
            }
            builder.append(t);

            // Adding the Space between characters: 00" "79
            if (_16be && i < b.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static String convertTextToUTF16be(String text) throws UnsupportedEncodingException {
        return convertTextToUTF16(text, true);
    }

    public static String convertTextToUTF16me(String text) throws UnsupportedEncodingException {
        return convertTextToUTF16(text, false);
    }

    public static String convertUTF16meToStr(String utf16me) throws IOException {
        String tmp = "";

        int length = utf16me.length() / 2;
        byte[] buf = new byte[length];
        int index = 0;
        for (int i = 0; i < buf.length; i++) {
            tmp = utf16me.substring(i * 2, i * 2 + 2);
            buf[index] = Byte.parseByte(tmp);
        }
        return new String(buf, "UTF-16");
    }
}
