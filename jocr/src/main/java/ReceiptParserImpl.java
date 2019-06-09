package jocr.src.main.java;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc;

import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static java.lang.Math.max;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;

import jocr.src.main.java.ReceiptParser;
import org.bytedeco.javacpp.tesseract.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.imageio.ImageIO;

public class ReceiptParserImpl implements ReceiptParser {


    /*
        Method to perform perspective transform on the given points to fit the receipt
            ROI to the entire image

        @param path: (String) path to picture of receipt
        @param points: array of paired coordinates [float x, float y], in order:
                top left, top right, bottom right, bottom left
     */
    public IplImage cropReceipt(String path, int[][] points) {
        File f = new File(path);
        String absolute_path = f.getAbsolutePath();
        IplImage receiptImage = cvLoadImage(absolute_path);

        int[] TL = points[0];
        int[] TR = points[1];
        int[] BR = points[2];
        int[] BL = points[3];

        int width = max(EuclideanDist(TL, TR), EuclideanDist(BL, BR));
        int height = max(EuclideanDist(TL, BL), EuclideanDist(TR, BR));

        float[] srcPoints = {TL[0], TL[1], TR[0], TR[1], BL[0], BL[1], BR[0], BR[1]};
        float [] dstPoints = {0, 0, width, 0, 0, height, width, height};

        CvMat M = cvCreateMat(3, 3, CV_32F);
        opencv_imgproc.cvGetPerspectiveTransform(srcPoints, dstPoints, M);

        IplImage dstImage = cvCloneImage(receiptImage);
        opencv_imgproc.cvWarpPerspective(receiptImage, dstImage, M);
        dstImage = cropImage(dstImage, 0, 0, width, height);

        return dstImage;
    }

    private IplImage cropImage( IplImage srcImage, int fromX, int fromY,
                                int toX, int toY){
        cvSetImageROI(srcImage, cvRect(fromX,fromY,toX,toY));
        IplImage destImage = cvCloneImage(srcImage);
        cvCopy(srcImage, destImage);
        return destImage;
    }

    private int EuclideanDist(int[] point1, int[] point2) {
        int x = Math.abs(point1[0] - point2[0]);
        int y = Math.abs(point1[1] - point2[1]);
        return (int) Math.sqrt(x * x + y * y);
    }


    /*
        Method read text from cropped receipt picture by converting the picture to
            gray-scale, binary threshold, and call tesseract API

        @params:
            - cropped_image: output of cropReceipt
     */
    public String readReceipt(IplImage croppedImage) {
        // preprocessing
        IplImage preprocessed = preprocessReceipt(croppedImage);

        opencv_core.Mat toMat = opencv_core.cvarrToMat(preprocessed);

//        try {
//            final URL tessDataResource = getClass().getResource("/tessdata");
//            final File tessFolder = new File(tessDataResource.toURI());
//            final String tessFolderPath = tessFolder.getAbsolutePath();
//            System.out.println(tessFolderPath);
//            BytePointer outText;
//            TessBaseAPI api = new TessBaseAPI();
//            api.SetVariable("tessedit_char_whitelist", "01234556789,/ABCDEFGHIJKLMNOPQRSTUVWXYZ");
//            if (api.Init(tessFolderPath, "eng") != 0) {
//                System.err.println("Could not initialize tesseract");
//            }
//            lept.PIX image = pixRead("D:/Projects/financelog/jocr/images/receipt_preprocessed.JPEG");
//            api.SetImage(image);
//            outText = api.GetUTF8Text();
//            String string = outText.getString();
//            api.End();
//            outText.deallocate();
//            pixDestroy(image);
//            return string;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }

        try {
            final URL tessDataResource = getClass().getResource("/tessdata");
            System.out.println(tessDataResource);
            final File tessFolder = new File(tessDataResource.toURI());
            final String tessFolderPath = tessFolder.getAbsolutePath();
            System.out.println(tessFolderPath);
            BytePointer outText;
            TessBaseAPI api = new TessBaseAPI();
            if (api.Init( tessFolderPath, "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            lept.PIX image = toPIX(preprocessed);
            api.SetVariable("tessedit_char_whitelist", "0123456789,/ABCDEFGHIJKLMNOPQRSTUVWXY");
            api.SetImage(toMat.data().asBuffer(), toMat.rows(), toMat.cols(), 1, (int)toMat.step());
            outText = api.GetUTF8Text();
            String string = outText.getString();
            api.End();
            outText.deallocate();
            return string;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    



    private IplImage preprocessReceipt(IplImage image) {
        IplImage dstImage = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
        opencv_imgproc.cvCvtColor(image, dstImage, opencv_imgproc.CV_BGR2GRAY);
        return dstImage;
    }

}
