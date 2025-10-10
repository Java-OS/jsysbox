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

#include "jdisk_manager.h"

#include <blkid/blkid.h>
#include <errno.h>
#include <jni.h>
#include <libfdisk/libfdisk.h>
#include <libudev.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mount.h>
#include <sys/statvfs.h>
#include <sys/swap.h>
#include <unistd.h>

#include <cstddef>
#include <iostream>

#include "common.cpp"

blkid_probe get_blk_probe(JNIEnv *env, const char *path) {
    if (path && strncmp(path, "/dev/", strlen("/dev/")) == 0) {
        blkid_probe pr = blkid_new_probe_from_filename(path);
        if (!pr) {
            blkid_free_probe(pr);
            throwException(env, "Failed to open partition");
            return NULL;
        }
        if (blkid_do_probe(pr) < 0) {
            blkid_free_probe(pr);
            throwException(env, "Failed to blk probe");
            return NULL;
        }
        return pr;
    }
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_mount(JNIEnv *env, jclass clazz, jstring src, jstring dst, jstring file_system_type, jint jflags, jstring options) {
    const char *src_path = env->GetStringUTFChars(src, 0);
    const char *dst_path = env->GetStringUTFChars(dst, 0);
    const char *fs_type = env->GetStringUTFChars(file_system_type, 0);
    const char *mnt_opt = options != NULL ? env->GetStringUTFChars(options, 0) : NULL;
    int flags = (int)jflags;
    int r = mount(src_path, dst_path, fs_type, flags, mnt_opt);
    env->ReleaseStringUTFChars(src, src_path);
    env->ReleaseStringUTFChars(dst, dst_path);
    env->ReleaseStringUTFChars(file_system_type, fs_type);
    env->ReleaseStringUTFChars(options, mnt_opt);

    return r == 0;
}

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_umount(JNIEnv *env, jclass clazz, jstring target) {
    const char *target_path = env->GetStringUTFChars(target, 0);
    int r = umount(target_path);
    env->ReleaseStringUTFChars(target, target_path);
    return r == 0;
}

JNIEXPORT jobjectArray JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_disks(JNIEnv *env, jclass clazz) {
    struct udev *udev = udev_new();
    if (!udev) {
        throwException(env, "Filed to create udev object");
        return NULL;
    }

    struct udev_enumerate *enumerate = udev_enumerate_new(udev);
    udev_enumerate_add_match_subsystem(enumerate, "block");
    udev_enumerate_add_match_property(enumerate, "DEVTYPE", "disk");
    udev_enumerate_scan_devices(enumerate);

    struct udev_list_entry *devices = udev_enumerate_get_list_entry(enumerate);
    struct udev_list_entry *entry;

    std::vector<std::string> list;
    udev_list_entry_foreach(entry, devices) {
        const char *path = udev_list_entry_get_name(entry);
        struct udev_device *device = udev_device_new_from_syspath(udev, path);

        const char *devNode = udev_device_get_devnode(device);
        std::string str(devNode);
        list.push_back(str);
        udev_device_unref(device);
    }

    udev_enumerate_unref(enumerate);
    udev_unref(udev);

    auto size = static_cast<jsize>(list.size());
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(size, stringClass, nullptr);

    for (int i = 0; i < size; i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(list[i].c_str()));
    }

    return result;
}

JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionType(JNIEnv *env, jclass clazz, jstring blkDisk, jint partition_number) {
    const char *device = env->GetStringUTFChars(blkDisk, 0);
    struct fdisk_context *cxt = NULL;
    struct fdisk_partition *pa = NULL;
    struct fdisk_table *table = NULL;
    int rc = 0;
    std::string result;

    auto cleanup = [&]() {
        if (table) fdisk_unref_table(table);
        if (pa) fdisk_unref_partition(pa);
        if (cxt) {
            fdisk_deassign_device(cxt, 1);
            fdisk_unref_context(cxt);
        }
        env->ReleaseStringUTFChars(blkDisk, device);
    };

    cxt = fdisk_new_context();
    if (!cxt) {
        throwException(env, "Failed to create fdisk context");
        cleanup();
        return env->NewStringUTF("error");
    }

    rc = fdisk_assign_device(cxt, device, 0);
    if (rc < 0) {
        std::string errMsg = "Failed to assign device " + std::string(device) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return env->NewStringUTF("error");
    }

    rc = fdisk_get_partitions(cxt, &table);
    if (rc < 0 || !table) {
        throwException(env, "Failed to get partition table");
        cleanup();
        return env->NewStringUTF("error");
    }

    pa = fdisk_table_get_partition(table, partition_number - 1);  // 0-based index
    if (!pa) {
        std::string errMsg = "Partition number " + std::to_string(partition_number) + " not found";
        throwException(env, errMsg);
        cleanup();
        return env->NewStringUTF("error");
    }

    jobject pt = NULL;
    jclass enumClass = env->FindClass("Lir/moke/jsysbox/disk/PartitionType;");

    if (fdisk_partition_is_container(pa)) {
        jfieldID fieldId = env->GetStaticFieldID(enumClass, "EXTENDED", "Lir/moke/jsysbox/disk/PartitionType;");
        pt = env->GetStaticObjectField(enumClass, fieldId);
    } else if (partition_number > 4) {
        jfieldID fieldId = env->GetStaticFieldID(enumClass, "LOGICAL", "Lir/moke/jsysbox/disk/PartitionType;");
        pt = env->GetStaticObjectField(enumClass, fieldId);
    } else {
        jfieldID fieldId = env->GetStaticFieldID(enumClass, "PRIMARY", "Lir/moke/jsysbox/disk/PartitionType;");
        pt = env->GetStaticObjectField(enumClass, fieldId);
    }

    cleanup();
    return pt;
}

JNIEXPORT jstring JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionUUID(JNIEnv *env, jclass clazz, jstring jblk_partition) {
    const char *path = env->GetStringUTFChars(jblk_partition, 0);
    blkid_probe pr = get_blk_probe(env, path);
    if (!pr) return NULL;

    const char *value;
    jstring result = NULL;
    if (blkid_probe_lookup_value(pr, "UUID", &value, NULL) == 0) {
        jclass stringClass = env->FindClass("java/lang/String");
        result = env->NewStringUTF(value);
    }

    blkid_free_probe(pr);
    env->ReleaseStringUTFChars(jblk_partition, path);
    return result;
}

JNIEXPORT jstring JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionLabel(JNIEnv *env, jclass clazz, jstring jblk_partition) {
    const char *path = env->GetStringUTFChars(jblk_partition, 0);
    blkid_probe pr = get_blk_probe(env, path);
    if (!pr) return NULL;

    const char *value;
    jstring result = NULL;
    if (blkid_probe_lookup_value(pr, "LABEL", &value, NULL) == 0) {
        jclass stringClass = env->FindClass("java/lang/String");
        result = env->NewStringUTF(value);
    }

    blkid_free_probe(pr);
    env->ReleaseStringUTFChars(jblk_partition, path);
    return result;
}

JNIEXPORT jlong JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionBlockSize(JNIEnv *env, jclass clazz, jstring jmount_point) {
    const char *mount_point = env->GetStringUTFChars(jmount_point, 0);
    struct statvfs vfs;
    if (statvfs(mount_point, &vfs) == 0) {
        return vfs.f_bsize;
    }

    throwException(env, "Failed to get filesystem statvfs block size");
    return -1;
}

JNIEXPORT jlong JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionAvailableSize(JNIEnv *env, jclass clazz, jstring jmount_point) {
    const char *mount_point = env->GetStringUTFChars(jmount_point, 0);
    struct statvfs vfs;
    if (statvfs(mount_point, &vfs) == 0) {
        return vfs.f_bavail ;
    }

    throwException(env, "Failed to get filesystem statvfs available size");
    return -1;
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_swapOn(JNIEnv *env, jclass clazz, jstring jblkPath) {
    const char *blk = env->GetStringUTFChars(jblkPath, 0);
    int ret = swapon(blk, SWAP_FLAG_PREFER | SWAP_FLAG_DISCARD);
    if (ret != 0) {
        throwException(env, "Failed to swap on");
    }
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_swapOff(JNIEnv *env, jclass clazz, jstring jblkPath) {
    const char *blk = env->GetStringUTFChars(jblkPath, 0);
    int ret = swapoff(blk);
    if (ret != 0) {
        throwException(env, "Failed to swap on");
    }
}

JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionTableType(JNIEnv *env, jclass clazz, jstring jblkPath) {
    const char *device = env->GetStringUTFChars(jblkPath, 0);
    struct fdisk_context *cxt = NULL;
    struct fdisk_label *label = NULL;
    int rc;

    auto cleanup = [&]() {
        if (cxt) {
            fdisk_deassign_device(cxt, 0);
            fdisk_unref_context(cxt);
        }
        env->ReleaseStringUTFChars(jblkPath, device);
    };

    // Initialize libfdisk context
    cxt = fdisk_new_context();
    if (!cxt) {
        std::string errMsg = "Failed to create fdisk context: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return NULL;
    }

    // Assign device in read-only mode
    rc = fdisk_assign_device(cxt, device, 1);  // 1 = read-only mode
    if (rc < 0) {
        std::string errMsg = "Failed to assign device " + std::string(device) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return NULL;
    }

    // Get the current disk label
    label = fdisk_get_label(cxt, NULL);
    if (!label) {
        std::string errMsg = "No partition table found on " + std::string(device);
        throwException(env, errMsg);
        cleanup();
        return NULL;
    }

    // Print the partition table type
    const char *label_name = fdisk_label_get_name(label);

    jobject ptt = NULL;
    jclass enumClass = env->FindClass("Lir/moke/jsysbox/disk/PartitionTable;");

    if (strcmp(label_name, "gpt") == 0) {
        jfieldID fieldId = env->GetStaticFieldID(enumClass, "GPT", "Lir/moke/jsysbox/disk/PartitionTable;");
        ptt = env->GetStaticObjectField(enumClass, fieldId);
    } else if (strcmp(label_name, "dos") == 0) {
        jfieldID fieldId = env->GetStaticFieldID(enumClass, "DOS", "Lir/moke/jsysbox/disk/PartitionTable;");
        ptt = env->GetStaticObjectField(enumClass, fieldId);
    } else {
        throwException(env, "Unknown partition table");
    }

    cleanup();
    return ptt;
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_initializePartitionTable(JNIEnv *env, jclass clazz, jstring jblkPath, jobject enumObj) {
    const char *device = env->GetStringUTFChars(jblkPath, 0);
    struct fdisk_context *cxt = NULL;
    int rc;

    auto cleanup = [&]() {
        if (cxt) {
            fdisk_deassign_device(cxt, 1);
            fdisk_unref_context(cxt);
        }
        env->ReleaseStringUTFChars(jblkPath, device);
    };

    // Initialize libfdisk context
    cxt = fdisk_new_context();
    if (!cxt) {
        std::string errMsg = "Failed to create fdisk context: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Assign device to context
    rc = fdisk_assign_device(cxt, device, 0);  // 0 = read-write mode
    if (rc < 0) {
        std::string errMsg = "Failed to assign device " + std::string(device) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    const char *enumName = getEnumName(env, enumObj);

    rc = fdisk_create_disklabel(cxt, enumName);
    if (rc < 0) {
        std::string errMsg = "Failed to create disk label: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_write_disklabel(cxt);
    if (rc < 0) {
        std::string errMsg = "Failed to write disk label: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Optional: reread partition table so OS updates
    fdisk_reread_partition_table(cxt);
    cleanup();
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_createPartition(JNIEnv *env, jclass clazz, jstring jblkPath, jint partition_number, jlong start_sector, jlong partition_size, jstring jtype_code, jboolean isPrimary) {
    const char *device = env->GetStringUTFChars(jblkPath, 0);
    const char *type_code = env->GetStringUTFChars(jtype_code, 0);

    struct fdisk_context *cxt = NULL;
    struct fdisk_table *table = NULL;
    struct fdisk_partition *pa = NULL;
    struct fdisk_parttype *parttype = NULL;
    const struct fdisk_label *lb = NULL;
    int rc = 0;

    auto cleanup = [&]() {
        if (parttype) fdisk_unref_parttype(parttype);
        if (pa) fdisk_unref_partition(pa);
        if (table) fdisk_unref_table(table);
        if (cxt) {
            fdisk_deassign_device(cxt, 1);
            fdisk_unref_context(cxt);
        }

        env->ReleaseStringUTFChars(jblkPath, device);
    };

    cxt = fdisk_new_context();
    if (!cxt) {
        std::string errMsg = "Failed to create fdisk context: " + std::string(strerror(errno));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_assign_device(cxt, device, 0);  // 0 = RW mode
    if (rc < 0) {
        std::string errMsg = "Failed to assign device " + std::string(device) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Get the current disk label
    lb = fdisk_get_label(cxt, NULL);
    if (!lb) {
        std::string errMsg = "No partition table found on " + std::string(device);
        throwException(env, errMsg);
        cleanup();
    }

    lb = fdisk_get_label(cxt, NULL);
    if (!lb) {
        std::string errMsg = "Failed to get disk label";
        throwException(env, errMsg);
        cleanup();
        return;
    }

    pa = fdisk_new_partition();
    if (!pa) {
        std::string errMsg = "Failed to create partition";
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Set partition number (1)
    fdisk_partition_set_partno(pa, partition_number);  // 0-based index for partition 1

    // Set partition size to entire disk (minus first 2048 sectors for alignment)
    fdisk_sector_t disk_size = fdisk_get_nsectors(cxt);
    if (disk_size <= 2048) {
        std::string errMsg = "Disk too small: " + std::to_string(disk_size) + " sectors";
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Set partition start sector and size
    fdisk_partition_set_start(pa, start_sector);
    if (partition_size <= 0) {
        std::string errMsg = "Invalid partition size: " + std::to_string(partition_size) + " sectors";
        throwException(env, errMsg);
        cleanup();
        return;
    }
    fdisk_partition_set_size(pa, partition_size);

    unsigned int hex_code = 0;
    sscanf(type_code, "%x", &hex_code);

    // Create and set partition type
    parttype = fdisk_label_get_parttype_from_code(lb, hex_code);
    if (!parttype) {
        std::string errMsg = "Failed to parse partition type: " + std::to_string(hex_code);
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_partition_set_type(pa, parttype);
    if (rc < 0) {
        std::string errMsg = "Failed to set partition type: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_get_partitions(cxt, &table);
    if (rc < 0) {
        std::string errMsg = "Failed to get partition table: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_table_add_partition(table, pa);
    if (rc < 0) {
        std::string errMsg = "Failed to add partition: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_apply_table(cxt, table);
    if (rc < 0) {
        std::string errMsg = "Failed to apply partition table: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Write the updated partition table to disk
    rc = fdisk_write_disklabel(cxt);
    if (rc < 0) {
        std::string errMsg = "Failed to write updated disk label: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Optional: reread partition table so OS updates
    fdisk_reread_partition_table(cxt);
    cleanup();
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_deletePartition(JNIEnv *env, jclass clazz, jstring jblkPath, jint partition_number) {
    const char *device = env->GetStringUTFChars(jblkPath, 0);
    struct fdisk_context *cxt = NULL;
    struct fdisk_table *table = NULL;
    int rc = 0;

    auto cleanup = [&]() {
        if (table) fdisk_unref_table(table);
        if (cxt) {
            fdisk_deassign_device(cxt, 1);
            fdisk_unref_context(cxt);
        }

        env->ReleaseStringUTFChars(jblkPath, device);
    };

    cxt = fdisk_new_context();
    if (!cxt) {
        throwException(env, "Failed to create fdisk context");
        cleanup();
        return;
    }

    // Open device in read/write mode
    rc = fdisk_assign_device(cxt, device, 0);
    if (rc < 0) {
        std::string errMsg = "Failed to assign device " + std::string(device) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
    }

    // Load current partition table
    rc = fdisk_get_partitions(cxt, &table);
    if (rc < 0 || !table) {
        throwException(env, "Failed to get partition table");
        cleanup();
    }

    // Delete the partition (partition_number is usually 1-based)
    rc = fdisk_delete_partition(cxt, partition_number);
    if (rc < 0) {
        std::string errMsg = "Failed to delete partition " + std::to_string(partition_number) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
    }

    // Apply and write changes
    rc = fdisk_apply_table(cxt, table);
    if (rc < 0) {
        std::string errMsg = "Failed to apply table: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
    }

    rc = fdisk_write_disklabel(cxt);
    if (rc < 0) {
        std::string errMsg = "Failed to write updated disk label: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
    }

    // Optional: reread partition table so OS updates
    fdisk_reread_partition_table(cxt);
    cleanup();
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_bootable(JNIEnv *env, jclass clazz, jstring jblkPath, jint partition_number) {
    const char *device = env->GetStringUTFChars(jblkPath, 0);
    struct fdisk_context *cxt = NULL;
    struct fdisk_partition *pa = NULL;
    struct fdisk_table *table = NULL;
    int rc = 0;

    auto cleanup = [&]() {
        if (table) fdisk_unref_table(table);
        if (pa) fdisk_unref_partition(pa);
        if (cxt) {
            fdisk_deassign_device(cxt, 1);
            fdisk_unref_context(cxt);
        }

        env->ReleaseStringUTFChars(jblkPath, device);
    };

    cxt = fdisk_new_context();
    if (!cxt) {
        throwException(env, "Failed to create fdisk context");
        return;
    }

    rc = fdisk_assign_device(cxt, device, 0);
    if (rc < 0) {
        std::string errMsg = "Failed to assign device " + std::string(device) + ": " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Load current partition table
    rc = fdisk_get_partitions(cxt, &table);
    if (rc < 0 || !table) {
        throwException(env, "Failed to get partition table");
        cleanup();
        return;
    }

    // Get the target partition
    pa = fdisk_table_get_partition(table, partition_number - 1);  // 0-based index
    if (!pa) {
        std::string errMsg = "Partition number " + std::to_string(partition_number) + " not found";
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Set or clear bootable flag
    rc = fdisk_toggle_partition_flag(cxt, partition_number, FDISK_FIELD_BOOT);
    if (rc < 0) {
        std::string errMsg = "Failed to set bootable flag: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Apply and write the new table
    rc = fdisk_apply_table(cxt, table);
    if (rc < 0) {
        std::string errMsg = "Failed to apply updated table: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    rc = fdisk_write_disklabel(cxt);
    if (rc < 0) {
        std::string errMsg = "Failed to write updated disk label: " + std::string(strerror(-rc));
        throwException(env, errMsg);
        cleanup();
        return;
    }

    // Optional: reread partition table so OS updates
    fdisk_reread_partition_table(cxt);
    cleanup();
}

JNIEXPORT jint JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_partitionCount(JNIEnv *env, jclass clazz, jstring jblkPath) {
    const char *device = env->GetStringUTFChars(jblkPath, 0);

    struct fdisk_context *cxt = NULL;
    struct fdisk_table *table = NULL;
    int rc = 0;
    size_t num_partitions = 0;

    auto cleanup = [&]() {
        if (table) fdisk_unref_table(table);

        if (cxt) {
            fdisk_deassign_device(cxt, 1);
            fdisk_unref_context(cxt);
        }

        env->ReleaseStringUTFChars(jblkPath, device);
    };

    cxt = fdisk_new_context();
    if (!cxt) {
        std::string errMsg = "Failed to create fdisk context: " + std::string(strerror(errno));
        throwException(env, errMsg);
        cleanup();
        return -1;
    }

    rc = fdisk_assign_device(cxt, device, 1);  // open read-only
    if (rc < 0) {
        std::string errMsg = "Failed to open device: " + std::string(strerror(errno));
        throwException(env, errMsg);
        cleanup();
        return -1;
    }

    rc = fdisk_get_partitions(cxt, &table);
    if (rc < 0) {
        std::string errMsg = "Failed to get partition table";
        throwException(env, errMsg);
        cleanup();
        return -1;
    }

    num_partitions = fdisk_table_get_nents(table);
    cleanup();
    return (int)num_partitions;
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_JDiskManager_sync(JNIEnv *env, jclass clazz) { sync(); }
