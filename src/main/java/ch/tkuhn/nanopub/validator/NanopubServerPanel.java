package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class NanopubServerPanel extends Panel {

	private static final long serialVersionUID = -5385086583534131943L;

	private Model<String> textFieldModel;

	public NanopubServerPanel(String id, ValidatorPage page) {
		super(id);
		add(new UrlForm("form", page));
	}

	public void setText(String text) {
		textFieldModel.setObject(text);
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
			textField = new TextField<String>("uriInput", textFieldModel);
			add(textField);
		}

		@Override
		protected void onSubmit() {
			String text = textField.getModelObject();
			if (text != null && !text.isEmpty()) {
				page.showResult(ValidatorPage.NANOPUB_SERVER_MODE, text);
			} else {
				page.clear();
			}
		}
	}

}
