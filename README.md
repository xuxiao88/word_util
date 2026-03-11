# Word Util

A simple Java utility library for word processing.

## Features

- Count words in a text
- Capitalize the first letter of each word

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.0 or higher

### Build

```bash
mvn clean package
```



## Usage

```java
import com.congyu.word.wordutil.WordUtil;

// Count words
int wordCount = WordUtil.countWords("Hello world test");
System.out.println("Word count: " + wordCount); // Output: Word count: 3

// Capitalize words
String capitalized = WordUtil.capitalizeWords("hello world test");
System.out.println("Capitalized: " + capitalized); // Output: Capitalized: Hello World Test
```

## Project Structure

```
word-util/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── congyu/
│   │   │           └── word/
│   │   │               ├── wordutil/
│   │   │               │   └── WordUtil.java
│   │   │               └── generate/
│   │   └── resources/
│   └── example/
│       └── java/
│           └── com/
│               └── congyu/
│                   └── word/
```
