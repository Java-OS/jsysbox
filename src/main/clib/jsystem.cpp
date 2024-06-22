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

#include <iostream>
#include <stdio.h>
#include <unistd.h>
#include <sys/reboot.h>
#include <stdlib.h>
#include <string.h>
#include <list>
#include <vector>
#include "common.cpp"
#include <fstream>
#include <sstream>
#include <sys/types.h>
#include <signal.h>
#include <libkmod.h>

extern char **environ;
int hostname_max_size = 64 ;

using namespace std;

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_reboot () {
    sync() ;
    reboot(RB_AUTOBOOT);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_shutdown () {
    sync() ;
    reboot(RB_POWER_OFF);
}

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_system_JSystem_chroot (JNIEnv *env, jclass, jstring jtarget) {
  if (jtarget == NULL || jtarget == NULL) return false; 
  const char *target = env->GetStringUTFChars(jtarget,0); 
  env->ReleaseStringUTFChars(jtarget,target);
  return chroot(target) == 0; 
}

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_system_JSystem_setEnv (JNIEnv *env, jclass clazz, jstring jkey, jstring jvalue) {
    if (jkey == NULL || jvalue == NULL) return false;
    const char *key = env->GetStringUTFChars(jkey,0);
    const char *value = env->GetStringUTFChars(jvalue,0);
    env->ReleaseStringUTFChars(jkey,key);
    env->ReleaseStringUTFChars(jvalue,value);
    int r = setenv(key, value, 1);
    if (r != 0)perror("setenv");
    return r == 0;
}

JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_system_JSystem_unSetEnv (JNIEnv *env, jclass clazz, jstring jkey) {
    const char *key = env->GetStringUTFChars(jkey,0);
    env->ReleaseStringUTFChars(jkey,key);
    int r = unsetenv(key);
    if (r != 0)perror("unsetenv");
    return r == 0;
}

JNIEXPORT jstring JNICALL Java_ir_moke_jsysbox_system_JSystem_getEnv (JNIEnv *env, jclass clazz, jstring jkey) {
    const char *key = env->GetStringUTFChars(jkey,0);
    env->ReleaseStringUTFChars(jkey,key);
    char *value = getenv(key);
    return env -> NewStringUTF(value);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_setHostname (JNIEnv *env, jclass clazz, jstring jkey) {
    int r ;
    const char *key = env->GetStringUTFChars(jkey,0);
    int size = strlen(key);
    if (size > hostname_max_size) {
                env->ReleaseStringUTFChars(jkey,key);
                const char *err = "name too long, max 64 character" ;
                throwException(env,err);
    }
    r = sethostname(key,size);
    env->ReleaseStringUTFChars(jkey,key);
    if (r != 0) perror("sethostname");
}

JNIEXPORT jstring JNICALL Java_ir_moke_jsysbox_system_JSystem_getHostname (JNIEnv *env, jclass clazz) {
    int r ;
    char value[hostname_max_size];
    r = gethostname(value,hostname_max_size);
    if (r != 0) perror("gethostname");

    return env->NewStringUTF(value);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_kill (JNIEnv *env, jclass, jlong pid, jlong signal) {
   kill(pid,signal);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_insmod(JNIEnv *env, jclass clazz, jstring moduleName, jobjectArray parameters) {
  const char *n = env->GetStringUTFChars(moduleName,0);
  struct kmod_ctx *ctx;
	struct kmod_module *mod;
	const char *null_config = NULL;
	int err;

	ctx = kmod_new(NULL, &null_config);
	if (ctx == NULL)
		exit(EXIT_FAILURE);

	err = kmod_module_new_from_name(ctx, n, &mod);
	if (err != 0) {
	    std::string errMsg = "Module does not exists: " + std::string(n);
	    throwException(env, errMsg);
	}

  // Convert the Java parameters array to a C array of strings
  std::stringstream paramStream; 

  if (parameters != NULL) {
      int paramCount = env->GetArrayLength(parameters);
      for (int i = 0; i < paramCount; i++) {
          jstring param = (jstring) env->GetObjectArrayElement(parameters, i);
          const char *paramStr = env->GetStringUTFChars(param, 0);
          paramStream << paramStr;
          if (i < paramCount - 1) {
                paramStream << " ";
          }
          env->ReleaseStringUTFChars(param, paramStr);
      }
  }

  std::string paramStr = paramStream.str();
  const char *params = paramStr.empty() ? NULL : paramStr.c_str();
  err = kmod_module_insert_module(mod, 0, params);
	if (err != 0) {
	    std::string errMsg = "Could not insert module: " + std::string(n);
    	throwException(env, errMsg);
	}
	kmod_unref(ctx);
  env->ReleaseStringUTFChars(moduleName, n);
}

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_system_JSystem_rmmod(JNIEnv *env, jclass clazz, jstring moduleName) {
  const char *n = env->GetStringUTFChars(moduleName,0);
  struct kmod_ctx *ctx;
	struct kmod_module *mod;
	const char *null_config = NULL;
	int err;

	ctx = kmod_new(NULL, &null_config);
	if (ctx == NULL)
		exit(EXIT_FAILURE);

	err = kmod_module_new_from_name(ctx, n, &mod);
	if (err != 0) {
	    std::string errMsg = "Module does not exists: " + std::string(n);
	    throwException(env, errMsg);
	}

	err = kmod_module_remove_module(mod, 0);
	if (err != 0) {
	    std::string errMsg = "Could not remove module: " + std::string(n);
    	throwException(env, errMsg);
	}
	kmod_unref(ctx);
  env->ReleaseStringUTFChars(moduleName, n);
}

JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_system_JSystem_modinfo(JNIEnv *env, jclass clazz, jstring moduleName) {
  const char *n = env->GetStringUTFChars(moduleName,0);
  struct kmod_ctx *ctx;
	struct kmod_module *mod_simple ;
	const char *null_config = NULL;
	int err;
	struct kmod_list *l, *list = NULL;

	ctx = kmod_new(NULL, &null_config);
	if (ctx == NULL)
		exit(EXIT_FAILURE);

	err = kmod_module_new_from_name(ctx, n, &mod_simple);
	if (err != 0) {
	  std::string errMsg = "Module does not exists: " + std::string(n);
	  throwException(env, errMsg);
	}

  err = kmod_module_get_info(mod_simple, &list); 
  if (err < 0) {
    std::string errMsg = "Could not get module information: " + std::string(n);
    throwException(env, errMsg);
	}

  jclass mapClass   = env->FindClass("java/util/HashMap");
  jmethodID mapInit = env->GetMethodID(mapClass, "<init>", "()V");
  jobject hashMap   = env->NewObject(mapClass, mapInit);

  jmethodID mapPut  = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
 
  kmod_list_foreach(l, list) {
		const char *key = kmod_module_info_get_key(l);
		const char *value = kmod_module_info_get_value(l);

    jstring key1   = env->NewStringUTF(key);
    jstring value1 = env->NewStringUTF(value);
    env->CallObjectMethod(hashMap, mapPut, key1, value1);
	} 

  return hashMap;
}


