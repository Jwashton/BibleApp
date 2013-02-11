LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog
LOCAL_MODULE    := BibleEngine
LOCAL_SRC_FILES := JCVerse.cpp JCMargin.cpp JCStrongs.cpp

include $(BUILD_SHARED_LIBRARY)

