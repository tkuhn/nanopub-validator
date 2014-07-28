package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class UrlPanel extends Panel {

	private static final long serialVersionUID = -5385086583534131943L;

	private Model<String> textFieldModel;

	public UrlPanel(String id, ValidatorPage page) {
		super(id);
		add(new UrlForm("form", page));
	}

	public void setUrl(String url) {
		textFieldModel.setObject(url);
	}

	private class UrlForm extends Form<Void> {

		private static final long serialVersionUID = 1129252599407413426L;

		TextField<String> textField;
		ValidatorPage page;

		public UrlForm(String name, ValidatorPage page) {
			super(name);
			this.page = page;
			setMultiPart(true);
			textFieldModel = new Model<String>();
			textField = new TextField<String>("urlInput", textFieldModel);
			add(textField);
		}

		@Override
		protected void onSubmit() {
			String text = textField.getModelObject();
			if (text != null && !text.isEmpty()) {
				page.showResult(ValidatorPage.URL_MODE, text);
			} else {
				page.clear();
			}
		}
	}

}
