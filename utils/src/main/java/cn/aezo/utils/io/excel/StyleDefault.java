package cn.aezo.utils.io.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * Created by smalle on 2017/9/15.
 * excel导入数据
 */
public class StyleDefault {

    private Font titleFont;              // 标题行字体
    private Font dateFont;               // 标题日期行字体
    private Font headFont;               // 表头行字体
    private Font contentFont;            // 内容行字体
    private CellStyle titleCellStyle;    // 标题行样式
    private CellStyle dateCellStyle;     // 标题日期行样式
    private CellStyle headCellStyle;     // 表头行样式
    private CellStyle contentCellStyle ; // 内容行样式

    public StyleDefault(Font titleFont, Font dateFont, Font headFont, Font contentFont, CellStyle titleCellStyle, CellStyle dateCellStyle, CellStyle headCellStyle, CellStyle contentCellStyle) {
        this.titleFont = titleFont;
        this.dateFont = dateFont;
        this.headFont = headFont;
        this.contentFont = contentFont;
        this.titleCellStyle = titleCellStyle;
        this.dateCellStyle = dateCellStyle;
        this.headCellStyle = headCellStyle;
        this.contentCellStyle = contentCellStyle;
    }

    /**
     * @Description: 初始化标题行字体
     */
    public StyleDefault initTitleFont() {
        Font titleFont = this.getTitleFont();
        if(titleFont != null) {
            titleFont.setFontName("华文楷体");
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            titleFont.setCharSet(Font.DEFAULT_CHARSET);
            titleFont.setColor(IndexedColors.BLACK.index);
        }
        return this;
    }

    /**
     * @Description: 初始化日期行字体
     */
    public StyleDefault initDateFont() {
        Font dateFont = this.getTitleFont();
        if(dateFont != null) {
            dateFont.setFontName("隶书");
            dateFont.setFontHeightInPoints((short) 10);
            dateFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            dateFont.setCharSet(Font.DEFAULT_CHARSET);
            dateFont.setColor(IndexedColors.BLACK.index);
        }
        return this;
    }

    /**
     * @Description: 初始化表头行字体
     */
    public StyleDefault initHeadFont() {
        Font headFont = this.getTitleFont();
        if(headFont != null) {
            headFont.setFontName("宋体");
            headFont.setFontHeightInPoints((short) 10);
            headFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headFont.setCharSet(Font.DEFAULT_CHARSET);
            headFont.setColor(IndexedColors.BLACK.index);
        }
        return this;
    }

    /**
     * @Description: 初始化内容行字体
     */
    public StyleDefault initContentFont() {
        Font contentFont = this.getTitleFont();
        if(contentFont != null) {
            contentFont.setFontName("宋体");
            contentFont.setFontHeightInPoints((short) 10);
            contentFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            contentFont.setCharSet(Font.DEFAULT_CHARSET);
            contentFont.setColor(IndexedColors.BLACK.index);
        }
        return this;
    }

    /**
     * @Description: 初始化标题行样式
     */
    public StyleDefault initTitleCellStyle() {
        CellStyle titleCellStyle = this.getTitleCellStyle();
        Font titleFont = this.getTitleFont();
        if(titleCellStyle != null) {
            titleCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            titleCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            if(titleFont != null) {
                titleCellStyle.setFont(titleFont);
            }
            // titleCellStyle.setFillBackgroundColor(IndexedColors.SKY_BLUE.index);
        }
        return this;
    }

    /**
     * @Description: 初始化日期行样式
     */
    public StyleDefault initDateCellStyle() {
        CellStyle dateCellStyle = this.getTitleCellStyle();
        Font dateFont = this.getTitleFont();
        if(dateCellStyle != null) {
            dateCellStyle.setAlignment(CellStyle.ALIGN_CENTER_SELECTION);
            dateCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            if(dateFont != null) {
                dateCellStyle.setFont(dateFont);
            }
        }
        return this;
    }

    /**
     * @Description: 初始化表头行样式
     */
    public StyleDefault initHeadCellStyle() {
        CellStyle headCellStyle = this.getTitleCellStyle();
        Font headFont = this.getTitleFont();
        if(headCellStyle != null) {
            headCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            headCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            if(headFont != null) {
                headCellStyle.setFont(headFont);
            }
        }
        return this;
    }

    /**
     * @Description: 初始化内容行样式
     */
    public StyleDefault initContentCellStyle() {
        CellStyle contentCellStyle = this.getTitleCellStyle();
        Font contentFont = this.getTitleFont();
        if(contentCellStyle != null) {
            contentCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            contentCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            contentCellStyle.setWrapText(true); // 字段换行
            if(contentFont != null) {
                contentCellStyle.setFont(contentFont);
            }
        }
        return this;
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    public Font getDateFont() {
        return dateFont;
    }

    public void setDateFont(Font dateFont) {
        this.dateFont = dateFont;
    }

    public Font getHeadFont() {
        return headFont;
    }

    public void setHeadFont(Font headFont) {
        this.headFont = headFont;
    }

    public Font getContentFont() {
        return contentFont;
    }

    public void setContentFont(Font contentFont) {
        this.contentFont = contentFont;
    }

    public CellStyle getTitleCellStyle() {
        return titleCellStyle;
    }

    public void setTitleCellStyle(CellStyle titleCellStyle) {
        this.titleCellStyle = titleCellStyle;
    }

    public CellStyle getDateCellStyle() {
        return dateCellStyle;
    }

    public void setDateCellStyle(CellStyle dateCellStyle) {
        this.dateCellStyle = dateCellStyle;
    }

    public CellStyle getHeadCellStyle() {
        return headCellStyle;
    }

    public void setHeadCellStyle(CellStyle headCellStyle) {
        this.headCellStyle = headCellStyle;
    }

    public CellStyle getContentCellStyle() {
        return contentCellStyle;
    }

    public void setContentCellStyle(CellStyle contentCellStyle) {
        this.contentCellStyle = contentCellStyle;
    }
}
