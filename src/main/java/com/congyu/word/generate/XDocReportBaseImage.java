package com.congyu.word.generate;

public interface XDocReportBaseImage {

    // 获取图片
    byte[] getImageBytes();
    // 图片类型 见：org.apache.poi.xwpf.usermodel.Document
    int getImageType();
    // 图片名称
    String getImageName();
    // 图片宽度
    int getWidthPx();
    // 图片高度
    int getHeightPx();

    // 图片标签,用于标识图片在文档中的位置
    default String getImageTag(){
        return "{{IMAGE:" + this.getImageName() + "}}";
    }
}
