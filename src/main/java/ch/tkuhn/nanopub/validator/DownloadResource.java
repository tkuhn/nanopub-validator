package ch.tkuhn.nanopub.validator;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.IResource;
import org.nanopub.NanopubUtils;
import org.openrdf.rio.RDFFormat;

public class DownloadResource implements IResource {

	private static final long serialVersionUID = -9045222635249458638L;

	private RDFFormat format;
	private ValidatorPage mainPage;
	
	public DownloadResource(RDFFormat format, ValidatorPage mainPage) {
		this.format = format;
		this.mainPage = mainPage;
	}

	@Override
	public void respond(Attributes attributes) {
		if (mainPage.getNanopub() == null) return;
		WebResponse resp = (WebResponse) attributes.getResponse();
		resp.setContentType(format.getMIMETypes().get(0));
		resp.setAttachmentHeader("nanopub." + format.getDefaultFileExtension());
		try {
			NanopubUtils.writeToStream(mainPage.getNanopub(), resp.getOutputStream(), format);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		resp.close();
	}

}
