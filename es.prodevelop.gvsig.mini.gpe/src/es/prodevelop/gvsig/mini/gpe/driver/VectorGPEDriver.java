package es.prodevelop.gvsig.mini.gpe.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.gvsig.compat.CompatLibrary;
import org.gvsig.compat.se.SECompatLibrary;
import org.gvsig.gpe.lib.api.GPELibrary;
import org.gvsig.gpe.lib.api.GPELocator;
import org.gvsig.gpe.lib.api.GPEManager;
import org.gvsig.gpe.lib.api.exceptions.ParserCreationException;
import org.gvsig.gpe.lib.api.parser.IGPEContentHandlerInmGeom;
import org.gvsig.gpe.lib.api.parser.IGPEErrorHandler;
import org.gvsig.gpe.lib.impl.DefaultGPELibrary;
import org.gvsig.gpe.lib.impl.parser.GPEErrorHandler;
import org.gvsig.gpe.lib.spi.GPEProviderLocator;
import org.gvsig.gpe.lib.spi.GPEProviderManager;
import org.gvsig.gpe.lib.spi.parser.IGPEParser;
import org.gvsig.gpe.prov.gml.GmlLibrary;
import org.gvsig.gpe.prov.gpx.GpxLibrary;
import org.gvsig.gpe.prov.kml.KmlLibrary;
import org.gvsig.gpe.prov.xml.XmlLibrary;
import org.gvsig.tools.ToolsLibrary;
import org.gvsig.tools.locator.ReferenceNotRegisteredException;
import org.gvsig.tools.persistence.xml.XMLPersistenceLibrary;
import org.gvsig.xmlpull.lib.api.XmlPullLibrary;
import org.gvsig.xmlpull.lib.impl.DefaultXmlPullLibrary;
import org.gvsig.xmlpull.prov.kxml.KxmlLibrary;
import org.gvsig.xmlpull.prov.stax.StaxLibrary;
import org.gvsig.xmlschema.lib.api.XMLSchemaLibrary;
import org.gvsig.xmlschema.lib.impl.DefaultXMLSchemaLibrary;

import android.content.Context;
import android.util.Log;
import es.prodevelop.gvsig.mini.gpe.GPEMiniContentHandler;
import es.prodevelop.gvsig.mini.gpe.GPEMiniErrorHandler;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;

public class VectorGPEDriver {

	private Context context;
	private IMapView mapView;

	private GPEManager gpeManager = null;
	private GPEProviderManager gpeProviderManager = null;
	private File file = null;
	private IGPEParser parser = null;
	private IGPEContentHandlerInmGeom contentHandler = null;
	private IGPEErrorHandler errorHandler = null;

	private FileInputStream fin;

	public VectorGPEDriver(Context context, IMapView mapView) {
		this.context = context;
		this.mapView = mapView;
	}

	public void setUp() {
		initializeLibraries();
	}

	public void initializeLibraries() {

		org.gvsig.gpe.lib.api.GPELibrary t1 = new GPELibrary();
		org.gvsig.tools.ToolsLibrary t2 = new ToolsLibrary();
		org.gvsig.tools.persistence.xml.XMLPersistenceLibrary t3 = new XMLPersistenceLibrary();
		org.gvsig.xmlpull.lib.api.XmlPullLibrary t4 = new XmlPullLibrary();
		org.gvsig.gpe.lib.impl.DefaultGPELibrary t5 = new DefaultGPELibrary();
		org.gvsig.gpe.lib.impl.DefaultGPEProviderLibrary t6 = new org.gvsig.gpe.lib.impl.DefaultGPEProviderLibrary();
		org.gvsig.xmlschema.lib.api.XMLSchemaLibrary t7 = new XMLSchemaLibrary();
		// org.gvsig.gpe.prov.gml.GmlTestLibrary (null)
		// org.gvsig.gpe.prov.gml.GmlLibrary (null)
		org.gvsig.compat.CompatLibrary t8 = new CompatLibrary();
		org.gvsig.compat.se.SECompatLibrary t9 = new SECompatLibrary();
		org.gvsig.gpe.prov.xml.XmlLibrary t10 = new XmlLibrary();
		org.gvsig.xmlpull.lib.impl.DefaultXmlPullLibrary t11 = new DefaultXmlPullLibrary();
		// org.gvsig.xmlpull.lib.spi.XmlPullProviderLibrary t12 = new
		// XmlPullProviderLibrary();
		org.gvsig.xmlpull.prov.kxml.KxmlLibrary t13 = new KxmlLibrary();
		org.gvsig.xmlpull.prov.stax.StaxLibrary t14 = new StaxLibrary();
		org.gvsig.xmlschema.lib.impl.DefaultXMLSchemaLibrary t15 = new DefaultXMLSchemaLibrary();
		// org.gvsig.gpe.prov.kml.KmlTestLibrary t17 = new KmlTestLibrary();
		org.gvsig.gpe.prov.kml.KmlLibrary t16 = new KmlLibrary();
		org.gvsig.gpe.prov.gpx.GpxLibrary t17 = new GpxLibrary();
		org.gvsig.gpe.prov.gml.GmlLibrary t18 = new GmlLibrary();

		t1.initialize();
		t2.initialize();
		t3.initialize();
		t4.initialize();
		t5.initialize();
		t6.initialize();
		t7.initialize();
		t8.initialize();
		t9.initialize();
		t10.initialize();
		t11.initialize();
		// t12.initialize();
		t13.initialize();
		t14.initialize();
		t15.initialize();
		t16.initialize();
		t17.initialize();
		t18.initialize();

		t1.postInitialize();
		t2.postInitialize();
		t3.postInitialize();
		t4.postInitialize();
		t5.postInitialize();
		t6.postInitialize();
		t7.postInitialize();
		t8.postInitialize();
		t9.postInitialize();
		t10.postInitialize();
		t11.postInitialize();
//		 t12.postInitialize();
		t13.postInitialize();
		t14.postInitialize();
		t15.postInitialize();
		t16.postInitialize();
		t17.postInitialize();
		t18.postInitialize();

		gpeManager = GPELocator.getGPEManager();
		gpeProviderManager = GPEProviderLocator.getGPEProviderManager();

		if (gpeProviderManager == null) {
			throw new ReferenceNotRegisteredException(
					GPEProviderLocator.GPE_PROVIDER_MANAGER_NAME,
					GPEProviderLocator.getInstance());
		}

//		try {
//			gpeProviderManager.addGpeParser("GML 2.0.0",
//					"Parser for GML 2.0.0",
//					org.gvsig.gpe.prov.gml.parser.GPEGml2_0_0_Parser.class);
//			gpeProviderManager.addGpeParser("GML 2.1.0",
//					"Parser for GML 2.1.0",
//					org.gvsig.gpe.prov.gml.parser.GPEGml2_1_0_Parser.class);
//			gpeProviderManager.addGpeParser("GML 2.1.2",
//					"Parser for GML 2.1.2",
//					org.gvsig.gpe.prov.gml.parser.GPEGml2_1_2_Parser.class);
//			gpeProviderManager.addGpeParser("GML 3.1.2",
//					"Parser for GML 3.1.2",
//					org.gvsig.gpe.prov.gml.parser.GPEGmlSFP0Parser.class);
//			gpeProviderManager.addGpeParser("KML 2.1", "Parser for KML 2.1",
//					org.gvsig.gpe.prov.kml.parser.GPEKml2_1_Parser.class);
//			gpeProviderManager.addGpeParser("KMZ 2.1", "Parser for KMZ 2.1",
//					org.gvsig.gpe.prov.kml.parser.GPEKmz2_1_Parser.class);
//			// gpeProviderManager.addGpeParser("GPX v1.1", "Parser for GPX 1.1",
//			// org.gvsig.gpe.prov.gpx.parser.GPEGpxv11Parser.class);
//
//		} catch (ParserNotRegisteredException e) {
//			// logger.error("Impossible to register a KML parser");
//		}

	}

	/**
	 * Set the ErrorHandler.
	 */
	private void setErrorHandler(IGPEErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * Get a list of parsed layers.
	 * 
	 * @return Layers : ArrayList of Layers (OJO!!! FLyrGpeVectorial).
	 */
	public FeatureOverlay getOverlay() {
		return ((GPEMiniContentHandler) parser.getContentHandler())
				.getOverlay();
	}

	/**
	 * Get the ContentHandler, if it don't exists creates a new one.
	 * 
	 * @return contentHandler : GPEContentHandler.
	 */
	public IGPEContentHandlerInmGeom getContenHandler() {
		if (contentHandler == null) {
			contentHandler = new GPEMiniContentHandler(this.context,
					this.mapView, this.getErrorHandler(), this.file.getName());
		}
		return contentHandler;
	}

	/**
	 * Get the ErrorHandler, if it don't exists creates a new one.
	 * 
	 * @return errorHandler : GPEErrorHandler.
	 */
	public GPEErrorHandler getErrorHandler() {
		if (errorHandler == null) {
			errorHandler = new GPEMiniErrorHandler();
		}
		return (GPEErrorHandler) errorHandler;
	}

	/**
	 * Return true if accepted, in this case only GPX, GML, XML or KML is
	 * accepted.
	 */
	public boolean accept(File f) {
		return (f.getName().toUpperCase().endsWith("GML"))
				|| (f.getName().toUpperCase().endsWith("XML"))
				|| (f.getName().toUpperCase().endsWith("KML"))
				|| (f.getName().toUpperCase().endsWith("KMZ"))
				|| (f.getName().toUpperCase().endsWith("GPX"));
	}

	/**
	 * Close the gpe file.
	 */
	public void close() throws IOException {
		try {
			fin.close();
		} catch (IOException e1) {
			Log.e("VectorGPEDriver",
					"While closing file input stream: " + e1.getMessage());
		}
	}

	/**
	 * Starts libGPE to parse the file.
	 */
	public void initialize() {
		// CREATE THE HANDLERS...
		this.errorHandler = null;
		this.contentHandler = null;

		// SET UP THE GPE PARSER'S REGISTER...

		setUp();
		Log.d("", "INFO: Terminado Registro de parser's GPE.");

		// CHECK IF IT'S PARSEABLE FILE
		File file = getFile();
		Log.d("", "INFO: Abriendo Fichero: " + file.getName());
		// IF THERE ARE A CORRECT PARSER TO THE FILE FORMAT, START THE PARSING
		// PROCESS
		// if(GPEFactory.accept(file.toURI())==true){
		if (gpeProviderManager.accept(file.toURI()) == true) {
			Log.d("", "INFO: Existe parser registrado para el fomato ");

			// CREATES A NEW PARSER AND SET IT AS DEFAULT DRIVER'S PARSER.
			/******************** VIEJO REGISTRO ********************/

			try {
				setParser((gpeProviderManager.createParser(file)));
			} catch (ParserCreationException e) {
				// TODO Auto-generated catch block
				Log.e("", "create parser", e);
			}

			// CALL TO LIBGPE PARSER TO GET THE PARSED FEATURES THROUGH THE
			// CONTENT HANDLER
			getParser().parse(getContenHandler(), getErrorHandler(),
					file.toURI());
			Log.d("", "File " + file.getPath() + " Readed by GPE Parser");

			for (int i = 0; i < getErrorHandler().getErrorsSize(); i++) {
				Log.e("GPEMap", "errors", getErrorHandler().getErrorAt(i));
			}

			for (int i = 0; i < getErrorHandler().getWarningsSize(); i++) {
				Log.e("GPEMap", "warnings", getErrorHandler().getWarningAt(i));
			}
			FeatureOverlay overlay = getOverlay();

			this.decideLegendByGeometryType(overlay);

			final int size = overlay.getOverlaysCount();
			FeatureOverlay ov;
			for (int i = 0; i < size; i++) {
				ov = overlay.getOverlay(i);
				if (ov.getName() == null || ov.getName().length() == 0) {
					ov.setName(getFilePath() + i);
				}
				this.mapView.addOverlay(ov);
			}
		}
	}

	/**
	 * Open the GPE file.
	 * 
	 * @param File
	 *            to be opened
	 * @throws IOException
	 */
	public void open(File f) throws IOException {
		file = f;
		fin = new FileInputStream(file);
	}

	/**
	 * If is writable layer return true.
	 * 
	 * @return boolean
	 */
	public boolean isWritable() {
		return true;
	}

	/**
	 * Reload the layer.
	 */
	public void reload() throws IOException {
		open(file);
		initialize();
	}

	/**
	 * Return the Name of the Driver.
	 * 
	 * @return String
	 */
	public String getName() {
		return "GPE Vectorial driver";
	}

	/**
	 * Return Data File the same in GPE as the parsered one.
	 * 
	 * @return File
	 */
	public File getDataFile(File f) {
		return file;
	}

	/**
	 * Return the File parsered.
	 * 
	 * @return File
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Return the parsered File absolute path.
	 * 
	 * @return String
	 */
	public String getFilePath() {
		return file.getAbsolutePath();
	}

	/**
	 * Return the Parser from GPE.
	 * 
	 * @return The GPEParser
	 */
	public IGPEParser getParser() {
		return parser;
	}

	// ------------------------------------------
	public void setParser(IGPEParser igpeParser) {
		this.parser = igpeParser;
	}

	public void decideLegendByGeometryType(FeatureOverlay overlay) {

		final int size = overlay.getOverlaysCount();

		FeatureOverlay ov;
		for (int i = 0; i < size; i++) {
			ov = overlay.getOverlay(i);
			decideLegendByGeometryType(ov);
		}

		if (!overlay.isEmpty()) {
			overlay.setOriginalFileName(this.getFilePath());
			if (overlay.getNumPoints() > 0) {
				if (overlay.getNumGeomCol() == 0 && overlay.getNumLines() == 0
						&& overlay.getNumMultiLines() == 0
						&& overlay.getNumMultiPoints() == 0
						&& overlay.getNumMultiPol() == 0
						&& overlay.getNumPol() == 0) {
					overlay.setGeomType(FeatureOverlay.TYPE_POINT);
					// poner leyenda de puntos
				} else {
					overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
					// poner multigeometria
				}
			} else if (overlay.getNumMultiPoints() > 0) {
				if (overlay.getNumGeomCol() == 0 && overlay.getNumLines() == 0
						&& overlay.getNumMultiLines() == 0
						&& overlay.getNumMultiPol() == 0
						&& overlay.getNumPol() == 0) {
					// poner leyenda de multipuntos
					overlay.setGeomType(FeatureOverlay.TYPE_MULTIPOINT);
				} else {
					// poner multigeometria
					overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
				}
			} else if (overlay.getNumLines() > 0) {
				if (overlay.getNumGeomCol() == 0
						&& overlay.getNumMultiLines() == 0
						&& overlay.getNumMultiPol() == 0
						&& overlay.getNumPol() == 0) {
					// poner leyenda de lineas
					overlay.setGeomType(FeatureOverlay.TYPE_LINE);
				} else {
					// poner multigeometria
					overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
				}
			} else if (overlay.getNumMultiLines() > 0) {
				if (overlay.getNumGeomCol() == 0
						&& overlay.getNumMultiPol() == 0
						&& overlay.getNumPol() == 0) {
					// poner leyenda de multilineas
					overlay.setGeomType(FeatureOverlay.TYPE_MULTILINE);
				} else {
					// poner multigeometria
					overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
				}
			} else if (overlay.getNumPol() > 0) {
				if (overlay.getNumGeomCol() == 0
						&& overlay.getNumMultiPol() == 0) {
					// poner leyenda de poligono
					overlay.setGeomType(FeatureOverlay.TYPE_POLYGON);
				} else {
					// poner multigeometria
					overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
				}
			} else if (overlay.getNumMultiPol() > 0) {
				if (overlay.getNumGeomCol() == 0) {
					// poner leyenda de multipoligono
					overlay.setGeomType(FeatureOverlay.TYPE_MULTIPOLYGON);
				} else {
					// poner multigeometria
					overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
				}
			} else if (overlay.getNumGeomCol() > 0) {
				// poner multigeometria
				overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
			} else {
				// no tiene geometrias
				overlay.setGeomType(FeatureOverlay.TYPE_GEOMETRYCOLLECTION);
			}
		}
	}
}
