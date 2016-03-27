package com.zxing.util;

import com.google.zxing.WriterException;

import java.io.IOException;

public class MyClass {
    public static void main(String[] args){
        try {
            GenerateQRCode.generatePNG("123456789");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
