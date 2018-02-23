package com.aast.TestJavaCV;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class TestFFmpg {
	static OpenCVFrameConverter.ToIplImage converter1 = new OpenCVFrameConverter.ToIplImage();//转换器  

	public static void openUSBCamera(String outputFile,Camera canvas){
		Java2DFrameConverter converter = new Java2DFrameConverter();//转换器  
		Loader.load(opencv_objdetect.class);  
		try {
					
			FrameGrabber grabber = FrameGrabber.createDefault(0);//本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码  
			grabber.start();//开启抓取器  
			BufferedImage grabbedImage=converter.convert(grabber.grab());
			int width = grabbedImage.getWidth();
			int height =grabbedImage.getHeight();
//			System.out.println(grabber.getAudioCodec());
			FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);  
			recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码  
			recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式  
			recorder.setFrameRate(25);  
			recorder.start();//开启录制器  
			long startTime=0,videoTS=0;  
			Frame rotatedFrame=null; 
			while (canvas.isVisible() && (rotatedFrame=grabber.grab()) != null) { 
				grabbedImage=converter.getBufferedImage(rotatedFrame);
				canvas.setImg(grabbedImage);
				Thread.sleep(40);  
				if (startTime == 0) {  
					startTime = System.currentTimeMillis();  
				}  
				videoTS = 1000 * (System.currentTimeMillis() - startTime);  
				recorder.setTimestamp(videoTS);  
				recorder.record(rotatedFrame);  
			}  
			recorder.stop();  
			recorder.release();  
			grabber.stop();
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}  
	}

	/***********************************************************************************************************/
	/** 
	 * 按帧录制视频 
	 * @param inputFile -该地址可以是网络直播/录播地址，也可以是远程/本地文件路径 
	 * @param outputFile -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式 
	 * @throws FrameGrabber.Exception 
	 * @throws FrameRecorder.Exception 
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception 
	 */  
	public static void frameRecord(String inputFile, String outputFile,Camera canvas, int audioChannel){  
		Java2DFrameConverter converter = new Java2DFrameConverter();//转换器  
		OpenCVFrameConverter.ToIplImage converter1 = new OpenCVFrameConverter.ToIplImage();  
		boolean isStart=true;//该变量建议设置为全局控制变量，用于控制录制结束  
		// 获取视频源  
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）  
//		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1920,1080, audioChannel); 
		//System.out.println(grabber.);
//		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码  
//		
//		recorder.setFormat("avi");//封装格式，如果是推送到rtmp就必须是flv封装格式  
//		recorder.setFrameRate(25);  
		// 开始取视频源  
		try {
			grabber.start();  
//			recorder.start();  
			Frame frame = null;  
			while (isStart&& (frame = grabber.grabFrame()) != null) {  
				BufferedImage grabbedImage=converter.convert(frame);
				Mat m=converter1.convertToMat(frame);
				canvas.setImg(grabbedImage);
//				recorder.record(frame);  
			}  
//			recorder.stop();  
			grabber.stop(); 
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}  
}
