package ch.tkuhn.nanopub.validator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;

import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubImpl;

public class ValidatorPage extends WebPage {

	private static final long serialVersionUID = -8749816277274862810L;

	static final int DIRECT_INPUT_MODE = 1;

	private Model<String> resultModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> resultTitleStyleModel = new Model<String>("display:none");

	public ValidatorPage(final PageParameters parameters) {
		super(parameters);

		add(new DirectInputPanel("panel", this));

		Label resultTitle = new Label("resulttitle", resultTitleModel);
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));
		add(resultTitle);
		add(new Label("result", resultModel));
    }

	void showResult(int mode, Object... objs) {
		Nanopub nanopub = null;
		try {
			if (mode == DIRECT_INPUT_MODE) {
				String inputText = (String) objs[0];
				if (inputText == null || inputText.isEmpty()) {
					resultTitleModel.setObject("");
					resultTitleStyleModel.setObject("color:black");
					resultModel.setObject("");
					return;
				}
				nanopub = new NanopubImpl(inputText, (RDFFormat) objs[1]);
			}
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
			return;
		} else if (nanopub.getProvenance().isEmpty()) {
			resultTitleModel.setObject("Warning");
			resultTitleStyleModel.setObject("color:orange");
			resultModel.setObject("Empty provenance graph");
			return;
		} else if (nanopub.getPubinfo().isEmpty()) {
			resultTitleModel.setObject("Warning");
			resultTitleStyleModel.setObject("color:orange");
			resultModel.setObject("Empty publication info graph");
			return;
		} else if (nanopub.getCreators().isEmpty() && nanopub.getAuthors().isEmpty()) {
			resultTitleModel.setObject("Warning");
			resultTitleStyleModel.setObject("color:orange");
			resultModel.setObject("No creators or authors found");
			return;
		} else if (nanopub.getCreationTime() == null) {
			resultTitleModel.setObject("Warning");
			resultTitleStyleModel.setObject("color:orange");
			resultModel.setObject("No creation time found");
			return;
		}
		resultModel.setObject("Congratulations, this is a valid nanopublication!");
		resultTitleModel.setObject("Valid");
		resultTitleStyleModel.setObject("color:green");
	}

}
