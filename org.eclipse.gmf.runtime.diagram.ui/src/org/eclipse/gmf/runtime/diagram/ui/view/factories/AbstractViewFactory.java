/******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.view.factories;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.core.view.factories.ViewFactory;
import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.LineStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;


/**
 * The abstract implementation of the ViewFactory interface. This class could be the 
 * base class for any generic View  
 * @author mmostafa
 */
abstract public class AbstractViewFactory implements ViewFactory {
	

	/**
	 * The hint used to find the appropriate preference store from which general
	 * diagramming preference values for properties of shapes, connections, and
	 * diagrams can be retrieved. This hint is mapped to a preference store in
	 * the {@link DiagramPreferencesRegistry}.
	 */
	private PreferencesHint preferencesHint;

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.core.view.factories.ViewFactory#createView(org.eclipse.core.runtime.IAdaptable, org.eclipse.gmf.runtime.notation.View, java.lang.String, int, boolean, java.lang.String)
	 */
	abstract public View createView(final IAdaptable semanticAdapter,
			final View containerView, final String semanticHint,
			final int index, final boolean persisted, final PreferencesHint thePreferencesHint);

	/**
	 * creates styles for the passed view and return them as List; the returned list 
	 * could be empty if the view does not need any style
	 * 
	 * @return a list of style for the passed view, or an empty list if none (do not return null)
	 * 
	 */
	protected List createStyles(View view) {
		return new ArrayList();
	}
	
	/**
	 * Indicates if the passed view requires an element inside it or it can use
	 * its container's element
	 * 
	 * @param semanticAdapter
	 * @param containerView
	 * @return
	 */
	protected boolean requiresElement(IAdaptable semanticAdapter, View view) {		
		if (semanticAdapter!=null){			
			return requiresElement((EObject)semanticAdapter.getAdapter(EObject.class),view);
		}
		return true;
	}
	
	/**
	 * Indicates if the passed view requires an element inside it or it can use
	 * its container's element
	 * 
	 * @param semanticAdapter
	 * @param containerView
	 * @return
	 */
	protected boolean requiresElement(EObject semanticElement, View view) {		
		return !(semanticElement == view.getElement());		
	}
	
	/**
	 * Initialize the passed view from the preference store
	 * @param view the view to initialize
	 */
	protected void initializeFromPreferences(View view) {
		
		IPreferenceStore store = (IPreferenceStore) getPreferencesHint().getPreferenceStore();
		if (store == null) {
			return;
		}

        LineStyle lineStyle = (LineStyle) view
			.getStyle(NotationPackage.Literals.LINE_STYLE);
		if (lineStyle != null) {
			// line color
			RGB lineRGB = PreferenceConverter.getColor(store,
				IPreferenceConstants.PREF_LINE_COLOR);

			lineStyle.setLineColor(FigureUtilities.RGBToInteger(lineRGB)
				.intValue());
		}

		FontStyle fontStyle = (FontStyle) view
			.getStyle(NotationPackage.Literals.FONT_STYLE);

		if (fontStyle != null) {
			// default font
			FontData fontData = PreferenceConverter.getFontData(store,
				IPreferenceConstants.PREF_DEFAULT_FONT);
			fontStyle.setFontName(fontData.getName());
			fontStyle.setFontHeight(fontData.getHeight());
			fontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
			fontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
			// font color
			RGB fontRGB = PreferenceConverter.getColor(store,
				IPreferenceConstants.PREF_FONT_COLOR);
			fontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB)
				.intValue());
		}

	}
	
	
	/**
	 * Method getViewService.
	 * a utility function to return the view service instance
	 * @return ViewService
	 */
	protected static ViewService getViewService() {
		return ViewService.getInstance();
	}
	
	/**
	 * Gets the preferences hint that is to be used to find the appropriate
	 * preference store from which to retrieve diagram preference values. The
	 * preference hint is mapped to a preference store in the preference
	 * registry <@link DiagramPreferencesRegistry>.
	 * 
	 * @return the preferences hint
	 */
	protected PreferencesHint getPreferencesHint() {
		return preferencesHint;
	}
	
	/**
	 * Sets the preferences hint that is to be used to find the appropriate
	 * preference store from which to retrieve diagram preference values. The
	 * preference hint is mapped to a preference store in the preference
	 * registry <@link DiagramPreferencesRegistry>.
	 * 
	 * @param preferencesHint the preferences hint
	 */
	protected void setPreferencesHint(PreferencesHint preferencesHint) {
		this.preferencesHint = preferencesHint;
	}

    /**
     * Determines the editing domain for the view creation.
     * 
     * @param semanticElement
     *            the semantic elemement; may be null
     * @param containerView
     *            the container view
     * @return the editing domain
     */
    protected TransactionalEditingDomain getEditingDomain(EObject semanticElement, View containerView) {
    
        TransactionalEditingDomain result = null;
    
        if (semanticElement != null) {
            result = TransactionUtil.getEditingDomain(semanticElement);
        }
    
        if (result == null) {
            result = TransactionUtil.getEditingDomain(containerView);
        }
        return result;
    }
}
