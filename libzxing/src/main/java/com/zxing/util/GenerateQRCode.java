package com.zxing.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-03-27.
 */
public class GenerateQRCode {
    /**project path*/
    public static final String PROJECTEC_PATH = System.getProperty("user.dir")+ File.separator+"libzxing";
    public static final int BITMAP_SIZE = 200;
    public static void generatePNG(String content) throws WriterException, IOException {
        int width = BITMAP_SIZE; // 图像宽度
        int height = BITMAP_SIZE; // 图像高度
        String format = "png";//
        HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
        Path path = FileSystems.getDefault().getPath(PROJECTEC_PATH, content+".png");
        MatrixToImageWriter.writeToPath(bitMatrix, format, path);// 输出图像
        System.out.println(new String("输出成功.".getBytes("gbk"),"UTF-8"));
    }
}
