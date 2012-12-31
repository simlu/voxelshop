#! /usr/bin/env python

import os
import subprocess

# java
subprocess.call('compiler\protoc -I="." --java_out="../fols/src" "gen_msgs.proto"', shell=True);

# c++
cpp_dir = '../foc/src/network'
subprocess.call('compiler\protoc -I="." --cpp_out="' + cpp_dir+ '" "gen_msgs.proto"', shell=True);
try:
    os.remove('../foc/src/network/gen_msgs.pb.cpp');
except WindowsError:
    pass
os.rename(cpp_dir + '/gen_msgs.pb.cc', cpp_dir + '/gen_msgs.pb.cpp');
