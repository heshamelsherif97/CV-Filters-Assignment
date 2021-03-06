import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Noise {
	
	//Calculate noisy pixels using variance
	public static double calculateNoise(Mat img) {
		double noise = 0.0;
		Mat meanMatrix = new Mat();
		Imgproc.blur(img, meanMatrix, new Size(3, 3));
		for (int i = 1; i < img.rows() - 1; i++) {
			for (int j = 1; j < img.cols() - 1; j++) {
				double variance = calculateVariance(img, meanMatrix.get(i, j)[0], i, j);
				if(variance > 150) noise++;
			}
		}
		return (noise/(img.cols()*img.rows()))*100.0;
	}

	//Calculate variance in a 3x3 kernel
	public static double calculateVariance(Mat img, double mean, int i, int j) {
		double variance = 0.0;
		for (int k = i -1; k < i+2; k++) {
			for (int k2 = j - 1; k2 < j+2; k2++) {
				variance += Math.pow(img.get(k, k2)[0] - mean, 2);
			}
		}
		variance /= 9;
		return variance;
	}
	
	//Sobel Edge Detection
	public static Mat SobelDetector(Mat img) {
		Mat result = img.clone();
		Mat Gy = new Mat();
		Mat Gx = new Mat();
		Mat GyAbs = new Mat();
		Mat GxAbs = new Mat();
		
		Imgproc.Sobel(img, Gx, CvType.CV_16S, 1, 0, 3);
		Imgproc.Sobel(img, Gy, CvType.CV_16S, 0, 1, 3);
		
        Core.convertScaleAbs(Gx, GxAbs);
        Core.convertScaleAbs(Gy, GyAbs);
		
        Core.addWeighted(GxAbs, 0.5, GyAbs, 0.5, 0, result);
		return result;
	}
	
	//UnSharp Filter
	public static Mat unsharpFilter(Mat img) {
		Mat result = new Mat();
		Mat smoothed = new Mat();
		Imgproc.GaussianBlur(img, smoothed, new Size(0,0), 10);
		Core.addWeighted(img, 1.7, smoothed, -0.7, 0, result);
		return result;
	}
	
	//Calculate Blur
	public static double calculateBlur(Mat img) {
		double blur = 0.0;
		Mat meanMatrix = new Mat();
		Imgproc.blur(img, meanMatrix, new Size(3, 3));
		for (int i = 1; i < img.rows() - 1; i++) {
			for (int j = 1; j < img.cols() - 1; j++) {
				double variance = calculateVariance(img, meanMatrix.get(i, j)[0], i, j);
				if(variance <= 50) blur++;
			}
		}
		return (blur/(img.cols()*img.rows()))*100.0;
	}
	
	//Applying median filter 3x3
	public static Mat medianFilter(Mat img) {
		Mat result = img.clone();
		Imgproc.medianBlur(img, result, 3);
		return result;
	}
	
	//Compute the histogram of an image
	public static int[] histogram(Mat img) {
		int [] histogram = new int[256];
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				double[] rgb = img.get(i, j);
				histogram[(int)rgb[0]]++;
			}
		}
		return histogram;
	}
	
	//Check Color range percentage from 0-255
	public static double checkRange(Mat img) {
		int low = getLowBound(histogram(img));
		int high = getHighBound(histogram(img));
		
		return (high - low)*100.0/255.0;
	}
	
	//Contrast Stretching
	public static Mat contrastStretch(Mat img) {
		int [] histogram = histogram(img);
		int c = getLowBound(histogram);
		int d = getHighBound(histogram);
		Mat result = img.clone();
		double scalingFactor = (255)/(d - c);
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				double[] rgb = img.get(i, j);
				double newValue = (rgb[0]-c)*scalingFactor;
				result.put(i, j, newValue, newValue, newValue);
			}
		}
		return result;
	}
	
	public static int getLowBound(int[]x) {
		for (int i = 0; i < x.length; i++) {
			if(x[i] > 5000)
				return i;
		}
		return 0;
	}
	
	public static int getHighBound(int[]x) {
		for (int i = x.length -1 ; i >= 0; i--) {
			if(x[i] > 5000)
				return i;
		}
		return 0;
	}
	
	public static void enhanceImage(String filePath) {
		CV2 cv = new CV2();
		Mat result = cv.loadImage(filePath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		
		double noise = calculateNoise(result);
		System.out.println("Noisy Pixels: "+noise+"%");
		
		if(noise >= 50.0) {
			result = medianFilter(result);
			System.out.println("Median Filter is applied");
		}
			
		double blurry = calculateBlur(SobelDetector(result));
		System.out.println("Blurry Pixels: "+blurry+"%");
		if(blurry > 75.0) {
			result = unsharpFilter(result);
			System.out.println("Unsharp Filter is applied");
		}
		
		double intensityRange = checkRange(result);
		System.out.println("Color Range from 0-255: "+intensityRange+"%");
		if(intensityRange < 50.0) {
			result = contrastStretch(result);
			System.out.println("Contrast Stretching is applied");
		}
		
		cv.saveImage("output/"+filePath.charAt(6)+"_enhanced.jpg", result);
		System.out.println("_____________________________________");
	}
	
	public static void main(String[] args) {
		enhanceImage("input/1.jpg");
		enhanceImage("input/2.jpg");
		enhanceImage("input/3.jpg");
		enhanceImage("input/4.jpg");
		enhanceImage("input/5.jpg");
		enhanceImage("input/6.jpg");
		enhanceImage("input/7.jpg");
		enhanceImage("input/8.jpg");
	}
}
