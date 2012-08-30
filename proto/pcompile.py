#! /usr/bin/env python

import os
import subprocess

# java
subprocess.call('protoc -I="." --java_out="../fos/src" "fantasy_messages.proto"', shell=True);

# c++
cpp_dir = '../foc/src/network'
subprocess.call('protoc -I="." --cpp_out="' + cpp_dir+ '" "fantasy_messages.proto"', shell=True);
os.remove('../foc/src/network/fantasy_messages.pb.cpp')
os.rename(cpp_dir + '/fantasy_messages.pb.cc', cpp_dir + '/fantasy_messages.pb.cpp');
