package com.congyu.word.generate;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XDocReportUtil {

    public static File generateWordFile(XDocReportBaseData dataBean) throws Exception {
        // 获取模版
        InputStream ins = Files.newInputStream(dataBean.getTemplateFile().toPath());
        //注册xdocreport实例并加载FreeMarker模板引擎
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(ins,
                TemplateEngineKind.Freemarker);

        //创建xdocreport上下文对象
        IContext context = report.createContext();

        Map<String,Object> dataMap = dataBean.getDataMap();
        context.putMap(dataMap);

        // 处理图片
        if (dataBean.getImagePattern() != null && MapUtils.isNotEmpty(dataBean.getImageFields())){
            // 使用内存流处理，不创建临时文件
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            report.process(context, baos);

            File file = new File(dataBean.getTargetPath());
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 FileOutputStream fos = new FileOutputStream(file)) {
                
                replaceImagePlaceholdersInStream(
                        bais,
                        fos,
                        dataBean.getImagePattern(),
                        dataBean.getImageFields()
                );
            }

            return file;
        }

        FileOutputStream out = new FileOutputStream(dataBean.getTargetPath());
        report.process(context, out);
        return new File(dataBean.getTargetPath());
    }


    private static void replaceImagePlaceholdersInStream(
            InputStream inputStream,
            OutputStream outputStream,
            Pattern imagePattern,
            Map<String, XDocReportBaseImage> imageMap) throws Exception {

        // 1. 读取输入流
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {

            // 2. 执行替换
            replaceImagePlaceholdersInDocument(doc, imagePattern,imageMap);

            // 3. 写入输出流
            doc.write(outputStream);
        }
    }

    private static void replaceImagePlaceholdersInDocument(
            XWPFDocument doc,
            Pattern imagePattern,
            Map<String, XDocReportBaseImage> imageMap) throws Exception {

        // 替换正文段落
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            replaceInParagraph(paragraph, imagePattern, imageMap);
        }

        // 替换表格（含嵌套表格）
        for (XWPFTable table : doc.getTables()) {
            replaceInTable(table, imagePattern, imageMap);
        }
    }

    private static void replaceInTable(XWPFTable table, Pattern pattern, Map<String, XDocReportBaseImage> imageMap) throws Exception {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraph(p, pattern, imageMap);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    replaceInTable(nestedTable, pattern, imageMap);
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph paragraph, Pattern pattern, Map<String, XDocReportBaseImage> imageMap) throws Exception {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null) return;

        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null) continue;
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // 找到第一个匹配项（假设一个 run 只有一个占位符）
                String fullMatch = matcher.group(0);
                XDocReportBaseImage imageData = imageMap.get(fullMatch);

                // 清空文本
                run.setText("", 0);
                if (imageData != null) {
                    // 插入图片
                    int widthEmu = imageData.getWidthPx() * 9525;
                    int heightEmu = imageData.getHeightPx() * 9525;

                    run.addPicture(
                            new ByteArrayInputStream(imageData.getImageBytes()),
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
