#include <android/log.h>
#include <jni.h>

#include <iomanip>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d.hpp>

#define TAG "JNI"
#define GREEN cv::Scalar(0, 255, 0)
#define SIGN_THICKNESS 4
#define FONT_SCALE 0.8
#define FONT_FACE cv::FONT_HERSHEY_SIMPLEX
#define TEXT_START_X 25
#define TEXT_THICKNESS 2
#define TEXT_LINE_HEIGHT 40
//__android_log_print(ANDROID_LOG_DEBUG, TAG, "FPS %d", fpsCount);

extern "C"

JNIEXPORT jintArray JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_search(JNIEnv *env,
    jclass /* activity */, jlong matAddress) {
    cv::Mat original = (*(cv::Mat*) matAddress).clone();

    cv::medianBlur(original, original, 3);

    cv::Mat hsv;
    cv::cvtColor(original, hsv, cv::COLOR_RGB2HSV);

    std::vector<cv::Mat> channels;
    cv::split(hsv, channels);
    cv::Mat minHueThreshold = channels[0] < 12;
    cv::Mat maxHueThreshold = channels[0] > 168;
    cv::Mat saturationThreshold = channels[1] > 50;
    *(cv::Mat*) matAddress = (minHueThreshold | maxHueThreshold) & saturationThreshold;

    //findContours(colorFiltered, contours, hierarchy, cv::RETR_TREE, cv::CHAIN_APPROX_SIMPLE);

    /*std::vector<std::vector<cv::Point>> contours;
    std::vector<cv::Vec4i> hierarchy;
    findContours(colorFiltered, contours, hierarchy, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
    for (int i = 0; i < contours.size(); i++) {
        std::vector<cv::Point> contour;
        cv::approxPolyDP(contours[i], contour, 0.01 * cv::arcLength(contours[i], JNI_TRUE), JNI_TRUE);
        if (contour.size() > 15) {
            drawContours(*(cv::Mat *) matAddress, contours, i, GREEN, SIGN_THICKNESS);
        }
        /*std::vector<cv::Point> contoursOUT;
        cv::approxPolyDP(contours[i], contoursOUT, 0.01 * cv::arcLength(contours[i], JNI_TRUE),
                                  JNI_TRUE);
        double area = cv::contourArea(contours[i]);
        if ((contoursOUT.size() > 8) & (area > 30)) {
            drawContours(*(cv::Mat *) matAddress, contours, i, GREEN, 2, 8, hierarchy, 0);
        }
    }*/

    jsize length = 0;
    jint buffer[length];
    jintArray result = env->NewIntArray(length);
    if (result == NULL) {
        return NULL;
    }
    env->SetIntArrayRegion(result, 0, length, buffer);
    return result;
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
    for (int index = 0; index < length; index += 3) {
        cv::Point center(elements[index], elements[index + 1]);
        cv::circle(*mat, center, elements[index + 2], GREEN, SIGN_THICKNESS);
    }
    env->ReleaseIntArrayElements(circlesArray, elements, 0);
}

extern "C"

JNIEXPORT void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_information(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint fpsCount, jintArray circlesArray) {
    cv::Mat* mat = (cv::Mat*) matAddress;
    int textStartY = TEXT_LINE_HEIGHT;
    std::ostringstream output;
    output << std::setw(2) << std::setfill('0') << fpsCount << " FPS";
    cv::putText(*mat, output.str(), cv::Point(TEXT_START_X, textStartY), FONT_FACE, FONT_SCALE,
                GREEN, TEXT_THICKNESS);
    output.seekp(0);
    jsize length = env->GetArrayLength(circlesArray);
    if (length % 3 != 0) {
        return;
    }
    jint* elements = env->GetIntArrayElements(circlesArray, JNI_FALSE);
    for (int index = 0; index < length; index += 3) {
        textStartY += TEXT_LINE_HEIGHT;
        output << '[' << std::setw(3) << std::setfill('0') << elements[index] << ", "
               << std::setw(3) << std::setfill('0') << elements[index + 1] << ", "
               << std::setw(3) << std::setfill('0') << elements[index + 2] << ']';
        cv::putText(*mat, output.str(), cv::Point(TEXT_START_X, textStartY), FONT_FACE, FONT_SCALE,
                    GREEN, TEXT_THICKNESS);
        output.seekp(0);
    }
    env->ReleaseIntArrayElements(circlesArray, elements, 0);
}

/*
 *@brief rotate image by multiple of 90 degrees
 *
 *@param source : input image
 *@param dst : output image
 *@param angle : factor of 90, even it is not factor of 90, the angle
 * will be mapped to the range of [-360, 360].
 * {angle = 90n; n = {-4, -3, -2, -1, 0, 1, 2, 3, 4} }
 * if angle bigger than 360 or smaller than -360, the angle will
 * be map to -360 ~ 360.
 * mapping rule is : angle = ((angle / 90) % 4) * 90;
 *
 * ex : 89 will map to 0, 98 to 90, 179 to 90, 270 to 3, 360 to 0.
 *
 */
void rotate_image_90n(cv::Mat &src, cv::Mat &dst, int angle)
{
    if(src.data != dst.data){
        src.copyTo(dst);
    }

    angle = ((angle / 90) % 4) * 90;

    //0 : flip vertical; 1 flip horizontal
    bool const flip_horizontal_or_vertical = angle > 0 ? 1 : 0;
    int const number = std::abs(angle / 90);

    for(int i = 0; i != number; ++i){
        cv::transpose(dst, dst);
        cv::flip(dst, dst, flip_horizontal_or_vertical);
    }
}

extern "C"

JNIEXPORT void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_rotate(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint angle) {
    CV_Assert(angle % 90 == 0 && angle <= 360 && angle >= -360);
    if(angle == 270 || angle == -90){
        // Rotate clockwise 270 degrees
        cv::transpose(src, dst);
        cv::flip(dst, dst, 0);
    } else if(angle == 180 || angle == -180){
        // Rotate clockwise 180 degrees
        cv::flip(src, dst, -1);
    } else if(angle == 90 || angle == -270){
        // Rotate clockwise 90 degrees
        cv::transpose(src, dst);
        cv::flip(dst, dst, 1);
    } else if(angle == 360 || angle == 0 || angle == -360){
        if(src.data != dst.data){
            src.copyTo(dst);
        }
    }
}