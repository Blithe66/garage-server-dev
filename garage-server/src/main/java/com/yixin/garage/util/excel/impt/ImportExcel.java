/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.yixin.garage.util.excel.impt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.yixin.garage.util.excel.inptVO.DataCreateImportVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.yixin.common.exception.BzException;
import com.yixin.garage.annotation.ExcelField;
import com.yixin.garage.util.Reflections;

/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式） <br>
 * 参考jeesite
 * 
 * Package : com.yixin.rentcar.utils.common.excel
 * @author YixinCapital -- pangguowei 2017年5月18日 下午3:54:30
 */
public class ImportExcel {
	
	private static Logger log = LoggerFactory.getLogger(ImportExcel.class);
	
	private static final String YES="是";
	private static final String NO="否"; 
			
	/**
	 * 工作薄对象
	 */
	private Workbook wb;
	
	/**
	 * 工作表对象
	 */
	private Sheet sheet;
	
	/**
	 * 标题行号
	 */
	private int headerNum;
	
	/**
	 * 构造函数
	 * @param path 导入文件，读取第一个工作表
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ImportExcel(String fileName, int headerNum) throws InvalidFormatException, IOException {
		this(new File(fileName), headerNum);
	}
	
	/**
	 * 构造函数
	 * @param path 导入文件对象，读取第一个工作表
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ImportExcel(File file, int headerNum) throws InvalidFormatException, IOException {
		this(file, headerNum, 0);
	}

	/**
	 * 构造函数
	 * @param path 导入文件
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ImportExcel(String fileName, int headerNum, int sheetIndex) 
			throws InvalidFormatException, IOException {
		this(new File(fileName), headerNum, sheetIndex);
	}
	
	/**
	 * 构造函数
	 * @param path 导入文件对象
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ImportExcel(File file, int headerNum, int sheetIndex) 
			throws InvalidFormatException, IOException {
		this(file.getName(), new FileInputStream(file), headerNum, sheetIndex);
	}
	
	/**
	 * 构造函数
	 * @param file 导入文件对象
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ImportExcel(MultipartFile multipartFile, int headerNum, int sheetIndex) 
			throws InvalidFormatException, IOException {
		this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), headerNum, sheetIndex);
	}

	/**
	 * 构造函数
	 * @param path 导入文件对象
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ImportExcel(String fileName, InputStream is, int headerNum, int sheetIndex) 
			throws InvalidFormatException, IOException {
		if (StringUtils.isBlank(fileName)) {
			throw new BzException("导入文档为空!");
		} else if (fileName.toLowerCase().endsWith("xls")) {
			this.wb = new HSSFWorkbook(is);
		} else if (fileName.toLowerCase().endsWith("xlsx")) {
			this.wb = new XSSFWorkbook(is);
		} else {
			throw new BzException("文档格式不正确!");
		}
		if (this.wb.getNumberOfSheets() < sheetIndex) {
			throw new BzException("文档中没有工作表!");
		}
		this.sheet = this.wb.getSheetAt(sheetIndex);
		this.headerNum = headerNum;
		log.debug("Initialize success.");
	}
	
	/**
	 * 获取行对象
	 * @param rownum
	 * @return
	 */
	public Row getRow(int rownum){
		return this.sheet.getRow(rownum);
	}

	/**
	 * 获取数据行号
	 * @return
	 */
	public int getDataRowNum(){
		return headerNum+1;
	}
	
	/**
	 * 获取最后一个数据行号
	 * @return
	 */
	public int getLastDataRowNum(){
		return this.sheet.getLastRowNum()+headerNum;
	}
	
	/**
	 * 获取最后一个列号
	 * @return
	 */
	public int getLastCellNum(){
		return this.getRow(headerNum).getLastCellNum();
	}
	
	/**
	 * 获取单元格值
	 * @param row 获取的行
	 * @param column 获取单元格列号
	 * @return 单元格值
	 */
	public Object getCellValue(Row row, int column){
		Object value = "";
		try {
			Cell cell = row.getCell(column);
    	        switch(cell.getCellType()){
    	            case STRING:
    	                value = cell.getStringCellValue();
    	                break;
    	            case NUMERIC:
    	                value = cell.getNumericCellValue();
    	                break;
    	            case BOOLEAN:
    	                value = cell.getBooleanCellValue();
    	                break;
    	            case FORMULA:
    	               // value = new FormulaCellValue(cell.getCellFormula());
                        try {
                            value = cell.getNumericCellValue();
                        } catch (IllegalStateException e) {
                            value = String.valueOf(cell.getRichStringCellValue());
                        }
    	                    
    	                break;
    	            default:
    	                    value = null;
    	                    break;
    	        }
		} catch (Exception e) {
			return value;
		}
		return value;
	}
	
	/**
	 * 获取导入数据列表
	 * @param cls 导入对象类型
	 * @param groups 导入分组
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public <E> List<E> getDataList(Class<E> cls, int... groups) throws 
			InstantiationException, IllegalAccessException, SecurityException, 
		    IllegalArgumentException, NoSuchFieldException{
		List<Object[]> annotationList = Lists.newArrayList();
		
		// Get annotation field 
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs) {
			ExcelField ef = f.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
				if (groups != null && groups.length > 0) {
					boolean inGroup = false;
					for (int g : groups) {
						if (inGroup) {
							break;
						}
						for (int efg : ef.groups()) {
							if (g == efg){
								inGroup = true;
								annotationList.add(new Object[]{ef, f});
								break;
							}
						}
					}
				} else {
					annotationList.add(new Object[]{ef, f});
				}
			}
		}
		
		// Get annotation method
		Method[] ms = cls.getDeclaredMethods();
		for (Method m : ms){
			ExcelField ef = m.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
				if (groups != null && groups.length > 0) {
					boolean inGroup = false;
					for (int g : groups) {
						if (inGroup) {
							break;
						}
						for (int efg : ef.groups()) {
							if (g == efg) {
								inGroup = true;
								annotationList.add(new Object[]{ef, m});
								break;
							}
						}
					}
				} else {
					annotationList.add(new Object[]{ef, m});
				}
			}
		}
		
		// Field sorting
		Collections.sort(annotationList, new Comparator<Object[]>() {
			public int compare(Object[] o1, Object[] o2) {
				return new Integer(((ExcelField)o1[0]).sort()).compareTo(
					   new Integer(((ExcelField)o2[0]).sort()));
			};
		});
		log.debug("Import column count:"+annotationList.size());
		
		// Get excel data
		List<E> dataList = Lists.newArrayList();
		NumberFormat nf = NumberFormat.getInstance();
		for (int i = this.getDataRowNum(); i <= this.getLastDataRowNum(); i++) {
			E e = (E)cls.newInstance();
			int column = 0;
			Row row = this.getRow(i);
			StringBuilder sb = new StringBuilder();
			for (Object[] os : annotationList) {
				Object val = this.getCellValue(row, column++);
				if (val != null) {
					ExcelField ef = (ExcelField)os[0];
					
					// If is dict type, get dict value
					/*if (StringUtils.isNotBlank(ef.dictType())){
						val = DictUtils.getDictValue(val.toString(), ef.dictType(), "");
						//log.debug("Dictionary type value: ["+i+","+colunm+"] " + val);
					}*/
					
					// Get param type and type cast					
					Class<?> valType = Class.class;
					if (os[1] instanceof Field) {
						valType = ((Field)os[1]).getType();
					} else if (os[1] instanceof Method) {
						Method method = ((Method)os[1]);
						if ("get".equals(method.getName().substring(0, 3))) {
							valType = method.getReturnType();
						} else if ("set".equals(method.getName().substring(0, 3))) {
							valType = ((Method)os[1]).getParameterTypes()[0];
						}
					}
					log.debug("Import value type: ["+i+","+column+"] " + valType);
					
					try {
						if (valType == String.class) {
							String s = String.valueOf(val.toString());
							if (StringUtils.endsWith(s, ".0")) {
								val = StringUtils.substringBefore(s, ".0");
							} else {
								val = String.valueOf(val.toString().trim());
							}
						} else if (valType == Integer.class) {
							val = Double.valueOf(val.toString()).intValue();
						} else if (valType == Long.class) {
							val = Double.valueOf(val.toString()).longValue();
						} else if (valType == Double.class) {
							val = Double.valueOf(val.toString());
						} else if (valType == Float.class) {
							val = Float.valueOf(val.toString());
						} else if (valType == Date.class) {
							val = DateUtil.getJavaDate((Double)val);
						} else if (valType == BigDecimal.class) {
						    String s = nf.format(val);
						    if (s.indexOf(",") >= 0) {
						        s = s.replace(",", "");
						    }
                            val = new BigDecimal(s);
                        } else {
							if (ef.fieldType() != Class.class) {
								val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val.toString());
							} else {
								val = Class.forName(getClass().getName().replaceAll(getClass().getSimpleName(), 
														"fieldtype." + valType.getSimpleName() + "Type"))
										.getMethod("getValue", String.class).invoke(null, val.toString());
							}
						}
					} catch (Exception ex) {
						log.info("Get cell value [" + i + "," + column + "] error: " + ex.toString());
						val = null;
					}
					
					// set entity value
					if (os[1] instanceof Field) {
						Reflections.invokeSetter(e, ((Field)os[1]).getName(), val);
					} else if (os[1] instanceof Method) {
						String mthodName = ((Method)os[1]).getName();
						if ("get".equals(mthodName.substring(0, 3))) {
							mthodName = "set" + StringUtils.substringAfter(mthodName, "get");
						}
						Reflections.invokeMethod(e, mthodName, new Class[] {valType}, new Object[] {val});
					}
				}
				sb.append(val + ", ");
			}
			if (!Reflections.isNullObject(e, true)) {
				dataList.add(e);
			}
			log.debug("Read success: [" + i + "] " + sb.toString());
		}
		return dataList;
	}

	private static Field[] getAllFields(Class clazz) {
		List<Field> fieldList = new ArrayList<>();
		while (clazz != null) {
			fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		Field[] fields = new Field[fieldList.size()];
		fieldList.toArray(fields);
		return fields;
	}

	// 获取声明的所有方法
	private static Method[] getAllDeclaredMethods(Class clazz) {
		List<Method> methodList = new ArrayList<>();
		while (clazz != null && !clazz.equals(Object.class)) {
			methodList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods())));
			clazz = clazz.getSuperclass();
		}
		Method[] methods = new Method[methodList.size()];
		methodList.toArray(methods);
		return methods;
	}
	/**
	 * 获取导入数据列表, 不区分表格中字段顺序是否与DTO中字段顺序是否一致。
	 * @param cls 导入对象类型
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public <E> List<E> getDataListNew(Class<E> cls) throws 
			InstantiationException, IllegalAccessException, SecurityException, 
		    NoSuchFieldException{
		List<Object[]> annotationList = Lists.newArrayList();
		
		// Get annotation field 
//		Field[] fs = cls.getDeclaredFields();
		Field[] fs = getAllFields(cls);
		
		for (Field f : fs) {
			ExcelField ef = f.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
					annotationList.add(new Object[]{ef, f});
			}
		}
		
		// Get annotation method
		Method[] ms = getAllDeclaredMethods(cls);
		for (Method m : ms){
			ExcelField ef = m.getAnnotation(ExcelField.class);
			if (ef != null && (ef.type() == 0 || ef.type() == 2)) {
				
					annotationList.add(new Object[]{ef, m});
			}
		}
		int headerColumn = 0;
		Row headerRow = this.getRow(0);//标题列
		Map<String,Integer> headerMap = new HashMap<>(annotationList.size());
		Object headerName = this.getCellValue(headerRow, headerColumn);
		while(StringUtils.isNotBlank(headerName.toString())) {
			headerMap.put(headerName.toString(), headerColumn);
			headerName = this.getCellValue(headerRow, ++headerColumn);
		}
		if(headerMap.size()!=annotationList.size()){
			throw new BzException("excel标题与模板不一致,请核实！");
		}
		log.debug("Import column count:"+annotationList.size());
		
		// Get excel data
		List<E> dataList = Lists.newArrayList();
		for (int i = this.getDataRowNum(); i <= this.getLastDataRowNum(); i++) {
			E e = cls.newInstance();
			int column = 0;
			Row row = this.getRow(i);
			StringBuilder sb = new StringBuilder();
			for (Object[] os : annotationList) {
				ExcelField ef = (ExcelField)os[0];
				String annotationName = ef.title();
				if(headerMap.get(annotationName)==null){
					throw new BzException("excel标题与模板不一致,请核实！");
				}
				Object val = this.getCellValue(row, headerMap.get(annotationName));
				if (val != null) {
					// Get param type and type cast					
					Class<?> valType = Class.class;
					if (os[1] instanceof Field) {
						valType = ((Field)os[1]).getType();
					} else if (os[1] instanceof Method) {
						Method method = ((Method)os[1]);
						if ("get".equals(method.getName().substring(0, 3))) {
							valType = method.getReturnType();
						} else if ("set".equals(method.getName().substring(0, 3))) {
							valType = ((Method)os[1]).getParameterTypes()[0];
						}
					}
					log.debug("Import value type: ["+i+","+column+"] " + valType);
					
					try {
						if (valType == String.class) {
							if(StringUtils.isBlank(val.toString())) {
								val = null;
							}else {
								val = val.toString().trim();
							}
						} else if (valType == Integer.class) {
							val = Double.valueOf(val.toString()).intValue();
						} else if (valType == Long.class) {
							val = Double.valueOf(val.toString()).longValue();
						} else if (valType == Double.class) {
							val = Double.valueOf(val.toString());
						} else if (valType == Float.class) {
							val = Float.valueOf(val.toString());
						} else if (valType == Date.class) {
							val = DateUtil.getJavaDate(Double.valueOf(val.toString()));
						} else if (valType == BigDecimal.class){
							if(StringUtils.isNotBlank(val.toString())) {
								val = new BigDecimal(val.toString());
							}else {
								val = null;
							}
							
						}else if (valType == Boolean.class) { //
							if (YES.equals(val.toString().trim())) {
								val = Boolean.TRUE;
							}else if (NO.equals(val.toString().trim())) {
								val = Boolean.FALSE;
							}else {
								val = null;
							}
						}else {
							if (ef.fieldType() != Class.class) {
								val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val.toString());
							} else {
								val = Class.forName(getClass().getName().replaceAll(getClass().getSimpleName(), 
														"fieldtype." + valType.getSimpleName() + "Type"))
										.getMethod("getValue", String.class).invoke(null, val.toString());
							}
						}
					} catch (Exception ex) {
						log.info("Get cell value [" + i + "," + column + "] error: " + ex.toString());
						val = null;
					}
					
					// set entity value
					if (os[1] instanceof Field) {
						Reflections.invokeSetter(e, ((Field)os[1]).getName(), val);
					} else if (os[1] instanceof Method) {
						String mthodName = ((Method)os[1]).getName();
						if ("get".equals(mthodName.substring(0, 3))) {
							mthodName = "set" + StringUtils.substringAfter(mthodName, "get");
						}
						Reflections.invokeMethod(e, mthodName, new Class[] {valType}, new Object[] {val});
					}
				}
				sb.append(val + ", ");
			}
			if (!Reflections.isNullObject(e, true)) {
				dataList.add(e);
			}
			log.debug("Read success: [" + i + "] " + sb.toString());
		}
		return dataList;
	}
	/**
	 * 导入测试
	 */
	public static void main(String[] args) throws Throwable {

		ImportExcel ei = new ImportExcel("D:/aaa.xlsx", 0);

		List<DataCreateImportVO> importList = ei.getDataList(DataCreateImportVO.class);

		System.out.println(JSONObject.toJSON(importList));


//		for (int i = ei.getDataRowNum(); i < ei.getLastDataRowNum(); i++) {
//
//
//
//			Row row = ei.getRow(i);
//			for (int j = 0; j < ei.getLastCellNum(); j++) {
//				Object val = ei.getCellValue(row, j);
//				System.out.print(val+", ");
//			}
//			System.out.print("\n");
//		}

	}

}
