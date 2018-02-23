package com.aast.TestJavaCV;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avformat.AVInputFormat;
import org.bytedeco.javacpp.avutil.AVDictionary;
import org.bytedeco.javacpp.freenect.freenect_frame_mode;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class TestFFmpg2 {
	
	public static void openUSBCamera(String outputFile,Camera canvas){
		try {
			Loader.load(opencv_objdetect.class);  
			FrameGrabber grabber = FrameGrabber.createDefault(0);//本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码  
			Java2DFrameConverter converter = new Java2DFrameConverter();//转换器  
			grabber.start();//开启抓取器  
			//OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器  
			BufferedImage grabbedImage=converter.convert(grabber.grab());
//			IplImage grabbedImage = converter.convert(grabber.grab());//抓取一帧视频并将其转换为图像  
//			int width = grabbedImage.width();  
//			int height = grabbedImage.height();  
			int width = grabbedImage.getWidth();
			int height =grabbedImage.getHeight();
			
			

			FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);  
			recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码  
			recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式  
			recorder.setFrameRate(25);  
			recorder.start();//开启录制器  
			long startTime=0,videoTS=0;  
//			Frame rotatedFrame=converter.convert(grabbedImage);//不知道为什么这里不做转换就不能推到rtmp  
			Frame rotatedFrame=null; 
			while (canvas.isVisible() && (rotatedFrame=grabber.grab()) != null) { 
				//grabbedImage = converter.convert(rotatedFrame);
				grabbedImage=converter.getBufferedImage(rotatedFrame);
//				rotatedFrame = converter.convert(grabbedImage);  
				canvas.setImg(grabbedImage);
				if (startTime == 0) {  
					startTime = System.currentTimeMillis();  
				}  
				videoTS = 1000 * (System.currentTimeMillis() - startTime);  
				recorder.setTimestamp(videoTS);  
				recorder.record(rotatedFrame);  
				Thread.sleep(40);  
			}  
			recorder.stop();  
			recorder.release();  
			grabber.stop();
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	/** 
	 * 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制） 
	 *  
	 * @author eguid 
	 * @param outputFile -录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址 
	 * @param frameRate - 视频帧率 
	 * @throws Exception 
	 * @throws InterruptedException 
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception 
	 */  
	public static void recordCamera(String outputFile, double frameRate)throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {  
		Loader.load(opencv_objdetect.class);  
		FrameGrabber grabber = FrameGrabber.createDefault(0);//本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码  
		//FrameGrabber grabber = FrameGrabber.createDefault("rtsp://admin:admin123@192.168.1.64:554/h264/ch1/main/av_stream");//本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码  
		grabber.start();//开启抓取器  

		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器  
		IplImage grabbedImage = converter.convert(grabber.grab());//抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加  
		int width = grabbedImage.width();  
		int height = grabbedImage.height();  
		/********************************************/
//		avcodec.av_xiphlacing(arg0, arg1)
		// Register all formats and codes
//		avformat.av_register_all();
//		avcodec.avcodec_register_all();
//		
//		// Open video file
//		avformat.AVFormatContext formatCtx = avformat.avformat_alloc_context();
//		avformat.avformat_find_stream_info(formatCtx, (AVDictionary) null);
		/********************************************/
		FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);  
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码  
		recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式  
		recorder.setFrameRate(frameRate);  
		
		recorder.start();//开启录制器  
		long startTime=0;  
		long videoTS=0;  
		CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.setAlwaysOnTop(true);  
		Frame rotatedFrame=converter.convert(grabbedImage);//不知道为什么这里不做转换就不能推到rtmp  
		while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {  
			rotatedFrame = converter.convert(grabbedImage);  
			frame.showImage(rotatedFrame);
			if (startTime == 0) {  
				startTime = System.currentTimeMillis();  
			}  
			videoTS = 1000 * (System.currentTimeMillis() - startTime);  
			recorder.setTimestamp(videoTS);  
			recorder.record(rotatedFrame);  
			Thread.sleep(40);  
		}  
		frame.dispose();  
		recorder.stop();  
		recorder.release();  
		grabber.stop();  

	}  
//	public static void main(String[] args) throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception { 
//		//推流
//		//recordCamera("C:/a.mp4",25);  
//		//recordCamera("rtmp://192.168.30.21/live/record1",25);  
//		
//		
//		//拉流
//		String inputFile = "rtsp://admin:admin123@192.168.1.64:554/h264/ch1/main/av_stream";  
//        // Decodes-encodes  
//        String outputFile = "recorde.mp4";  
//        frameRecord(inputFile, outputFile,1);  
//	} 
	
	
	
	
	
	/***********************************************************************************************************/
	/** 
     * 按帧录制视频 
     *  
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径 
     * @param outputFile 
     *            -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式 
     * @throws FrameGrabber.Exception 
     * @throws FrameRecorder.Exception 
     * @throws org.bytedeco.javacv.FrameRecorder.Exception 
     */  
    public static void frameRecord(String inputFile, String outputFile, int audioChannel)  
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {  
          
            boolean isStart=true;//该变量建议设置为全局控制变量，用于控制录制结束  
        // 获取视频源  
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);  
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）  
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720, audioChannel); 
        // 开始取视频源  
        recordByFrame(grabber, recorder,isStart);  
    } 
    
    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {  
    	
    	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器  
    	CanvasFrame canvas = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());  
    	canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
    	canvas.setAlwaysOnTop(true);  
        try {//建议在线程中使用该方法  
            grabber.start();  
            recorder.start();  
            Frame frame = null;  
            while (status&& (frame = grabber.grabFrame()) != null) {  
            	//
            	//converter.
            	canvas.showImage(frame);
            	//
                recorder.record(frame);  
            }  
            recorder.stop();  
            grabber.stop();  
        } finally {  
            if (grabber != null) {  
                grabber.stop();  
            }  
        }  
    }  
}
