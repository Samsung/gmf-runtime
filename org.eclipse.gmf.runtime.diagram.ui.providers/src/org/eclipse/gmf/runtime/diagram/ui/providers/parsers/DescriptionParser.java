/******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.providers.parsers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
import org.eclipse.gmf.runtime.diagram.core.commands.SetPropertyCommand;
import org.eclipse.gmf.runtime.diagram.core.util.ViewType;
import org.eclipse.gmf.runtime.diagram.ui.internal.properties.Properties;
import org.eclipse.gmf.runtime.notation.DescriptionStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 * Description parser
 * 
 * @author melaasar
 * @since 1.4
 */
public class DescriptionParser implements IParser {
	/** instance of parser */
	static protected IParser instance = null;

	static final String DESC_DEFAULT_STR = StringStatics.BLANK;

	/**
	 * Constructor for DescriptionParser.
	 */
	protected DescriptionParser() {
		super();
	}

	/**
	 * Gets the instance of the parser
	 * @return IParser the single instance of the parser
	 */
	static public IParser getInstance() {
		if (instance == null) {
			instance = new DescriptionParser();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.common.ui.services.parser.IParser#getEditString(org.eclipse.core.runtime.IAdaptable, int)
	 */
	public String getEditString(IAdaptable adapter, int flags) {
		return getPrintString(adapter, flags);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.common.ui.services.parser.IParser#getParseCommand(org.eclipse.core.runtime.IAdaptable, java.lang.String, int)
	 */
	public ICommand getParseCommand(
		IAdaptable adapter,
		String newString,
		int flags) {
        
        final View view = (View) adapter.getAdapter(View.class);
        
        if (view != null) {
            TransactionalEditingDomain domain = TransactionUtil
            .getEditingDomain(view);
            
            if (domain != null) {
                return new SetPropertyCommand(domain, adapter,
                    Properties.ID_DESCRIPTION, ViewType.TEXT, newString);
            }
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.common.ui.services.parser.IParser#getPrintString(org.eclipse.core.runtime.IAdaptable, int)
	 */
	public String getPrintString(IAdaptable adapter, int flags) {
		final View view = (View) adapter.getAdapter(View.class);
		DescriptionStyle style = (DescriptionStyle) view.getStyle(NotationPackage.eINSTANCE.getDescriptionStyle());
		if (style != null) {
			String descString = style.getDescription();
			if (descString == null || descString.length() == 0)
				descString = DESC_DEFAULT_STR;
			return descString;
		}
		return StringStatics.BLANK;
	}

	/**
	 * @see org.eclipse.gmf.runtime.common.ui.services.parser.IParser#isAffectingEvent(Object, int)
	 */
	public boolean isAffectingEvent(Object event, int flags) {
		if (event instanceof Notification) {
			Object feature = ((Notification) event).getFeature();
			if (feature == NotationPackage.eINSTANCE.getDescriptionStyle_Description()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.gmf.runtime.common.ui.services.parser.IParser#isValidEditString(org.eclipse.core.runtime.IAdaptable, java.lang.String)
	 */
	public IParserEditStatus isValidEditString(IAdaptable element, String editString) {
		return ParserEditStatus.EDITABLE_STATUS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.common.ui.services.parser.IParser#getCompletionProcessor(org.eclipse.core.runtime.IAdaptable)
	 */
	public IContentAssistProcessor getCompletionProcessor(IAdaptable element) {
		return null;
	}
}
