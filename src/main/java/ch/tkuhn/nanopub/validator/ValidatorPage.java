package ch.tkuhn.nanopub.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.trustyuri.TrustyUriUtils;

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
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.trusty.TrustyNanopubUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;

public class ValidatorPage extends WebPage {

	private static final long serialVersionUID = -8749816277274862810L;

	static final int DIRECT_INPUT_MODE = 1;
	static final int EXAMPLE_MODE = 2;
	static final int FILE_UPLOAD_MODE = 3;
	static final int URL_MODE = 4;
	static final int SPARQL_ENDPOINT_MODE = 5;
	static final int NANOPUB_SERVER_MODE = 6;

	static final int MADE_TRUSTY = 11;
	static final int CONVERTED = 12;


	private Model<String> messageTextModel = new Model<>("");
	private Model<String> resultTextModel = new Model<>("");
	private Model<String> trustyUriTextModel = new Model<>("");
	private Model<String> messageTitleModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> trustyUriTitleModel = new Model<>("");
	private Model<String> resultTitleStyleModel = new Model<>();
	private Model<String> trustyUriTitleStyleModel = new Model<>();

	private DirectInputPanel directInputPanel;
	private NanopubServerPanel nanopubServerPanel;
	private Panel examplePanel, fileUploadPanel, urlPanel, sparqlEndpointPanel;

	private WebMarkupContainer trustySection, publishSection, actionBox;

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

		tabs.add(new AbstractTab(new Model<String>("Load Example")) {

			private static final long serialVersionUID = -5236526957334243565L;

			public Panel getPanel(String panelId) {
				if (examplePanel == null) {
					examplePanel = new ExamplePanel(panelId, ValidatorPage.this);
				}
				return examplePanel;
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

		tabs.add(new AbstractTab(new Model<String>("from URL")) {

			private static final long serialVersionUID = 2645031269099631888L;

			public Panel getPanel(String panelId) {
				if (urlPanel == null) {
					urlPanel = new UrlPanel(panelId, ValidatorPage.this);
				}
				return urlPanel;
			}

		});

		tabs.add(new AbstractTab(new Model<String>("from SPARQL Endpoint")) {

			private static final long serialVersionUID = 5357887346654745284L;

			public Panel getPanel(String panelId) {
				if (sparqlEndpointPanel == null) {
					sparqlEndpointPanel = new SparqlEndpointPanel(panelId, ValidatorPage.this);
				}
				return sparqlEndpointPanel;
			}

		});

		tabs.add(new AbstractTab(new Model<String>("from Nanopub Server")) {

			private static final long serialVersionUID = -976569935347713558L;

			public Panel getPanel(String panelId) {
				if (nanopubServerPanel == null) {
					nanopubServerPanel = new NanopubServerPanel(panelId, ValidatorPage.this);
				}
				return nanopubServerPanel;
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

		add(new Label("messagetitle", messageTitleModel));
		add(new Label("messagetext", messageTextModel));

		actionBox = new WebMarkupContainer("actions");
		actionBox.add(new AttributeModifier("class", new Model<String>("hidden")));
		add(actionBox);

		actionBox.add(new ConvertAction("trigconvert", RDFFormat.TRIG, this));
		actionBox.add(new ConvertAction("trixconvert", RDFFormat.TRIX, this));
		actionBox.add(new ConvertAction("nqconvert", RDFFormat.NQUADS, this));

		actionBox.add(new ResourceLink<Object>("download", new DownloadResource(format, this)));

		trustySection = new WebMarkupContainer("trustyaction");
		trustySection.add(new AttributeModifier("class", new Model<String>("hidden")));
		actionBox.add(trustySection);

		trustySection.add(new MakeTrustyAction("maketrusty", this));

		publishSection = new WebMarkupContainer("publishaction");
		publishSection.add(new AttributeModifier("class", new Model<String>("hidden")));
		actionBox.add(publishSection);

		publishSection.add(new PublishAction("publish", this));
    }

	Nanopub getNanopub() {
		return nanopub;
	}

	RDFFormat getFormat() {
		return format;
	}

	void showResult(int mode, Object... objs) {
		Nanopub nanopub = null;
		clear();
		String messageText = null;

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
				messageText = "Loaded from direct input.";
			} else if (mode == EXAMPLE_MODE) {
				InputStream in = getClass().getResourceAsStream(objs[0].toString());
				nanopub = new NanopubImpl(in, RDFFormat.TRIG);
				messageText = "Example loaded.";
			} else if (mode == FILE_UPLOAD_MODE) {
				File file = (File) objs[0];
				nanopub = new NanopubImpl(file);
				messageText = "Loaded from file " + file.getName() + ".";
			} else if (mode == URL_MODE) {
				URL url = new URL((String) objs[0]);
				nanopub = new NanopubImpl(url);
				messageText = "Loaded from URL " + url + ".";
			} else if (mode == SPARQL_ENDPOINT_MODE) {
				String sparqlEndpointUrl = (String) objs[0];
				SPARQLRepository sr = new SPARQLRepository(sparqlEndpointUrl);
				URI nanopubUri = new URIImpl((String) objs[1]);
				sr.initialize();
				nanopub = new NanopubImpl(sr, nanopubUri);
				sr.shutDown();
				messageText = "Loaded from SPARQL endpoint " + sparqlEndpointUrl + ".";
			} else if (mode == NANOPUB_SERVER_MODE) {
				String uriOrArtifactCode = (String) objs[0];
				nanopub = GetNanopub.get(uriOrArtifactCode);
				if (nanopub == null) {
					throw new IOException("Couldn't find nanopublication");
				}
				messageText = "Loaded from nanopub server.";
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
		} catch (MalformedURLException ex) {
			resultTitleModel.setObject("Malformed URL");
			resultTitleStyleModel.setObject("color:red");
			resultTextModel.setObject(ex.getMessage());
			return;
		} catch (IOException ex) {
			resultTitleModel.setObject("Failed to Read Nanopublication");
			resultTitleStyleModel.setObject("color:red");
			resultTextModel.setObject(ex.getClass().getName() + ": " + ex.getMessage());
			return;
		} catch (IllegalArgumentException ex) {
			resultTitleModel.setObject("Illegal Argument");
			resultTitleStyleModel.setObject("color:red");
			resultTextModel.setObject(ex.getMessage());
			return;
		} catch (Exception ex) {
			resultTitleModel.setObject("Unexpected Error");
			resultTitleStyleModel.setObject("color:black");
			resultTextModel.setObject(ex.getClass().getName() + ": " + ex.getMessage());
			return;
		}
		setNanopub(nanopub, mode);
		setMessageText(messageText);
	}

	public void setMessageText(String messageText) {
		messageTextModel.setObject(messageText);
	}

	public void setNanopub(Nanopub nanopub, int mode) {
		setNanopub(nanopub, null, mode);
	}

	public void setNanopub(Nanopub nanopub, RDFFormat format, int mode) {
		clear();
		this.nanopub = nanopub;
		if (format != null) {
			this.format = format;
		} else {
			format = this.format;
		}
		if (mode != DIRECT_INPUT_MODE) {
			tabbedPanel.setSelectedTab(0);
			directInputPanel.setNanopub(nanopub, format);
		}

		messageTitleModel.setObject("Nanopublication " + nanopub.getUri());
		if (!TrustyUriUtils.isPotentialTrustyUri(nanopub.getUri())) {
			trustyUriTitleModel.setObject("No trusty URI");
			trustyUriTitleStyleModel.setObject("color:black");
			trustyUriTextModel.setObject("This nanopublication has no <a href=\"http://arxiv.org/abs/1401.5775\">trusty URI</a>.");
			trustySection.add(new AttributeModifier("class", new Model<String>("visible")));
		} else if (TrustyNanopubUtils.isValidTrustyNanopub(nanopub)) {
			trustyUriTitleModel.setObject("Valid trusty URI");
			trustyUriTitleStyleModel.setObject("color:green");
			trustyUriTextModel.setObject("This nanopublication has a valid <a href=\"http://arxiv.org/abs/1401.5775\">trusty URI</a>.");
			publishSection.add(new AttributeModifier("class", new Model<String>("visible")));
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

	public void showTrustyUri(String trustyUriOrArtifactCode) {
		tabbedPanel.setSelectedTab(5);
		nanopubServerPanel.setText(trustyUriOrArtifactCode);
	}

	void clear() {
		nanopub = null;
		messageTitleModel.setObject("");
		messageTextModel.setObject("");
		resultTitleModel.setObject("");
		resultTextModel.setObject("");
		trustyUriTitleModel.setObject("");
		trustyUriTextModel.setObject("");
		actionBox.add(new AttributeModifier("class", new Model<String>("hidden")));
		trustySection.add(new AttributeModifier("class", new Model<String>("hidden")));
		publishSection.add(new AttributeModifier("class", new Model<String>("hidden")));
	}

}
