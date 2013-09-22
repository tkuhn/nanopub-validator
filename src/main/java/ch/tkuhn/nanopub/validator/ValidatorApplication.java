package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

public class ValidatorApplication extends WebApplication {

	@Override
	public Class<? extends WebPage> getHomePage() {
		return ValidatorPage.class;
	}

	public void init() {
		super.init();
	}

}
