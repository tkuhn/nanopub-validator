package ch.tkuhn.nanopub.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.nanopub.MalformedNanopubException;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.nanopub.NanopubPattern;
import org.nanopub.NanopubPatterns;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.trusty.TrustyNanopubUtils;

import net.trustyuri.TrustyUriUtils;

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
	static final int ERROR = 100;

	
	private static ValueFactory vf = SimpleValueFactory.getInstance();

	private Model<String> messageTextModel = new Model<>("");
	private Model<String> resultTextModel = new Model<>("");
	private Model<String> publishedTextModel = new Model<>("");
	private Model<String> messageTitleModel = new Model<>("");
	private Model<String> resultTitleModel = new Model<>("");
	private Model<String> publishedTitleModel = new Model<>("");
	private Model<String> messageTitleStyleModel = new Model<>();
	private Model<String> resultTitleStyleModel = new Model<>();
	private Model<String> publishedTitleStyleModel = new Model<>();

	private DirectInputPanel directInputPanel;
	private NanopubServerPanel nanopubServerPanel;
	private Panel examplePanel, fileUploadPanel, urlPanel, sparqlEndpointPanel;
	private Label messageTextLabel;

	private WebMarkupContainer trustySection, publishSection, resultBox;

	private TabbedPanel<ITab> tabbedPanel;

	private Nanopub nanopub;
	private RDFFormat format = RDFFormat.TRIG;

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

		Label messageTitle = new Label("messagetitle", messageTitleModel);
		add(messageTitle);
		messageTitle.add(new AttributeModifier("style", messageTitleStyleModel));
		messageTextLabel = new Label("messagetext", messageTextModel);
		add(messageTextLabel);

		resultBox = new WebMarkupContainer("result");
		resultBox.add(new AttributeModifier("class", new Model<String>("hidden")));
		add(resultBox);

		Label resultTitle = new Label("resulttitle", resultTitleModel);
		resultBox.add(resultTitle);
		resultTitle.add(new AttributeModifier("style", resultTitleStyleModel));
		resultBox.add(new Label("resulttext", resultTextModel));

		Label publishedTitle = new Label("publishedtitle", publishedTitleModel);
		resultBox.add(publishedTitle);
		publishedTitle.add(new AttributeModifier("style", publishedTitleStyleModel));
		resultBox.add(new Label("publishedtext", publishedTextModel).setEscapeModelStrings(false));

		resultBox.add(new ConvertAction("trigconvert", RDFFormat.TRIG, this));
		resultBox.add(new ConvertAction("trixconvert", RDFFormat.TRIX, this));
		resultBox.add(new ConvertAction("nqconvert", RDFFormat.NQUADS, this));
		resultBox.add(new ConvertAction("jsonldconvert", RDFFormat.JSONLD, this));

		resultBox.add(new ResourceLink<Object>("download", new DownloadResource(this)));

		trustySection = new WebMarkupContainer("trustyaction");
		trustySection.add(new AttributeModifier("class", new Model<String>("hidden")));
		resultBox.add(trustySection);

		trustySection.add(new MakeTrustyAction("maketrusty", this));

		publishSection = new WebMarkupContainer("publishaction");
		publishSection.add(new AttributeModifier("class", new Model<String>("hidden")));
		resultBox.add(publishSection);

		publishSection.add(new PublishAction("publish", this));

		ListView<NanopubPattern> patternList = new ListView<NanopubPattern>("patternlist", NanopubPatterns.getPatterns()) {

			private static final long serialVersionUID = -7597306960671602403L;

			protected void populateItem(ListItem<NanopubPattern> item) {
				NanopubPattern pattern = item.getModelObject();
				if (nanopub == null) {
					item.add(new AttributeModifier("class", new Model<String>("hidden")));
			        item.add(new Label("patternvalid", "(not used)"));
			        item.add(new ExternalLink("patternlink", "#", pattern.getName()));
			        item.add(new Label("patterntext", ""));
					return;
				}
				String url = "#";
				try {
					url = pattern.getPatternInfoUrl().toString();
				} catch (MalformedURLException ex) {}
		        item.add(new ExternalLink("patternlink", url, pattern.getName()));
				Model<String> styleModel = new Model<>();
				String v;
				if (!pattern.appliesTo(nanopub)) {
					v = "(does not apply)";
					styleModel.setObject("color:gray");
				} else if (item.getModelObject().isCorrectlyUsedBy(nanopub)) {
					v = "(valid)";
					styleModel.setObject("color:green");
				} else {
					v = "(invalid)";
					styleModel.setObject("color:red");
				}
				Label validLabel = new Label("patternvalid", v);
				validLabel.add(new AttributeModifier("style", styleModel));
				item.add(validLabel);
				if (pattern.appliesTo(nanopub)) {
					String description = pattern.getDescriptionFor(nanopub);
					if (description == null) description = "";
					item.add(new Label("patterntext", description));
				} else {
					item.add(new Label("patterntext", ""));
				}
		    }

		};
		//patternList.setReuseItems(true);
		resultBox.add(patternList);
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
				setMessage("Nanopublication", "Loaded from direct input:");
			} else if (mode == EXAMPLE_MODE) {
				InputStream in = getClass().getResourceAsStream(objs[0].toString());
				format = RDFFormat.TRIG;
				nanopub = new NanopubImpl(in, format);
				setMessage("Nanopublication", "Example loaded:");
			} else if (mode == FILE_UPLOAD_MODE) {
				File file = (File) objs[0];
				nanopub = new NanopubImpl(file);
				setMessage("Nanopublication", "Loaded from file " + file.getName() + ":");
			} else if (mode == URL_MODE) {
				URL url = new URL((String) objs[0]);
				nanopub = new NanopubImpl(url);
				setMessage("Nanopublication", "Loaded from URL: <a href=\"" + url + "\">" + url + "</a>", false);
			} else if (mode == SPARQL_ENDPOINT_MODE) {
				String url = (String) objs[0];
				SPARQLRepository sr = new SPARQLRepository(url);
				IRI nanopubUri = vf.createIRI((String) objs[1]);
				sr.initialize();
				nanopub = new NanopubImpl(sr, nanopubUri);
				sr.shutDown();
				setMessage("Nanopublication", "Loaded from SPARQL endpoint: <a href=\"" + url + "\">" + url + "</a>", false);
			} else if (mode == NANOPUB_SERVER_MODE) {
				String uriOrArtifactCode = (String) objs[0];
				nanopub = GetNanopub.get(uriOrArtifactCode);
				if (nanopub == null) {
					throw new IOException("Couldn't find nanopublication");
				}
				setMessage("Nanopublication", "Loaded from nanopub server:");
			}
		} catch (MalformedNanopubException ex) {
			setMessage("Invalid Nanopublication", "color:red", ex.getMessage());
			return;
		} catch (MalformedURLException ex) {
			setMessage("Malformed URL", "color:red", ex.getMessage());
			return;
		} catch (IOException ex) {
			setMessage("Failed to Read Nanopublication", "color:red", ex.getClass().getName() + ": " + ex.getMessage());
			return;
		} catch (IllegalArgumentException ex) {
			setMessage("Illegal Argument", "color:red", ex.getMessage());
			return;
		} catch (Exception ex) {
			setMessage("Unexpected Error", "color:red", ex.getClass().getName() + ": " + ex.getMessage());
			return;
		}
		setNanopub(nanopub, mode);
	}

	public void setMessage(String title, String text) {
		setMessage(title, "", text, true);
	}

	public void setMessage(String title, String text, boolean escape) {
		setMessage(title, "", text, escape);
	}

	public void setMessage(String title, String titleStyle, String text) {
		setMessage(title, titleStyle, text, true);
	}

	public void setMessage(String title, String titleStyle, String text, boolean escape) {
		messageTitleModel.setObject(title);
		messageTitleStyleModel.setObject(titleStyle);
		messageTextModel.setObject(text);
		messageTextLabel.setEscapeModelStrings(escape);
	}

	public void showNanopub(Nanopub nanopub, int mode) {
		showNanopub(nanopub, null, mode);
	}

	public void showNanopub(Nanopub nanopub, RDFFormat format, int mode) {
		clear();
		setNanopub(nanopub, format, mode);
	}

	private void setNanopub(Nanopub nanopub, int mode) {
		setNanopub(nanopub, null, mode);
	}

	private void setNanopub(Nanopub nanopub, RDFFormat format, int mode) {
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

		if (!TrustyUriUtils.isPotentialTrustyUri(nanopub.getUri())) {
			publishedTitleModel.setObject("Not published");
			publishedTitleStyleModel.setObject("color:black");
			publishedTextModel.setObject("Only nanopublications with a valid trusty URI can be published on nanopub servers.");
			trustySection.add(new AttributeModifier("class", new Model<String>("visible")));
		} else if (TrustyNanopubUtils.isValidTrustyNanopub(nanopub)) {
			try {
				String u = "https://np.knowledgepixels.com/" + TrustyUriUtils.getArtifactCode(nanopub.getUri().toString());
				new NanopubImpl(new URL(u));
				publishedTitleModel.setObject("Published");
				publishedTitleStyleModel.setObject("color:green");
				publishedTextModel.setObject("On nanopub server: <a href=\"" + u + "\">" + u + "</a>");
			} catch (IOException ex) {
				publishedTitleModel.setObject("Not published");
				publishedTitleStyleModel.setObject("color:black");
				String u = "https://np.knowledgepixels.com/";
				publishedTextModel.setObject("This nanopublication is not published on a nanopub server (at least not on <a href=\"" + u + "\">" + u + "</a>).");
				publishSection.add(new AttributeModifier("class", new Model<String>("visible")));
			} catch (Exception ex) {}
		} else {
			publishedTitleModel.setObject("Not published");
			publishedTitleStyleModel.setObject("color:black");
			publishedTextModel.setObject("Only nanopublications with a valid trusty URI can be published on nanopub servers.");
		}
		resultBox.add(new AttributeModifier("class", new Model<String>("visible")));
		resultTextModel.setObject("Congratulations, this is a valid nanopublication!");
		resultTitleModel.setObject("Valid Nanopublication");
		resultTitleStyleModel.setObject("color:green");
	}

	public void refresh() {
		showNanopub(nanopub, format, -1);
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
		publishedTitleModel.setObject("");
		publishedTextModel.setObject("");
		resultBox.add(new AttributeModifier("class", new Model<String>("hidden")));
		trustySection.add(new AttributeModifier("class", new Model<String>("hidden")));
		publishSection.add(new AttributeModifier("class", new Model<String>("hidden")));
		messageTitleStyleModel.setObject("");
		resultTitleStyleModel.setObject("");
		publishedTitleStyleModel.setObject("");
	}

}
