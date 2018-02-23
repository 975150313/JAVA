package com.aast.TestJavaCV;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import org.bytedeco.javacpp.opencv_core.Mat;

public class Camera extends Canvas {
	private static final long serialVersionUID = 1L;
	private BufferedImage img;
	private boolean stop = false;
	@Override 
	public void update(Graphics g) {
		paint(g);
	}
	
	@Override 
	public void paint(Graphics g) {
		try {
			if (getWidth() <= 0 || getHeight() <= 0) {
				return;
			}
			BufferStrategy strategy = getBufferStrategy();
			do {
				do {
					g = strategy.getDrawGraphics();
					Mat a=null;
					if (img != null) {                        
						g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
//						g.drawImage(img1, 0, 0, img1.getWidth(), img1.getHeight(), null);
//						g.drawRect(10, 10, 200, 200);
					}
					g.dispose();
				} while (strategy.contentsRestored());
				strategy.show();
			} while (strategy.contentsLost());
		} catch (NullPointerException e) {
		} catch (IllegalStateException e) { }
	}


	public BufferedImage getImg() {
		return img;
	}
	public void setImg(BufferedImage img) {
		init();
		this.img = img;      
		repaint();                       
	}
	public void stop(){
		stop=true;
	}

	public void init(){
		new Thread(){
			public void run(){
				while(!stop){
					try{
						stop = true;
						createBufferStrategy(2);
					}catch(IllegalStateException e){
						stop = false;
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}.start();
	}
}
