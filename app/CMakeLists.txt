cmake_minimum_required(VERSION 3.4.1)

add_library(native-lib SHARED ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/native-lib.cpp)

include_directories(~/OpenCV-android-sdk/sdk/native/jni/include)
add_library(opencv-lib SHARED IMPORTED)
set_target_properties(opencv-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

target_link_libraries(native-lib opencv-lib)