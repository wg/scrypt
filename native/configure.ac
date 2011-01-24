AC_INIT(scrypt, 1.0, will@glozer.net)
AC_PREREQ(2.61)

AM_INIT_AUTOMAKE(-Wall foreign)

AC_PROG_CC
AM_PROG_LIBTOOL

AC_CHECK_DECLS(be64enc)
AC_CHECK_FUNCS(posix_memalign)
AC_CHECK_HEADER(err.h)

AX_CHECK_JAVA_JNI_H

AC_CONFIG_HEADER(config.h)

AC_OUTPUT(Makefile)
