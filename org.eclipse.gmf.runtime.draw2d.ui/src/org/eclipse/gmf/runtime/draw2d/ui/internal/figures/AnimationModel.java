/******************************************************************************
 * Copyright (c) 2002, 2003  IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.draw2d.ui.internal.figures;

import java.util.Date;

/**
 * Holds the count information, and notifies
 * interested figures of changes in animation.
 * Created by a root, which loops through the
 * animation process.
 * 
 * <p>
 * Code taken from Eclipse reference bugzilla #98820
 *
 */
public class AnimationModel {

private long startTime = new Date().getTime();
private long duration = 0;

private boolean ascending;

/**
 * Default constructor taking in number of
 * milliseconds the animation should take.
 */
public AnimationModel(long duration, boolean ascending){
	this.duration = duration;
	this.ascending = ascending;
}

/**
 * Called to notify the start of the animation process.
 * Notifies all listeners to get ready for animation start.
 */
public void animationStarted(){
	startTime = new Date().getTime();
}

/**
 * Returns (0.0<=value<=1.0), of current position
 */
public float getProgress(){
	long presentTime = new Date().getTime();
	float elapsed = (presentTime-startTime);
	float progress = Math.min(1.0f, elapsed/duration);
	if (!ascending)
		return 1.0f - progress;
	return progress;
}

public boolean isFinished(){
	return (new Date().getTime() - startTime) > duration;
}

}

