package es.prodevelop.gvsig.mini.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import es.prodevelop.gvsig.mini.legend.ILegend;
import es.prodevelop.gvsig.mini.overlay.Paints;
import es.prodevelop.gvsig.mini.symbol.LineSymbol;
import es.prodevelop.gvsig.mini.symbol.MultiLineSymbol;
import es.prodevelop.gvsig.mini.symbol.MultiPointSymbol;
import es.prodevelop.gvsig.mini.symbol.MultiPolygonSymbol;
import es.prodevelop.gvsig.mini.symbol.PointSymbol;
import es.prodevelop.gvsig.mini.symbol.PointSymbolizer;
import es.prodevelop.gvsig.mini.symbol.PointsAsLinesSymbol;
import es.prodevelop.gvsig.mini.symbol.PolygonSymbol;
import es.prodevelop.gvsig.mini.symbol.Symbol;

public class LegendView extends View {

	private ILegend legend;
	private Symbol symbol;
	private Context context;

	private final static int WIDTH_LOW = 24;
	private final static int HEIGHT_LOW = 24;

	private final static int WIDTH_HIGH = 48;
	private final static int HEIGHT_HIGH = 48;

	private int width = WIDTH_LOW;
	private int height = HEIGHT_LOW;

	public LegendView(Context context) {
		super(context);
		this.context = context;
		calcDensity();
	}

	public LegendView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		calcDensity();
	}

	public LegendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		calcDensity();
	}

	public void calcDensity() {
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		if (metrics.densityDpi != DisplayMetrics.DENSITY_LOW) {
			width = WIDTH_HIGH;
			height = HEIGHT_HIGH;
		}
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		final Symbol symbol = this.getSymbol();

		if (symbol == null)
			return;

		// FIXME Difficult to extend
		if (symbol instanceof PointSymbol) {
			canvas.drawPoint(width / 2, height / 2, symbol.getPaint());
		} else if (symbol instanceof PointSymbolizer) {
			final Bitmap bitmap = ((PointSymbolizer) symbol).getBitmap();
			canvas.drawBitmap(bitmap, bitmap.getWidth() / 2,
					bitmap.getHeight() / 2, Paints.mPaintR);
		} else if (symbol instanceof PointsAsLinesSymbol) {
			Path path = new Path();
			path.moveTo(0, 0);
			path.lineTo(0, height);
			path.lineTo(width, 0);
			path.lineTo(width, height);
			canvas.drawPath(path, symbol.getPaint());
		} else if (symbol instanceof MultiPointSymbol) {
			canvas.drawPoint(width / 2, height / 2, symbol.getPaint());
			canvas.drawPoint(width, height, symbol.getPaint());
			canvas.drawPoint(0, 0, symbol.getPaint());
			canvas.drawPoint(0, width, symbol.getPaint());
			canvas.drawPoint(0, height, symbol.getPaint());
		} else if (symbol instanceof LineSymbol) {
			Path path = new Path();
			path.moveTo(0, 0);
			path.lineTo(0, height);
			path.lineTo(width, 0);
			path.lineTo(width, height);
			canvas.drawPath(path, symbol.getPaint());
			path.rewind();
		} else if (symbol instanceof MultiLineSymbol) {

		} else if (symbol instanceof PolygonSymbol) {
			Path path = new Path();
			path.moveTo(0, 0);
			path.lineTo(0, height);
			path.lineTo(width, height);
			path.lineTo(width, 0);
			path.lineTo(0, 0);
			canvas.drawPath(path, symbol.getPaint());
		} else if (symbol instanceof MultiPolygonSymbol) {

		} else {
			// GeometryCollection or whatever
			canvas.drawPoint(width / 2, height / 2, symbol.getPaint());
			Path path = new Path();
			path.moveTo(0, 0);
			path.lineTo(0, height / 3);
			path.lineTo(width / 3, 0);
			path.lineTo(width / 3, height / 3);
			canvas.drawPath(path, symbol.getPaint());

			// path.rewind();
			// path.moveTo(width / 3, height / 3);
			// path.lineTo(width / 3, height);
			// path.lineTo(width, height);
			// path.lineTo(width, height / 3);
			// path.lineTo(width / 3, height / 3);
			// canvas.drawPath(path, symbol.getPaint());
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		super.onMeasure(width, height);
		setMeasuredDimension(width, height);
	}

	public ILegend getLegend() {
		return legend;
	}

	public void setLegend(ILegend legend) {
		this.legend = legend;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

}
