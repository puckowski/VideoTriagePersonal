package com.keypointforensics.videotriage.image.match;

import java.util.*;

public class ORBMatcher {

    public static class Match {
        public final OrbKeypoint kp1;
        public final OrbKeypoint kp2;
        public final int distance;

        public Match(OrbKeypoint kp1, OrbKeypoint kp2, int distance) {
            this.kp1 = kp1;
            this.kp2 = kp2;
            this.distance = distance;
        }
    }

    public static List<Match> matchWithoutCrossCheck(final List<OrbKeypoint> list1, final List<OrbKeypoint> list2) {
        List<Match> matches = new ArrayList<>();

        for (OrbKeypoint kp1 : list1) {
            OrbKeypoint bestKp = null;
            int bestDist = Integer.MAX_VALUE;

            for (OrbKeypoint kp2 : list2) {
                int dist = hammingDistance(kp1.descriptor, kp2.descriptor);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestKp = kp2;
                }
            }

            if (bestKp != null) {
                matches.add(new Match(kp1, bestKp, bestDist));
            }
        }

        return matches;
    }

    // Match descriptors between two lists using brute-force Hamming distance with cross-check
    public static List<Match> match(final List<OrbKeypoint> list1, final List<OrbKeypoint> list2) {
        List<Match> matches = new ArrayList<>();

        Map<OrbKeypoint, OrbKeypoint> forward = new HashMap<>();
        Map<OrbKeypoint, Integer> forwardDist = new HashMap<>();

        // Forward matches: list1 -> list2
        for (OrbKeypoint kp1 : list1) {
            OrbKeypoint bestKp = null;
            int bestDist = Integer.MAX_VALUE;

            for (OrbKeypoint kp2 : list2) {
                int dist = hammingDistance(kp1.descriptor, kp2.descriptor);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestKp = kp2;
                }
            }

            if (bestKp != null) {
                forward.put(kp1, bestKp);
                forwardDist.put(kp1, bestDist);
            }
        }

        // Cross-check: list2 -> list1
        for (OrbKeypoint kp2 : list2) {
            OrbKeypoint bestKp = null;
            int bestDist = Integer.MAX_VALUE;

            for (OrbKeypoint kp1 : list1) {
                int dist = hammingDistance(kp1.descriptor, kp2.descriptor);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestKp = kp1;
                }
            }

            if (bestKp != null && forward.get(bestKp) == kp2) {
                matches.add(new Match(bestKp, kp2, forwardDist.get(bestKp)));
            }
        }

        return matches;
    }

    private static int hammingDistance(byte[] a, byte[] b) {
        int dist = 0;
        for (int i = 0; i < a.length; i++) {
            dist += Integer.bitCount(a[i] ^ b[i] & 0xFF);
        }
        return dist;
    }
}