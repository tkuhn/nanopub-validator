package ch.tkuhn.nanopub.validator;

import net.trustyuri.TrustyUriUtils;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.nanopub.extra.server.PublishNanopub;

public class PublishAction extends AjaxLink<Object> {

	private static final long serialVersionUID = -5645804251324015992L;

	private static final String message = "Do you really want to publish this nanopublication " +
			"in the nanopub server network? This cannot be undone.";

	private ValidatorPage mainPage;
	
	public PublishAction(String id, ValidatorPage mainPage) {
		super(id);
		this.mainPage = mainPage;
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		AjaxCallListener ajaxCallListener = new AjaxCallListener();
		ajaxCallListener.onPrecondition("return confirm('" + message + "');");
		attributes.getAjaxCallListeners().add(ajaxCallListener);
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		try {
			PublishNanopub p = new PublishNanopub();
			p.publishNanopub(mainPage.getNanopub());
			String ac = TrustyUriUtils.getArtifactCode(mainPage.getNanopub().getUri().toString());
			mainPage.setMessageText("Successfully published at: " + p.getPublishedNanopubUrl());
			mainPage.showTrustyUri(ac);
		} catch (Exception ex) {
			mainPage.setMessageText("Publication failed.");
			ex.printStackTrace();
		}
		setResponsePage(mainPage);
	}

}
