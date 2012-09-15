#!/usr/bin/python

import os

class Mkf:
    @property
    def path(self):
        return self.__path

    @path.setter
    def path(self, value):
        self.__path = value

    @property
    def name(self):
        return self.__name

    @name.setter
    def name(self, value):
        self.__name = value

    @property
    def full_name(self):
        return os.path.join(self.path, self.name)

    @property
    def source_files(self):
        return self.__source_files

    @property
    def subprojects(self):
        return self.__subprojects

    @property
    def defines(self):
        return self.__defines

    @property
    def options(self):
        return self.__options

    @property
    def includepaths(self):
        return self.__includepaths


    def __init__(self):
        self.__path = ""
        self.__name = ""
        self.__source_files = []
        self.__subprojects = []
        self.__module_paths = []
        self.__defines = []
        self.__options = []
        self.__includepaths = []


def dump_mkf_command_list(file, command_name, list):
    if len(list) == 0:
        return

    file.writelines(command_name + "\n")
    file.writelines("{\n")
    
    for i in list:
        file.writelines("    " + i)
        file.writelines("\n")

    file.writelines("}\n\n")

def create_mkf_file(path, mkf):
    mkf_full_name = os.path.join(path, mkf.name)
    print mkf_full_name

    file = open(mkf_full_name, "w")

    dump_mkf_command_list(file, "defines", mkf.defines)
    dump_mkf_command_list(file, "includepaths", mkf.includepaths)
    dump_mkf_command_list(file, "options", mkf.options)
    dump_mkf_command_list(file, "files", mkf.source_files)
    dump_mkf_command_list(file, "subprojects", mkf.subprojects)

    file.flush()
    file.close()

def process(mkf, path, dir_name_in):
    enum_dir = os.listdir(path)

    files = []
    dirs = []

    for file_name in enum_dir:
        full_name = os.path.join(path, file_name)
        
        if not os.path.isdir(full_name):
            file_ext = os.path.splitext(file_name)[1]
            if (file_ext == ".h") or (file_ext == ".cpp"):
                files.append(file_name)
        else:
            dirs.append(file_name)

    for file_name in files:
        mkf.source_files.append(file_name);

    for dir_name in dirs:
        full_name = os.path.join(path, dir_name)
        cur_path = os.path.join(dir_name_in, dir_name).replace("\\", "/")
        mkf.source_files.append("[" + cur_path + "]")
        mkf.source_files.append("(" + cur_path + ")")          
        process(mkf, full_name, cur_path)

def main():
    mkf = Mkf()
    mkf.path = "."
    mkf.name = "polyvox.mkf"
    mkf.includepaths.append("./PolyVoxCore/include")
    mkf.includepaths.append("./PolyVoxUtil/include")
    mkf.includepaths.append("./")

    mkf_files = []

    process(mkf, ".", "");

    create_mkf_file(".", mkf)

main()
