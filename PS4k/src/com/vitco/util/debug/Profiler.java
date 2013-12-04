package com.vitco.util.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Allows profiling of certain code segments.
 */
public class Profiler {
    // constructor
    public Profiler() {
    }

    // --------------------------

    // profile class
    private class Profile {
        private final String name;
        private Profile(String name) {
            this.name = name;
        }
        // true iff profile is currently active
        private boolean active = false;

        // activation count
        private int count = 0;

        // activation time
        private long time = 0;

        // start time
        private long startTime = 0;

        public final void activate() {
            if (!active) {
                active = true;
                startTime = System.currentTimeMillis();
            } else {
                System.err.println("Profile already active!");
            }
        }

        public final void deactivate() {
            if (active) {
                active = false;
                time += (System.currentTimeMillis() - startTime);
                count++;
            } else {
                System.err.println("Profile already inactive!");
            }
        }

        public final long getTime() {
            if (count > 0) {
                return (time/count);
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return name + " : " + time + " / " + count + " = " + getTime();
        }
    }

    // list of all profiles
    private final HashMap<String, Profile> profiles = new HashMap<String, Profile>();

    // --------------------------

    public final void createProfile(String id, String name) {
        if (!profiles.containsKey(id)) {
            profiles.put(id, new Profile(name));
        } else {
            System.err.println("Profile \"" + id + "\" already defined.");
        }
    }

    public final void activateProfile(String id) {
        Profile profile = profiles.get(id);
        if (profile != null) {
            profile.activate();
        } else {
            System.err.println("Unknown Profile \"" + id + "\".");
        }
    }

    public final void deactivateProfile(String id) {
        Profile profile = profiles.get(id);
        if (profile != null) {
            profile.deactivate();
        } else {
            System.err.println("Unknown Profile \"" + id + "\".");
        }
    }

    // --------------------------

    // print all info
    public final void print() {
        ArrayList<Profile> list = new ArrayList<Profile>();
        list.addAll(profiles.values());
        Collections.sort(list, new Comparator<Profile>() {
            @Override
            public int compare(Profile o1, Profile o2) {
                return (int)Math.signum(o1.getTime() - o2.getTime());
            }
        });
        for (Profile profile : list) {
            System.out.println(profile);
        }
        System.out.println("");
    }


}
