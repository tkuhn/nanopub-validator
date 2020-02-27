package ch.tkuhn.nanopub.validator;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.eclipse.rdf4j.rio.RDFFormat;

public class DirectInputPanel extends Panel {

	private static final long serialVersionUID = 5672448402585463667L;

	private static final List<String> FORMATS = Arrays.asList(new String[] { "TriG", "TriX", "N-Quads", "JSON-LD" });
	private String selectedFormat = "TriG";

	private Model<String> inputTextModel = new Model<>("");
	private PropertyModel<String> formatModel = new PropertyModel<String>(this, "selectedFormat");

	public DirectInputPanel(String id, final ValidatorPage page) {
		super(id);

		Form<?> form = new Form<Void>("form") {

			private static final long serialVersionUID = 7483611710394125186L;

			protected void onSubmit() {
				String text = inputTextModel.getObject();
				if (text != null && !text.isEmpty()) {
					page.showResult(ValidatorPage.DIRECT_INPUT_MODE, text, getFormat());
				} else {
					page.clear();
				}
			}

		};

		add(form);
		form.add(new TextArea<String>("nanopubtext", inputTextModel));
		form.add(new RadioChoice<String>("format", formatModel, FORMATS).setSuffix("&nbsp;"));
	}

	public void setNanopub(Nanopub np) {
		setNanopub(np, null);
	}

	public void setNanopub(Nanopub np, RDFFormat format) {
		if (format == null) {
			format = RDFFormat.TRIG;
		}
		setFormat(format);
		try {
			inputTextModel.setObject(NanopubUtils.writeToString(np, format));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void setFormat(RDFFormat format) {
		if (format.equals(RDFFormat.TRIG)) {
			formatModel.setObject("TriG");
		} else if (format.equals(RDFFormat.TRIX)) {
			formatModel.setObject("TriX");
		} else if (format.equals(RDFFormat.NQUADS)) {
			formatModel.setObject("N-Quads");
		} else if (format.equals(RDFFormat.JSONLD)) {
			formatModel.setObject("JSON-LD");
		}
	}

	protected RDFFormat getFormat() {
		if (selectedFormat.equals("TriG")) {
			return RDFFormat.TRIG;
		} else if (selectedFormat.equals("TriX")) {
			return RDFFormat.TRIX;
		} else if (selectedFormat.equals("N-Quads")) {
			return RDFFormat.NQUADS;
		} else if (selectedFormat.equals("JSON-LD")) {
			return RDFFormat.JSONLD;
		}
		return null;
	}

}
