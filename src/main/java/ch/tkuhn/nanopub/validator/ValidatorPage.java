package ch.tkuhn.nanopub.validator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;

import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.NanopubImpl;

public class ValidatorPage extends WebPage {

	private static final long serialVersionUID = -8749816277274862810L;

	private Model<String> inputTextModel = new Model<>("");
	private Model<String> resultModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> resultTitleStyleModel = new Model<String>("display:none");

	public ValidatorPage(final PageParameters parameters) {
		super(parameters);

		Form<?> form = new Form<Void>("form") {

			private static final long serialVersionUID = 7483611710394125186L;

			protected void onSubmit() {
				try {
					new NanopubImpl(inputTextModel.getObject(), RDFFormat.TRIG);
					resultModel.setObject("Congratulations, this is a valid nanopublication!");
					resultTitleModel.setObject("Valid");
					resultTitleStyleModel.setObject("color:green");
				} catch (OpenRDFException ex) {
					resultTitleModel.setObject("Invalid");
					resultTitleStyleModel.setObject("color:red");
					resultModel.setObject(ex.getMessage());
				} catch (MalformedNanopubException ex) {
					resultTitleModel.setObject("Invalid");
					resultTitleStyleModel.setObject("color:red");
					resultModel.setObject(ex.getMessage());
				} catch (Exception ex) {
					resultTitleModel.setObject("Unexpected Error");
					resultTitleStyleModel.setObject("color:black");
					resultModel.setObject(ex.getMessage());
				}
			}

		};

		add(form);

		form.add(new TextArea<String>("nanopubtext", inputTextModel));

		Label resultTitle = new Label("resulttitle", resultTitleModel);
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));
		add(resultTitle);
		add(new Label("result", resultModel));
    }
}
