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
#include <parted/parted.h>
#include <cstddef>
#include <sys/mount.h>
#include <sys/swap.h>
#include <sys/statvfs.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <libudev.h>
#include "jdisk_manager.h"
#include "disk_utils.cpp"


JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_mount (JNIEnv *env, jclass clazz, jstring src, jstring dst,jstring file_system_type,jint jflags,jstring options) {
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

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_umount (JNIEnv *env, jclass clazz, jstring target) {
    const char *target_path = env->GetStringUTFChars(target,0);
    int r = umount(target_path) ;
    env->ReleaseStringUTFChars(target,target_path);
    return r == 0;
}

JNIEXPORT jobjectArray JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_getPartitionInformation(JNIEnv *env, jclass clazz, jstring jblkPath) { 
    PedDevice* dev = getBlockDevice(env, jblkPath);

    if (!dev) {
       throwException(env, "Failed to open device");
       return NULL ;
    }

    PedDisk* disk = ped_disk_new(dev);
    if (!disk) {
        close(dev,NULL,NULL);
        return NULL;
    }

    std::vector<PartitionInfo> partitionInfos;
    PedPartition* part = nullptr;
    while ((part = ped_disk_next_partition(disk, part)) != nullptr) {
        if (part->num > 0) {
            PartitionInfo info = getPartitionInfo(env, part, dev);
            partitionInfos.push_back(info);
        }
    }

    close(dev,disk,NULL);

    auto size = static_cast<jsize>(partitionInfos.size());
    jclass partitionInfoClass = env->FindClass("Lir/moke/jsysbox/disk/PartitionInformation;");
    jobjectArray result = env->NewObjectArray(size, partitionInfoClass, nullptr);

    jmethodID constructor = env->GetMethodID(partitionInfoClass, "<init>", "()V");

    for (size_t i = 0; i < partitionInfos.size(); ++i) {
        jobject partitionInfoObj = env->NewObject(partitionInfoClass, constructor);

        jfieldID blkField = env->GetFieldID(partitionInfoClass, "blk", "Ljava/lang/String;");
        jfieldID mountPointField = env->GetFieldID(partitionInfoClass, "mountPoint", "Ljava/lang/String;");
        jfieldID uuidField = env->GetFieldID(partitionInfoClass, "uuid", "Ljava/lang/String;");
        jfieldID labelField = env->GetFieldID(partitionInfoClass, "label", "Ljava/lang/String;");
        jfieldID typeField = env->GetFieldID(partitionInfoClass, "type", "Ljava/lang/String;");
        jfieldID totalSizeField = env->GetFieldID(partitionInfoClass, "totalSize", "J");
        jfieldID freeSizeField = env->GetFieldID(partitionInfoClass, "freeSize", "J");
        jfieldID startSectorField = env->GetFieldID(partitionInfoClass, "startSector", "J");
        jfieldID endSectorField = env->GetFieldID(partitionInfoClass, "endSector", "J");
        jfieldID sectorSizeField = env->GetFieldID(partitionInfoClass, "sectorSize", "J");

        env->SetObjectField(partitionInfoObj, blkField, env->NewStringUTF(partitionInfos[i].blk.c_str()));
        env->SetObjectField(partitionInfoObj, mountPointField, env->NewStringUTF(partitionInfos[i].mountPoint.c_str()));
        env->SetObjectField(partitionInfoObj, uuidField, env->NewStringUTF(partitionInfos[i].uuid.c_str()));
        env->SetObjectField(partitionInfoObj, labelField, env->NewStringUTF(partitionInfos[i].label.c_str()));
        env->SetObjectField(partitionInfoObj, typeField, env->NewStringUTF(partitionInfos[i].type.c_str()));
        env->SetLongField(partitionInfoObj, totalSizeField, partitionInfos[i].totalSize);
        env->SetLongField(partitionInfoObj, freeSizeField, partitionInfos[i].freeSize);
        env->SetLongField(partitionInfoObj, startSectorField, partitionInfos[i].startSector);
        env->SetLongField(partitionInfoObj, endSectorField, partitionInfos[i].endSector);
        env->SetLongField(partitionInfoObj, sectorSizeField, partitionInfos[i].sectorSize);

        env->SetObjectArrayElement(result, i, partitionInfoObj);
    }

    return result;
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_swapOn(JNIEnv *env, jclass clazz, jstring jblkPath) {
  const char* blk = env->GetStringUTFChars(jblkPath,0);
  int ret = swapon(blk,SWAP_FLAG_PREFER|SWAP_FLAG_DISCARD); 
  if (ret != 0) {
    throwException(env,"Failed to swap on"); 
  }
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_swapOff(JNIEnv *env, jclass clazz,jstring jblkPath) {
  const char* blk = env->GetStringUTFChars(jblkPath,0);
  int ret = swapoff(blk); 
  if (ret != 0) {
    throwException(env,"Failed to swap on"); 
  }
}

JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionTableType(JNIEnv *env, jclass clazz,jstring jblkPath) {

  PedDevice* dev = getBlockDevice(env, jblkPath);
  if (!dev) {
    throwException(env, "Failed to open device");
    return NULL;
  }

  PedDiskType* disk_type = ped_disk_probe(dev) ; 
  if (!disk_type) {
    return NULL ;
  } else {
    const char* name = disk_type->name;
    jobject ptt = NULL;
    jclass enumClass = env->FindClass("Lir/moke/jsysbox/disk/PartitionTable;");

    if (strcmp(name, "gpt") == 0) {
      jfieldID fieldId = env->GetStaticFieldID(enumClass,"GPT","Lir/moke/jsysbox/disk/PartitionTable;");
      ptt = env->GetStaticObjectField(enumClass,fieldId);
    } else if (strcmp(name, "msdos") == 0) {
      jfieldID fieldId = env->GetStaticFieldID(enumClass,"MSDOS","Lir/moke/jsysbox/disk/PartitionTable;");
      ptt = env->GetStaticObjectField(enumClass,fieldId);
    } else {
      throwException(env, "Unknown partition table");
    }

    close(dev,NULL,NULL);
    return ptt ;
  }
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_initializePartitionTable(JNIEnv *env, jclass clazz,jstring jblkPath, jobject enumObj) {
  
  // normalize enum name
  char* enumName = getEnumName(env,enumObj); 
  toLowerCase(enumName);

  PedDevice* dev = getBlockDevice(env, jblkPath);
  if (!dev) {
    throwException(env, "Failed to open device");
    free(enumName);
    return ;
  }

  // Initialize a new MBR partition table
  PedDiskType* disk_type =  ped_disk_type_get(enumName); 
  if (!disk_type) {
    close(dev,NULL,NULL);
    free(enumName);
    throwException(env, "Invalid partition table");
    return ;
  } 

  PedDisk* disk = ped_disk_new_fresh(dev, disk_type); 
  if (!disk) {
    close(dev,disk,NULL);
    free(enumName);
    throwException(env, "Failed to create partition table");
    return ;
  } 

  // Commit the initial partition table to the disk
  if (!ped_disk_commit(disk)) {
    throwException(env,"Failed to commit partition table");
    close(dev,disk,NULL);
    free(enumName);
    return ;
  }

  close(dev,disk,NULL);
  free(enumName);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_createPartition(JNIEnv *env, jclass clazz, jstring jblkPath, jlong start, jlong end, jobject enumObj,jboolean isPrimary) {
  char* enumName = getEnumName(env,enumObj); 
  toLowerCase(enumName);

  PedDevice* dev = getBlockDevice(env, jblkPath);
  if (!dev) {
    throwException(env, "Failed to open device");
    free(enumName);
    return ;
  }

  PedDisk* disk = ped_disk_new(dev);
  if (!disk) {
    close(dev,NULL,NULL);
    free(enumName);
    throwException(env, "Failed to create disk object");
    return ;
  }

  PedFileSystemType* fs_type = ped_file_system_type_get(enumName);
  if (!fs_type) {
    close(dev,disk,NULL);
    free(enumName);
    throwException(env, "Invalid filesystem type");
    return ;
  }

  PedPartition* part = ped_partition_new(disk, isPrimary ? PED_PARTITION_NORMAL : PED_PARTITION_EXTENDED, fs_type, start, end);
  if (!part) {
    close(dev,disk,part);
    free(enumName);
    throwException(env, "Failed to create partition object");
    return ;
  }

  if (!ped_disk_add_partition(disk, part, ped_constraint_any(dev))) {
    close(dev,disk,part);
    free(enumName);
    throwException(env, "Failed to add partition");
    return ;
  }

  if (!ped_disk_commit(disk)) {
    close(dev,disk,NULL);
    throwException(env, "Failed to commit disk changes");
    return ;
  }


  free(enumName);
  close(dev,NULL,part);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_deletePartition (JNIEnv *env, jclass clazz, jstring jblkPath, jint partitionNumber) {
  PedDevice* dev = getBlockDevice(env, jblkPath);
  if (!dev) {
    throwException(env, "Failed to open device");
    return ;
  }

  PedDisk* disk = ped_disk_new(dev);
  if (!disk) {
    close(dev,NULL,NULL);
    throwException(env, "Failed to create disk object");
    return ;
  }

  PedPartition* part = ped_disk_get_partition(disk,partitionNumber);
  if (!part) {
    close(dev,disk,NULL);
    throwException(env, "Partition does not exists");
    return ;
  }

  if (!ped_disk_delete_partition(disk, part)) {
    close(dev,disk,part);
    throwException(env, "Failed to add partition");
    return ;
  }

  if (!ped_disk_commit(disk)) {
    close(dev,disk,NULL);
    throwException(env, "Failed to commit disk changes");
    return ;
  }

  close(dev,NULL,part);
}


JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_bootable (JNIEnv *env, jclass clazz, jstring jblkPath, jint partitionNumber) {
  PedDevice* dev = getBlockDevice(env, jblkPath);
  if (!dev) {
    throwException(env, "Failed to open device");
    return ;
  }

  PedDisk* disk = ped_disk_new(dev);
  if (!disk) {
    close(dev,NULL,NULL);
    throwException(env, "Failed to create disk object");
    return ;
  }

  PedPartition* part = ped_disk_get_partition(disk,partitionNumber);
  if (!part) {
    close(dev,disk,NULL);
    throwException(env, "Partition does not exists");
    return ;
  }

  if (!ped_partition_set_flag(part,PED_PARTITION_BOOT,1)) {
    close(dev,disk,part);
    throwException(env, "Failed to add partition");
    return ;
  }

  if (!ped_disk_commit(disk)) {
    close(dev,disk,NULL);
    throwException(env, "Failed to commit disk changes");
    return ;
  }

  close(dev,NULL,part);

}

JNIEXPORT jobjectArray JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_getDisks(JNIEnv *env, jclass clazz) {
  struct udev* udev = udev_new();
  if (!udev) {
    throwException(env, "Filed to create udev object");
    return NULL ;
  }

  struct udev_enumerate* enumerate = udev_enumerate_new(udev);
  udev_enumerate_add_match_subsystem(enumerate, "block");
  udev_enumerate_add_match_property(enumerate, "DEVTYPE", "disk");
  udev_enumerate_scan_devices(enumerate);

  struct udev_list_entry* devices = udev_enumerate_get_list_entry(enumerate);
  struct udev_list_entry* entry;

  std::vector<std::string> list ;
  udev_list_entry_foreach(entry, devices) {
      const char* path = udev_list_entry_get_name(entry);
      struct udev_device* device = udev_device_new_from_syspath(udev, path);

      const char* devNode = udev_device_get_devnode(device);
      std::string str(devNode) ;
      list.push_back(str);
      udev_device_unref(device);
  }

  udev_enumerate_unref(enumerate);
  udev_unref(udev); 
  
  auto size = static_cast<jsize>(list.size());
  jclass stringClass = env->FindClass("java/lang/String");
  jobjectArray result = env->NewObjectArray(size, stringClass,nullptr); 

  for (int i = 0; i < size; i++) {
    env->SetObjectArrayElement(result, i, env->NewStringUTF(list[i].c_str()));
  }

  return result ;
}

