package ch.tkuhn.nanopub.validator;

import net.trustyuri.rdf.TransformNanopub;

import org.apache.wicket.markup.html.link.Link;

public class MakeTrusty extends Link<Object> {

	private static final long serialVersionUID = -4558302923207618223L;

	private ValidatorPage mainPage;
	
	public MakeTrusty(String id, ValidatorPage mainPage) {
		super(id);
		this.mainPage = mainPage;
	}

	@Override
	public void onClick() {
		try {
			mainPage.setNanopub(TransformNanopub.transform(mainPage.getNanopub()), true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}