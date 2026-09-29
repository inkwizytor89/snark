package org.enoch.snark.common;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HashDecoder {

    public static void main(String[] args) throws DecoderException {

        String input = "server;link";
        String encode = encode(input);
        System.out.println("encode: "+encode);
        String decode = decode(encode);
        System.out.println("decode: "+decode);
    }

    private static String encode(String input) {
        return Hex.encodeHexString(input.getBytes());
    }

    private static String decode(String input) {
        try {
            return new String(Hex.decodeHex(input));
        } catch (DecoderException e) {
            e.printStackTrace();
            throw new RuntimeException("Incorrect server hash: " + input);
        }
    }

    public static String parseLink(String input) {
        String decode = decode(input);
        return decode.split(";")[1];
    }
}
