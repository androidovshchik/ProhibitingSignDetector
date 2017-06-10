#include <android/log.h>
#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d.hpp>

#define TAG "JNI"

extern "C"

JNIEXPORT jintArray JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_search(JNIEnv *env,
    jclass /* activity */, jlong matAddress) {
    cv::Mat original = (*(cv::Mat*) matAddress).clone();
    cv::medianBlur(original, original, 3);
    // Convert input image to HSV
    cv::Mat hsv;
    cv::cvtColor(original, hsv, CV_RGB2HSV);

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
    original = (thres1 | thres2) & saturationThres;
    cv::GaussianBlur(original, original, cv::Size(9, 9), 2, 2);
    //*(cv::Mat*) matAddress = original;

    /*cv::Ptr<cv::FeatureDetector> blobsDetector = cv::SimpleBlobDetector::create();
    std::vector<cv::KeyPoint> keyPoints;
    blobsDetector->detect(original, keyPoints);
    */
    //jsize length = (jsize) keyPoints.size() * 3;
    jsize length = 0;
    jint buffer[length];
    for (size_t index = 0; index < length; index += 3) {
        /*buffer[index] = static_cast<jint>(keyPoints[index].pt.x);
        buffer[index + 1] = static_cast<jint>(keyPoints[index].pt.y);
        buffer[index + 2] = 20;*/
    }
    jintArray result = env->NewIntArray(length);
    if (result == NULL) {
        return NULL;
    }
    env->SetIntArrayRegion(result, 0, length, buffer);
    return result;
    /*std::vector<cv::Vec3f> circles;
    cv::HoughCircles(original, circles, CV_HOUGH_GRADIENT, 1, original.rows/8, 100, 20, 0, 0);
    jsize length = (jsize) circles.size() * 3;
    jint buffer[length];
    for (size_t index = 0; index < length; index += 3) {
        buffer[index] = static_cast<jint>(circles[index / 3][0]);
        buffer[index + 1] = static_cast<jint>(circles[index / 3][1]);
        buffer[index + 2] = static_cast<jint>(circles[index / 3][2]);
    }*/
}

extern "C"

JNIEXPORT void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_selection(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jintArray circlesArray) {
    cv::Mat* mat = (cv::Mat*) matAddress;
    jsize length = env->GetArrayLength(circlesArray);
    if (length % 3 != 0) {
        return;
    }
    jint* elements = env->GetIntArrayElements(circlesArray, JNI_FALSE);
    for (size_t index = 0; index < length; index += 3) {
        cv::Point center(elements[index], elements[index + 1]);
        cv::circle(*mat, center, elements[index + 2], cv::Scalar(0, 255, 0), 4);
    }
    env->ReleaseIntArrayElements(circlesArray, elements, 0);
    cv::putText(*mat, "Hi all...", cv::Point(50,50), cv::FONT_HERSHEY_SIMPLEX, 1,
                cv::Scalar(0,200,200), 5);
}