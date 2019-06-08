import jocr.src.main.java.ReceiptParserImpl;
import org.bytedeco.javacpp.opencv_core;

import java.io.File;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

public class ReceiptParserImplTest {

    public static void main(String[] args) {
        String path = "D:/Projects/financelog/ocr/test_images/002.jpg";
        ReceiptParserImpl test = new ReceiptParserImpl();
        int[][] coords = {{755, 1773}, {715, 851}, {2748, 775}, {2780, 1701} };
        opencv_core.IplImage cropped = test.cropReceipt(path, coords);

        File transformedPic = new File("D:/Projects/financelog/jocr/images/receipt_pertrans.jpeg");
        System.out.println(transformedPic.getAbsolutePath());
        cvSaveImage(transformedPic.getAbsolutePath(), cropped);

        String read = test.readReceipt(cropped);
        if (read == null) {
            System.out.println("null");
        } else {
            System.out.print(read);
        }
    }
}
