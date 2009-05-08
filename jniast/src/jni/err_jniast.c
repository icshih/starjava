/*
*+
*  Name:
*     err_jniast.c

*  Purpose:
*     Error message module for AST called from JNI.

*  Language:
*     ANSI C.

*  Description:
*     This module provides the astPutErr function which is called by
*     routines in the AST library when an error is detected.
*     It simply concatenates any messages registered by this routine
*     and provides functions by which the error can be retrieved.
*
*     Note that this, like the error-handling mechanism of the AST C 
*     library itself, assumes that only one thread is executing AST
*     code at any one time.  Care should therefore be taken with 
*     synchronisation of the methods which call astPutErr.

*  Authors:
*     MBT: Mark Taylor (Starlink)

*  History:
*     18-SEP-2001 (MBT):
*        Original version.
*-
*/

/* Header files. */
#include "err_jniast.h"
#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include "jni.h"

/* Constants. */
#define BUFLENG 1024

/* Typedefs. */
typedef struct {
   int msgleng;
   char buffer[ BUFLENG ];
} errInfo;

/* Static variables. */
static pthread_key_t errInfo_key;

/* Static functions. */
static errInfo *getErrInfo();


/* Public function. */
void astPutErr_( int status, const char *message ) {
/*
*  Name:
*     astPutErr

*  Purpose:
*     Deliver an error message.

*  Type:
*     Protected function.

*  Synopsis:
*     #include "err.h"
*     void astPutErr( int status, const char *message )

*  Description:
*     This function registers an error message by storing it in a buffer,
*     appended to any others which have been written since the last call
*     to jniastClearErrMsg().  The current value of the buffer can be
*     got by calling jniastGetErrMsg().  The buffer is of fixed length;
*     any messages too long to fit in it are silently discarded.

*  Parameters:
*     status
*        The error status value.
*     message
*        A pointer to a null-terminated character string containing
*        the error message to be delivered. This should not contain
*        newline characters.
*-
*/
   errInfo *info = getErrInfo();
   if ( info->msgleng < BUFLENG - 2 ) {
      if ( info->msgleng > 0 ) {
         info->buffer[ info->msgleng++ ] = '\n';
      }
      while ( info->msgleng < BUFLENG - 1 && *message != '\0' ) {
         info->buffer[ info->msgleng++ ] = *(message++);
      }
      info->buffer[ info->msgleng ] = '\0';
   }
}


/* Package functions. */

int jniastErrInit( JNIEnv *env ) {
/*
*  Name:
*     jniastErrInit

*  Purpose:
*     Initialise JNIAST error reporting.

*  Description:
*     Must be called before any invocation of astPutErr_().

*  Parameters:
*     env = JNIEnv *
*        Pointer to the JNI interface.

*  Return value:
*     Zero if all is well; non-zero in case of some initialisation error.
*/
   return jniastPthreadKeyCreate( env, &errInfo_key, free );
}

errInfo *getErrInfo() {
/*
*  Name:
*     getErrInfo

*  Purpose:
*     Returns the errInfo structure.

*  Description:
*     Returns the thread-specific errInfo structure for use with the
*     current thread.  Lazily initialises it if required.

*  Return value:
*     Pointer to errInfo structure ready for use.
*/
   void *info;
   info = pthread_getspecific( errInfo_key );
   if ( ! info ) {
      info = calloc( 1, sizeof( errInfo ) );
      pthread_setspecific( errInfo_key, info );
   }
   return (errInfo *) info;
}

void jniastClearErrMsg() {
/*
*+
*  Name:
*     jniastClearErrMsg

*  Purpose:
*     Reset the contents of the JNI/AST error message buffer.

*  Description:
*     This routine should be called at the start of a new AST 'error
*     context', i.e. before a sequence of AST calls is made, to ensure
*     that there are no pending error messages.
*-
*/
   errInfo *info;
   info = getErrInfo();
   info->msgleng = 0;
   *(info->buffer) = '\0';
}

const char *jniastGetErrMsg() {
/*
*+
*  Name:
*     jniastGetErrMsg

*  Purpose:
*     Retrieve the contents of the JNI/AST error message buffer.

*  Description:
*     This routine returns a pointer to a string giving a concatenation
*     of all error messages logged by AST since the last call to 
*     jniastClearErrMsg().

*  Return value:
*     A pointer to a static buffer containing the error message text.
*     Messages generated by separate calls from AST are separated
*     by newline characters.  The string is terminated by a zero,
*     but not by an additional newline.
*-
*/
   return getErrInfo()->buffer;
}
