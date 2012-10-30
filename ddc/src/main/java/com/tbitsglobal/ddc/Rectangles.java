//import java.awt.Point;
//import java.awt.image.BufferedImage;
//import java.util.List;
//
//import com.googlecode.javacv.cpp.opencv_core.CvPoint;
//import com.googlecode.javacpp.Pointer;
//import com.googlecode.javacv.*;
//import static com.googlecode.javacv.cpp.opencv_core.*;
//import static com.googlecode.javacv.cpp.opencv_imgproc.*;
//import static com.googlecode.javacv.cpp.opencv_highgui.*;
//
//
//
//
//public class Rectangles {
//
//static int thresh = 50, N = 11;
//static String wndname = "Square Detection Demo";
//
//// helper function:
//// finds a cosine of angle between vectors
//// from pt0->pt1 and from pt0->pt2
//public static double angle(  Point pt1,  Point pt2,  Point pt0 )
//{
//    double dx1 = pt1.x() - pt0.x();
//    double dy1 = pt1.y() - pt0.y();
//    double dx2 = pt2.x() - pt0.x();
//    double dy2 = pt2.y() - pt0.y();
//    return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
//}
//
//// returns sequence of squares detected on the image.
//// the sequence is stored in the specified memory storage
//public static void findSquares( CvMat image, List<List<Point> > squares )
//{
//    squares.clear();
//    CvMat gray0 =CvMat.create(image.rows(), image.cols(), CV_8U);
//    //= cvLoadImage(image.cvSize(), CV_8U);
//    
//    CvArr a;
//    
//    CvMat pyr, timg, gray;
//
//    // down-scale and upscale the image to filter out the noise
//    cvPyrDown((CvArr)image, (CvArr)pyr, image.size()/4);
//    cvPyrUp((CvArr)pyr, (CvArr)timg, image.size());
//    List<List<Point> > contours;
//
//    // find squares in every color plane of the image
//    for( int c = 0; c < 3; c++ )
//    {
//        int ch[] = {c, 0};
//        cvMixChannels(new CvArr[]{timg}, 1, new CvArr[]{gray0}, 1, ch, 1);
//
//        // try several threshold levels
//        for( int l = 0; l < N; l++ )
//        {
//            // hack: use Canny instead of zero threshold level.
//            // Canny helps to catch squares with gradient shading
//            if( l == 0 )
//            {
//                // apply Canny. Take the upper threshold from slider
//                // and set the lower to 0 (which forces edges merging)
//                cvCanny(gray0, gray, 0, thresh, 5);
//                // dilate canny output to remove potential
//                // holes between edge segments
//                IplConvKernel kernel = new IplConvKernel();
//                
//                
//                cvDilate((CvArr)gray, (CvArr)gray, kernel, 1);
//            }
//            else
//            {
//                // apply threshold if l!=0:
//                //     tgray(x,y) = gray(x,y) < (l+1)*255/N ? 255 : 0
//                gray = gray0 >= (l+1)*255/N;
//                
//            }
//
//            // find contours and store them all as a list
//            cvFindContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
//
//            vector<Point> approx;
//
//            // test each contour
//            for( size_t i = 0; i < contours.size(); i++ )
//            {
//                // approximate contour with accuracy proportional
//                // to the contour perimeter
//                approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);
//
//                // square contours should have 4 vertices after approximation
//                // relatively large area (to filter out noisy contours)
//                // and be convex.
//                // Note: absolute value of an area is used because
//                // area may be positive or negative - in accordance with the
//                // contour orientation
//                if( approx.size() == 4 &&
//                    fabs(contourArea(Mat(approx))) > 1000 &&
//                    isContourConvex(Mat(approx)) )
//                {
//                    double maxCosine = 0;
//
//                    for( int j = 2; j < 5; j++ )
//                    {
//                        // find the maximum cosine of the angle between joint edges
//                        double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
//                        maxCosine = MAX(maxCosine, cosine);
//                    }
//
//                    // if cosines of all angles are small
//                    // (all angles are ~90 degree) then write quandrange
//                    // vertices to resultant sequence
//                    if( maxCosine < 0.3 )
//                        squares.push_back(approx);
//                }
//            }
//        }
//    }
//}
//
//
//// the function draws all the squares in the image
//static void drawSquares( Mat& image, const vector<vector<Point> >& squares )
//{
//    for( size_t i = 0; i < squares.size(); i++ )
//    {
//        const Point* p = &squares[i][0];
//        int n = (int)squares[i].size();
//        polylines(image, &p, &n, 1, true, Scalar(0,255,0), 3, CV_AA);
//    }
//
//    imshow(wndname, image);
//}
//
//
//int main(int /*argc*/, char** /*argv*/)
//{
//    static const char* names[] = { "pic1.png", "pic2.png", "pic3.png",
//        "pic4.png", "pic5.png", "pic6.png", 0 };
//    help();
//    namedWindow( wndname, 1 );
//    vector<vector<Point> > squares;
//
//    for( int i = 0; names[i] != 0; i++ )
//    {
//        Mat image = imread(names[i], 1);
//        if( image.empty() )
//        {
//            cout << "Couldn't load " << names[i] << endl;
//            continue;
//        }
//
//        findSquares(image, squares);
//        drawSquares(image, squares);
//
//        int c = waitKey();
//        if( (char)c == 27 )
//            break;
//    }
//
//    return 0;
//}
//}