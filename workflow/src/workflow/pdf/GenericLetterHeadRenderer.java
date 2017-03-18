package workflow.pdf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;

public class GenericLetterHeadRenderer extends LetterHeadRenderer {

	float lastYline = 0.0f;
	private String content;
	private String division;
	private String userName;
	
	public GenericLetterHeadRenderer(String postalCode, String addressLine1,
			String addressLine2, String addressLine3, String companyName,
			String customerName, String content, String division, String userName, String templateFileName) {
		super(postalCode, addressLine1, addressLine2, addressLine3, companyName,
				customerName, null, templateFileName);
		this.content = content;
		this.division = division;
		this.userName = userName;
	}

	@Override
	protected float lastContentYLine() {
		return lastYline;
	}

	@Override
	protected String postalCodeMark() {
		return this.getMessage("postalcode", "");
	}

	@Override
	protected String sama() {
		return this.getMessage("sama", "");
	}

	@Override
	protected DateFormat dateFormat() {
		String fmt = this.getMessage("default.date.format", "yyyy/MM/dd");
		return new SimpleDateFormat(fmt);
	}

	@Override
	protected String myCompanyName() {
		return this.getMessage("default.company.name", "PWM Japan Securities");
	}

	@Override
	protected String myDivisionName() {
		return division;
	}

	@Override
	protected String farewellMessage() {
		return null;
	}

	private Box contentBox() {
		return new Box(MARGIN_LEFT, MM_IN_POINT * 200, this.getContentWidth(), MM_IN_POINT * 20);
	}
	
	@Override
	protected void renderContent(PdfContentByte canvas) throws Exception {
		ColumnText ct = this.addColumnText(canvas, content, getDefaultFont(), Element.ALIGN_LEFT, this.contentBox());
		lastYline = ct.getYLine();
	}

	@Override
	protected Box divisionBox() {
		Box box = super.divisionBox();
		box.h += 5;
		return box;
	}

	@Override
	protected String divisionText() {
		String text =  super.divisionText();
		if(this.userName != null) {
			text = text + "\n" + this.userName;
		}
		return text;
	}

	protected Font getDefaultFont() {
		return this.getRomanFontOfSize(10);
	}

}
