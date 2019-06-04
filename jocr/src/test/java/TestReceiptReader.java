import jocr.src.main.java.ReceiptReader;
import org.bytedeco.javacpp.opencv_core;

public class TestReceiptReader {

    public static void main(String[] args) {
        String path = "D:/Projects/financelog/ocr/test_images/002.jpg";
        ReceiptReader test = new ReceiptReader();
        opencv_core.IplImage read = test.getFile(path);
        opencv_core.IplImage cannied = test.applyCanny(read, 50);
        opencv_core.CvSeq contour = test.applyContours(cannied);
        opencv_core.IplImage preprocessed = test.applyPerspectiveTransform(read, contour, 50);
    }

}
