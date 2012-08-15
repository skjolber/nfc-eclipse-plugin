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
package eu.medsea.mimeutil;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * This class represents a simple MimeType object. A mime type is made up of
 * two parts <code>&lt;media type&gt;/&lt;sub type&gt;</code>.
 * The media type can be something like <code>application</code> or <code>text</code> and
 * the the sub type can be something like <code>xml</code> or <code>plain</code>.
 *
 * Both the media type and sub type can also be the wild card <code>*</code> such as
 * <code>*&#47;*</code> and <code>text&#47;*</code>. Note, if the media type is the wild card
 * then the sub type must also be a wild card.
 *
 * @author Steven McArdle
 *
 */
public class MimeType implements Comparable<MimeType>, Serializable {

	private static final long serialVersionUID = -1324243127744494894L;

	private static final Pattern mimeSplitter = Pattern.compile ("[/;]++" );

	protected String mediaType = "*";
	protected String subType = "*";

	/**
	 * Construct a MimeType from another MimeType instance
	 * @param mimeType
	 */
	public MimeType(final MimeType mimeType) {
		this.mediaType = mimeType.mediaType;
		this.subType = mimeType.subType;
	}

	/**
	 * Construct a mime type from a String such as <code>text/plain</code>.
	 * It tries to ensure that the mime type pattern passed in is correctly
	 * formatted.
	 *
	 * @param mimeType
	 * @throws MimeException
	 */
	public MimeType(final String mimeType) throws MimeException {
		if(mimeType == null || mimeType.trim().length() == 0){
			throw new MimeException("Invalid MimeType [" + mimeType + "]");
		}
		String [] parts = mimeSplitter.split(mimeType.trim());

		if(parts.length > 0) {
			// Treat as the mediaType
			mediaType = getValidMediaType(parts[0]);
		} if(parts.length > 1) {
			subType = getValidSubType(parts[1]);
		}
	}

	/**
	 * Get the media type part of the mime type.
	 * @return media type
	 */
	public String getMediaType() {
		return mediaType;
	}

	/**
	 * Get the sub type of the mime type
	 * @return sub type
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * Overrides the toString method of <code>java.lang.Object</code>.
	 * @return String representation i.e. <code>&lt;media type&gt;/&lt;sub type&gt;.
	 * @see Object#toString()
	 */
	public String toString() {
		return mediaType + "/" + subType;
	}

	/*
	 * Check the media type at least looks valid.
	 * TODO: Enforce more rigorous checking of valid media types.
	 */
	private String getValidMediaType(final String mediaType) {
		if(mediaType == null  || mediaType.trim().length() == 0) {
			return "*";
		}
		return mediaType;
	}

	/*
	 * Check the sub type at least looks valid.
	 * TODO: Enforce more rigorous checking of valid sub types.
	 */
	private String getValidSubType(final String subType) {
		if(subType == null || subType.trim().length() == 0 || "*".equals(mediaType)) {
			// If the mediaType is a wild card then the sub type must also be a wild card
			return "*";
		}
		return subType;
	}

	/**
	 * Allows us to use MimeType(s) in Sortable Set's such as the TreeSet.
	 */
	public int compareTo(MimeType mimeType) {
		return toString().compareTo(mimeType.toString());
	}
	
}
