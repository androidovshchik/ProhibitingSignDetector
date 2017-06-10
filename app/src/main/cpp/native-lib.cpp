#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

extern "C"

void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_mask(JNIEnv *env, jobject instance,
                                                                     jlong matAddress) {
    cv::Mat &mGr = *(cv::Mat*) matAddress;

    cv::Mat src;
    cv::Mat hsv;

    cv::cvtColor(mGr, src , CV_RGBA2RGB);
    cv::cvtColor(src, hsv, CV_RGB2HSV);

    std::vector<cv::Mat> channels;
    cv::split(hsv, channels);

    // opencv = hue values are divided by 2 to fit 8 bit range
    float red1 = 25 / 2.0f;
    // red has one part at the beginning and one part at the end of the range (I assume 0° to 25° and 335° to 360°)
    float red2 = (360 - 25) / 2.0f;

    // compute both thresholds
    cv::Mat thres1 = channels[0] < red1;
    cv::Mat thres2 = channels[0] > red2;

    // choose some minimum saturation
    cv::Mat saturationThres = channels[1] > 50;

    // combine the results
    mGr = (thres1 | thres2) & saturationThres;
}