import com.congyu.word.generate.XDocReportBaseImage;
import com.congyu.word.generate.XDocReportUtil;
import org.apache.poi.xwpf.usermodel.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class ExampleXDocReportUtil {

    public static void main(String[] args) throws Exception {
        ExampleXDocReportData dataBean = new ExampleXDocReportData();
        // 普通内容
        dataBean.setName("testName");

        // 循环内容
        dataBean.setList(Arrays.asList("item1", "item2"));

        // 表格内容
        List<ExampleXDocReportData.TableData> tableDataList = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            ExampleXDocReportData.TableData tableData = new ExampleXDocReportData.TableData();
            tableData.setCol1("col1_"+ i);
            tableData.setCol2("col2_"+ i);
            tableDataList.add(tableData);
        }
        dataBean.setTableDataList(tableDataList);

        // 图片


        InputStream inputStream = ExampleXDocReportUtil.class.getClassLoader().getResourceAsStream("TestImage.png");
        if (inputStream == null) {
            throw new RuntimeException("无法找到资源文件：TestImage");
        }
        byte[] imageBytes = readAllBytes(inputStream);
        inputStream.close();

        dataBean.setImagePattern(Pattern.compile("\\{\\{IMAGE:\\d+(?:_\\d+)*\\}\\}"));

        Map<String, XDocReportBaseImage> imageFields = new HashMap<>();
        for (int i = 0; i < 3; i++){
            ExampleXDocReportImage image = new ExampleXDocReportImage();
            image.setImageBytes(imageBytes);
            image.setWidthPx(100);
            image.setHeightPx(100);
            image.setImageType(Document.PICTURE_TYPE_PNG);
            image.setImageName("image"+i+".png");
            imageFields.put("{{IMAGE:"+i+"}}", image);
        }
        dataBean.setImageFields(imageFields);

        XDocReportUtil.generateWordFile(dataBean);
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        // 创建一个字节数组输出流
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            // 设置一个 16KB 的缓冲区
            byte[] data = new byte[16384];

            // 循环读取直到流末尾 (-1)
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            // 刷新并转换为字节数组
            buffer.flush();
            return buffer.toByteArray();
        }
    }
}
