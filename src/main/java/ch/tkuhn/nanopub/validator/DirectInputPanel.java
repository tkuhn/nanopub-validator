package ch.tkuhn.nanopub.validator;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.openrdf.rio.RDFFormat;

public class DirectInputPanel extends Panel {

	private static final long serialVersionUID = 5672448402585463667L;

	private static final List<String> FORMATS = Arrays.asList(new String[] { "TriG", "TriX", "N-Quads" });
	private String selectedFormat = "TriG";

	private Model<String> inputTextModel = new Model<>("");
	private PropertyModel<String> formatModel = new PropertyModel<String>(this, "selectedFormat");

	public DirectInputPanel(String id, final ValidatorPage page) {
		super(id);

		Form<?> form = new Form<Void>("form") {

			private static final long serialVersionUID = 7483611710394125186L;

			protected void onSubmit() {
				page.showResult(ValidatorPage.DIRECT_INPUT_MODE, inputTextModel.getObject(), getFormat());
			}

		};

		add(form);

		form.add(new TextArea<String>("nanopubtext", inputTextModel));
		form.add(new RadioChoice<String>("format", formatModel, FORMATS));
	}

	private RDFFormat getFormat() {
		if (selectedFormat.equals("TriG")) {
			return RDFFormat.TRIG;
		} else if (selectedFormat.equals("TriX")) {
			return RDFFormat.TRIX;
		} else if (selectedFormat.equals("N-Quads")) {
			return RDFFormat.NQUADS;
		}
		return null;
	}

}
