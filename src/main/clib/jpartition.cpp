/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#include <jni.h>
#include "jsystem.h"
#include <sys/mount.h>
#include <sys/swap.h>
#include <sys/statvfs.h>
#include <iostream>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include "common.cpp"

extern char **environ;
int hostname_max_size = 64 ;

using namespace std;

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_system_JSystem_mount (JNIEnv *env, jclass clazz, jstring src, jstring dst,jstring file_system_type,jint jflags,jstring options) {
    const char *src_path = env->GetStringUTFChars(src,0);
    const char *dst_path = env->GetStringUTFChars(dst,0);
    const char *fs_type = env->GetStringUTFChars(file_system_type,0);
    const char *mnt_opt = options != NULL ? env->GetStringUTFChars(options,0) : NULL;
    int flags = (int) jflags;
    int r =  mount(src_path,dst_path,fs_type,flags,mnt_opt)  ;
    env->ReleaseStringUTFChars(src,src_path);
    env->ReleaseStringUTFChars(dst,dst_path);
    env->ReleaseStringUTFChars(file_system_type,fs_type);
    env->ReleaseStringUTFChars(options,mnt_opt);

    return r == 0;
}

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_system_JSystem_umount (JNIEnv *env, jclass clazz, jstring target) {
    const char *target_path = env->GetStringUTFChars(target,0);
    int r = umount(target_path) ;
    env->ReleaseStringUTFChars(target,target_path);
    return r == 0;
}

JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_system_JSystem_getFilesystemStatistics(JNIEnv *env, jclass clazz, jstring jFilesystem) {
    if (jFilesystem == NULL) {
      throwException(env,"Mount point is null");
      return NULL;
    }

    struct blkinfo blk_info ;
    struct statvfs fiData;
    struct swapinfo swInfo ;

    const char* fs = env->GetStringUTFChars(jFilesystem, 0);
    std::string file_system(fs);

    if (file_system.size() == 0) {
      env->ReleaseStringUTFChars(jFilesystem, fs);
      throwException(env,"Mount point is empty");
      return NULL;
    }

    get_blk_info(file_system,blk_info);
    bool isMounted = isFilesystemMounted(file_system);

    std::string mountPoint = isMounted ? getMountPoint(file_system) : "";

    if (isMounted) {
        statvfs(mountPoint.c_str(), &fiData);
    } else if (blk_info.type == "swap") {
        get_swap_info(file_system,swInfo);
    }

    jclass hddpClass = env->FindClass("ir/moke/jsysbox/system/HDDPartition");
    jmethodID constructor = env->GetMethodID(hddpClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Long;)V");

    jobject jTotalSizeObj = NULL ; 
    jobject jFreeSizeObj = NULL;
    
    if (isMounted) {
        long freeSize = (fiData.f_bsize/1024) * fiData.f_bfree;
        jFreeSizeObj = env->NewObject(env->FindClass("java/lang/Long"), env->GetMethodID(env->FindClass("java/lang/Long"), "<init>", "(J)V"), freeSize);
    } 

    if (blk_info.type == "swap") {
        jTotalSizeObj = env->NewObject(env->FindClass("java/lang/Long"), env->GetMethodID(env->FindClass("java/lang/Long"), "<init>", "(J)V"), swInfo.size);
        jFreeSizeObj = env->NewObject(env->FindClass("java/lang/Long"), env->GetMethodID(env->FindClass("java/lang/Long"), "<init>", "(J)V"), swInfo.size - swInfo.used);
    } else {
        long size = get_filesystem_size(file_system);
        jTotalSizeObj = env->NewObject(env->FindClass("java/lang/Long"), env->GetMethodID(env->FindClass("java/lang/Long"), "<init>", "(J)V"), size);
    }

    jstring jMountPoint = env->NewStringUTF(mountPoint.c_str());
    jstring jUUID  = env->NewStringUTF(blk_info.uuid.c_str());
    jstring jLabel = env->NewStringUTF(blk_info.label.c_str());
    jstring jType  = env->NewStringUTF(blk_info.type.c_str());

    jobject hddPartitionObj = env->NewObject(hddpClass, constructor, jFilesystem,jMountPoint,jUUID,jLabel,jType, jTotalSizeObj, jFreeSizeObj);

    env->ReleaseStringUTFChars(jFilesystem, fs);
    env->DeleteLocalRef(jFilesystem);
    env->DeleteLocalRef(jTotalSizeObj);
    env->DeleteLocalRef(jFreeSizeObj);
    env->DeleteLocalRef(hddpClass);

    return hddPartitionObj;
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_swapOn(JNIEnv *env, jclass clazz, jstring jblkPath) {
  const char* blk = env->GetStringUTFChars(jblkPath,0);
  int ret = swapon(blk,SWAP_FLAG_PREFER|SWAP_FLAG_DISCARD); 
  if (ret != 0) {
    throwException(env,"Failed to swap on"); 
  }
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_swapOff(JNIEnv *env, jclass clazz,jstring jblkPath) {
  const char* blk = env->GetStringUTFChars(jblkPath,0);
  int ret = swapoff(blk); 
  if (ret != 0) {
    throwException(env,"Failed to swap on"); 
  }
}


