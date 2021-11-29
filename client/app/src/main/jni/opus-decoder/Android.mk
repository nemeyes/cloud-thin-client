# Android.mk for opus-decoder and binding
MY_LOCAL_PATH := $(call my-dir)

include $(call all-subdir-makefiles)

LOCAL_PATH := $(MY_LOCAL_PATH)

include $(CLEAR_VARS)
LOCAL_MODULE    := opus-decoder

LOCAL_SRC_FILES := opus.c \

LOCAL_CFLAGS := -DLC_ANDROID

ifeq ($(NDK_DEBUG),1)
LOCAL_CFLAGS += -DLC_DEBUG
endif

LOCAL_LDLIBS := -llog

LOCAL_STATIC_LIBRARIES := libopus
LOCAL_LDFLAGS += -Wl,--exclude-libs,ALL

include $(BUILD_SHARED_LIBRARY)
