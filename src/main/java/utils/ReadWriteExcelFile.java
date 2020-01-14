//package utils;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.util.CellAddress;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.net.URL;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//public class ReadWriteExcelFile {
//
//
//    public static void updatePriceFile() throws IOException {
//        URL url = new URL("https://docs.google.com/spreadsheets/d/13CVOVtKkDuoGKOU3chEgZ-W4WNEOuqwI96jMysih5eU/export?format=xlsx&id=13CVOVtKkDuoGKOU3chEgZ-W4WNEOuqwI96jMysih5eU");
//        Path workingDir = Paths.get(System.getProperty("user.dir"));
//        File file = new File(workingDir.resolve("db/global/prices.xlsx").toUri());
//        if (file.exists()) {
//            file.delete();
//        }
//        FileUtils.copyURLToFile(url, file);
//    }
//
//    public static int getPrice(String itemName) throws IOException {
//        int price = 0;
//        Path workingDir = Paths.get(System.getProperty("user.dir"));
//        File file = new File(workingDir.resolve("db/global/prices.xlsx").toUri());
//
//        FileInputStream excelFile = new FileInputStream(file);
//        Workbook workbook = new XSSFWorkbook(excelFile);
//        DataFormatter dataFormatter = new DataFormatter();
//
//
//        for (Sheet sheet : workbook) {
//            for (Cell cell : sheet.getRow(0)) {
//                String data = dataFormatter.formatCellValue(cell);
//                if (!data.isEmpty() && checkStrings(data.trim(), itemName.trim()) ? checkStrings(data.trim(), itemName.trim()) : checkSimilarStrings(data.trim(), itemName.trim())) {
//                    price = getPriceOf(workbook, sheet, cell.getAddress());
//                }
//            }
//        }
//
//        workbook.close();
//        return price;
//    }
//
//    private static int getPriceOf(Workbook workbook, Sheet sheet, CellAddress ca) {
//        int price = 0;
//        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
//        DataFormatter dataFormatter = new DataFormatter();
//        for (Row row : sheet) {
//            for (Cell cell : row) {
//                if (dataFormatter.formatCellValue(cell).equalsIgnoreCase("Total Average")) {
//                    for (Cell c : row) {
//                        if (c.getAddress().getColumn() == ca.getColumn()) {
//                            try {
//                                price = Integer.parseInt(dataFormatter.formatCellValue(c, evaluator).replaceAll(",", ""));
//                            } catch (NumberFormatException e) {
//                                price = -1;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return price;
//    }
//
//    private static Boolean checkStrings(String str1, String str2) {
//        if (str1.equalsIgnoreCase(str2)) {
//            return true;
//        } else if (str1.replaceAll("[^A-Za-z0-9]", "").equalsIgnoreCase(str2)) {
//            return true;
//        } else if (str2.toLowerCase().startsWith(str1.toLowerCase())) {
//            return true;
//        } else return str1.toLowerCase().startsWith(str2.toLowerCase());
//
//	}
//
//    private static Boolean checkSimilarStrings(String str1, String str2) {
//		return str1.toLowerCase().contains(str2.toLowerCase());
//	}
//}
