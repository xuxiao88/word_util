import com.congyu.word.generate.XDocReportBaseImage;
import lombok.Data;

@Data
public class ExampleXDocReportImage implements XDocReportBaseImage {

    private byte[] imageBytes;

    private int imageType;

    private String imageName;

    private int widthPx;

    private int heightPx;

}
