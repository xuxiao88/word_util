package com.congyu.word.generate;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class XDocReportUtil {


    public static File generateWordFile(XDocReportBaseDataBean dataBean) throws Exception {
        // 获取模版
        InputStream ins = Files.newInputStream(dataBean.getTemplatePath());
        //注册xdocreport实例并加载FreeMarker模板引擎
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(ins,
                TemplateEngineKind.Freemarker);

        //创建xdocreport上下文对象
        IContext context = report.createContext();

        Map<String,Object> dataMap = dataBean.getMap();
        context.putMap(dataMap);

        //创建字段元数据
        FieldsMetadata fm = report.createFieldsMetadata();
        for(Map.Entry<String,Class> keyClass : dataBean.getKeyClass().entrySet()){
            //Word模板中的表格数据对应的集合类型
            fm.load(keyClass.getKey(), keyClass.getValue(), true);
        }

        //输出到本地目录
        String filePath = ConfigPathUtil.getTempPath() + File.separator + dataBean.getTargetName();
        // 处理图片
        if (MapUtils.isNotEmpty(dataBean.getImageFields())){
            String tempFilePath = ConfigPathUtil.getTempPath() + File.separator + "temp_"+ dataBean.getTargetName();
            FileOutputStream out = new FileOutputStream(tempFilePath);
            report.process(context, out);
            File tempfile = new File(tempFilePath);
            File file = new File(filePath);

            replaceImagePlaceholdersInFile(
                    tempfile,
                    file,
                    dataBean.getImageFields(),
                    250,
                    150
            );

            return file;
        }

        FileOutputStream out = new FileOutputStream(filePath);
        report.process(context, out);
        return new File(filePath);
    }


    private static void replaceImagePlaceholdersInFile(
            File inputDocx,
            File outputDocx,
            Map<String, byte[]> imageMap,
            int widthPx,
            int heightPx) throws Exception {

        // 1. 读取输入文件
        try (FileInputStream fis = new FileInputStream(inputDocx);
             XWPFDocument doc = new XWPFDocument(fis)) {

            // 2. 执行替换
            replaceImagePlaceholdersInDocument(doc, imageMap, widthPx, heightPx);

            // 3. 写入输出文件
            try (FileOutputStream fos = new FileOutputStream(outputDocx)) {
                doc.write(fos);
            }
        }
    }

    private static void replaceImagePlaceholdersInDocument(
            XWPFDocument doc,
            Map<String, byte[]> imageMap,
            int widthPx,
            int heightPx) throws Exception {

        Pattern pattern = Pattern.compile("\\{\\{image:\\d+(?:_\\d+)*\\}\\}");

        // 替换正文段落
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            replaceInParagraph(paragraph, pattern, imageMap, widthPx, heightPx);
        }

        // 替换表格（含嵌套表格）
        for (XWPFTable table : doc.getTables()) {
            replaceInTable(table, pattern, imageMap, widthPx, heightPx);
        }
    }

    private static void replaceInTable(XWPFTable table, Pattern pattern, Map<String, byte[]> imageMap, int widthPx, int heightPx) throws Exception {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraph(p, pattern, imageMap, widthPx, heightPx);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    replaceInTable(nestedTable, pattern, imageMap, widthPx, heightPx);
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph paragraph, Pattern pattern, Map<String, byte[]> imageMap, int widthPx, int heightPx) throws Exception {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null) return;

        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null) continue;
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // 找到第一个匹配项（假设一个 run 只有一个占位符）
                String fullMatch = matcher.group(0);
                byte[] imageData = imageMap.get(fullMatch);

                // 清空文本
                run.setText("", 0);
                if (imageData != null) {
                    // 插入图片
                    int widthEmu = widthPx * 9525;
                    int heightEmu = heightPx * 9525;

                    run.addPicture(
                            new ByteArrayInputStream(imageData),
                            determineImageType(fullMatch),
                            sanitizeFileName(fullMatch) + ".png",
                            widthEmu,
                            heightEmu
                    );
                }
            }
        }
    }

    private static int determineImageType(String placeholder) {
        if (placeholder.toLowerCase().contains(".jpg") || placeholder.toLowerCase().contains(".jpeg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        }
        return XWPFDocument.PICTURE_TYPE_PNG;
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[{}]", "").replaceAll("IMG_", "img_");
    }
}
