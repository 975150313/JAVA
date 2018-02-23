package com.aast.TestJavaCV;

import org.bytedeco.javacpp.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.avdevice.*;
import static org.bytedeco.javacpp.avformat.AVFormatContext.*;

public class JavaCppTest {

	public static void main(String[] args) throws Exception {
		String out_filename = "rtmp://localhost:1935/live/test3";
		File file = new File("F:/test/15.avi");
//		File file = new File("E:/test.h264");
		final RandomAccessFile raf = new RandomAccessFile(file, "r");
//		Loader.load(avcodec.class);
//		Loader.load(avformat.class);
//		Loader.load(avutil.class);
//		Loader.load(avdevice.class);
//		Loader.load(swscale.class);
//		Loader.load(swresample.class);
		av_register_all();
		avformat_network_init();
		Read_packet_Pointer_BytePointer_int reader = new Read_packet_Pointer_BytePointer_int() {
			@Override
			public int call(Pointer pointer, BytePointer buf, int bufSize) {
				try {
					byte[] data = new byte[bufSize];
					int read = raf.read(data);
					if (read <= 0) {
						System.out.println("EOF found.");
						return AVERROR_EOF;
					}
					System.out.println("Successfully read " + read
							+ " bytes of data.");
					buf.position(0);
					buf.put(data, 0, read);
					return read;
				} catch (Exception ex) {
					ex.printStackTrace();
					return -1;
				}
			}
		};
		long start_time;
		AVPacket pkt = null;
		int inputBufferSize = 32768;
		AVOutputFormat ofmt = null;
		AVFormatContext intContext = null;
		AVFormatContext outContext = null;
		int frame_index = 0;
		intContext = avformat_alloc_context();
		BytePointer inputBuffer = new BytePointer(av_malloc(inputBufferSize));
		AVIOContext ioContext = avio_alloc_context(inputBuffer,inputBufferSize, 0, null, reader, null, null);
		
		intContext.flags(intContext.flags() | AVFMT_FLAG_CUSTOM_IO);
		intContext.pb(ioContext);
		int result;
		// 在avformat_open_input初始化avio，并赋给ifmt_ctx->pb，ffmpeg自动转换为内存读取
		if ((result = avformat_open_input(intContext, "0", null, null)) < 0) {
			System.out.println("open file fail");
			return;
		}
		result = avformat_find_stream_info(intContext,
				(PointerPointer<Pointer>) null);
		if (result != 0) {
			System.out.println("find stream fail");
			return;
		}

		int i = 0;
		int videoStream = -1;
		for (i = 0; i < intContext.nb_streams(); i++) {
			if (intContext.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_VIDEO) {
				videoStream = i;
				break;
			}
		}
		if (videoStream == -1) {
			System.out.println("find stream video fail");
			return;
		}
		System.out.println("find stream video succ.");
		av_dump_format(intContext, 0, inputBuffer, 0);
		// 输出（Output
		//out_filename=rtmp://localhost:1935/live/test3
		avformat_alloc_output_context2(outContext, null, "flv", out_filename);
		if (outContext.isNull()) {
			System.out.println("Could not create output context\n");
			result = AVERROR_UNKNOWN;
			return;
		}
		ofmt = intContext.oformat();
		for (i = 0; i < intContext.nb_streams(); i++) {
			// 根据输入流创建输出流（Create output AVStream according to input AVStream）
			AVStream in_stream = intContext.streams(i);
			AVStream out_stream = avformat_new_stream(intContext, in_stream
					.codec().codec());
			if (out_stream.isNull()) {
				System.out.println("Failed allocating output stream\n");
				result = AVERROR_UNKNOWN;
				return;
			}
			// 复制AVCodecContext的设置（Copy the settings of AVCodecContext）
			result = avcodec_copy_context(out_stream.codec(), in_stream.codec());
			if (result < 0) {
				System.out
						.println("Failed to copy context from input to output stream codec context\n");
				return;
			}
			out_stream.codec().codec_tag(0);
			if ((intContext.oformat().flags() & AVFMT_GLOBALHEADER) > 0)
				out_stream.codec().flags(
						out_stream.codec().flags() | CODEC_FLAG_GLOBAL_HEADER);
		}
		// Dump Format------------------
		av_dump_format(intContext, 0, out_filename, 1);
		// 打开输出URL（Open output URL）
		if (!((ofmt.flags() & AVFMT_NOFILE) > 0)) {
			result = avio_open(intContext.pb(), out_filename,
					AVIO_FLAG_WRITE);
			if (result < 0) {
				System.out.printf("Could not open output URL '%s'",
						out_filename);
				return;
			}
		}
		// 写文件头（Write file header）
		result = avformat_write_header(intContext, (AVDictionary) null);
		if (result < 0) {
			System.out.println("Error occurred when opening output URL\n");
			return;
		}
		start_time = System.currentTimeMillis();
		while (true) {
			AVStream in_stream, out_stream;
			// 获取一个AVPacket（Get an AVPacket）
			result = av_read_frame(intContext, pkt);
			if (result < 0)
				break;
			// FIX：No PTS (Example: Raw H.264)
			// Simple Write PTS
			if (pkt.pts() == AV_NOPTS_VALUE) {
				// Write PTS
				AVRational time_base1 = intContext.streams(videoStream)
						.time_base();
				// Duration between 2 frames (us)
				double calc_duration = (double) AV_TIME_BASE
						/ av_q2d(intContext.streams(videoStream)
								.r_frame_rate());
				// Parameters
				pkt.pts((long) ((double) (frame_index * calc_duration) / (double) (av_q2d(time_base1) * AV_TIME_BASE)));
				pkt.dts(pkt.pts());
				pkt.duration((long) ((double) calc_duration / (double) (av_q2d(time_base1) * AV_TIME_BASE)));
			}
			// Important:Delay
			if (pkt.stream_index() == videoStream) {
				AVRational time_base = intContext.streams(videoStream)
						.time_base();
				AVRational time_base_q = av_make_q(1, AV_TIME_BASE);
				long pts_time = av_rescale_q(pkt.dts(), time_base, time_base_q);
				long now_time = System.currentTimeMillis() - start_time;
				if (pts_time > now_time)
					Thread.currentThread().sleep(pts_time - now_time);
			}

			in_stream = intContext.streams(pkt.stream_index());
			out_stream = outContext.streams(pkt.stream_index());
			/* copy packet */
			// 转换PTS/DTS（Convert PTS/DTS）
			pkt.pts(av_rescale_q_rnd(pkt.pts(), in_stream.time_base(),
					out_stream.time_base(),
					(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
			pkt.dts(av_rescale_q_rnd(pkt.dts(), in_stream.time_base(),
					out_stream.time_base(),
					(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
			pkt.duration(av_rescale_q(pkt.duration(), in_stream.time_base(),
					out_stream.time_base()));
			pkt.pos(-1);
			// Print to Screen
			if (pkt.stream_index() == videoStream) {
				System.out.printf("Send %8d video frames to output URL\n",
						frame_index);
				frame_index++;
			}
			// ret = av_write_frame(ofmt_ctx, &pkt);
			result = av_interleaved_write_frame(outContext, pkt);

			if (result < 0) {
				System.out.printf("Error muxing packet\n");
				break;
			}
			av_free_packet(pkt);

		}
		// 写文件尾（Write file trailer）
		av_write_trailer(outContext);
		avformat_close_input(intContext);
		/* close output */
		if (outContext.isNull() && (ofmt.flags() & AVFMT_NOFILE) > 0)
			avio_close(outContext.pb());
		avformat_free_context(outContext);
		if (result < 0 && result != AVERROR_EOF) {
			System.out.printf("Error occurred.\n");
			return;
		}
		return;
	}

}
