#include <jni.h>

#include <iomanip>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d.hpp>

#define GREEN cv::Scalar(0, 255, 0)
#define SIGN_THICKNESS 4
#define FONT_SCALE 0.8
#define FONT_FACE cv::FONT_HERSHEY_SIMPLEX
#define TEXT_START_X 25
#define TEXT_THICKNESS 2
#define TEXT_LINE_HEIGHT 40

// syncing with java constants
#define LAYER_RGBA 1
#define LAYER_HSV 2
#define LAYER_HUE_LOWER 3
#define LAYER_HUE_UPPER 4
#define LAYER_HUE 5
#define LAYER_SATURATION 6
#define LAYER_VALUE 7
#define LAYER_RED_FILTERED 8
#define LAYER_BLUR 9

extern "C"

JNIEXPORT jintArray JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_search(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint layerType, jint lowerHue, jint upperHue,
    jint minSaturation, jint minValue, jint blur, jint minArea, jfloat minCircularity,
    jfloat minInertiaRatio) {
    cv::Mat original = (*(cv::Mat*) matAddress).clone();

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
    cv::Mat saturationThreshold = channels[1] > minSaturation;
    if (layerType == LAYER_SATURATION) {
        *(cv::Mat*) matAddress = saturationThreshold;
    }
    cv::Mat valueThreshold = channels[2] > minValue;
    if (layerType == LAYER_VALUE) {
        *(cv::Mat*) matAddress = valueThreshold;
    }
    cv::Mat colorFiltered = (minHueThreshold | maxHueThreshold) & saturationThreshold & valueThreshold;
    if (layerType == LAYER_RED_FILTERED) {
        *(cv::Mat*) matAddress = colorFiltered;
    }

    cv::medianBlur(colorFiltered, colorFiltered, blur);
    if (layerType == LAYER_BLUR) {
        *(cv::Mat*) matAddress = colorFiltered;
    }

    if (layerType != LAYER_RGBA && layerType != LAYER_HSV) {
        cv::cvtColor(*(cv::Mat*) matAddress, *(cv::Mat*) matAddress, cv::COLOR_GRAY2RGB);
    }

    cv::SimpleBlobDetector::Params params;
    params.filterByArea = true;
    params.minArea = minArea;
    params.filterByCircularity = true;
    params.minCircularity = minCircularity;
    params.filterByInertia = true;
    params.minInertiaRatio = minInertiaRatio;
    cv::Ptr<cv::SimpleBlobDetector> detector = cv::SimpleBlobDetector::create(params);
    std::vector<cv::KeyPoint> keyPoints;
    detector->detect(colorFiltered, keyPoints);

    jsize length = (jsize) keyPoints.size() * 3;
    jint buffer[length];
    for (int index = 0; index < length; index += 3) {
        buffer[index] = (int) keyPoints[index / 3].pt.x;
        buffer[index + 1] = (int) keyPoints[index / 3].pt.y;
        buffer[index + 2] = (int) keyPoints[index / 3].size / 2;
    }
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
            return "LAYER HSV";
        case LAYER_HUE_LOWER:
            return "LAYER LOWER HUE";
        case LAYER_HUE_UPPER:
            return "LAYER UPPER HUE";
        case LAYER_HUE:
            return "LAYER HUE";
        case LAYER_SATURATION:
            return "LAYER SATURATION";
        case LAYER_VALUE:
            return "LAYER VALUE";
        case LAYER_RED_FILTERED:
            return "LAYER RED FILTERED";
        case LAYER_BLUR:
            return "LAYER BLURED";
        default:
            return "LAYER RGBA";
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