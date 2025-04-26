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
#include <stdlib.h>
#include <nftables/libnftables.h>

#include "common.cpp"
#include "jfirewall.h"

JNIEXPORT void JNICALL Java_ir_moke_jsysbox_firewall_JFirewall_restore (JNIEnv *env, jclass clazz, jstring jfilePath) {
  struct nft_ctx *ctx;
  int err;

  ctx = nft_ctx_new(0);
  if (!ctx) {
    throwException(env, "Cannot allocate nft context");
  	return ;
  }

  nft_ctx_output_set_flags(ctx, NFT_CTX_OUTPUT_JSON);

  /* create ruleset: all commands in the buffer are atomically applied */
  const char *filePath = env->GetStringUTFChars(jfilePath, 0);
  err = nft_run_cmd_from_filename(ctx, filePath);
  if (err < 0) {
    throwException(env, "failed to apply rules");
    return ;
  }

  env->ReleaseStringUTFChars(jfilePath, filePath);
	nft_ctx_free(ctx);
}

JNIEXPORT jstring JNICALL Java_ir_moke_jsysbox_firewall_JFirewall_exec (JNIEnv *env, jclass clazz, jstring jcmd) {
  struct nft_ctx *ctx;
  int err;
  const char *output;

  ctx = nft_ctx_new(0);
  if (!ctx) {
    throwException(env, "Cannot allocate nft context");
    return NULL;
  }
 
  nft_ctx_output_set_flags(ctx, NFT_CTX_OUTPUT_HANDLE | NFT_CTX_OUTPUT_JSON);
  nft_ctx_buffer_output(ctx);

  const char *cmd = env->GetStringUTFChars(jcmd, 0);
  err = nft_run_cmd_from_buffer(ctx, cmd);
  if (err < 0) {
    std::string err("Failed to execute firewall statement: [");
    err.append(cmd);
    err.append("]");
    throwException(env,err);
    return NULL ;
  }

  output = nft_ctx_get_output_buffer(ctx); 
  if (output == NULL) return NULL ;

  env->ReleaseStringUTFChars(jcmd, cmd);

  if (!output) {
    nft_ctx_free(ctx);
    return NULL;
  } else {
    jstring result = env -> NewStringUTF(output);
    nft_ctx_free(ctx);
    return result ;
  }
}
