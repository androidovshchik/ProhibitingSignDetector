#include <jni.h>

#include <iomanip>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d.hpp>

typedef unsigned char uc;

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

float verifyCircle(cv::Mat dt, cv::Point2f center, float radius, std::vector<cv::Point2f> & inlierSet) {
    unsigned int counter = 0;
    unsigned int inlier = 0;
    float minInlierDist = 2.0f;
    float maxInlierDistMax = 100.0f;
    float maxInlierDist = radius / 25.0f;
    if (maxInlierDist < minInlierDist) {
        maxInlierDist = minInlierDist;
    }
    if (maxInlierDist > maxInlierDistMax) {
        maxInlierDist = maxInlierDistMax;
    }
    for (float t = 0; t < std::atan(1.0) * 8; t += 0.05f) {
        counter++;
        int cX = (int) (radius * cos(t) + center.x);
        int cY = (int) (radius * sin(t) + center.y);
        if (cX < dt.cols && cX >= 0 && cY < dt.rows && cY >= 0 &&
            dt.at<float>(cY, cX) < maxInlierDist) {
            inlier++;
            inlierSet.push_back(cv::Point2f(cX, cY));
        }
    }
    return (float) inlier / float(counter);
}

inline void getCircle(cv::Point2f& p1,cv::Point2f& p2,cv::Point2f& p3, cv::Point2f& center, float& radius) {
    float x1 = p1.x;
    float x2 = p2.x;
    float x3 = p3.x;
    float y1 = p1.y;
    float y2 = p2.y;
    float y3 = p3.y;
    center.x = (x1 * x1 + y1 * y1) * (y2 - y3) + (x2 * x2 + y2 * y2) * (y3 - y1) + (x3 * x3 + y3 * y3) * (y1 - y2);
    center.x /= (2 * (x1 * (y2 - y3) - y1 * (x2 - x3) + x2 * y3 - x3 * y2));
    center.y = (x1 * x1 + y1 * y1) * (x3 - x2) + (x2 * x2 + y2 * y2) * (x1 - x3) + (x3 * x3 + y3 * y3) * (x2 - x1);
    center.y /= (2 * (x1 * (y2 - y3) - y1 * (x2 - x3) + x2 * y3 - x3 * y2));
    radius = (float) sqrt((center.x - x1) * (center.x - x1) + (center.y - y1) * (center.y - y1));
}

std::vector<cv::Point2f> getPointPositions(cv::Mat binaryImage) {
    std::vector<cv::Point2f> pointPositions;
    for(unsigned int y = 0; y < binaryImage.rows; ++y) {
        for(unsigned int x = 0; x < binaryImage.cols; ++x) {
            if(binaryImage.at<uc>(y, x) > 0){
                pointPositions.push_back(cv::Point2f(x, y));
            }
        }
    }
    return pointPositions;
}

extern "C"

JNIEXPORT jintArray JNICALL Java_ru_dksta_prohibitingsigndetector_ActivityMain_search(JNIEnv *env,
    jclass /* activity */, jlong matAddress, jint layerType, jint lowerHue, jint upperHue,
    jint minSaturation, jint minValue, jint blur, jint minArea, jfloat minCircularity,
    jfloat minInertiaRatio) {
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
    cv::GaussianBlur(colorFiltered, colorBlured, cv::Size(blur, blur), 0);
    if (layerType == LAYER_BLUR) {
        *(cv::Mat*) matAddress = colorBlured;
    }

    /*cv::SimpleBlobDetector::Params params;
    params.filterByArea = true;
    params.minArea = minArea;
    params.filterByCircularity = true;
    params.minCircularity = minCircularity;
    params.filterByInertia = true;
    params.minInertiaRatio = minInertiaRatio;
    cv::Ptr<cv::SimpleBlobDetector> detector = cv::SimpleBlobDetector::create(params);
    std::vector<cv::KeyPoint> keyPoints;
    detector->detect(colorBlured, keyPoints);*/

    std::vector<cv::Point2f> edgePositions;
    edgePositions = getPointPositions(colorBlured);

// create distance transform to efficiently evaluate distance to nearest edge
    cv::Mat dt;
    cv::distanceTransform(255 - colorBlured, dt, CV_DIST_L1, 3);

    for (int t = 0; t < 30; t++) {
        //RANSAC: randomly choose 3 point and create a circle:
        //TODO: choose randomly but more intelligent,
        //so that it is more likely to choose three points of a circle.
        //For example if there are many small circles, it is unlikely to randomly choose 3 points of the same circle.
        unsigned int idx1 = rand()%edgePositions.size();
        unsigned int idx2 = rand()%edgePositions.size();
        unsigned int idx3 = rand()%edgePositions.size();

        // we need 3 different samples:
        if(idx1 == idx2) continue;
        if(idx1 == idx3) continue;
        if(idx3 == idx2) continue;

        // create circle from 3 points:
        cv::Point2f center; float radius;
        getCircle(edgePositions[idx1],edgePositions[idx2],edgePositions[idx3],center,radius);

        float minCirclePercentage = 0.4f;

        // inlier set unused at the moment but could be used to approximate a (more robust) circle from alle inlier
        std::vector<cv::Point2f> inlierSet;

        //verify or falsify the circle by inlier counting:
        float cPerc = verifyCircle(dt,center,radius, inlierSet);

        if(cPerc >= minCirclePercentage) {
            cv::circle((*(cv::Mat*) matAddress), center,radius, cv::Scalar(255,255,0),1);

            // accept circle => remove it from the edge list
            cv::circle(colorBlured, center,radius,cv::Scalar(0),10);

            //update edge positions and distance transform
            edgePositions = getPointPositions(colorBlured);
            cv::distanceTransform(255 - colorBlured, dt,CV_DIST_L1, 3);
        }

        // prevent cases where no fircle could be extracted (because three points collinear or sth.)
        // filter NaN values
        if((center.x == center.x)&&(center.y == center.y)&&(radius == radius)) {
            cv::circle(colorBlured, center, radius, cv::Scalar(255));
        }
    }

    if (layerType != LAYER_RGBA && layerType != LAYER_HSV) {
        cv::cvtColor(*(cv::Mat*) matAddress, *(cv::Mat*) matAddress, cv::COLOR_GRAY2RGB);
    }

    jsize length = 0;//(jsize) keyPoints.size() * 3;
    if (length == 0) {
        return NULL;
    }
    /*jint buffer[length];
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
    return result;*/
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