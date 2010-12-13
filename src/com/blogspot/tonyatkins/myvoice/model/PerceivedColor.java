package com.blogspot.tonyatkins.myvoice.model;

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

	@Override
	public int compareTo(PerceivedColor another) {
		return this.getPerceivedBrightness() - another.getPerceivedBrightness();
	}
}
