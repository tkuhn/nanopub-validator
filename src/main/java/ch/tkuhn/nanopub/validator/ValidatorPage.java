package ch.tkuhn.nanopub.validator;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;

import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubImpl;

public class ValidatorPage extends WebPage {

	private static final long serialVersionUID = -8749816277274862810L;

	private static final List<String> FORMATS = Arrays.asList(
			new String[] { "TriG", "TriX", "N-Quads" });
	private String selectedFormat = "TriG";

	private Model<String> inputTextModel = new Model<>("");
	private PropertyModel<String> formatModel = new PropertyModel<String>(this, "selectedFormat");
	private Model<String> resultModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> resultTitleStyleModel = new Model<String>("display:none");

	public ValidatorPage(final PageParameters parameters) {
		super(parameters);

		Form<?> form = new Form<Void>("form") {

			private static final long serialVersionUID = 7483611710394125186L;

			protected void onSubmit() {
				String inputText = inputTextModel.getObject();
				if (inputText == null || inputText.isEmpty()) {
					resultTitleModel.setObject("");
					resultTitleStyleModel.setObject("color:black");
					resultModel.setObject("");
					return;
				}
				Nanopub nanopub;
				try {
					nanopub = new NanopubImpl(inputText, getFormat());
					resultModel.setObject("Congratulations, this is a valid nanopublication!");
					resultTitleModel.setObject("Valid");
					resultTitleStyleModel.setObject("color:green");
				} catch (OpenRDFException ex) {
					resultTitleModel.setObject("Invalid");
					resultTitleStyleModel.setObject("color:red");
					resultModel.setObject(ex.getMessage());
					return;
				} catch (MalformedNanopubException ex) {
					resultTitleModel.setObject("Invalid");
					resultTitleStyleModel.setObject("color:red");
					resultModel.setObject(ex.getMessage());
					return;
				} catch (Exception ex) {
					resultTitleModel.setObject("Unexpected Error");
					resultTitleStyleModel.setObject("color:black");
					resultModel.setObject(ex.getMessage());
					return;
				}
				if (nanopub.getAssertion().isEmpty()) {
					resultTitleModel.setObject("Warning");
					resultTitleStyleModel.setObject("color:orange");
					resultModel.setObject("Empty assertion graph");
				} else if (nanopub.getProvenance().isEmpty()) {
					resultTitleModel.setObject("Warning");
					resultTitleStyleModel.setObject("color:orange");
					resultModel.setObject("Empty provenance graph");
				} else if (nanopub.getPubinfo().isEmpty()) {
					resultTitleModel.setObject("Warning");
					resultTitleStyleModel.setObject("color:orange");
					resultModel.setObject("Empty publication info graph");
				} else if (nanopub.getCreators().isEmpty() && nanopub.getAuthors().isEmpty()) {
					resultTitleModel.setObject("Warning");
					resultTitleStyleModel.setObject("color:orange");
					resultModel.setObject("No creators or authors found");
				} else if (nanopub.getCreationTime() == null) {
					resultTitleModel.setObject("Warning");
					resultTitleStyleModel.setObject("color:orange");
					resultModel.setObject("No creation time found");
				}
			}

		};

		add(form);

		form.add(new TextArea<String>("nanopubtext", inputTextModel));
		form.add(new RadioChoice<String>("format", formatModel, FORMATS));

		Label resultTitle = new Label("resulttitle", resultTitleModel);
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));
		add(resultTitle);
		add(new Label("result", resultModel));
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
