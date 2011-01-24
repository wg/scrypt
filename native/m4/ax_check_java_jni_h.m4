# SYNOPSIS
#
#   AX_CHECK_JAVA_JNI_H
#
# DESCRIPTION
#
#   Check for Sun Java JDK JNI header file (jni.h). Checks JAVA_HOME,
#   the parent directory of the 'java' executable if it is in PATH,
#   and then a few OS-specific locations.
#
# LICENSE
#
#   Copyright (c) 2011 Will Glozer <will@glozer.net>
#
#   This program is free software; you can redistribute it and/or modify it
#   under the terms of the GNU General Public License as published by the
#   Free Software Foundation; either version 2 of the License, or (at your
#   option) any later version.
#
#   This program is distributed in the hope that it will be useful, but
#   WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
#   Public License for more details.
#
#   You should have received a copy of the GNU General Public License along
#   with this program. If not, see <http://www.gnu.org/licenses/>.
#
#   As a special exception, the respective Autoconf Macro's copyright owner
#   gives unlimited permission to copy, distribute and modify the configure
#   scripts that are the output of Autoconf when processing the Macro. You
#   need not follow the terms of the GNU General Public License when using
#   or distributing such scripts, even though portions of the text of the
#   Macro appear in them. The GNU General Public License (GPL) does govern
#   all other use of the material that constitutes the Autoconf Macro.
#
#   This special exception to the GPL applies to versions of the Autoconf
#   Macro released by the Autoconf Archive. When you make and distribute a
#   modified version of the Autoconf Macro, you may extend this special
#   exception to the GPL to apply to your modified version as well.

AC_DEFUN([AX_CHECK_JAVA_JNI_H], [
AC_MSG_CHECKING([for Java jni.h])
while true
do
  if test -f "$JAVA_HOME/include/jni.h"; then
    jvm_header_dir="$JAVA_HOME/include/"
    break
  fi

  if test -f `type -P java`; then
     java_dir=`type -P java`
     if test -h $java_dir; then
       java_dir=`readlink $java_dir`;
     fi

     jvm_header_dir=`echo $java_dir | sed "s,\(.*\)/bin/java.*,\1/include/,"`

     test -f "$jvm_header_dir/jni.h" && break
  fi

  mac_osx_dirs="/System/Library/Frameworks/JavaVM.framework/Headers/
/Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Headers/
/Developer/SDKs/MacOSX10.5.sdk/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Headers/"
  for d in $mac_osx_dirs; do
      if test -f "$d/jni.h"; then
        jvm_header_dir=$d
        break
      fi
  done

  test -f "$jvm_header_dir/jni.h" && break

  AC_MSG_ERROR([Could not locate jni.h, set JAVA_HOME])
  break
done

AC_TRY_CPP([#include <jni.h>],,[
  ac_save_CPPFLAGS="$CPPFLAGS"
  CPPFLAGS="$ac_save_CPPFLAGS -I$jvm_header_dir"
  case "$host_os" in
    linux*)   CPPFLAGS="$CPPFLAGS -I$jvm_header_dir/linux";;
    freebsd*) CPPFLAGS="$CPPFLAGS -I$jvm_header_dir/freebsd";;
    solaris*) CPPFLAGS="$CPPFLAGS -I$jvm_header_dir/solaris";;    
  esac
  AC_TRY_CPP([#include <jni.h>],
    ac_save_CPPFLAGS="$CPPFLAGS",
    AC_MSG_ERROR([unable to include <jni.h>]))
  CPPFLAGS="$ac_save_CPPFLAGS"
])

])
