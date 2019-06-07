import org.bytedeco.javacpp.opencv_core.IplImage;

public interface ReceiptParser {
    public abstract IplImage cropReceipt(String path, float[][] points);
    public abstract String readReceipt(IplImage croppedImage);
}
