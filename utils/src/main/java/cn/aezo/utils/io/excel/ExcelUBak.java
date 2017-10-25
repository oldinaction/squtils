package cn.aezo.utils.io.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * Created by smalle on 2017/9/15.
 */
public class ExcelUBak implements IExcel {
    private Workbook workbook;
    private StyleDefault excelData;


    public ExcelUBak(Workbook workbook) {
        super();
        this.workbook = workbook;
    }

    public ExcelUBak getInstance() {
        HSSFWorkbook hssfWorkbook = init();
        return new ExcelUBak(hssfWorkbook);
    }

    private HSSFWorkbook init() {
        HSSFWorkbook wb = new HSSFWorkbook();
        StyleDefault styleDefault = new StyleDefault(wb.createFont(), wb.createFont(), wb.createFont(), wb.createFont(),
                wb.createCellStyle(), wb.createCellStyle(), wb.createCellStyle(), wb.createCellStyle());

        styleDefault.initTitleFont().initDateFont().initHeadFont().initContentFont()
                    .initTitleCellStyle().initDateCellStyle().initHeadCellStyle().initContentCellStyle();

        return wb;
    }

    @Override
    public List<List<Object>> importExcel() {
        return null;
    }

    @Override
    public void exportExcel(Workbook workbook) {

    }

}
