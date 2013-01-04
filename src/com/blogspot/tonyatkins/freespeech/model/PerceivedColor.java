/**
 * Copyright 2012-2013 Tony Atkins <duhrer@gmail.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Tony Atkins ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Tony Atkins OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 */
package com.blogspot.tonyatkins.freespeech.model;

import android.graphics.Color;

public class PerceivedColor implements Comparable<PerceivedColor> {
	private int r = 0;
	private int g = 0;
	private int b = 0;
	
	public PerceivedColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	private int getPerceivedBrightness() {
		// Adapted from http://www.nbdtech.com/Blog/archive/2008/04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx
		return (int)Math.sqrt(
				(r * r * .241) + 
				(g * g * .691) + 
				(b * b * .068));
	}
	
	@Override
	public String toString() {
		return "#" + getPaddedHexString(r) + getPaddedHexString(g) + getPaddedHexString(b);
	}
	
	public int getColor() {
		return Color.parseColor(this.toString());
	}
	
	private static StringBuffer getPaddedHexString(double cellValue) {
		StringBuffer out  = new StringBuffer();
		if (cellValue < 16) { out.append("0"); }
		out.append(Integer.toHexString((int) cellValue));
		return out;
	}

	public int compareTo(PerceivedColor another) {
		return this.getPerceivedBrightness() - another.getPerceivedBrightness();
	}
}
