package com.aast.test;
import java.io.*;
import java.nio.ByteBuffer;


//ffmpeg
import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;
//opencv
import static  org.bytedeco.javacpp.opencv_core.* ;
import static  org.bytedeco.javacpp.opencv_imgcodecs.* ;
import static  org.bytedeco.javacpp.opencv_stitching.* ;
import static  org.bytedeco.javacpp.opencv_highgui.* ;
public class FFmpegMemory {

	public static void main(String[] args) throws Exception {
		AVCodec pCodec=null;
		AVCodecContext  pCodecCtx = null;
		AVFrame  _frame = null;
		AVFrame  frameRGB = null;
		SwsContext  _sws=null;
		av_register_all();//初始化
		avcodec_register_all();
		AVFormatContext formatCtx =null;
		
		formatCtx=avformat_alloc_context();
		pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);//搜索解码器
		pCodecCtx = avcodec_alloc_context3(pCodec);//请求上下文对像
		
		if (avcodec_open2(pCodecCtx, pCodec,  (AVDictionary) null) < 0){//打开解码器
			System.out.println("打开解码器 失败");
			return;
		}
		_frame = av_frame_alloc();//申请解码侦对像
		frameRGB = av_frame_alloc();
		byte[] rgb = null;//1920 * 1080//384 * 290
		//		byte[] h264= new byte[384 * 290];
		byte[] h264= new byte[8000];
		int len = 0;
		InputStream in = new FileInputStream("E:/ds.h264");
		//		InputStream in = new FileInputStream("E:/a.mp4");
		//		System.out.println(in.available());
		int byteread=0;
		BytePointer rgbData=null;
		AVPacket packet = av_packet_alloc();
//		av_init_packet(packet);
		while (true){
			if((byteread = in.read(h264))==-1){
				break;
			}
			BytePointer data=new BytePointer(ByteBuffer.wrap(h264));
			packet.data(data);
			packet.size(byteread);
			//				        int result = avcodec_decode_video2(pCodecCtx, _frame,frameFinished, packet);
			int result= avcodec_send_packet(pCodecCtx, packet);
			if (result < 0) {
				System.out.println("Decode Error1.");
				return ;
			}
			result = avcodec_receive_frame(pCodecCtx, _frame);
			if (result < 0&&result!=-11){
				System.out.println("Decode Error2.");
				return ;
			}
			System.out.println(_frame.width()+","+_frame.height());
			//
			if(result==0){
				if (rgb == null){//分配rgb内存
					rgb = new byte[_frame.width()*_frame.height() * 3];
					rgbData=new BytePointer(ByteBuffer.wrap(rgb));
				}
				if (_sws == null){
					_sws = sws_getContext(pCodecCtx.width(), pCodecCtx.height(),pCodecCtx.pix_fmt(), pCodecCtx.width(), pCodecCtx.height(),AV_PIX_FMT_BGR24, SWS_BILINEAR, null, null, (DoublePointer)null);
				}
				av_image_fill_arrays(frameRGB.data(), frameRGB.linesize(), rgbData, AV_PIX_FMT_BGR24, pCodecCtx.width(), pCodecCtx.height(), 1);	// 新版推荐替换函数
				sws_scale(_sws, _frame.data(), _frame.linesize(), 0, pCodecCtx.height(), frameRGB.data(), frameRGB.linesize());
				System.out.println("解码一帧");
				Mat image=new Mat(pCodecCtx.height(), pCodecCtx.width(), CV_8UC3);
				image.data(new BytePointer(ByteBuffer.wrap(rgb)));
				imshow("haha", image);
				if(waitKey(40)==27){
					break;
				}
				av_packet_unref(packet);	// 新版推荐替换函数  擦包
			}
		}
		//下面为释放代码
		sws_freeContext(_sws);
		//		av_free(buffer);
		//		av_free(pFrameRGB);
		//		av_free(pFrame);
		// Close the codec
		av_packet_free(packet); //shi'fang
		avcodec_close(pCodecCtx);
		// Close the video file
		avformat_close_input(formatCtx);
	}
	//-Xms1000M -Xmx1100M
}
