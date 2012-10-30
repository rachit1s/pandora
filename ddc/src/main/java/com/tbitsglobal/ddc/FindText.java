import java.io.File;

import com.abbyy.FREngine.BlockTypeEnum;
import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.FieldMarkingTypeEnum;
import com.abbyy.FREngine.IBlock;
import com.abbyy.FREngine.IEngine;
import com.abbyy.FREngine.IFRDocument;
import com.abbyy.FREngine.ILayout;
import com.abbyy.FREngine.ILayoutBlocks;
import com.abbyy.FREngine.IPageProcessingParams;
import com.abbyy.FREngine.IParagraphs;
import com.abbyy.FREngine.IPrepareImageMode;
import com.abbyy.FREngine.IRegion;
import com.abbyy.FREngine.IText;
import com.abbyy.FREngine.ITextBlock;
import com.abbyy.FREngine.RotationTypeEnum;
import com.abbyy.FREngine.TextTypeEnum;
import com.abbyy.FREngine.WritingStyleEnum;

public class FindText {

	public static DocumentsData getData(File file) {
		DocumentsData data = null;
		try {

			// 4584,3309
			// 4762,3361
			//
			// 4763,3310
			// 4811,3360

			// 4430,3258
			// 4608,3310
			//
			// 4609,3259
			// 4657,3309

			// 4435,3270
			// 4730, 3344
			//
			// 4733,3274
			// 4791,3344

	
			
			int dLeft = 1247;
			int dTop = 3153;
			int dRight = 1628;
			int dBottom = 3192;
			
			int dLeft1 = 1244;
			int dTop1 = 3201;
			int dRight1 = 1616;
			int dBottom1 = 3243;
			

			int dLeft2 = 3819;
			int dTop2 = 3284;
			int dRight2 = 4381;
			int dBottom2 = 3353;
			
			int rLeft = 4621;
			int rTop = 3284;
			int rRight = 4706;
			int rBottom = 3357;
			
			
			

			FindText application = new FindText();
			data = application.Run(file, dLeft, dTop, dRight, dBottom,dLeft1, dTop1, dRight1, dBottom1, dLeft2, dTop2, dRight2, dBottom2,rLeft,
					rTop, rRight, rBottom);
		} catch (Exception ex) {
			displayMessage(ex.getMessage());
		}
		return data;
	}

	public static void main(String args[]) {
		DocumentsData data = null;
		try {

			// 4584,3309
			// 4762,3361
			//
			// 4763,3310
			// 4811,3360

			// 4430,3258
			// 4608,3310
			//
			// 4609,3259
			// 4657,3309

			// 4435,3270
			// 4730, 3344
			//
			// 4733,3274
			// 4791,3344

			
//			int dLeft = 1259;
//			int dTop = 3122;
//			int dRight = 1675;
//			int dBottom = 3168;
//			
//			int dLeft1 = 1259;
//			int dTop1 = 3176;
//			int dRight1 = 1675;
//			int dBottom1 = 3218;
//			
//
//			int dLeft2 = 3850;
//			int dTop2 = 3257;
//			int dRight2 = 4411;
//			int dBottom2 = 3336;
//			
//			int rLeft = 4652;
//			int rTop = 3261;
//			int rRight = 4741;
//			int rBottom = 3341;
			
			int dLeft = 1247;
			int dTop = 3153;
			int dRight = 1628;
			int dBottom = 3192;
			
			int dLeft1 = 1244;
			int dTop1 = 3201;
			int dRight1 = 1616;
			int dBottom1 = 3243;
			

			int dLeft2 = 3819;
			int dTop2 = 3284;
			int dRight2 = 4381;
			int dBottom2 = 3353;
			
			int rLeft = 4621;
			int rTop = 3284;
			int rRight = 4706;
			int rBottom = 3357;
			
			FindText application = new FindText();
			//CLBJ1542D010014_2_AF1.png
			// file37c976a6-3250-4fb5-b1b2-3daad1e62017.png
			data = application.Run(new File("D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\14-3-600MP0053-02012-DR-ME-0005_A_21.pdf"), dLeft, dTop, dRight, dBottom,dLeft1, dTop1, dRight1, dBottom1, dLeft2, dTop2, dRight2, dBottom2,rLeft,
					rTop, rRight, rBottom);
			data.print();
		} catch (Exception ex) {
			displayMessage(ex.getMessage());
		}
		
	}

	public DocumentsData Run(File file, int dLeft, int dTop, int dRight,
			int dBottom, int dLeft1, int dTop1, int dRight1, int dBottom1, int dLeft2, int dTop2, int dRight2, int dBottom2,int rLeft, int rTop, int rRight, int rBottom) {
		// Load ABBYY FineReader Engine
		DocumentsData data = null;
		loadEngine();
		setupFREngine();
		try {
			// Process with ABBYY FineReader Engine
			data = processImage(file, dLeft, dTop, dRight, dBottom,dLeft1, dTop1, dRight1, dBottom1, dLeft2, dTop2, dRight2, dBottom2,rLeft,
					rTop, rRight, rBottom);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Unload ABBYY FineReader Engine
			unloadEngine();
		}
		return data;
	}

	private void loadEngine() {
		displayMessage("Initializing Engine...");
		engine = Engine.LoadEx(SamplesConfig.GetDllFolder(),
				SamplesConfig.GetDeveloperSN(), null, null, null,null);
	}

	private DocumentsData processImage(File file,int dLeft, int dTop, int dRight,
			int dBottom, int dLeft1, int dTop1, int dRight1, int dBottom1, int dLeft2, int dTop2, int dRight2, int dBottom2,int rLeft, int rTop, int rRight, int rBottom) {
		DocumentsData data = new DocumentsData();
		IPrepareImageMode mode = engine.CreatePrepareImageMode();
		//mode.setCorrectSkewByHorizontalText(true);
		//mode.setUseFastBinarization(true);
		

		// IFRDocument document =
		// engine.CreateFRDocumentFromImage("C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title2.jpg",mode);
		IFRDocument document = engine.CreateFRDocumentFromImage(
				file.getAbsolutePath(), mode);
		System.out.println("height:"
				+ document.getPages().Item(0).getLayout().getHeight());
		System.out.println("width:"
				+ document.getPages().Item(0).getLayout().getWidth());
		System.out.println("text:" + document.getPlainText().getText());

		RotationTypeEnum rotation = document.getPages().Item(0)
				.DetectOrientation(null,null , null).getRotationType();
		System.out.println("rotation:" + rotation);
		if (rotation == RotationTypeEnum.RT_Clockwise) {
			document.getPages()
					.Item(0)
					.getImageDocument()
					.Transform(RotationTypeEnum.RT_Counterclockwise, false,
							false);
		} else if (rotation == RotationTypeEnum.RT_Counterclockwise) {
			document.getPages().Item(0).getImageDocument()
					.Transform(RotationTypeEnum.RT_Clockwise, false, false);
		} else if (rotation == RotationTypeEnum.RT_Upsidedown) {
			document.getPages().Item(0).getImageDocument()
					.Transform(RotationTypeEnum.RT_Upsidedown, false, false);
		}
		
//		document.getPages().Item(0).getImageDocument().SaveModified();

		rotation = document.getPages().Item(0).getImageDocument()
				.getImageRotation();
		System.out.println("rotation:" + rotation);
		System.out.println("text:" + document.getPlainText().getText());
		try {
			// Add image file to document
			displayMessage("Loading image...");
			// String imagePath =
			// "C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title1.png";
			// document.AddImageFile(imagePath, null, null);
			IPageProcessingParams params = engine.CreatePageProcessingParams();
			
			// IRecognizerParams rParams = engine.CreateRecognizerParams();
			// rParams.setFieldMarkingType(FieldMarkingTypeEnum.FMT_SimpleComb);
			//
			//
			// params.setRecognizerParams(rParams);

			ILayout layout1 = engine.CreateLayout();
			IRegion pRegion = engine.CreateRegion();
			// left, top, right, bottom
			pRegion.AddRect(dLeft, dTop, dRight, dBottom);

			IBlock newBlock = layout1.AddBlock(BlockTypeEnum.BT_Text, pRegion);
			ITextBlock textBlock = newBlock.GetAsTextBlock();
			// Specify the text type
			textBlock.getRecognizerParams().setTextType(
					TextTypeEnum.TT_ToBeDetected);
			// Specify the type of marking around the letters
			textBlock.getRecognizerParams().setFieldMarkingType(
					FieldMarkingTypeEnum.FMT_SimpleComb);
			// Specify the letters writing style
			textBlock.getRecognizerParams().setWritingStyle(
					WritingStyleEnum.WS_American);
			
			IRegion pRegion2 = engine.CreateRegion();
			// left, top, right, bottom
			pRegion2.AddRect(dLeft1, dTop1, dRight1, dBottom1);

			IBlock newBlock2 = layout1.AddBlock(BlockTypeEnum.BT_Text, pRegion2);
			ITextBlock textBlock2 = newBlock2.GetAsTextBlock();
			// Specify the text type
			textBlock2.getRecognizerParams().setTextType(
					TextTypeEnum.TT_ToBeDetected);
			// Specify the type of marking around the letters
			textBlock2.getRecognizerParams().setFieldMarkingType(
					FieldMarkingTypeEnum.FMT_SimpleComb);
			// Specify the letters writing style
			textBlock2.getRecognizerParams().setWritingStyle(
					WritingStyleEnum.WS_American);

			IRegion pRegion3 = engine.CreateRegion();
			// left, top, right, bottom
			pRegion3.AddRect(dLeft2, dTop2, dRight2, dBottom2);

			IBlock newBlock3 = layout1.AddBlock(BlockTypeEnum.BT_Text, pRegion3);
			ITextBlock textBlock3 = newBlock3.GetAsTextBlock();
			// Specify the text type
			textBlock3.getRecognizerParams().setTextType(
					TextTypeEnum.TT_ToBeDetected);
			// Specify the type of marking around the letters
			textBlock3.getRecognizerParams().setFieldMarkingType(
					FieldMarkingTypeEnum.FMT_SimpleComb);
			// Specify the letters writing style
			textBlock3.getRecognizerParams().setWritingStyle(
					WritingStyleEnum.WS_American);

			IRegion pRegion1 = engine.CreateRegion();
			// left, top, right, bottom
			pRegion1.AddRect(rLeft, rTop, rRight, rBottom);

			IBlock newBlock1 = layout1
					.AddBlock(BlockTypeEnum.BT_Text, pRegion1);
			ITextBlock textBlock1 = newBlock1.GetAsTextBlock();
			// Specify the text type
			textBlock1.getRecognizerParams().setTextType(
					TextTypeEnum.TT_ToBeDetected);
			// Specify the type of marking around the letters
			textBlock1.getRecognizerParams().setFieldMarkingType(
					FieldMarkingTypeEnum.FMT_SimpleComb);
			// Specify the letters writing style
			textBlock1.getRecognizerParams().setWritingStyle(
					WritingStyleEnum.WS_American);

			displayMessage("Process...");
			engine.RecognizePage(
					document.getPages().Item(0).getImageDocument(), null, null,
					layout1, null);

			ILayoutBlocks blocks = layout1.getBlocks();
			String option;
			for (int j = 0; j < blocks.getCount(); j++) {
				IBlock block = blocks.Item(j);
				System.out.println(block.getType());

				if (block.getType() == BlockTypeEnum.BT_Text) {
					ITextBlock textB = block.GetAsTextBlock();
					IText text = textB.getText();
					IParagraphs paras = text.getParagraphs();
					System.out.println(paras.getCount());
					System.out.println("paras:"+paras.Item(0).getText());
					if(j==0){
						data.setDocNo1(paras.Item(0).getText());
					}
					else if(j==1){
						data.setDocNo2(paras.Item(0).getText());
					}
					
					else if(j==2){
						data.setDocNo3(paras.Item(0).getText());
					}
					else if(j==3){
						data.setRev(paras.Item(0).getText());
					}

//					if (paras.Item(0).getText().indexOf("REV") != -1) {
//
//						String value = "";
//
//						if (paras.getCount() == 1) {
//							value = paras
//									.Item(0)
//									.getText()
//									.substring(
//											paras.Item(0)
//													.getText()
//													.indexOf(
//															' ',
//															paras.Item(0)
//																	.getText()
//																	.indexOf(
//																			"REV")));
//						}
//
//						for (int i = 1; i < paras.getCount(); i++) {
//							String t1 = paras.Item(i).getText();
//							System.out.println("text:" + t1);
//							value = value.concat(t1.trim());
//						}
//
//						data.setRev(value);
//
//					} //DWG 
//					else if (paras.Item(0).getText().indexOf("FMG DR. NO.") != -1) {
//
//						String value = "";
//
//						if (paras.getCount() == 1) {
//							value = paras
//									.Item(0)
//									.getText()
//									.substring(
//											paras.Item(0)
//													.getText()
//													.indexOf(
//															' ',
//															paras.Item(0)
//																	.getText()
//																	.indexOf(
//																			"FMG DR. NO.")));
//						}
//
//						for (int i = 1; i < paras.getCount(); i++) {
//							String t1 = paras.Item(i).getText();
//							System.out.println("text:" + t1);
//							value = value.concat(t1.trim());
//						}
//
//						data.setDocNo1(value);
//
//					} else if (paras.Item(0).getText()
//							.indexOf("DRAWING NUMBER") != -1) {
//
//						String value = "";
//
//						if (paras.getCount() == 1) {
//							value = paras
//									.Item(0)
//									.getText()
//									.substring(
//											paras.Item(0)
//													.getText()
//													.indexOf(
//															' ',
//															paras.Item(0)
//																	.getText()
//																	.indexOf(
//																			"DRAWING NUMBER")));
//						}
//
//						for (int i = 1; i < paras.getCount(); i++) {
//							String t1 = paras.Item(i).getText();
//							System.out.println("text:" + t1);
//							value = value.concat(t1.trim());
//						}
//
//						data.setDocNo1(value);
//
//					}
//
//					else if (paras.Item(0).getText().indexOf("DRAWING NO") != -1) {
//
//						String value = "";
//
//						if (paras.getCount() == 1) {
//							value = paras
//									.Item(0)
//									.getText()
//									.substring(
//											paras.Item(0)
//													.getText()
//													.indexOf(
//															' ',
//															paras.Item(0)
//																	.getText()
//																	.indexOf(
//																			"DRAWING NO")));
//						}
//
//						for (int i = 1; i < paras.getCount(); i++) {
//							String t1 = paras.Item(i).getText();
//							System.out.println("text:" + t1);
//							value = value.concat(t1.trim());
//						}
//
//						data.setDocNo1(value);
//
//					}
//
				}
				System.out.println();
			}

			displayMessage("Saving results...");

		} finally {
			// Close document
			document.Close();
		}

		return data;
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
