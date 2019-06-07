import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static java.lang.Math.max;
import java.io.File;



public class ReceiptParserImpl implements ReceiptParser{

    public ReceiptParserImpl() {
    }

    /*
        Method to perform perspective transform on the given points to fit the receipt
            ROI to the entire image

        @param path: (String) path to picture of receipt
        @param points: array of paired coordinates [float x, float y], in order:
                top left, top right, bottom right, bottom left
     */
    public IplImage cropReceipt(String path, float[][] points) {
        File f = new File(path);
        String absolute_path = f.getAbsolutePath();
        IplImage receiptImage = cvLoadImage(absolute_path);

        float[] TL = points[0];
        float[] TR = points[1];
        float[] BR = points[2];
        float[] BL = points[3];

        // topright.x - bottomleft.x or bottomright.x - bottomeleft.x
        int width = max((int) (TR[0] - BL[0]), (int) (BR[0] - BL[0]));
        // bottomleft.y - topleft.y or bottomright.y - topleft.y
        int height = max((int) (BL[1] - TL[1]), (int) (BR[1] - TL[1]));








        return null;
    }


    /*
        Method read text from cropped receipt picture by converting the picture to
            gray-scale, binary threshold, and call tesseract API

        @params:
            - cropped_image: returned output of cropReceipt
     */

    public String readReceipt(IplImage croppedImage) {
        return null;
    }

}
