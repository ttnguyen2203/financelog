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

import java.io.File;
import java.net.URL;

import jocr.src.main.java.ReceiptParser;
import org.bytedeco.javacpp.tesseract.*;


public class ReceiptParserImpl implements ReceiptParser {
    TessBaseAPI api;
    static final String charLimit = null; //= "0123456789:$.,/ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public void ReceiptParserImpl() {
        try {
            final URL tessDataResource = getClass().getResource("/tessdata");
            final File tessFolder = new File(tessDataResource.toURI());
            final String tessFolderPath = tessFolder.getAbsolutePath();
            //System.out.println(tessFolderPath);

            TessBaseAPI api = new TessBaseAPI();
            if (api.Init( tessFolderPath, "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            if (charLimit == null) {
                api.SetVariable("tessedit_char_whitelist", charLimit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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

        //API call
        //TODO: test this method with multiple inputs to make sure that api.SetImage does not need to be cleared
        BytePointer outText;
        api.SetImage(toMat.data().asBuffer(), toMat.size().width(), toMat.size().height(), toMat.channels(), (int)toMat.step());
        outText = api.GetUTF8Text();
        String string = outText.getString();
        outText.deallocate();
        return string;
    }

    /*
        call to close api instance
     */
    public void cleanUp() {
        api.End();
    }


    /*
        Debug version of method
        @param cropped_image: output of cropReceipt
    */
    public String readReceiptDebug(IplImage croppedImage) {
        // preprocessing
        IplImage preprocessed = preprocessReceipt(croppedImage);
        opencv_core.Mat toMat = opencv_core.cvarrToMat(preprocessed);

        try {
            final URL tessDataResource = getClass().getResource("/tessdata");
            //System.out.println(tessDataResource);
            final File tessFolder = new File(tessDataResource.toURI());
            final String tessFolderPath = tessFolder.getAbsolutePath();
            //System.out.println(tessFolderPath);
            BytePointer outText;
            TessBaseAPI api = new TessBaseAPI();
            if (api.Init( tessFolderPath, "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            //api.SetVariable("tessedit_char_whitelist", "0123456789:$.,/ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            api.SetImage(toMat.data().asBuffer(), toMat.size().width(), toMat.size().height(), toMat.channels(), (int)toMat.step());
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

    /*
        Handles conversion of data type from OpenCV (IplImage) to data type for Tesseract API (lept.PIX)
        @param image: IplImage returned from preprocessing / OpenCV data type
     */
    private PIX toPIXHack(IplImage image) {
        cvSaveImage("temp_PIX_hack.jpg", image);
        return pixRead("/temp_PIX_hack.jpg");
    }

    /*
        Method to preprocess cropped receipt image for better OCR performance
        Currently does: convert to grayscale, binarize, smooth
        @param image: cropped IplImage
     */
    private IplImage preprocessReceipt(IplImage image) {
        IplImage dstImage = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
        opencv_imgproc.cvCvtColor(image, dstImage, opencv_imgproc.CV_BGR2GRAY);
        opencv_imgproc.cvSmooth(dstImage, dstImage, opencv_imgproc.CV_MEDIAN, 3, 0, 0, 0);
        opencv_imgproc.cvThreshold(dstImage, dstImage, 0, 255, opencv_imgproc.CV_THRESH_OTSU)
        return dstImage;
    }

}
