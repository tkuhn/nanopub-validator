package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class SparqlEndpointPanel extends Panel {

	private static final long serialVersionUID = -8661484183644334786L;

	public SparqlEndpointPanel(String id, ValidatorPage page) {
		super(id);
		add(new UrlForm("form", page));
	}

	private class UrlForm extends Form<Void> {

		private static final long serialVersionUID = 8466486026244124882L;

		TextField<String> sparqlEndpointUrlTextField;
		TextField<String> nanopubUriTextField;
		ValidatorPage page;

		public UrlForm(String name, ValidatorPage page) {
			super(name);
			this.page = page;
			setMultiPart(true);
			sparqlEndpointUrlTextField = new TextField<String>("sparqlEndpointUrlInput", new Model<String>());
			add(sparqlEndpointUrlTextField);
			nanopubUriTextField = new TextField<String>("nanopubUriInput", new Model<String>());
			add(nanopubUriTextField);
		}

		@Override
		protected void onSubmit() {
			String seu = sparqlEndpointUrlTextField.getModelObject();
			String nu = nanopubUriTextField.getModelObject();
			if (seu != null && !seu.isEmpty() && nu != null && !nu.isEmpty()) {
				page.showResult(ValidatorPage.SPARQL_ENDPOINT_MODE, seu, nu);
			} else {
				page.clear();
			}
		}
	}

}
