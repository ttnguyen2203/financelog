import jocr.src.main.java.ReceiptParserImpl;

public class ReceiptParserImplTest {

    public static void main(String[] args) {
        String path = "D:/Projects/financelog/ocr/test_images/002.jpg";
        ReceiptParserImpl test = new ReceiptParserImpl();
        float [][] coords = {{755, 1773}, {715, 851}, {2748, 775}, {2780, 1701} };
        test.cropReceipt(path, coords);
    }
}
