
import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.IBaseLanguage;
import com.abbyy.FREngine.IDictionary;
import com.abbyy.FREngine.IDictionaryDescription;
import com.abbyy.FREngine.IDictionaryDescriptions;
import com.abbyy.FREngine.IEngine;
import com.abbyy.FREngine.IFRDocument;
import com.abbyy.FREngine.IPrepareImageMode;
import com.abbyy.FREngine.ITextLanguage;
import com.abbyy.FREngine.IUserDictionaryDescription;
import com.abbyy.FREngine.LanguageIdEnum;

public class MyUserDictionary {

	public static void main(String[] args) {
		try {
			// int dLeft =1807;
			// int dTop=427;
			// int dRight=3146;
			// int dBottom=590;
			//
			// int rLeft=3146;
			// int rTop=427;
			// int rRight=3337;
			// int rBottom=590;
			//
			MyUserDictionary application = new MyUserDictionary();
			application.Run();
		} catch (Exception ex) {
			displayMessage(ex.getMessage());
		}
	}

	public void Run() {
		// Load ABBYY FineReader Engine
		loadEngine();
		setupFREngine();
		try {
			// Process with ABBYY FineReader Engine
			processImage();
		} catch (Exception e) {
			e.printStackTrace();
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

	private void processImage() {
		IPrepareImageMode mode = engine.CreatePrepareImageMode();
		mode.setCorrectSkewByHorizontalText(true);
		mode.setUseFastBinarization(true);

		IFRDocument document = engine.CreateFRDocumentFromImage(
				"C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title2.jpg",
				mode);

		try {
			// Add image file to document
			displayMessage("Loading image...");
			// String imagePath =
			// "C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title1.png";
			// document.AddImageFile(imagePath, null, null);

		} finally {
			// Close document
			document.Close();
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

	public ITextLanguage MakeTextLanguage() {
		// Create new dictionary
		String dictionaryFile = "D:\\sample.amd";
		IDictionary pDictionary = engine.CreateNewDictionary(dictionaryFile,
				LanguageIdEnum.LI_EnglishUnitedStates);
		pDictionary.setName("Sample");
		// Add words to dictionary
		pDictionary.AddWord("the", 100);
		pDictionary.AddWord("a", 100);
		pDictionary.AddWord("an", 100);
		
		// Create new TextLanguage object
		ITextLanguage pTextLanguage = engine.CreateTextLanguage();
		// Copy all attributes from predefined English language
		ITextLanguage pEnglishLanguage = engine.getPredefinedLanguages()
				.FindLanguage("English").getTextLanguage();
		pTextLanguage.CopyFrom(pEnglishLanguage);
		pTextLanguage.setInternalName("SampleTL");
		
		// Bind new dictionary to first (and single) BaseLanguage object within
		// TextLanguage
		IBaseLanguage pBaseLanguage = pTextLanguage.getBaseLanguages().Item(0);
		// Change internal dictionary name to user-defined
		pBaseLanguage.setInternalName("SampleBL");
		// Get collection of dictionary descriptions and remove all items

		
		IDictionaryDescriptions pDictionaryDescriptions = pBaseLanguage
				.getDictionaryDescriptions();
		pDictionaryDescriptions.RemoveAll();
		// Create user dictionary description and add it to the collection
		IUserDictionaryDescription userDic = engine.CreateUserDictionaryDesc();//CreateUserDictionaryDesc();
		//userDic.setFileName(dictionaryFile);
		DictionaryDescription description = (UserDictionaryDescription)userDic;
		
		pDictionaryDescriptions.Add(userDic);
		
		return pTextLanguage;
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
