package cn.aezo.utils.io.excel;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author smalle
 * @date 2017/9/15 19:42
 *
 * <b>
 *     基于SAX解析Excel(只适用于>2007版的excel文件)
 * </b>
 * <br/><br/>
 *
 * <small>
 * XML解析器<br/>
 * jdk本身提供了两种XMl解析器：<br/>
 * 1.像文档对象模型(Document Object Model,DOM)解析器这样的树型解析器(tree parser),它们将读入的XML文档转换成树结构；<br/>
 * 2.像用于XML的简单API(Simple API for XML,SAX)解析器这样的流机制解析器(streaming parser),它们在读入XML文档时生成相应的事件；<br/><br/>
 *
 * 当文档很大，并且处理算法非常简单，可以在运行时解析节点，而不必要看到所有的树型结构时，DOM可能就会显得效率低下了，在这种情况下，应该使用流机制解析器(streaming parser)；<br/>
 * SAX解析器使用的事件回调(event callback)，SAX解析器在解析XML输入的构件时就报告事件，但不会以任何方式存储文档。<br/>
 * 由事件处理器决定是否要建立数据结构实际上，DOM解析器是在SAX解析器的基础上建立起来的，它在接受到解析器事件时就建立DOM树；<br/><br/>
 *
 * 在使用SAX解析器时，需要一个处理器来定义不同的解析器事件的事件动作。ContentHandler接口定义了若干个回调方法：<br/>
 * 1.startElement和endElement在每当遇到起始或终止标签时调用；<br/>
 * 2.characters每当遇到字符数据时调用；<br/>
 * 3.startDocument和endDocument分别在文档开始和结束时各调用一次。<br/><br/>
 *
 * 可以发现poi读取Excel的事件驱动模式api正是使用的SAX解析器，为什么SAX可以读取Excel，主要还是因为Excel2007以后，其内容采用XML的格式来存储，<br/>
 * 所以处理excel就是解析XML；可以改变Excel的后缀为.zip，就可以查看里面的xml文件了。<br/>
 * </small>
 */
public abstract class Excel2007SAXReader extends DefaultHandler {
    /**
     * <p>Field stylesTable: 单元格样式</p>
     */
    private StylesTable stylesTable;

    /**
     * <p>键值对{单元格列名,单元格值}</p>
     */
    private HashMap<String, String> valueMap = new HashMap<String, String>();;
    /**
     * <p>共享字符串表对象</p>
     */
    private SharedStringsTable sharedStringsTable;
    /**
     * <p>单元格内容</p>
     */
    private String lastContents;
    /**
     * <p>是否是字符串</p>
     */
    private boolean nextIsString;
    /**
     * <p>是否是日期</p>
     */
    private boolean nextIsDate;
    /**
     * <p>单元格坐标</p>
     */
    private String key;
    /**
     * <p>单元格值</p>
     */
    private String value;
    /**
     * <p>样式index</p>
     */
    private int index;

    /**
     * <p>Description: 读取到一行数据进行处理</p>
     * @param valueMap 当前行数据(Key为Excel列表名，如A、B、C)
     * @param nowRow 读取数据所在行(用于检查Excel哪一行数据插入报错)
     */
    public abstract void dealCurrentRow(Map<String, String> valueMap, String nowRow);

    /**
     * <p>Description: 处理Excel的第一个sheet</p>
     * @param filename 文件名带路径
     * @throws Exception 异常
     */
    public void readOneSheet(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        this.sharedStringsTable = r.getSharedStringsTable();
        this.stylesTable = r.getStylesTable();

        XMLReader parser = fetchSheetParser();

        // To look up the Sheet Name / Sheet Order / rID,
        // you need to process the core Workbook stream.
        // Normally it's of the form rId# or rSheet#
        InputStream sheet = r.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheet);
        parser.parse(sheetSource);
        sheet.close();
    }

    /**
     * <p>Description: 处理Excel的所有sheet</p>
     * @param filename 文件名带路径
     * @throws Exception 异常
     */
    public void readAllSheets(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        this.sharedStringsTable = r.getSharedStringsTable();
        this.stylesTable = r.getStylesTable();

        XMLReader parser = fetchSheetParser();

        Iterator<InputStream> sheets = r.getSheetsData();
        while (sheets.hasNext()) {
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
        }
    }

    /**
     * <p>Description: 获取XML访问对象</p>
     * @return XMLReader XML访问对象
     * @throws SAXException SAX异常
     */
    private XMLReader fetchSheetParser() throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
        // ContentHandler handler = new SheetHandler(sharedStringsTable);
        parser.setContentHandler(this);
        return parser;
    }

    /**
     * <p>Title: startElement</p>
     * <p>Description: </p>
     * @param uri uri
     * @param localName localName
     * @param name XML标签名
     * @param attributes XML标签对象
     * @throws SAXException SAX异常
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        // c => cell
        if ("c".equals(name)) {
            // Print the cell reference
            this.key = attributes.getValue("r");
            this.value = "";
            // Figure out if the value is an index in the SST
            String cellType = attributes.getValue("t");
            if ("s".equals(cellType)) {
                this.nextIsString = true;
            } else {
                // 单元格是日期格式时c标签中s属性的值是数字
                cellType = attributes.getValue("s");
                this.nextIsString = false;
            }
            // 判断是否是日期格式
            if (cellType != null && Pattern.compile("^[-\\+]?[\\d]*$").matcher(cellType).matches()) {
                this.index = Integer.parseInt(cellType);
                this.nextIsDate = true;
            } else {
                this.nextIsDate = false;
            }
        }
        // Clear contents cache
        this.lastContents = "";
    }

    /**
     * <p>Title: endElement</p>
     * <p>Description: </p>
     * @param uri uri
     * @param localName localName
     * @param name XML标签名
     * @throws SAXException SAX异常
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String name) throws SAXException {
        // Process the last contents as required.
        // Do now, as characters() may be called more than once
        if (this.nextIsString) {
            int idx = Integer.parseInt(this.lastContents);
            this.lastContents = new XSSFRichTextString(this.sharedStringsTable.getEntryAt(idx)).toString();
            this.nextIsString = false;
        }
        if (this.nextIsDate && !"".equals(this.lastContents)) {
            XSSFCellStyle style = stylesTable.getStyleAt(this.index);
            short formatIndex = style.getDataFormat();
            String formatString = style.getDataFormatString();
            if (formatString.contains("m/d/yy")) {
                formatString = "yyyy-MM-dd hh:mm:ss";
            }
            DataFormatter formatter = new DataFormatter();
            this.lastContents = formatter.formatRawCellContents(Double.parseDouble(this.lastContents), formatIndex,
                    formatString);
            this.nextIsDate = false;
        }
        // v => contents of a cell
        // Output after we've seen the string contents
        if ("v".equals(name)) {
            this.value = this.lastContents;
        } else if ("c".equals(name)) {
            this.valueMap.put(this.key.replaceAll("\\d+", ""), this.value);
        } else if ("row".equals(name)) {
            String nowRow = this.key.replaceAll(this.key.replaceAll("\\d+", ""), "");
            if (!"1".equals(nowRow)) { // 第一行为标题
                // 读取到一行
                this.dealCurrentRow(this.valueMap, nowRow);
            }
            // 清空存储集
            this.valueMap.clear();
        }

    }

    /**
     * <p>Title: characters</p>
     * <p>Description: </p>
     * @param ch 字符数组
     * @param start 起始位
     * @param length 长度
     * @throws SAXException SAX异常
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.lastContents += new String(ch, start, length);
    }


    public static void main(String[] args) throws Exception {
        Excel2007SAXReader example = new Excel2007SAXReader() {
            @Override
            public void dealCurrentRow(Map<String, String> valueMap, String nowRow) {
                System.out.println("valueMap = " + valueMap);
            }
        };
        example.readAllSheets("D:\\test.xlsx");
    }
}
