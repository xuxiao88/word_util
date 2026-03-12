import com.congyu.word.generate.XDocReportBaseData;
import com.congyu.word.generate.XDocReportBaseImage;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Data
class ExampleXDocReportData implements XDocReportBaseData {

    // 字段
    private String name;

    // 列表
    private List<String> list;

    // 表格
    private List<TableData> tableDataList;

    // 图片匹配模式
    private  Pattern imagePattern;
    // 图片
    private List<XDocReportBaseImage> imageFields;

    @Override
    public File getTemplateFile() {
        try {
            // 获取当前类的类加载器，从 test/resources 目录加载模板文件
            // 使用相对路径，确保在不同机器上只要项目结构一致即可运行
            java.net.URL resource = getClass().getClassLoader().getResource("ExampleTemplate.docx");
            if (resource == null) {
                throw new RuntimeException("模板文件未找到，请检查 src/test/resources 目录下是否存在 template.docx");
            }
            return new File(resource.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException("模板文件路径解析失败", e);
        }
    }

    @Override
    public String getTargetPath() {
        return System.getProperty("user.dir") + File.separator + "output.docx";
    }

    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("list", list);
        map.put("tableDataList", tableDataList);
        return map;
    }

    @Data
    public static class TableData {
        private String col1;
        private String col2;
    }
}