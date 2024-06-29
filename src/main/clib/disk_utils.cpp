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
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <blkid/blkid.h>
#include <mntent.h>
#include <cstring>
#include <fstream>
#include "common.cpp"
#include <iostream>

struct PartitionInfo {
    std::string blk ;
    std::string mountPoint;
    std::string uuid;
    std::string label;
    std::string type;
    long long totalSize;
    long long freeSize;
    PedSector startSector;
    PedSector endSector;
    PedSector sectorSize;
};

struct swapinfo {
   long size ;
   long used;
   swapinfo() : size(0), used(0) {}
};


int get_swap_info(const std::string& partitionPath, struct swapinfo &info) {
    std::ifstream swapFile("/proc/swaps");
    if (swapFile.is_open()) {
        std::string line;
        std::getline(swapFile, line);  // Skip the header line
        
        while (std::getline(swapFile, line)) {
            std::string device, type, size, used;
            std::istringstream iss(line);
            iss >> device >> type >> size >> used;
            
            if (device == partitionPath) {
                info.size = stol(size); 
                info.used = stol(used);
                return 0 ;
            }
        }
    } else {
        perror("Failed to open swap file.");
    }
    return -1;
}

std::string getMountPoint(const char* device) {
    std::ifstream mounts("/proc/mounts");
    if (!mounts.is_open()) return "";

    std::string line;
    while (std::getline(mounts, line)) {
        std::istringstream iss(line);
        std::string dev, mount, type, opts;
        int freq, passno;
        if (iss >> dev >> mount >> type >> opts >> freq >> passno) {
            if (dev == device) {
                return mount;
            }
        }
    }
    return "";
}

PartitionInfo getPartitionInfo(JNIEnv *env, PedPartition* part, PedDevice* dev) {
    PartitionInfo info = {};
    info.blk = ped_partition_get_path(part);
    info.startSector = part->geom.start;
    info.endSector = part->geom.end;
    info.sectorSize = part->geom.length;
    info.totalSize = part->geom.length * dev->sector_size;

    char* path = ped_partition_get_path(part);
    if (path && strncmp(path, "/dev/", strlen("/dev/")) == 0) {
        info.mountPoint = getMountPoint(path);
        blkid_probe pr = blkid_new_probe_from_filename(path);
        if (!pr) {
          blkid_free_probe(pr);
          throwException(env, "Failed to open partition") ;
        }

        if (blkid_do_probe(pr) < 0) {
          blkid_free_probe(pr);
          throwException(env, "Failed to blk probe") ;
        }

        if (pr) {
            const char* value;
            if (blkid_probe_lookup_value(pr, "UUID", &value, NULL) == 0) {
                info.uuid = value;
            }
            if (blkid_probe_lookup_value(pr, "LABEL", &value, NULL) == 0) {
                info.label = value;
            }
            if (blkid_probe_lookup_value(pr, "TYPE", &value, NULL) == 0) {
                info.type = value;
            }
            blkid_free_probe(pr);
        }

        if (!info.mountPoint.empty()) {
            struct statvfs vfs;
            if (statvfs(info.mountPoint.c_str(), &vfs) == 0) {
                info.freeSize = (vfs.f_bavail * vfs.f_bsize) / 1024;
            }
        }
        free(path);
    }
    return info;
}

PedDevice* getBlockDevice(JNIEnv *env, jstring jblkPath) {
  const char* blk = env->GetStringUTFChars(jblkPath,0); 

  PedDevice* dev = ped_device_get(blk); 
  if (!dev) {
    return NULL;
  }

  if (!ped_device_open(dev)) {
    env->ReleaseStringUTFChars(jblkPath, blk);
    ped_device_destroy(dev);
    return NULL;
  }

  env->ReleaseStringUTFChars(jblkPath, blk);
  return dev ;
}

void close(PedDevice* dev, PedDisk* disk, PedPartition* part) {
  if (part) {
    ped_partition_destroy(part);
  }

  if (disk) {
    ped_disk_destroy(disk);
  }

  if (dev) {
    ped_device_close(dev);
    ped_device_destroy(dev);
  }
}
