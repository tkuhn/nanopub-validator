package ch.tkuhn.nanopub.validator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.trustyuri.TrustyUriUtils;
import net.trustyuri.rdf.CheckNanopub;

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
import org.nanopub.MalformedNanopubException;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;

public class ValidatorPage extends WebPage {

	private static final long serialVersionUID = -8749816277274862810L;

	static final int DIRECT_INPUT_MODE = 1;
	static final int FILE_UPLOAD_MODE = 2;
	static final int URL_MODE = 3;
	static final int SPARQL_ENDPOINT_MODE = 4;

	private Model<String> resultTextModel = new Model<>("");
	private Model<String> trustyUriTextModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> trustyUriTitleModel = new Model<>("");
	private Model<String> resultTitleStyleModel = new Model<>();
	private Model<String> trustyUriTitleStyleModel = new Model<>();

	private DirectInputPanel directInputPanel;
	private Panel fileUploadPanel, urlPanel, sparqlEndpointPanel;

	private WebMarkupContainer trustySection, actionBox;

	private TabbedPanel<ITab> tabbedPanel;

	private Nanopub nanopub;
	private RDFFormat format;

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

		add(tabbedPanel = new TabbedPanel<ITab>("tabs", tabs));

		Label resultTitle = new Label("resulttitle", resultTitleModel);
		add(resultTitle);
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));
		add(new Label("resulttext", resultTextModel));

		Label trustyUriTitle = new Label("trustyurititle", trustyUriTitleModel);
		add(trustyUriTitle);
		trustyUriTitle.add(new AttributeModifier("style", trustyUriTitleStyleModel));
		add(new Label("trustyuritext", trustyUriTextModel).setEscapeModelStrings(false));
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));

		actionBox = new WebMarkupContainer("actions");
		actionBox.add(new AttributeModifier("class", new Model<String>("hidden")));
		add(actionBox);

		actionBox.add(new Convert("trigconvert", RDFFormat.TRIG, this));
		actionBox.add(new Convert("trixconvert", RDFFormat.TRIX, this));
		actionBox.add(new Convert("nqconvert", RDFFormat.NQUADS, this));

		actionBox.add(new ResourceLink<Object>("trigdownload", new DownloadResource(RDFFormat.TRIG, this)));
		actionBox.add(new ResourceLink<Object>("trixdownload", new DownloadResource(RDFFormat.TRIX, this)));
		actionBox.add(new ResourceLink<Object>("nqdownload", new DownloadResource(RDFFormat.NQUADS, this)));

		trustySection = new WebMarkupContainer("trustyaction");
		trustySection.add(new AttributeModifier("class", new Model<String>("hidden")));
		actionBox.add(trustySection);

		trustySection.add(new MakeTrusty("maketrusty", this));
    }

	Nanopub getNanopub() {
		return nanopub;
	}

	void showResult(int mode, Object... objs) {
		Nanopub nanopub = null;
		clear();

		try {
			if (mode == DIRECT_INPUT_MODE) {
				String inputText = (String) objs[0];
				if (inputText == null || inputText.isEmpty()) {
					resultTitleModel.setObject("");
					resultTitleStyleModel.setObject("color:black");
					resultTextModel.setObject("");
					return;
				}
				format = (RDFFormat) objs[1];
				nanopub = new NanopubImpl(inputText, format);
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
			resultTitleModel.setObject("Invalid Nanopublication");
			resultTitleStyleModel.setObject("color:red");
			resultTextModel.setObject(ex.getMessage());
			return;
		} catch (MalformedNanopubException ex) {
			resultTitleModel.setObject("Invalid Nanopublication");
			resultTitleStyleModel.setObject("color:red");
			resultTextModel.setObject(ex.getMessage());
			return;
		} catch (Exception ex) {
			resultTitleModel.setObject("Unexpected Error");
			resultTitleStyleModel.setObject("color:black");
			resultTextModel.setObject(ex.getMessage());
			return;
		}
		setNanopub(nanopub, mode != DIRECT_INPUT_MODE);
	}

	public void setNanopub(Nanopub nanopub, boolean refreshDirectInput) {
		setNanopub(nanopub, null, refreshDirectInput);
	}

	public void setNanopub(Nanopub nanopub, RDFFormat format, boolean refreshDirectInput) {
		this.nanopub = nanopub;
		if (format != null) {
			this.format = format;
		} else {
			format = this.format;
		}
		if (refreshDirectInput) {
			tabbedPanel.setSelectedTab(0);
			directInputPanel.setNanopub(nanopub, format);
		}

		if (!TrustyUriUtils.isPotentialTrustyUri(nanopub.getUri())) {
			trustyUriTitleModel.setObject("No trusty URI");
			trustyUriTitleStyleModel.setObject("color:black");
			trustyUriTextModel.setObject("This nanopublication has no <a href=\"http://arxiv.org/abs/1401.5775\">trusty URI</a>.");
			trustySection.add(new AttributeModifier("class", new Model<String>("visible")));
		} else if (CheckNanopub.isValid(nanopub)) {
			trustyUriTitleModel.setObject("Valid trusty URI");
			trustyUriTitleStyleModel.setObject("color:green");
			trustyUriTextModel.setObject("This nanopublication has a valid <a href=\"http://arxiv.org/abs/1401.5775\">trusty URI</a>.");
		} else {
			trustyUriTitleModel.setObject("Invalid trusty URI");
			trustyUriTitleStyleModel.setObject("color:red");
			trustyUriTextModel.setObject("This nanopublication has an invalid <a href=\"http://arxiv.org/abs/1401.5775\">trusty URI</a>.");
		}
		actionBox.add(new AttributeModifier("class", new Model<String>("visible")));
		if (nanopub.getCreators().isEmpty() && nanopub.getAuthors().isEmpty()) {
			resultTitleModel.setObject("Warning");
			resultTitleStyleModel.setObject("color:orange");
			resultTextModel.setObject("No creators or authors found");
			return;
		} else if (nanopub.getCreationTime() == null) {
			resultTitleModel.setObject("Warning");
			resultTitleStyleModel.setObject("color:orange");
			resultTextModel.setObject("No creation time found");
			return;
		}
		resultTextModel.setObject("Congratulations, this is a valid nanopublication!");
		resultTitleModel.setObject("Valid Nanopublication");
		resultTitleStyleModel.setObject("color:green");
	}

	void clear() {
		nanopub = null;
		resultTitleModel.setObject("");
		resultTextModel.setObject("");
		trustyUriTitleModel.setObject("");
		trustyUriTextModel.setObject("");
		actionBox.add(new AttributeModifier("class", new Model<String>("hidden")));
		trustySection.add(new AttributeModifier("class", new Model<String>("hidden")));
	}

}
