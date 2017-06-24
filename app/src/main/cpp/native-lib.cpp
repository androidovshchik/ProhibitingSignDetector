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

JNIEXPORT void JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_saltPepperNoise(JNIEnv /* *env */,
    jclass /* activity */, jlong matAddress) {
    cv::Mat* mat = (cv::Mat*) matAddress;
    cv::Mat noise = cv::Mat::zeros((*mat).rows, (*mat).cols, CV_8U);
    cv::randu(noise, 0, 255);
    cv::Mat black = noise < 30;
    cv::Mat white = noise > 225;
    (*mat).setTo(255, white);
    (*mat).setTo(0, black);
}

extern "C"

JNIEXPORT jintArray JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_search(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint layerType, jint lowerHue, jint upperHue,
    jint minSaturation, jint minValue, jint blur, jint minArea, jfloat minCircularity,
    jfloat minInertiaRatio, jboolean secondView) {
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

    cv::Mat colorBlured;
    cv::dilate(colorFiltered, colorBlured, cv::Mat(), cv::Point(-1,-1));
    if (layerType == LAYER_BLUR) {
        *(cv::Mat*) matAddress = colorBlured;
    }

    cv::SimpleBlobDetector::Params params;
    params.filterByColor = false;
    params.filterByConvexity = false;
    params.filterByArea = true;
    params.minArea = 255;// A = 254.46900494077 px^2 при D = 9 px
    params.maxArea = 723823; // A = 723822.94738709 px^2 при D = 480 px
    params.filterByCircularity = true;
    params.minCircularity = 0.87f;
    params.filterByInertia = true;
    params.minInertiaRatio = 0.1f;
    cv::Ptr<cv::SimpleBlobDetector> detector = cv::SimpleBlobDetector::create(params);
    std::vector<cv::KeyPoint> keyPoints;
    detector->detect(colorBlured, keyPoints);

    if (layerType != LAYER_RGBA && layerType != LAYER_HSV) {
        cv::cvtColor(*(cv::Mat*) matAddress, *(cv::Mat*) matAddress, cv::COLOR_GRAY2RGB);
    }

    if (secondView) {
        cv::Mat miniView = colorFiltered.clone();
        cv::cvtColor(miniView, miniView, cv::COLOR_GRAY2RGB);
        cv::resize(miniView, miniView, cv::Size(), 0.6, 0.6, cv::INTER_LINEAR);
        cv::Size miniSize = miniView.size();
        cv::Size maxSize = original.size();
        int startY = maxSize.height - miniSize.height;
        for (int y = startY; y < maxSize.height; y++) {
            for (int x = 0; x < miniSize.width; x++) {
                (*(cv::Mat*) matAddress).at<cv::Vec3b>(cv::Point(x, y)) =
                        miniView.at<cv::Vec3b>(cv::Point(x, y - startY));
            }
        }
    }

    jsize length = (jsize) keyPoints.size() * 3;
    if (length == 0) {
        return NULL;
    }
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
    if (circlesArray == NULL) {
        return;
    }
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