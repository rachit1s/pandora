// � ABBYY. 2011.
// SAMPLES code is property of ABBYY, exclusive rights are reserved. 
//
// DEVELOPER is allowed to incorporate SAMPLES into his own APPLICATION and modify it under 
// the  terms of  License Agreement between  ABBYY and DEVELOPER.

// ABBYY FineReader Engine 10 Sample

// This sample shows basic steps of ABBYY FineReader Engine usage:
// Initializing, opening image file, recognition and export.

import java.io.PrintStream;

import com.abbyy.FREngine.BlockTypeEnum;
import com.abbyy.FREngine.DocumentElementTypeEnum;
import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.FieldMarkingTypeEnum;
import com.abbyy.FREngine.FileExportFormatEnum;
import com.abbyy.FREngine.IBlock;
import com.abbyy.FREngine.IDocumentElement;
import com.abbyy.FREngine.IEngine;
import com.abbyy.FREngine.IFRDocument;
import com.abbyy.FREngine.IFRPage;
import com.abbyy.FREngine.IFRPages;
import com.abbyy.FREngine.IImageDocument;
import com.abbyy.FREngine.ILayout;
import com.abbyy.FREngine.ILayoutBlocks;
import com.abbyy.FREngine.IPDFExportParams;
import com.abbyy.FREngine.IPageProcessingParams;
import com.abbyy.FREngine.IParagraph;
import com.abbyy.FREngine.IPrepareImageMode;
import com.abbyy.FREngine.IRecognizerParams;
import com.abbyy.FREngine.ISynthesisParamsForDocument;
import com.abbyy.FREngine.ITableBlock;
import com.abbyy.FREngine.ITableCell;
import com.abbyy.FREngine.ITableCells;
import com.abbyy.FREngine.IText;
import com.abbyy.FREngine.ITextTable;
import com.abbyy.FREngine.PDFExportScenarioEnum;

public class Structure {

	public static void main(String[] args) {
		try {
			Structure application = new Structure();
			application.Run();
		} catch (Exception ex) {
			displayMessage(ex.getMessage());
		}
	}

	public void Run() {
		// Load ABBYY FineReader Engine
		loadEngine();
		try {
			// Process with ABBYY FineReader Engine
			processWithEngine();
		} finally {
			// Unload ABBYY FineReader Engine
			unloadEngine();
		}
	}

	private void loadEngine() {
		displayMessage("Initializing Engine...");
		engine = Engine.Load(SamplesConfig.GetDllFolder(),
				SamplesConfig.GetDeveloperSN(), null, null);
	}

	private void processWithEngine() {
		try {
			// Setup FREngine
			setupFREngine();

			// Process sample image
			processImage();
		} catch (Exception ex) {
			displayMessage(ex.getMessage());
		}
	}

	private void setupFREngine() {
		displayMessage("Loading predefined profile...");
		engine.LoadPredefinedProfile("DocumentConversion_Accuracy");
		// Possible profile names are:
		// "DocumentConversion_Accuracy", "DocumentConversion_Speed",
		// "DocumentArchiving_Accuracy", "DocumentArchiving_Speed",
		// "BookArchiving_Accuracy", "BookArchiving_Speed",
		// "TextExtraction_Accuracy", "TextExtraction_Speed",
		// "FieldLevelRecognition",
		// "BarcodeRecognition",
		// "Version9Compatibility",
		// "Default"
	}

	private void processImage() {

		IPrepareImageMode mode = engine.CreatePrepareImageMode();

		mode.setCorrectSkewByHorizontalText(true);
		mode.setUseFastBinarization(true);

		// mode.setInvertImage(true);
		// mode.setRotation(RotationTypeEnum.RT_Upsidedown);

		// mode.SaveToFile(
		// "C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title31.png");

		// Create document
		// IFRDocument document =
		// engine.CreateFRDocumentFromImage("C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title2.jpg",mode);
		IFRDocument document = engine.CreateFRDocumentFromImage(
				"D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\16-2-600MP0053-02012-DR-ME-0017_A_2.pdf", mode);
		// IFRDocument document =
		// engine.CreateFRDocumentFromImage("D:\\ABC.png",mode);

		// document.SaveToFolder("C:\\Users\\Rahu\\Downloads\\images");

		// System.out.println(mode.getRotation());
		// IFRDocument document = engine.CreateFRDocument();
		try {
			// Add image file to document
			displayMessage("Loading image...");
			// String imagePath =
			// "C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title1.png";
			// document.AddImageFile(imagePath, null, null);
			IPageProcessingParams params = engine.CreatePageProcessingParams();

			IRecognizerParams rParams = engine.CreateRecognizerParams();
			rParams.setFieldMarkingType(FieldMarkingTypeEnum.FMT_SimpleComb);

			params.setRecognizerParams(rParams);

			ISynthesisParamsForDocument docParam = engine
					.CreateSynthesisParamsForDocument();
			docParam.setDetectDocumentStructure(true);
			// Process document
			displayMessage("Process...");
			document.Process(params, null, docParam);
			IFRPages pages = document.getPages();
			int pagesCount = pages.getCount();
			int dLeft = -1;
			int dRight = -1;
			int dTop = -1;
			int dBottom = -1;

			int rLeft = -1;
			int rRight = -1;
			int rTop = -1;
			int rBottom = -1;
			
			

			for (int s = 0; s < document.getDocumentStructure()
					.getDocumentSectionsCount(); s++) {
				for (int a = 0; a < document.getDocumentStructure()
						.getDocumentSection(s).getDocumentStreamsCount(); a++) {
					IDocumentElement ele = document.getDocumentStructure()
							.getDocumentSection(s).getDocumentStream(a)
							.getFirstElement();
					System.out.println("ele:");
					DocumentElementTypeEnum e = ele.getType();
					while (true) {
						System.out.print(ele.getType() + ",");
						if (ele.equals(document.getDocumentStructure()
								.getDocumentSection(s).getDocumentStream(a)
								.getLastElement())) {
							break;
						}

						ITextTable table = ele.GetAsTable();
						if (table != null) {
							for (int b = 0; b < table.getCellsCount(); b++) {
								System.out.println(table.getCell(b)
										.getElement().getType());
								System.out.println(table.getCell(b)
										.getElement().GetAsTable());
								IText text = table.getCell(b).getElement()
										.GetAsText();
								for (int c = 0; c < text.getParagraphs()
										.getCount(); c++) {
									System.out.print(text.getParagraphs()
											.Item(c).getText()
											+ ",");
								}
								System.out.println();
							}
						}
						
						IParagraph para = ele.GetAsParagraph();
						if (para != null) {
							
								
//								for (int c = 0; c < para.getParagraphs()
//										.getCount(); c++) {
//									System.out.print(text.getParagraphs()
//											.Item(c).getText()
//											+ ",");
//								}
								System.out.println(para.getText());
							
						}
						
						ele = document.getDocumentStructure()
								.getDocumentSection(s).getDocumentStream(a)
								.getNextElement(ele);
						if (ele == null) {
							break;
						}

					}
					System.out.println();
				}
				System.out.println();
			}

			for (int i = 0; i < pagesCount; i++) {
				IFRPage page = pages.Item(i);
				System.out.println("Xres:"+page.getImageDocument().getSourceImageXResolution());
				System.out.println("Yres:"+page.getImageDocument().getSourceImageYResolution());
				// page.RemoveGeometricalDistortions(null);
				System.out.println("oreint:"
						+ page.DetectOrientation(null, null, null)
								.getRotationType());

				IImageDocument doc = page.getImageDocument();
				System.out.println(doc.getImageColorType());

				doc.DescreenImage();

				// doc.DitherImage(DitheringMethodEnum.DM_Bayer);
				// doc.RemoveCameraBlur(null);
				// doc.RemoveCameraNoise(null);
				// doc.EvenBrightness();

				doc.RemoveGarbage(null, -1);
				// doc.SaveToFile("D:\\ABC.png");

				doc.CorrectSkew(1);
				System.out.println(doc.getImageRotation());

//				doc.getBlackWhiteImage().WriteToFile("D:\\DEF.png",
//						ImageFileFormatEnum.IFF_PngBwPng, null, null);

				ILayout layout = page.getLayout();
				ILayoutBlocks blocks = layout.getBlocks();
				int blocksCount = blocks.getCount();
				for (int j = 0; j < blocksCount; j++) {

					IBlock block = blocks.Item(j);

					System.out.println(block.getType());

					if (block.getType() == BlockTypeEnum.BT_Text) {
						int size = block.GetAsTextBlock().getText()
								.getParagraphs().getCount();
						System.out.println("size:" + size);
						String t = new String();
						for (int q = 0; q < size; q++) {
							String p = block.GetAsTextBlock().getText()
									.getParagraphs().Item(q).getText()
									.concat(" ");
							t = t.concat(p);
						}

						System.out.println(j + ":" + t);
					} else if (block.getType() == BlockTypeEnum.BT_Table) {

						ITableBlock tableBlock = block.GetAsTableBlock();

						// ITableSeparators separators =
						// tableBlock.getVSeparators();
						// for(int f=0;f<separators.getCount();f++){
						// System.out.println("sep:"+separators.Item(f).getPosition()+","+separators.Item(f).getType(f));
						// }

						ITableCells cells = tableBlock.getCells();

						for (int k = 0; k < cells.getCount(); k++) {
							ITableCell cell = cells.Item(k);

							IBlock cellBlock = cell.getBlock();

							System.out.println("type:" + cellBlock.getType());
							if (cellBlock.getType() == BlockTypeEnum.BT_Text) {

								int size = cellBlock.GetAsTextBlock().getText()
										.getParagraphs().getCount();
								System.out.println("size:" + size);
								String t = new String();
								for (int q = 0; q < size; q++) {
									String p = cellBlock.GetAsTextBlock()
											.getText().getParagraphs().Item(q)
											.getText().concat(" ");
									t = t.concat(p);
								}

								System.out.println(k + ":" + t);
								try {
									PrintStream out = new PrintStream(
											System.out, true, "UTF-8");

									out.println(t);
								} catch (Exception e) {
									e.printStackTrace();
								}

								System.out.println();
								if (dLeft == -1) {
									int index = t.toLowerCase().indexOf(
											"DRAWING NUMBER".toLowerCase());

									if (index != -1) {
										t = t.replaceFirst("DRAWING NUMBER", "");

										if (t.trim().length() > 1) {
											System.out
													.println("DRAWING NUMBER:"
															+ t.trim());

										}
										{
											dLeft = cellBlock.getRegion()
													.getLeft(0);
											dRight = cellBlock.getRegion()
													.getRight(0);
											dTop = cellBlock.getRegion()
													.getTop(0);
											dBottom = cellBlock.getRegion()
													.getBottom(0);
											System.out
													.println("dLeft:" + dLeft);
											System.out.println("dRight:"
													+ dRight);
											System.out.println("dTop:" + dTop);
											System.out.println("dBottom:"
													+ dBottom);
										}
										continue;
									}

									index = t.toLowerCase().indexOf(
											"DRG No".toLowerCase());
									if (index != -1) {
										t = t.replaceFirst("DRG No", "");
										if (t.trim().length() > 1) {
											System.out
													.println("DRAWING NUMBER:"
															+ t.trim());

										}
										{
											dLeft = cellBlock.getRegion()
													.getLeft(0);
											dRight = cellBlock.getRegion()
													.getRight(0);
											dTop = cellBlock.getRegion()
													.getTop(0);
											dBottom = cellBlock.getRegion()
													.getBottom(0);
											System.out
													.println("dLeft:" + dLeft);
											System.out.println("dRight:"
													+ dRight);
											System.out.println("dTop:" + dTop);
											System.out.println("dBottom:"
													+ dBottom);
										}
										continue;
									}
								}
								if (rLeft == -1) {
									int index = t.toLowerCase().indexOf(
											"REV".toLowerCase());
									if (index != -1) {
										t = t.replaceFirst("REV", "");
										if (t.trim().length() > 1) {
											System.out.println("REV:"
													+ t.trim());

										}
										{
											rLeft = cellBlock.getRegion()
													.getLeft(0);
											rRight = cellBlock.getRegion()
													.getRight(0);
											rTop = cellBlock.getRegion()
													.getTop(0);
											rBottom = cellBlock.getRegion()
													.getBottom(0);

											System.out
													.println("rLeft:" + rLeft);
											System.out.println("rRight:"
													+ rRight);
											System.out.println("rTop:" + rTop);
											System.out.println("rBottom:"
													+ rBottom);
										}
									}
									continue;
								}

								if (dLeft != -1) {
									if ((cellBlock.getRegion().getLeft(0) == dLeft && cellBlock
											.getRegion().getRight(0) == dRight)
											|| (cellBlock.getRegion().getTop(0) == dTop && cellBlock
													.getRegion().getBottom(0) == dBottom)) {
										System.out.println("DRAWING NUMBER:"
												+ t.trim());
									}
								}
								if (rLeft != -1) {
									if ((cellBlock.getRegion().getLeft(0) == rLeft && cellBlock
											.getRegion().getRight(0) == rRight)
											|| (cellBlock.getRegion().getTop(0) == rTop && cellBlock
													.getRegion().getBottom(0) == rBottom)) {
										System.out.println("REV:" + t.trim());
									}
								}
							}
						}
					}
				}
				page.Flush();
			}

			// System.out.println(document.getPlainText().getText());

			// Save results
			displayMessage("Saving results...");

			// Save results to rtf with default parameters
			String rtfExportPath = SamplesConfig.GetSamplesFolder()
					+ "\\SampleImages\\Demo1.rtf";
			document.Export(rtfExportPath, FileExportFormatEnum.FEF_RTF, null);

			// Save results to pdf using 'balanced' scenario
			IPDFExportParams pdfParams = engine.CreatePDFExportParams();
			pdfParams.setScenario(PDFExportScenarioEnum.PES_Balanced);

			String pdfExportPath = SamplesConfig.GetSamplesFolder()
					+ "\\SampleImages\\Demo1.pdf";
			document.Export(pdfExportPath, FileExportFormatEnum.FEF_PDF,
					pdfParams);
		} finally {
			// Close document
			document.Close();
		}
	}

	private void unloadEngine() {
		displayMessage("Deinitializing Engine...");
		engine = null;
		System.gc();
		System.runFinalization();
		Engine.Unload();
	}

	public static void displayMessage(String message) {
		System.out.println(message);
	}

	private IEngine engine = null;
}
