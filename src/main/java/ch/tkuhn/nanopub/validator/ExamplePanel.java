package ch.tkuhn.nanopub.validator;

import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class ExamplePanel extends Panel {

	private static final long serialVersionUID = -7190105906448504120L;

	public ExamplePanel(String id, ValidatorPage page) {
		super(id);
		add(new ExampleForm("form", page));
	}

	private class ExampleForm extends Form<Void> {

		private static final long serialVersionUID = -7092493023464899908L;

		private Select<String> select;
		private ValidatorPage page;
		private String selected = "example1";

		public ExampleForm(String name, ValidatorPage page) {
			super(name);
			this.page = page;
			setMultiPart(true);
			select = new Select<String>("exampleSelect", new PropertyModel<String>(this, "selected"));
			select.add(new SelectOption<String>("example1", new Model<String>("example1")));
			add(select);
		}

		@Override
		protected void onSubmit() {
			String selected = select.getModelObject();
			if (selected != null) {
				page.showResult(ValidatorPage.EXAMPLE_MODE, "example.trig");
			} else {
				page.clear();
			}
		}

		@SuppressWarnings("unused")
		public String getSelected() {
			return selected;
		}

		@SuppressWarnings("unused")
		public void setSelected(String selected) {
			this.selected = selected;
		}

	}

}
