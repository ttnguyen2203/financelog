package jocr.src.main.java;
import org.bytedeco.javacpp.opencv_core.IplImage;

public interface ReceiptParser {
    abstract IplImage cropReceipt(String path, float[][] points);
    abstract String readReceipt(IplImage croppedImage);
}
