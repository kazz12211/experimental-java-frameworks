package workflow.pdf;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ariba.ui.aribaweb.core.AWServerApplication;
import ariba.ui.aribaweb.util.AWResource;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.util.core.StringUtil;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public abstract class PdfRenderer {

	protected NumberFormat MONEY_FORMAT = null;
	protected NumberFormat MONEY_FORMAT_RU = null;
	protected NumberFormat FOREIGN_MONEY_FORMAT = null;
	protected NumberFormat FOREIGN_MONEY_FORMAT_RU = null;
	protected NumberFormat PERCENT_FORMAT = null;
	protected NumberFormat INT_FORMAT = null;
	protected DateFormat DATE_FORMAT = null;
	protected NumberFormat EXCHANGE_RATE_FORMAT = null;

	protected File tempFile;
	protected Document document;
	protected PdfWriter writer;
	
	protected Colors colors = new Colors();

	public void init() throws Exception {
		
		MONEY_FORMAT = DecimalFormat.getNumberInstance();
		MONEY_FORMAT.setGroupingUsed(true);
		MONEY_FORMAT.setMaximumFractionDigits(0);
		MONEY_FORMAT.setMinimumFractionDigits(0);
		MONEY_FORMAT_RU = DecimalFormat.getNumberInstance();
		MONEY_FORMAT_RU.setGroupingUsed(true);
		MONEY_FORMAT_RU.setMaximumFractionDigits(0);
		MONEY_FORMAT_RU.setMinimumFractionDigits(0);
		MONEY_FORMAT_RU.setRoundingMode(RoundingMode.UP);
		FOREIGN_MONEY_FORMAT = DecimalFormat.getNumberInstance();
		FOREIGN_MONEY_FORMAT.setGroupingUsed(true);
		FOREIGN_MONEY_FORMAT.setMaximumFractionDigits(2);
		FOREIGN_MONEY_FORMAT.setMinimumFractionDigits(2);
		FOREIGN_MONEY_FORMAT_RU = DecimalFormat.getNumberInstance();
		FOREIGN_MONEY_FORMAT_RU.setGroupingUsed(true);
		FOREIGN_MONEY_FORMAT_RU.setMaximumFractionDigits(2);
		FOREIGN_MONEY_FORMAT_RU.setMinimumFractionDigits(2);
		FOREIGN_MONEY_FORMAT_RU.setRoundingMode(RoundingMode.UP);
		PERCENT_FORMAT = DecimalFormat.getNumberInstance();
		PERCENT_FORMAT.setGroupingUsed(true);
		PERCENT_FORMAT.setMaximumFractionDigits(2);
		PERCENT_FORMAT.setMinimumFractionDigits(2);
		INT_FORMAT = DecimalFormat.getNumberInstance();
		INT_FORMAT.setGroupingUsed(true);
		INT_FORMAT.setMaximumFractionDigits(0);
		INT_FORMAT.setMinimumFractionDigits(0);
		EXCHANGE_RATE_FORMAT = DecimalFormat.getNumberInstance();
		EXCHANGE_RATE_FORMAT.setGroupingUsed(true);
		EXCHANGE_RATE_FORMAT.setMaximumFractionDigits(3);
		EXCHANGE_RATE_FORMAT.setMinimumFractionDigits(3);
		DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	
		this.createTempFile();

		this.setupDocument();
		this.prepareWriter();

	}

	protected String _Money(double number) {
		return MONEY_FORMAT.format(number);
	}
	protected String _Money(double number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && number == 0.0)
			return stringIfZero;
		return _Money(number);
	}
	protected String _Money(Object number) {
		if(number == null)
			return  "";
		return MONEY_FORMAT.format(number);
	}
	protected String _Money(Object number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && (number == null || ((Number)number).doubleValue() == 0.0))
			return stringIfZero;
		return _Money(number);
	}
	
	protected String _MoneyRU(double number) {
		return MONEY_FORMAT_RU.format(number);
	}
	protected String _MoneyRU(double number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && number == 0.0)
			return stringIfZero;
		return _MoneyRU(number);
	}
	
	protected String _MoneyRU(Object number) {
		if(number == null)
			return  "";
		return MONEY_FORMAT_RU.format(number);
	}
	protected String _MoneyRU(Object number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && (number == null || ((Number)number).doubleValue() == 0.0))
			return stringIfZero;
		return _MoneyRU(number);
	}

	protected String _ForeignMoney(double number) {
		return FOREIGN_MONEY_FORMAT.format(number);
	}
	protected String _ForeignMoney(double number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && number == 0.0)
			return stringIfZero;
		return _ForeignMoney(number);
	}
	protected String _ForeignMoney(Object number) {
		if(number == null)
			return  "";
		return FOREIGN_MONEY_FORMAT.format(number);
	}
	protected String _ForeignMoney(Object number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && (number == null || ((Number)number).doubleValue() == 0.0))
			return stringIfZero;
		return _ForeignMoney(number);
	}

	protected String _ForeignMoneyRU(double number) {
		return FOREIGN_MONEY_FORMAT_RU.format(number);
	}
	protected String _ForeignMoneyRU(double number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && number == 0.0)
			return stringIfZero;
		return _ForeignMoneyRU(number);
	}
	protected String _ForeignMoneyRU(Object number) {
		if(number == null)
			return  "";
		return FOREIGN_MONEY_FORMAT_RU.format(number);
	}
	protected String _ForeignMoneyRU(Object number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && (number == null || ((Number)number).doubleValue() == 0.0))
			return stringIfZero;
		return _ForeignMoneyRU(number);
	}

	protected String _Percent(double number) {
		return PERCENT_FORMAT.format(number);
	}
	protected String _Percent(double number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && number == 0.0)
			return stringIfZero;
		return _Percent(number);
	}
	protected String _Percent(Object number) {
		if(number == null)
			return  "";
		return PERCENT_FORMAT.format(number);
	}
	protected String _Percent(Object number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && (number == null || ((Number)number).doubleValue() == 0.0))
			return stringIfZero;
		return _Percent(number);
	}

	protected String _Integer(Object number) {
		if(number == null)
			return "";
		return INT_FORMAT.format(number);
	}
	
	protected String _ExchangeRate(double number) {
		return EXCHANGE_RATE_FORMAT.format(number);
	}
	protected String _ExchangeRate(double number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && number == 0.0)
			return stringIfZero;
		return _ExchangeRate(number);
	}
	protected String _ExchangeRate(Object number) {
		if(number == null)
			return  "";
		return EXCHANGE_RATE_FORMAT.format(number);
	}
	protected String _ExchangeRate(Object number, String stringIfZero) {
		if(!StringUtil.nullOrEmptyString(stringIfZero) && (number == null || ((Number)number).doubleValue() == 0.0))
			return stringIfZero;
		return _ExchangeRate(number);
	}
	
	protected String _Date(Date date) {
		if(date == null)
			return  "";
		return DATE_FORMAT.format(date);
	}
	
	public PdfReader getReader() throws IOException {
		return new PdfReader(this.getData());
	}

	public abstract void render();

	protected abstract void setupDocument();
	protected abstract void createTempFile() throws IOException;

	protected void prepareWriter() throws Exception {
		if(document != null)
			writer = PdfWriter.getInstance(document, this.pdfOutputStream());
	}

	protected OutputStream pdfOutputStream() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tempFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fos;
	}

	public byte[] getData() throws IOException {
		if(tempFile != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(tempFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte[] readBytes = new byte[8192];
			int numRead = 0;
			while((numRead = bis.read(readBytes)) != -1) {
				bos.write(readBytes, 0, numRead);
			}
			return bos.toByteArray();
		}
		return null;
	}
	

	public void deleteTempFile() {
		if(tempFile.exists()) {
			tempFile.delete();
		}
	}
	public File getTempFile() {
		return tempFile;
	}

	protected String getResourcePath(String resourceName) {
		AWServerApplication app = AWServletApplication.sharedInstance();
		AWResource resource = app.resourceManager().resourceNamed(resourceName);
		return resource.fullUrl();
	}
	
	protected PdfPCell createCell(String string, Font font, int alignment, float grayFill) {
		Phrase phrase = new Phrase(string, font);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(alignment);
		cell.setGrayFill(grayFill);
		return cell;
	}
	
	protected PdfPCell createCell(String string, Font font, int alignment) {
		Phrase phrase = new Phrase(string, font);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(alignment);
		return cell;
	}
	

	protected PdfPCell createCell(String string, Font font, int alignment, int border) {
		Phrase phrase = new Phrase(string, font);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(alignment);
		cell.setBorder(border);
		return cell;
	}
	protected PdfPCell createCell(String string, Font font, int alignment, int border, float grayFill) {
		Phrase phrase = new Phrase(string, font);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(alignment);
		cell.setBorder(border);
		cell.setGrayFill(grayFill);
		return cell;
	}

	protected Font getGothicFontOfSizeAndStyle(int size, int style) {
		BaseFont base = null;
		try {
			base = BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Font font = new Font(base, size, style);
		font.setColor(colors.getColor("black"));
		return font;
	}
	
	protected Font getRomanFontOfSizeAndStyle(int size, int style) {
		BaseFont base = null;
		try {
			base = BaseFont.createFont("HeiseiMin-W3", "UniJIS-UCS2-HW-H", BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Font font = new Font(base, size, style);
		font.setColor(colors.getColor("black"));
		return font;
	}
	
	protected Font getFontOfSizeAndStyle(int size, int style) {
		return this.getGothicFontOfSizeAndStyle(size, style);
	}
	
	protected Font getFontOfSize(int size) {
		return this.getFontOfSizeAndStyle(size, Font.NORMAL);
	}
	
	protected Font getRomanFontOfSize(int size) {
		return this.getRomanFontOfSizeAndStyle(size, Font.NORMAL);
	}
	
	protected ColumnText addColumnText(PdfContentByte canvas, String string, Font font, int alignment, float x1, float y1, float x2, float y2) throws DocumentException {
		ColumnText text = new ColumnText(canvas);
		text.addText(new Phrase(string, font));
		text.setAlignment(alignment);
		text.setSimpleColumn(x1, y1, x2, y2);
		text.go();
		return text;
	}
}
