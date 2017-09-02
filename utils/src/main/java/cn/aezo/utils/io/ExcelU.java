package cn.aezo.utils.io;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by smalle on 2017/4/11.
 */
public class ExcelU {

    // poi sax 事件驱动读取数据
    private static class Sax {
        public List<List<IndexValue>> dataList = new ArrayList<List<IndexValue>>();
        private final int startRow;
        private final int endRow;
        private int currentRow = 0;
        private final String filename;
        private List<IndexValue> rowData;

        public Sax(String filename,int startRow,int endRow) throws Exception {
            if(StringUtils.isBlank(filename)) throw new Exception("文件名不能空");
            this.filename = filename;
            this.startRow = startRow;
            this.endRow = endRow;
            processFirstSheet();
        }

        /**
         * 指定获取第一个sheet
         * @throws Exception
         */
        private void processFirstSheet() throws Exception {
            OPCPackage pkg = OPCPackage.open(filename);
            XSSFReader r = new XSSFReader( pkg );
            SharedStringsTable sst = r.getSharedStringsTable();

            XMLReader parser = fetchSheetParser(sst);

            // To look up the Sheet Name / Sheet Order / rID,
            //  you need to process the core Workbook stream.
            // Normally it's of the form rId# or rSheet#
            InputStream sheet1 = r.getSheet("rId1");
            InputSource sheetSource = new InputSource(sheet1);
            parser.parse(sheetSource);
            sheet1.close();
        }

        private XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler handler = new PagingHandler(sst);
            parser.setContentHandler(handler);
            return parser;
        }

        /**
         * See org.xml.sax.helpers.DefaultHandler javadocs
         */
        private  class PagingHandler extends DefaultHandler {
            private SharedStringsTable sst;
            private String lastContents;
            private boolean nextIsString;
            private String index = null;

            private PagingHandler(SharedStringsTable sst) {
                this.sst = sst;
            }
            /**
             * 每个单元格开始时的处理
             */
            @Override
            public void startElement(String uri, String localName, String name,
                                     Attributes attributes) throws SAXException {
                // c => cell
                if(name.equals("c")) {
                    // Print the cell reference
                    // System.out.print(attributes.getValue("r") + " - ");

                    index = attributes.getValue("r");
                    System.out.println(index);
                    if(index.contains("N")){
                        System.out.println("##"+attributes+"##");
                    }

                    //这是一个新行
                    if(Pattern.compile("^A[0-9]+$").matcher(index).find()){

                        // 存储上一行数据
                        if(rowData!=null&&isAccess()&&!rowData.isEmpty()){
                            dataList.add(rowData);
                        }
                        rowData = new ArrayList<IndexValue>();;//新行要先清除上一行的数据
                        currentRow++;//当前行+1
                        // System.out.println(currentRow);
                    }
                    if(isAccess()){
                        // Figure out if the value is an index in the SST
                        String cellType = attributes.getValue("t");
                        if(cellType != null && cellType.equals("s")) {
                            nextIsString = true;
                        } else {
                            nextIsString = false;
                        }
                    }

                }
                // Clear contents cache
                lastContents = "";
            }

            /**
             * 每个单元格结束时的处理
             */
            @Override
            public void endElement(String uri, String localName, String name)
                    throws SAXException {
                if(isAccess()){
                    // Process the last contents as required.
                    // Do now, as characters() may be called more than once
                    if(nextIsString) {
                        int idx = Integer.parseInt(lastContents);
                        lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                        nextIsString = false;
                    }

                    // v => contents of a cell
                    // Output after we've seen the string contents
                    if(name.equals("v")) {
                        // System.out.println(lastContents);
                        rowData.add(new IndexValue(index,lastContents));
                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {
                if(isAccess()){
                    lastContents += new String(ch, start, length);
                }

            }

            /**
             * 如果文档结束后，发现读取的末尾行正处在当前行中，存储下这行
             * （存在这样一种情况，当待读取的末尾行正好是文档最后一行时，最后一行无法存到集合中，
             * 因为最后一行没有下一行了，所以不为启动starElement()方法，
             * 当然我们可以通过指定最大列来处理，但不想那么做，扩展性不好）
             */
            @Override
            public void endDocument ()throws SAXException{
                if(rowData!=null&&isAccess()&&!rowData.isEmpty()){
                    dataList.add(rowData);
                    System.out.println("--end");
                }

            }

        }

        private boolean isAccess() {
            if(currentRow >= startRow && startRow <= endRow) {
                return true;
            }
            return false;
        }

        private class IndexValue{
            String v_index;
            String v_value;

            public IndexValue(String v_index, String v_value) {
                super();
                this.v_index = v_index;
                this.v_value = v_value;
            }

            @Override
            public String toString() {
                return "IndexValue [v_index=" + v_index + ", v_value="
                        + v_value + "]";
            }

            public int getLevel(IndexValue p){

                char[] other = p.v_index.replaceAll("[0-9]", "").toCharArray();
                char[] self = this.v_index.replaceAll("[0-9]", "").toCharArray();
                if(other.length!=self.length) return -1;
                for(int i=0;i<other.length;i++){
                    if(i==other.length-1){
                        return self[i]-other[i];
                    }else{
                        if(self[i]!=other[i]){
                            return -1;
                        }
                    }

                }
                return -1;
            }
        }

        /**
         * 获取真实的数据（处理空格）
         * @return
         * @throws Exception
         */
        public List<List<String>> getMyDataList() throws Exception {

            List<List<String>> myDataList = new ArrayList<List<String>>();
            if(dataList==null||dataList.size()<=0) return myDataList;

            for(int i=0;i<dataList.size();i++){
                List<IndexValue> i_list = dataList.get(i);
                List<String> tem = new ArrayList<String>();
                int j=0;
                for(;j< i_list.size()-1;j++){
                    //获取当前值,并存储
                    IndexValue current = i_list.get(j);
                    tem.add(current.v_value);
                    //预存下一个
                    IndexValue next = i_list.get(j+1);
                    //获取差值
                    int level = next.getLevel(current);
                    if(level<=0) throw new Exception("超出处理范围");
                    for(int k = 0;k<level-1;k++){
                        tem.add(null);
                    }
                }
                tem.add(i_list.get(j).v_value);
                myDataList.add(tem);

            }
            return myDataList;
        }
    }

    public static void main(String[] args) throws Exception {

            /*System.out.println('O'-'M');
            System.out.println("O12".hashCode()-"M12".hashCode());

            String str = "ggg";
            char[] bm;
            bm = str.toCharArray();
            str = String.valueOf(bm);

            String p1 = "OOM123".replaceAll("[0-9]", "");
            String p2 = "OOO123".replaceAll("[0-9]", "");
            System.out.println(p1.hashCode()-p2.hashCode());*/



        /*  List<String> list = new ArrayList<String>();
            list.add("a");
            list.add(null);
            list.add("b");
            list.add("");
            list.add("c");
            list.add(" ");
            System.out.println(list);
            System.out.println(list.get(1));
            System.out.println(null=="null");
            System.out.println("null".equals(null));*/

        ExcelU.Sax reader = new ExcelU.Sax("C:/Users/smalle/Desktop/out_plan_boxmanager.xls",1,100);
        System.out.println("\n---"+reader.getMyDataList());

    }
}
