package com.congyu.word.generate;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class XDocReportUtil {

    public static ByteArrayOutputStream generateWordStream(XDocReportBaseData dataBean) throws Exception {
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
        if (CollectionUtils.isNotEmpty(dataBean.getImageFields())){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            report.process(context, baos);
            Map<String,XDocReportBaseImage> imageMap = dataBean.getImageFields().stream()
                    .collect(Collectors.toMap(XDocReportBaseImage::getImageTag, v -> v));
            ByteArrayOutputStream resultBaos = new ByteArrayOutputStream();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                replaceImagePlaceholdersInStream(
                        bais,
                        resultBaos,
                        imageMap
                );
            }

            return resultBaos;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        report.process(context, baos);
        return baos;
    }

    public static File generateWordFile(XDocReportBaseData dataBean) throws Exception {
        ByteArrayOutputStream baos = generateWordStream(dataBean);
        File file = new File(dataBean.getTargetPath());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            baos.writeTo(fos);
        }
        return file;
    }


    private static void replaceImagePlaceholdersInStream(
            InputStream inputStream,
            OutputStream outputStream,
            Map<String, XDocReportBaseImage> imageMap) throws Exception {

        // 1. 读取输入流
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {

            // 2. 执行替换
            replaceImagePlaceholdersInDocument(doc,imageMap);

            // 3. 写入输出流
            doc.write(outputStream);
        }
    }

    private static void replaceImagePlaceholdersInDocument(
            XWPFDocument doc,
            Map<String, XDocReportBaseImage> imageMap) throws Exception {

        // 替换正文段落
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            replaceInParagraph(paragraph, imageMap);
        }

        // 替换表格（含嵌套表格）
        for (XWPFTable table : doc.getTables()) {
            replaceInTable(table, imageMap);
        }
    }

    private static void replaceInTable(XWPFTable table, Map<String, XDocReportBaseImage> imageMap) throws Exception {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    replaceInParagraph(p, imageMap);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    replaceInTable(nestedTable, imageMap);
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph paragraph,Map<String, XDocReportBaseImage> imageMap) throws Exception {
        String text = paragraph.getText();
        String prefix = "{{";
        String suffix = "}}";

        // 1. 预先收集所有匹配项及其位置
        List<MatchRange> matches = new ArrayList<>();
        int cursor = 0;
        while ((cursor = text.indexOf(prefix, cursor)) != -1) {
            int endIdx = text.indexOf(suffix, cursor);
            if (endIdx == -1) break;

            int fullEnd = endIdx + suffix.length();
            String fullMatch = text.substring(cursor, fullEnd);
            if (imageMap.containsKey(fullMatch)) {
                matches.add(new MatchRange(cursor, fullEnd, fullMatch));
            }

            cursor = fullEnd;
        }

        // 2. 从后往前执行替换，这样前面的 index 依然有效
        for (int i = matches.size() - 1; i >= 0; i--) {
            MatchRange match = matches.get(i);
            XDocReportBaseImage imageData = imageMap.get(match.content);

            if (imageData != null) {
                replaceRangeWithImage(paragraph, match.start, match.end, match.content, imageData);
            }
        }
    }

    private static void replaceRangeWithImage(XWPFParagraph paragraph, int start, int end, String fullMatch, XDocReportBaseImage imageData) throws Exception {
        List<XWPFRun> runs = paragraph.getRuns();
        int currentPos = 0;

        // 我们需要找到覆盖 [start, end] 范围的所有 Run
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String runText = run.getText(0);
            if (runText == null) continue;

            int runLen = runText.length();
            int runEnd = currentPos + runLen;

            // 判断当前 Run 是否与匹配范围有交集
            if (runEnd > start && currentPos < end) {
                // 计算在这个 Run 中需要处理的局部起止点
                int localStart = Math.max(0, start - currentPos);
                int localEnd = Math.min(runLen, end - currentPos);

                String textBefore = runText.substring(0, localStart);
                String textAfter = runText.substring(localEnd);

                // 如果是该占位符涉及到的第一个 Run，负责插入图片
                if (currentPos <= start) {
                    run.setText(textBefore, 0);
                    insertImageToRun(run, imageData, fullMatch);
                    // 巧妙处：如果该占位符在同一个 Run 里结束，直接把尾巴接上
                    if (runEnd >= end) {
                        run.setText(run.getText(0) + textAfter, 0);
                    }
                } else {
                    // 如果是占位符跨越的后续 Run
                    // 如果该 Run 超出了占位符范围，保留尾巴；否则清空
                    run.setText(runEnd > end ? textAfter : "", 0);
                }
            }
            currentPos = runEnd;
        }
    }

    private static void insertImageToRun(XWPFRun run, XDocReportBaseImage imageData, String fullMatch) throws Exception {
        int widthEmu = imageData.getWidthPx() * 9525;
        int heightEmu = imageData.getHeightPx() * 9525;
        run.addPicture(
                new ByteArrayInputStream(imageData.getImageBytes()),
                imageData.getImageType(),
                imageData.getImageName(),
                widthEmu,
                heightEmu
        );
    }

    private static class MatchRange {
        int start;
        int end;
        String content;
        MatchRange(int s, int e, String c) { this.start = s; this.end = e; this.content = c; }
    }
}
