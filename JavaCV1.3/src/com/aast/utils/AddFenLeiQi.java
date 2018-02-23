//package com.aast.utils;
//
//import org.bytedeco.javacpp.opencv_core.Rect;
//
//public class AddFenLeiQi {
//	String haar="resources/lbpcascades/haarcascade_frontalface_alt.xml";//haar分类器
//	String lbp="resources/lbpcascades/lbpcascade_frontalface.xml";//lbp分类器
//	public static void checkboxSelection(String... classifierPath)
//	{
//	        // load the classifier(s)
//	        for (String xmlClassifier : classifierPath)
//	        {
//	                this.faceCascade.load(xmlClassifier);
//	        }
//
//	        // now the capture can start
//	        this.cameraButton.setDisable(false);
//	}
//	
//	public static void startCheck(){
//		//一旦我们加载了分类器，我们就可以开始检测; 我们将在该detectAndDisplay方法中实现检测。首先，我们需要以灰度转换帧，并使直方图均衡以改善结果：
//		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
//		Imgproc.equalizeHist(grayFrame, grayFrame);
//		//然后我们必须设置要检测的脸部的最小尺寸（这在实际检测功能中是必需的）。我们设置最小尺寸为框架高度的20％：
//		if (this.absoluteFaceSize == 0)
//		{
//		    int height = grayFrame.rows();
//		    if (Math.round(height * 0.2f) > 0)
//		    {
//		            this.absoluteFaceSize = Math.round(height * 0.2f);
//		    }
//		}
//		//现在我们可以开始检测：
//		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
//		/**该detectMultiScale功能可以检测输入图像中不同大小的物体。检测到的对象作为矩形列表返回。参数为：
//			包含检测到对象的图像的CV_8U类型的图像矩阵。
//			objects每个矩形包含检测到的对象的矩形矢量。
//			scaleFactor参数指定在每个图像尺度下图像大小减少了多少。
//			minNeighbors参数，指定每个候选矩形应该保留多少邻居。
//			flags与函数cvHaarDetectObjects中的旧级联具有相同含义的参数。它不用于新的级联。
//			minSize最小可能的对象大小。小于该值的对象将被忽略。
//			maxSize最大可能的对象大小。大于此值的对象将被忽略。**/
//		//因此，检测的结果将在对象参数中或在我们的情况下faces。并代表左上角和右下角，它们代表两个相对的顶点。最后一个参数只是设置矩形边框的厚度。Scalar(0, 255, 0, 255). .tl() and .br()
//		//可以通过调用detectAndDisplay每个帧的方法来实现跟踪部分。
//		
//		Rect[] facesArray = faces.toArray();
//		for (int i = 0; i < facesArray.length; i++)
//		    Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
//	}
//}
