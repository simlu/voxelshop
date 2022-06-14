#!/bin/sh
# by Poikilos (Jake Gustafson)
me=`basename $0`
echo "Installing VoxelShop..."

customExit() {
    echo
    echo "ERROR:"
    echo " $1"
    echo
    echo
    code=1
    if [ ! -z $2 ]; then code=$2; fi
    exit $code
}

# region environment checks
if [ -z "$REINSTALL" ]; then
    REINSTALL=false
fi

for var in "$@"
do
    if [ "$var" = "--reinstall" ]; then
        REINSTALL=true
    fi
done

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
    if [ "$EUID" = "0" ]; then
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
programs_path=$PREFIX/lib
dest_dir_path=$programs_path/$dest_dir_name
install_src=`pwd`
lib_jar_name=voxelshop-start.jar
lib_jar_path=$install_src/$lib_jar_name
if [ -z "$BASH_VERSION" ]; then
    # Handle shells other than bash.
    SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
    echo "INFO: Bash is not being used, so \$0 will be used to determine SCRIPT_DIR=\"$SCRIPT_DIR\""
else
    echo "THIS_SHELL=$THIS_SHELL"
    echo "BASH_VERSION=$BASH_VERSION"
    SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]:-$0}"; )" &> /dev/null && pwd 2> /dev/null; )";\
fi
# ^ from https://stackoverflow.com/a/246128/4541104

if [ ! -f "$lib_jar_path" ]; then
    customExit "You must run $me in same path as built $lib_jar_name."
fi
icon_img_name=voxelshop.png
# ^ This (only) sets the destination name, therefore it must be unique.
icon_img_path=$install_src/share/pixmaps/icon-48x48.png

if [ ! -f "$icon_img_path" ]; then
    try_icon_path="$SCRIPT_DIR/share/pixmaps/icon-48x48.png"
    if [ -f "$try_icon_path" ]; then
        echo "INFO: $icon_img_path wasn't present, so $try_icon_path will be used instead."
        icon_img_path="$try_icon_path"
    else
        echo "WARNING: Neither $icon_img_path nor $try_icon_path exists."
    fi
fi
if [ ! -f "$icon_img_path" ]; then
    echo "WARNING: Missing '$icon_img_path' (shortcut will have no icon image)"
fi
shortcut_name=com.blackflux.voxelshop.desktop
shortcut_path=$install_src/share/applications/$shortcut_name
if [ ! -f "$shortcut_path" ]; then
    try_shortcut_path="$SCRIPT_DIR/share/applications/$shortcut_name"
    if [ -f "$try_shortcut_path" ]; then
        echo "INFO: $shortcut_path wasn't present, so $try_shortcut_path will be used instead."
        shortcut_path="$try_shortcut_path"
    else
        echo "WARNING: Neither $shortcut_path nor $try_shortcut_path exists."
    fi
fi
# endregion hardcoded settings

if [ ! -d $PREFIX/lib ]; then
    mkdir -p $PREFIX/lib || customExit "Cannot create $PREFIX/lib"
fi

if [ ! -d $PREFIX/bin ]; then
    mkdir -p $PREFIX/bin || customExit "Cannot create $PREFIX/bin"
fi

install_log_name=install.log
install_log=$dest_dir_path/$install_log_name
if [ -f "$install_log" ]; then
    # stopping in this case avoids creating a faulty install_log that
    # may contain files the user added to the directory.
    customExit "The program is already installed. Run uninstall-linux.sh (recommended) or delete $dest_dir_path before trying to reinstall (Deleting only $install_log is not recommended as that will cause the next install to log all files there, so after that, an uninstall would remove any files you may have put there)."
fi
if [ -d "$dest_dir_path" ]; then
    echo "WARNING: \"$dest_dir_path\" already exists but there is no install log"
    if [ "$REINSTALL" != "true" ]; then
        echo "  so nothing was done. To force reinstall, use the --reinstall option."
        exit 1
    else
        echo "  The directory will be upgraded (any prepackaged files that remain will be overwritten)."
    fi
fi

if [ ! -d "$dest_dir_path/lib" ]; then
    msg="Cannot create '$dest_dir_path/lib'"
    mkdir -p $dest_dir_path/lib || customExit "$msg"
fi

if [ ! -d "$dest_dir_path/share/applications" ]; then
    msg="Cannot create '$dest_dir_path/share/applications'"
    mkdir -p $dest_dir_path/share/applications || customExit "$msg"
fi


cp -R $install_src $programs_path/
# ^ dest_dir_path can't be used for cp -R, otherwise if it exists, the
#   folder will be copied inside the dest instead of as the dest.

find $dest_dir_path | grep -v "$install_log_name" > $install_log
if [ ! -f "$install_log" ]; then
    customExit "Cancelling install since cannot create $install_log."
fi

if [ ! -d "$dest_dir_path/lib" ]; then
    customExit "lib wasn't copied, so install could not complete."
fi

echo
if [ -f "$bin_name" ]; then
    echo "Rewriting $bin_name..."
else
    echo "Writing $bin_name..."
fi
echo '#!/bin/sh' > $PREFIX/bin/$bin_name
cat >>$PREFIX/bin/$bin_name <<END

JAVA_GUI_PACKAGES="openjdk-8-jre (Ubuntu), java-1.8.0-openjdk (Fedora)"
if [ ! -f "\`command -v java\`" ]; then
    xmessage "VoxelShop Error: Java was not found. Install \$JAVA_GUI_PACKAGES, or another version of Java with GUI support."
    exit 127  # 127 is for command not found.
fi
logs_dir="\$HOME/.var/log/voxelshop"
if [ -f "\$logs_dir/last_run.err" ]; then
    rm "\$logs_dir/last_run.err"
fi
mkdir -p "\$logs_dir"
java -jar $dest_dir_path/$lib_jar_name > \$logs_dir/last_run.log 2>&1
code=\$?
if [ \$code -ne 0 ]; then
    date > \$logs_dir/last_run.err
    echo "VoxelShop exited with error code \$code:" >> \$logs_dir/last_run.err

    echo >> \$logs_dir/last_run.err
    echo "Ensure you have a java package with GUI support such as:" >> \$logs_dir/last_run.err
    echo "\$JAVA_GUI_PACKAGES." >> $logs_dir/last_run.err
    echo "The exact error will be shown below" >> \$logs_dir/last_run.err
    echo "(ignore incorrect sRGB profile and canberra-gtk-module):" >> \$logs_dir/last_run.err

    echo >> \$logs_dir/last_run.err
    cat \$logs_dir/last_run.log >> \$logs_dir/last_run.err
    xmessage -file \$logs_dir/last_run.err
fi
END
# ^ NOTE: >& only works with bash, so use 1>&2
# ^ NOTE: any file(s) written above should also be placed in the uninstall script.
echo "$PREFIX/bin/$bin_name" >> $install_log
chmod +x $PREFIX/bin/$bin_name
if [ -f $shortcut_path ]; then
    tmp_shortcut=$dest_dir_path/share/applications/$shortcut_name
    if [ -f $tmp_shortcut ]; then
        rm $tmp_shortcut || customExit "Cannot remove old $tmp_shortcut"
    fi
    cat $shortcut_path | grep -v "Icon=" | grep -v "Exec=" | grep -v "Path=" > $tmp_shortcut
    echo "$tmp_shortcut" >> $install_log
    if [ ! -f $tmp_shortcut ]; then
        customExit "Cannot rewrite $tmp_shortcut."
    fi
    echo Exec=$PREFIX/bin/$bin_name >> $tmp_shortcut
    if [ -f $icon_img_path ]; then
        if [ ! -d $PREFIX/share/pixmaps ]; then
            mkdir -p $PREFIX/share/pixmaps || customExit "Cannot create $PREFIX/share/pixmaps"
        fi
        if [ ! -d $PREFIX/share/pixmaps ]; then
            echo "ERROR: cannot create $PREFIX/share/pixmaps, so not installing graphic for icon"
        else
            cp -f $icon_img_path $PREFIX/share/pixmaps/$icon_img_name
            echo Icon=$PREFIX/share/pixmaps/$icon_img_name >> $tmp_shortcut
        fi
        if [ ! -d $PREFIX/share/applications ]; then
            mkdir -p "$PREFIX/share/applications" || customExit "Cannot create $PREFIX/share/applications."
        fi
        if [ -f $PREFIX/share/applications/$shortcut_name ]; then
            rm -f $PREFIX/share/applications/$shortcut_name || customExit "Cannot remove old $PREFIX/share/applications/$shortcut_name."
        fi
        if [ -d $PREFIX/share/applications/$shortcut_name ]; then
            rm -f $PREFIX/share/applications/$shortcut_name || customExit "Cannot remove bogus folder (should be file): $PREFIX/share/applications/$shortcut_name"
        fi
        cp $tmp_shortcut $PREFIX/share/applications/$shortcut_name
        echo "$PREFIX/share/applications/$shortcut_name" >> $install_log
        echo "Writing shortcut '$PREFIX/share/applications/$shortcut_name' is complete."
        if [ "@$REFRESH_WM" = "@true" ]; then
            if [ "$EUID" != "0" ]]; then
                THIS_DE=
                # See <https://unix.stackexchange.com/a/645761/343286>
                # regarding the old and new ways of checking what DE
                # is running.
                if [ ! -z "$XDG_SESSION_DESKTOP" ]; then
                    THIS_DE=`echo "$XDG_SESSION_DESKTOP" | tr '[:upper:]' '[:lower:]'`
                elif [ ! -z "$XDG_CURRENT_DESKTOP" ]; then
                    THIS_DE=`echo "$XDG_CURRENT_DESKTOP" | tr '[:upper:]' '[:lower:]'`
                fi
                echo "THIS_DE=$THIS_DE"
                if [ "$THIS_DE" = "kde-plasma" ]; then
                    THIS_DE="kde"
                elif [ "$THIS_DE" = "xfce4" ]; then
                    THIS_DE="xfce"
                fi

                # if [ -f "`command -v gnome-shell`" ]; then
                if [ "$THIS_DE" = "gnome" ]; then
                    echo "* refreshing Gnome icons..."
                    gnome-shell --replace & disown
                    sleep 10
                # if [ -f "`command -v kquitapp5`" ]; then
                elif [ "$THIS_DE" = "kde" ]; then
                    if [ -f "$HOME/.cache/icon-cache.kcache" ]; then
                        echo "* clearing $HOME/.cache/icon-cache.kcache..."
                        rm $HOME/.cache/icon-cache.kcache
                    fi
                    echo "* refreshing KDE icons..."
                    if [ "`command -v kstart5`" ]; then
                        kquitapp5 plasmashell && kstart5 plasmashell && sleep 15 || echo " - skipping plasmashell icon refresh (session not loaded)"
                    else
                        kquitapp5 plasmashell && kstart plasmashell && sleep 15 || echo " - skipping plasmashell icon refresh (session not loaded)"
                    fi
                # if [ -f "`command -v xfce4-panel`" ]; then
                elif [ "$THIS_DE" = "xfce" ]; then
                    echo "* refreshing Xfce icons..."
                    xfce4-panel -r && xfwm4 --replace & disown
                    sleep 5
                # if [ -f "`command -v lxpanelctl`" ]; then
                elif [ "$THIS_DE" = "lxde" ]; then
                    echo "* refreshing LXDE icons..."
                    lxpanelctl restart && openbox --restart & disown
                    sleep 5
                # if [ -f "`command -v lxqt-panel`" ]; then
                elif [ "$THIS_DE" = "lxqt" ]; then
                    echo "* refreshing LXQt icons..."
                    killall lxqt-panel && lxqt-panel & disown
                elif [ "$THIS_DE" = "mate" ]; then
                    echo
                    echo "If the icon doesn't appear, logout or try the following in a terminal:"
                    echo "  mate-panel --replace & disown"
                    echo
                    # sleep 3
                else
                    echo "WARNING: You may have to restart your panel to see the new shortcut. Your desktop environment \"$THIS_DE\" is unknown, so please report the issue along with this full message."
                fi
                # ^ Sleep commands ensure that the panel gets through its startup phase so console spam from the panel doesn't appear after the "Install is complete" message shown below.

            else
               echo "Since running as root, this script will not update application menus for any currently logged in user(s)."
            fi
        fi
    else
        echo "WARNING: cannot add graphical icon to shortcut since missing $icon_img_path."
    fi
else
    echo "WARNING: cannot add shortcut since missing $shortcut_path."
fi
echo "Install is complete."

