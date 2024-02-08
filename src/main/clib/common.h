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
#include <string>
#include <iostream> 
#include <string.h> 
#include <algorithm> 
#include <cctype>
#include <locale>


inline void throwException(JNIEnv *env, std::string err) {
    jclass jexception = env->FindClass("ir/moke/jsysbox/JSysboxException");
    env->ThrowNew(jexception, err.data());
}

inline char *ltrim(char *s)
{
    while(isspace(*s)) s++;
    return s;
}

inline char *rtrim(char *s)
{
    char* back = s + strlen(s);
    while(isspace(*--back));
    *(back+1) = '\0';
    return s;
}

inline char *trim(char *s)
{
    return rtrim(ltrim(s)); 
}
