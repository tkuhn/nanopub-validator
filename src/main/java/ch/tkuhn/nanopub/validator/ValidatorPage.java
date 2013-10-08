package ch.tkuhn.nanopub.validator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;

import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubImpl;

public class ValidatorPage extends WebPage {

	private static final long serialVersionUID = -8749816277274862810L;

	static final int DIRECT_INPUT_MODE = 1;
	static final int FILE_UPLOAD_MODE = 2;
	static final int URL_MODE = 3;
	static final int SPARQL_ENDPOINT_MODE = 4;

	private Model<String> resultModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> resultTitleStyleModel = new Model<String>("display:none");

	private Panel directInputPanel, fileUploadPanel, urlPanel, sparqlEndpointPanel;

	private WebMarkupContainer downloadSection;

	private Nanopub nanopub;

	public ValidatorPage(final PageParameters parameters) {
		super(parameters);

		List<ITab> tabs = new ArrayList<>();
		tabs.add(new AbstractTab(new Model<String>("Direct Input")) {

			private static final long serialVersionUID = -7872913277026344515L;

			public Panel getPanel(String panelId) {
				if (directInputPanel == null) {
					directInputPanel = new DirectInputPanel(panelId, ValidatorPage.this);
				}
				return directInputPanel;
			}

		});

		tabs.add(new AbstractTab(new Model<String>("Upload File")) {

			private static final long serialVersionUID = -5236526957334243565L;

			public Panel getPanel(String panelId) {
				if (fileUploadPanel == null) {
					fileUploadPanel = new FileUploadPanel(panelId, ValidatorPage.this);
				}
				return fileUploadPanel;
			}

		});

		tabs.add(new AbstractTab(new Model<String>("URL")) {

			private static final long serialVersionUID = 2645031269099631888L;

			public Panel getPanel(String panelId) {
				if (urlPanel == null) {
					urlPanel = new UrlPanel(panelId, ValidatorPage.this);
				}
				return urlPanel;
			}

		});

		tabs.add(new AbstractTab(new Model<String>("SPARQL Endpoint")) {

			private static final long serialVersionUID = 5357887346654745284L;

			public Panel getPanel(String panelId) {
				if (sparqlEndpointPanel == null) {
					sparqlEndpointPanel = new SparqlEndpointPanel(panelId, ValidatorPage.this);
				}
				return sparqlEndpointPanel;
			}

		});

		add(new TabbedPanel<ITab>("tabs", tabs));

		Label resultTitle = new Label("resulttitle", resultTitleModel);
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));
		add(resultTitle);
		add(new Label("result", resultModel));

		downloadSection = new WebMarkupContainer("download");
		downloadSection.add(new AttributeModifier("class", new Model<String>("hidden")));
		add(downloadSection);

		downloadSection.add(new ResourceLink<Object>("trigdownload", new DownloadResource(RDFFormat.TRIG, this)));
		downloadSection.add(new ResourceLink<Object>("trixdownload", new DownloadResource(RDFFormat.TRIX, this)));
		downloadSection.add(new ResourceLink<Object>("nqdownload", new DownloadResource(RDFFormat.NQUADS, this)));
		downloadSection.add(new ResourceLink<Object>("rdfjsondownload", new DownloadResource(RDFFormat.RDFJSON, this)));
    }

	Nanopub getNanopub() {
		return nanopub;
	}

	void showResult(int mode, Object... objs) {
		nanopub = null;
		clear();

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
			} else if (mode == FILE_UPLOAD_MODE) {
				File file = (File) objs[0];
				nanopub = new NanopubImpl(file);
			} else if (mode == URL_MODE) {
				URL url = new URL((String) objs[0]);
				nanopub = new NanopubImpl(url);
			} else if (mode == SPARQL_ENDPOINT_MODE) {
				SPARQLRepository sr = new SPARQLRepository((String) objs[0]);
				URI nanopubUri = new URIImpl((String) objs[1]);
				sr.initialize();
				nanopub = new NanopubImpl(sr, nanopubUri);
				sr.shutDown();
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
		downloadSection.add(new AttributeModifier("class", new Model<String>("visible")));
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

	void clear() {
		downloadSection.add(new AttributeModifier("class", new Model<String>("hidden")));
		resultTitleModel.setObject("");
		resultModel.setObject("");
	}

}
