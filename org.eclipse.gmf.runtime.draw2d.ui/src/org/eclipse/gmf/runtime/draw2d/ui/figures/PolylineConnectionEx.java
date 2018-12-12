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

package org.eclipse.gmf.runtime.draw2d.ui.figures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.draw2d.ArrowLocator;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.RoutingAnimator;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.LineSeg;
import org.eclipse.gmf.runtime.draw2d.ui.geometry.PointListUtilities;
import org.eclipse.gmf.runtime.draw2d.ui.internal.figures.ConnectionLayerEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.figures.DelegatingLayout;
import org.eclipse.gmf.runtime.draw2d.ui.internal.routers.OrthogonalRouter;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;


/**
 * This is an extension of the <code>PolylineConnection</code> class to support avoid obstructions, smoothness
 * and jump-links behavior.
 * 
 * @author sshaw
 */
public class PolylineConnectionEx extends PolylineConnection implements IPolygonAnchorableFigure {
	
	private RotatableDecoration startDecoration, endDecoration;

    private static Rectangle LINEBOUNDS = Rectangle.SINGLETON;
    private static int TOLERANCE = 3;
    
    private static Rectangle EMPTY_BOUNDS = new Rectangle(0,0,0,0);
    
    /**
     * No smoothness
     */
    public static final int SMOOTH_NONE = 0x0000;
    
    /**
     * Some smoothness
     */
    public static final int SMOOTH_LESS = 0x0010;
    
    /**
     * Normal smoothness
     */
    public static final int SMOOTH_NORMAL = 0x0020;
    
    /**
     * More exagerated smoothness
     */
    public static final int SMOOTH_MORE = 0x0040;

    /**
     * Flag indicates whether the connection will attempt to "jump"
     * other connection below it in the z-order.
     */
    public static final int JUMPLINK_FLAG_BELOW = 0x4000; // jump links below
    
    /**
     * Flag indicates whether the connection will attempt to "jump"
     * other connection above it in the z-order.
     */
    public static final int JUMPLINK_FLAG_ABOVE = 0x8000; // jump links above
    
    /**
     * Flag indicates whether the connection will attempt to "jump"
     * all other connections regardless of z-order.
     */
    public static final int JUMPLINK_FLAG_ALL =
        JUMPLINK_FLAG_BELOW | JUMPLINK_FLAG_ABOVE;

    private static final int SMOOTH_FACTOR_LESS = 15;
    private static final int SMOOTH_FACTOR_NORMAL = 30;
    private static final int SMOOTH_FACTOR_MORE = 50;

    // extra routing styles
    // avoid intersection with other nodes
    private static final int ROUTE_AVOID_OBSTACLE = 0x0100;
    // always use closest route to the destination
    private static final int ROUTE_CLOSEST_ROUTE = 0x0200;    
    // jump other links
    private static final int ROUTE_JUMP_LINKS = 0x0400; 

    // jump link flags
    private static final int JUMPLINK_FLAG_SMOOTH = 0x0800;
    // indicates whether the jump links are smooth or not
    private static final int JUMPLINK_FLAG_ANGLEIN = 0x1000;
    // indicates whether the link is angled inwards
    private static final int JUMPLINK_FLAG_ONBOTTOM = 0x2000;

    private static final int JUMPLINK_DEFAULT_SMOOTHNESS = 30;
    
	// round bendpoints radius (applicable only for rectilinear routing with no
	// smoothness) default value 0 means that bendpoints are not rounded
	private int roundedBendpointsRadius = 0;
	// if rounding bendpoints fails (it shouldn't happen), then roundedBendpointsRadius is set to 0 so
	// connection can be routed without rounded corners, and origRoundedBendpointsRad gets
	// the original value of roundedBendpointsRadius (so it can be used in future attempts)
	private int origRoundedBendpointsRad = 0;
    
    private long styleBits;
    private JumpLinkSet jumpLinkSet;
    private Hashtable connectionAnchors;
    /**
     * When rounded bendpoints is turned on, keeping track of arcs that have smaller size than the default 
     */
    private Hashtable<Integer, Integer> rForBendpointArc;
	
    static private final String szAnchor = ""; //$NON-NLS-1$
    
    /**
     * A dimension used by the isFeedbackLayer() method to check if we are
     * on a feedback layer.
     */
    private static final Dimension dimCheck = new Dimension(100, 100);
    
    /**
     * This method checks if we are on a feedback layer by comparing
     * the value of a Dimension with the value after translating it
     * into relative coordinates.
     * 
     * @return true if we are on a feedback layer, which means the
     * results after translating were the same as not translating, or false
     * if we are not on a feedback layer.
     */
    private boolean isFeedbackLayer() {
    	Dimension copied = dimCheck.getCopy();
    	translateToRelative(copied);
    	return dimCheck.equals(copied);
    }
    
    /**
     * 
     */
    public PolylineConnectionEx() {
        styleBits =
                JUMPLINK_FLAG_BELOW
                | JUMPLINK_FLAG_SMOOTH
                | JUMPLINK_FLAG_ANGLEIN;            
        setLayoutManager(new DelegatingLayout());
        addRoutingListener(RoutingAnimator.getDefault());
    }
    
    /**
     * Provides a utility function for dirtying the jump links and repainting the line.
     */
    public void refreshLine() {
        dirtyJumpLinks();
        repaint();
    }

    /**
     * Add a point to the polyline connection.
     */
    public void addPoint(Point pt) {
        super.addPoint(pt);
        refreshLine();
    }

    /**
     * Calculate and store the tolerance value for determining whether the line contains a point or not.
     * 
     * @param isFeedbackLayer  see the isFeedbackLayer() method
     */
    private int calculateTolerance(boolean isFeedbackLayer) {
		Dimension absTol = new Dimension(TOLERANCE, 0);
		
    	if (!isFeedbackLayer) {
    		MapModeUtil.getMapMode(this).DPtoLP(absTol);
    	}

    	return absTol.width + lineWidth / 2;
    }
    
    /**
     * Returns the bounds which holds all the points in this
     * polyline connection. Returns any previously existing
     * bounds, else calculates by unioning all the children's
     * dimensions.
     *
     * @return  Bounds to hold all the points.
     */
    public Rectangle getBounds(){
        if (bounds == null) {
            if (getSmoothFactor() != 0) {
                bounds = getSmoothPoints().getBounds();
                bounds.expand(lineWidth/2, lineWidth/2);
                
                for(int i=0; i<getChildren().size(); i++) {
                    IFigure child = (IFigure)getChildren().get(i);
                    bounds.union(child.getBounds());
                }
            }
            else
                super.getBounds();
            
            boolean isFeedbackLayer = isFeedbackLayer();
            int calculatedTolerance = calculateTolerance(isFeedbackLayer);
            Dimension jumpLinkSize = calculateJumpLinkSize(isFeedbackLayer);
            
            // extend the boundary slightly by the jumplinks height value
            bounds.expand(jumpLinkSize.height + calculatedTolerance, jumpLinkSize.height + calculatedTolerance);
        }
        return getSourceAnchor() != null && getTargetAnchor() != null ? bounds : EMPTY_BOUNDS;
    }

    /**
     * Method getShallowBounds.
     * @return Rectangle
     */
    public Rectangle getSimpleBounds(){
        Point s = getStart();
        Point e = getEnd();
        Point start = new Point(Math.min(s.x, e.x), Math.min(s.y, e.y));
        Dimension d = new Dimension(Math.abs(s.x-e.x), Math.abs(s.y-e.y));
        return new Rectangle(start.x, start.y, d.width, d.height);
    }

    /**
     * Determine if the polyline connection contains a given point.
     * @param x int value of the point to check containment of
     * @param y int value of the point to check containment of.
     * @return boolean true indicating containment, false otherwise.
     */
    public boolean containsPoint(int x, int y) {
    
    	boolean isFeedbackLayer = isFeedbackLayer();
    	int calculatedTolerance = calculateTolerance(isFeedbackLayer);
    
        LINEBOUNDS.setBounds(getBounds());
        LINEBOUNDS.expand(calculatedTolerance,calculatedTolerance);
        if (!LINEBOUNDS.contains(x, y))
            return false;
    
    	int ints[] = getSmoothPoints().toIntArray();
    	for (int index = 0; index < ints.length - 3; index  += 2) {
    		if (lineContainsPoint(ints[index], ints[index + 1],
    			ints[index + 2], ints[index + 3], x, y,
				isFeedbackLayer))
    			return true;
    	}
    	List children = getChildren();
    	for (int i = 0; i < children.size(); i++) {
    		if (((IFigure)children.get(i)).containsPoint(x, y))
    			return true;
    	}
    	
        return false;
    }
    
    private boolean lineContainsPoint(
        int x1, int y1,
        int x2, int y2,
        int px, int py,
		boolean isFeedbackLayer) {
        LINEBOUNDS.setSize(0,0);
        LINEBOUNDS.setLocation(x1,y1);
        LINEBOUNDS.union(x2,y2);
        int calculatedTolerance = calculateTolerance(isFeedbackLayer);
        LINEBOUNDS.expand(calculatedTolerance,calculatedTolerance);
        if (!LINEBOUNDS.contains(px,py))
            return false;
    
        double v1x, v1y, v2x, v2y;
        double numerator, denominator;
        double result = 0;
    
        if( x1 != x2 && y1 != y2 ) {
            v1x = (double)x2 - x1;
            v1y = (double)y2 - y1;
            v2x = (double)px - x1;
            v2y = (double)py - y1;
            
            numerator = v2x * v1y - v1x * v2y;
            
            denominator = v1x * v1x + v1y * v1y;
    
            result = numerator * numerator / denominator;
        }
        
        // if it is the same point, and it passes the bounding box test,
        // the result is always true.
        return result <= calculatedTolerance * calculatedTolerance;
                             
    }
    
    /**
     * Calculate the line segment index for a given point.  This is important
     * for the drag tracker that add's bendpoints on a connection.
     * 
     * @param x the x value in relative coordinates
     * @param y the y value in relative coordinates
     * @return the index of the line segment that is nearest to the given point.
     */
    public int findLineSegIndexOfPoint(int x, int y) {
        calculateTolerance(isFeedbackLayer());

        return PointListUtilities.findNearestLineSegIndexOfPoint(getPoints(), new Point(x, y));
    }
    
	/**
	 * Returns points for this connection. If the smooth factor is turned on,
	 * then calculate the approximated smooth polyline for display or other
	 * purposes. If bendpoints need to be rounded, calculates points taking that
	 * into account. In that case, if calculateAprox is true, it will also
	 * calculate points that approximate corner arcs. If it is false, it will
	 * replace each bendpoint with two points that represent start and end of an
	 * arc. In any case, calculated points are not persisted.
	 * 
	 * @param calculateAppox
	 *            If true, and rounding bendpoints is on, then calculate points
	 *            that approximate bendpoint arc.
	 * @return Resulting <code>PointList</code>. In case when sooth factor is
	 *         on, it is a polyline approximation of a bezier curve calculated
	 *         based on the smoothness factor.
	 * @since 1.2
	 */
    public PointList getSmoothPoints(boolean calculateAppox) {
        if (getSmoothFactor() > 0) {
            return PointListUtilities.calcSmoothPolyline(getPoints(), getSmoothFactor(), PointListUtilities.DEFAULT_BEZIERLINES);
        } else if (isRoundingBendpoints()){
        	PointList result = getRoundedCornersPoints(calculateAppox);
        	if (result == null) {
        		// this shouldn't happen, but if it does happen that rounded corners
        		// cannot be calculated set radius to 0 and use original bendpoints
        		// (will produce non-rounded rectangular connection)
        		origRoundedBendpointsRad = roundedBendpointsRadius;
        		roundedBendpointsRadius = 0;
        		result = PointListUtilities.copyPoints(getPoints());
        	}
        	return result;
        } else {
        	return PointListUtilities.copyPoints(getPoints());
        }
    }
    
    /**
     * See {@link #getSmoothPoints(boolean calculateAppoxPoints)}
     */
    public PointList getSmoothPoints() {
    	return getSmoothPoints(true);
    }    

    /**
     * Insert a point at the given index into the polyline connection.
     */
    public void insertPoint(Point pt, int index) {
        super.insertPoint(pt, index);
        refreshLine();
    }

    /**
     * Override the figure method "outlineShape" to draw the actual polyline connection shape.
     * Special code to regenerate the jumplinks, draw the polyline smooth, and round the bendpoints
     * is also done during this method call.
     */
    protected void outlineShape(Graphics g) {

        PointList displayPoints = getSmoothPoints(false);        
        
        Hashtable<Point, Integer> originalDisplayPoints = null;
        if (isRoundingBendpoints()) {
            // If rounded bendpoints feature is turned on, remember the original points, will be 
            // needed later.        	
        	originalDisplayPoints = new Hashtable<Point, Integer>();   
        	for (int i = 0; i < displayPoints.size(); i++) {
        		originalDisplayPoints.put(displayPoints.getPoint(i), new Integer(i));        
        	}
        }
        
        int incline = calculateJumpLinkIncline(isFeedbackLayer());
        
        if (shouldJumpLinks()) {
       	            
        	regenerateJumpLinks();

            JumpLinkSet pJumpLinkSet = getJumpLinkSet();
            if (pJumpLinkSet != null && pJumpLinkSet.m_pJumpLinks != null) {
                int nSmoothNess = 0;
                if (isJumpLinksSmooth())
                    nSmoothNess = JUMPLINK_DEFAULT_SMOOTHNESS;

                boolean bOnBottom = isJumpLinksOnBottom();

                ListIterator linkIter = pJumpLinkSet.m_pJumpLinks.listIterator();
                
                while (linkIter.hasNext()) {
                    JumpLink pJumpLink = (JumpLink) linkIter.next();

                    PointList jumpLinkPoints = PointListUtilities.routeAroundPoint(
                        displayPoints,
                        pJumpLink.m_ptIntersect,
                        pJumpLink.m_nHeight,
                        pJumpLink.m_nWidth,
                        nSmoothNess,
                        incline,
                        !bOnBottom);
                    if (jumpLinkPoints != null)
                    	displayPoints = jumpLinkPoints;
                }
            }
        }
        if (!isRoundingBendpoints()) {
        	g.drawPolyline(displayPoints);
        	if (origRoundedBendpointsRad > 0) {
        		// we unsuccessfully tried to do rounding, allow the next routing to try again.
        		roundedBendpointsRadius = origRoundedBendpointsRad;
        		origRoundedBendpointsRad = 0;
        	}        	
        } else {
        	// In originalDisplayPoints, each bendpoint will be replaced with two points: start and end point of the arc.
        	// If jump links is on, then displayPoints will also contain points identifying jump links, if any.
        	int i = 1;
        	int rDefault = getRoundedBendpointsRadius();
        	while (i < displayPoints.size() - 1) {
        		// Consider points at indexes i-1, i, i+1.
        		int x0 = 0, y0 = 0;
        		boolean firstPointAssigned = false;
        		if (shouldJumpLinks()) {
        			boolean containsPoint2;
        			boolean containsPoint3;
        			PointList jumpLinkPoints = new PointList();
        			do {
        	       		// First, check if point at index i or i+1 is start point of a jump link (if the point is not in original points).
                		// If so, draw a polyline ending at the last point of the jump link.           				
        				containsPoint2 = true;
        				containsPoint3 = true;
        				if (i < displayPoints.size()) {
        					containsPoint2 = originalDisplayPoints.containsKey(displayPoints.getPoint(i));
        					if (i < displayPoints.size() - 1) {
        						containsPoint3 = originalDisplayPoints.containsKey(displayPoints.getPoint(i+1));
        					}
        				}
        				if (!containsPoint2 || !containsPoint3) {
        					// start adding jump link points
        					jumpLinkPoints.addPoint(displayPoints.getPoint(i-1));
        					Point p; // next point to be added to jump link points
        					int j; // index of the next point in jump link
        					if (!containsPoint2) {
        						// jump link starts somewhere on the segment before the arc begins
        						j = i;
        						p = displayPoints.getPoint(i);        			
        					} else {
        						// jump link starts within an arc; it means that one part of the arc will be replaced with 
        						// a segment between points at i and i-1, and another part is replaced with the jump link,
        						j = i + 1; 
        						p = displayPoints.getPoint(i+1);        			
        						jumpLinkPoints.addPoint(displayPoints.getPoint(i));
        					}
        					do {
        						jumpLinkPoints.addPoint(p);
        						j++;
        						p = displayPoints.getPoint(j);
        					} while (!originalDisplayPoints.containsKey(p) && j < displayPoints.size() - 1);
        					// Now, check if p is start point of a line segment or an arc. In first case, index of p in 
        					// the original list is even, in second it's odd.
        					int origIndex = ((Integer)originalDisplayPoints.get(p)).intValue();
        					firstPointAssigned = false;
        					if (origIndex % 2 == 0) {
        						// p is start point of a segment, it means that the jump link finished somewhere within the arc,
        						// so one part of the arc is replaced with the jump link, and another will be replaced with a segment.
        						jumpLinkPoints.addPoint(p);
        						i = j + 1; 
        					} else {
        						// p is start point of an arc, the last point in jump link polyline becomes the first point for 
        						// the drawing that follows (p could be the last point too)
        						x0 = jumpLinkPoints.getLastPoint().x;
        						y0 = jumpLinkPoints.getLastPoint().y;
        						i = j;             			
        						firstPointAssigned = true;        				
        					}
        					// Reason for the loop: we cannot be sure that points at newly founded i=j+1, or i+1 (when i == j) are
        					// not starting points of a new jump link;
        					// in most of the cases, though, the loop will execute just one step 
        					// Jump link algorithm sometimes inserts duplicate points, we need to get rid of those.
        					while (i < displayPoints.size() - 1 && 
        							displayPoints.getPoint(i).equals(displayPoints.getPoint(i+1))) {
        						i++;
        					}
        				}
        			} while (!containsPoint2 || !containsPoint3);
        			if (jumpLinkPoints != null) {
        				// draw jump link
        				g.drawPolyline(jumpLinkPoints);
        			}
        		}
        		if (i < displayPoints.size() - 1) { // if we still didn't reach the end after drawing jump link polyline
        			// Draw a segment starting at index i-1 and ending at index i, 
        			// and arc with starting point at index i and ending point at index i+1.
        			// But first, find points at i-1, i and i+1.
        			if (!firstPointAssigned) {
        				x0 = displayPoints.getPoint(i-1).x; 
        				y0 = displayPoints.getPoint(i-1).y;
        			}
        			int x1;; 
        			int y1;
        			// If points at i-1 and i are equal (could happen if jump link algorithm 
        			// inserts a point that already exists), just skip the point i        		
        			while (i < displayPoints.size() - 1 && 
        					x0 == displayPoints.getPoint(i).x && y0 == displayPoints.getPoint(i).y) {
        				i++;
        			}
        			if (i < displayPoints.size() - 1) {
        				x1 = displayPoints.getPoint(i).x; 
        				y1 = displayPoints.getPoint(i).y;
        			} else {
        				break;
        			}
        			// The same goes for point at i and i+1         		
        			int x2;
        			int y2;        		
        			while (i + 1 < displayPoints.size() - 1 && 
        					x1 == displayPoints.getPoint(i+1).x && y1 == displayPoints.getPoint(i+1).y) {
        				i++;
        			}
        			if (i < displayPoints.size() - 1) {
        				x2 = displayPoints.getPoint(i+1).x; 
        				y2 = displayPoints.getPoint(i+1).y;
        			} else {
        				break;
        			}
        			// Draw the segment
        			g.drawLine(x0, y0, x1, y1);

        			// Find out if arc size is default, or if it had to be decreased because of lack of space
        			int r = rDefault;         			
        			Point p = displayPoints.getPoint(i);
        			int origIndex = ((Integer)originalDisplayPoints.get(p)).intValue();       	
        			Object o = rForBendpointArc.get(new Integer((origIndex+1)/2));
        			if (o != null) {
        				r = ((Integer)o).intValue();
        			}

        			// Find out the location of enclosing rectangle (x, y), as well as staring angle of the arc.
        			int x, y;
        			int startAngle;        		
        			if (x0 == x1 && x1 < x2) {
        				x = x1;
        				y = y1 - r;
        				if (y1 > y2) {
        					startAngle = 90;
        				} else {
        					startAngle = 180;
        				}
        			} else if (x0 > x1 && x1 > x2) {
        				x = x2; 
        				y = y2 - r;
        				if (y1 > y2) {
        					startAngle = 180;
        				} else {
        					startAngle = 90;
        				}        		
        			} else  if (x0 == x1  && x1 > x2) {
        				if (y1 > y2) {
        					x = x2 - r;
        					y = y2;
        					startAngle = 0;
        				} else {
        					x = x1 - 2*r;
        					y = y1 - r;
        					startAngle = 270;
        				}        		        		
        			} else { // x0 < x1 && x1 < x2
        				if (y1 > y2) {
        					x = x2 - 2*r;
        					y = y2 - r;
        					startAngle = 270;
        				} else {
        					x = x1 - r;
        					y = y1;
        					startAngle = 0;
        				}        		        		         		
        			}
        			// Draw the arc.
        			g.drawArc(x, y, 2*r, 2*r, startAngle, 90);
        			i+=2;
        		}
        	}
        	// Draw the last segment.
        	g.drawLine(displayPoints.getPoint(displayPoints.size()-2), displayPoints.getLastPoint());
        }
    }
    
    /**
     * Set the line width of the polyline connection.
     */
    public void setLineWidth(int w) {
        bounds = null;
        super.setLineWidth(w);
    }

    /**
     * Sets the list of points to be used by this polyline connection.
     * Removes any previously existing points. 
     *
     * @param points  New set of points.
     */
    public void setPoints(PointList points) {
        super.setPoints(points);

        dirtyAllJumpLinks();
        refreshLine();
    }
    
    /**
     * Calculate the jumplink dimensions.
     */
    private static final int JUMPLINK_DEFAULT_WIDTH = 25;
    private static final int JUMPLINK_DEFAULT_HEIGHT = 10;
   
    /**
     * Calculate the size of the jump link.
     * 
     * @param isFeedbackLayer the <code>boolean</code> that determines if mapping of the coordinates will occur.  
     * This is necessary since the feedback layer doesn't not go through the zooming or mapmode scaling.
     * 
     * @return <code>Dimension</code> that is the jump link size
     */
    private Dimension calculateJumpLinkSize(boolean isFeedbackLayer) {
    	Dimension jumpDim = new Dimension(JUMPLINK_DEFAULT_WIDTH, JUMPLINK_DEFAULT_HEIGHT);
    	
    	if (!isFeedbackLayer) {
    		MapModeUtil.getMapMode(this).DPtoLP(jumpDim);
    	}
    	
        return jumpDim;
    }
    
    /**
     * Calculate the jumplink incline
     * 
     * @param isFeedbackLayer the <code>boolean</code> that determines if mapping of the coordinates will occur.  
     * This is necessary since the feedback layer doesn't not go through the zooming or mapmode scaling.
     */
    private int calculateJumpLinkIncline(boolean isFeedbackLayer) {
    	if (isJumpLinksAngledIn())
        	return calculateJumpLinkSize(isFeedbackLayer).width / 5;
    	
    	return 0;
    }
    
    /**
     * Dirty all connection jump links in the diagram
     */
    public void dirtyAllJumpLinks()
    {
        IFigure pParent = getParent();
        if (pParent instanceof ConnectionLayerEx)
            ((ConnectionLayerEx)pParent).dirtyJumpLinks(getBounds());
    }
     
    /**
     * Inner class for storing the specific JumpLink information.
     */
    protected class JumpLink {
        
        /**
         * intersection point value
         */
        public Point m_ptIntersect;
        
        /**
         * width of the jump link
         */
        public int m_nWidth;
        
        /**
         * height of the jump link
         */
        public int m_nHeight;
  
        /**
         * distance along the polyline
         */
        public int m_nDistance;
    }

    /**
     * Inner class for storing the set of JumpLink's associated with the
     * polyline connection.
     */
    protected class JumpLinkSet {

        /**
         * Default constructor
         */
        public JumpLinkSet() {
            m_bDirty = true;
            m_pJumpLinks = null;
        }
        
        /**
         * Determines if the jump links need to be regenerated.
         * 
         * @return <code>boolean</code> <code>true</code> if jump links need to be regenerated, <code>false</code> otherwise.
         */
        public boolean isDirty() {
            return m_bDirty;
        }
        
        /**
         * Sets the dirty flag back to false and notifies the connection layer
         * that it has been cleaned.
         * 
         * @param connect <code>Connection</code> whose jump links need to be regenerated.
         */
        protected void cleanJumpLinks(Connection connect) {
            m_bDirty = false;
            
            IFigure pParent = connect.getParent();
            if (pParent instanceof ConnectionLayerEx)
                ((ConnectionLayerEx)pParent).cleanJumpLinks();
        }
        
        /** 
         * Sets the jump links set as being dirty so that they will be regenerated
         * again at display time.
         */
        public void dirtyJumpLinks() {
            m_bDirty = true;
        }
        
        /**
         * Regenerates the jump links again according to the current arrangement of connections
         * on the diagram.
         * 
         * @param connect <code>Connection</code> whose jump links need to be regenerated.
         * @return <code>boolean</code> <code>true</code> if jump links were regenerated, <code>false</code> otherwise.
         */
        public boolean regenerateJumpLinks(Connection connect) {
            // check if we need to regenerate the jump link info
            if (isDirty()) {
                // regenerate the points where jump links will occur    
                calculateIntersections(connect);

                cleanJumpLinks(connect);
                
                return true;
            }

            return false;
        }

        /**
         * Inner class useed to compare two jump links to see which is further
         * along the polyline connection.
         */
        private class CompareDistance implements Comparator {
           
            public int compare(Object obj1, Object obj2) {
                JumpLink j1 = (JumpLink) obj1;
                JumpLink j2 = (JumpLink) obj2;

                if (j1.m_nDistance < j2.m_nDistance)
                    return -1;
                else
                    if (j1.m_nDistance > j2.m_nDistance)
                        return 1;

                return 0;
            }
        }

        /**
         * Sort the jump links according to their distance along the polyline
         * connection line.
         */
        private void sortByDistance() {

            Object[] jumpArray = m_pJumpLinks.toArray();
            Arrays.sort(jumpArray, new CompareDistance());

            for (int i = 0; i < m_pJumpLinks.size(); i++)
                m_pJumpLinks.set(i, jumpArray[i]);
        }
        
        /**
         * Calculate the intersections that occur between this connection and all the
         * other connections on the diagram.
         * 
         * @param connect <code>Connection</code> to calculate intersections with other connections in the layer.
         */
        private void calculateIntersections(Connection connect) {
            // regenerate the points where jump links will occur
            IFigure pParent = connect.getParent();

            if (m_pJumpLinks != null)
                m_pJumpLinks = null;

            PointList tmpLine = getSmoothPoints();

            long jumpType = (styleBits & JUMPLINK_FLAG_ALL);

            // only check intersections with connect views which are below this one.
            List children = pParent.getChildren();
            int nIndex = children.indexOf(connect);

            ListIterator childIter = children.listIterator();
            boolean bForwards = true;
            if (jumpType != JUMPLINK_FLAG_ALL)
            {
                childIter = children.listIterator(nIndex);
                if (jumpType == JUMPLINK_FLAG_BELOW)
                    bForwards = false;
            }
            
            boolean isFeedbackLayer = isFeedbackLayer();
            Dimension jumpLinkSize = calculateJumpLinkSize(isFeedbackLayer);
            
            while (bForwards ? childIter.hasNext() : childIter.hasPrevious()) {
                IFigure figure =
                    (IFigure) (bForwards ? childIter.next() : childIter.previous());
                PointList checkLine = null;

                if (figure != connect) {
                    if (figure instanceof PolylineConnectionEx)
                        checkLine = ((PolylineConnectionEx) figure).getSmoothPoints();
                    else
                        if (figure instanceof Connection)
                            checkLine = PointListUtilities.copyPoints(((Connection) figure).getPoints());

                    if (checkLine != null) {
                        PointList intersections = new PointList();
                        PointList distances = new PointList();
    
                        if (m_pJumpLinks == null)
                            m_pJumpLinks = new ArrayList(intersections.size());
    
                        if (PointListUtilities.findIntersections(tmpLine, checkLine, intersections, distances)) {
                            for (int i = 0; i < intersections.size(); i++) {
                                double dist1 = intersections.getPoint(i).getDistance(tmpLine.getFirstPoint());
                                double dist2 = intersections.getPoint(i).getDistance(tmpLine.getLastPoint());
                                double dist3 = intersections.getPoint(i).getDistance(checkLine.getFirstPoint());
                                double dist4 = intersections.getPoint(i).getDistance(checkLine.getLastPoint());
                                double minDist = Math.min(Math.min(dist1,dist2), Math.min(dist3,dist4));
                                if (minDist > jumpLinkSize.width/2){
                                    addJumpLink(intersections.getPoint(i), distances.getPoint(i).x, isFeedbackLayer);
                                }
                            }
                        }
                    }
                }
            }

            // check to see if we need to combine intersects that are close together
            combineCloseLinks(tmpLine);
        }

        /**
         * Add a new jump with the given intersection point and distance along the polyline
         * connection line.
         * @param ptIntersect
         * @param nDistance
         * @param isFeedbackLayer see the isFeedbackLayer() method
         */
        private void addJumpLink(Point ptIntersect, int nDistance, boolean isFeedbackLayer) {
            JumpLink pNewJumpLink = new JumpLink();
            pNewJumpLink.m_ptIntersect = new Point(ptIntersect);
            
            Dimension jumpLinkSize = calculateJumpLinkSize(isFeedbackLayer);
            
            pNewJumpLink.m_nWidth = jumpLinkSize.width;
            pNewJumpLink.m_nHeight = jumpLinkSize.height;
            pNewJumpLink.m_nDistance = nDistance;

            m_pJumpLinks.add(pNewJumpLink);
        }

        /**
         * If there are two consecutive jump links that are close together with a certain
         * tolerance value, then combine them into one larger jump link.
         * 
         * @param tmpLine the <code>PointList</code> 
         */
        private void combineCloseLinks(PointList tmpLine) {

            if (m_pJumpLinks == null || m_pJumpLinks.size() < 2)
                return;

            Dimension jumpLinkSize = calculateJumpLinkSize(isFeedbackLayer());
            int nCurrentWidth = jumpLinkSize.width;
            ArrayList jumpLinks = new ArrayList(m_pJumpLinks.size());

            // sort the jump links by distance
            sortByDistance();

            jumpLinks.addAll(m_pJumpLinks);
            m_pJumpLinks.clear();

            ListIterator linkIter = jumpLinks.listIterator();

            // combine intersects that are close together and increase jump link width
            JumpLink pLastJumpLink = (JumpLink) linkIter.next();
            JumpLink pPrevJumpLink = null;

            final int nDeltaMin = jumpLinkSize.width * 4 / 3;

            while (pLastJumpLink != null) {
                JumpLink pJumpLink = null;
                int nDelta = 0;

                if (linkIter.hasNext()) {
                    pJumpLink = (JumpLink) linkIter.next();
                    nDelta = pJumpLink.m_nDistance - pLastJumpLink.m_nDistance;
                }

                if ((nDelta > nDeltaMin) || pJumpLink == null) {
                    JumpLink pNewJumpLink = new JumpLink();

                    pNewJumpLink.m_nHeight = jumpLinkSize.height;
                    pNewJumpLink.m_nWidth = nCurrentWidth;
                    pNewJumpLink.m_nDistance = 0;
                    pNewJumpLink.m_ptIntersect = new Point(pLastJumpLink.m_ptIntersect);

                    if (pPrevJumpLink != null) {
                        // need to recalc the intersection point
                        long nNewDistance =
                            pPrevJumpLink.m_nDistance
                                + ((pLastJumpLink.m_nDistance - pPrevJumpLink.m_nDistance) / 2);
                        pNewJumpLink.m_ptIntersect = new Point();
                        PointListUtilities.pointOn(tmpLine, nNewDistance, LineSeg.KeyPoint.ORIGIN, pNewJumpLink.m_ptIntersect);
                    }

                    m_pJumpLinks.add(pNewJumpLink);
                    nCurrentWidth = jumpLinkSize.width;
                    pPrevJumpLink = null;
                } else {
                    if (pPrevJumpLink == null)
                        pPrevJumpLink = pLastJumpLink;
                    nCurrentWidth += jumpLinkSize.width - (nDeltaMin - nDelta);
                }

                pLastJumpLink = pJumpLink;
            }
        }

        private boolean m_bDirty;
        
        private List m_pJumpLinks;
    }

    /**
     * Get the smoothness factor for the polyline connection.  A value of 0
     * indicates that there is no smoothness.
     * 
     * @return the value is one of 0 - no smoothing, SMOOTH_FACTOR_LESS - rounded edges, 
     * SMOOTH_FACTOR_NORMAL - more curved look, SMOOTH_FACTOR_MORE - exagerated curving
     */
    private final int getSmoothFactor() {
        int smoothStyle = getSmoothness();

        if (smoothStyle == SMOOTH_LESS)
            return SMOOTH_FACTOR_LESS;
        else
            if (smoothStyle == SMOOTH_NORMAL)
                return SMOOTH_FACTOR_NORMAL;
            else
                if (smoothStyle == SMOOTH_MORE)
                    return SMOOTH_FACTOR_MORE;

        return 0;
    }

    /**
     * Sets the smoothness factor of this Connection that is reflected when the polyline is rendered. 
     * 
     * @param smooth the value is one of SMOOTH_NONE - no smoothing, SMOOTH_LESS - rounded edges, 
     * SMOOTH_NORMAL - more curved look, SMOOTH_MORE - exagerated curving.
     */
    public final void setSmoothness(int smooth) {
        // always turn off all smoothing
        styleBits &= ~(SMOOTH_LESS | SMOOTH_NORMAL | SMOOTH_MORE);

        if (smooth == SMOOTH_LESS
            || smooth == SMOOTH_NORMAL
            || smooth == SMOOTH_MORE) {
            styleBits |= smooth;
        }
    }

    /**
     * Gets the smoothness factor.  
     * 
     * @return the value is one of SMOOTH_NONE - no smoothing, SMOOTH_LESS - rounded edges, 
     * SMOOTH_NORMAL - more curved look, SMOOTH_MORE - exagerated curving.
     */
    public final int getSmoothness() {
        if ((styleBits & SMOOTH_LESS) != 0)
            return SMOOTH_LESS;
        else
            if ((styleBits & SMOOTH_NORMAL) != 0)
                return SMOOTH_NORMAL;
            else
                if ((styleBits & SMOOTH_MORE) != 0)
                    return SMOOTH_MORE;

        return 0;
    }
    
     /**
     * Determines if this polyline connection is using closest distance routing or not.
     * 
     * @return <code>boolean</code> <code>true</code> if it should be using closest distance routing, 
     * <code>false</code otherwise.
     */
    public final boolean isClosestDistanceRouting() {
        return ((styleBits & ROUTE_CLOSEST_ROUTE) != 0);
    }

    /**
     * Determines if this polyline connection is using avoid obstruction routing or not.
     * 
     * @return <code>boolean</code> <code>true</code> if it should be using avoid obstruction routing, 
     * <code>false</code otherwise.
     */
    public final boolean isAvoidObstacleRouting() {
        return ((styleBits & ROUTE_AVOID_OBSTACLE) != 0);
    }

    /**
     * Set the overall routing styles for this polyline connection.
     *
     * @param closestDistance <code>boolean</code> <code>true</code> if it should be using closest distance routing, 
     * <code>false</code otherwise
     * @param avoidObstacles <code>boolean</code> <code>true</code> if it should be using avoid obstruction routing, 
     * <code>false</code otherwise
     */
    public void setRoutingStyles(
        final boolean closestDistance,
        final boolean avoidObstacles) {
        
        if (closestDistance)
            styleBits |= ROUTE_CLOSEST_ROUTE;
        else {
            styleBits &= ~ROUTE_CLOSEST_ROUTE;
        }

        if (avoidObstacles) {
            if (!closestDistance)
                styleBits |= ROUTE_CLOSEST_ROUTE;

            styleBits |= ROUTE_AVOID_OBSTACLE;
        } else
            styleBits &= ~ROUTE_AVOID_OBSTACLE;
    }
      
    /**
     * Determines if the bendpoints should be rounded.
     * This option is available only when orthogonal router is used, and 
     * when smoothness is not selected.
     * 
     * @return true if the option is selected and the current router is orthogonal
     * @since 1.2
     */
    public boolean isRoundingBendpoints() {
    	if (roundedBendpointsRadius > 0 && getSmoothFactor() == 0) {
    		return getConnectionRouter() instanceof OrthogonalRouter;
    	}
    	return false;
    }
    
    /**
     * Sets the parameter indicating the arc radius for rounded bendpoints.
     * 
     * @param radius
     * @since 1.2
     */
    public void setRoundedBendpointsRadius(int radius) {
    	roundedBendpointsRadius = radius;
    	if (origRoundedBendpointsRad > 0) {
    		origRoundedBendpointsRad = radius;
    	}
    }
    
    /**
     * Returns the parameter indicating the arc radius for rounded bendpoints.
     * 
     * @return the parameter indicating the arc radius for rounded bendpoints
     * @since 1.2
     */
    public int getRoundedBendpointsRadius() {
    	return MapModeUtil.getMapMode(this).DPtoLP(roundedBendpointsRadius);
    }

    /**
     * Determines if this polyline connection should use the jump links methodology
     * or not.
     * 
     * @return <code>boolean</code> <code>true</code> if this connection should support jump links, 
     * <code>false</code> otherwise.
     */
    public final boolean shouldJumpLinks() {
        if ((styleBits & ROUTE_JUMP_LINKS) != 0) {
            IFigure pParent = getParent();
            if (pParent instanceof ConnectionLayerEx)
                return ConnectionLayerEx.shouldJumpLinks();
            
            return true;
        }

        return false;
    }

    /** 
     * Turns on or off the jump links functionality.
     * 
     * @param on the <code>boolean</code> <code>true</code> if this connection should support jump links, 
     * <code>false</code> otherwise.
     */
    public void setJumpLinks(boolean on) {
        if (on)
            styleBits |= ROUTE_JUMP_LINKS;
        else
            styleBits &= ~ROUTE_JUMP_LINKS;
    }

    /**
     * Set the jump links styles associated with the jump links functionality.
     *
     * @param jumpType value can be one of <code>PolylineConnectionEx.JUMPLINK_FLAG_BELOW</code>, 
     * <code>PolylineConnectionEx.JUMPLINK_FLAG_ABOVE</code> or <code>PolylineConnectionEx.JUMPLINK_FLAG_ALL</code>
     * @param curved the <code>boolean</code> indicating if <code>true</code> the jump link should be curved (semi-circle) 
     * or if <code>false</code> it should be straight (rectangular).
     * @param angleIn the <code>boolean</code> if <code>true</code> indicating the sides of the jump link are angled or 
     * if <code>false</code> then the sides of the jump link are straight.
     * @param onBottom the <code>boolean</code> <code>true</code> it will be oriented on the bottom of the connection,
     * <code>false</code> it will oriented on top.
     */
    public void setJumpLinksStyles(
        int jumpType,
        boolean curved,
        boolean angleIn,
        boolean onBottom) {

        styleBits &= ~JUMPLINK_FLAG_ALL;
        styleBits |= jumpType;

        if (curved)
            styleBits |= JUMPLINK_FLAG_SMOOTH;
        else
            styleBits &= ~JUMPLINK_FLAG_SMOOTH;

        if (angleIn)
            styleBits |= JUMPLINK_FLAG_ANGLEIN;
        else
            styleBits &= ~JUMPLINK_FLAG_ANGLEIN;

        if (onBottom)
            styleBits |= JUMPLINK_FLAG_ONBOTTOM;
        else
            styleBits &= ~JUMPLINK_FLAG_ONBOTTOM;
            
        dirtyJumpLinks();
    }

    /**
     * Determines if the jump links are smooth or not.
     * 
     * @return <code>boolean</code> indicating if <code>true</code> the jump link should be curved (semi-circle) or 
     * if <code>false</code> it should be straight (rectangular).
     */
    public final boolean isJumpLinksSmooth() {
        return ((styleBits & JUMPLINK_FLAG_SMOOTH) != 0);
    }

    /**
     * Determines if the jump links are angled in or not.
     * 
     * @return <code>boolean</code> if <code>true</code> indicating the sides of the jump link are angled or 
     * if <code>false</code> then the sides of the jump link are straight.
     */
    public final boolean isJumpLinksAngledIn() {
        return ((styleBits & JUMPLINK_FLAG_ANGLEIN) != 0);
    }

    /**
     * Determines if the jump links are on the bottom of the polyline connection or not.
     * 
     * @return <code>boolean</code> <code>true</code> it will be oriented on the bottom of the connection,
     * <code>false</code> it will oriented on top.
     */
    public final boolean isJumpLinksOnBottom() {
        return ((styleBits & JUMPLINK_FLAG_ONBOTTOM) != 0);
    }

    /**
     * Dirty the jump links in this polyline connection.
     */
    void dirtyJumpLinks() {
        JumpLinkSet pJumpLinkSet = getJumpLinkSet();
        if (pJumpLinkSet != null) {
            pJumpLinkSet.dirtyJumpLinks();
        }
    }

    /**
     * Regenerate all the jump links in this polyline connection.
     */
    private boolean regenerateJumpLinks() {
        JumpLinkSet pJumpLinkSet = getJumpLinkSet();
        if (pJumpLinkSet != null) {
            return pJumpLinkSet.regenerateJumpLinks(this);
        }

        return false;
    }

    /**
     * Gets the set of all the jump links in this polyline connection.
     */
    private JumpLinkSet getJumpLinkSet() {
        if (shouldJumpLinks()) {
            if (jumpLinkSet == null) {
                jumpLinkSet = new JumpLinkSet();
            }
        } else {
            jumpLinkSet = null;
        }

        return jumpLinkSet;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.IPolygonAnchorableFigure#getPolygonPoints()
	 */
	public PointList getPolygonPoints() {
		return getSmoothPoints();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.IAnchorableFigure#getConnectionAnchor(java.lang.String)
	 */
	public ConnectionAnchor getConnectionAnchor(String terminal) {

		ConnectionAnchor connectAnchor =
			(ConnectionAnchor) getConnectionAnchors().get(terminal);
		if (connectAnchor == null) {
			if (terminal.equals(szAnchor)) {
				// get a new one - this figure doesn't support static anchors
				connectAnchor = createDefaultAnchor();
				getConnectionAnchors().put(terminal,connectAnchor);
			}
			else {
				connectAnchor = createAnchor(BaseSlidableAnchor.parseTerminalString(terminal));
			}
		}

		return connectAnchor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.IAnchorableFigure#getConnectionAnchorTerminal(org.eclipse.draw2d.ConnectionAnchor)
	 */
	public String getConnectionAnchorTerminal(ConnectionAnchor c) {
		if (c instanceof BaseSlidableAnchor) {
			return ((BaseSlidableAnchor) c).getTerminal();
		}
		if (getConnectionAnchors().containsValue(c)) {
			Iterator iter = getConnectionAnchors().keySet().iterator();
			String key;
			while (iter.hasNext()) {
				key = (String) iter.next();
				if (getConnectionAnchors().get(key).equals(c))
					return key;
			}
		}
		getConnectionAnchor(szAnchor);
		return szAnchor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.IAnchorableFigure#getSourceConnectionAnchorAt(org.eclipse.draw2d.geometry.Point)
	 */
	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		return createConnectionAnchor(p);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.figures.IAnchorableFigure#getTargetConnectionAnchorAt(org.eclipse.draw2d.geometry.Point)
	 */
	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		return createConnectionAnchor(p);
	}
	
	/**
	 * Creates the default Slidable anchor with a reference point at the center
	 * of the figure's bounds
	 * 
	 * @return - default SlidableAnchor, relative reference the center of the figure
	 */
	protected ConnectionAnchor createDefaultAnchor() {
		return new BaseSlidableAnchor(this);
	}
	
	/**
	 * Creates a slidable anchor at the specified point (from the ratio of the
	 * reference's coordinates and bounds of the figure
	 * 
	 * @param p - relative reference for the <Code>SlidableAnchor</Code>
	 * @return a <code>SlidableAnchor</code> for this figure with relative reference at p
	 */
	protected ConnectionAnchor createAnchor(PrecisionPoint p) {
		if (p==null)
			// If the old terminal for the connection anchor cannot be resolved (by SlidableAnchor) a null
			// PrecisionPoint will passed in - this is handled here
			return createDefaultAnchor();
		return new BaseSlidableAnchor(this, p);
	}

	/**
	 * Returns a new anchor for this node figure.
	 * 
	 * @param p <code>Point</code> on the figure that gives a hint which anchor to return.
	 * @return <code>ConnectionAnchor</code> reference to an anchor associated with the 
	 * given point on the figure.
	 */
	protected ConnectionAnchor createConnectionAnchor(Point p) {
		if (p == null) {
			return getConnectionAnchor(szAnchor);
		}
		else {
			Point temp = p.getCopy();
			translateToRelative(temp);
			PrecisionPoint pt = BaseSlidableAnchor.getAnchorRelativeLocation(temp, getBounds());
			return createAnchor(pt);
		}
	} 
	
	/**
	 * Checks whether the <PrecisionPoint> p which is a candidate for a relative reference
	 * for the <Code>SlidableAnchor</Code> belongs to the area where the default anchor
	 * must be created
	 * 
	 * @param p
	 * @return <code>boolean</code> <code>true</code> if <PrecisionPoint> belongs to the area where the default anchor must be 
	 * created, <code>false</code> otherwise
	 */
	protected boolean isDefaultAnchorArea(PrecisionPoint p) {
		return p.preciseX >= getSlidableAnchorArea()/2 && p.preciseX <= 1 - getSlidableAnchorArea()/2 &&
			p.preciseY >= getSlidableAnchorArea()/2 && p.preciseY <= 1 - getSlidableAnchorArea()/2;
	}
	
	/**
	 * Returns the connectionAnchors.
	 * @return Hashtable
	 */
	protected Hashtable getConnectionAnchors() {
		if (connectionAnchors == null)
			connectionAnchors = new Hashtable(1);
		return connectionAnchors;
	}
	
	/**
	 * Specifies how large the area of the figure's bounds where <Code>SlidableAnchor</Code>
	 * will be created. The result number: 0<=result<=1
	 * 
	 * @return  the size of the area of the figure's bounds
	 */
	protected double getSlidableAnchorArea() {
		return 0.25;
	}

    /* 
     * (non-Javadoc)
     * @see org.eclipse.draw2d.IFigure#setForegroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setForegroundColor(Color fg) {
        super.setForegroundColor(fg);
        if (getTargetDecoration() != null){
            getTargetDecoration().setForegroundColor(fg);
        }
        if (getSourceDecoration() != null){
            getSourceDecoration().setForegroundColor(fg);
        }
    }
    
    
    /**
     * Sets the decoration to be used at the start of the {@link Connection}.
     * 
     * @param dec the new source decoration
     * @param locator the <code>ConnectionLocator</code> that allows placement of the source
     * <code>RotableDecoration</code>.
     */
    public void setSourceDecoration(RotatableDecoration dec, ConnectionLocator locator) {
    	if (getSourceDecoration() != null)
    		remove(getSourceDecoration());
    	startDecoration = dec;
    	if (dec != null) {
    		add(dec, locator);
    	}
    }
    
    /**
     * Sets the decoration to be used at the end of the {@link Connection}.
     * 
     * @param dec the new target decoration
     * @param locator the <code>ConnectionLocator</code> that allows placement of the target
     * <code>RotableDecoration</code>.
     */
    public void setTargetDecoration(RotatableDecoration dec, ConnectionLocator locator) {
    	if (getTargetDecoration() != null)
    		remove(getTargetDecoration());
    	endDecoration = dec;
    	if (dec != null) {
    		add(dec, locator);
    	} 
    		
    } 
    
    /**
     * @return the target decoration - possibly null
     */
    protected RotatableDecoration getTargetDecoration() {
    	return endDecoration;
    }    
    
    /**
     * @return the source decoration - possibly null
     */
    protected RotatableDecoration getSourceDecoration() {
    	return startDecoration;
    }
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.PolylineConnection#setTargetDecoration(org.eclipse.draw2d.RotatableDecoration)
	 */
	public void setTargetDecoration(RotatableDecoration dec) {
		if (getTargetDecoration() != null)
    		remove(getTargetDecoration());
    	endDecoration = dec;
    	if (dec != null) {
    		add(dec, new ArrowLocator(this, ConnectionLocator.TARGET));
    	} 
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.PolylineConnection#setSourceDecoration(org.eclipse.draw2d.RotatableDecoration)
	 */
	public void setSourceDecoration(RotatableDecoration dec) {
		if (getSourceDecoration() != null)
    		remove(getSourceDecoration());
    	startDecoration = dec;
    	if (dec != null) {
    		add(dec, new ArrowLocator(this, ConnectionLocator.SOURCE));
    	}
	}


	/**
	 * Sets the line dash style.
	 * @see org.eclipse.swt.graphics.LineAttributes#dash
	 * @param dashes The dashes attribute.
	 */
	public void setLineDash(int[] dashes) {
		float [] floatDashes = new float[dashes.length];
		for (int i = 0; i < dashes.length; i++) {
			floatDashes[i] = dashes[i];
		}
		setLineDash(floatDashes);
	}

	/**
	* Currently we cannot create bendpoints with avoid obstructions 
	* routing style turned on. Hence we need to define a special cursor
	* to give user feedback about the disabled bendpoint editing 
	*/
	static private final Cursor NO_COMMAND_SPECIAL_CURSOR = 
		new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);

	/**
	 * Overriden to display special cursor when needed. Fix for bug #145467 
	 */
	public Cursor getCursor() {
		if (isAvoidObstacleRouting())
			return NO_COMMAND_SPECIAL_CURSOR;
		return super.getCursor();
	}
	
	/**
	 * Returns the list of points for this connection when rounded bendpoints
	 * option is selected. Each bendpoint in the result list is replaced by
	 * start and end points of the arc, and if calculateApproxPoints is true, it
	 * will also have arc approximation points in between.
	 * 
	 * @param calculateAppoxPoints
	 *            Indicates if arcs replacing bendpoints should be approximated
	 * @return List of connection points when rounded bendpoints option is on
	 * @since 1.2
	 */
	public PointList getRoundedCornersPoints(boolean calculateAppoxPoints) {
		if (rForBendpointArc != null) {
			rForBendpointArc.clear();
		} else {
			rForBendpointArc = new Hashtable<Integer, Integer>();
		}
		
		return PointListUtilities.calcRoundedCornersPolyline(getPoints(), getRoundedBendpointsRadius(), 
				rForBendpointArc, calculateAppoxPoints);
	}

	
}

