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
#include <magic.h>
#include <iostream>
#include "common.cpp"
#include "jfile.h"

JNIEXPORT jstring JNICALL Java_ir_moke_jsysbox_file_JFile_mime (JNIEnv *env, jclass clazz, jstring jFilePath) {
  const char *filePath = env->GetStringUTFChars(jFilePath, 0);
  magic_t magic = magic_open(MAGIC_MIME_TYPE);
  if (magic == NULL) {
    throwException(env,"Failed to initialize libmagic");
    return NULL ;
  }

  if (magic_load(magic,"/usr/share/magic.mgc") != 0) {
    magic_close(magic);
    throwException(env,"Failed to load /usr/share/magic.mgc");
    return NULL;
  }

  const char* mimeType = magic_file(magic, filePath);
  jstring result = env -> NewStringUTF(mimeType);
  magic_close(magic);
  env->ReleaseStringUTFChars(jFilePath, filePath);
  return result;
}