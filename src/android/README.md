To build and use native library on android, do the following:

### Build

1. download and install Android NDK [1]

1. check (and potentially edit) the target platform defined in
   src/android/jni/Android.mk as TARGET_PLATFORM [2]

1. check (and potentially edit) the target architectures defined in
   src/android/jni/Application.mk as APP_ABI [3]

1. make sure your current directory is src/android/jni

1. run `ndk-build`

1. if you want to clean the build, run `ndk-build clean`

1. the result native libraries for the platforms/architectures you
   chose would be resulted in src/android/libs

### Useage

1. since the android build tool chain will reject to include a jar
   file with native library that no android platform supports and the
   official scrypt build include such libraries, you need to strip
   those libraries (and potentially the signature) from the scrypt jar
   you are going to use in the android project.

1. in the android project (following eclipse adt directory structure
   conventions) you want to use scrypt with native libraries, make
   sure there's a folder called libs under the project root.

1. copy the striped scrypt jar file to the libs folder of your android
   project.

1. copy the subfolders of src/android/libs you just built to the libs
   folder of your android project.

1. build and package your android application and it should be using
   one of the native scrypt libraries matching the device's
   architecture.

Hint: you can always make sure you're calling the native version of
scrypt instead of the java version by specifically use
`com.lambdaworks.crypto.SCrypt.scryptN(..)`
instead of
`com.lambdaworks.crypto.SCrypt.scrypt(..)`

[1]: https://developer.android.com/tools/sdk/ndk/index.html
[2]: https://developer.android.com/ndk/guides/android_mk.html
[3]: https://developer.android.com/ndk/guides/application_mk.html

- - -

Haochen Xie
