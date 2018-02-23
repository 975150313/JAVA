package com.aast.test;
import java.io.*;
import java.nio.ByteBuffer;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.Cast;

//ffmpeg
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;
import static org.bytedeco.javacpp.avformat.AVFormatContext.*;

//opencv
import static  org.bytedeco.javacpp.opencv_core.* ;
import static  org.bytedeco.javacpp.opencv_imgcodecs.* ;
import static  org.bytedeco.javacpp.opencv_stitching.* ;
import static  org.bytedeco.javacpp.opencv_highgui.* ;

/**
 * 这个是好用的，千万不要修改 TOT
 * @author QQ
 *
 */
public class FFmpegRead {
	static InputStream in=null;
	final int BUF_SIZE = 1400;

	Read_packet_Pointer_BytePointer_int read_buffer=new Read_packet_Pointer_BytePointer_int(){

		@Override
		public int call(Pointer opaque, BytePointer buf, int buf_size) {
			byte[] bytebuf=new byte[buf_size];
			int size=-1;
			try {
				size = in.read(bytebuf, 0, buf_size);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// arg1=new BytePointer(ByteBuffer.wrap(buf));
			buf.position(0);
			buf.put(bytebuf, 0, size);
			return size;
		}
	};
	public FFmpegRead() throws FileNotFoundException {
		AVFrame  pFrame = null;
		AVFrame  pFrameRGB = null;
		AVIOContext  pIoCtx=null;
		AVInputFormat  pInputFmt = new AVInputFormat();
		AVFormatContext  pFormatCtx = null;
		AVCodecContext  pCodecCtx = null;
		AVCodec  pCodec = null;
		AVPacket  packet =null;
		SwsContext   pSwxCtx  =null;
		in = new FileInputStream("E:/test.h264");
		
		av_register_all(); //注册所有FFmpeg库所支持的文件格式和codec
		avformat_network_init();
		int result = 0;
		// Pointer inputBuffer = av_malloc(BUF_SIZE);
		// BytePointer inputBuffer=(BytePointer) av_malloc(BUF_SIZE);
		byte[] buf=new byte[BUF_SIZE];
//		BytePointer inputBuffer=new BytePointer(ByteBuffer.wrap(buf));
		BytePointer inputBuffer = new BytePointer(av_malloc(BUF_SIZE));
		pIoCtx = avio_alloc_context(inputBuffer, BUF_SIZE, 0, null, read_buffer, null,null);

		int ret = av_probe_input_buffer2(pIoCtx, pInputFmt, (BytePointer)null, (Pointer)null, 0, 0);
//		int ret = av_probe_input_buffer2(pIoCtx, pInputFmt, "0", null, 0, 0);
		if (ret < 0) {
			System.out.println("探测失败");
			return ;
		}
		pFormatCtx = avformat_alloc_context();
		pFormatCtx.pb(pIoCtx);
		// step1: 打开媒体文件,最后2个参数是用来指定文件格式，buffer大小和格式参数，设置成null的话，libavformat库会自动去探测它们
		result = avformat_open_input(pFormatCtx, "0", null, null);
		//result = avformat_open_input(pFormatCtx, "0",pInputFmt, nullptr); //avformat_close_input
		if (result != 0)
		{
			System.out.println("open file fail");
			return  ;
		}
		// step2:查找信息流的信息
		result = avformat_find_stream_info(pFormatCtx, (PointerPointer<Pointer>)null);
		if (result != 0)
		{
			System.out.println("find stream fail");
			return  ;
		}
		// step3: 打印信息
		 av_dump_format(pFormatCtx, 0, pFormatCtx.filename(), 0);
		// step4：找到video流数据
		int i = 0;
		int videoStream = -1;

		for (i = 0; i < pFormatCtx.nb_streams(); i++){
			if (pFormatCtx.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_VIDEO){
				videoStream = i;
				break;
			}
		}

		if (videoStream == -1){
			System.out.println("find stream video fail");
			return ;
		}
		// 得到video编码格式
		// pCodecCtx = pFormatCtx.streams[videoStream].codec;
		/* 新版推荐替换方法 */
		pCodecCtx = avcodec_alloc_context3(null);
		result = avcodec_parameters_to_context(pCodecCtx, pFormatCtx.streams(videoStream).codecpar());
		if (result < 0)
			return ;
		av_codec_set_pkt_timebase(pCodecCtx, pFormatCtx.streams(videoStream).time_base());
		// step5: 得到解码器
		pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
		if (pCodec == null){
			System.out.println("find decoder fail" );
			return ;
		}
		result = avcodec_open2(pCodecCtx, pCodec, (PointerPointer<Pointer>)null);
		if (result != 0){
			System.out.println("open codec fail");
			return ;
		}
		// step6: 申请原始数据帧 和 RGB帧内存
		pFrame = av_frame_alloc();
		pFrameRGB = av_frame_alloc();
		if (pFrame == null || pFrameRGB == null)
		{
			return ;
		}
//		 int numBytes = avpicture_get_size(AV_PIX_FMT_RGB24, pCodecCtx.width, pCodecCtx.height);
		int numBytes = av_image_get_buffer_size(AV_PIX_FMT_BGR24, pCodecCtx.width(), pCodecCtx.height(), 1);
		// 新版推荐替换函数
		BytePointer rgbData=new BytePointer(av_malloc(numBytes));
		av_image_fill_arrays(pFrameRGB.data(), pFrameRGB.linesize(), rgbData, AV_PIX_FMT_BGR24, pCodecCtx.width(), pCodecCtx.height(), 1);
		// 新版推荐替换函数
		packet = av_packet_alloc();
		// step7: 创建格式转化文本
		pSwxCtx = sws_getContext(pCodecCtx.width(), pCodecCtx.height(), pCodecCtx.pix_fmt(),pCodecCtx.width(), pCodecCtx.height(), AV_PIX_FMT_RGB24,SWS_BILINEAR, null, null, (DoublePointer)null);

		Mat image=new Mat(pCodecCtx.height(), pCodecCtx.width(), CV_8UC3);
		while ( av_read_frame(pFormatCtx, packet) >= 0){// 得到数据包
			if (packet.stream_index() == videoStream){
				// 解码 新版推荐替换方法 
				result = avcodec_send_packet(pCodecCtx, packet);
				if (result < 0) {
					System.out.println("Decode Error");
					return ;
				}
				result = avcodec_receive_frame(pCodecCtx, pFrame);
				if (result < 0 && result != -11){
					System.out.println("Decode Error1");
					 return  ;
				}
				// 转换
				sws_scale(pSwxCtx, pFrame.data(), pFrame.linesize(), 0, pCodecCtx.height(),pFrameRGB.data(), pFrameRGB.linesize());
				
				//显示
				image.data(pFrameRGB.data(0));
				imshow("haha", image);
				if(waitKey(30)==27){
					break;
				}
			}
			av_packet_unref(packet); // 新版推荐替换函数
		}
		avformat_close_input(pFormatCtx);
		av_packet_free(packet);
		// 新版推荐替换函数
	}

	public static void main(String[] args) throws Exception {
		FFmpegRead ffmpegRead=new FFmpegRead();
	}


}