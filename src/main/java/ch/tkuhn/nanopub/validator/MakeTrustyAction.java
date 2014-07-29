package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.link.Link;
import org.nanopub.trusty.MakeTrustyNanopub;

public class MakeTrustyAction extends Link<Object> {

	private static final long serialVersionUID = -4558302923207618223L;

	private ValidatorPage mainPage;
	
	public MakeTrustyAction(String id, ValidatorPage mainPage) {
		super(id);
		this.mainPage = mainPage;
	}

	@Override
	public void onClick() {
		try {
			mainPage.showNanopub(MakeTrustyNanopub.transform(mainPage.getNanopub()), ValidatorPage.MADE_TRUSTY);
			mainPage.setMessage("Nanopublication", "Transformed to a nanopublication with a trusty URI:");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
