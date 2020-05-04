package ch.tkuhn.nanopub.validator;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Folder;

public class ValidatorApplication extends WebApplication {

	private Folder uploadFolder = null;

	@Override
	public Class<? extends WebPage> getHomePage() {
		return ValidatorPage.class;
	}

	public void init() {
		super.init();
		uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
		uploadFolder.mkdirs();
	}

	public Folder getUploadFolder() {
		return uploadFolder;
	}

}
