package cn.aezo.utils.io.excel;

import cn.aezo.utils.base.ReflectionU;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class ExcelExport {
    private static HSSFWorkbook wb;

    private static CellStyle titleStyle;        // 标题行样式
    private static Font titleFont;              // 标题行字体
    private static CellStyle dateStyle;         // 日期行样式
    private static Font dateFont;               // 日期行字体
    private static CellStyle headStyle;         // 表头行样式
    private static Font headFont;               // 表头行字体
    private static CellStyle contentStyle;     // 内容行样式
    private static Font contentFont;            // 内容行字体

    /**
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @Description: 将Map里的集合对象数据输出Excel数据流
     */
    @Deprecated
    @SuppressWarnings({"unchecked"})
    public static void export2Excel(ExportSetInfo setInfo) throws
            IOException, IllegalArgumentException, IllegalAccessException {
        init();
        Set<Entry<String, List>> set = setInfo.getObjsMap().entrySet();
        String[] sheetNames = new String[setInfo.getObjsMap().size()];
        int sheetNameNum = 0;
        for (Entry<String, List> entry : set) {
            sheetNames[sheetNameNum] = entry.getKey();
            sheetNameNum++;
        }
        HSSFSheet[] sheets = getSheets(setInfo.getObjsMap().size(), sheetNames);
        int sheetNum = 0;
        for (Entry<String, List> entry : set) {
            // Sheet
            List objs = entry.getValue();
            // 标题行
            createTableTitleRow(setInfo, sheets, sheetNum);
            // 日期行
            createTableDateRow(setInfo, sheets, sheetNum);
            // 表头
            creatTableHeadRow(setInfo, sheets, sheetNum);
            // 表体
            String[] fieldNames = setInfo.getFieldNames().get(sheetNum);
            int rowNum = 3;
            for (Object obj : objs) {
                HSSFRow contentRow = sheets[sheetNum].createRow(rowNum);
                contentRow.setHeight((short) 300);
                HSSFCell[] cells = getCells(contentRow, setInfo.getFieldNames().get(sheetNum).length);
                int cellNum = 1; // 去掉一列序号，因此从1开始
                if (fieldNames != null) {
                    for (int num = 0; num < fieldNames.length; num++) {
                        Object value = ReflectionU.invokeGetterMethod(obj, fieldNames[num]);
                        cells[cellNum].setCellValue(value == null ? "" : value.toString());
                        cellNum++;
                    }
                }
                rowNum++;
            }
//          adjustColumnSize(sheets, sheetNum, fieldNames); // 自动调整列宽
            sheetNum++;
        }
        wb.write(setInfo.getOut());
    }

    /**
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @Description: 将Map里的List对象数据输出Excel数据流
     */
    @SuppressWarnings({"unchecked"})
    public static void export2ExcelByList(ExportSetInfo setInfo) throws
            IOException, IllegalArgumentException, IllegalAccessException {
        init();
        Set<Entry<String, List>> set = setInfo.getObjsMap().entrySet();
        String[] sheetNames = new String[setInfo.getObjsMap().size()];
        int sheetNameNum = 0;
        for (Entry<String, List> entry : set) {
            sheetNames[sheetNameNum] = entry.getKey();
            sheetNameNum++;
        }
        HSSFSheet[] sheets = getSheets(setInfo.getObjsMap().size(), sheetNames);
        int sheetNum = 0;
        for (Entry<String, List> entry : set) {
            // Sheet
            List objs = entry.getValue();
            // 标题行
            createTableTitleRow(setInfo, sheets, sheetNum);
            // 日期行
            createTableDateRow(setInfo, sheets, sheetNum);
            // 表头
            creatTableHeadRow(setInfo, sheets, sheetNum);
            // 表体
            String[] fieldNames = setInfo.getFieldNames().get(sheetNum);
            int rowNum = 3;
            for (Object obj : objs) {
                HSSFRow contentRow = sheets[sheetNum].createRow(rowNum);
                contentRow.setHeight((short) 300);
                HSSFCell[] cells = getCells(contentRow, setInfo.getFieldNames().get(sheetNum).length);
                int cellNum = 1;                    // 去掉一列序号，因此从1开始
                if (fieldNames != null) {
                    for (int num = 0; num < fieldNames.length; num++) {
                        Object value = ((HashMap) obj).get(fieldNames[num]);
                        //cells[cellNum].setEncoding(HSSFCell.ENCODING_UTF_16);
                        cells[cellNum].setCellValue(value == null ? "" : value.toString());
                        cellNum++;
                    }
                }
                rowNum++;
            }
            adjustColumnSize(sheets, sheetNum, fieldNames); // 自动调整列宽
            sheetNum++;
        }
        wb.write(setInfo.getOut());
    }

    public static void export2ExcelCustomBylist(ExportSetInfo setInfo) throws IOException, IllegalArgumentException, IllegalAccessException {
        init();
        Set<Entry<String, List>> set = setInfo.getObjsMap().entrySet();
        String[] sheetNames = new String[setInfo.getObjsMap().size()];
        int sheetNameNum = 0;
        for (Entry<String, List> entry : set) {
            sheetNames[sheetNameNum] = entry.getKey();
            sheetNameNum++;
        }
        HSSFSheet[] sheets = getSheets(setInfo.getObjsMap().size(), sheetNames);
        int sheetNum = 0;
        for (Entry<String, List> entry : set) {
            // Sheet
            List objs = entry.getValue();
            // 标题行
            createTableTitleRow(setInfo, sheets, sheetNum);
            // 日期行
            createTableDateRow(setInfo, sheets, sheetNum);
            // 表头
            creatTableHeadRow(setInfo, sheets, sheetNum);
            // 表体
            String[] fieldNames = setInfo.getFieldNames().get(sheetNum);
            int rowNum = 3;
            int customNum = 0;
            for (Object obj : objs) {
                HSSFRow contentRow = sheets[sheetNum].createRow(rowNum);
                contentRow.setHeight((short) 300);
                HSSFCell[] cells = getCellsCustom(contentRow, setInfo.getFieldNames().get(sheetNum).length, customNum);
                int cellNum = 1;                    // 去掉一列序号，因此从1开始
                if (fieldNames != null) {
                    for (int num = 0; num < fieldNames.length; num++) {
                        Object value = ((HashMap) obj).get(fieldNames[num]);
                        //cells[cellNum].setEncoding(HSSFCell.ENCODING_UTF_16);
                        cells[cellNum].setCellValue(value == null ? "" : value.toString());
                        cellNum++;
                    }
                }
                rowNum++;
            }
            adjustColumnSize(sheets, sheetNum, fieldNames); // 自动调整列宽
            setSizeColumn(sheets[sheetNum], fieldNames.length);
            sheetNum++;
        }
        wb.write(setInfo.getOut());
    }


    /**
     * @Description: 初始化
     */
    private static void init() {
        wb = new HSSFWorkbook();

        titleFont = wb.createFont();
        titleStyle = wb.createCellStyle();
        dateStyle = wb.createCellStyle();
        dateFont = wb.createFont();
        headStyle = wb.createCellStyle();
        headFont = wb.createFont();
        contentStyle = wb.createCellStyle();
        contentFont = wb.createFont();

        initTitleCellStyle();
        initTitleFont();
        initDateCellStyle();
        initDateFont();
        initHeadCellStyle();
        initHeadFont();
        initContentCellStyle();
        initContentFont();
    }

    // 自适应宽度(中文支持)
    private static void setSizeColumn(HSSFSheet sheet, int size) {
        for (int columnNum = 0; columnNum < size; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                HSSFRow currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }

                if (currentRow.getCell(columnNum) != null) {
                    HSSFCell currentCell = currentRow.getCell(columnNum);
                    if (currentCell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            sheet.setColumnWidth(columnNum, columnWidth * 256);
        }
    }

    /**
     * @Description: 自动调整列宽
     */
    @SuppressWarnings("unused")
    private static void adjustColumnSize(HSSFSheet[] sheets, int sheetNum,
                                         String[] fieldNames) {
        for (int i = 0; i < fieldNames.length + 1; i++) {
            sheets[sheetNum].autoSizeColumn(i, true);
        }
    }

    /**
     * @Description: 创建标题行(需合并单元格)
     */
    private static void createTableTitleRow(ExportSetInfo setInfo,
                                            HSSFSheet[] sheets, int sheetNum) {
        CellRangeAddress titleRange = new CellRangeAddress(0, 0, 0,
                setInfo.getFieldNames().get(sheetNum).length);
        sheets[sheetNum].addMergedRegion(titleRange);
        HSSFRow titleRow = sheets[sheetNum].createRow(0);
        titleRow.setHeight((short) 800);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellStyle(titleStyle);
        titleCell.setCellValue(setInfo.getTitles()[sheetNum]);
    }

    /**
     * @Description: 创建日期行(需合并单元格)
     */
    private static void createTableDateRow(ExportSetInfo setInfo,
                                           HSSFSheet[] sheets, int sheetNum) {
        CellRangeAddress dateRange = new CellRangeAddress(1, 1, 0,
                setInfo.getFieldNames().get(sheetNum).length);
        sheets[sheetNum].addMergedRegion(dateRange);
        HSSFRow dateRow = sheets[sheetNum].createRow(1);
        dateRow.setHeight((short) 350);
        HSSFCell dateCell = dateRow.createCell(0);
        dateCell.setCellStyle(dateStyle);
        dateCell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    /**
     * @Description: 创建表头行(需合并单元格)
     */
    private static void creatTableHeadRow(ExportSetInfo setInfo,
                                          HSSFSheet[] sheets, int sheetNum) {
        // 表头
        HSSFRow headRow = sheets[sheetNum].createRow(2);
        headRow.setHeight((short) 350);
        // 序号列
        HSSFCell snCell = headRow.createCell(0);
        snCell.setCellStyle(headStyle);
        snCell.setCellValue("序号");
        // 列头名称
        for (int num = 1, len = setInfo.getHeadNames().get(sheetNum).length; num <= len; num++) {
            HSSFCell headCell = headRow.createCell(num);
            headCell.setCellStyle(headStyle);
            headCell.setCellValue(setInfo.getHeadNames().get(sheetNum)[num - 1]);
        }
    }

    /**
     * @Description: 创建所有的Sheet
     */
    private static HSSFSheet[] getSheets(int num, String[] names) {
        HSSFSheet[] sheets = new HSSFSheet[num];
        for (int i = 0; i < num; i++) {
            sheets[i] = wb.createSheet(names[i]);

        }
        return sheets;
    }

    /**
     * @Description: 创建内容行的每一列(附加一列序号)
     */
    private static HSSFCell[] getCells(HSSFRow contentRow, int num) {
        HSSFCell[] cells = new HSSFCell[num + 1];

        for (int i = 0, len = cells.length; i < len; i++) {
            cells[i] = contentRow.createCell(i);

            cells[i].setCellStyle(contentStyle);
        }
        // 设置序号列值，因为出去标题行和日期行，所有-2
        cells[0].setCellValue(contentRow.getRowNum() - 2);

        return cells;
    }

    /**
     * 定制行时创建内容行
     *
     * @param contentRow
     * @param num
     * @param customNum
     * @return
     * @author smalle
     * @date 2016年12月30日 下午12:20:51
     */
    private static HSSFCell[] getCellsCustom(HSSFRow contentRow, int num, int customNum) {
        HSSFCell[] cells = new HSSFCell[num + 1];

        for (int i = 0, len = cells.length; i < len; i++) {
            cells[i] = contentRow.createCell(i);

            cells[i].setCellStyle(contentStyle);
        }
        // 设置序号列值，因为出去标题行和日期行，所有-2
        cells[0].setCellValue(contentRow.getRowNum() - 2 - customNum);

        return cells;
    }

    /**
     * @Description: 初始化标题行样式
     */
    private static void initTitleCellStyle() {
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
    }

    /**
     * @Description: 初始化日期行样式
     */
    private static void initDateCellStyle() {
        dateStyle.setAlignment(HorizontalAlignment.CENTER);
        dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dateStyle.setFont(dateFont);
    }

    /**
     * @Description: 初始化表头行样式
     */
    private static void initHeadCellStyle() {
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setFont(headFont);
    }

    /**
     * @Description: 初始化内容行样式
     */
    private static void initContentCellStyle() {
        contentStyle.setAlignment(HorizontalAlignment.CENTER);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle.setFont(contentFont);
        contentStyle.setWrapText(true); // 字段换行
    }

    /**
     * @Description: 初始化标题行字体
     */
    private static void initTitleFont() {
        titleFont.setFontName("微软雅黑");
        titleFont.setFontHeightInPoints((short) 20);
        titleFont.setBold(true);
        titleFont.setCharSet(Font.DEFAULT_CHARSET);
        titleFont.setColor(IndexedColors.BLACK.index);
    }

    /**
     * @Description: 初始化日期行字体
     */
    private static void initDateFont() {
        dateFont.setFontName("微软雅黑");
        dateFont.setFontHeightInPoints((short) 10);
        dateFont.setBold(true);
        dateFont.setCharSet(Font.DEFAULT_CHARSET);
        dateFont.setColor(IndexedColors.BLACK.index);
    }

    /**
     * @Description: 初始化表头行字体
     */
    private static void initHeadFont() {
        headFont.setFontName("微软雅黑");
        headFont.setFontHeightInPoints((short) 10);
        headFont.setBold(true);
        headFont.setCharSet(Font.DEFAULT_CHARSET);
        headFont.setColor(IndexedColors.BLACK.index);
    }

    /**
     * @Description: 初始化内容行字体
     */
    private static void initContentFont() {
        contentFont.setFontName("微软雅黑");
        contentFont.setFontHeightInPoints((short) 10);
        contentFont.setBold(false);
        contentFont.setCharSet(Font.DEFAULT_CHARSET);
        contentFont.setColor(IndexedColors.BLACK.index);
    }

    /**
     * @Description: 封装Excel导出的设置信息
     */
    public static class ExportSetInfo {
        @SuppressWarnings("unchecked")
        private LinkedHashMap<String, List> objsMap;

        private String[] titles;

        private List<String[]> headNames;

        private List<String[]> fieldNames;

        private OutputStream out;

        @SuppressWarnings("unchecked")
        public LinkedHashMap<String, List> getObjsMap() {
            return objsMap;
        }

        @SuppressWarnings("unchecked")
        public void setObjsMap(LinkedHashMap<String, List> objsMap) {
            this.objsMap = objsMap;
        }

        public List<String[]> getFieldNames() {
            return fieldNames;
        }

        public void setFieldNames(List<String[]> fieldNames) {
            this.fieldNames = fieldNames;
        }

        public String[] getTitles() {
            return titles;
        }

        /**
         * @param titles 对应每个sheet里的标题，即顶部大字
         */
        public void setTitles(String[] titles) {
            this.titles = titles;
        }

        public List<String[]> getHeadNames() {
            return headNames;
        }

        /**
         * @param headNames 对应每个页签的表头的每一列的名称
         */
        public void setHeadNames(List<String[]> headNames) {
            this.headNames = headNames;
        }

        public OutputStream getOut() {
            return out;
        }

        /**
         * @param out Excel数据将输出到该输出流
         */
        public void setOut(OutputStream out) {
            this.out = out;
        }
    }
}