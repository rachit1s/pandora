package qap.com.tbitsGlobal.server;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;

public class QapUtils {

	private static final String BIRT_TEMPLATE_HANDLER = "BirtTemplateHandler";

	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	public static ByteArrayOutputStream generateTransmittalNoteInHtml(
			String rptDesignFileName, QapBirtTemplateHelper kth,
			String contextPath)throws EngineException, IOException, TBitsException {
		
		TBitsReportEngine tBitsEngine;
		tBitsEngine = TBitsReportEngine.getInstance();

		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();

		String tempDir = Configuration.findAbsolutePath(PropertiesHandler
				.getProperty(transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
		reportVariables.put(BIRT_TEMPLATE_HANDLER, kth);

		// Report Design
		LOG.info("Opening RPTDesign file for preview.");
		LOG.info("Rendering...");

		// Setup rendering to HTML
		HTMLRenderOption options = new HTMLRenderOption();
		options.setImageHandler(new HTMLServerImageHandler());

		ByteArrayOutputStream htmlOS = new ByteArrayOutputStream();
		options.setOutputStream(htmlOS);
		options.setOutputFormat("html");

		options.setBaseImageURL(contextPath + "/web/images/dashboard_images");
		options.setImageDirectory(tempDir
				+ "/../webapps/web/images/dashboard_images");

		// Setting this to true removes html and body tags
		options.setEmbeddable(true);
		tBitsEngine.generateReportFile(rptDesignFileName, reportVariables,
				reportParams, options);
		return htmlOS;

	}

}
