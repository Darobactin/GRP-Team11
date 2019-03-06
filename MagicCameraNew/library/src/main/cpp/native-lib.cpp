#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

extern "C" JNIEXPORT jstring JNICALL
Java_duxiaoman_guofeng_myapplicationbeauty_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    cv::Mat mat;
    return env->NewStringUTF(hello.c_str());
}
