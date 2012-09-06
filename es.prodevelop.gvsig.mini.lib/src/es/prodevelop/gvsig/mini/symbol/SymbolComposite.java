package es.prodevelop.gvsig.mini.symbol;

import android.graphics.Canvas;
import android.graphics.Paint;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;

public class SymbolComposite extends Symbol {

	private Symbol[] symbols;

	public SymbolComposite(Symbol... symbols) {
		this.symbols = symbols;
	}

	@Override
	public Paint getPaint() {
		//FIXME Think about this
		return symbols[0].getPaint();
	}

	@Override
	public void draw(Canvas c, IMapView mapView, JTSFeature feature)
			throws BaseException {
		for (Symbol symbol : symbols) {
			symbol.draw(c, mapView, feature);
		}
	}
	
	public Symbol[] getSymbols() {
		return this.symbols;
	}

}
