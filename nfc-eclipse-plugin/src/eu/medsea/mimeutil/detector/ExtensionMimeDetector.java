/*
 * Copyright 2007-2009 Medsea Business Solutions S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.medsea.mimeutil.detector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeType;

/**
 * 
 * Fork from mimeutil adding mime-type to ext mapping
 *
 */
public class ExtensionMimeDetector extends MimeDetector {

	private static Logger log = LoggerFactory.getLogger(ExtensionMimeDetector.class);

	// Extension MimeTypes
	private static Map extMimeTypes;
	private static Map<String, Set<String>> mimeTypeExts;

	static {
		ExtensionMimeDetector.initMimeTypes();
	}
	
	public ExtensionMimeDetector() {
	}

	public String getDescription() {
		return "Get the mime types of file extensions";
	}

	/**
	 * Get the mime type of a file using extension mappings. The file path
	 * can be a relative or absolute path or can refer to a completely non-existent file as
	 * only the extension is important here.
	 *
	 * @param file points to a file or directory. May not actually exist
	 * @return collection of the matched mime types.
	 * @throws MimeException if errors occur.
	 */
	public Collection getMimeTypesFile(final File file) throws MimeException {
		return getMimeTypesFileName(file.getName());
	}

	/**
	 * Get the mime type of a URL using extension mappings. Only the extension is important here.
	 *
	 * @param url is a valid URL
	 * @return collection of the matched mime types.
	 * @throws MimeException if errors occur.
	 */
	public Collection getMimeTypesURL(final URL url) throws MimeException {
		return getMimeTypesFileName(url.getPath());
	}

	/**
	 * Get the mime type of a file name using file name extension mappings. The file name path
	 * can be a relative or absolute path or can refer to a completely non-existent file as
	 * only the extension is important here.
	 *
	 * @param fileName points to a file or directory. May not actually exist
	 * @return collection of the matched mime types.
	 * @throws MimeException if errors occur.
	 */
	public Collection getMimeTypesFileName(final String fileName) throws MimeException {

		String fileExtension = getExtension(fileName);
		
		return getMimeTypesExt(fileExtension);
	}

	public Collection<MimeType> getMimeTypesExt(String fileExtension) {
		Collection<MimeType> mimeTypes = new HashSet<MimeType>();
		while(fileExtension.length() != 0) {
			String types = null;
			// First try case sensitive
			types = (String) extMimeTypes.get(fileExtension);
			if (types != null) {
				String [] mimeTypeArray = types.split(",");
				for(int i = 0; i < mimeTypeArray.length; i++) {
					mimeTypes.add(new MimeType(mimeTypeArray[i]));
				}
				return mimeTypes;
			}
			if(mimeTypes.isEmpty()) {
				// Failed to find case insensitive extension so lets try again with
				// lowercase
				types = (String) extMimeTypes.get(fileExtension.toLowerCase());
				if (types != null) {
					String [] mimeTypeArray = types.split(",");
					for(int i = 0; i < mimeTypeArray.length; i++) {
						mimeTypes.add(new MimeType(mimeTypeArray[i]));
					}
					return mimeTypes;
				}
			}
			fileExtension = getExtension(fileExtension);
		}
		return mimeTypes;
	}

	/*
	 * This loads the mime-types.properties files that define mime types based
	 * on file extensions using the following load sequence 1. Loads the
	 * property file from the mime utility jar named
	 * eu.medsea.mime.mime-types.properties. 2. Locates and loads a file named
	 * .mime-types.properties from the users home directory if one exists. 3.
	 * Locates and loads a file named mime-types.properties from the classpath
	 * if one exists 4. locates and loads a file named by the JVM property
	 * mime-mappings i.e. -Dmime-mappings=../my-mime-types.properties
	 */
	private static void initMimeTypes() {
		InputStream is = null;
		extMimeTypes = new Properties();
		try {
			// Load the file extension mappings from the internal property file and
			// then
			// from the custom property files if they can be found
			try {
				// Load the default supplied mime types
				is = MimeType.class.getClassLoader().getResourceAsStream(
				"eu/medsea/mimeutil/mime-types.properties");
				if(is != null) {
					((Properties) extMimeTypes).load(is);
				}
			}catch (Exception e) {
				// log the error but don't throw the exception up the stack
				log.error("Error loading internal mime-types.properties", e);
			}finally {
				is = closeStream(is);
			}

			// Load any .mime-types.properties from the users home directory
			try {
				File f = new File(System.getProperty("user.home")
						+ File.separator + ".mime-types.properties");
				if (f.exists()) {
					is = new FileInputStream(f);
					if (is != null) {
						log.debug("Found a custom .mime-types.properties file in the users home directory.");
						Properties props = new Properties();
						props.load(is);
						if (props.size() > 0) {
							extMimeTypes.putAll(props);
						}
						log.debug("Successfully parsed .mime-types.properties from users home directory.");
					}
				}
			} catch (Exception e) {
				log.error("Failed to parse .magic.mime file from users home directory. File will be ignored.",e);
			} finally {
				is = closeStream(is);
			}

			// Load any classpath provided mime types that either extend or
			// override the default mime type entries. Could also be in jar files.
			// Get an enumeration of all files on the classpath with this name. They could be in jar files as well
			try {
				Enumeration e = MimeType.class.getClassLoader().getResources("mime-types.properties");
				while(e.hasMoreElements()) {
					URL url = (URL)e.nextElement();
					if(log.isDebugEnabled()) {
						log.debug("Found custom mime-types.properties file on the classpath [" + url + "].");
					}
					Properties props = new Properties();
					try {
						is = url.openStream();
						if(is != null) {
							props.load(is);
							if(props.size() > 0) {
								extMimeTypes.putAll(props);
								if(log.isDebugEnabled()) {
									log.debug("Successfully loaded custome mime-type.properties file [" + url + "] from classpath.");
								}
							}
						}
					}catch(Exception ex) {
						log.error("Failed while loading custom mime-type.properties file [" + url + "] from classpath. File will be ignored.");
					}
				}
			}catch(Exception e) {
				log.error("Problem while processing mime-types.properties files(s) from classpath. Files will be ignored.", e);
			} finally {
				is = closeStream(is);
			}

			try {
				// Load any mime extension mappings file defined with the JVM
				// property -Dmime-mappings=../my/custom/mappings.properties
				String fname = System.getProperty("mime-mappings");
				if (fname != null && fname.length() != 0) {
					is = new FileInputStream(fname);
					if (is != null) {
						if (log.isDebugEnabled()) {
							log.debug("Found a custom mime-mappings property defined by the property -Dmime-mappings ["
								+ System.getProperty("mime-mappings") + "].");
						}
						Properties props = new Properties();
						props.load(is);
						if (props.size() > 0) {
							extMimeTypes.putAll(props);
						}
						log.debug("Successfully loaded the mime mappings file from property -Dmime-mappings ["
								+ System.getProperty("mime-mappings") + "].");
					}
				}
			} catch (Exception ex) {
				log.error("Failed to load the mime-mappings file defined by the property -Dmime-mappings ["
						+ System.getProperty("mime-mappings") + "].");
			} finally {
				is = closeStream(is);
			}
		} finally {
			
			// convert ext -> mimetypes to mimetype -> exts
			mimeTypeExts = new HashMap<String, Set<String>>();
			
			for(Object fileExtension : extMimeTypes.keySet()) {

				String types = (String) extMimeTypes.get(fileExtension);

				String [] mimeTypeArray = types.split(",");
				for(int i = 0; i < mimeTypeArray.length; i++) {
					Set<String> exts = (Set<String>) mimeTypeExts.get(mimeTypeArray[i]);
					if(exts == null) {
						exts = new HashSet<String>();
						
						mimeTypeExts.put(mimeTypeArray[i], exts);
					}
					exts.add(fileExtension.toString());
				}
			}
			
			
		}
	}
	
	public static Set<String> getExtensions(String mime) {
		return mimeTypeExts.get(mime);
	}

	/**
	 * This method is required by the abstract MimeDetector class. As we do not support extension mapping of streams
	 * we just throw an {@link UnsupportedOperationException}. This ensures that the getMimeTypes(...) methods ignore this
	 * method. We could also have just returned an empty collection.
	 */
	public Collection getMimeTypesInputStream(InputStream in)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("This MimeDetector does not support detection from streams.");
	}

	/**
	 * This method is required by the abstract MimeDetector class. As we do not support extension mapping of byte arrays
	 * we just throw an {@link UnsupportedOperationException}. This ensures that the getMimeTypes(...) methods ignore this
	 * method. We could also have just returned an empty collection.
	 */
	public Collection getMimeTypesByteArray(byte[] data)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("This MimeDetector does not support detection from byte arrays.");
	}
	
	/**
	 * Get the extension part of a file name defined by the fileName parameter.
	 * There may be no extension or it could be a single part extension such as
	 * .bat or a multi-part extension such as .tar.gz
	 *
	 * @param fileName
	 *            a relative or absolute path to a file
	 * @return the file extension or null if it does not have one.
	 */
	public static String getExtension(final String fileName) {
		if(fileName == null || fileName.length() == 0) {
			return "";
		}
		int index = fileName.indexOf(".");
		return index < 0 ? "" : fileName.substring(index + 1);
	}
}