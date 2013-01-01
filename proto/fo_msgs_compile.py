#! /usr/bin/env python

import os
import subprocess

# java
subprocess.call('compiler\protoc -I="." --java_out="../fos/src" "fo_msgs.proto"', shell=True);
subprocess.call('compiler\protoc -I="." --java_out="../fols/src" "fo_msgs.proto"', shell=True);

# c++
cpp_dir = '../foc/src/network'
subprocess.call('compiler\protoc -I="." --cpp_out="' + cpp_dir+ '" "fo_msgs.proto"', shell=True);
try:
    os.remove('../foc/src/network/fo_msgs.pb.cpp')
except WindowsError:
    pass
os.rename(cpp_dir + '/fo_msgs.pb.cc', cpp_dir + '/fo_msgs.pb.cpp');
