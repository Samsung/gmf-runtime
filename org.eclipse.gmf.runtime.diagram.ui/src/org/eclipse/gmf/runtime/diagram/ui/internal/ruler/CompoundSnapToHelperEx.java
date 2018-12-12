/******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.internal.ruler;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToHelper;

/**
 * Overridden to:
 * <li> support snapping to geometry in a restricted direction. See
 * {@link SnapToHelperUtil#RESTRICTED_DIRECTIONS}.</li>
 * 
 * @author crevells
 */
public class CompoundSnapToHelperEx
    extends CompoundSnapToHelper {

    public CompoundSnapToHelperEx(SnapToHelper[] delegates) {
        super(delegates);
    }

    public int snapRectangle(Request request, int snapOrientation,
            PrecisionRectangle baseRect, PrecisionRectangle result) {

        Integer restrictedDirections = (Integer) request.getExtendedData().get(
            SnapToHelperUtil.RESTRICTED_DIRECTIONS);
        if (restrictedDirections == null
            || restrictedDirections == PositionConstants.NONE) {
            return super.snapRectangle(request, snapOrientation, baseRect,
                result);
        }

        snapOrientation = SnapToHelperUtil.updateSnapLocations(snapOrientation,
            restrictedDirections);

        SnapToHelper delegates[] = getDelegates();
        int snapOrientations[] = new int[delegates.length];
        PrecisionRectangle results[] = new PrecisionRectangle[delegates.length];

        for (int i = 0; i < delegates.length; i++) {
            results[i] = new PrecisionRectangle();
            snapOrientations[i] = getDelegates()[i].snapRectangle(request,
                snapOrientation, baseRect, results[i]);
        }

        int snapOrientationToReturn = snapOrientation;
        for (int i = 0; i < delegates.length; i++) {
            // find out which snapping bits changed
            int snapChanged = snapOrientation ^ snapOrientations[i];
            if (snapChanged != 0) {
                // some snapping occurred
                if ((snapChanged & HORIZONTAL) != 0) {
                    if (result.preciseX == 0
                        || Math.abs(results[i].preciseX) < Math
                            .abs(result.preciseX)) {
                        result.preciseX = results[i].preciseX;
                    }
                    snapOrientationToReturn &= ~HORIZONTAL;
                }
                if ((snapChanged & VERTICAL) != 0) {
                    if (result.preciseY == 0
                        || Math.abs(results[i].preciseY) < Math
                            .abs(result.preciseY)) {
                        result.preciseY = results[i].preciseY;
                    }
                    snapOrientationToReturn &= ~VERTICAL;
                }
            }
        }
        result.updateInts();
        return snapOrientationToReturn;
    }
}
