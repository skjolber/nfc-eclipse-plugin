package com.antares.nfc.plugin;

import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewNdefFileWizardPage extends WizardNewFileCreationPage {

    public NewNdefFileWizardPage(IStructuredSelection selection) {
        super("NewNdefFileWizardPage", selection);
        setTitle("NDEF File");
        setDescription("Creates a new NDEF file");
        setFileExtension("ndef");
    }

    @Override
    protected InputStream getInitialContents() {
        return null;
    }
}