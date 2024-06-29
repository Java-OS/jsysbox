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

#include <cstddef>
#include <jni.h>
#include <parted/parted.h>
#include <unistd.h>
#include <stdlib.h>
#include <blkid/blkid.h>
#include <mntent.h>
#include <cstring>
#include <experimental/filesystem>
#include <cstring>

namespace fs = std::experimental::filesystem; 

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

char* getEnumName(JNIEnv *env, jobject enumObj) {
    jclass enumClass     = env->GetObjectClass(enumObj);
    jmethodID nameMethod = env->GetMethodID(enumClass, "name", "()Ljava/lang/String;");
    jstring nameString   = (jstring)(env)->CallObjectMethod(enumObj, nameMethod);
    const char* name = env->GetStringUTFChars(nameString,0);

    char *mutableName = strdup(name);
    env->ReleaseStringUTFChars(nameString, name);

    return mutableName ;
}

void toLowerCase(char *str) {
    for (int i = 0; str[i]; i++) {
        str[i] = tolower(str[i]);
    }
}
