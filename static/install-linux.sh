#!/bin/sh
# by Poikilos (Jake Gustafson)
me=`basename $0`
echo "Installing VoxelShop..."

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

# region environment checks

if [ -z "$REFRESH_WM" ]; then
    REFRESH_WM=true
    echo "REFRESH_WM not set, so using: '$REFRESH_WM'"
elif [ "@$REFRESH_WM" = "@TRUE" ]; then
    REFRESH_WM=true
elif [ "@$REFRESH_WM" = "@1" ]; then
    REFRESH_WM=true
elif [ "@$REFRESH_WM" = "@yes" ]; then
    REFRESH_WM=true
elif [ "@$REFRESH_WM" = "@on" ]; then
    REFRESH_WM=true
fi
if [ -z "$PREFIX" ]; then
    if [[ $EUID = 0 ]]; then
        PREFIX=/usr/local
        echo "PREFIX not set, so using '$PREFIX'"
    else
        PREFIX=$HOME/.local
        echo "PREFIX not set, and you're not root, so using '$PREFIX'"
    fi
else
    echo "Installing to specified PREFIX '$PREFIX'..."
fi

if [ ! -d "$PREFIX" ]; then
    echo "WARNING: PREFIX $PREFIX not a directory; continuing anyway..."
    echo "Press Ctrl+C to cancel..."
    sleep 1
    echo "3..."
    sleep 1
    echo "2..."
    sleep 1
    echo "1..."
    sleep 1
fi

# endregion environment checks

# region hardcoded settings

bin_name=voxelshop
dest_dir_name=voxelshop
dest_dir_path=$PREFIX/lib/$dest_dir_name
install_src=`pwd`
lib_jar_name=voxelshop-start.jar
lib_jar_path=$install_src/$lib_jar_name

if [ ! -f "$lib_jar_path" ]; then
    customDie "You must run $me in same path as built $lib_jar_name."
fi
icon_img_name=voxelshop.png
icon_img_path=$install_src/share/pixmaps/icon-48x48.png
if [ ! -f "$icon_img_path" ]; then
    echo "WARNING: Missing '$icon_img_path' (shortcut will have no icon image)"
fi
shortcut_name=com.blackflux.voxelshop.desktop
shortcut_path=$install_src/share/applications/$shortcut_name

# endregion hardcoded settings

if [ ! -d $PREFIX/lib ]; then
    mkdir -p $PREFIX/lib || customDie "Cannot create $PREFIX/lib"
fi

if [ ! -d $PREFIX/bin ]; then
    mkdir -p $PREFIX/bin || customDie "Cannot create $PREFIX/bin"
fi

if [ ! -d "$dest_dir_path/lib" ]; then
    msg="Cannot create '$dest_dir_path/lib'"
    mkdir -p $dest_dir_path/lib || customDie "$msg"
fi

if [ ! -d "$dest_dir_path/share/applications" ]; then
    msg="Cannot create '$dest_dir_path/share/applications'"
    mkdir -p $dest_dir_path/share/applications || customDie "$msg"
fi
install_log_name=install.log
install_log=$dest_dir_path/$install_log_name
if [ -f "$install_log" ]; then
    # stopping in this case avoids creating a faulty install_log that
    # may contain files the user added to the directory.
    customDie "The program is already installed. Run uninstall-linux.sh or delete $dest_dir_path before trying to reinstall."
fi
cp -f $install_src/* $dest_dir_path/
find $dest_dir_path | grep -v "$install_log_name" > $install_log
if [ ! -f "$install_log" ]; then
    customDie "Cancelling install since cannot create $install_log."
fi
cp -f $install_src/lib/* $dest_dir_path/lib/
find $dest_dir_path | grep -v "$install_log_name" >> $install_log
echo
if [ -f "$bin_name" ]; then
    echo "Rewriting $bin_name..."
else
    echo "Writing $bin_name..."
fi
echo '#!/bin/sh' > $PREFIX/bin/$bin_name
echo "java -jar $dest_dir_path/$lib_jar_name || notify-send \"Install openjdk-8-jre (Ubuntu), java-1.8.0-openjdk (Fedora), or another version of Java with GUI support.\"" >> $PREFIX/bin/$bin_name
echo "$PREFIX/bin/$bin_name" >> $install_log
chmod +x $PREFIX/bin/$bin_name
if [ -f $shortcut_path ]; then
    #tmp_shortcut=/tmp/$USER$shortcut_name
    tmp_shortcut=$dest_dir_path/share/applications/$shortcut_name
    if [ -f $tmp_shortcut ]; then
        rm $tmp_shortcut || customDie "Cannot remove old $tmp_shortcut"
    fi
    cat $shortcut_path | grep -v "Icon=" | grep -v "Exec=" | grep -v "Path=" > $tmp_shortcut
    echo "$tmp_shortcut" >> $install_log
    if [ ! -f $tmp_shortcut ]; then
        customDie "Cannot rewrite $tmp_shortcut."
    fi
    echo Exec=$PREFIX/bin/$bin_name >> $tmp_shortcut
    if [ -f $icon_img_path ]; then
        if [ ! -d $PREFIX/share/pixmaps ]; then
            mkdir -p $PREFIX/share/pixmaps || customDie "Cannot create $PREFIX/share/pixmaps"
        fi
        if [ ! -d $PREFIX/share/pixmaps ]; then
            echo "ERROR: cannot create $PREFIX/share/pixmaps, so not installing graphic for icon"
        else
            cp -f $icon_img_path $PREFIX/share/pixmaps/$icon_img_name
            echo Icon=$PREFIX/share/pixmaps/$icon_img_name >> $tmp_shortcut
        fi
        if [ ! -d $PREFIX/share/applications ]; then
            mkdir -p "$PREFIX/share/applications" || customDie "Cannot create $PREFIX/share/applications."
        fi
        if [ -f $PREFIX/share/applications/$shortcut_name ]; then
            rm -f $PREFIX/share/applications/$shortcut_name || customDie "Cannot remove old $PREFIX/share/applications/$shortcut_name."
        fi
        if [ -d $PREFIX/share/applications/$shortcut_name ]; then
            rm -f $PREFIX/share/applications/$shortcut_name || customDie "Cannot remove bogus folder (should be file): $PREFIX/share/applications/$shortcut_name"
        fi
        cp $tmp_shortcut $PREFIX/share/applications/$shortcut_name
        echo "$PREFIX/share/applications/$shortcut_name" >> $install_log
        echo "Writing shortcut '$PREFIX/share/applications/$shortcut_name' is complete."
        if [ "@$REFRESH_WM" = "@true" ]; then
            if [[ $EUID -ne 0 ]]; then
                if [ -f "`command -v gnome-shell`" ]; then
                    echo "* refreshing Gnome icons..."
                    gnome-shell --replace & disown
                    sleep 10
                fi
                if [ -f "$HOME/.cache/icon-cache.kcache" ]; then
                    echo "* clearing $HOME/.cache/icon-cache.kcache..."
                    rm $HOME/.cache/icon-cache.kcache
                fi
                if [ -f "`command -v kquitapp5`" ]; then
                    echo "* refreshing KDE icons..."
                    if [ "`command -v kstart5`" ]; then
                        kquitapp5 plasmashell && kstart5 plasmashell && sleep 15 || echo " - skipping plasmashell icon refresh (session not loaded)"
                    else
                        kquitapp5 plasmashell && kstart plasmashell && sleep 15 || echo " - skipping plasmashell icon refresh (session not loaded)"
                    fi
                fi
                if [ -f "`command -v xfce4-panel`" ]; then
                    echo "* refreshing Xfce icons..."
                    xfce4-panel -r && xfwm4 --replace
                    sleep 5
                fi
                if [ -f "`command -v lxpanelctl`" ]; then
                    echo "* refreshing LXDE icons..."
                    lxpanelctl restart && openbox --restart
                    sleep 5
                fi
                if [ -f "`command -v lxqt-panel`" ]; then
                    echo "* refreshing LXQt icons..."
                    killall lxqt-panel && lxqt-panel &
                fi
            else
               echo "Since running as root, this script will not update application menus for any currently logged in user(s)."
            fi
        fi
    else
        echo "WARNING: cannot add graphical icon to shortcut since missing $install_src/$icon_img_name."
    fi
else
    echo "WARNING: cannot add shortcut since missing $shortcut_path."
fi

echo "Install is complete."
