package magicmod.common.util;

import java.util.Random;

public interface Curve {

    double getValue(Random rng);

    static Curve normal(double min, double max, double mean, double stddev) {
        return rng -> DataUtils.clamp(mean + stddev * rng.nextGaussian(), min, max);
    }
}
