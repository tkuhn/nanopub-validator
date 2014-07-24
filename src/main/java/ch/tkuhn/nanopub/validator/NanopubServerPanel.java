package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class NanopubServerPanel extends Panel {

	private static final long serialVersionUID = -5385086583534131943L;

	public NanopubServerPanel(String id, ValidatorPage page) {
		super(id);
		add(new UrlForm("form", page));
	}

	private class UrlForm extends Form<Void> {

		private static final long serialVersionUID = 1129252599407413426L;

		TextField<String> textField;
		ValidatorPage page;

		public UrlForm(String name, ValidatorPage page) {
			super(name);
			this.page = page;
			setMultiPart(true);
			textField = new TextField<String>("uriInput", new Model<String>());
			add(textField);
		}

		@Override
		protected void onSubmit() {
			String text = textField.getModelObject();
			if (text != null && !text.isEmpty()) {
				page.showResult(ValidatorPage.TRUSTY_URI_MODE, text);
			} else {
				page.clear();
			}
		}
	}

}
