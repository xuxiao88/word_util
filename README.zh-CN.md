# Word Util

[![Java](https://img.shields.io/badge/Java-8%2B-blue)](https://www.java.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6.0%2B-C71A36)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

一个轻量级的 Java 工具库，用于基于模板生成 Word 文档，实现了不确定数量图片的批量插入场景。
基于 [XDocReport](https://github.com/opensagres/xdocreport) 和 [Apache POI](https://poi.apache.org/) 构建。

[English Documentation](README.md)

## ✨ 特性

- 📝 **基于模板的 Word 生成** - 使用 FreeMarker 语法从 DOCX 模板创建 Word 文档
- 🔄 **动态内容** - 支持变量、循环和条件渲染
- 🖼️ **图片注入** - 动态插入图片到文档占位符中
- 📊 **表格生成** - 生成可变行数的动态表格
- 💾 **灵活的输出方式** - 支持生成 File 或 ByteArrayOutputStream
- 🔧 **易于集成** - 简单的 API，配置要求极低

## 📋 环境要求

- Java 8 或更高版本
- Maven 3.6.0 或更高版本

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.congyu.word</groupId>
    <artifactId>word-util</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 创建模板

创建一个带有 FreeMarker 占位符的 DOCX 模板文件：

```
你好，${name}！

项目列表：
[#list list as item]
- ${item}
[/#list]
```

对于图片，使用占位符格式：`{{IMAGE:imageName}}`

### 3. 实现数据接口

```java
import com.congyu.word.generate.XDocReportBaseData;
import com.congyu.word.generate.XDocReportBaseImage;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MyDocumentData implements XDocReportBaseData {

    private String name;
    private List<String> list;
    private List<XDocReportBaseImage> imageFields;

    @Override
    public File getTemplateFile() {
        return new File("path/to/template.docx");
    }

    @Override
    public String getTargetPath() {
        return "path/to/output.docx";
    }

    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("list", list);
        return map;
    }
}
```

### 4. 生成文档

```java
import com.congyu.word.generate.XDocReportUtil;

// 创建数据对象
MyDocumentData data = new MyDocumentData();
data.setName("张三");
data.setList(Arrays.asList("项目 1", "项目 2", "项目 3"));

// 生成 File
File document = XDocReportUtil.generateWordFile(data);

// 或生成 ByteArrayOutputStream
ByteArrayOutputStream stream = XDocReportUtil.generateWordStream(data);
```

## 📖 详细用法

### 图片处理

```java
import org.apache.poi.xwpf.usermodel.Document;

// 创建图片数据
ExampleXDocReportImage image = new ExampleXDocReportImage();
image.setImageBytes(imageBytes);
image.setWidthPx(200);
image.setHeightPx(200);
image.setImageType(Document.PICTURE_TYPE_PNG);
image.setImageName("logo");  // 将匹配模板中的 {{IMAGE:logo}}

// 添加到数据对象
data.setImageFields(Arrays.asList(image));
```

### 表格处理

定义表格行类并将其包含在数据中：

```java
@Data
public static class TableRow {
    private String column1;
    private String column2;
    private String column3;
}

// 在数据对象中
List<TableRow> rows = new ArrayList<>();
// ... 填充数据
map.put("tableDataList", rows);
```

在模板中使用：
```
[#list tableDataList as row]
${row.column1} | ${row.column2} | ${row.column3}
[/#list]
```

### API 参考

#### XDocReportUtil

| 方法 | 描述 |
|------|------|
| `generateWordFile(XDocReportBaseData data)` | 生成文档并返回 `File` |
| `generateWordStream(XDocReportBaseData data)` | 生成文档并返回 `ByteArrayOutputStream` |

#### XDocReportBaseData 接口

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `getTemplateFile()` | `File` | 返回模板 DOCX 文件 |
| `getTargetPath()` | `String` | 返回输出文件路径（用于文件生成） |
| `getDataMap()` | `Map<String, Object>` | 返回模板变量的数据映射 |
| `getImageFields()` | `List<XDocReportBaseImage>` | 返回要注入的图片列表 |

#### XDocReportBaseImage 接口

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `getImageBytes()` | `byte[]` | 原始图片数据 |
| `getImageType()` | `int` | 图片类型（例如 `Document.PICTURE_TYPE_PNG`） |
| `getImageName()` | `String` | 图片标识符，用于占位符匹配 |
| `getWidthPx()` | `int` | 图片宽度（像素） |
| `getHeightPx()` | `int` | 图片高度（像素） |
| `getImageTag()` | `String` | 返回占位符格式 `{{IMAGE:imageName}}` |

## 🏗️ 从源码构建

```bash
# 克隆仓库
git clone https://github.com/yourusername/word-util.git
cd word-util

# 使用 Maven 构建
mvn clean package

# 运行测试
mvn test
```

## 📁 项目结构

```
word-util/
├── pom.xml
├── README.md
├── README.zh-CN.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── congyu/
│   │   │           └── word/
│   │   │               ├── generate/
│   │   │               │   ├── XDocReportUtil.java      # 主工具类
│   │   │               │   ├── XDocReportBaseData.java  # 数据接口
│   │   │               │   └── XDocReportBaseImage.java # 图片接口
│   │   │               └── util/
│   │   │                   └── ConfigPathUtil.java
│   │   └── resources/
│   └── test/
│       ├── java/
│       │   ├── ExampleXDocReportUtil.java  # 使用示例
│       │   ├── ExampleXDocReportData.java  # 示例实现
│       │   └── ExampleXDocReportImage.java # 示例图片实现
│       └── resources/
│           ├── ExampleTemplate.docx        # 示例模板
│           └── TestImage.png               # 示例图片
```

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- [XDocReport](https://github.com/opensagres/xdocreport) - 文档报告工具
- [Apache POI](https://poi.apache.org/) - Microsoft 文档的 Java API
- [FreeMarker](https://freemarker.apache.org/) - 模板引擎

## 📧 联系方式

如有问题或需要支持，请在 GitHub 上提交 issue。

---

⭐ 如果觉得本项目对你有帮助，请给个 Star！
