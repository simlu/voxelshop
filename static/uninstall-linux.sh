#!/bin/sh
# by Poikilos (Jake Gustafson)

me=`basename $0`
echo "Uninstalling VoxelShop..."

customDie() {
    echo
    echo "ERROR:"
    echo " $1"
    echo
    echo
    ret=1
    if [ ! -z $2 ]; then ret=$2; fi
    exit $ret
}

dest_dir_name=voxelshop
install_log_name=install.log

if [ -z "$PREFIX" ]; then
    if [ -f $HOME/.local/lib/$dest_dir_name/$install_log_name ]; then
        PREFIX=$HOME/.local
    elif [ -f /usr/local/lib/$dest_dir_name/$install_log_name ]; then
        PREFIX=/usr/local
    elif [ -f /usr/lib/$dest_dir_name/$install_log_name ]; then
        PREFIX=/usr
    fi
fi

dest_dir_path=$PREFIX/lib/$dest_dir_name

if [ -f "$install_log_name" ]; then
    # Detect PREFIX as whatever was used before since log is here:
    dest_dir_path=`pwd`
    PREFIX_lib=`dirname $dest_dir_path`
    PREFIX=`dirname $PREFIX_lib`
    echo "Using PREFIX $PREFIX automatically since $install_log_name"
    echo "  is in `pwd`"
fi

if [ ! -f "$dest_dir_path/$install_log_name" ]; then
    echo ""
    customDie "There is no $install_log_name at any known location."
fi
install_log="$dest_dir_path/$install_log_name"

echo "Processing install log..."
while read p; do
    if [ ! -z "$p" ]; then
        if [ -f "$p" ]; then
            rm -f "$p" && echo "rm -f $p" || echo "rm -f '$p'  # FAILED"
        elif [ ! -d "$p" ]; then
            echo "rm '$p'  # WARNING: neither a file nor a directory"
        fi
    fi
done <"$install_log"
echo "Finished processing install log..."
if [ -f "$install_log" ]; then
    rm "$install_log" || echo "WARNING: Cannot remove '$install_log'--you must remove it before attempting to reinstall."
else
    echo "WARNING: $install_log must have been listed as an installed"
    echo "  file in itself (this is not ideal, as entries after it may"
    echo "  have been lost, leading to incomplete uninstall)"
fi
rmdir "$PREFIX/lib/$dest_dir_name/share/applications"
rmdir "$PREFIX/lib/$dest_dir_name/share"
rmdir "$PREFIX/lib/$dest_dir_name/lib"
# echo "deleting uninstall.sh..."
# rm "$PREFIX/lib/$dest_dir_name/uninstall.sh"
rmdir "$PREFIX/lib/$dest_dir_name"
# Statements below won't do anything bad--they only remove dir if empty.
rmdir --ignore-fail-on-non-empty "$PREFIX/share/applications"
rmdir --ignore-fail-on-non-empty "$PREFIX/share/pixmaps"
rmdir --ignore-fail-on-non-empty "$PREFIX/share"
rmdir --ignore-fail-on-non-empty "$PREFIX/lib"
if [ -f "$HOME/.var/log/voxelshop/last_run.err" ]; then
    rm "$HOME/.var/log/voxelshop/last_run.err"
fi
if [ -f "$HOME/.var/log/voxelshop/last_run.log" ]; then
    rm "$HOME/.var/log/voxelshop/last_run.log"
fi
rmdir --ignore-fail-on-non-empty "$HOME/.var/log/voxelshop"
rmdir --ignore-fail-on-non-empty "$HOME/.var/log"
rmdir --ignore-fail-on-non-empty "$HOME/.var"

echo "Uninstall is complete."
