package cn.aezo.utils.io.excel;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * Created by smalle on 2017/9/15.
 */
public interface IExcel {
    List<List<Object>> importExcel();

    void exportExcel(Workbook workbook);
}
