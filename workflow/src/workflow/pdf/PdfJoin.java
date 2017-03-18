package workflow.pdf;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import ariba.util.core.ListUtil;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

public class PdfJoin {
	private List<PdfReader> readers;
	private String outputFilePath;

	public PdfJoin(String outputFilePath) {
		this.readers = ListUtil.list();
		this.outputFilePath = outputFilePath;
	}
	
	public void addReader(PdfReader reader) {
		this.readers.add(reader);
	}
	
	public byte[] joinedPdf() throws Exception {
		Document document = null;
		PdfCopy writer = null;
		
		for(PdfReader reader : readers) {
			reader.consolidateNamedDestinations();
			int numPages = reader.getNumberOfPages();
			
			if(document == null) {
				document = new Document(reader.getPageSizeWithRotation(1));
				writer = new PdfCopy(document, new FileOutputStream(outputFilePath));
				document.open();
			}
			
			PdfImportedPage page;
			for(int i = 1; i <= numPages; i++) {
				page = writer.getImportedPage(reader, i);
				writer.addPage(page);
			}
		}
		
		if(document != null)
			document.close();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(outputFilePath);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] readBytes = new byte[8192];
		int numRead = 0;
		while((numRead = bis.read(readBytes)) != -1) {
			bos.write(readBytes, 0, numRead);
		}
		return bos.toByteArray();
	}
}
