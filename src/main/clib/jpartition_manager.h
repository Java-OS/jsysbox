/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class ir_moke_jsysbox_disk_PartitionManager */

#ifndef _Included_ir_moke_jsysbox_disk_PartitionManager
#define _Included_ir_moke_jsysbox_disk_PartitionManager
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    mount
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_mount
  (JNIEnv *, jclass, jstring, jstring, jstring, jint, jstring);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    umount
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_umount
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    getFilesystemStatistics
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_getDisks
  (JNIEnv *, jclass);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    getFilesystemStatistics
 * Signature: (Ljava/lang/String;)Lir/moke/jsysbox/disk/HDDPartition;
 */
JNIEXPORT jobjectArray JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_getPartitionInformation
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    swapOn
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_swapOn
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    swapOff
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_swapOff
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    partitionTableType
 * Signature: (Ljava/lang/String;)Lir/moke/jsysbox/disk/PartitionTable;
 */
JNIEXPORT jobject JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_partitionTableType
  (JNIEnv *, jclass, jstring);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    initializePartitionTable
 * Signature: (Ljava/lang/String;Lir/moke/jsysbox/disk/PartitionTable;)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_initializePartitionTable
  (JNIEnv *, jclass, jstring, jobject);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    createPartition
 * Signature: (Ljava/lang/String;JJLir/moke/jsysbox/disk/FilesystemType;)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_createPartition
  (JNIEnv *, jclass, jstring, jlong, jlong, jobject);

/*
 * Class:     ir_moke_jsysbox_disk_PartitionManager
 * Method:    deletePartition
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_ir_moke_jsysbox_disk_PartitionManager_deletePartition
  (JNIEnv *, jclass, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif
