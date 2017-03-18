package workflow.pdf;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import workflow.util.Messages;
import ariba.util.core.StringUtil;
import ariba.util.log.Log;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public abstract class LetterHeadRenderer extends PdfRenderer {

	String postalCode;
	String addressLine1;
	String addressLine2;
	String addressLine3;
	String companyName;
	String customerName;
	String greeting;

	private String templateFileName;
	protected Locale locale;

	public static final float INCH 			= 25.4f; // 24.5mm
	public static final float POINT			= INCH / 72.0f;
	public static final float MM_IN_POINT	= 1.0f / INCH * 72.0f;
	public static final float MARGIN_LEFT	= MM_IN_POINT * 20.0f;
	public static final float MARGIN_RIGHT	= MM_IN_POINT * 20.0f;
	public static final float MARGIN_TOP	= MM_IN_POINT * 30.0f;
	public static final float MARGIN_BOTTOM	= MM_IN_POINT * 14.0f;
	
	public static final float DEFAULT_DIVISION_X = MM_IN_POINT * 120;
	public static final float DEFAULT_DIVISION_Y = MM_IN_POINT * 265;
	public static final float DEFAULT_DIVISION_W = PageSize.A4.getWidth() - MARGIN_RIGHT;
	public static final float DEFAULT_DIVISION_H = MM_IN_POINT * 15;
	
	public static final float DEFAULT_ADDRESS_X = MARGIN_LEFT;
	public static final float DEFAULT_ADDRESS_Y = MM_IN_POINT * 250;
	public static final float DEFAULT_ADDRESS_W = MM_IN_POINT * 95;
	public static final float DEFAULT_ADDRESS_H = MM_IN_POINT * 20;
	
	public static final float DEFAULT_NAME_X = MARGIN_LEFT;
	public static final float DEFAULT_NAME_Y = MM_IN_POINT * 225;
	public static final float DEFAULT_NAME_W = MM_IN_POINT * 95;
	public static final float DEFAULT_NAME_H = MM_IN_POINT * 15;
	
	public static final float DEFAULT_GREETING_X = MARGIN_LEFT;
	public static final float DEFAULT_GREETING_Y = MM_IN_POINT * 200;
	public static final float DEFAULT_GREETING_W = PageSize.A4.getWidth() - MARGIN_RIGHT;
	public static final float DEFAULT_GREETING_H = MM_IN_POINT * 20;

	/*
	 * addressLine1 = street address, addressLine2 = city/ward, addressLine3 = pref
	 */
	public LetterHeadRenderer(
			String postalCode, 
			String addressLine1, 
			String addressLine2,
			String addressLine3,
			String companyName,
			String customerName,
			String greeting,
			String templateFileName) {
		super();
		this.postalCode = postalCode;
		this.companyName = companyName;
		this.customerName = customerName;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.greeting = greeting;
		this.templateFileName = templateFileName;
		this.locale = Locale.getDefault();
	}
		
	
	protected PdfReader getTemplateReader() {
		String pdfFilePath = this.getResourcePath(templateFileName);
		PdfReader reader = null;
		try {
			reader = new PdfReader(pdfFilePath);
		} catch (IOException e) {
			Log.customer.error("LetterHeaderRenderer: could not locate PDF template file.", e);
		}
		return reader;
	}
	
	protected Box divisionBox() {
		return new Box(DEFAULT_DIVISION_X, DEFAULT_DIVISION_Y, DEFAULT_DIVISION_W, DEFAULT_DIVISION_H);
	}
	protected Box addressBox() {
		return new Box(DEFAULT_ADDRESS_X, DEFAULT_ADDRESS_Y, DEFAULT_ADDRESS_W, DEFAULT_ADDRESS_H);
	}
	protected Box nameBox() {
		return new Box(DEFAULT_NAME_X, DEFAULT_NAME_Y, DEFAULT_NAME_W, DEFAULT_NAME_H);
	}
	protected Box greetingBox() {
		return new Box(DEFAULT_GREETING_X, DEFAULT_GREETING_Y, DEFAULT_GREETING_W, DEFAULT_GREETING_H);
	}
	protected Box farewellBox() {
		float height = MM_IN_POINT * 10;
		return new Box(MARGIN_LEFT, this.lastContentYLine() - height, MARGIN_LEFT + this.getContentWidth(), height);
	}

	protected String divisionText() {
		DateFormat dateFormat = this.dateFormat();
		String string = dateFormat.format(new Date()) + "\n" +
				this.myCompanyName() + "\n" +
				this.myDivisionName();
		return string;
	}
	
	protected void renderDivision(PdfContentByte canvas) throws Exception {
		this.addColumnText(canvas, divisionText(), this.getDefaultFont(), Element.ALIGN_RIGHT, this.divisionBox());
	}

	protected void renderAddress(PdfContentByte canvas) throws Exception {
		String address = this.postalCodeMark() + " " + this.postalCode + "\n" + this.addressLine3 + "\n" + this.addressLine2 + "\n";
		if(!StringUtil.nullOrEmptyOrBlankString(this.addressLine1))
			address += this.addressLine1;
	
		this.addColumnText(canvas, address, this.getDefaultFont(), Element.ALIGN_LEFT, this.addressBox());
		
		String name = "";
		if(this.companyName != null)
			name = this.companyName + "\n";
		name += this.customerName + " " + this.sama();
		
		this.addColumnText(canvas, name, this.getDefaultFont(), Element.ALIGN_LEFT, this.nameBox());
	}
	
	protected void renderGreeting(PdfContentByte canvas) throws Exception {
		if(this.greeting == null)
			return;
		this.addColumnText(canvas, greeting, this.getDefaultFont(), Element.ALIGN_LEFT, this.greetingBox());
	}
	
	protected void renderFarewell(PdfContentByte canvas) throws Exception {
		if(this.farewellMessage() == null)
			return;
		
		Box box = this.farewellBox();
		
		this.addColumnText(canvas, this.farewellMessage(), this.getDefaultFont(), Element.ALIGN_LEFT, box);
	}
	
	protected abstract float lastContentYLine();
	


	protected abstract String postalCodeMark();
	protected abstract String sama();
	protected abstract DateFormat dateFormat();
	protected abstract String myCompanyName();
	protected abstract String myDivisionName();
	protected abstract String farewellMessage();
	
	
	protected void renderBody(PdfContentByte canvas) throws Exception {
		this.renderDivision(canvas);
		this.renderAddress(canvas);
		this.renderGreeting(canvas);
		this.renderContent(canvas);
		this.renderFarewell(canvas);
	}
	
	protected abstract void renderContent(PdfContentByte canvas) throws Exception;
	
	@Override
	public void render() {
		try {
			PdfReader reader = this.getTemplateReader();
			PdfStamper stamper = new PdfStamper(reader, this.pdfOutputStream());
			PdfContentByte canvas = stamper.getOverContent(1);

			canvas.beginText();
			
			this.renderBody(canvas);
			
			canvas.endText();
			stamper.close();

		} catch (Exception e) {
			Log.customer.error("LetterHeadRenderer failed to render PDF", e);
		}
	}
	
	@Override
	protected void setupDocument() {
		//document = new Document(PageSize.A4, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
	}

	@Override
	protected void createTempFile() throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		tempFile = File.createTempFile("letter_" + dateFormat.format(new Date()) + "_",".pdf", new File(
				System.getProperty("java.io.tmpdir")));
	}
	
	protected float getContentWidth() {
		return PageSize.A4.getWidth() - (MARGIN_LEFT + MARGIN_RIGHT);
	}

	protected float getContentHeight() {
		return PageSize.A4.getHeight() - (MARGIN_TOP + MARGIN_BOTTOM);
	}

	protected Font getDefaultFont() {
		return this.getRomanFontOfSize(9);
	}
	
	public String getMessage(String key, String defaultString) {
		if(this.locale == null)
			this.locale = Locale.getDefault();
		return Messages.getMessage(key, defaultString, locale);
	}
		
	protected ColumnText addColumnText(PdfContentByte canvas, String string, Font font, int alignment, Box bounds) throws DocumentException {
		return this.addColumnText(
				canvas, 
				string, 
				font, 
				alignment, 
				bounds.x, 
				bounds.y, 
				bounds.w, 
				bounds.h);
	}
	

	public class Box {
		public float x;
		public float y;
		public float w;
		public float h;
		public Box(float x, float y, float w, float h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
}
