/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2011 Prodevelop.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *   Prodevelop, S.L.
 *   Pza. Don Juan de Villarrasa, 14 - 5
 *   46001 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   prode@prodevelop.es
 *   http://www.prodevelop.es
 *
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la PequeÔøΩa y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2011.
 *   author Alberto Romeu aromeu@prodevelop.es
 */

package es.prodevelop.gvsig.mini.symbol;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rasterizer;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;

public abstract class BaseSymbol extends Symbol {

	private Paint mPaint;

	public BaseSymbol(int color, float pointWidth) {
		mPaint = new Paint();		
		mPaint.setColor(color);
		mPaint.setStrokeWidth(pointWidth);
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
	}

	public BaseSymbol(float pointWidth) {
		Random rand = new Random();
		int r = rand.nextInt();
		int g = rand.nextInt();
		int b = rand.nextInt();

		mPaint = new Paint();
		mPaint.setColor(Color.rgb(r, g, b));
		mPaint.setStrokeWidth(pointWidth);
		mPaint.setStyle(Style.STROKE);
	}

	@Override
	public Paint getPaint() {
		if (mPaint == null)
			mPaint = new Paint();
		return mPaint;
	}

//	@Override
//	public void draw(Canvas c, IMapView mapView, JTSFeature feature)
//			throws BaseException {
//		// TODO Auto-generated method stub
//
//	}

	public float ascent() {
		return mPaint.ascent();
	}

	public int breakText(char[] text, int index, int count, float maxWidth,
			float[] measuredWidth) {
		return mPaint.breakText(text, index, count, maxWidth, measuredWidth);
	}

	public int breakText(CharSequence text, int start, int end,
			boolean measureForwards, float maxWidth, float[] measuredWidth) {
		return mPaint.breakText(text, start, end, measureForwards, maxWidth,
				measuredWidth);
	}

	public int breakText(String text, boolean measureForwards, float maxWidth,
			float[] measuredWidth) {
		return mPaint.breakText(text, measureForwards, maxWidth, measuredWidth);
	}

	public void clearShadowLayer() {
		mPaint.clearShadowLayer();
	}

	public float descent() {
		return mPaint.descent();
	}

	public boolean equals(Object o) {
		return mPaint.equals(o);
	}

	public int getAlpha() {
		return mPaint.getAlpha();
	}

	public int getColor() {
		return mPaint.getColor();
	}

	public ColorFilter getColorFilter() {
		return mPaint.getColorFilter();
	}

	public boolean getFillPath(Path src, Path dst) {
		return mPaint.getFillPath(src, dst);
	}

	public int getFlags() {
		return mPaint.getFlags();
	}

	public FontMetrics getFontMetrics() {
		return mPaint.getFontMetrics();
	}

	public float getFontMetrics(FontMetrics metrics) {
		return mPaint.getFontMetrics(metrics);
	}

	public FontMetricsInt getFontMetricsInt() {
		return mPaint.getFontMetricsInt();
	}

	public int getFontMetricsInt(FontMetricsInt fmi) {
		return mPaint.getFontMetricsInt(fmi);
	}

	public float getFontSpacing() {
		return mPaint.getFontSpacing();
	}

	public MaskFilter getMaskFilter() {
		return mPaint.getMaskFilter();
	}

	public PathEffect getPathEffect() {
		return mPaint.getPathEffect();
	}

	public Rasterizer getRasterizer() {
		return mPaint.getRasterizer();
	}

	public Shader getShader() {
		return mPaint.getShader();
	}

	public Cap getStrokeCap() {
		return mPaint.getStrokeCap();
	}

	public Join getStrokeJoin() {
		return mPaint.getStrokeJoin();
	}

	public float getStrokeMiter() {
		return mPaint.getStrokeMiter();
	}

	public float getStrokeWidth() {
		return mPaint.getStrokeWidth();
	}

	public Style getStyle() {
		return mPaint.getStyle();
	}

	public Align getTextAlign() {
		return mPaint.getTextAlign();
	}

	public void getTextBounds(char[] text, int index, int count, Rect bounds) {
		mPaint.getTextBounds(text, index, count, bounds);
	}

	public void getTextBounds(String text, int start, int end, Rect bounds) {
		mPaint.getTextBounds(text, start, end, bounds);
	}

	public void getTextPath(char[] text, int index, int count, float x,
			float y, Path path) {
		mPaint.getTextPath(text, index, count, x, y, path);
	}

	public void getTextPath(String text, int start, int end, float x, float y,
			Path path) {
		mPaint.getTextPath(text, start, end, x, y, path);
	}

	public float getTextScaleX() {
		return mPaint.getTextScaleX();
	}

	public float getTextSize() {
		return mPaint.getTextSize();
	}

	public float getTextSkewX() {
		return mPaint.getTextSkewX();
	}

	public int getTextWidths(char[] text, int index, int count, float[] widths) {
		return mPaint.getTextWidths(text, index, count, widths);
	}

	public int getTextWidths(CharSequence text, int start, int end,
			float[] widths) {
		return mPaint.getTextWidths(text, start, end, widths);
	}

	public int getTextWidths(String text, float[] widths) {
		return mPaint.getTextWidths(text, widths);
	}

	public int getTextWidths(String text, int start, int end, float[] widths) {
		return mPaint.getTextWidths(text, start, end, widths);
	}

	public Typeface getTypeface() {
		return mPaint.getTypeface();
	}

	public Xfermode getXfermode() {
		return mPaint.getXfermode();
	}

	public int hashCode() {
		return mPaint.hashCode();
	}

	public float measureText(char[] text, int index, int count) {
		return mPaint.measureText(text, index, count);
	}

	public float measureText(CharSequence text, int start, int end) {
		return mPaint.measureText(text, start, end);
	}

	public float measureText(String text, int start, int end) {
		return mPaint.measureText(text, start, end);
	}

	public float measureText(String text) {
		return mPaint.measureText(text);
	}

	public void reset() {
		mPaint.reset();
	}

	public void set(Paint src) {
		mPaint.set(src);
	}

	public void setARGB(int a, int r, int g, int b) {
		mPaint.setARGB(a, r, g, b);
	}

	public void setAlpha(int a) {
		mPaint.setAlpha(a);
	}

	public void setAntiAlias(boolean aa) {
		mPaint.setAntiAlias(aa);
	}

	public void setColor(int color) {
		mPaint.setColor(color);
	}

	public ColorFilter setColorFilter(ColorFilter filter) {
		return mPaint.setColorFilter(filter);
	}

	public void setDither(boolean dither) {
		mPaint.setDither(dither);
	}

	public void setFakeBoldText(boolean fakeBoldText) {
		mPaint.setFakeBoldText(fakeBoldText);
	}

	public void setFilterBitmap(boolean filter) {
		mPaint.setFilterBitmap(filter);
	}

	public void setFlags(int flags) {
		mPaint.setFlags(flags);
	}

	public void setLinearText(boolean linearText) {
		mPaint.setLinearText(linearText);
	}

	public MaskFilter setMaskFilter(MaskFilter maskfilter) {
		return mPaint.setMaskFilter(maskfilter);
	}

	public PathEffect setPathEffect(PathEffect effect) {
		return mPaint.setPathEffect(effect);
	}

	public Rasterizer setRasterizer(Rasterizer rasterizer) {
		return mPaint.setRasterizer(rasterizer);
	}

	public Shader setShader(Shader shader) {
		return mPaint.setShader(shader);
	}

	public void setShadowLayer(float radius, float dx, float dy, int color) {
		mPaint.setShadowLayer(radius, dx, dy, color);
	}

	public void setStrikeThruText(boolean strikeThruText) {
		mPaint.setStrikeThruText(strikeThruText);
	}

	public void setStrokeCap(Cap cap) {
		mPaint.setStrokeCap(cap);
	}

	public void setStrokeJoin(Join join) {
		mPaint.setStrokeJoin(join);
	}

	public void setStrokeMiter(float miter) {
		mPaint.setStrokeMiter(miter);
	}

	public void setStrokeWidth(float width) {
		mPaint.setStrokeWidth(width);
	}

	public void setStyle(Style style) {
		mPaint.setStyle(style);
	}

	public void setSubpixelText(boolean subpixelText) {
		mPaint.setSubpixelText(subpixelText);
	}

	public void setTextAlign(Align align) {
		mPaint.setTextAlign(align);
	}

	public void setTextScaleX(float scaleX) {
		mPaint.setTextScaleX(scaleX);
	}

	public void setTextSize(float textSize) {
		mPaint.setTextSize(textSize);
	}

	public void setTextSkewX(float skewX) {
		mPaint.setTextSkewX(skewX);
	}

	public Typeface setTypeface(Typeface typeface) {
		return mPaint.setTypeface(typeface);
	}

	public void setUnderlineText(boolean underlineText) {
		mPaint.setUnderlineText(underlineText);
	}

	public Xfermode setXfermode(Xfermode xfermode) {
		return mPaint.setXfermode(xfermode);
	}

}
