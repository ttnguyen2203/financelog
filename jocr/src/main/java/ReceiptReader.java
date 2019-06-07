package jocr.src.main.java;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_photo;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
/*
    @author: https://gmartinezgil.wordpress.com/2016/04/16/create-a-receipt-scanner-app-in-java-using-javacv-opencv-and-tesseract-ocr/
 */


public final class ReceiptReader {

    public String readImage(String file_path) {
        File image = new File(file_path);
        String absolute_path = image.getAbsolutePath();
        IplImage receipt = cvLoadImage(absolute_path);

        return null;
    }


    // HELPERS //
    /*
        resizes image to the given percent
     */
    public IplImage getFile(String file_path) {
        File image = new File(file_path);
        String absolute_path = image.getAbsolutePath();
        IplImage receipt = cvLoadImage(absolute_path);
        return receipt;
    }
    public IplImage applyDownScale(IplImage src, int percent) {
        IplImage dest = opencv_core.cvCreateImage(
                opencv_core.cvSize((src.width() * percent / 100),(src.height() * percent / 100))
                , src.depth(), src.nChannels());
        opencv_imgproc.cvResize(src, dest);
        return dest;
    }

    public IplImage applyCanny(IplImage src, int percent) {
        IplImage downscaled = applyDownScale(src, percent);
        IplImage grayed = opencv_core.cvCreateImage(opencv_core.cvGetSize(downscaled), opencv_core.IPL_DEPTH_8U, 1);
        opencv_imgproc.cvCvtColor(downscaled, grayed, opencv_imgproc.CV_BGR2GRAY);
        OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
        Frame grayedFrame = matConverter.convert(grayed);
        opencv_core.Mat grayedMat = matConverter.convert(grayedFrame);

        opencv_imgproc.GaussianBlur(grayedMat, grayedMat, new opencv_core.Size(5,5), 0.0, 0.0, BORDER_DEFAULT);
        IplImage dest = matConverter.convertToIplImage(grayedFrame);
        opencv_imgproc.cvErode(dest, dest);
        opencv_imgproc.cvDilate(dest, dest);
        opencv_imgproc.cvCanny(dest, dest, 100.0, 200.0);

//        File f = new File("D:/Projects/financelog/jocr/images/receipt_cannied.jpeg");
//        System.out.println(f.getAbsolutePath());
//        cvSaveImage(f.getAbsolutePath(), dest);

        return dest;
    }

    public opencv_core.CvSeq applyContours (IplImage canniedImage) {
        IplImage foundedContoursImage = cvCloneImage(canniedImage);
        CvMemStorage memory = CvMemStorage.create();
        CvSeq contours = new CvSeq();
        opencv_imgproc.cvFindContours(foundedContoursImage, memory, contours, Loader.sizeof(CvContour.class), opencv_imgproc.CV_RETR_LIST, opencv_imgproc.CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));

        int maxWidth = 0;
        int maxHeight = 0;
        CvRect contour;
        CvSeq seqFounded = null;
        CvSeq nextSeq = new CvSeq();
        for (nextSeq = contours; nextSeq != null; nextSeq = nextSeq.h_next()) {
            contour = opencv_imgproc.cvBoundingRect(nextSeq, 0);
            if ((contour.width() >= maxWidth) && (contour.height() >= maxHeight)) {
                maxWidth = contour.width();
                maxHeight = contour.height();
                seqFounded = nextSeq;
            }
        }
        CvSeq result = opencv_imgproc.cvApproxPoly(seqFounded, Loader.sizeof(CvContour.class),
                memory, opencv_imgproc.CV_POLY_APPROX_DP, opencv_imgproc.cvContourPerimeter(seqFounded) * 0.02, 0);
        for (int i = 0; i < result.total(); i++) {
            CvPoint v = new CvPoint(cvGetSeqElem(result, i));
            opencv_imgproc.cvDrawCircle(foundedContoursImage, v, 5, CvScalar.BLUE, 20, 8, 0);
            System.out.println("found point(" + v.x() + "," + v.y() + ")");
        }
        File f = new File("D:/Projects/financelog/jocr/images/receipt_cannied.jpeg");
        System.out.println(f.getAbsolutePath());
        cvSaveImage(f.getAbsolutePath(), foundedContoursImage);
        return result;
    }
    private IplImage cropImage( IplImage srcImage, int fromX, int fromY,
                                int toWidth, int toHeight){
        cvSetImageROI(srcImage, cvRect(fromX,fromY,toWidth,toHeight));
        IplImage destImage = cvCloneImage(srcImage);
        cvCopy(srcImage, destImage);
        return destImage;
    }

    public IplImage applyPerspectiveTransform(IplImage srcImage, CvSeq contour, int percent) {
        IplImage warpImage = cvCloneImage(srcImage);
        for (int i = 0; i < contour.total(); i ++) {
            CvPoint point = new CvPoint(cvGetSeqElem(contour, i));
            point.x((int) (point.x() * 100 / percent));
            point.y((int) (point.y() * 100 / percent));
        }
        CvPoint topRight = new CvPoint(cvGetSeqElem(contour, 0));
        CvPoint topLeft = new CvPoint(cvGetSeqElem(contour, 1));
        CvPoint bottomLeft = new CvPoint(cvGetSeqElem(contour, 2));
        CvPoint bottomRight = new CvPoint(cvGetSeqElem(contour, 3));
        int resultWidth = (int) (topRight.x() - bottomLeft.x());
        int bottomWidth = (int) (bottomRight.x() - bottomLeft.x());
        if (bottomWidth > resultWidth) {
            resultWidth = bottomWidth;
        }
        int resultHeight = (int) (bottomLeft.y() - topLeft.y());
        int bottomHeight = (int) (bottomRight.y() - topLeft.y());
        if (bottomHeight > resultHeight) {
            resultHeight = bottomHeight;
        }
        float[] sourcePoints = { topLeft.x(), topLeft.y(),
            topRight.x(), topRight.y(), bottomLeft.x(),
            bottomLeft.y(), bottomRight.x(), bottomRight.y()};
        for (int m = 0; m < sourcePoints.length; m ++) {
            System.out.println(sourcePoints[m]);

        }
        float[] destinationPoints = {0, 0, resultWidth, 0, 0, resultHeight, resultWidth, resultHeight};
        CvMat homography = cvCreateMat(3, 3, CV_32FC1);
        opencv_imgproc.cvGetPerspectiveTransform(sourcePoints, destinationPoints, homography);
        IplImage destImage = cvCloneImage(warpImage);
        opencv_imgproc.cvWarpPerspective(warpImage, destImage, homography, opencv_imgproc.CV_INTER_LINEAR, CvScalar.ZERO);
        IplImage cropped = cropImage(destImage, 0, 0, resultWidth, resultHeight);

        File f = new File("D:/Projects/financelog/jocr/images/receipt_preprocessed.jpeg");
        System.out.println(f.getAbsolutePath());
        cvSaveImage(f.getAbsolutePath(), cropped);

        return cropped;
    }







}
