/******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.actions;

/**
 * A list of constants defining the diagram action and menu action ids.
 * <p>
 * This interface defines constants only, it is <EM>not</EM> intended to be
 * implemented by clients.
 * </p>
 */
public interface ActionIds {

	/* Menu contribution ids */
	public final String MENU_DIAGRAM = "diagramMenu"; //$NON-NLS-1$
	
	public final String MENU_DIAGRAM_ADD = "diagramAddMenu"; //$NON-NLS-1$
	public final String MENU_NAVIGATE = "navigateMenu"; //$NON-NLS-1$
	public final String MENU_FILE = "fileMenu"; //$NON-NLS-1$
	public final String MENU_EDIT = "editMenu"; //$NON-NLS-1$
	public final String MENU_FILTERS = "filtersMenu"; //$NON-NLS-1$
	public final String MENU_FORMAT = "formatMenu"; //$NON-NLS-1$

    public final String MENU_ARRANGE = "arrangeMenu"; //$NON-NLS-1$     
    public final String MENU_ARRANGE_TOOLBAR = "arrangeMenuToolbar"; //$NON-NLS-1$     
	public final String MENU_SELECT = "selectMenu"; //$NON-NLS-1$
    public final String MENU_SELECT_TOOLBAR = "selectMenuToolbar"; //$NON-NLS-1$
    public final String MENU_ALIGN = "alignMenu"; //$NON-NLS-1$
	public final String MENU_SHOW_IN = "showInMenu"; //$NON-NLS-1$
	public final String MENU_COMPARTMENT = "compartmentMenu"; //$NON-NLS-1$
	public final String MENU_CONNECTION_LABEL = "connectorLabelMenu"; //$NON-NLS-1$
	public final String MENU_ROUTER = "routerMenu"; //$NON-NLS-1$
	public final String MENU_OPEN_WITH = "openWithMenu"; //$NON-NLS-1$

	/* ZOrder menu and action ids */
	public final String MENU_ZORDER = "zorderMenu"; //$NON-NLS-1$
	public final String ACTION_BRING_TO_FRONT = "bringToFrontAction"; //$NON-NLS-1$
	public final String ACTION_SEND_TO_BACK   = "sendToBackAction"; //$NON-NLS-1$
	public final String ACTION_BRING_FORWARD  = "bringForwardAction"; //$NON-NLS-1$
	public final String ACTION_SEND_BACKWARD  = "sendBackwardAction"; //$NON-NLS-1$

	/* View Menu and action ids */
	public final String MENU_VIEW = "viewMenu"; //$NON-NLS-1$
	public final String ACTION_VIEW_GRID = "viewGridAction"; //$NON-NLS-1$
	public final String ACTION_SNAP_TO_GRID = "snapToGridAction"; //$NON-NLS-1$
	public final String ACTION_VIEW_PAGEBREAKS = "viewPageBreaks";//$NON-NLS-1$
	public final String ACTION_VIEW_RULERS = "viewRulerAction"; //$NON-NLS-1$

	/* Make Same Size Menu and actions ids */
	public final String MENU_MAKE_SAME_SIZE = "makeSameSizeMenu"; //$NON-NLS-1$
	public final String ACTION_MAKE_SAME_SIZE_BOTH = "makeSameSizeBothAction"; //$NON-NLS-1$
	public final String ACTION_MAKE_SAME_SIZE_HEIGHT = "makeSameHeightAction"; //$NON-NLS-1$
	public final String ACTION_MAKE_SAME_SIZE_WIDTH = "makeSameWidthAction"; //$NON-NLS-1$

	/* Custom contribution ids */
	public final String CUSTOM_FONT_NAME = "fontNameContributionItem"; //$NON-NLS-1$
	public final String CUSTOM_FONT_SIZE = "fontSizeContributionItem"; //$NON-NLS-1$
	public final String CUSTOM_FONT_COLOR = "fontColorContributionItem"; //$NON-NLS-1$
	public final String CUSTOM_LINE_COLOR = "lineColorContributionItem"; //$NON-NLS-1$
	public final String CUSTOM_FILL_COLOR = "fillColorContributionItem"; //$NON-NLS-1$
	public final String CUSTOM_ZOOM = "zoomContributionItem"; //$NON-NLS-1$

	/* Action contribution ids */
	public final String ACTION_FONT_DIALOG = "fontDialogAction"; //$NON-NLS-1$
	public final String ACTION_FONT_BOLD = "fontBoldAction"; //$NON-NLS-1$
	public final String ACTION_FONT_ITALIC = "fontItalicAction"; //$NON-NLS-1$
	public final String ACTION_ARRANGE_ALL = "arrangeAllAction"; //$NON-NLS-1$
	public final String ACTION_ARRANGE_SELECTION = "arrangeSelectionAction"; //$NON-NLS-1$
	public final String ACTION_TOOLBAR_ARRANGE_ALL = "toolbarArrangeAllAction"; //$NON-NLS-1$
	public final String ACTION_TOOLBAR_ARRANGE_SELECTION = "toolbarArrangeSelectionAction"; //$NON-NLS-1$
    public final String SELECT_CONNECTION_SOURCE = "selectConnectionSource"; //$NON-NLS-1$
    public final String SELECT_CONNECTION_TARGET = "selectConnectionTarget"; //$NON-NLS-1$
    public final String ACTION_SELECT_ALL_SHAPES = "selectAllShapesAction"; //$NON-NLS-1$
	public final String ACTION_SELECT_ALL_CONNECTIONS = "selectAllConnectorsAction"; //$NON-NLS-1$
	public final String ACTION_TOOLBAR_SELECT_ALL = "toolbarSelectAllAction"; //$NON-NLS-1$
	public final String ACTION_TOOLBAR_SELECT_ALL_SHAPES = "toolbarSelectAllShapesAction"; //$NON-NLS-1$
	public final String ACTION_TOOLBAR_SELECT_ALL_CONNECTIONS = "toolbarSelectAllConnectorsAction"; //$NON-NLS-1$
	public final String ACTION_AUTOSIZE = "autoSizeAction"; //$NON-NLS-1$
	public final String ACTION_COMPARTMENT_ALL = "allCompartmentsAction"; //$NON-NLS-1$
	public final String ACTION_COMPARTMENT_NONE = "noCompartmentsAction"; //$NON-NLS-1$
	public final String ACTION_ROUTER_RECTILINEAR = "rectilinearRouterAction"; //$NON-NLS-1$
	public final String ACTION_ROUTER_OBLIQUE = "obliqueRouterAction"; //$NON-NLS-1$
	public final String ACTION_ROUTER_TREE = "treeRouterAction"; //$NON-NLS-1$
	public final String ACTION_TOGGLE_ROUTER = "toggleRouter"; //$NON-NLS-1$
	public final String ACTION_COPY_APPEARANCE_PROPERTIES = "copyAppearancePropertiesAction"; //$NON-NLS-1$
	public final String ACTION_SNAP_BACK = "snapBackAction"; //$NON-NLS-1$
	public final String ACTION_COPY_BITMAP = "copyBitmapAction"; //$NON-NLS-1$
	public final String ACTION_DELETE_FROM_MODEL = "deleteFromModelAction"; //$NON-NLS-1$	
	public final String ACTION_DELETE_FROM_DIAGRAM = "deleteFromDiagramAction"; //$NON-NLS-1$	
	public final String ACTION_RECALC_PAGEBREAKS = "recalcPageBreaks";//$NON-NLS-1$
	public final String ACTION_SORT_FILTER = "sortfilterAction";//$NON-NLS-1$
	public final String ACTION_SHOW_PROPERTIES_VIEW = "showPropertiesViewAction";//$NON-NLS-1$
	public final String ACTION_SHOW_CONNECTION_LABELS = "showConnectorLabels"; //$NON-NLS-1$
	public final String ACTION_HIDE_CONNECTION_LABELS = "hideConnectorLabels"; //$NON-NLS-1$	
	public final String ACTION_SHOW_COMPARTMENT_TITLE = "showCompartmentTitle"; //$NON-NLS-1$	
    public final String ACTION_GROUP = "groupAction"; //$NON-NLS-1$
    public final String ACTION_UNGROUP = "ungroupAction"; //$NON-NLS-1$
    
	/* The menu ID for the diagram editor popup menu */
	public final String DIAGRAM_EDITOR_CONTEXT_MENU = "org.eclipse.gmf.runtime.diagram.ui.DiagramEditorContextMenu"; //$NON-NLS-1$
	public final String DIAGRAM_OUTLINE_CONTEXT_MENU = "org.eclipse.gmf.runtime.diagram.ui.DiagramOutlineContextMenu"; //$NON-NLS-1$
	
	//actions for adding text and note views
    public final String ACTION_ADD_NOTELINK = "addNoteLinkAction"; //$NON-NLS-1$	
	public final String ACTION_ADD_TEXT = "addTextAction"; //$NON-NLS-1$
	public final String ACTION_ADD_NOTE = "addNoteAction"; //$NON-NLS-1$
	
	//actions for align in non toolbar menus
	public final String ACTION_ALIGN_LEFT = "alignLeftAction"; //$NON-NLS-1$	
	public final String ACTION_ALIGN_RIGHT = "alignRightAction"; //$NON-NLS-1$
	public final String ACTION_ALIGN_TOP = "alignTopAction"; //$NON-NLS-1$ 
	public final String ACTION_ALIGN_BOTTOM = "alignBottomAction"; //$NON-NLS-1$
	public final String ACTION_ALIGN_MIDDLE = "alignMiddleAction"; //$NON-NLS-1$ 
	public final String ACTION_ALIGN_CENTER = "alignCenterAction"; //$NON-NLS-1$

	// actions for keyboard accelerators
	public final String ACTION_INSERT_SEMANTIC = "insertSemantic"; //$NON-NLS-1$
	
	// Navigate Action Ids
	public final String OPEN = "OpenAction"; //$NON-NLS-1$

	//actions for text alignment in non toolbar menus
    public final String MENU_TEXT_ALIGNMENT = "textAlignmentMenu"; //$NON-NLS-1$
	public final String ACTION_TEXT_ALIGNMENT_LEFT = "textAlignmentLeftAction"; //$NON-NLS-1$	
	public final String ACTION_TEXT_ALIGNMENT_CENTER = "textAlignmentCenterAction"; //$NON-NLS-1$
	public final String ACTION_TEXT_ALIGNMENT_RIGHT = "textAlignmentRightAction"; //$NON-NLS-1$

	//actions for line width in non toolbar menus
    public final String MENU_LINE_WIDTH = "lineWidthMenu"; //$NON-NLS-1$
	public final String ACTION_LINE_WIDTH_ONE = "lineWidthOneAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_WIDTH_TWO = "lineWidthTwoAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_WIDTH_THREE = "lineWidthThreeAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_WIDTH_FOUR = "lineWidthFourAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_WIDTH_FIVE = "lineWidthFiveAction"; //$NON-NLS-1$	

	//actions for line type in non toolbar menus
    public final String MENU_LINE_TYPE = "lineTypeMenu"; //$NON-NLS-1$
	public final String ACTION_LINE_TYPE_SOLID = "lineTypeSolidAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_TYPE_DASH = "lineTypeDashAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_TYPE_DOT = "lineTypeDotAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_TYPE_DASH_DOT = "lineTypeDashDotAction"; //$NON-NLS-1$	
	public final String ACTION_LINE_TYPE_DASH_DOT_DOT = "lineTypeDashDotDotAction"; //$NON-NLS-1$	

	//actions for line type in non toolbar menus
    public final String MENU_ARROW_TYPE = "arrowTypeMenu"; //$NON-NLS-1$
	public final String MENU_ARROW_TYPE_SOURCE = "arrowTypeSourceMenu"; //$NON-NLS-1$	
	public final String MENU_ARROW_TYPE_TARGET = "arrowTypeTargetMenu"; //$NON-NLS-1$	
	public final String ACTION_ARROW_TYPE_SOURCE_NONE = "arrowTypeSourceNoneAction"; //$NON-NLS-1$	
	public final String ACTION_ARROW_TYPE_SOURCE_SOLID = "arrowTypeSourceSolidAction"; //$NON-NLS-1$	
	public final String ACTION_ARROW_TYPE_SOURCE_OPEN = "arrowTypeSourceOpenAction"; //$NON-NLS-1$	
	public final String ACTION_ARROW_TYPE_TARGET_NONE = "arrowTypeTargetNoneAction"; //$NON-NLS-1$	
	public final String ACTION_ARROW_TYPE_TARGET_SOLID = "arrowTypeTargetSolidAction"; //$NON-NLS-1$	
	public final String ACTION_ARROW_TYPE_TARGET_OPEN = "arrowTypeTargetOpenAction"; //$NON-NLS-1$	
	
}
