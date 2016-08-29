package com.zxing.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

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
    /**以默认参数生成二维码*/
    public static void generatePNG(String content) throws WriterException, IOException {
        generateQRCODEForParams(ErrorCorrectionLevel.L,BITMAP_SIZE,0xffffffff,0xff000001,PROJECTEC_PATH+File.separator+content+".png",content);
    }

    /**
     *
     * @param erroCorrection   枚举值，排错率；分四个等级：L(7%)、M(15%)、Q(25%)、H(30%);
     *                         排错率越高可存储的信息越少，但对二维码清晰度的要求越小
     *                         （这个会影响二维码扫码的解析速度，排错率越高，解析速度越快）
     * @param size             尺寸大小
     * @param bgColor          背景色,16进制颜色值
     * @param fgColor          二维码颜色,16进制颜色值
     * @param savedPath        保存路径
     * @param content          二维码信息
     */
    public static void generateQRCODEForParams(ErrorCorrectionLevel erroCorrection,int size,int bgColor,int fgColor,String savedPath,String content)throws WriterException, IOException {
        if(savedPath==null){
            throw new IOException("存储二维码的路径不能为空!");
        }
        if(content==null||content.length()<=0){
            throw new IOException("生成二维码的内容不能为空!");
        }
        String format = "png";//
        if(!savedPath.endsWith("."+format)) savedPath += ("."+format);

        if(size<1) size = 1;
        int width = size; // 图像宽度
        int height = size; // 图像高度

        HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, erroCorrection);

//        String md5Content = getMD5(content)+"&"+content;
        System.out.println(content);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵

        if((fgColor&0xff000000)==0) fgColor = fgColor&0xff000000;
        if((bgColor&0xff000000)==0) bgColor = bgColor&0xff000000;
        MatrixToImageConfig config = new MatrixToImageConfig(fgColor, bgColor);

        Path path = FileSystems.getDefault().getPath(savedPath);
        MatrixToImageWriter.writeToPath(bitMatrix, format, path,config);// 输出图像
        System.out.println("输出成功.");
    }

//    public static String getMD5(String content) {
//        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
//        try {
//            byte[] btInput = content.getBytes();
//            // 获得MD5摘要算法的 MessageDigest 对象
//            MessageDigest mdInst = MessageDigest.getInstance("MD5");
//            // 使用指定的字节更新摘要
//            mdInst.update(btInput);
//            // 获得密文
//            byte[] md = mdInst.digest();
//            // 把密文转换成十六进制的字符串形式
//            int j = md.length;
//            char str[] = new char[j * 2];
//            int k = 0;
//            for (int i = 0; i < j; i++) {
//                byte byte0 = md[i];
//                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
//                str[k++] = hexDigits[byte0 & 0xf];
//            }
//            return new String(str);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
