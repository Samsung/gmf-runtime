/******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 * 	  Dmitry Stadnik (Borland) - contribution for bugzilla 135694
 *	  Dmitry Stadnik (Borland) - contribution for bugzilla 136582
 ****************************************************************************/
package org.eclipse.gmf.runtime.diagram.ui.editparts;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gmf.runtime.common.core.util.Log;
import org.eclipse.gmf.runtime.common.core.util.Trace;
import org.eclipse.gmf.runtime.common.ui.services.parser.CommonParserHint;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserOptions;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserService;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.LabelDirectEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIDebugOptions;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIPlugin;
import org.eclipse.gmf.runtime.diagram.ui.internal.DiagramUIStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.diagram.ui.label.ILabelDelegate;
import org.eclipse.gmf.runtime.diagram.ui.label.WrappingLabelDelegate;
import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.diagram.ui.tools.TextDirectEditManager;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ISemanticParser;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ParserHintAdapter;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.TextAlignment;
import org.eclipse.gmf.runtime.notation.TextStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

/**
 * The controller for the text compartment.
 * 
 * @author mmostafa
 */
public class TextCompartmentEditPart extends CompartmentEditPart implements ITextAwareEditPart {
	
	/** the direct edit manager for text editing */
	private DirectEditManager manager;
	/** the text parser */
	protected IParser parser;
	/** the text parser options */
	private ParserOptions parserOptions;
	/** the element to listen to as suggested by the parser*/
	private List parserElements = null;
	/** the number of icons in the text label */
	private int numIcons = 0;

	/** Label that is displayed as the tooltip. */
	private Label toolTipLabel = new Label();
	  
	private ILabelDelegate labelDelegate;

	/**
	 * coinstructor
	 * @param view the view controlled by this edit part
	 */
	public TextCompartmentEditPart(EObject model) {
		super(model);
	}

	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(
			EditPolicy.DIRECT_EDIT_ROLE,
			new LabelDirectEditPolicy());
		// Text Compartment do not handle creation request for views
		removeEditPolicy(EditPolicyRoles.CREATION_ROLE);
		
	}

    /**
     * Override to use a different figure for this editpart.
     * <p>
     * IMPORTANT NOTES:
     * <li> If you override this to create a different type of figure, you
     * should also override {@link #createLabelDelegate()} and make sure you no
     * longer call {@link #getLabel()}.
     * <li> Do not call {@link #getLabelDelegate()} from within this method. Any
     * initialization of the label delegate should be done in the
     * {@link #createLabelDelegate()} method.
     * </p>
     */
    protected IFigure createFigure() {
        return createWrapLabel();
    }
    
    /**
     * @return WrapLabel, the created wrap label
     * @deprecated This method has been deprecated to remove the assumption that
     *             the figure of a <code>TextCompartmentEditPart</code> is a
     *             <code>WrapLabel</code>. Create your figure in the
     *             {@link #createFigure()} method instead and don't forget to
     *             stop calling {@link #getLabel()}.
     */
    protected WrapLabel createWrapLabel() {
        // alignment properties are set in createLabelDelegate().
        return new WrapLabel();
    }
    
    /**
     * Creates the label delegate that will be used to interact with the label
     * figure.
     * <p>
     * Note: If you have overridden {@link #createFigure()} to create a
     * different type of figure, you need to also override this method to return
     * the appropriate type of <code>LabelDelegate</code>.
     * 
     * @return the new label delegate
     * @since 2.1
     */
    protected ILabelDelegate createLabelDelegate() {

        // just in case the client has overridden getLabel()...
        WrapLabel label = getLabel();
        ILabelDelegate newLabelDelegate = null;
        if (label != null) {
            newLabelDelegate = new WrappingLabelDelegate(label);
        } else {

            // this should be a WrappingLabel since this is what is created in
            // createFigure()...
            newLabelDelegate = new WrappingLabelDelegate(
                (WrappingLabel) getFigure());
        }

        newLabelDelegate.setTextJustification(PositionConstants.CENTER);
        newLabelDelegate.setAlignment(PositionConstants.CENTER);
        newLabelDelegate.setTextAlignment(PositionConstants.TOP);
        return newLabelDelegate;
    }
    
    public IFigure getFigure() {
        if (figure == null) {
            setFigure(createFigure());
            setLabelDelegate(createLabelDelegate());
        }
        return figure;
    }

    /**
     * Returns the label delegate that can be used to interact with the label
     * figure.
     * 
     * @return the label delegate
     * @since 2.1
     */
    public ILabelDelegate getLabelDelegate() {
        if (labelDelegate == null) {
            // this means that getFigure() has never been called as getFigure()
            // sets the label delegate. Call getFigure() first.
            getFigure();

            // check if the label delegate is null, just in case getFigure() was
            // overridden
            if (labelDelegate == null) {
                setLabelDelegate(createLabelDelegate());
            }

            return labelDelegate;
        }
        return labelDelegate;
    }

    /**
     * Sets the label delegate.
     * 
     * @param labelDelegate
     *            the label delegate to be set
     * @since 2.1
     */
    protected void setLabelDelegate(ILabelDelegate labelDelegate) {
        this.labelDelegate = labelDelegate;
    }
    
    public Object getAdapter(Class key) {
        if (key == ILabelDelegate.class) {
            return getLabelDelegate();
        }
        return super.getAdapter(key);
    }  
	
	/**
     * This should be used instead of getFigure() to get the figure
     * 
     * @return Return the WrapLabel for the TextCompartment
     * @deprecated This method has been deprecated to remove the assumption that
     *             the figure of a <code>TextCompartmentEditPart</code> is a
     *             <code>WrapLabel</code>. Use {@link #getLabelDelegate()} if
     *             the behavior you need is available from the label delegate,
     *             if not use {@link #getFigure()} and cast to the type of label
     *             that you created in the {@link #createFigure()} method.
     */
    public WrapLabel getLabel() {
        return (WrapLabel) getFigure();
    }

	/**
	 * gets the label Icon for this edit part
	 * @param index the index to use
	 * @return Image
	 */
	protected Image getLabelIcon(int index) {
		return null;
	}

	/**
	 * gets the label text
	 * @return the lebel text
	 */
	protected String getLabelText() {
		EObject element = resolveSemanticElement();
		return (element == null) ? null
			: (getParser() == null) ? null
				: getParser().getPrintString(new EObjectAdapter(element),
					getParserOptions().intValue());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart#getEditText()
	 */
	public String getEditText() {
		EObject element = resolveSemanticElement();
		return (element == null) ? "" //$NON-NLS-1$
		: getParser().getEditString(
			new EObjectAdapter(element),
			getParserOptions().intValue());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart#editTextModified(java.lang.String)
	 */
	public void setLabelText(String text) {
        getLabelDelegate().setText(text);
	}

	/* 
     * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart#getCompletionProcessor()
	 */
	public IContentAssistProcessor getCompletionProcessor() {
		EObject element = resolveSemanticElement();
		if (element != null) {
			return getParser().getCompletionProcessor(new EObjectAdapter(element));
		}
		return null;
	}

	private boolean canParse() {
		return getEditText() != null;
	}

	/* 
     * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart#getEditTextValidator()
	 */
	public ICellEditorValidator getEditTextValidator() {
		return new ICellEditorValidator() {
			public String isValid(final Object value) {
				if (value instanceof String) {
					final EObject element = resolveSemanticElement();

					final IParser theParser = getParser();
					try {
						IParserEditStatus isValid = (IParserEditStatus) getEditingDomain()
							.runExclusive(new RunnableWithResult.Impl() {

									public void run() {
										setResult(theParser.isValidEditString(
											new EObjectAdapter(element),
											(String) value));
									}
								});
						return isValid.getCode() == ParserEditStatus.EDITABLE ? null
							: isValid.getMessage();
					} catch (InterruptedException e) {
						Trace.catching(DiagramUIPlugin.getInstance(),
							DiagramUIDebugOptions.EXCEPTIONS_CATCHING,
							getClass(), "getEditTextValidator", e); //$NON-NLS-1$
						Log.error(DiagramUIPlugin.getInstance(),
							DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
							"getEditTextValidator", e); //$NON-NLS-1$
					}

				}

				// shouldn't get here
				return null;
			}
		};
	}

	/* 
     * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart#getParserOptions()
	 */
	public final ParserOptions getParserOptions() {
		if (parserOptions == null)
			parserOptions = buildParserOptions();
		return parserOptions;
	}

	/**
	 * Builds the parser options. 
	 * @return ParserOptions the parser options
	 */
	protected ParserOptions buildParserOptions() {
		return ParserOptions.NONE;
	}

	/**
	 * Builds the parser options.
	 */
	protected final void refreshParserOptions() {
		parserOptions = buildParserOptions();
	}

	/**
	 * Determines if the given event affects the paser options
	 * 
	 * @param evt The event in question
	 * @return whether the given event affects the parser options
	 */
	protected boolean isAffectingParserOptions(PropertyChangeEvent evt) {
		return false;
	}
	
	/**
	 * Determines if the given Notification affects the paser options
	 * 
	 * @param evt The notification in question
	 * @return whether the given notification affects the parser options
	 */
	protected boolean isAffectingParserOptions(Notification evt) {
		return false;
	}


	/**
	 * Method getLabelToolTip.
	 * @return IFigure
	 */
	protected IFigure getLabelToolTip() {
		String text = getToolTipText();
		if (text != null && text.length() > 0) {
			toolTipLabel.setText(text);
			return toolTipLabel;
		}
		return null;
	}

	/**
	 * This method can be overridden in the subclass to return
	 * text for the tooltip.  
	 * @return String the tooltip
	 */
	protected String getToolTipText() {
		return null;
	}

	/**
	 * check if this edit part is editable or not
	 * @return true or false
	 */
	protected boolean isEditable() {
		EObject element = resolveSemanticElement();
		if (element != null && canParse()) {
			return true;
		}
		return false;
	}
    

	/**
	 * performas direct edit
	 */
	protected void performDirectEdit() {
		getManager().show();
	}

	/**
	 * Performs direct edit and will initiate another mouse click 
	 * event so that the cursor will appear under the mouse
	 * 	 
	 *  @param eventLocation
	 */
	protected void performDirectEdit(Point eventLocation) {
		if (getManager().getClass() == TextDirectEditManager.class) {
			((TextDirectEditManager) getManager()).show(eventLocation.getSWTPoint());
		} else {
            performDirectEdit();
		}
	}
	
	/**
	 * 
	 * Performs direct edit setting the initial text to be the initialCharacter
	 * 
	 * @param initialCharacter
	 */
	private void performDirectEdit(char initialCharacter) {
		// Run the TextDirectEditManager show with the initial character
		// This will not send an extra mouse click
		if (getManager() instanceof TextDirectEditManager) {
			((TextDirectEditManager) getManager()).show(initialCharacter);
		} else {
			performDirectEdit();
		}
	}
    
    private void showEditPart(){
        EditPart parent = getParent();
        if (parent!=null){
            EditPartViewer viewer = parent.getViewer();
            if (viewer!=null){
                viewer.reveal(this);
            }
        }
    }

	
	/**
	 * 
	 * Performs direct edit request based on request type
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#performDirectEditRequest(org.eclipse.gef.requests.DirectEditRequest)
	 */
	protected void performDirectEditRequest(Request request) {

		final Request theRequest = request;

		try {
			getEditingDomain().runExclusive(new Runnable() {
				public void run() {
					if (isActive() && isEditable()) {
                        showEditPart();
						// IF the direct edit request has an initial character...
						if (theRequest.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR) instanceof Character) {							
							Character initialChar = (Character) theRequest.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR);
							performDirectEdit(initialChar.charValue());
						} else if ((theRequest instanceof DirectEditRequest) && (getEditText().equals(getLabelText()))) {
							DirectEditRequest editRequest = (DirectEditRequest) theRequest;
							performDirectEdit(editRequest.getLocation());
						} else { // Some other Request
							performDirectEdit();
						}
					}
				}
			});
		} catch (InterruptedException e) {
			Trace.catching(DiagramUIPlugin.getInstance(),
				DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
				"performDirectEditRequest", e); //$NON-NLS-1$
			Log.error(DiagramUIPlugin.getInstance(),
				DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
				"performDirectEditRequest", e); //$NON-NLS-1$
		}

	}

	protected void handleNotificationEvent(Notification event) {
		Object feature = event.getFeature();
		if (NotationPackage.eINSTANCE.getFontStyle_FontColor().equals(feature)) {
			Integer c = (Integer) event.getNewValue();

			setFontColor(DiagramColorRegistry.getInstance().getColor(c));
		} 
		else if (NotationPackage.eINSTANCE.getFontStyle_Underline().equals(feature))
			refreshUnderline();
		else if (NotationPackage.eINSTANCE.getFontStyle_StrikeThrough().equals(feature))
			refreshStrikeThrough();
		else if (NotationPackage.eINSTANCE.getFontStyle_FontHeight().equals(feature) ||
				NotationPackage.eINSTANCE.getFontStyle_FontName().equals(feature) || 
				NotationPackage.eINSTANCE.getFontStyle_Bold().equals(feature) ||
				NotationPackage.eINSTANCE.getFontStyle_Italic().equals(feature)) {
			refreshFont();
		} 
		else if (NotationPackage.eINSTANCE.getTextStyle_TextAlignment().equals(feature))
			refreshTextAlignment();
		else if (isAffectingParserOptions(event)) {
			refreshParserOptions();
			refreshLabel();
			
		} else if (getParser() != null) {
			
			boolean sematicsAffected = getParser() instanceof ISemanticParser
					&& ((ISemanticParser) getParser())
							.areSemanticElementsAffected(null, event);
							
			boolean parserAffected = getParser().isAffectingEvent(event,
					getParserOptions().intValue());

			if (sematicsAffected) {
				removeSemanticListeners();
				
				if (resolveSemanticElement() != null) {
					addSemanticListeners();
				}
			}
			
			if (sematicsAffected || parserAffected) {
				refreshLabel();
			}
		}
		super.handleNotificationEvent(event);
	}


	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshParserOptions();
		refreshLabel();
        refreshFont();
		refreshUnderline();
		refreshStrikeThrough();
		refreshFontColor();
		refreshTextAlignment();
	}

    /* (non-Javadoc)
     * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#refreshFont()
     */
    protected void refreshFont() {
        FontStyle style = (FontStyle) getPrimaryView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
        FontData fontData = null;
        
        if (style != null) {
            fontData = new FontData(
                style.getFontName(), 
                style.getFontHeight(), 
                (style.isBold() ? SWT.BOLD : SWT.NORMAL) | 
                (style.isItalic() ? SWT.ITALIC : SWT.NORMAL));
        } else {
            // initialize font to defaults
            fontData =
                PreferenceConverter.getFontData(
                    (IPreferenceStore)getDiagramPreferencesHint().getPreferenceStore(),
                    IPreferenceConstants.PREF_DEFAULT_FONT);
        }
        
        setFont(fontData);
    }

	protected void setFontColor(Color color) {
		getFigure().setForegroundColor(color);
	}

	protected void addNotationalListeners() {
		super.addNotationalListeners();
		addListenerFilter("PrimaryView", this, getPrimaryView()); //$NON-NLS-1$
	}

	protected void addSemanticListeners() {
		if (getParser() instanceof ISemanticParser) {
			EObject semanticElement = resolveSemanticElement();
			parserElements =
				((ISemanticParser) getParser()).getSemanticElementsBeingParsed(semanticElement);

			for (int i = 0; i < parserElements.size(); i++)
				addListenerFilter("SemanticModel" + i, this,(EObject)parserElements.get(i)); //$NON-NLS-1$

		} else 
			super.addSemanticListeners();
	}

	protected void removeNotationalListeners() {
		super.removeNotationalListeners();
		removeListenerFilter("PrimaryView"); //$NON-NLS-1$
	}

	protected void removeSemanticListeners() {
		if (parserElements != null) {
			for (int i = 0; i < parserElements.size(); i++)
				removeListenerFilter("SemanticModel" + i); //$NON-NLS-1$
		} else
			super.removeSemanticListeners();
	}

	/**
	 * getter for the Num Icons
	 * @return num icons
	 */
	public int getNumIcons() {
		return numIcons;
	}

	/**
	 * setter for the num icons
	 * @param numIcons
	 */
	public void setNumIcons(int numIcons) {
		this.numIcons = numIcons;
	}

	protected List getModelChildren() {
		return Collections.EMPTY_LIST;
	}

	/* 
     * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart#getParser()
	 */
	public IParser getParser() {
		if (parser == null) {
			String parserHint = ((View)getModel()).getType();
			EObject element = resolveSemanticElement();
			if (element != null) {
				ParserHintAdapter hintAdapter =
					new ParserHintAdapter(element, parserHint);
				parser = ParserService.getInstance().getParser(hintAdapter);
			}
		}
		return parser;
	}

	/**
	 * Will update the tool tip text for the figure and also the icons for the label.  In additional
	 * it will apply any font constraints to the label based on the type of Text Compartment we
	 * are dealing with.
	 * Any body overriding this method should either can this super.refreshLabel() or
	 * call applyFontContraintsToLabel() to ensure the the proper font constraints are apply to
	 * the label.
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.TextCompartmentEditPart#refreshLabel()
	 */
	protected void refreshLabel() {
		// refreshes the label text
		getLabelDelegate().setText(getLabelText());

		// refreshes the label icon(s)
		for (int i = 0; i < numIcons; i++)
			getLabelDelegate().setIcon(getLabelIcon(i), i);

		// refreshes the label tool tip
		getFigure().setToolTip(getLabelToolTip());
	}

	/**
	 * Refreshes the font underline property
	 */
	protected void refreshUnderline() {
		FontStyle style = (FontStyle) getPrimaryView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null)
			getLabelDelegate().setTextUnderline(style.isUnderline());
	}

	/**
	 * Refreshes the font underline property
	 */
	protected void refreshStrikeThrough() {
		FontStyle style = (FontStyle) getPrimaryView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null)
			getLabelDelegate().setTextStrikeThrough(style.isStrikeThrough());
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getAccessibleEditPart()
	 */
	protected AccessibleEditPart getAccessibleEditPart() {
		if (accessibleEP == null)
			accessibleEP = new AccessibleGraphicalEditPart() {
			public void getName(AccessibleEvent e) {
                ILabelDelegate label = getLabelDelegate();
                if (label != null) {
                    e.result = label.getText();
                }

			}
		};
		return accessibleEP;
	}

	/**
	 * There is no children to text compartments 
	 * 
	 * @param semanticHint
	 * @return IGraphicalEditPart
	 */
	public IGraphicalEditPart getChildBySemanticHint(String semanticHint) {
		return null;
	}
	/**
	 * @return Returns the manager.
	 *
	 */
	protected DirectEditManager getManager() {
		if (manager == null)
			setManager(
				new TextDirectEditManager(
					this));
		return manager;
	}
	/**
	 * @param manager The manager to set.
	 * 
	 */
	protected void setManager(DirectEditManager manager) {
		this.manager = manager;
	}
	
	/**
	 * gets the primary child view for this edit part, this is usually used
	 * by direct edit requests, to see where the edit will happen
	 * @return <code>View</code>
	 */
	public View getPrimaryChildView(){
		if (getModel()!=null){
			View view = (View)getModel();
			return ViewUtil.getChildBySemanticHint(view,CommonParserHint.DESCRIPTION);
		}
		return null;
	}

	/**
	 * Refreshes the text alignment property
	 */
	protected void refreshTextAlignment() {
		TextStyle style = (TextStyle) getPrimaryView().getStyle(NotationPackage.eINSTANCE.getTextStyle());
		if (style != null) {
			if (style.getTextAlignment() == TextAlignment.RIGHT_LITERAL) {
				if((getLabelDelegate().getIcon(0)==null) )
					getLabelDelegate().setAlignment(PositionConstants.RIGHT);
				else 
					getLabelDelegate().setTextJustification(PositionConstants.RIGHT);
			} else if (style.getTextAlignment() == TextAlignment.CENTER_LITERAL) {
				if((getLabelDelegate().getIcon(0)==null) )
					getLabelDelegate().setAlignment(PositionConstants.CENTER);
				else 
					getLabelDelegate().setTextJustification(PositionConstants.CENTER);
			} else {
				// default to TextAlignment.LEFT_LITERAL
				if((getLabelDelegate().getIcon(0)==null) )
					getLabelDelegate().setAlignment(PositionConstants.LEFT);
				else 
					getLabelDelegate().setTextJustification(PositionConstants.LEFT);
			}
		}
	}
}
