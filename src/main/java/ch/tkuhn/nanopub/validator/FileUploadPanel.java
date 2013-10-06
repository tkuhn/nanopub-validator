package ch.tkuhn.nanopub.validator;

import java.io.File;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;

public class FileUploadPanel extends Panel {

	private static final long serialVersionUID = -5775371879170421613L;

	private static int fileCount = 0;

	public FileUploadPanel(String id, ValidatorPage page) {
		super(id);
		add(new FileUploadForm("form", page));
	}

	private class FileUploadForm extends Form<Void> {

		private static final long serialVersionUID = -8234908103006659185L;

		FileUploadField fileUploadField;
		ValidatorPage page;

		public FileUploadForm(String name, ValidatorPage page) {
			super(name);
			this.page = page;
			setMultiPart(true);
			add(fileUploadField = new FileUploadField("fileInput"));
			setMaxSize(Bytes.kilobytes(100));
		}

		@Override
		protected void onSubmit() {
			final List<FileUpload> uploads = fileUploadField.getFileUploads();

			if (uploads != null) {
				FileUpload upload = uploads.get(0);
				String ext = "";
				String fileName = upload.getClientFileName();
				int i = fileName.lastIndexOf('.');
				if (i > 0) ext = fileName.substring(i);
				fileName = "nanopub-" + (fileCount++) + ext;
				File file = new File(getUploadFolder(), fileName);
                try {
                	file.createNewFile();
                	upload.writeTo(file);
                	page.showResult(ValidatorPage.FILE_UPLOAD_MODE, file);
                } catch (Exception e) {
                	throw new IllegalStateException("Unable to write file", e);
                }
			}
		}
	}

	private Folder getUploadFolder() {
		return ((ValidatorApplication) Application.get()).getUploadFolder();
	}

}
