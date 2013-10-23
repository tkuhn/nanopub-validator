package ch.tkuhn.nanopub.validator;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.IResource;
import org.openrdf.rio.RDFFormat;

import ch.tkuhn.hashuri.rdf.TransformNanopub;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubUtils;

public class HashDownloadResource implements IResource {

	private static final long serialVersionUID = -4558302923207618223L;

	private RDFFormat format;
	private ValidatorPage mainPage;
	
	public HashDownloadResource(RDFFormat format, ValidatorPage mainPage) {
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
			Nanopub np = mainPage.getNanopub();
			Nanopub hashNp = TransformNanopub.transform(np, np.getUri().toString());
			NanopubUtils.writeToStream(hashNp, resp.getOutputStream(), format);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		resp.close();
	}

}
