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
#include <stdio.h>
#include <string>
#include <iostream> 
#include <string.h> 
#include <algorithm> 
#include <cctype>
#include <locale>
#include <fstream>
#include <blkid/blkid.h>
#include <mntent.h>
#include <cstring>
#include <sstream>
#include <optional>
#include <experimental/filesystem>

namespace fs = std::experimental::filesystem; 

struct blkinfo {
  std::string uuid;
  std::string label; 
  std::string type; 
};

struct swapinfo {
   long size ;
   long used;
   swapinfo() : size(0), used(0) {}
};

void throwException(JNIEnv *env, std::string err) {
    jclass jexception = env->FindClass("ir/moke/jsysbox/JSysboxException");
    env->ThrowNew(jexception, err.data());
}

char *ltrim(char *s)
{
    while(isspace(*s)) s++;
    return s;
}

char *rtrim(char *s)
{
    char* back = s + strlen(s);
    while(isspace(*--back));
    *(back+1) = '\0';
    return s;
}

char *trim(char *s)
{
    return rtrim(ltrim(s)); 
}

bool isFilesystemMounted(const std::string& filesystem) {
    std::ifstream mountsFile("/proc/mounts");
    std::string line;
    while (std::getline(mountsFile, line)) {
        size_t pos = line.find(' ');
        std::string mountedFilesystem = line.substr(0, pos);
        if (mountedFilesystem == filesystem) {
            return true; // Filesystem is mounted
        }
    }
    return false; // Filesystem is not mounted
}

std::string getMountPoint(const std::string& partition) {
    FILE* mountsFile = std::fopen("/proc/mounts", "r");
    if (!mountsFile) {
        perror("Failed to open mounts file.");
        return "";
    }

    struct mntent* entry;
    while ((entry = getmntent(mountsFile))) {
        if (std::strcmp(entry->mnt_fsname, partition.c_str()) == 0) {
            std::string mountPoint(entry->mnt_dir);
            std::fclose(mountsFile);
            return mountPoint;
        }
    }

    std::fclose(mountsFile);

    return "";
}

int get_blk_info(std::string partition, struct blkinfo &info) {
   const char *uuid = NULL;
   const char *label = NULL;
   const char *type = NULL;

   blkid_probe pr = blkid_new_probe_from_filename(partition.c_str());
   if (!pr) {
      perror("Failed to open partition") ;
      return -1;
   }

   if (blkid_do_probe(pr) < 0) {
      perror("Failed to blk probe") ;
      blkid_free_probe(pr);
      return -1;
   }

   blkid_probe_lookup_value(pr, "TYPE", &type, NULL);
   blkid_probe_lookup_value(pr, "LABEL", &label, NULL);
   blkid_probe_lookup_value(pr, "UUID", &uuid, NULL);

   std::string label_string;
   if (label != NULL) {
     label_string = label;
   } else {
     label_string = "";
   }

   std::string type_string;
   if (type != NULL) {
     type_string = type;
   } else {
     type_string = "";
   }


   std::string uuid_string;
   if (uuid != NULL) {
     uuid_string = uuid;
   } else {
     uuid_string = "";
   }

   blkid_free_probe(pr);

   info.type  = type_string ;
   info.label = label_string;
   info.uuid  = uuid_string;
   return 0 ;
}

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

bool isSymbolicLink(const std::string& filePath) {
    return fs::is_symlink(fs::path(filePath));
}

bool isRegularFile(const std::string& filePath) {
    return fs::is_regular_file(fs::path(filePath));
}

std::string getRealPath(const std::string& filePath) {
    fs::path path(filePath);
    try {
        return fs::canonical(path).string();
    } catch (const fs::filesystem_error& e) {
        std::cerr << "Error resolving path: " << e.what() << std::endl;
        return "";
    }
}

std::string extractLastSegment(const std::string& str) {
    size_t lastSlashPos = str.rfind('/');
    if (lastSlashPos != std::string::npos) {
        return str.substr(lastSlashPos + 1);
    } else {
        // If no '/' is found, return the entire string
        return str;
    }
}


long get_filesystem_size(const std::string& partition) {
    std::string resolvedPartition = partition;
    if (isSymbolicLink(partition)) {
       resolvedPartition = getRealPath(partition); 
    } 
    std::ifstream partitionFile("/proc/partitions");
    if (partitionFile.is_open()) {
        std::string line;
        std::getline(partitionFile, line);  // Skip the header line
        std::getline(partitionFile, line);  // Skip empty (second) line
        
        std::string lsg = extractLastSegment(resolvedPartition) ;
        while (std::getline(partitionFile, line)) {
            std::string major, minor, blocks, name;
            std::istringstream iss(line);
            iss >> major >> minor >> blocks >> name;            
            if (name == lsg) {
                return stol(blocks) ;
            }
        }
    } else {
        perror("Failed to open partitions file.");
    }
    return -1;
}
