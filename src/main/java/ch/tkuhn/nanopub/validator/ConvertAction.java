package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.link.Link;
import org.openrdf.rio.RDFFormat;

public class ConvertAction extends Link<Object> {

	private static final long serialVersionUID = -8778391683346409183L;

	private RDFFormat format;
	private ValidatorPage mainPage;
	
	public ConvertAction(String id, RDFFormat format, ValidatorPage mainPage) {
		super(id);
		this.format = format;
		this.mainPage = mainPage;
	}

	@Override
	public void onClick() {
		try {
			String initialFormatName = "(unknown)";
			if (mainPage.getFormat() != null) {
				initialFormatName = mainPage.getFormat().getName();
			}
			mainPage.setNanopub(mainPage.getNanopub(), format, ValidatorPage.CONVERTED);
			mainPage.setMessage("Nanopublication", "", "Converted from " + initialFormatName + ":");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
