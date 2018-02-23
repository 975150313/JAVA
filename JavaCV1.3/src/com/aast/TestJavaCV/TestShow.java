package com.aast.TestJavaCV;

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.Canvas;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TestShow {

	private JFrame frame;
	private Camera usbCamera,hkCamera;
	private JButton btnOpenUSBCamera,btnOpenHKCamera,btnAddPicture;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestShow window = new TestShow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public TestShow() {
		initialize();
		
		myEvent();
	}


	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1034, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		usbCamera = new Camera();
		usbCamera.setBounds(44, 31, 268, 227);
		frame.getContentPane().add(usbCamera);
		
		hkCamera = new Camera();
		hkCamera.setBounds(181, 31, 784, 654);
		frame.getContentPane().add(hkCamera);
		
		btnOpenUSBCamera = new JButton("打开摄像头");
		
		btnOpenUSBCamera.setBounds(56, 703, 118, 23);
		frame.getContentPane().add(btnOpenUSBCamera);
		
		btnOpenHKCamera = new JButton("获取海康码流");
		btnOpenHKCamera.setBounds(184, 703, 176, 23);
		frame.getContentPane().add(btnOpenHKCamera);
		
		btnAddPicture = new JButton("添加水印");
		btnAddPicture.setBounds(436, 703, 170, 23);
		frame.getContentPane().add(btnAddPicture);
		
//		usbCamera.init();
//		hkCamera.init();
	}
	
	public void myEvent(){
		btnOpenUSBCamera.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(){

					@Override
					public void run() {
						TestFFmpg.openUSBCamera("C:/temp/a.avf", usbCamera);
					}
					
				}.start();
			}
		});
		btnOpenHKCamera.addActionListener(new ActionListener() {
			//海康
			public void actionPerformed(ActionEvent e) {
				new Thread(){
					
					@Override
					public void run() {
//						String inputFile = "http://123.124.159.5:6713/mag/hls/dfec5dacd4044a62986d95078eed7ac1/1/live.m3u8";  
//						String inputFile = "c:/v.h264";  
//						String outputFile = null;  
//						String inputFile = "c:/55dcdedc6d564808b17e9e72100fc603.mp4";  
						String inputFile = "rtsp://admin:admin123@192.168.2.101:554/h264/ch1/main/av_stream";  
//						String inputFile = "rtsp://123.124.159.5:554/pag://192.168.1.5:7302:cfbc4efb23284f0c895f2b80f1784887:0:MAIN:TCP?cnid=4&pnid=5";  
						 String outputFile = "http://123.124.159.5:6713/mag/hls/dfec5dacd4044a62986d95078eed7ac1/1/live.m3u8";  
						TestFFmpg.frameRecord(inputFile,outputFile,hkCamera,0);
					}
					
				}.start();
			}
		});
	}
}
