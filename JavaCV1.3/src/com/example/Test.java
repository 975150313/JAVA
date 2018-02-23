package com.example;

import org.bytedeco.javacpp.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.avformat.AVFormatContext.*;

public class Test {

    public static void main(String[] args) throws Exception {
        File file = new File("E:/test.h264");
        final RandomAccessFile raf = new RandomAccessFile(file, "r");

        av_register_all();
        avformat_network_init();

        Read_packet_Pointer_BytePointer_int reader = new Read_packet_Pointer_BytePointer_int() {
            @Override
            public int call(Pointer pointer, BytePointer buf, int bufSize) {
                try {
                    byte[] data = new byte[bufSize]; // this is inefficient, just use as a quick example
                    int read = raf.read(data);

                    if (read <= 0) {
                        // I am still unsure as to return '0', '-1' or 'AVERROR_EOF'.
                        // But according to the following link, it should return 'AVERROR_EOF',
                        // http://www.codeproject.com/Tips/489450/Creating-Custom-FFmpeg-IO-Context
                        // btw 'AVERROR_EOF' is a nasty negative number, '-541478725'.
                        return AVERROR_EOF;
                    }

                    buf.position(0);
                    buf.put(data, 0, read);
                    return read;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return -1;
                }
            }
        };

        Seek_Pointer_long_int seeker = new Seek_Pointer_long_int() {
//        	raf.seek(offset);
            @Override
            public long call(Pointer pointer, long offset, int whence) {

                try {
                    if (whence == AVSEEK_SIZE) {
                        // Returns the entire file length. If not supported, simply returns a negative number.
                        // https://www.ffmpeg.org/doxygen/trunk/avio_8h.html#a427ff2a881637b47ee7d7f9e368be63f
                        return raf.length();
                    }

                    raf.seek(offset);
                    return offset;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return -1;
                }
            }
        };

        int inputBufferSize = 32768;
        BytePointer inputBuffer = new BytePointer(av_malloc(inputBufferSize));

        AVIOContext ioContext = avio_alloc_context(inputBuffer,
                                                   inputBufferSize,
                                                   0, // CRITICAL, if the context is for reading, it should be ZERO
                                                      //           if the context is for writing, then it is ONE
                                                   null,
                                                   reader,
                                                   null,
                                                   seeker);

        AVInputFormat format = av_find_input_format("h264");
        AVFormatContext formatContext = avformat_alloc_context();
        formatContext.iformat(format);
        formatContext.flags(formatContext.flags() | AVFMT_FLAG_CUSTOM_IO);
        formatContext.pb(ioContext);

        // Now this is working properly.
        int result = avformat_open_input(formatContext, "", format, null);
        System.out.println("result == " + result);

        // all clean-up code omitted for simplicity
    }

}