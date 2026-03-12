# Word Util

[![Java](https://img.shields.io/badge/Java-8%2B-blue)](https://www.java.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6.0%2B-C71A36)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A lightweight Java utility library for generating Word documents using templates. Built on top of [XDocReport](https://github.com/opensagres/xdocreport) and [Apache POI](https://poi.apache.org/).

[中文文档](README.zh-CN.md)

## ✨ Features

- 📝 **Template-based Word Generation** - Create Word documents from DOCX templates using FreeMarker syntax
- 🔄 **Dynamic Content** - Support for variables, loops, and conditional rendering
- 🖼️ **Image Injection** - Dynamically insert images into document placeholders
- 📊 **Table Generation** - Generate dynamic tables with variable row counts
- 💾 **Flexible Output** - Generate documents as File or ByteArrayOutputStream
- 🔧 **Easy Integration** - Simple API with minimal configuration required

## 📋 Requirements

- Java 8 or higher
- Maven 3.6.0 or higher

## 🚀 Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.congyu.word</groupId>
    <artifactId>word-util</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Create a Template

Create a DOCX template file with FreeMarker placeholders:

```
Hello, ${name}!

Items:
[#list list as item]
- ${item}
[/#list]
```

For images, use the placeholder format: `{{IMAGE:imageName}}`

### 3. Implement Data Interface

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

### 4. Generate Document

```java
import com.congyu.word.generate.XDocReportUtil;

// Create data bean
MyDocumentData data = new MyDocumentData();
data.setName("John Doe");
data.setList(Arrays.asList("Item 1", "Item 2", "Item 3"));

// Generate as File
File document = XDocReportUtil.generateWordFile(data);

// Or generate as ByteArrayOutputStream
ByteArrayOutputStream stream = XDocReportUtil.generateWordStream(data);
```

## 📖 Detailed Usage

### Working with Images

```java
import org.apache.poi.xwpf.usermodel.Document;

// Create image data
ExampleXDocReportImage image = new ExampleXDocReportImage();
image.setImageBytes(imageBytes);
image.setWidthPx(200);
image.setHeightPx(200);
image.setImageType(Document.PICTURE_TYPE_PNG);
image.setImageName("logo");  // Will match {{IMAGE:logo}} in template

// Add to data bean
data.setImageFields(Arrays.asList(image));
```

### Working with Tables

Define a table row class and include it in your data:

```java
@Data
public static class TableRow {
    private String column1;
    private String column2;
    private String column3;
}

// In your data bean
List<TableRow> rows = new ArrayList<>();
// ... populate rows
map.put("tableDataList", rows);
```

In your template, use:
```
[#list tableDataList as row]
${row.column1} | ${row.column2} | ${row.column3}
[/#list]
```

### API Reference

#### XDocReportUtil

| Method | Description |
|--------|-------------|
| `generateWordFile(XDocReportBaseData data)` | Generates document and returns as `File` |
| `generateWordStream(XDocReportBaseData data)` | Generates document and returns as `ByteArrayOutputStream` |

#### XDocReportBaseData Interface

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getTemplateFile()` | `File` | Returns the template DOCX file |
| `getTargetPath()` | `String` | Returns the output file path (for File generation) |
| `getDataMap()` | `Map<String, Object>` | Returns the data map for template variables |
| `getImageFields()` | `List<XDocReportBaseImage>` | Returns list of images to inject |

#### XDocReportBaseImage Interface

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getImageBytes()` | `byte[]` | Raw image data |
| `getImageType()` | `int` | Image type (e.g., `Document.PICTURE_TYPE_PNG`) |
| `getImageName()` | `String` | Image identifier for placeholder matching |
| `getWidthPx()` | `int` | Image width in pixels |
| `getHeightPx()` | `int` | Image height in pixels |
| `getImageTag()` | `String` | Returns placeholder format `{{IMAGE:imageName}}` |

## 🏗️ Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/word-util.git
cd word-util

# Build with Maven
mvn clean package

# Run tests
mvn test
```

## 📁 Project Structure

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
│   │   │               │   ├── XDocReportUtil.java      # Main utility class
│   │   │               │   ├── XDocReportBaseData.java  # Data interface
│   │   │               │   └── XDocReportBaseImage.java # Image interface
│   │   │               └── util/
│   │   │                   └── ConfigPathUtil.java
│   │   └── resources/
│   └── test/
│       ├── java/
│       │   ├── ExampleXDocReportUtil.java  # Usage example
│       │   ├── ExampleXDocReportData.java  # Sample implementation
│       │   └── ExampleXDocReportImage.java # Sample image implementation
│       └── resources/
│           ├── ExampleTemplate.docx        # Sample template
│           └── TestImage.png               # Sample image
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [XDocReport](https://github.com/opensagres/xdocreport) - Document reporting tool
- [Apache POI](https://poi.apache.org/) - Java API for Microsoft Documents
- [FreeMarker](https://freemarker.apache.org/) - Template engine

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

⭐ Star this repository if you find it helpful!
