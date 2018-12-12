/******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.emf.core.internal.util;

/**
 * Various internal constants.
 * 
 * @author rafikj
 */
public class EMFCoreConstants {

	public final static String EMPTY_STRING = ""; //$NON-NLS-1$

	public final static char META_CLASS_BEGIN = '<';

	public final static char META_CLASS_END = '>';

	public final static char ID_SEPARATOR = '.';

	public final static char PATH_SEPARATOR = '/';

	public final static char REF_SEPARATOR = ',';

	public final static char FRAGMENT_SEPARATOR = '?';

	public final static char SCHEME_SEPARATOR = ':';

	public final static String QUALIFIED_NAME_SEPARATOR = "::"; //$NON-NLS-1$

	public final static String XMI_ENCODING = "UTF-8"; //$NON-NLS-1$

	public final static String PATH_MAP_SCHEME = "pathmap"; //$NON-NLS-1$

	public final static String PLATFORM_SCHEME = "platform"; //$NON-NLS-1$

	public final static String RESOURCE = "resource"; //$NON-NLS-1$

	public final static String PLUGIN = "plugin"; //$NON-NLS-1$

	public final static String FILE_SCHEME = "file"; //$NON-NLS-1$

	public final static Integer OUTPUT_BUFFER_SIZE = new Integer(256 * 1024);

}