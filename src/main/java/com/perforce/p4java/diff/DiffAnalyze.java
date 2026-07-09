package com.perforce.p4java.diff;

import com.perforce.p4java.Log;
import com.perforce.p4java.exception.FileEncoderException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DiffAnalyze {

    static int P4TUNE_DIFF_STHRESH = 50000;
    static int P4TUNE_DIFF_SLIMIT1 = 10000000;
    static int P4TUNE_DIFF_SLIMIT2 = 100000000;
    static int DEBUG_LEVEL = 0;

    static class SymmetricVector {

        public SymmetricVector() {
            halfSize = 0;
            vec = null;
        }

        int Get(int k) {
            return vec[halfSize + k];
        }

        void Set(int k, int v) {
            vec[halfSize + k] = v;
        }

        void Resize(int newHalfSize) {
            halfSize = newHalfSize;
            vec = new int[halfSize * 2 + 1];
        }

        void Dump(int d) {
            StringBuilder sb = new StringBuilder("index: ");
            int i;
            for (i = -d; i <= d; i += 2)
                sb.append(String.format("%3d ", i));
            sb.append("\nline:  ");
            for (i = -d; i <= d; i += 2) {
                sb.append(String.format("%3d ", Get(i)));
            }
            sb.append("\n");
            Log.info("%s", sb.toString());
        }

        private int halfSize;
        private int[] vec;

    }

    public DiffAnalyze(ISequence fromFile, ISequence toFile) throws IOException, FileEncoderException {
        Init(fromFile, toFile, 0);
    }

    public DiffAnalyze(ISequence fromFile, ISequence toFile, int fastMaxD) throws IOException, FileEncoderException {
        Init(fromFile, toFile, fastMaxD);
    }

    private void Init(ISequence fromFile, ISequence toFile, int fastMaxD) throws IOException, FileEncoderException {
        a = fromFile;
        b = toFile;

        // Calculate a limit on the amount of searching FindSnake does, so as to have
        // a reasonable upper bound on compute time (and space, although that's less relevant;
        // it's only 2*maxD elements of length LineNo, i.e. typically 8*maxD bytes).
        // FindSnake does a two-sided search, -D to +D, hence the variable name maxD
        // and its default value of half the sum of the number of lines in the two files.
        // With that value maxD does not limit the search in FindSnake.
        // If this value is beyond what results in a "reasonable" compute time
        // on modern hardware, then maxD is adjusted downwards, trading off potentially
        // poorer diff output for a reasonable running time.  The way to determine
        // the constants used here is to determine the compute time given the
        // worst case inputs, which are two files which are completely different.

        // maxD calculation change
        //
        // This new calculation reduces maxD for bigger files.
        //
        // Added sLimit:
        //
        // "sLimit" is used to control the value of maxD so that as avgLines
        // becomes large maxD begins to tail off (reducing search time).
        // For RCS files we need maxD to tail off more rapidly as its important
        // that submit/checkin happens as quick as possible.
        //
        // An upper limit of 50000 lines of code has also been factored in to
        // cause maxD to significantly reduce the searching that FindSnake
        // will do.

        int avgLines = (a.Lines() + b.Lines()) / 2;

        final int sThresh = P4TUNE_DIFF_STHRESH;
        final int sLimit1 = P4TUNE_DIFF_SLIMIT1;
        final int sLimit2 = P4TUNE_DIFF_SLIMIT2;

        final int sLimit =
                (fastMaxD == 0 && avgLines < sThresh)
                        ? sLimit2 : sLimit1;

        maxD = sLimit / (avgLines != 0 ? avgLines : 1);

        if (maxD > avgLines)
            maxD = avgLines;

        if (maxD < 42)
            maxD = 42;

        // fV and rV are shared by all invocations of FindSnake()
        // This is required in order for the algorithm to take O(D) space
        // rather than O(D^2).  (See Myers p264.)  Thus we just make fV and
        // rV large enough to handle the worst case, then leave 'em alone.

        fV.Resize(maxD);
        rV.Resize(maxD);

        firstSnake = lastSnake = null;

        if (DEBUG_LEVEL > 0) {
            Log.info(String.format("N %d M %d maxD %d", a.Lines(), b.Lines(), maxD));
        }

        // find the longest common subsequence - note that there will
        // never be a common subsequence if one of the files is empty,
        // so don't call LCS in this case (this is not just an optimization,
        // this is necessary for correctness!)

        if (a.Lines() > 0 && b.Lines() > 0)
            LCS(0, 0, a.Lines(), b.Lines());

        // Free vectors now that we will not need them anymore
        fV.Resize(0);
        rV.Resize(0);

        // Ensure there's a snake at the beginning by adding a zero-length
        // snake if necessary - this makes life easier for the code
        // which processes the list of snakes.  It also is used by
        // ApplyForwardBias()
        BracketSnake();

        // Select optimal output which is of a canonical form so that
        // the 3 way merge algorithm, which compares diff outputs,
        // does not see any bogus conflicts.
        // The term "forward bias" is intended to mean that given a choice
        // between two optimal outputs the result will be the one which
        // you would find by a forward search, rather than a reverse search.
        ApplyForwardBias();
    }

    public ISequence GetFromFile() {
        return a;
    }

    public ISequence GetToFile() {
        return b;
    }

    public Snake GetSnake() {
        return firstSnake;
    }


    private int maxD;
    private ISequence a;
    private ISequence b;

    private Snake firstSnake;
    private Snake lastSnake;

    private SymmetricVector fV = new SymmetricVector();
    private SymmetricVector rV = new SymmetricVector();

    private void BracketSnake() {
        Snake s;

        // If the first snake doesn't include the first lines of
        // both files, make a "null" snake for the beginning.
        // Note that if there is no snake (files completely unmatched)
        // we'll add a start snake anyhow.

        s = firstSnake;

        if (s == null || s.x.get() != 0 || s.y.get() != 0) {
            Snake toadd = new Snake();

            toadd.x.set(0);
            toadd.u.set(0);
            toadd.y.set(0);
            toadd.v.set(0);
            toadd.next = s;

            if (s == null)
                lastSnake = toadd;

            firstSnake = toadd;
        }

        // If the last snake doesn't include the last lines of
        // both files, make a "null" snake for the end.
        // If we added a start snake for completely unmatched files
        // above, then we'll only wind up adding a tailing snake
        // if the files are not both empty.

        s = lastSnake;

        if (s.u.get() < a.Lines() || s.v.get() < b.Lines()) {
            Snake toadd = new Snake();

            toadd.x.set(a.Lines());
            toadd.u.set(a.Lines());
            toadd.y.set(b.Lines());
            toadd.v.set(b.Lines());
            toadd.next = null;

            s.next = toadd;
            lastSnake = toadd;
        }
    }

    private void ApplyForwardBias() throws IOException, FileEncoderException {
        Snake s, next;
        int endx = a.Lines();
        int endy = b.Lines();

        // Traverse the list of snakes, extending any which can be extended.
        // If the current snake extends into the next snake, then that snake
        // is shortened.  If the second snake is totally consumed then it is
        // removed.

        for (s = firstSnake, next = s.next; next != null; s = next, next = next.next) {
            while (s.u.get() < endx && s.v.get() < endy && a.Equal(s.u.get(), b, s.v.get())) {
                // start stretching current chunk

                s.u.incrementAndGet();
                s.v.incrementAndGet();

                // if we are in an adjacent chunk then start compacting it

                if (s.u.get() > next.x.get() || s.v.get() > next.y.get()) {

                    next.x.incrementAndGet();
                    next.y.incrementAndGet();

                    // if its totally consumed remove it (unless its the last).

                    if (next.x.get() == next.u.get() && next != lastSnake) {
                        // handle special case of snake completely eating up the
                        // next snake - but don't delete the last snake!

                        s.next = next.next;
                        next = s.next;
                    }
                }
            }
        }
    }

    private void FollowDiagonal(
            AtomicInteger x, AtomicInteger y,
            int endx, int endy) {
        while (x.get() < endx && y.get() < endy && a.ProbablyEqual(x.get(), b, y.get())) {
            x.incrementAndGet();
            y.incrementAndGet();
        }
    }

    private void FollowReverseDiagonal(
            AtomicInteger x, AtomicInteger y,
            int startx, int starty) {
        while (x.get() > startx && y.get() > starty && a.ProbablyEqual(x.get() - 1, b, y.get() - 1)) {
            x.decrementAndGet();
            y.decrementAndGet();
        }
    }

    private void FindSnake(Snake s,
                           int startx, int starty,
                           int endx, int endy) {
        final int n = endx - startx;
        final int m = endy - starty;
        final int delta = n - m;

        // note that ==1 wouldn't work (-1%2==-1)
        final boolean deltaEven = ((delta % 2) == 0);

        if (DEBUG_LEVEL > 2) {
            Log.info(String.format("FindSnake(%d,%d,%d,%d)", startx, starty, endx, endy));
            if (n <= 0 || m <= 0 || maxD <= 0) {
                throw new RuntimeException("N<=0 or M<=0 or maxD<=0 - that's not good!");
            }
        }

        // initialize values for D=0

        s.x.set(startx);
        s.u.set(startx);
        fV.Set(0, startx);
        s.y.set(starty);
        s.v.set(starty);

        // advances s.u,s.v to end of snake
        FollowDiagonal(s.u, s.v, endx, endy);

        // Heuristic: return immediately with initial prefix

        if (s.u.get() > s.x.get()) {
            if (DEBUG_LEVEL > 2) {
                Log.info(String.format("FindSnake returning with initial prefix %d to %d", s.x.get(), s.u.get()));
            }
            return;
        }

        s.u.set(endx);
        s.x.set(endx);
        rV.Set(0, endx);
        s.y.set(endy);
        s.v.set(endy);

        // s.x,s.y set to start of snake
        FollowReverseDiagonal(s.x, s.y, startx, starty);

        // Heuristic: return immediately with initial suffix

        if (s.u.get() > s.x.get()) {
            if (DEBUG_LEVEL > 2) {
                Log.info(String.format("FindSnake returning with initial suffix %d to %d", s.x.get(), s.u.get()));
            }
            return;
        }

        for (int d = 1; d <= maxD; d++) {

            final int minkF = (d <= m) ? -d : -(2 * m - d);
            final int maxkF = (d <= n) ? d : 2 * n - d;
            final int minkR = -maxkF;
            final int maxkR = -minkF;
            int k;

            if (DEBUG_LEVEL > 3) {
                Log.info(String.format("D=%d Forward min,max (%d,%d) Reverse min,max (%d,%d)", d, minkF, maxkF, minkR, maxkR));
            }

            // search from top left of edit graph ("forward")

            for (k = minkF; k <= maxkF; k += 2) {

                if (k == minkF || (k != maxkF && fV.Get(k + 1) > fV.Get(k - 1)))
                    s.x.set(fV.Get(k + 1));   // down in edit graph
                else
                    s.x.set(fV.Get(k - 1) + 1); // to the right in edit graph

                // Follow the (possibly 0 length) diagonal in the
                // edit graph (aka snake)

                s.u.set(s.x.get());
                s.v.set(mapxtoy(s.u.get(), k, startx, starty));

                // advances s.u,s.v to end of snake
                FollowDiagonal(s.u, s.v, endx, endy);

                if (!deltaEven) { // Delta odd

                    // check if path overlaps end of furthest reaching
                    // reverse (D-1)-path

                    final int dr = d - 1;
                    final int pminkR = (dr <= n) ? -dr : -(2 * n - dr);
                    final int pmaxkR = (dr <= m) ? dr : 2 * m - dr;

                    if (DEBUG_LEVEL > 3) {
                        Log.info(String.format("DR=%d Reverse min,max (%d,%d)", dr, pminkR, pmaxkR));
                    }

                    if (k - delta >= pminkR && k - delta <= pmaxkR) {
                        if (DEBUG_LEVEL > 3) {
                            Log.info(String.format("s.u %d rv[%d] %d", s.u.get(), k - delta, rV.Get(k - delta)));
                        }
                        if (s.u.get() >= rV.Get(k - delta)) {
                            // fill in the fields of s which haven't
                            // already been filled in

                            s.y.set(mapxtoy(s.x.get(), k, startx, starty));
                            if (DEBUG_LEVEL > 2) {
                                Log.info(String.format("FindSnake returning during forward search (%d,%d) to (%d,%d)", s.x.get(), s.y.get(), s.u.get(), s.v.get()));
                            }
                            return; // finished!
                        }
                    }
                }

                fV.Set(k, s.u.get()); // ok, now set it to the end of the snake
            }

            if (DEBUG_LEVEL > 2) {
                Log.info(String.format("Forward D=%d", d));
                fV.Dump(d);
            }

            // search from bottom right of edit graph ("reverse")

            for (k = minkR; k <= maxkR; k += 2) {

                if (k == maxkR || (k != minkR && rV.Get(-1) < rV.Get(k + 1)))
                    s.u.set(rV.Get(k - 1)); // up
                else
                    s.u.set(rV.Get(k + 1) - 1); // to the left

                s.x.set(s.u.get());
                s.y.set(mapxtoy(s.x.get(), k, endx, endy));
                FollowReverseDiagonal(s.x, s.y, startx, starty);

                // check if path overlaps end of furthest reaching
                // forward D-path

                if (deltaEven) {

                    if (k + delta >= minkF && k + delta <= maxkF) {
                        if (DEBUG_LEVEL > 3) {
                            Log.info(String.format("s.x %d fV[%d] %d", s.x.get(), k + delta, fV.Get(k + delta)));
                        }
                        if (s.x.get() <= fV.Get(k + delta)) {
                            // fill in the fields of s which haven't
                            // already been filled in
                            s.v.set(mapxtoy(s.u.get(), k, endx, endy));
                            if (DEBUG_LEVEL > 2) {
                                Log.info(String.format("FindSnake returning during reverse search (%d,%d) to (%d,%d)", s.x.get(), s.y.get(), s.u.get(), s.v.get()));
                            }
                            return; // finished!
                        }
                    }
                }
                rV.Set(k, s.x.get()); // ok, now set it to the end of the snake
            }

            if (DEBUG_LEVEL > 2) {
                Log.info(String.format("Reverse D=%d", d));
                rV.Dump(d);
            }
        }

        // we only get to this point if we've failed to make ends meet within
        // maxD iterations.  Just return the midpoint so that we're sure we're
        // doing log N rather than N.  If we're fortunate enough to have
        // stumbled upon a snake in the middle, then extend it to its
        // maximum length.

        s.u.set(startx + (endx - startx) / 2);
        s.x.set(s.u.get());
        s.v.set(starty + (endy - starty) / 2);
        s.y.set(s.v.get());

        if (DEBUG_LEVEL > 2) {
            Log.info(String.format("exceeded maxD midpt (%d,%d)", s.x.get(), s.y.get()));
        }

        FollowReverseDiagonal(s.x, s.y, startx, starty);
        FollowDiagonal(s.u, s.v, endx, endy);

        if (DEBUG_LEVEL > 2) {
            Log.info(String.format("after extending: (%d,%d) to (%d,%d)", s.x.get(), s.y.get(), s.u.get(), s.v.get()));
        }
    }

    static private int
    mapxtoy(int x,
            int diagonal,
            int originx,
            int originy) {
        return x - (diagonal + (originx - originy));
    }

    private void LCS(
            final int startx, final int starty,
            final int endx, final int endy) throws IOException, FileEncoderException {
        Snake s = new Snake();
        FindSnake(s, startx, starty, endx, endy); // s is a reference parameter for speed

        if (s.x.get() > startx && s.y.get() > starty) {

            if (DEBUG_LEVEL > 0) {
                Log.info(String.format("LCS %d , %d , %d , %d", startx, starty, s.x.get(), s.y.get()));
                if (s.x.get() == endx && s.y.get() == endy) {
                    throw new RuntimeException("INFINITE RECURSION!");
                }
            }
            LCS(startx, starty, s.x.get(), s.y.get());
        }

        if (s.u.get() > s.x.get()) { // snake has nonzero length

            int snakes_added = 0;
            if (DEBUG_LEVEL > 1) {
                Log.info(String.format("SNAKE (%d,%d) to (%d,%d)", s.x.get(), s.y.get(), s.u.get(), s.v.get()));
            }
            // verify actual sequence contents, splitting snake up if there 
            // are any pairs of lines which are ProbablyEqual() but not Equal()

            int cx, cy;
            for (cx = s.x.get(), cy = s.y.get(); cx < s.u.get(); cx++, cy++) {

                s.x.set(cx);
                s.y.set(cy);
                while (cx < s.u.get() && a.Equal(cx, b, cy)) {
                    cx++;
                    cy++;
                }

                if (cx > s.x.get()) { // nonzero length snake to add
                    Snake toadd = new Snake();

                    toadd.next = null;
                    toadd.x.set(s.x.get());
                    toadd.y.set(s.y.get());
                    toadd.u.set(cx);
                    toadd.v.set(cy);

                    if (DEBUG_LEVEL > 1) {
                        snakes_added++;
                        Log.info(String.format("Adding snake (%d,%d) to (%d,%d)", toadd.x.get(), toadd.y.get(), toadd.u.get(), toadd.v.get()));
                    }

                    // add snake to end of linked list

                    if (firstSnake != null) {
                        // already found first one
                        lastSnake.next = toadd;
                        lastSnake = toadd;
                    } else {
                        firstSnake = lastSnake = toadd;
                    }
                }
            }
            if (DEBUG_LEVEL > 1) {
                if (snakes_added == 0)
                    Log.info("SNAKE WAS COMPLETELY BOGUS!!!");
                if (snakes_added > 1)
                    Log.info(String.format("Snake was broken into %d parts!", snakes_added));
            }
        }

        if (endx > s.u.get() && endy > s.v.get()) {

            if (DEBUG_LEVEL > 0) {
                Log.info(String.format("LCS %d , %d , %d , %d", s.u.get(), s.v.get(), endx, endy));
                if (s.u.get() == startx && s.v.get() == starty) {
                    throw new RuntimeException("INFINITE RECURSION!");
                }
            }

            LCS(s.u.get(), s.v.get(), endx, endy);
        }
    }
}
