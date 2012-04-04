package com.antares.nfc.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * 
 * http://blog.eclipse-tips.com/2008/07/how-to-create-new-file-wizard.html
 * 
 * @author trs
 *
 */

public class NewNdefFileWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;
    private NewNdefFileWizardPage newFileWizardPage;
    private IWorkbench workbench;

    public NewNdefFileWizard() {
        setWindowTitle("New NDEF File");
    } 

    @Override
    public void addPages() {

        newFileWizardPage = new NewNdefFileWizardPage(selection);
        addPage(newFileWizardPage);
    }
    
    @Override
    public boolean performFinish() {
        
        IFile file = newFileWizardPage.createNewFile();
        if (file != null)
            return true;
        else
            return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
}