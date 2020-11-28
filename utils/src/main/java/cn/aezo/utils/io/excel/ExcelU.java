package cn.aezo.utils.io.excel;

import cn.aezo.utils.base.MiscU;
import cn.aezo.utils.base.StringU;
import cn.aezo.utils.base.ValidU;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Excel导入导出
 * @ClassName: Excel 
 * @author smalle
 * @date 2016年9月6日 上午11:03:34
 */
@Slf4j
public class ExcelU {
	private static String tempFolder = "temp/"; // 导入导出临时路径
	private static String sourceFolder = "source/"; // 导入源文件保存路径

	public static List<List<List<Object>>> getExcelList(byte[] bytes, String originalFilename) throws IOException {
		String newFileName = System.currentTimeMillis() + "_" + originalFilename;

		String tempPathStr = StringU.buffer(null, tempFolder, File.separator, newFileName);
		Path tempPath = Paths.get(tempPathStr);
		if (!Files.isWritable(tempPath)) {
			Files.createDirectories(Paths.get(tempFolder));
		}

		String sourcePathStr = StringU.buffer(null, tempFolder, sourceFolder, File.separator, newFileName);
		Path sourcePath = Paths.get(sourcePathStr);
		if (!Files.isWritable(sourcePath)) {
			Files.createDirectories(Paths.get(tempFolder + sourceFolder));
		}

		Files.write(tempPath, bytes);
		File tempFile = new File(tempPathStr);
		List<List<List<Object>>> list = readExcel(tempFile);

		try{
			File sourceFile = new File(sourcePathStr);
			tempFile.renameTo(sourceFile);
		} catch (Exception e) {
			log.error("移动导入源文件出错", e);
		}

		return list;
	}

	/**
	 * 获取上传文件表单数据（包括其他input字段）
	 * @param request
	 * @return
	 * @throws IOException
	 * @author smalle
	 * @date 2016年7月18日 下午3:04:59
	 */
	public static Map<String, Object> getData(HttpServletRequest request) throws IOException {
		Map<String, Object> retMap = new HashMap<>();
		List<List<List<Object>>> excelList = new ArrayList<>();

		// 判断enctype属性是否为multipart/form-data  
		// boolean isMultipart = ServletFileUpload.isMultipartContent(request);  

		DiskFileItemFactory factory = new DiskFileItemFactory();
		  
		// 当上传文件太大时，因为虚拟机能使用的内存是有限的，所以此时要通过临时文件来实现上传文件的保存 ，此方法是设置是否使用临时文件的临界值（单位：字节）   
		factory.setSizeThreshold(1024*1024);  
		// 与上一个结合使用，设置临时文件的路径（绝对路径） 
		File tempFolderFile = new File(tempFolder);
        if(!tempFolderFile.isDirectory()) {
        	tempFolderFile.mkdirs();
        }
		factory.setRepository(tempFolderFile);  

		ServletFileUpload upload = new ServletFileUpload(factory);
		  
		// 设置上传内容的大小限制（单位：字节）  
		// upload.setSizeMax(yourMaxRequestSize);

		try {
			List<?> items = upload.parseRequest(request);
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
			  
			    if (item.isFormField()) {  
			        // 如果是普通表单字段  
			        String name = item.getFieldName();
			        String value = item.getString();
			        retMap.put(name, value);
			    } else {  
			        // 如果是excel文件字段/列名  (可直接读流)
			    	String fileName = item.getName();
		            File tempFile = new File(tempFolderFile.getCanonicalPath() + File.separator + fileName);
					item.write(tempFile);
					excelList = readExcel(tempFile);
					retMap.put("excelList", excelList);
			    }  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		  
		return retMap;
	}
	
	/**
	 * 导出excel到浏览器
	 * @param response
	 * @param exportData 需要导出的全部数据。MiscU.Instance.toMap("用户信息", List<Map<String, Object>>数据)
	 * @param exportFields 全部数据中的某些字段名。MiscU.Instance.toList(new String[]{"username", "sex"});
	 * @param headNames Excel的列名。MiscU.Instance.toList(new String[]{"用户名", "性别"});
	 * @param titles Excel的标题和Sheet名。new String[]{"用户信息"}
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @author smalle
	 * @date 2016年10月24日 下午4:15:29
	 */
	public static void export(HttpServletResponse response, LinkedHashMap<String, List> exportData, List<String[]> exportFields, List<String[]> headNames, String[] titles)
            throws IOException, IllegalArgumentException, IllegalAccessException {
		OutputStream ouputStream = null;
		try {
			ExcelExport.ExportSetInfo setInfo = new ExcelExport.ExportSetInfo();
			ouputStream = response.getOutputStream();
			Date date = new Date();
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
	        String filename = sdf.format(date);
	        
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
	        response.setHeader("Content-disposition", "attachment;filename=" + filename + ".xls");
	        setInfo.setObjsMap(exportData);
	        setInfo.setFieldNames(exportFields);
	        setInfo.setHeadNames(headNames);
	        setInfo.setTitles(titles);
	        setInfo.setOut(ouputStream);

	        ExcelExport.export2ExcelByList(setInfo);

	        ouputStream.flush();
		} finally {
			if(ouputStream != null) {
				ouputStream.close();
			}
		}
	}
	
	/**
	 * 导出文件到本地
	 * @param filePath 文件路径
	 * @param exportData 需要导出的全部数据
	 * @param exportFields 全部数据中的某些字段名
	 * @param headNames Excel的列名
	 * @param titles Excel的标题和Sheet名
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @author smalle
	 * @date 2016年10月24日 下午5:50:16
	 */
	public static void export(String filePath, LinkedHashMap<String, List> exportData, List<String[]> exportFields, List<String[]> headNames, String[] titles)
            throws IOException, IllegalArgumentException, IllegalAccessException {
		ExcelExport.ExportSetInfo setInfo = new ExcelExport.ExportSetInfo();
		
		File file = new File(filePath);
        String separator = File.separator.equals("\\") ? "\\\\" : File.separator;
        String pathString = file.getPath().replaceAll(separator + file.getName(), "");
        File dirFile = new File(pathString);
        if(!dirFile.isDirectory()) {
        	dirFile.mkdirs();
        }
        OutputStream outputStream = new FileOutputStream(file);
        
        setInfo.setObjsMap(exportData);
        setInfo.setFieldNames(exportFields);
        setInfo.setHeadNames(headNames);
        setInfo.setTitles(titles);
        setInfo.setOut(outputStream);

        ExcelExport.export2ExcelByList(setInfo);

		outputStream.flush();
		outputStream.close();
	}

    /**
     * 导出excel
     * @param response
     * @param result
     * @param titles
     * @param keys
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IOException
     * @author smalle
     * @date 2018年3月7日 上午10:17:57
     */
    public static void export(HttpServletResponse response, List<Map<String, Object>> result, String[] titles, String[] keys)
            throws IllegalArgumentException, IllegalAccessException, IOException {
        OutputStream os = null;
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet();
            int rowIndex = 0;
            ExcelU.setRow(sheet, rowIndex++, titles);
            CellStyle style = wb.createCellStyle();

            for (Map<String, Object> item : result) {
                ExcelU.setRowByKeys(sheet, rowIndex++, item, keys, style);
            }

            for(int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            String filename = sdf.format(date);
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + filename + ".xls");

            os = response.getOutputStream();
            wb.write(os);
            os.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public static void export(String filePathFull, List<List<Object>> exportData, String sheetName)
			throws IOException, IllegalArgumentException {
		if(ValidU.isEmpty(exportData.size())) {
			throw new IllegalStateException("无有效数据");
		}

		OutputStream ouputStream = null;
		try {
			File file = new File(filePathFull);
			String separator = File.separator.equals("\\") ? "\\\\" : File.separator;
			String pathString = file.getPath().substring(0, file.getPath().lastIndexOf(File.separator));
//			String pathString = file.getPath().replace(separator + file.getName(), "");
			File dirFile = new File(pathString);
			if(!dirFile.isDirectory()) {
				dirFile.mkdirs();
			}
			ouputStream = new FileOutputStream(file);

			Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet(sheetName);
			int count = 1;
			int j;
			for (int i = 0; i < exportData.size(); i++) {
				j = i - (count - 1) * 65535;
				if(i / 65535 > count - 1) {
					// 单个sheet最大只能为65535行
					sheet = wb.createSheet(sheetName + count);
					count ++;
				}

				setRow(sheet, j, exportData.get(i));
			}

			wb.write(ouputStream);
			ouputStream.flush();
		} finally {
			if(ouputStream != null) {
                ouputStream.close();
            }
		}
	}
	
	/**
	 * 选择读取2003或者2007的excel
	 * @param file
	 * @return
	 * @throws IOException
	 * @author smalle
	 * @date 2016年10月24日 下午1:12:24
	 */
	public static List<List<List<Object>>> readExcel(File file) throws IOException {
		String fileName = file.getName();
		String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1);

		Workbook wb = null;
		if ("xls".equals(extension)) {
			wb = new HSSFWorkbook(new FileInputStream(file));
		} else if ("xlsx".equals(extension)) {
			wb = new XSSFWorkbook(new FileInputStream(file));
		} else {
			throw new IOException("不支持的文件类型");
		}

		List<List<List<Object>>> sheetList = new LinkedList<List<List<Object>>>();
		for (int s = 0; s < wb.getNumberOfSheets(); s++) {
			List<List<Object>> list = new LinkedList<>();
			Sheet sheet = wb.getSheetAt(s); // 获取第s个sheet
			Row row; // 行
			Cell cell; // 单元格
			Object value;
			int counter = 0;
			int sheetTitleCols = 0;
			for (int i = sheet.getFirstRowNum(); counter < sheet.getPhysicalNumberOfRows(); i++) {
				row = sheet.getRow(i);
				if (row == null) {
					continue;
				} else {
					counter ++;
				}

				if(counter == 1) {
					sheetTitleCols = row.getLastCellNum();
				}

				List<Object> linked = new LinkedList<>();
				for (int j = 0; j < sheetTitleCols; j++) {
					cell = row.getCell(j);
					if (cell == null) {
						linked.add(null);
						continue;
					}

					value = getCellValue(cell);
					if(value instanceof String) {
                        value = ValidU.isEmpty(value) ? "" : ((String) value).trim();
                    }

					linked.add(value);
				}

				list.add(linked);
			}

			sheetList.add(list);
		}

		// file.delete();

		return sheetList;
	}

	/**
	 * 根据单元格类型获取单元格的值
	 * @param cell
	 * @return
	 * @author smalle
	 * @date 2016年10月24日 下午1:10:22
	 */
	private static Object getCellValue(Cell cell) {
		Object value = null;
		try {
			switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_NUMERIC:
					// yyyy-MM-dd-----	14
					// yyyy年m月d日---	31
					// yyyy年m月-------	57
					// m月d日  --------- 58
					// HH:mm-----------	20
					// h时mm分  -------	32
					List dateFormat = MiscU.toList("14", "31", "57", "58", "20", "32");
					if(HSSFDateUtil.isCellDateFormatted(cell)
						|| (cell.getCellStyle().getDataFormatString() != null
								&& cell.getCellStyle().getDataFormatString().contains("m")
							|| dateFormat.contains(cell.getCellStyle().getDataFormat() + "")
							)
					) {
						// 狭义的定为日期型
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						value = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
						break;
					}
				default:
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					value = cell.getStringCellValue();
			}
		} catch (Exception e) {
			log.error("read excel data error", e);
		}
		
		return value;
	}
	
	/**
	 * 创建一个合并单元格行
	 * @param wb
	 * @param sheet
	 * @param value
	 * @param fontWeight 字体粗细
	 * @param indexs 分别为：firstRow, lastRow, firstCol, lastCol
	 */
	public static void setMergeRow(Workbook wb, Sheet sheet, String value, Short fontWeight, int... indexs) {
		int lastRow = 0;
		int firstCol = 0;
		int lastCol = 6;
		if(indexs.length > 1) {
            lastRow = indexs[1];
        }
		if(indexs.length > 2) {
            firstCol = indexs[2];
        }
		if(indexs.length > 3) {
            lastCol = indexs[3];
        }
		
		CellRangeAddress rowRange = new CellRangeAddress(indexs[0], lastRow, firstCol, lastCol);
		Row row = sheet.createRow(indexs[0]);
		row.setHeight((short) 350);
		sheet.addMergedRegion(rowRange);
        
        Cell titleCell = row.createCell(0);
        
        if(ValidU.isNotEmpty(fontWeight)) {
        	CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
        }
        
        titleCell.setCellValue(value);
	}
	
	/**
	 * 设置一行的值
	 * @param sheet
	 * @param rowIndex 行下标
	 * @param values
	 */
	public static void setRow(Sheet sheet, int rowIndex, Object... values) {
		setRow2(sheet, rowIndex, values);
	}
	
	/**
	 * 设置一行的值
	 * @param sheet
	 * @param rowIndex 行下标
	 * @param values
	 */
	public static void setRow(Sheet sheet, int rowIndex, List values) {
		setRow2(sheet, rowIndex, values.toArray());
	}
	
	public static void setRow(Row row, int rowIndex, Object[] values) {
		setRowWithStartCol(row, rowIndex, 0, values);
	}
	
	/**
	 * 设置一行的值
	 * @param sheet
	 * @param rowIndex 行下标
	 * @param values
	 */
	public static void setRow2(Sheet sheet, int rowIndex, Object[] values) {
		setRowWithStartCol(sheet, rowIndex, 0, values);
	}
	
	public static void setRowWithStartCol(Sheet sheet, int rowIndex, int startCol, List values) {
		setRowWithStartCol(sheet, rowIndex, startCol, values.toArray());
	}
	
	public static void setRowWithStartCol(Sheet sheet, int rowIndex, int startCol, Object[] values) {
		Row row = sheet.createRow(rowIndex);
		setRowWithStartCol(row, rowIndex, startCol, values);
	}
	
	public static void setRowWithStartCol(Row row, int rowIndex, int startCol, List values) {
		setRowWithStartCol(row, rowIndex, startCol, values.toArray());
	}
	
	public static void setRowWithStartCol(Row row, int rowIndex, int startCol, Object[] values) {
		if(values != null) {
			for (int i = 0; i < values.length; i++) {
				Cell cell = row.createCell(i + startCol);
				
				String value = "";
				if(ValidU.isNotEmpty(values[i])) {
					if(values[i] instanceof Date) {
						value = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format((Date) values[i]);
					} else if(values[i] instanceof Long){
						Long val = (Long) values[i];
						cell.setCellValue(val.doubleValue());
						continue;
					} else {
						value = String.valueOf(values[i]);
					}
				}
			    cell.setCellValue(value);
			}
		}
	}
	
	/**
	 * 基于一行进行单元格合并
	 * @param sheet
	 * @param rowIndex
	 * @param startCol 起始合并单元格
	 * @param values
	 * @param mergeCols
	 */
	public static void setRowMergeCol(Sheet sheet, int rowIndex, int startCol, Object[] values, int... mergeCols) {
		Row row = sheet.createRow(rowIndex);
		
		int start = startCol;
		int total = 0;
		for (int i = 0; i < mergeCols.length; i++) {
			int cols = mergeCols[i];
			int end = start + cols - 1;
			
			CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex, start, end);
			sheet.addMergedRegion(cra);
			
			Cell cell = row.createCell(start);
			String value = "";
			if(ValidU.isNotEmpty(values[i])) {
				if(values[i] instanceof Date) {
					value = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format((Date) values[i]);
				} else {
					value = String.valueOf(values[i]);
				}
			}
		    cell.setCellValue(value);
			
			total += cols;
			start = startCol + total;
		}
	}
	
	/**
	 * 根据key从map中取值插入到某行中
	 * @param row
	 * @param valueMap
	 * @param keys
	 * @param colOffset valueMap中第一列向右的偏移
	 */
	public static void setRowByKeys(Row row, Map valueMap, Object[] keys, Integer colOffset) {
		if(keys != null) {
			for (int i = 0; i < keys.length; i++) {
				Cell cell = null;
				if(ValidU.isNotEmpty(colOffset)) {
					cell = row.createCell(i + colOffset);
				} else {
					cell = row.createCell(i);
				}
				
				String value = "";
				if(ValidU.isNotEmpty(keys[i])) {
					Object valueObj = valueMap.get(keys[i]);
					
					if(valueObj instanceof Timestamp) {
						value = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format((Timestamp) valueObj);
					} else if(ValidU.isNotEmpty(valueObj)) {
						value = String.valueOf(valueObj);
					}
				}
				cell.setCellValue(value);
			}
		}
	}
	
	public static void setRowByKeys(Sheet sheet, int rowIndex, Map valueMap, Object[] keys, CellStyle style) {
		if(keys != null) {
			Row row = sheet.createRow(rowIndex);
			for (int i = 0; i < keys.length; i++) {
				Cell cell = row.createCell(i);
				String value = "";
				Boolean isNum = false;
		        Boolean isInteger = false;
				if(ValidU.isNotEmpty(keys[i])) {
					Object valueObj = valueMap.get(keys[i]);
					if (null != valueObj && !"".equals(valueObj)) {
		            	isNum = valueObj.toString().matches("^(-?\\d+)(\\.\\d+)?$");//判断是否是数值
		            	isInteger = valueObj.toString().matches("^[-\\+]?[\\d]*$");//是否为整数，小数部分为0
		            }
					
					if (isInteger) {
	            		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,#0"));//只显示原整数
	            		cell.setCellStyle(style);
	            	} else if(isNum) {
	            		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));//保留两位小数点
	            		cell.setCellStyle(style);
	            	}
					
					if(valueObj instanceof Timestamp) {
						value = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format((Timestamp) valueObj);
					} else if(ValidU.isNotEmpty(valueObj)) {
						value = String.valueOf(valueObj);
					}
				}
				
				if(isNum || isInteger) {
					cell.setCellValue(Double.parseDouble(value));
				} else {
					cell.setCellValue(value);
				}
				
			}
		}
	}
	
	/**
	 * 根据key从map中取值插入到某行中
	 * @param sheet
	 * @param rowIndex 行下标
	 * @param valueMap
	 * @param keys
	 * @param colOffset valueMap中第一列向右的偏移
	 */
	public static void setRowByKeys(Sheet sheet, int rowIndex, Map valueMap, Object[] keys, Integer colOffset) {
		Row row = sheet.createRow(rowIndex);
		setRowByKeys(row, valueMap, keys, colOffset);
	}

	public static String getTempFolder() {
		return tempFolder;
	}

	public static void setTempFolder(String tempFolder) {
		ExcelU.tempFolder = tempFolder;
	}

}
