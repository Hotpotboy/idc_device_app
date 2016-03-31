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
import java.security.MessageDigest;
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
        String md5Content = getMD5(content)+"&"+content;
        System.out.println(md5Content);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(md5Content,
                BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
        Path path = FileSystems.getDefault().getPath(PROJECTEC_PATH, content+".png");
        MatrixToImageWriter.writeToPath(bitMatrix, format, path);// 输出图像
        System.out.println(new String("输出成功.".getBytes("gbk"),"UTF-8"));
    }

    private static String getMD5(String content) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = content.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
