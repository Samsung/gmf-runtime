/******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/


package org.eclipse.gmf.runtime.draw2d.ui.internal.graphics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.printing.Printer;


/**
 * Created on May 8, 2003
 *
 * @author sshaw
 * @version 1.0
 * 
 * Overridden to handle printing problem with transparencies.  Specifically,
 * dropshadows print with as a black bar (RATLC00513998)
 */
public class PrinterGraphics extends org.eclipse.draw2d.PrinterGraphics {
	
    private double printScale = 1.0;
    private boolean roundFonts = false;
    private Stack stateStack = new Stack();
    
    /**
	 * @return Returns the printScale.
	 */
	protected double getPrintScale() {
		return printScale;
	}
	
    /**
	* Creates a new <code>PrinterGraphics</code> with <code>Graphics</code> g, using 
	* <code>Printer</code> p
	* 
	* @param g <code>Graphics</code> object to draw with
	* @param p <code>Printer</code> to print to
	* @param roundFonts the <code>boolean</code> if <code>true</code> indicates that fonts 
	* should be rounded to account for printer font scaling errors.
	*/
	public PrinterGraphics(Graphics g, Printer p, boolean roundFonts) {
		super(g, p);
        this.roundFonts = roundFonts;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#dispose()
	 */
	public void dispose() {
		
		for (Iterator<Image> it = collapseImageCache.values().iterator(); it.hasNext();)
		{
	        Image img = it.next();
	        img.dispose();
		}

		collapseImageCache.clear();

		super.dispose();
	}
	
	Map<Image, Image> collapseImageCache = new HashMap<Image, Image>();

    /**
     * collapseImage
     * This method is necessary to work around a problem with the printer GC that
     * doesn't seem to support alpha channels on Images.  Consequently we collapse the
     * image based on the background.
     * This workaround won't be necessary when Bugzilla 28766 is fixed.
     * 
	 * @param srcImage Image to collapse the alpha channel of.
	 * @return Image that has no alpha channel.
	 */
	private Image collapseImage(Image srcImage) {
		Image result = (Image)collapseImageCache.get(srcImage);
		if (result != null)
			return result;
		
		org.eclipse.swt.graphics.Rectangle r1 = srcImage.getBounds();
        Image newImg = new Image( null, r1.width, r1.height);
        
        GC gc = new GC(newImg);
        gc.setForeground(getForegroundColor());
        gc.setBackground(getBackgroundColor());
        gc.drawImage(srcImage, 0, 0);
        gc.dispose();
		
        collapseImageCache.put(srcImage, newImg);
    	return newImg;
    }
    
    /**
	 * @see org.eclipse.draw2d.Graphics#drawImage(org.eclipse.swt.graphics.Image, int, int)
	 */
	public void drawImage(Image srcImage, int x, int y) {
		Image collapseImage = srcImage.getImageData().transparentPixel == -1 ? collapseImage(srcImage)
				: srcImage;
		super.drawImage(collapseImage, x, y);
	}
    
    /**
	 * @see org.eclipse.draw2d.Graphics#drawImage(org.eclipse.swt.graphics.Image, int, int, int, int, int, int, int, int)
	 */
	public void drawImage(Image srcImage, int sx, int sy, int sw, int sh,
			int tx, int ty, int tw, int th) {
		Image collapseImage = srcImage.getImageData().transparentPixel == -1 ? collapseImage(srcImage)
				: srcImage;
		super.drawImage(collapseImage, sx, sy, sw, sh, tx, ty, tw, th);
	}
    
    /**
     * shouldRoundFonts
     * Accessor method to determine if fonts should be rounded to account for printer
     * font scaling errors.
     * 
	 * @return boolean true if fonts should be rounded, false otherwise.
	 */
	public boolean shouldRoundFonts() {
        return roundFonts;
    }
    
    /**
	 * @see org.eclipse.draw2d.Graphics#scale(double)
	 */
	public void scale(double amount) {
        printScale = printScale * amount;
        super.scale(amount);
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Graphics#popState()
     */
    public void popState() {
        if (!stateStack.isEmpty())
            printScale = ((Double)stateStack.pop()).doubleValue();
        super.popState();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Graphics#pushState()
     */
    public void pushState() {
        stateStack.push(new Double(printScale));
        super.pushState();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Graphics#restoreState()
     */
    public void restoreState() {
        if (!stateStack.isEmpty())
            printScale = ((Double)stateStack.firstElement()).doubleValue();
        super.restoreState();
    }
    
    /**
     * This should be escalated as a GEF / SWT problem - difficult to reproduce in 
     * logic example though.  The font size needs to scaled down to account for 
     * printer font scaling issues.
     * 
	 * @see org.eclipse.draw2d.Graphics#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font f) {
       
       Font newFont = f;
       
       if (shouldRoundFonts()) {
           FontData fd = f.getFontData()[0];
           int nAdjustedHeight = (int)(((fd.getHeight() - 0.5f) / getPrintScale()) * getPrintScale());
           if (fd.getHeight() != nAdjustedHeight) {
               fd.setHeight(nAdjustedHeight);    
               newFont = FontRegistry.getInstance().getFont(null, fd);   
           }
       }
       
       super.setFont(newFont);
    }
    
}
