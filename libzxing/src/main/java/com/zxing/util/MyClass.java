package com.zxing.util;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;

public class MyClass {
    public static void main(String[] args){
        try {
            GenerateQRCode.generatePNG("123456789");//以默认如此生成的二维码
            GenerateQRCode.generateQRCODEForParams(ErrorCorrectionLevel.H,50,0xff49070d,0xff000000,"D:/123","123456789");//指定入参生成的二维码
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
