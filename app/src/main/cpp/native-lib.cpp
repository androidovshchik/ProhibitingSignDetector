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

// syncing with java constants
#define LAYER_DEFAULT 1
#define LAYER_HSV 2
#define LAYER_HUE_LOWER 3
#define LAYER_HUE_UPPER 4
#define LAYER_HUE 5
#define LAYER_SATURATION 6
#define LAYER_COLOR_FILTERED 7
#define LAYER_BLUR 8

extern "C"

JNIEXPORT jintArray JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_search(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint layerType, jint lowerHue, jint upperHue,
    jint saturation, jint blur) {
    cv::Mat original = (*(cv::Mat*) matAddress).clone();

    cv::medianBlur(original, original, 3);

    cv::Mat hsv;
    cv::cvtColor(original, hsv, cv::COLOR_RGB2HSV);
    if (layerType == LAYER_HSV) {
        *(cv::Mat*) matAddress = hsv;
    }

    std::vector<cv::Mat> channels;
    cv::split(hsv, channels);
    cv::Mat minHueThreshold = channels[0] < lowerHue;
    if (layerType == LAYER_HUE_LOWER) {
        *(cv::Mat*) matAddress = minHueThreshold;
    }
    cv::Mat maxHueThreshold = channels[0] > upperHue;
    if (layerType == LAYER_HUE_UPPER) {
        *(cv::Mat*) matAddress = maxHueThreshold;
    }
    if (layerType == LAYER_HUE) {
        *(cv::Mat*) matAddress = minHueThreshold | maxHueThreshold;
    }
    cv::Mat saturationThreshold = channels[1] > saturation;
    if (layerType == LAYER_SATURATION) {
        *(cv::Mat*) matAddress = saturationThreshold;
    }
    cv::Mat colorFiltered = (minHueThreshold | maxHueThreshold) & saturationThreshold;
    if (layerType == LAYER_COLOR_FILTERED) {
        *(cv::Mat*) matAddress = colorFiltered;
    }

    if (layerType == LAYER_BLUR) {
        cv::medianBlur(colorFiltered, *(cv::Mat*) matAddress, blur);
    }

    if (layerType != LAYER_DEFAULT && layerType != LAYER_HSV) {
        cv::cvtColor(*(cv::Mat*) matAddress, *(cv::Mat*) matAddress, cv::COLOR_GRAY2RGB);
    }

    cv::SimpleBlobDetector::Params params;
// Change thresholds
    params.minThreshold = 10;
    params.maxThreshold = 200;
// Filter by Area.
    params.filterByArea = true;
    params.minArea = 1500;
// Filter by Circularity
    params.filterByCircularity = true;
    params.minCircularity = 0.1;
// Filter by Convexity
    params.filterByConvexity = true;
    params.minConvexity = 0.87;
// Filter by Inertia
    params.filterByInertia = true;
    params.minInertiaRatio = 0.01;
    std::vector<cv::KeyPoint> keypoints;
    cv::Ptr<cv::SimpleBlobDetector> detector = cv::SimpleBlobDetector::create(params);
    detector->detect(colorFiltered, keypoints);
    for (int i=0; i<keypoints.size(); i++){
        float X = keypoints[i].pt.x;
        float Y = keypoints[i].pt.y;
        cv::circle(*(cv::Mat*) matAddress, cv::Point(X,Y), keypoints[i].size, GREEN, 4);
    }

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

std::string getLayerTypeDesc(jint layerType) {
    switch (layerType) {
        case LAYER_HSV:
            return "LAYER TYPE HSV";
        case LAYER_HUE_LOWER:
            return "LAYER TYPE LOWER HUE";
        case LAYER_HUE_UPPER:
            return "LAYER TYPE UPPER HUE";
        case LAYER_HUE:
            return "LAYER TYPE HUE";
        case LAYER_SATURATION:
            return "LAYER TYPE SATURATION";
        case LAYER_COLOR_FILTERED:
            return "LAYER TYPE RED FILTERED";
        case LAYER_BLUR:
            return "LAYER TYPE BLURED";
        default:
            return "LAYER TYPE RGBA";
    }
}

extern "C"

JNIEXPORT void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_information(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint fpsCount, jint layerType, jintArray circlesArray) {
    cv::Mat* mat = (cv::Mat*) matAddress;
    int textStartY = TEXT_LINE_HEIGHT;
    std::ostringstream output;
    output << std::setw(2) << std::setfill('0') << fpsCount << " FPS";
    cv::putText(*mat, output.str(), cv::Point(TEXT_START_X, textStartY), FONT_FACE, FONT_SCALE,
                GREEN, TEXT_THICKNESS);
    output.seekp(0);
    textStartY += TEXT_LINE_HEIGHT;
    cv::putText(*mat, getLayerTypeDesc(layerType), cv::Point(TEXT_START_X, textStartY), FONT_FACE,
                FONT_SCALE, GREEN, TEXT_THICKNESS);
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

extern "C"

JNIEXPORT void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_rotation(JNIEnv /* *env */,
    jclass /* activity */, jlong matAddress, jint angle) {
    CV_Assert(angle % 90 == 0 && angle <= 360 && angle >= -360);
    cv::Mat* mat = (cv::Mat*) matAddress;
    if (angle == 180 || angle == -180) {
        cv::flip(*mat, *mat, -1);
    }
}