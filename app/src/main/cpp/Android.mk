LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES := on
include C:\ECE420FinalProject\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_MODULE := face-lib
LOCAL_SRC_FILES += $(LOCAL_PATH)/face-lib.cpp $(LOCAL_PATH)/FaceRecognitionLib/Facebase.cpp
LOCAL_SRC_FILES += $(LOCAL_PATH)/FaceRecognitionLib/Eigenfaces.cpp $(LOCAL_PATH)/FaceRecognitionLib/Fisherfaces.cpp
LOCAL_SRC_FILES += $(LOCAL_PATH)/FaceRecognitionLib/PCA.cpp $(LOCAL_PATH)/FaceRecognitionLib/LDA.cpp
LOCAL_C_INCLUDES += C:\ECE420FinalProject\eigen-eigen-26667be4f70b $(LOCAL_PATH)\FaceRecognitionLib\RedSVD\include
LOCAL_LDLIBS += -llog -ldl
LOCAL_CPPFLAGS += -std=gnu++11 -frtti -fexceptions

include $(BUILD_SHARED_LIBRARY)
