package com.antares.nfc.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.antares.nfc.plugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Writes a message to plugin log
	 * @param message
	 */
	public static void info(String message) {
		log(IStatus.INFO, message);
	}

	public static void warn(String message) {
		log(IStatus.WARNING, message);
	}

	public static void error(String message) {
		log(IStatus.ERROR, message);
	}

	public static void error(String message, Throwable e) {
		log(IStatus.ERROR, message, e);
	}

	public static void log(int severity, String message) {
		log(new Status(severity, PLUGIN_ID, IStatus.OK, message, null));
	}
	public static void log(int severity, String message, Throwable exception) {
		log(new Status(severity, PLUGIN_ID, IStatus.OK, message, exception));
	}

}
