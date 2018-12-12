/******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
/**
 * The base abstract node view factory 
 * @see  org.eclipse.gmf.runtime.diagram.ui.view.factories.BasicNodeViewFactory
 * @author mmostafa
 */
abstract public class AbstractShapeViewFactory extends BasicNodeViewFactory {

	/**
	 * Method used to create the layout constraint that will get set on the 
	 * created view. You can override this method in your own factory to change
	 * the default constraint. This method is called by @link #createView(IAdaptable, View, String, int, boolean) 
	 * @return a new layout constraint for the view
	 */
	protected LayoutConstraint createLayoutConstraint() {
		return NotationFactory.eINSTANCE.createBounds();
	}
	
	/**
	 * Initialize the newly created view from the preference store, this
	 * method get called by @link #decorateView(View, IAdaptable, String)
	 * @param view the view to initialize
	 */
	protected void initializeFromPreferences(View view) {
		super.initializeFromPreferences(view);

		IPreferenceStore store = (IPreferenceStore) getPreferencesHint().getPreferenceStore();
		
		FillStyle fillStyle = (FillStyle) view
			.getStyle(NotationPackage.Literals.FILL_STYLE);
		if (fillStyle != null) {
			// fill color
			RGB fillRGB = PreferenceConverter.getColor(store,
				IPreferenceConstants.PREF_FILL_COLOR);

			fillStyle.setFillColor(FigureUtilities.RGBToInteger(fillRGB)
				.intValue());
		}		
	}
	
	/**
	 * this method is called by @link #createView(IAdaptable, View, String, int, boolean) to 
	 * create the styles for the view that will be created, you can override this 
	 * method in you factory sub class to provide additional styles
	 * @return a list of style for the newly created view or an empty list if none (do not return null)
	 */
	protected List createStyles(View view) {
		List styles = new ArrayList();
		styles.add(NotationFactory.eINSTANCE.createShapeStyle());
		return styles;
	}
}