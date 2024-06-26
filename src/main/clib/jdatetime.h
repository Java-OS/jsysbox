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

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class ir_moke_jsysbox_time_JDateTime */

#ifndef _Included_ir_moke_jsysbox_time_JDateTime
#define _Included_ir_moke_jsysbox_time_JDateTime
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     ir_moke_jsysbox_time_JDateTime
 * Method:    setTimezone
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_time_JDateTime_setTimezone
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ir_moke_jsysbox_time_JDateTime
 * Method:    setDateTime
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_time_JDateTime_setDateTime
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     ir_moke_jsysbox_time_JDateTime
 * Method:    getDateTime
 * Signature: ()Ljava/time/ZonedDateTime;
 */
JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_time_JDateTime_getZonedDateTime
  (JNIEnv *, jclass);

/*
 * Class:     ir_moke_jsysbox_time_JDateTime
 * Method:    syncSystemToHardware
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_time_JDateTime_syncSystemToHardware
  (JNIEnv *, jclass);

/*
 * Class:     ir_moke_jsysbox_time_JDateTime
 * Method:    syncHardwareToSystem
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_time_JDateTime_syncHardwareToSystem
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
