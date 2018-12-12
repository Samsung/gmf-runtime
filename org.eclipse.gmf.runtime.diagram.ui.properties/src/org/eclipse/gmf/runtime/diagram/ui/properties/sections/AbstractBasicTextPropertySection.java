/******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.properties.sections;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.common.ui.util.StatusLineUtil;
import org.eclipse.gmf.runtime.diagram.ui.properties.views.TextChangeHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * A generic class to edit a property via text field. This class supports
 * multi-select behavior
 * 
 * @author natalia balaba
 */
public abstract class AbstractBasicTextPropertySection
	extends AbstractModelerPropertySection {

	// text widget to display and set value of the property
	private Text textWidget;

	// label widget for the property name
	private CLabel propertyNameLabelWidget;

	/**
	 * @return - name of the property to place in the label widget
	 */
	abstract protected String getPropertyNameLabel();

	/**
	 * Set property value for the given object
	 * 
	 * @param object -
	 *            owner of the property
	 * @param value -
	 *            new value
	 */
	abstract protected void setPropertyValue(EObject object, Object value);

	/**
	 * @return - string representation of the property value
	 */
	abstract protected String getPropertyValueString();

	/**
	 * @return - title of the command which will be executed to set the property
	 */
	protected abstract String getPropertyChangeCommandName();

	/**
	 * A helper to listen for events that indicate that a text field has been
	 * changed.
	 */
	private TextChangeHelper listener = new TextChangeHelper() {
		boolean textModified = false;
		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Modify :
					textModified = !isNonUserChange();
					break;
				case SWT.KeyDown :
					if (event.character == SWT.CR) {
						textChanged((Control)event.widget);
					}
					break;
				case SWT.FocusOut :
					textChanged((Control)event.widget);
					break;
			}
		}
		
		public void textChanged(Control control) {
			if (textModified) {
				// clear error message
				IWorkbenchPart part = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getActivePart();
				StatusLineUtil.outputErrorMessage(part, StringStatics.BLANK);

				setPropertyValue(control);
				textModified = false;
			}
		}		
	};

	private Composite sectionComposite;


	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		doCreateControls(parent, aTabbedPropertySheetPage);
	}
	
	/**
	 * Creates the GUI <code>Control</code> for this text property section
	 * @param parent parent <code>Composite</code>
	 * @param aTabbedPropertySheetPage <code>TabbedPropertySheetPage</code>
	 * @see org.eclipse.gmf.runtime.common.ui.properties.ISection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.gmf.runtime.common.ui.properties.TabbedPropertySheetPage)
	 */
	public void doCreateControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		sectionComposite = getWidgetFactory().createFlatFormComposite(parent);
		textWidget = createTextWidget(sectionComposite);
		propertyNameLabelWidget = createLabelWidget(sectionComposite);

		startTextWidgetEventListener();

	}

	/**
	 * Start listening to the text widget events
	 */
	protected void startTextWidgetEventListener() {
		if (!isReadOnly()) {
			getListener().startListeningTo(getTextWidget());
			getListener().startListeningForEnter(getTextWidget());
		}
	}

	/**
	 * Stop listening to text widget events
	 */
	protected void stopTextWidgetEventListener() {
		if (!isReadOnly())
			getListener().stopListeningTo(getTextWidget());
	}

	/**
	 * Instantiate a text widget
	 * 
	 * @param parent -
	 *            parent composite
	 * @return - a text widget to display and edit the property
	 */
	protected Text createTextWidget(Composite parent) {
		Text text = getWidgetFactory().createText(parent, StringStatics.BLANK);
		FormData data = new FormData();
		data.left = new FormAttachment(0, getStandardLabelWidth(parent,
			getPropertyNameStringsArray()));
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		text.setLayoutData(data);
		if (isReadOnly())
			text.setEditable(false);
		return text;
	}

	/**
	 * returns as an array the property name
	 * @return - array of strings where each describes a property name one per
	 *         property. The strings will be used to calculate common indent
	 *         from the left
	 */
	protected String[] getPropertyNameStringsArray() {
		return new String[] {getPropertyNameLabel()};
	}

	/**
	 * Create a label for property name
	 * 
	 * @param parent -
	 *            parent composite
	 * @return - label to show property name
	 */
	protected CLabel createLabelWidget(Composite parent) {
		CLabel label = getWidgetFactory().createCLabel(parent,
			getPropertyNameLabel());
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(getTextWidget(),
			-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(getTextWidget(), 0, SWT.CENTER);
		label.setLayoutData(data);
		return label;
	}

	/**
	 * User pressed Enter key after editing text field - update the model
	 * 
	 * @param control <code>Control</code>
	 */
	protected synchronized void setPropertyValue(Control control) {

		final Object value = computeNewPropertyValue();
		ArrayList commands = new ArrayList();
		for (Iterator it = getEObjectList().iterator(); it.hasNext();) {
			final EObject next = (EObject) it.next();
			commands.add(createCommand(getPropertyChangeCommandName(), next,
				new Runnable() {

					public void run() {
						setPropertyValue(next, value);
					}

				}));
		}

		executeAsCompositeCommand(getPropertyChangeCommandName(), commands);
		refresh();

	}

	/**
	 * @return - a default implementation returns contents of the text widget as
	 *         a new value for the property. Subclasses can override.
	 */
	protected Object computeNewPropertyValue() {
		return getTextWidget().getText();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#dispose()
	 */
	public void dispose() {
		stopTextWidgetEventListener();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
	 */
	public void refresh() {
		getListener().startNonUserChange();
		try {
			executeAsReadAction(new Runnable() {

				public void run() {
					refreshUI();
				}
			});
		} finally {
			getListener().finishNonUserChange();
		}
	}

	/**
	 * Refresh UI body - referesh will surround this with read action block
	 */
	protected void refreshUI() {
		getTextWidget().setText(getPropertyValueString());
	}

	/**
	 * @return Returns the listener.
	 */
	protected TextChangeHelper getListener() {
		return listener;
	}

	/**
	 * @return Returns the textWidget.
	 */
	protected Text getTextWidget() {
		return textWidget;
	}

	/**
	 * @return Returns the propertyNameLabelWidget.
	 */
	public CLabel getPropertyNameLabelWidget() {
		return propertyNameLabelWidget;
	}

	/**
	 * @return Returns the sectionComposite.
	 */
	public Composite getSectionComposite() {
		return sectionComposite;
	}
}