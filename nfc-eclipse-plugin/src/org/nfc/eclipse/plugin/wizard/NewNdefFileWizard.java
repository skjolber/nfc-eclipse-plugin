/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/

package org.nfc.eclipse.plugin.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * 
 * http://blog.eclipse-tips.com/2008/07/how-to-create-new-file-wizard.html
 * 
 * @author trs
 *
 */

public class NewNdefFileWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;
    private IWorkbench workbench;
    private NewNdefFileWizardPage newFileWizardPage;

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
        if (file != null) {
            try {
                IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file);
            } catch (PartInitException e) {
            }
         
            return true;
        } else {
            return false;
        }
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
    	this.workbench = workbench;
        this.selection = selection;
    }
}