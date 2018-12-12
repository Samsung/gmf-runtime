/******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.gef.ui.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A menu manager is a contribution manager which realizes itself and its items
 * in a menu control; either as a menu bar, a sub-menu, or a context menu.
 * <p>
 * This class may be instantiated; it may also be subclassed.
 * </p>
 * 
 * @deprecated Use MenuManager or ActionMenuManager instead.
 */
public class MenuManagerEx
	extends ContributionManager
	implements IMenuManager {

	/**
	 * The menu control; <code>null</code> before
	 * creation and after disposal.
	 */
	private Menu menu = null;

	/**
	 * List of registered menu listeners (element type: <code>IMenuListener</code>).
	 */
	private ListenerList listeners = new ListenerList(1);

	/**
	 * The menu id.
	 */
	private String id;

	/**
	 * The menu item widget; <code>null</code> before
	 * creation and after disposal. This field is used
	 * when this menu manager is a sub-menu.
	 */
	private MenuItem menuItem;

	/**
	 * The text for a sub-menu.
	 */
	private String menuText;

	/**
	 * Indicates whether <code>removeAll</code> should be
	 * called just before the menu is displayed.
	 */
	private boolean removeAllWhenShown = false;

	/**
	 * Indicates this item is visible in its manager; <code>true</code> 
	 * by default.
	 */
	private boolean visible = true;

	/**
	 * Creates a menu manager.  The text and id are <code>null</code>.
	 * Typically used for creating a context menu, where it doesn't need to be referred to by id.
	 */
	public MenuManagerEx() {
		this(null, null);
	}
	/**
	 * Creates a menu manager with the given text. The id of the menu
	 * is <code>null</code>.
	 * Typically used for creating a sub-menu, where it doesn't need to be referred to by id.
	 *
	 * @param text the text for the menu, or <code>null</code> if none
	 */
	public MenuManagerEx(String text) {
		this(text, null);
	}
	/**
	 * Creates a menu manager with the given text and id.
	 * Typically used for creating a sub-menu, where it needs to be referred to by id.
	 *
	 * @param text the text for the menu, or <code>null</code> if none
	 * @param id the menu id, or <code>null</code> if it is to have no id
	 */
	public MenuManagerEx(String text, String id) {
		this.menuText = text;
		this.id = id;
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void addMenuListener(IMenuListener listener) {
		listeners.add(listener);
	}
	/**
	 * Creates and returns an SWT context menu control for this menu,
	 * and installs all registered contributions.
	 * Does not create a new control if one already exists.
	 * <p>
	 * Note that the menu is not expected to be dynamic.
	 * </p>
	 *
	 * @param parent the parent control
	 * @return the menu control
	 */
	public Menu createContextMenu(Control parent) {
		if (menu == null || menu.isDisposed()) {
			menu = new Menu(parent);
			initializeMenu();
		}
		return menu;
	}
	/**
	 * Creates and returns an SWT menu bar control for this menu,
	 * for use in the given shell, and installs all registered contributions.
	 * Does not create a new control if one already exists.
	 *
	 * @param parent the parent shell
	 * @return the menu control
	 */
	public Menu createMenuBar(Shell parent) {
		if (menu == null || menu.isDisposed()) {
			menu = new Menu(parent, SWT.BAR);
			update(false);
		}
		return menu;
	}

	/**
	 * @param parent
	 * @return <code>Menu</code>
	 */
	public Menu createSubMenu(Menu parent) {
		if (menu == null || menu.isDisposed()) {
			menu = new Menu(parent);
			initializeMenu();
		}
		return menu;
	}
	/**
	 * Disposes of this menu manager and frees all allocated SWT resources.
	 * Note that this method does not clean up references between this menu
	 * manager and its associated contribution items.
	 * Use <code>removeAll</code> for that purpose.
	 */
	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
		if (menuItem != null) {
			menuItem.dispose();
			menuItem = null;
		}
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void fill(Composite parent) {
		// empty method
	}

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void fill(Menu parent, int index) {
		if (menuItem == null || menuItem.isDisposed()) {
			if (index >= 0)
				menuItem = new MenuItem(parent, SWT.CASCADE, index);
			else
				menuItem = new MenuItem(parent, SWT.CASCADE);

			menuItem.setText(menuText);

			if (menu == null || menu.isDisposed())
				menu = new Menu(parent);

			menuItem.setMenu(menu);

			initializeMenu();

			// populate the submenu, in order to enable accelerators
			// and to set enabled state on the menuItem properly
			update(true);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void fill(ToolBar parent, int index) {
		// empty method
	}

	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public IMenuManager findMenuUsingPath(String path) {
		IContributionItem item = findUsingPath(path);
		if (item instanceof IMenuManager)
			return (IMenuManager) item;
		return null;
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public IContributionItem findUsingPath(String path) {
		String menuId = path;
		String rest = null;
		int separator = path.indexOf('/');
		if (separator != -1) {
			menuId = path.substring(0, separator);
			rest = path.substring(separator + 1);
		} else {
			return super.find(path);
		}

		IContributionItem item = super.find(menuId);
		if (item instanceof IMenuManager) {
			IMenuManager manager = (IMenuManager) item;
			return manager.findUsingPath(rest);
		}

		return null;
	}

	/**
	 * Notifies any menu listeners that a menu is about to show.
	 * Only listeners registered at the time this method is called are notified.
	 *
	 * @param manager the menu manager
	 *
	 * @see IMenuListener#menuAboutToShow
	 */
	private void fireAboutToShow(IMenuManager manager) {
		Object[] listenerList = this.listeners.getListeners();
		for (int i = 0; i < listenerList.length; ++i) {
			((IMenuListener) listenerList[i]).menuAboutToShow(manager);
		}
	}

	/**
	 * Returns the menu id.
	 * The menu id is used when creating a contribution item 
	 * for adding this menu as a sub menu of another.
	 *
	 * @return the menu id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Returns the SWT menu control for this menu manager.
	 *
	 * @return the menu control
	 */
	public Menu getMenu() {
		return menu;
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public boolean getRemoveAllWhenShown() {
		return removeAllWhenShown;
	}
	/**
	 * Notifies all listeners that this menu is about to appear.
	 */
	private void handleAboutToShow() {
		if (removeAllWhenShown)
			removeAll();
		fireAboutToShow(this);
		update(false);
	}
	/**
	 * Initializes the menu control.
	 */
	private void initializeMenu() {
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				handleAboutToShow();
			}
			public void menuHidden(MenuEvent e) {
				//			ApplicationWindow.resetDescription(e.widget);
			}
		});
		markDirty();
		// Don't do an update(true) here, in case menu is never opened.
		// Always do it lazily in handleAboutToShow().
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isDynamic() {
		return false;
	}
	/**
	 * Returns whether this menu should be enabled or not.
	 * Used to enable the menu item containing this menu when it is realized as a sub-menu.
	 * <p>
	 * The default implementation of this framework method
	 * returns <code>true</code>. Subclasses may reimplement.
	 * </p>
	 *
	 * @return <code>true</code> if enabled, and
	 *   <code>false</code> if disabled
	 */
	public boolean isEnabled() {
		return true;
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isGroupMarker() {
		return false;
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isSeparator() {
		return false;
	}
	/**
	 * @param item
	 * @return <code>boolean</code>
	 */
	public boolean isSubstituteFor(IContributionItem item) {
		return this.equals(item);
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isVisible() {
		return visible;
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void removeMenuListener(IMenuListener listener) {
		listeners.remove(listener);
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void setRemoveAllWhenShown(boolean removeAll) {
		this.removeAllWhenShown = removeAll;
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void setParent(IContributionManager manager) {
		// empty method
	}

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void update() {
		updateMenuItem();
	}
	/**
	 * The <code>MenuManager</code> implementation of this <code>IContributionManager</code>
	 * updates this menu, but not any of its submenus.
	 *
	 * @see #updateAll
	 */
	public void update(boolean force) {
		update(force, false);
	}
	
	/** 
	 * Tests the supplied items <code>isVisible()</code> method.
	 * This method may be overridden to provide more detailed testing.  
	 * @param ci
	 * @return <tt>true</tt> if the supplied item is visible, otherwise <tt>false</tt>.
	 */
	protected boolean shouldDisplay( IContributionItem ci ) { return ci.isVisible(); }
			
	
	
	/**
	 * Incrementally builds the menu from the contribution items.
	 * This method leaves out double separators and separators in the first 
	 * or last position.
	 *
	 * @param force <code>true</code> means update even if not dirty,
	 *   and <code>false</code> for normal incremental updating
	 * @param recursive <code>true</code> means recursively update 
	 *   all submenus, and <code>false</code> means just this menu
	 */
	protected void update(boolean force, boolean recursive) {
		if (isDirty() || force) {
			if (menu != null && !menu.isDisposed()) {

				// clean contains all active items without double separators
				IContributionItem[] items = getItems();
				List clean = new ArrayList(items.length);
				IContributionItem separator = null;
				for (int i = 0; i < items.length; ++i) {
					IContributionItem ci = items[i];
					if ( !shouldDisplay(ci) ) { 
						continue; 
					}
					if (ci.isSeparator()) {
						// delay creation until necessary 
						// (handles both adjacent separators, and separator at end)
						separator = ci;
					} else {
						if (separator != null) {
							if (clean.size() > 0) // no separator if first item
								clean.add(separator);
							separator = null;
						}
						clean.add(ci);
					}
				}

				// remove obsolete (removed or non active)
				Item[] mi = menu.getItems();
				for (int i = 0; i < mi.length; i++) {
					Object data = mi[i].getData();
					if (data == null
						|| !clean.contains(data)
						|| (data instanceof IContributionItem
							&& ((IContributionItem) data).isDynamic()))
						mi[i].dispose();
				}

				// add new
				mi = menu.getItems();
				int srcIx = 0;
				int destIx = 0;
				for (Iterator e = clean.iterator(); e.hasNext();) {
					IContributionItem src = (IContributionItem) e.next();
					IContributionItem dest;

					// get corresponding item in SWT widget
					if (srcIx < mi.length)
						dest = (IContributionItem) mi[srcIx].getData();
					else
						dest = null;

					if (dest != null && src.equals(dest)) {
						srcIx++;
						destIx++;
					} else if (
						dest != null
							&& dest.isSeparator()
							&& src.isSeparator()) {
						mi[srcIx].setData(src);
						srcIx++;
						destIx++;
					} else {
						int start = menu.getItemCount();
						src.fill(menu, destIx);
						int newItems = menu.getItemCount() - start;
						Item[] tis = menu.getItems();
						for (int i = 0; i < newItems; i++)
							tis[destIx + i].setData(src);
						destIx += newItems;
					}

					// May be we can optimize this call. If the menu has just
					// been created via the call src.fill(fMenuBar, destIx) then
					// the menu has already been updated with update(true) 
					// (see MenuManager). So if force is true we do it again. But
					// we can't set force to false since then information for the
					// sub sub menus is lost.
					if (recursive) {
						if (src instanceof IMenuManager)
							 ((IMenuManager) src).updateAll(force);
					}

				}

				setDirty(false);

				updateMenuItem();
			}
		} else {
			// I am not dirty. Check if I must recursivly walk down the hierarchy.
			if (recursive) {
				IContributionItem[] items = getItems();
				for (int i = 0; i < items.length; ++i) {
					IContributionItem ci = items[i];
					if (ci instanceof IMenuManager) {
						IMenuManager mm = (IMenuManager) ci;
						if (mm.isVisible()) {
							mm.updateAll(force);
						}
					}
				}
			}
		}
	}

	public void update(String property) {
		IContributionItem items[] = getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].update(property);
		}
		if (menu != null && (IAction.TEXT.equals(property))) {
			String text = getOverrides().getText(this);
			if (text == null)
				text = menuText;
			if (menu == null || menu.isDisposed())
				return;
			if ((text != null) && (menu.getParentItem() != null))
				menu.getParentItem().setText(text);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void updateAll(boolean force) {
		update(force, true);
	}
	/**
	 * Updates the menu item for this sub menu.
	 * The menu item is disabled if this sub menu is empty.
	 * Does nothing if this menu is not a submenu.
	 */
	private void updateMenuItem() {
		if (menuItem != null && !menuItem.isDisposed()) {
			boolean enabled = menu.getItemCount() > 0;
			// Workaround for 1GDDCN2: SWT:Linux - MenuItem.setEnabled() always causes a redraw
			if (menuItem.getEnabled() != enabled)
				menuItem.setEnabled(enabled);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar, int)
	 */
	public void fill(CoolBar parent, int index) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
	 */
	public void saveWidgetState() {
		// TODO Auto-generated method stub
		
	}
}
