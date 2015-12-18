LOCAL_PATH      := $(call my-dir)

SSE2 :=
TARGET_PLATFORM := android-9

include $(CLEAR_VARS)
LOCAL_MODULE     := scrypt

LOCAL_SRC_FILES  := $(wildcard $(LOCAL_PATH)/c/*.c)
LOCAL_SRC_FILES  := $(filter-out $(if $(SSE2),%-nosse.c,%-sse.c),$(LOCAL_SRC_FILES))
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS     += -DANDROID -DHAVE_CONFIG_H -DANDROID_TARGET_ARCH="$(TARGET_ARCH)"
LOCAL_LDFLAGS    += -lc -llog

include $(BUILD_SHARED_LIBRARY)
