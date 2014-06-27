package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.link.Link;
import org.openrdf.rio.RDFFormat;

public class Convert extends Link<Object> {

	private static final long serialVersionUID = -8778391683346409183L;

	private RDFFormat format;
	private ValidatorPage mainPage;
	
	public Convert(String id, RDFFormat format, ValidatorPage mainPage) {
		super(id);
		this.format = format;
		this.mainPage = mainPage;
	}

	@Override
	public void onClick() {
		try {
			mainPage.setNanopub(mainPage.getNanopub(), format, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
