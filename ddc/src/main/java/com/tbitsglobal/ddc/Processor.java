// © ABBYY. 2011.
// SAMPLES code is property of ABBYY, exclusive rights are reserved. 
// DEVELOPER is allowed to incorporate SAMPLES into his own APPLICATION and modify it 
// under the terms of License Agreement between ABBYY and DEVELOPER.



namespace Sample
{
    public class EnumToStringByDescription : TypeConverter
    {
        public override bool CanConvertFrom( ITypeDescriptorContext context, Type sourceType ) 
        { 
            return ( sourceType.Equals( typeof( Enum ) ) ); 
        } 
        public override bool CanConvertTo( ITypeDescriptorContext context, Type destinationType ) 
        { 
            return ( destinationType.Equals( typeof( String ) ) ); 
        } 
        public override object ConvertTo( ITypeDescriptorContext context, System.Globalization.CultureInfo culture, object value, Type destinationType ) 
        { 
            if( !destinationType.Equals( typeof( String ) ) ) { 
                throw new ArgumentException( "Can only convert to string.", "destinationType" ); 
            } 
            if( !value.GetType().BaseType.Equals( typeof( Enum ) ) ) {
                throw new ArgumentException( "Can only convert an instance of enum.", "value" ); 
            }
            string name = value.ToString();
            object[] attrs = value.GetType().GetField( name ).GetCustomAttributes( typeof(DescriptionAttribute ), false );         
            return ( attrs.Length > 0 ) ? ( (DescriptionAttribute)attrs[0] ).Description : name; 
        }
    } 

    class ProcessingSettings
    {
        public string Language = "English";

        // --- Plain image transformations

        // How engine detects image orientation
        [TypeConverter(typeof(EnumToStringByDescription))]
        public enum OrientationCorrectionMode
        {
            [Description( "None" )] 
            None,
            [Description( "Automatic" )] 
            Automatic,
            [Description( "Rotate 90 CW" )] 
            Rotate90CW,
            [Description( "Rotate 90 CCW" )] 
            Rotate90CCW,
            [Description( "Rotate 180 Upside Down" )] 
            Rotate180UpsideDown,
            [Description( "Mirror Horizontally" )] 
            MirrorHorizontally
        };
        public OrientationCorrectionMode CorrectOrientation = OrientationCorrectionMode.None;
        
        public bool CorrectSkew = false; 

        public bool RemoveGarbage = false;
        // Garbage area in pixels. -1 means detect garbage size automatically
        public int GarbageSize = -1;

        // Tell engine to change resolution to specified value
        // If these variables are less than or equal to zero, resolution is detected automatically
        public bool CorrectResolution = false;
        public int NewResolution = 0;

        public enum PageSplittingMode
        {
            None, Automatic, Manual
        };
        // Direction in which to split - used only in manual mode
        public enum PageSplittingDirection
        {
            Horizontal, Vertical
        }
        public PageSplittingMode SplitDualPages = PageSplittingMode.None;
        public PageSplittingDirection SplittingDirection = PageSplittingDirection.Vertical;
        // X- or Y- coordinate of manual split on original image
        public int PageSplittingPosition = 0;
        
        public bool InvertImage = false;

        // --- Remove color objects

        public bool SubtractColor = false;
        // Hue in HSL representation to subtract
        public byte HslHueToSubtract = 0;
        public byte HslSaturationBoundaryToSubtract = 1;


        public bool RemoveColorObjects = false;
        public enum ObjectsColorEnum { Red, Green, Blue, Yellow };
        public ObjectsColorEnum ColorToRemove = ObjectsColorEnum.Red;
        public enum ObjectsTypeEnum { Full, Background, Stamp };
        public ObjectsTypeEnum ObjectsTypeToRemove = ObjectsTypeEnum.Full;

        // --- Camera OCR

        public bool CorrectPerspective = false;
        public bool CorrectMotionBlur = false;
        public bool CorrectIsoNoise = false;

        // --- Methods to improve visual quality only

        public bool ConvertToBW = false;
        public bool EnableDithering = false; // Not yet in the API

        public enum ImageCroppingMode { Disable, Automatic, Manual }
        public ImageCroppingMode CropImage = ImageCroppingMode.Disable;
        // Cropping offset from image borders for each direction
        // Used only in manual mode
        public uint LeftCroppingOffset = 0;
        public uint TopCroppingOffset = 0;
        public uint RightCroppingOffset = 0;
        public uint BottomCroppintOffset = 0;

        public bool DescreenImage = false; // Not yet in the API

        public bool EvenBackground = false;
        
        public bool IsRecognize = true;
    }

    struct PageRect 
    {
        public int PageIndex;
        public System.Drawing.Rectangle Rect;
        public PageRect( int pageIndex, int x, int y, int w, int h )
        {
            PageIndex = pageIndex;
            Rect = new System.Drawing.Rectangle( x, y, w, h );
        }
    }

    class RecognitionStatistics
    {
        // Total number of characters recognized in document
        public int TotalCharsCount;
        // Total number of suspicious characters in document
        public int SuspiciousCharsCount;
        // Total processing time
        public TimeSpan TotalProcessingTime;
        // Processed Image
        public System.Drawing.Image[] PreprocessedImages;

        // Recognized text for each page
        public List<RecognizedData.RecognizedText> RecognizedText = new List<Sample.RecognizedData.RecognizedText>();
        // Each element specifies the number of a offcut page. If page was not split the element is equal to 0.
        public int[] OffcutPageNumbers;

        public static RecognitionStatistics operator+( RecognitionStatistics a, RecognitionStatistics b ) 
        {
            RecognitionStatistics result = new RecognitionStatistics();
            result.TotalCharsCount = a.TotalCharsCount + b.TotalCharsCount;
            result.SuspiciousCharsCount = a.SuspiciousCharsCount + b.SuspiciousCharsCount;
            if (a.RecognizedText.Count > 0)
                result.RecognizedText = a.RecognizedText;
            else if (b.RecognizedText.Count > 0)
                result.RecognizedText = b.RecognizedText;
            return result;
        }
    }

    class Processor : IDisposable
    {
        // Event that is called when next step is performed
        public delegate void StepChangedEventHandler(string description);
        public event StepChangedEventHandler StepChangedEvent;

        public Processor()
        {
        }

        public void Dispose()
        {
            if (engineLoader != null)
            {
                engineLoader.Dispose();
                engineLoader = null;
            }
        }

        public void LoadEngine()
        {
            if (engineLoader == null)
            {
                setStep("Loading engine...");
                engineLoader = new EngineLoader();
            }
        }

        // Warm-up recognition engine
        // It's good to call warm-up with all settings enabled
        public void WarmUp(string imagePath, ProcessingSettings settings)
        {
            LoadEngine();
            setStep("Warming up...");
            engine.LoadModule(FREngine.FREngineModuleEnum.FREM_ImageSupport);
            engine.LoadModule(FREngine.FREngineModuleEnum.FREM_Recognizer);
            engine.LoadModule(FREngine.FREngineModuleEnum.FREM_DocumentAnalyzer);
            engine.LoadModule(FREngine.FREngineModuleEnum.FREM_EuropeanPatterns);
        }

        
        
        // Performs work with FR Engine
        // load image from imagePath and save result in rtf to resultPath
        public RecognitionStatistics Process( string imagePath, ProcessingSettings settings )
        {
            DateTime startTime = System.DateTime.Now;

            setStep("Applying profile...");
            engine.LoadPredefinedProfile("DocumentConversion_Accuracy");

            setStep("Applying settings...");
            FREngine.PrepareImageMode pim = engine.CreatePrepareImageMode();
            FREngine.PageProcessingParams ppp = engine.CreatePageProcessingParams();

            ppp.RecognizerParams.SetPredefinedTextLanguage(settings.Language);
            disableAllModifications(pim, ppp);

            pim.AutoOverwriteResolution = false;
            if (settings.CorrectResolution)
            {
                if (settings.NewResolution == 0)
                {
                    pim.AutoOverwriteResolution = true;
                }
                else if (settings.NewResolution > 0)
                {
                    pim.OverwriteResolution = true;
                    pim.XResolutionToOverwrite = settings.NewResolution;
                    pim.YResolutionToOverwrite = settings.NewResolution;
                }
            }

            // Detect orientation for all pages
            setStep("Detecting orientation...");
            FREngine.RotationTypeEnum[] rotation = null;
            if (settings.CorrectOrientation == ProcessingSettings.OrientationCorrectionMode.Automatic)
                rotation = detectOrientation(imagePath, pim, ppp);
                        
            setStep("Loading image...");
            
            // Create document
            FREngine.FRDocument frDoc = engine.CreateFRDocument();

            // Add image file to document
            frDoc.AddImageFile(imagePath, pim, null);

            if (frDoc.Pages.Count == 0)
                throw new Exception("No pages in a file");

            int[] offcutPageNumbers = new int[frDoc.Pages.Count];
            
            setStep("Performing image modification...");
            for( int pageIndex = 0; pageIndex < frDoc.Pages.Count; pageIndex++ ) {
                FREngine.FRPage page = frDoc.Pages[pageIndex];

                FREngine.RotationTypeEnum rot = FREngine.RotationTypeEnum.RT_UnknownRotation;
                if (rotation != null && pageIndex < rotation.Length )
                    rot = rotation[pageIndex];

                removeDistortions(page, ppp, settings, rot);
                // If split is successfull, a new page is added to the end of frDoc
                bool isSplit = splitImage(page, frDoc, ppp, settings);
                if( pageIndex < offcutPageNumbers.Length ) {
                    offcutPageNumbers[pageIndex] = isSplit ? frDoc.Pages.Count - 1 : 0;
                }
                performFiltering(page, settings);
            }
            
            for( int pageIndex = 0; pageIndex < frDoc.Pages.Count; pageIndex++ ) {
                FREngine.IFRPage page = frDoc.Pages[pageIndex];

                // Perform modifications that affect only visual quality of image and can degrade recognition
                performVisualModifications(page, ppp, settings);
            }

            if( settings.IsRecognize ) {
                setStep("Recognizing image...");
                frDoc.Process(ppp, null, null);
            }

            TimeSpan processingTime = DateTime.Now - startTime;

            setStep( "Computing statistics..." );
            
            RecognitionStatistics recognitionStats = computeStatistics(frDoc);
            recognitionStats.OffcutPageNumbers = offcutPageNumbers;
            recognitionStats.TotalProcessingTime = processingTime;

            setStep("Retrieving images...");
            for( int pageIndex = 0; pageIndex < frDoc.Pages.Count; pageIndex++ ) {
                if( recognitionStats.PreprocessedImages == null ) {
                    recognitionStats.PreprocessedImages = new System.Drawing.Image[frDoc.Pages.Count];
                }

                FREngine.IFRPage page = frDoc.Pages[pageIndex];
                
                FREngine.IImageDocument imageDoc = page.ImageDocument;
                FREngine.IImage image = imageDoc.BlackWhiteImage;
                if (settings.ConvertToBW == false &&
                     imageDoc.ImageColorType != FREngine.ImageColorTypeEnum.ICT_BlackWhite)
                {
                    image = imageDoc.ColorImage;
                }
             
                IntPtr hBitmap = (IntPtr)image.GetPicture( null, 0 );
                recognitionStats.PreprocessedImages[pageIndex] = System.Drawing.Image.FromHbitmap( hBitmap );
                Gdi32.DeleteObject( hBitmap );
            }

            frDoc.Close();

            return recognitionStats;
        }

        private void disableAllModifications(FREngine.PrepareImageMode pim, FREngine.PageProcessingParams ppp)
        {
            pim.CorrectSkewMode = 0;
            pim.AutoOverwriteResolution = false;

            ppp.DetectOrientation = false;
            ppp.DetectInvertedImage = false;
            ppp.RemoveGeometricalDistortions = false;
        }

        // Detect orientation of all pages in a document
        // Return array with orientations for all pages
        private FREngine.RotationTypeEnum[] detectOrientation(string imagePath, FREngine.PrepareImageMode pim, FREngine.PageProcessingParams ppp)
        {
            // Correct skew during loading
            FREngine.PrepareImageMode localPim = engine.CreatePrepareImageMode();
            localPim.CopyFrom(pim);
            localPim.CorrectSkewMode = (int)(FREngine.CorrectSkewModeEnum.CSM_CorrectSkewByHorizontalText |
                FREngine.CorrectSkewModeEnum.CSM_CorrectSkewByVerticalText);

            FREngine.FRDocument frDoc = engine.CreateFRDocument();
            frDoc.AddImageFile(imagePath, localPim, null);

            // Get orientation for every page
            int pagesCount = frDoc.Pages.Count;
            FREngine.RotationTypeEnum[] rotations = new FREngine.RotationTypeEnum[pagesCount];
            for (int i = 0; i < pagesCount; i++)
            {
                FREngine.FRPage page = frDoc.Pages[i];
                FREngine.TextOrientation ori = page.DetectOrientation(null, ppp.ObjectsExtractionParams, ppp.RecognizerParams);
                rotations[i] = FREngine.RotationTypeEnum.RT_UnknownRotation;
                if( ori != null ) {
                    rotations[i] = ori.RotationType;
                }
            }

            return rotations;
        }
        
        // Remove geometrical distortions: rotation, skew, and straighten lines
        private void removeDistortions(FREngine.IFRPage page, FREngine.IPageProcessingParams ppp, ProcessingSettings settings,
            FREngine.RotationTypeEnum detectedRotation )
        {
            // Rotate image if necessary
            if (settings.CorrectOrientation != ProcessingSettings.OrientationCorrectionMode.None &&
                detectedRotation != FREngine.RotationTypeEnum.RT_NoRotation )
            {
                FREngine.RotationTypeEnum rotation = FREngine.RotationTypeEnum.RT_NoRotation;
                bool mirror = false;
                if (detectedRotation != FREngine.RotationTypeEnum.RT_UnknownRotation)
                {
                    switch (detectedRotation)
                    {
                        case FREngine.RotationTypeEnum.RT_Clockwise:
                            rotation = FREngine.RotationTypeEnum.RT_Counterclockwise;
                            break;
                        case FREngine.RotationTypeEnum.RT_Upsidedown:
                            rotation = FREngine.RotationTypeEnum.RT_Upsidedown;
                            break;
                        case FREngine.RotationTypeEnum.RT_Counterclockwise:
                            rotation = FREngine.RotationTypeEnum.RT_Clockwise;
                            break;
                        default:
                            break;
                    }
                }
                else
                {
                    switch (settings.CorrectOrientation)
                    {
                        case ProcessingSettings.OrientationCorrectionMode.Rotate90CW:
                            rotation = FREngine.RotationTypeEnum.RT_Clockwise;
                            break;
                        case ProcessingSettings.OrientationCorrectionMode.Rotate180UpsideDown:
                            rotation = FREngine.RotationTypeEnum.RT_Upsidedown;
                            break;
                        case ProcessingSettings.OrientationCorrectionMode.Rotate90CCW:
                            rotation = FREngine.RotationTypeEnum.RT_Counterclockwise;
                            break;
                        case ProcessingSettings.OrientationCorrectionMode.MirrorHorizontally:
                            mirror = true;
                            break;
                        default:
                            break;
                    }
                }

                // Perform rotation
                page.ImageDocument.Transform(rotation, mirror, false);
            }

            // Correct 3d distortions
            if (settings.CorrectPerspective)
            {
                page.RemoveGeometricalDistortions(ppp.ObjectsExtractionParams);
            }

            // Perform deskew
            if (settings.CorrectSkew)
            {
                int skewCorrectionMode = (int)FREngine.CorrectSkewModeEnum.CSM_CorrectSkewByHorizontalText + (int)FREngine.CorrectSkewModeEnum.CSM_CorrectSkewByVerticalText;
                page.ImageDocument.CorrectSkew(skewCorrectionMode);
            }

        }

        private void performFiltering(FREngine.IFRPage page, ProcessingSettings settings)
        {
            if (settings.RemoveGarbage)
            {
                page.ImageDocument.RemoveGarbage(null, settings.GarbageSize);
            }

            if (settings.CorrectIsoNoise)
            {
                page.ImageDocument.RemoveCameraNoise(null);
            }

            if (settings.CorrectMotionBlur)
            {
                page.ImageDocument.RemoveCameraBlur(null);
            }

            if (settings.InvertImage)
            {
                page.ImageDocument.Transform(FREngine.RotationTypeEnum.RT_NoRotation, false, true);
            }

            if (settings.SubtractColor)
            {
                page.ImageDocument.SubtractColor(settings.HslHueToSubtract, settings.HslSaturationBoundaryToSubtract);
            }

            if (settings.RemoveColorObjects)
            {
                FREngine.ObjectsColorEnum colorToRemove = FREngine.ObjectsColorEnum.OC_Blue;
                switch (settings.ColorToRemove)
                {
                    case ProcessingSettings.ObjectsColorEnum.Red:
                        colorToRemove = FREngine.ObjectsColorEnum.OC_Red; break;
                    case ProcessingSettings.ObjectsColorEnum.Green:
                        colorToRemove = FREngine.ObjectsColorEnum.OC_Green; break;
                    case ProcessingSettings.ObjectsColorEnum.Blue:
                        colorToRemove = FREngine.ObjectsColorEnum.OC_Blue; break;
                    case ProcessingSettings.ObjectsColorEnum.Yellow:
                        colorToRemove = FREngine.ObjectsColorEnum.OC_Yellow; break;
                }

                FREngine.ObjectsTypeEnum objectsTypeToRemove = FREngine.ObjectsTypeEnum.OT_Background;
                switch (settings.ObjectsTypeToRemove)
                {
                    case ProcessingSettings.ObjectsTypeEnum.Full:
                        objectsTypeToRemove = FREngine.ObjectsTypeEnum.OT_Full; break;
                    case ProcessingSettings.ObjectsTypeEnum.Background:
                        objectsTypeToRemove = FREngine.ObjectsTypeEnum.OT_Background; break;
                    case ProcessingSettings.ObjectsTypeEnum.Stamp:
                        objectsTypeToRemove = FREngine.ObjectsTypeEnum.OT_Stamp; break;
                }

                page.ImageDocument.RemoveColorObjects(null, colorToRemove, objectsTypeToRemove);
            }
        }

        private bool splitImage(FREngine.FRPage page, FREngine.IFRDocument frDoc, FREngine.PageProcessingParams ppp, ProcessingSettings settings)
        {
            if (settings.SplitDualPages == ProcessingSettings.PageSplittingMode.None)
            {
                return false;
            }

            int splitStartPosition = settings.PageSplittingPosition;
            int splitEndPosition = settings.PageSplittingPosition;
            bool isVerticalSplit = true;

            if (settings.SplitDualPages == ProcessingSettings.PageSplittingMode.Automatic)
            {

                FREngine.TextOrientation ori = page.DetectOrientation(null, ppp.ObjectsExtractionParams, ppp.RecognizerParams);
                // There should be no rotation detected
                
                FREngine.PageSplitDirectionEnum pageSplitDirection;
                page.FindPageSplitPosition(ppp.ObjectsExtractionParams, ori, out pageSplitDirection, out splitStartPosition, out splitEndPosition);

                if (pageSplitDirection == FREngine.PageSplitDirectionEnum.PSD_NoSplit)
                {
                    return false;
                }
                else if (pageSplitDirection == FREngine.PageSplitDirectionEnum.PSD_HorizontalSplit)
                {
                    isVerticalSplit = false;
                }
            }
            else
            {
                if (settings.SplittingDirection == ProcessingSettings.PageSplittingDirection.Horizontal)
                {
                    isVerticalSplit = false;
                }
            }

            // Crop the image and select left/top (if leaveFirstPage == true) or right/bottom part
            

            // Image dimensions
            int width = page.Layout.Width;
            int height = page.Layout.Height;

            // Duplicate page
            frDoc.AddPage(page);
            FREngine.FRPage secondPage = frDoc.Pages[frDoc.Pages.Count - 1];

            // Crop first and second pages
            FREngine.ImageModification firstModification = engine.CreateImageModification();
            FREngine.Region firstClipRegion = engine.CreateRegion();
            FREngine.ImageModification secondModification = engine.CreateImageModification();
            FREngine.Region secondClipRegion = engine.CreateRegion();
            if (isVerticalSplit)
            {
                firstClipRegion.AddRect(0, 0, splitStartPosition, height - 1 );
                secondClipRegion.AddRect(splitEndPosition, 0, width - 1, height - 1);
            }
            else
            {
                firstClipRegion.AddRect(0, 0, width - 1, splitStartPosition);
                secondClipRegion.AddRect(0, splitEndPosition, width - 1, height - 1);
            }
            
            firstModification.AddClipRegion(firstClipRegion);
            page.ImageDocument.Modify(firstModification);

            secondModification.AddClipRegion(secondClipRegion);
            secondPage.ImageDocument.Modify(secondModification);

            // Correct perspective
            if (settings.CorrectPerspective)
            {
                page.RemoveGeometricalDistortions(ppp.ObjectsExtractionParams);
                secondPage.RemoveGeometricalDistortions(ppp.ObjectsExtractionParams);
            }
            // Correct skew
            if (settings.CorrectSkew)
            {
                page.ImageDocument.CorrectSkew(correctSkewFlags);
                secondPage.ImageDocument.CorrectSkew(correctSkewFlags);
            }
            return true;
        }

        private void performVisualModifications(FREngine.IFRPage page, FREngine.PageProcessingParams ppp, 
            ProcessingSettings settings)
        {
            // Crop in automatic mode is performed after document analysis
            if (settings.CropImage != ProcessingSettings.ImageCroppingMode.Disable)
            {
                page.AnalyzeAndRecognize(ppp, null);
                page.Synthesize(null);
                cropImage(page, settings);
            }

            if (settings.EvenBackground)
            {
                page.ImageDocument.EvenBrightness();
            }

            if (settings.DescreenImage)
            {
                page.ImageDocument.DescreenImage();
            }

            // Perform dithering
            if (settings.ConvertToBW && settings.EnableDithering )
            {
                page.ImageDocument.DitherImage(FREngine.DitheringMethodEnum.DM_FloydSteinberg);
            }
        }

        // Convert 1/100 of points to pixels
        private int twipsToPixels(int dpi, int twips)
        {
            return (int)(1.0 * dpi / 7200 * twips);
        }

        private void cropImage(FREngine.IFRPage page, ProcessingSettings settings)
        {
            if (settings.CropImage == ProcessingSettings.ImageCroppingMode.Disable)
            {
                return;
            }
            
            FREngine.Region clipRegion = engine.CreateRegion();

            if (settings.CropImage == ProcessingSettings.ImageCroppingMode.Automatic)
            {
                // Perform automatic cropping of the image based on data from FRPage.PageStructure.PageRect
                FREngine.FRRectangle pageRect = page.PageStructure.PageRect;

                // Convert pageRect from centipoints to pixels
                int dpiX = page.ImageDocument.BlackWhiteImage.XResolution;
                int dpiY = page.ImageDocument.BlackWhiteImage.YResolution;

                int left = twipsToPixels(dpiX, pageRect.Left);
                int top = twipsToPixels(dpiY, pageRect.Top);
                int width = twipsToPixels(dpiX, pageRect.Width);
                int height = twipsToPixels(dpiY, pageRect.Height);
                int right = left + width;
                int bottom = top + height;

                int pageWidth = page.ImageDocument.BlackWhiteImage.Width;
                int pageHeight = page.ImageDocument.BlackWhiteImage.Height;


                if (left < 0) left = 0;
                if (top < 0) top = 0;
                if (right >= pageWidth) right = pageWidth - 1;
                if (bottom >= pageHeight) bottom = pageHeight - 1;

                clipRegion.AddRect(left, top, right, bottom);
            }
            else
            {
                // manual cropping
                // Crop the image and select left/top (if leaveFirstPage == true) or right/bottom part
                

                // Image dimensions
                int width = page.Layout.Width;
                int height = page.Layout.Height;

                int left = (int) settings.LeftCroppingOffset;
                int top = (int) settings.TopCroppingOffset;
                int right = width - 1 - (int) settings.RightCroppingOffset;
                int bottom = height - 1 - (int) settings.BottomCroppintOffset;

                if (left < 0 || left >= width || right < 0 || right >= width || left >= right ||
                    top < 0 || top >= height || bottom < 0 || bottom >= height || top >= bottom)
                {
                    throw new Exception("Invalid page split position");
                }

                clipRegion.AddRect(left, top, right, bottom);

                
            }

            FREngine.ImageModification imageModification = engine.CreateImageModification();
            imageModification.AddClipRegion(clipRegion);
            // Remove all blocks in layout before image modification
            page.Layout.Blocks.RemoveAll();
            page.Layout.BlackSeparators.RemoveAll();
            page.Layout.VisualBlocks.RemoveAll();
            page.ImageDocument.Modify(imageModification);
        }

        private RecognitionStatistics computeStatistics(FREngine.IFRDocument frDoc)
        {
            RecognitionStatistics result = new RecognitionStatistics();
            result.RecognizedText = new List<Sample.RecognizedData.RecognizedText>();

            for (int iPage = 0; iPage < frDoc.Pages.Count; iPage++)
            {
                FREngine.IFRPage page = frDoc.Pages[iPage];
                FREngine.ILayout layout = page.Layout;
                int blocksCount = layout.Blocks.Count;
                for (int iBlock = 0; iBlock < blocksCount; iBlock++)
                {
                    FREngine.IBlock block = layout.Blocks[iBlock];

                    result += computeStatisticsForBlock(iPage, block);
                }

                result.RecognizedText.Add(new Sample.RecognizedData.RecognizedText(page));
            }

            return result;
        }

        private RecognitionStatistics computeStatisticsForBlock(int pageIndex, FREngine.IBlock block)
        {
            RecognitionStatistics result = new RecognitionStatistics();
            if (block.Type == FREngine.BlockTypeEnum.BT_Table)
            {
                FREngine.TableBlock tableBlock = block.GetAsTableBlock();
                for (int iCell = 0; iCell < tableBlock.Cells.Count; iCell++)
                {
                    result += computeStatisticsForBlock(pageIndex, tableBlock.Cells[iCell].Block);
                }
            }
            else if (block.Type == FREngine.BlockTypeEnum.BT_Text)
            {
                FREngine.ITextBlock textBlock = block.GetAsTextBlock();

                int paragraphsCount = textBlock.Text.Paragraphs.Count;
                for (int iPar = 0; iPar < paragraphsCount; iPar++)
                {
                    FREngine.IParagraph par = textBlock.Text.Paragraphs[iPar];
                    string text = par.Text;
                    result.TotalCharsCount += text.Length;
                    FREngine.CharParams charParams = engine.CreateCharParams();
                    for (int iChar = 0; iChar < text.Length; iChar++)
                    {
                        par.GetCharParams(iChar, charParams);
                        if (charParams.IsSuspicious)
                        {
                            result.SuspiciousCharsCount++;
                        }
                    }
                }
            }

            return result;
        }

        private void setStep(string description)
        {
            if (StepChangedEvent != null)
            {
                StepChangedEvent(description);
            }
        }

        private int correctSkewFlags = (int)FREngine.CorrectSkewModeEnum.CSM_CorrectSkewByHorizontalText + (int)FREngine.CorrectSkewModeEnum.CSM_CorrectSkewByVerticalText;
        private EngineLoader engineLoader;


        private FREngine.IEngine engine
        {
            get
            {
                if (engineLoader == null)
                {
                    LoadEngine();
                }
                return engineLoader.Engine;
            }
        }

        
    }
}
