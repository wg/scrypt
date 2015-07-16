LOCAL_PATH      := $(call my-dir)

SSE2 :=
TARGET_PLATFORM := android-19

include $(CLEAR_VARS)
LOCAL_MODULE     := scrypt

LOCAL_SRC_FILES  := $(wildcard c/*.c)
LOCAL_SRC_FILES  := $(filter-out $(if $(SSE2),%-nosse.c,%-sse.c),$(LOCAL_SRC_FILES))
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS     += -DHAVE_CONFIG_H
LOCAL_LDFLAGS    += -lc

include $(BUILD_SHARED_LIBRARY)
